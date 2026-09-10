package com.sure.tool.codec;

import com.sure.tool.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * URL 编解码工具类，参考 Hutool 的 {@code URLEncodeUtil} 设计。
 * 保留 {@code A-Z a-z 0-9 - _ . ~} 五个非保留字符，其余按字节 {@code %XX} 编码。
 *
 * @author suretool
 * @since 0.1.0
 */
public class EncodeUtil {

	/** RFC 3986 非保留字符 */
	private static final String UNRESERVED = "-_.~";

	private EncodeUtil() {
	}

	/**
	 * URL 编码（UTF-8），空格编码为 {@code %20}。
	 *
	 * @param url 原文
	 * @return 编码结果，输入为 {@code null} 时返回 {@code null}
	 */
	public static String encode(String url) {
		return encode(url, CharsetUtil.UTF_8);
	}

	/**
	 * URL 编码。
	 *
	 * @param url     原文
	 * @param charset 字符集
	 * @return 编码结果，输入为 {@code null} 时返回 {@code null}
	 */
	public static String encode(String url, Charset charset) {
		if (url == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(url.length() * 2);
		byte[] bytes = url.getBytes(charset);
		for (byte b : bytes) {
			int c = b & 0xFF;
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
					|| UNRESERVED.indexOf(c) >= 0) {
				sb.append((char) c);
			} else {
				sb.append('%').append(String.format("%02X", c));
			}
		}
		return sb.toString();
	}

	/**
	 * URL 解码（UTF-8），{@code +} 视为空格。
	 *
	 * @param url 编码后的文本
	 * @return 解码结果，输入为 {@code null} 时返回 {@code null}
	 */
	public static String decode(String url) {
		return decode(url, CharsetUtil.UTF_8);
	}

	/**
	 * URL 解码。
	 *
	 * @param url     编码后的文本
	 * @param charset 字符集
	 * @return 解码结果，输入为 {@code null} 时返回 {@code null}
	 */
	public static String decode(String url, Charset charset) {
		if (url == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(url.length());
		for (int i = 0; i < url.length(); i++) {
			char c = url.charAt(i);
			if (c == '%' && i + 2 < url.length()) {
				int high = Character.digit(url.charAt(i + 1), 16);
				int low = Character.digit(url.charAt(i + 2), 16);
				if (high >= 0 && low >= 0) {
					out.write((high << 4) | low);
					i += 2;
					continue;
				}
			}
			if (c == '+') {
				out.write(' ');
			} else {
				byte[] bytes = String.valueOf(c).getBytes(charset);
				out.write(bytes, 0, bytes.length);
			}
		}
		return new String(out.toByteArray(), charset);
	}
}
