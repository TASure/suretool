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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import com.sure.tool.codec.HashUtil;

/**
 * 安全工具门面：哈希、AES/DES/RSA/HMAC 一键加解密、随机密钥，参考 Hutool 的 {@code SecureUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class SecureUtil {

	/** 随机源（复用实例，避免每次创建阻塞熵源） */
	private static final SecureRandom RANDOM = new SecureRandom();

	private SecureUtil() {
	}

	/**
	 * MD5 摘要（32 位小写十六进制）。
	 *
	 * @param data 数据
	 * @return 摘要
	 */
	public static String md5(String data) {
		return HashUtil.md5Hex(data);
	}

	/**
	 * SHA-1 摘要（40 位小写十六进制）。
	 *
	 * @param data 数据
	 * @return 摘要
	 */
	public static String sha1(String data) {
		return HashUtil.sha1Hex(data);
	}

	/**
	 * SHA-256 摘要（64 位小写十六进制）。
	 *
	 * @param data 数据
	 * @return 摘要
	 */
	public static String sha256(String data) {
		return HashUtil.sha256Hex(data);
	}

	/**
	 * SHA-512 摘要。
	 *
	 * @param data 数据
	 * @return 摘要
	 */
	public static String sha512(String data) {
		return HashUtil.sha512Hex(data);
	}

	/**
	 * AES 加密为十六进制。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return 十六进制密文
	 */
	public static String aesEncryptHex(String data, String key) {
		return AesUtil.encryptHex(data, key);
	}

	/**
	 * AES 解密十六进制密文。
	 *
	 * @param hex 十六进制密文
	 * @param key 密钥
	 * @return 明文
	 */
	public static String aesDecryptHex(String hex, String key) {
		return AesUtil.decryptHex(hex, key);
	}

	/**
	 * AES 加密为 Base64。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return Base64 密文
	 */
	public static String aesEncryptBase64(String data, String key) {
		return AesUtil.encryptBase64(data, key);
	}

	/**
	 * AES 解密 Base64 密文。
	 *
	 * @param base64 Base64 密文
	 * @param key    密钥
	 * @return 明文
	 */
	public static String aesDecryptBase64(String base64, String key) {
		return AesUtil.decryptBase64(base64, key);
	}

	/**
	 * DES 加密为十六进制。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return 十六进制密文
	 */
	public static String desEncryptHex(String data, String key) {
		return DesUtil.encryptHex(data, key);
	}

	/**
	 * DES 解密十六进制密文。
	 *
	 * @param hex 十六进制密文
	 * @param key 密钥
	 * @return 明文
	 */
	public static String desDecryptHex(String hex, String key) {
		return DesUtil.decryptHex(hex, key);
	}

	/**
	 * HMAC-SHA256 十六进制。
	 *
	 * @param data 数据
	 * @param key  密钥
	 * @return MAC
	 */
	public static String hmacSha256(String data, String key) {
		return HmacUtil.hmacSha256Hex(data, key);
	}

	/**
	 * HMAC-SHA1 十六进制。
	 *
	 * @param data 数据
	 * @param key  密钥
	 * @return MAC
	 */
	public static String hmacSha1(String data, String key) {
		return HmacUtil.hmacSha1Hex(data, key);
	}

	/**
	 * 生成 RSA 密钥对（2048 位）。
	 *
	 * @return 密钥对
	 */
	public static KeyPair rsaGenerateKeyPair() {
		return RsaUtil.generateKeyPair();
	}

	/**
	 * RSA 公钥加密为 Base64。
	 *
	 * @param data      明文
	 * @param publicKey 公钥
	 * @return Base64 密文
	 */
	public static String rsaEncryptBase64(String data, PublicKey publicKey) {
		return RsaUtil.encryptBase64(data, publicKey);
	}

	/**
	 * RSA 私钥解密 Base64 密文。
	 *
	 * @param base64     Base64 密文
	 * @param privateKey 私钥
	 * @return 明文
	 */
	public static String rsaDecryptBase64(String base64, PrivateKey privateKey) {
		return RsaUtil.decryptBase64(base64, privateKey);
	}

	/**
	 * 生成随机密钥（Base64 编码）。
	 *
	 * @param byteCount 字节数（AES-128 用 16，AES-256 用 32）
	 * @return Base64 密钥
	 */
	public static String randomSecret(int byteCount) {
		byte[] key = new byte[byteCount];
		RANDOM.nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}
}