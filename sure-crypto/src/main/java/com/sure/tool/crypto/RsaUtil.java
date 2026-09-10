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
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.sure.tool.codec.Base64Util;
import com.sure.tool.codec.HexUtil;

/**
 * RSA 加解密工具类（RSA/ECB/PKCS1Padding），无状态静态方法、线程安全，参考 Hutool 的 {@code RSA} 设计。
 * <p>
 * 默认 2048 位密钥；单次加密明文上限 = 密钥位数 / 8 - 11 字节（2048 位约 245 字节），超出请自行分段。
 *
 * @author suretool
 * @since 0.1.0
 */
public class RsaUtil {

	private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
	private static final String ALGORITHM = "RSA";
	/** 默认密钥位数 */
	public static final int DEFAULT_KEY_SIZE = 2048;

	private RsaUtil() {
	}

	/**
	 * 生成密钥对（2048 位）。
	 *
	 * @return 密钥对
	 */
	public static KeyPair generateKeyPair() {
		return generateKeyPair(DEFAULT_KEY_SIZE);
	}

	/**
	 * 生成密钥对。
	 *
	 * @param keySize 密钥位数（建议 2048 及以上）
	 * @return 密钥对
	 */
	public static KeyPair generateKeyPair(int keySize) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
			generator.initialize(keySize);
			return generator.generateKeyPair();
		} catch (GeneralSecurityException e) {
			throw new CryptoException("RSA 密钥对生成失败", e);
		}
	}

	/**
	 * RSA 加密。
	 *
	 * @param data 明文
	 * @param key  公钥或私钥
	 * @return 密文字节
	 */
	public static byte[] encrypt(byte[] data, Key key) {
		return transform(Cipher.ENCRYPT_MODE, data, key);
	}

	/**
	 * RSA 解密。
	 *
	 * @param data 密文字节
	 * @param key  与加密相反的公钥或私钥
	 * @return 明文字节
	 */
	public static byte[] decrypt(byte[] data, Key key) {
		return transform(Cipher.DECRYPT_MODE, data, key);
	}

	/**
	 * 公钥加密为 Base64。
	 *
	 * @param data      明文（UTF-8）
	 * @param publicKey 公钥
	 * @return Base64 密文
	 */
	public static String encryptBase64(String data, PublicKey publicKey) {
		return Base64Util.encode(encrypt(bytes(data), publicKey));
	}

	/**
	 * 私钥解密 Base64 密文。
	 *
	 * @param base64     Base64 密文
	 * @param privateKey 私钥
	 * @return 明文（UTF-8）
	 */
	public static String decryptBase64(String base64, PrivateKey privateKey) {
		return new String(decrypt(Base64Util.decode(base64), privateKey), StandardCharsets.UTF_8);
	}

	/**
	 * 公钥加密为十六进制。
	 *
	 * @param data      明文（UTF-8）
	 * @param publicKey 公钥
	 * @return 十六进制密文
	 */
	public static String encryptHex(String data, PublicKey publicKey) {
		return HexUtil.encodeHexStr(encrypt(bytes(data), publicKey));
	}

	/**
	 * 私钥解密十六进制密文。
	 *
	 * @param hex        十六进制密文
	 * @param privateKey 私钥
	 * @return 明文（UTF-8）
	 */
	public static String decryptHex(String hex, PrivateKey privateKey) {
		return new String(decrypt(HexUtil.decodeHex(hex), privateKey), StandardCharsets.UTF_8);
	}

	/**
	 * 公钥转 Base64（X.509）。
	 *
	 * @param keyPair 密钥对
	 * @return Base64 公钥
	 */
	public static String getPublicKeyBase64(KeyPair keyPair) {
		return Base64Util.encode(keyPair.getPublic().getEncoded());
	}

	/**
	 * 私钥转 Base64（PKCS#8）。
	 *
	 * @param keyPair 密钥对
	 * @return Base64 私钥
	 */
	public static String getPrivateKeyBase64(KeyPair keyPair) {
		return Base64Util.encode(keyPair.getPrivate().getEncoded());
	}

	/**
	 * Base64 解析公钥。
	 *
	 * @param base64 Base64 公钥
	 * @return 公钥
	 */
	public static PublicKey parsePublicKey(String base64) {
		try {
			KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
			return factory.generatePublic(new X509EncodedKeySpec(Base64Util.decode(base64)));
		} catch (GeneralSecurityException e) {
			throw new CryptoException("公钥解析失败", e);
		}
	}

	/**
	 * Base64 解析私钥。
	 *
	 * @param base64 Base64 私钥
	 * @return 私钥
	 */
	public static PrivateKey parsePrivateKey(String base64) {
		try {
			KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
			return factory.generatePrivate(new PKCS8EncodedKeySpec(Base64Util.decode(base64)));
		} catch (GeneralSecurityException e) {
			throw new CryptoException("私钥解析失败", e);
		}
	}

	private static byte[] transform(int mode, byte[] data, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(mode, key);
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("RSA 加解密失败", e);
		}
	}

	private static byte[] bytes(String value) {
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}
}