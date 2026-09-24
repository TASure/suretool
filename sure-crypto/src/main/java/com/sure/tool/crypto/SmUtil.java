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

import java.math.BigInteger;

/**
 * 国密算法统一入口（SM2 / SM3 / SM4）。
 *
 * <p>全部为纯 JDK 实现，零第三方依赖，遵循 GB/T 32905 / 32907 / 32918。</p>
 *
 * @author suretool
 * @since 1.0.1
 */
public final class SmUtil {

	private SmUtil() {
	}

	// ---- SM2 ----

	/**
	 * 生成 SM2 密钥对。
	 *
	 * @return 密钥对
	 */
	public static Sm2Util.Sm2KeyPair sm2KeyPair() {
		return Sm2Util.generateKeyPair();
	}

	/**
	 * SM2 签名（r||s，64 字节）。
	 *
	 * @param keyPair 密钥对
	 * @param message 消息
	 * @return 签名
	 */
	public static byte[] sm2Sign(Sm2Util.Sm2KeyPair keyPair, byte[] message) {
		return Sm2Util.sign(keyPair, message);
	}

	/**
	 * SM2 验签。
	 *
	 * @param pubX      公钥 x
	 * @param pubY      公钥 y
	 * @param message   消息
	 * @param signature 签名
	 * @return 是否通过
	 */
	public static boolean sm2Verify(BigInteger pubX, BigInteger pubY, byte[] message, byte[] signature) {
		return Sm2Util.verify(pubX, pubY, message, signature);
	}

	/**
	 * SM2 加密（C1||C3||C2）。
	 *
	 * @param pubX 公钥 x
	 * @param pubY 公钥 y
	 * @param data 明文
	 * @return 密文
	 */
	public static byte[] sm2Encrypt(BigInteger pubX, BigInteger pubY, byte[] data) {
		return Sm2Util.encrypt(pubX, pubY, data);
	}

	/**
	 * SM2 解密。
	 *
	 * @param keyPair 密钥对
	 * @param cipher  密文
	 * @return 明文
	 */
	public static byte[] sm2Decrypt(Sm2Util.Sm2KeyPair keyPair, byte[] cipher) {
		return Sm2Util.decrypt(keyPair, cipher);
	}

	// ---- SM3 ----

	/**
	 * SM3 摘要。
	 *
	 * @param data 输入
	 * @return 32 字节摘要
	 */
	public static byte[] sm3(byte[] data) {
		return Sm3Util.sm3(data);
	}

	/**
	 * SM3 摘要（十六进制）。
	 *
	 * @param text 字符串
	 * @return 64 位十六进制摘要
	 */
	public static String sm3Hex(String text) {
		return Sm3Util.sm3Hex(text);
	}

	// ---- SM4 ----

	/**
	 * SM4 生成密钥。
	 *
	 * @return 16 字节密钥
	 */
	public static byte[] sm4Key() {
		return Sm4Util.generateKey();
	}

	/**
	 * SM4-ECB 加密（十六进制）。
	 *
	 * @param key  16 字节密钥
	 * @param text 明文字符串
	 * @return 十六进制密文
	 */
	public static String sm4EncryptHex(byte[] key, String text) {
		return Sm4Util.encryptHex(key, text);
	}

	/**
	 * SM4-ECB 解密（十六进制）。
	 *
	 * @param key 16 字节密钥
	 * @param hex 十六进制密文
	 * @return 明文字符串
	 */
	public static String sm4DecryptStr(byte[] key, String hex) {
		return Sm4Util.decryptStr(key, hex);
	}
}
