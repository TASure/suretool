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
import java.util.Base64;

import javax.crypto.Cipher;

import com.sure.tool.codec.HexUtil;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES 加解密工具类（仅兼容旧系统存量密文，新代码请使用 {@link AesUtil}）。
 *
 * <p><strong>安全警告：</strong>DES 为不安全的弱算法（56 位密钥），本类保留仅用于解密旧系统历史数据；
 * 新项目一律使用 {@link AesUtil}（AES-GCM 认证加密）。
 *
 * @author suretool
 * @since 0.1.0
 * @deprecated 不安全算法（DES），仅兼容旧系统，新代码请使用 {@link AesUtil}
 */
@Deprecated
public class DesUtil {

	/** 变换算法：CBC 模式 + PKCS5 填充 */
	private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";

	/** 固定 IV（与历史版本一致，保证存量密文可解） */
	private static final byte[] IV = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};

	private DesUtil() {
	}

	/**
	 * DES 加密为十六进制。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return 十六进制密文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static String encryptHex(String data, String key) {
		return HexUtil.encodeHexStr(encrypt(data.getBytes(StandardCharsets.UTF_8), key));
	}

	/**
	 * DES 解密十六进制密文。
	 *
	 * @param hex 十六进制密文
	 * @param key 密钥
	 * @return 明文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static String decryptHex(String hex, String key) {
		return new String(decrypt(HexUtil.decodeHex(hex), key), StandardCharsets.UTF_8);
	}

	/**
	 * DES 加密为 Base64。
	 *
	 * @param data 明文
	 * @param key  密钥
	 * @return Base64 密文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static String encryptBase64(String data, String key) {
		return Base64.getEncoder().encodeToString(encrypt(data.getBytes(StandardCharsets.UTF_8), key));
	}

	/**
	 * DES 解密 Base64 密文。
	 *
	 * @param base64 Base64 密文
	 * @param key    密钥
	 * @return 明文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static String decryptBase64(String base64, String key) {
		return new String(decrypt(Base64.getDecoder().decode(base64), key), StandardCharsets.UTF_8);
	}

	/**
	 * DES-CBC 加密（字节）。
	 *
	 * @param data 明文
	 * @param key  密钥字符串
	 * @return 密文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static byte[] encrypt(byte[] data, String key) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, buildKey(key), new IvParameterSpec(IV));
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptoException("DES 加密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * DES-CBC 解密（字节）。
	 *
	 * @param data 密文
	 * @param key  密钥字符串
	 * @return 明文
	 * @deprecated 不安全算法（DES），仅兼容旧系统
	 */
	@Deprecated
	public static byte[] decrypt(byte[] data, String key) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, buildKey(key), new IvParameterSpec(IV));
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptoException("DES 解密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 由任意长度字符串派生 8 字节 DES 密钥（MD5 截断，与历史版本一致）。
	 */
	@Deprecated
	private static SecretKeySpec buildKey(String key) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
			byte[] desKey = new byte[8];
			System.arraycopy(hash, 0, desKey, 0, 8);
			return new SecretKeySpec(desKey, "DES");
		} catch (Exception e) {
			throw new CryptoException("DES 密钥派生失败", e);
		}
	}
}
