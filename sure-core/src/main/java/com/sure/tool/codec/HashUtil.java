package com.sure.tool.codec;

import com.sure.tool.util.CharsetUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * 哈希工具类，参考 Hutool 的 {@code DigestUtil} / {@code HashUtil} 设计。
 *
 * <p>基于 JDK 原生 {@link MessageDigest} 实现 MD5、SHA 系列摘要与 CRC32 校验。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class HashUtil {

	private HashUtil() {
	}

	/**
	 * 计算字符串（UTF-8）的 MD5 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return MD5 十六进制字符串
	 */
	public static String md5Hex(String data) {
		return md5Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 MD5 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return MD5 十六进制字符串
	 */
	public static String md5Hex(byte[] data) {
		return digest("MD5", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-1 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-1 十六进制字符串
	 */
	public static String sha1Hex(String data) {
		return sha1Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-1 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-1 十六进制字符串
	 */
	public static String sha1Hex(byte[] data) {
		return digest("SHA-1", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-256 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-256 十六进制字符串
	 */
	public static String sha256Hex(String data) {
		return sha256Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-256 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-256 十六进制字符串
	 */
	public static String sha256Hex(byte[] data) {
		return digest("SHA-256", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-512 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-512 十六进制字符串
	 */
	public static String sha512Hex(String data) {
		return sha512Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-512 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-512 十六进制字符串
	 */
	public static String sha512Hex(byte[] data) {
		return digest("SHA-512", data);
	}

	/**
	 * 通用摘要计算。
	 *
	 * @param algorithm 算法名
	 * @param data      数据
	 * @return 十六进制摘要
	 */
	public static String digest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			return HexUtil.encodeHexStr(md.digest(data));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的摘要算法: " + algorithm, e);
		}
	}

	/**
	 * 计算字符串（UTF-8）的 CRC32 校验值。
	 *
	 * @param data 字符串
	 * @return CRC32 值
	 */
	public static long crc32(String data) {
		return crc32(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 CRC32 校验值。
	 *
	 * @param data 字节数组
	 * @return CRC32 值
	 */
	public static long crc32(byte[] data) {
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return crc32.getValue();
	}

	/**
	 * 计算字符串（UTF-8）的 CRC32 校验值（8 位十六进制字符串）。
	 *
	 * @param data 字符串
	 * @return CRC32 十六进制字符串
	 */
	public static String crc32Hex(String data) {
		return crc32Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 CRC32 校验值（8 位十六进制字符串）。
	 *
	 * @param data 字节数组
	 * @return CRC32 十六进制字符串
	 */
	public static String crc32Hex(byte[] data) {
		String hex = Long.toHexString(crc32(data));
		return "00000000".substring(hex.length()) + hex;
	}
}
