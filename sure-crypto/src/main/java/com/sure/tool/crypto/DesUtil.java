package com.sure.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sure.tool.codec.Base64Util;
import com.sure.tool.codec.HexUtil;

/**
 * DES 加解密工具类（DES/CBC/PKCS5Padding），无状态静态方法、线程安全，参考 Hutool 的 {@code Des} 设计。
 * <p>
 * 注意：DES 为历史算法（56 位密钥），仅用于兼容旧系统；新项目请使用 {@link AesUtil}。
 * 密钥派生：任意长度字符串密钥经 MD5 摘要取前 8 字节；IV 固定 8 字节。
 *
 * @author suretool
 */
public class DesUtil {

	private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";
	private static final String ALGORITHM = "DES";
	private static final byte[] IV = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};

	private DesUtil() {
	}

	/**
	 * DES 加密。
	 *
	 * @param data 明文
	 * @param key  密钥（任意长度，UTF-8，经 MD5 派生 8 字节）
	 * @return 密文字节
	 */
	public static byte[] encrypt(byte[] data, byte[] key) {
		return transform(Cipher.ENCRYPT_MODE, data, key);
	}

	/**
	 * DES 解密。
	 *
	 * @param data 密文字节
	 * @param key  密钥（与加密时一致）
	 * @return 明文字节
	 */
	public static byte[] decrypt(byte[] data, byte[] key) {
		return transform(Cipher.DECRYPT_MODE, data, key);
	}

	/**
	 * DES 加密为十六进制字符串。
	 *
	 * @param data 明文（UTF-8）
	 * @param key  密钥
	 * @return 十六进制密文
	 */
	public static String encryptHex(String data, String key) {
		return HexUtil.encodeHexStr(encrypt(bytes(data), bytes(key)));
	}

	/**
	 * 十六进制密文 DES 解密。
	 *
	 * @param hex 十六进制密文
	 * @param key 密钥
	 * @return 明文（UTF-8）
	 */
	public static String decryptHex(String hex, String key) {
		return new String(decrypt(HexUtil.decodeHex(hex), bytes(key)), StandardCharsets.UTF_8);
	}

	/**
	 * DES 加密为 Base64 字符串。
	 *
	 * @param data 明文（UTF-8）
	 * @param key  密钥
	 * @return Base64 密文
	 */
	public static String encryptBase64(String data, String key) {
		return Base64Util.encode(encrypt(bytes(data), bytes(key)));
	}

	/**
	 * Base64 密文 DES 解密。
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
			SecretKeySpec keySpec = new SecretKeySpec(deriveKey(key, 8), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(mode, keySpec, new IvParameterSpec(IV));
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("DES 加解密失败", e);
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
}
