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
package com.sure.tool.codec;

import com.sure.tool.util.CharUtil;
import com.sure.tool.util.StrUtil;

/**
 * 十六进制编解码工具类，参考 Hutool 的 {@code HexUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HexUtil {

	private static final char[] DIGITS_LOWER = "0123456789abcdef".toCharArray();
	private static final char[] DIGITS_UPPER = "0123456789ABCDEF".toCharArray();

	private HexUtil() {
	}

	/**
	 * 字节数组编码为十六进制字符串（小写）。
	 *
	 * @param data 字节数组
	 * @return 十六进制字符串
	 */
	public static String encodeHexStr(byte[] data) {
		return encodeHexStr(data, true);
	}

	/**
	 * 字节数组编码为十六进制字符串。
	 *
	 * @param data         字节数组
	 * @param toLowerCase  是否小写
	 * @return 十六进制字符串
	 */
	public static String encodeHexStr(byte[] data, boolean toLowerCase) {
		return new String(encodeHex(data, toLowerCase));
	}

	/**
	 * 字节数组编码为十六进制字符数组（小写）。
	 *
	 * @param data 字节数组
	 * @return 十六进制字符数组
	 */
	public static char[] encodeHex(byte[] data) {
		return encodeHex(data, true);
	}

	/**
	 * 字节数组编码为十六进制字符数组。
	 *
	 * @param data        字节数组
	 * @param toLowerCase 是否小写
	 * @return 十六进制字符数组
	 */
	public static char[] encodeHex(byte[] data, boolean toLowerCase) {
		char[] out = new char[data.length << 1];
		char[] digits = toLowerCase ? DIGITS_LOWER : DIGITS_UPPER;
		for (int i = 0; i < data.length; i++) {
			int v = data[i] & 0xFF;
			out[i << 1] = digits[v >>> 4];
			out[(i << 1) + 1] = digits[v & 0x0F];
		}
		return out;
	}

	/**
	 * 十六进制字符串解码为字节数组。
	 *
	 * @param hexStr 十六进制字符串
	 * @return 字节数组
	 */
	public static byte[] decodeHex(String hexStr) {
		if (StrUtil.isBlank(hexStr)) {
			return new byte[0];
		}
		char[] data = hexStr.toCharArray();
		int len = data.length;
		if ((len & 0x01) != 0) {
			throw new IllegalArgumentException("十六进制字符串长度必须为偶数: " + hexStr);
		}
		byte[] out = new byte[len >> 1];
		for (int i = 0, j = 0; j < len; i++) {
			int high = toDigit(data[j], j);
			int low = toDigit(data[j + 1], j + 1);
			out[i] = (byte) ((high << 4) | low);
			j += 2;
		}
		return out;
	}

	private static int toDigit(char ch, int index) {
		int digit = CharUtil.digitValue(ch);
		if (digit < 0) {
			throw new IllegalArgumentException("非法的十六进制字符 '" + ch + "'，位置 " + index);
		}
		return digit;
	}

	/**
	 * 是否为十六进制数字字符串（支持 {@code 0x} / {@code 0X} 前缀）。
	 *
	 * @param str 字符串
	 * @return 是否为十六进制数字
	 */
	public static boolean isHexNumber(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		String s = str.trim();
		if (s.startsWith("0x") || s.startsWith("0X")) {
			s = s.substring(2);
		}
		if (s.isEmpty()) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!CharUtil.isHexChar(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}