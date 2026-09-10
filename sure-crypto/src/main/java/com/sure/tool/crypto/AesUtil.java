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
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sure.tool.codec.Base64Util;
import com.sure.tool.codec.HexUtil;

/**
 * AES 加解密工具类（AES/CBC/PKCS5Padding），无状态静态方法、线程安全，参考 Hutool 的 {@code Aes} 设计。
 * <p>
 * 密钥派生：任意长度字符串密钥经 MD5 摘要为 16 字节（AES-128）。
 * IV：固定 16 字节常量，简单场景够用；对安全性要求极高的场景请自行派生 IV 并更换为自定义实现。
 *
 * @author suretool
 * @since 0.1.0
 */
public class AesUtil {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM = "AES";
	/** 固定 IV（16 字节），生产环境建议自定义 */
	private static final byte[] IV = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	/** 随机源（复用实例，避免每次创建阻塞熵源） */
	private static final SecureRandom RANDOM = new SecureRandom();

	private AesUtil() {
	}

	/**
	 * AES 加密。
	 *
	 * @param data 明文
	 * @param key  密钥（任意长度，UTF-8，经 MD5 派生 16 字节）
	 * @return 密文字节
	 */
	public static byte[] encrypt(byte[] data, byte[] key) {
		return transform(Cipher.ENCRYPT_MODE, data, key);
	}

	/**
	 * AES 解密。
	 *
	 * @param data 密文字节
	 * @param key  密钥（与加密时一致）
	 * @return 明文字节
	 */
	public static byte[] decrypt(byte[] data, byte[] key) {
		return transform(Cipher.DECRYPT_MODE, data, key);
	}

	/**
	 * AES 加密为十六进制字符串。
	 *
	 * @param data 明文（UTF-8）
	 * @param key  密钥
	 * @return 十六进制密文
	 */
	public static String encryptHex(String data, String key) {
		return HexUtil.encodeHexStr(encrypt(bytes(data), bytes(key)));
	}

	/**
	 * 十六进制密文 AES 解密。
	 *
	 * @param hex 十六进制密文
	 * @param key 密钥
	 * @return 明文（UTF-8）
	 */
	public static String decryptHex(String hex, String key) {
		return new String(decrypt(HexUtil.decodeHex(hex), bytes(key)), StandardCharsets.UTF_8);
	}

	/**
	 * AES 加密为 Base64 字符串。
	 *
	 * @param data 明文（UTF-8）
	 * @param key  密钥
	 * @return Base64 密文
	 */
	public static String encryptBase64(String data, String key) {
		return Base64Util.encode(encrypt(bytes(data), bytes(key)));
	}

	/**
	 * Base64 密文 AES 解密。
	 *
	 * @param base64 Base64 密文
	 * @param key    密钥
	 * @return 明文（UTF-8）
	 */
	public static String decryptBase64(String base64, String key) {
		return new String(decrypt(Base64Util.decode(base64), bytes(key)), StandardCharsets.UTF_8);
	}

	private static byte[] transform(int mode, byte[] data, byte[] key) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(deriveKey(key, 16), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(mode, keySpec, new IvParameterSpec(IV));
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("AES 加解密失败", e);
		}
	}

	private static byte[] deriveKey(byte[] key, int length) {
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(key);
			byte[] result = new byte[length];
			System.arraycopy(digest, 0, result, 0, length);
			return result;
		} catch (GeneralSecurityException e) {
			throw new CryptoException("密钥派生失败", e);
		}
	}

	private static byte[] bytes(String value) {
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * 生成随机 AES 密钥（Base64 编码，供存储与传输）。
	 *
	 * @return Base64 密钥
	 */
	public static String generateKey() {
		byte[] key = new byte[16];
		RANDOM.nextBytes(key);
		return Base64Util.encode(key);
	}
}