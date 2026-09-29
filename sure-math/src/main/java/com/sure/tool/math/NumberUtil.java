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
package com.sure.tool.math;

/**
 * 数字解析与判断工具门面。
 *
 * <p>提供容错解析（非法输入回退默认值）与类型判断，适合配置项、命令行参数等
 * 不确定来源的数字转换场景。</p>
 *
 * @since 1.4.0
 */
public final class NumberUtil {

	private NumberUtil() {
	}

	/**
	 * 容错解析 int。
	 *
	 * @param s    字符串
	 * @param def  解析失败时的默认值
	 * @return 解析结果或默认值
	 */
	public static int parseInt(String s, int def) {
		if (s == null || s.isBlank()) {
			return def;
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * 容错解析 long。
	 *
	 * @param s   字符串
	 * @param def 解析失败时的默认值
	 * @return 解析结果或默认值
	 */
	public static long parseLong(String s, long def) {
		if (s == null || s.isBlank()) {
			return def;
		}
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * 容错解析 double。
	 *
	 * @param s   字符串
	 * @param def 解析失败时的默认值
	 * @return 解析结果或默认值
	 */
	public static double parseDouble(String s, double def) {
		if (s == null || s.isBlank()) {
			return def;
		}
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * 判断是否为合法 int 表示。
	 *
	 * @param s 字符串
	 * @return true 表示可解析为 int
	 */
	public static boolean isInteger(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 判断是否为合法 long 表示。
	 *
	 * @param s 字符串
	 * @return true 表示可解析为 long
	 */
	public static boolean isLong(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			Long.parseLong(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 判断是否为合法 double 表示。
	 *
	 * @param s 字符串
	 * @return true 表示可解析为 double
	 */
	public static boolean isDouble(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 判断是否为任意数值类型（int/long/double）表示。
	 *
	 * @param s 字符串
	 * @return true 表示可解析为数值
	 */
	public static boolean isNumber(String s) {
		return isInteger(s) || isLong(s) || isDouble(s);
	}

	/**
	 * 转 int，null/空/非法返回 0。
	 *
	 * @param s 字符串
	 * @return int 或 0
	 */
	public static int toInt(String s) {
		return parseInt(s, 0);
	}

	/**
	 * 转 long，null/空/非法返回 0L。
	 *
	 * @param s 字符串
	 * @return long 或 0L
	 */
	public static long toLong(String s) {
		return parseLong(s, 0L);
	}

	/**
	 * 求一组整数的最小值。
	 *
	 * @param values 整数序列（≥1 个）
	 * @return 最小值
	 * @throws IllegalArgumentException values 为空
	 */
	public static int min(int... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		int m = values[0];
		for (int i = 1; i < values.length; i++) {
			m = Math.min(m, values[i]);
		}
		return m;
	}

	/**
	 * 求一组整数的最大值。
	 *
	 * @param values 整数序列（≥1 个）
	 * @return 最大值
	 * @throws IllegalArgumentException values 为空
	 */
	public static int max(int... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		int m = values[0];
		for (int i = 1; i < values.length; i++) {
			m = Math.max(m, values[i]);
		}
		return m;
	}
}
