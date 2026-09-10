package com.sure.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sure.tool.codec.Base64Util;
import com.sure.tool.codec.HexUtil;

/**
 * HMAC 消息认证码工具类（MD5/SHA-1/SHA-256/SHA-512），参考 Hutool 的 {@code HMac} 设计。
 * 用于数据完整性校验与签名场景，输出 hex 与 Base64 双格式。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HmacUtil {

	private HmacUtil() {
	}

	/**
	 * HMAC-MD5 十六进制。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return 十六进制 MAC
	 */
	public static String hmacMd5Hex(String data, String key) {
		return hmacHex("HmacMD5", data, key);
	}

	/**
	 * HMAC-SHA1 十六进制。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return 十六进制 MAC
	 */
	public static String hmacSha1Hex(String data, String key) {
		return hmacHex("HmacSHA1", data, key);
	}

	/**
	 * HMAC-SHA256 十六进制（最常用）。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return 十六进制 MAC
	 */
	public static String hmacSha256Hex(String data, String key) {
		return hmacHex("HmacSHA256", data, key);
	}

	/**
	 * HMAC-SHA512 十六进制。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return 十六进制 MAC
	 */
	public static String hmacSha512Hex(String data, String key) {
		return hmacHex("HmacSHA512", data, key);
	}

	/**
	 * HMAC-SHA256 Base64。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return Base64 MAC
	 */
	public static String hmacSha256Base64(String data, String key) {
		return Base64Util.encode(hmac("HmacSHA256", bytes(data), bytes(key)));
	}

	/**
	 * HMAC-SHA1 Base64。
	 *
	 * @param data 数据（UTF-8）
	 * @param key  密钥（UTF-8）
	 * @return Base64 MAC
	 */
	public static String hmacSha1Base64(String data, String key) {
		return Base64Util.encode(hmac("HmacSHA1", bytes(data), bytes(key)));
	}

	/**
	 * 通用 HMAC 计算。
	 *
	 * @param algorithm 算法（HmacMD5/HmacSHA1/HmacSHA256/HmacSHA512）
	 * @param data      数据
	 * @param key       密钥
	 * @return MAC 字节
	 */
	public static byte[] hmac(String algorithm, byte[] data, byte[] key) {
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return mac.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException("不支持的 HMAC 算法: " + algorithm, e);
		} catch (InvalidKeyException e) {
			throw new CryptoException("HMAC 密钥无效", e);
		}
	}

	private static String hmacHex(String algorithm, String data, String key) {
		return HexUtil.encodeHexStr(hmac(algorithm, bytes(data), bytes(key)));
	}

	private static byte[] bytes(String value) {
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}
}
