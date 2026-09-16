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
package com.sure.tool.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 十六进制编解码工具：字节与十六进制字符串互转，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class HexUtil {

	private static final char[] DIGITS_LOWER = "0123456789abcdef".toCharArray();
	private static final char[] DIGITS_UPPER = "0123456789ABCDEF".toCharArray();

	private HexUtil() {
	}

	/**
	 * 字节数组转小写十六进制。
	 *
	 * @param bytes 字节
	 * @return 十六进制字符串
	 */
	public static String encodeHex(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return encodeHex(bytes, DIGITS_LOWER);
	}

	/**
	 * 字节数组转大写十六进制。
	 *
	 * @param bytes 字节
	 * @return 大写十六进制字符串
	 */
	public static String encodeHexUpper(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return encodeHex(bytes, DIGITS_UPPER);
	}

	/**
	 * 十六进制字符串解码为字节数组（忽略空白，大小写均可）。
	 *
	 * @param hex 十六进制字符串
	 * @return 字节数组
	 */
	public static byte[] decodeHex(String hex) {
		if (hex == null) {
			return null;
		}
		String clean = hex.replaceAll("\\s", "");
		if (clean.length() % 2 != 0) {
			throw new IllegalArgumentException("十六进制长度必须为偶数: " + clean.length());
		}
		byte[] result = new byte[clean.length() / 2];
		for (int i = 0; i < result.length; i++) {
			int high = Character.digit(clean.charAt(i * 2), 16);
			int low = Character.digit(clean.charAt(i * 2 + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException("非法十六进制字符: " + clean.substring(i * 2, i * 2 + 2));
			}
			result[i] = (byte) ((high << 4) | low);
		}
		return result;
	}

	/**
	 * 字符串转十六进制（UTF-8 编码）。
	 *
	 * @param str 字符串
	 * @return 十六进制
	 */
	public static String encodeHexStr(String str) {
		return encodeHexStr(str, StandardCharsets.UTF_8);
	}

	/**
	 * 字符串转十六进制（指定字符集）。
	 *
	 * @param str     字符串
	 * @param charset 字符集
	 * @return 十六进制
	 */
	public static String encodeHexStr(String str, Charset charset) {
		return str == null ? null : encodeHex(str.getBytes(charset));
	}

	/**
	 * 十六进制转字符串（UTF-8 解码）。
	 *
	 * @param hex 十六进制
	 * @return 字符串
	 */
	public static String decodeHexStr(String hex) {
		return decodeHexStr(hex, StandardCharsets.UTF_8);
	}

	/**
	 * 十六进制转字符串（指定字符集）。
	 *
	 * @param hex     十六进制
	 * @param charset 字符集
	 * @return 字符串
	 */
	public static String decodeHexStr(String hex, Charset charset) {
		byte[] bytes = decodeHex(hex);
		return bytes == null ? null : new String(bytes, charset);
	}

	private static String encodeHex(byte[] bytes, char[] digits) {
		char[] out = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			out[i * 2] = digits[v >>> 4];
			out[i * 2 + 1] = digits[v & 0x0F];
		}
		return new String(out);
	}

	/**
	 * 判断字符串是否为十六进制数（可含 {@code 0x}/{@code 0X} 前缀，忽略空白）。
	 *
	 * @param str 字符串
	 * @return 是否为十六进制数
	 */
	public static boolean isHexNumber(String str) {
		if (str == null) {
			return false;
		}
		String s = str.trim();
		if (s.isEmpty()) {
			return false;
		}
		int start = 0;
		if (s.startsWith("0x") || s.startsWith("0X")) {
			start = 2;
			if (s.length() == 2) {
				return false;
			}
		}
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
				return false;
			}
		}
		return true;
	}
}
