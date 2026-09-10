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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * RSA 加解密工具类（OAEP-SHA256 填充，语义安全），参考 Hutool 的 {@code RSA} 设计。
 *
 * <p>安全说明：
 * <ul>
 *   <li>使用 RSA/ECB/OAEPWithSHA-256AndMGF1Padding（替代不安全的 PKCS1 v1.5），抵抗 Bleichenbacher 攻击；</li>
 *   <li>默认 2048 位密钥，{@link #generateKeyPair(int)} 拒绝低于 2048 位的弱密钥；</li>
 *   <li>单次加密明文上限约 190 字节（2048 位 OAEP-SHA256），大文本请先对称加密（如 {@link AesUtil}）。</li>
 * </ul>
 *
 * @author suretool
 * @since 0.1.0
 */
public class RsaUtil {

	/** 变换算法：OAEP-SHA256 填充 */
	private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

	/** 最小安全密钥位数 */
	public static final int MIN_KEY_SIZE = 2048;

	/** 随机源 */
	private static final SecureRandom RANDOM = new SecureRandom();

	private RsaUtil() {
	}

	/**
	 * 生成默认 2048 位 RSA 密钥对。
	 *
	 * @return 密钥对
	 */
	public static KeyPair generateKeyPair() {
		return generateKeyPair(MIN_KEY_SIZE);
	}

	/**
	 * 生成指定长度 RSA 密钥对（拒绝低于 {@link #MIN_KEY_SIZE} 的弱密钥）。
	 *
	 * @param keySize 密钥位数
	 * @return 密钥对
	 */
	public static KeyPair generateKeyPair(int keySize) {
		if (keySize < MIN_KEY_SIZE) {
			throw new IllegalArgumentException("RSA 密钥长度不能低于 " + MIN_KEY_SIZE + " 位（收到 " + keySize + "）");
		}
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(keySize, RANDOM);
			return generator.generateKeyPair();
		} catch (Exception e) {
			throw new CryptoException("RSA 密钥对生成失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 公钥加密为十六进制。
	 *
	 * @param data      明文
	 * @param publicKey 公钥
	 * @return 十六进制密文
	 */
	public static String encryptHex(String data, PublicKey publicKey) {
		return HexUtil.encodeHexStr(encrypt(data.getBytes(StandardCharsets.UTF_8), publicKey));
	}

	/**
	 * 私钥解密十六进制密文。
	 *
	 * @param hex        十六进制密文
	 * @param privateKey 私钥
	 * @return 明文
	 */
	public static String decryptHex(String hex, PrivateKey privateKey) {
		return new String(decrypt(HexUtil.decodeHex(hex), privateKey), StandardCharsets.UTF_8);
	}

	/**
	 * 公钥加密为 Base64。
	 *
	 * @param data      明文
	 * @param publicKey 公钥
	 * @return Base64 密文
	 */
	public static String encryptBase64(String data, PublicKey publicKey) {
		return Base64.getEncoder().encodeToString(encrypt(data.getBytes(StandardCharsets.UTF_8), publicKey));
	}

	/**
	 * 私钥解密 Base64 密文。
	 *
	 * @param base64     Base64 密文
	 * @param privateKey 私钥
	 * @return 明文
	 */
	public static String decryptBase64(String base64, PrivateKey privateKey) {
		return new String(decrypt(Base64.getDecoder().decode(base64), privateKey), StandardCharsets.UTF_8);
	}

	/**
	 * 公钥 OAEP 加密（字节）。
	 *
	 * @param data      明文
	 * @param publicKey 公钥
	 * @return 密文
	 */
	public static byte[] encrypt(byte[] data, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptoException("RSA 加密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 私钥 OAEP 解密（字节）。
	 *
	 * @param data       密文
	 * @param privateKey 私钥
	 * @return 明文
	 */
	public static byte[] decrypt(byte[] data, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptoException("RSA 解密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 提取公钥 Base64（X.509）。
	 *
	 * @param keyPair 密钥对
	 * @return Base64 公钥
	 */
	public static String getPublicKeyBase64(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
	}

	/**
	 * 提取私钥 Base64（PKCS#8）。
	 *
	 * @param keyPair 密钥对
	 * @return Base64 私钥
	 */
	public static String getPrivateKeyBase64(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
	}

	/**
	 * 解析 Base64 公钥。
	 *
	 * @param base64 Base64 公钥
	 * @return 公钥
	 */
	public static PublicKey parsePublicKey(String base64) {
		try {
			byte[] bytes = Base64.getDecoder().decode(base64);
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
		} catch (Exception e) {
			throw new CryptoException("RSA 公钥解析失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 解析 Base64 私钥。
	 *
	 * @param base64 Base64 私钥
	 * @return 私钥
	 */
	public static PrivateKey parsePrivateKey(String base64) {
		try {
			byte[] bytes = Base64.getDecoder().decode(base64);
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch (Exception e) {
			throw new CryptoException("RSA 私钥解析失败: " + e.getMessage(), e);
		}
	}
}
