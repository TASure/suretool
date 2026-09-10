/*
 * Copyright (c) 2026 suretool contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sure.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 加解密工具类（AES/GCM/NoPadding，认证加密），无状态静态方法、线程安全，参考 Hutool 的 {@code Aes} 设计。
 *
 * <p>安全说明：
 * <ul>
 *   <li>采用 GCM 认证加密：密文自带完整性校验，可防篡改（替代旧版 CBC + 无认证，消除 padding oracle 风险）；</li>
 *   <li>每次加密使用 12 字节随机 IV（SecureRandom），密文格式为 {@code IV || ciphertext+tag}，解密自动解析；</li>
 *   <li>密钥派生使用 SHA-256 截取 16 字节（替代不安全的 MD5）。</li>
 * </ul>
 *
 * @author suretool
 * @since 0.1.0
 */
public class AesUtil {

	/** 变换算法：GCM 认证加密 */
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";

	/** GCM 标准随机 IV 长度（12 字节） */
	private static final int IV_LENGTH = 12;

	/** GCM 认证标签长度（128 位） */
	private static final int TAG_LENGTH_BITS = 128;

	/** 随机源 */
	private static final SecureRandom RANDOM = new SecureRandom();

	private AesUtil() {
	}

	/**
	 * 生成随机 AES 密钥（Base64 编码，32 字节 = AES-256）。
	 *
	 * @return Base64 密钥
	 */
	public static String generateKey() {
		byte[] key = new byte[32];
		RANDOM.nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}

	/**
	 * AES 加密为十六进制密文（自动携带随机 IV）。
	 *
	 * @param data 明文
	 * @param key  密钥（任意长度，SHA-256 派生）
	 * @return 十六进制密文
	 */
	public static String encryptHex(String data, String key) {
		return HexUtil.encodeHexStr(encrypt(data.getBytes(StandardCharsets.UTF_8), key));
	}

	/**
	 * AES 解密十六进制密文。
	 *
	 * @param hex 十六进制密文（含 IV）
	 * @param key 密钥
	 * @return 明文
	 */
	public static String decryptHex(String hex, String key) {
		return new String(decrypt(HexUtil.decodeHex(hex), key), StandardCharsets.UTF_8);
	}

	/**
	 * AES 加密为 Base64 密文。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return Base64 密文
	 */
	public static String encryptBase64(String data, String key) {
		return Base64.getEncoder().encodeToString(encrypt(data.getBytes(StandardCharsets.UTF_8), key));
	}

	/**
	 * AES 解密 Base64 密文。
	 *
	 * @param base64 Base64 密文
	 * @param key    密钥
	 * @return 明文
	 */
	public static String decryptBase64(String base64, String key) {
		return new String(decrypt(Base64.getDecoder().decode(base64), key), StandardCharsets.UTF_8);
	}

	/**
	 * AES-GCM 加密（字节）。
	 *
	 * @param data 明文
	 * @param key  密钥字符串
	 * @return 密文（IV || ciphertext+tag）
	 */
	public static byte[] encrypt(byte[] data, String key) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			RANDOM.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, buildKey(key), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			byte[] body = cipher.doFinal(data);
			byte[] result = new byte[IV_LENGTH + body.length];
			System.arraycopy(iv, 0, result, 0, IV_LENGTH);
			System.arraycopy(body, 0, result, IV_LENGTH, body.length);
			return result;
		} catch (Exception e) {
			throw new CryptoException("AES 加密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * AES-GCM 解密（字节）。
	 *
	 * @param data 密文（IV || ciphertext+tag）
	 * @param key  密钥字符串
	 * @return 明文
	 */
	public static byte[] decrypt(byte[] data, String key) {
		try {
			if (data == null || data.length <= IV_LENGTH) {
				throw new CryptoException("密文长度非法（缺少 IV）");
			}
			byte[] iv = new byte[IV_LENGTH];
			System.arraycopy(data, 0, iv, 0, IV_LENGTH);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, buildKey(key), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			return cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH);
		} catch (CryptoException e) {
			throw e;
		} catch (Exception e) {
			throw new CryptoException("AES 解密失败（密钥错误或密文被篡改）: " + e.getMessage(), e);
		}
	}

	/**
	 * 由任意长度字符串派生 16 字节密钥（SHA-256 截断）。
	 */
	private static SecretKeySpec buildKey(String key) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
			byte[] aesKey = new byte[16];
			System.arraycopy(hash, 0, aesKey, 0, 16);
			return new SecretKeySpec(aesKey, "AES");
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException("SHA-256 不可用", e);
		}
	}
}
