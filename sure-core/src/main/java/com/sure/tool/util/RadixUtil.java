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

/**
 * 进制转换工具：2~36 进制与十进制互转、任意进制间转换，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class RadixUtil {

	private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private RadixUtil() {
	}

	/**
	 * 十进制转指定进制（2~36，大写字母）。
	 *
	 * @param value 十进制值
	 * @param radix 进制（2~36）
	 * @return 指定进制字符串
	 */
	public static String toString(long value, int radix) {
		checkRadix(radix);
		if (value == 0) {
			return "0";
		}
		boolean negative = value < 0;
		long v = Math.abs(value);
		StringBuilder sb = new StringBuilder();
		while (v > 0) {
			sb.append(DIGITS.charAt((int) (v % radix)));
			v /= radix;
		}
		if (negative) {
			sb.append('-');
		}
		return sb.reverse().toString();
	}

	/**
	 * 指定进制转十进制。
	 *
	 * @param value 指定进制字符串（大小写均可）
	 * @param radix 进制（2~36）
	 * @return 十进制值
	 */
	public static long toLong(String value, int radix) {
		checkRadix(radix);
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("进制字符串不能为空");
		}
		String s = value.trim().toUpperCase();
		boolean negative = s.startsWith("-");
		if (negative || s.startsWith("+")) {
			s = s.substring(1);
		}
		long result = 0;
		for (int i = 0; i < s.length(); i++) {
			int digit = DIGITS.indexOf(s.charAt(i));
			if (digit < 0 || digit >= radix) {
				throw new IllegalArgumentException("非法字符: " + s.charAt(i) + "（进制 " + radix + "）");
			}
			result = result * radix + digit;
		}
		return negative ? -result : result;
	}

	/**
	 * 任意进制间转换（如 16 进制转 2 进制）。
	 *
	 * @param value    原进制字符串
	 * @param fromRadix 原进制
	 * @param toRadix   目标进制
	 * @return 目标进制字符串
	 */
	public static String convert(String value, int fromRadix, int toRadix) {
		return toString(toLong(value, fromRadix), toRadix);
	}

	private static void checkRadix(int radix) {
		if (radix < 2 || radix > 36) {
			throw new IllegalArgumentException("进制范围 2~36: " + radix);
		}
	}
}
