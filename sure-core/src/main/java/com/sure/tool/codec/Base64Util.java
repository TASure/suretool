package com.sure.tool.codec;

import com.sure.tool.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Base64 编解码工具类，参考 Hutool 的 {@code Base64} 设计。
 *
 * @author suretool
 */
public class Base64Util {

	private Base64Util() {
	}

	/**
	 * 字节数组编码为 Base64 字符串。
	 *
	 * @param data 字节数组
	 * @return Base64 字符串
	 */
	public static String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	/**
	 * 字符串（UTF-8）编码为 Base64 字符串。
	 *
	 * @param data 字符串
	 * @return Base64 字符串
	 */
	public static String encode(String data) {
		return encode(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * URL 安全的 Base64 编码（不含填充符 {@code =}）。
	 *
	 * @param data 字节数组
	 * @return URL 安全 Base64 字符串
	 */
	public static String encodeUrlSafe(byte[] data) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
	}

	/**
	 * URL 安全的 Base64 编码（不含填充符）。
	 *
	 * @param data 字符串（UTF-8）
	 * @return URL 安全 Base64 字符串
	 */
	public static String encodeUrlSafe(String data) {
		return encodeUrlSafe(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * Base64 字符串解码为字节数组（兼容标准与 URL 安全两种编码）。
	 *
	 * @param base64 Base64 字符串
	 * @return 字节数组
	 */
	public static byte[] decode(String base64) {
		try {
			return Base64.getDecoder().decode(base64);
		} catch (IllegalArgumentException e) {
			return Base64.getUrlDecoder().decode(base64);
		}
	}

	/**
	 * Base64 字符串解码为 UTF-8 字符串。
	 *
	 * @param base64 Base64 字符串
	 * @return 解码后的字符串
	 */
	public static String decodeStr(String base64) {
		return decodeStr(base64, CharsetUtil.UTF_8);
	}

	/**
	 * Base64 字符串解码为指定字符集的字符串。
	 *
	 * @param base64  Base64 字符串
	 * @param charset 字符集
	 * @return 解码后的字符串
	 */
	public static String decodeStr(String base64, Charset charset) {
		return new String(decode(base64), charset);
	}

	/**
	 * 判断字符串是否为合法的标准 Base64（标准字符集 + 长度对齐）。
	 *
	 * @param base64 字符串
	 * @return 是否为 Base64
	 */
	public static boolean isBase64(String base64) {
		if (base64 == null || base64.isEmpty()) {
			return false;
		}
		return base64.matches("^[A-Za-z0-9+/]*={0,2}$") && base64.length() % 4 == 0;
	}
}
