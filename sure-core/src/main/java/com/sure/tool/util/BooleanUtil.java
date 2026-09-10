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
 * 布尔工具类，参考 Hutool 的 {@code BooleanUtil} 设计。
 *
 * <p>支持多种字符串形式解析布尔值。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class BooleanUtil {

	private BooleanUtil() {
	}

	/**
	 * 字符串转布尔值，支持：true/false、yes/no、y/n、on/off、1/0、是/否、真/假、对/错（忽略大小写）。
	 *
	 * @param value 字符串
	 * @return 无法识别时返回 {@code false}
	 */
	public static boolean toBoolean(String value) {
		return toBoolean(value, false);
	}

	/**
	 * 字符串转布尔值，无法识别时返回默认值。
	 *
	 * @param value        字符串
	 * @param defaultValue 默认值
	 * @return 布尔值
	 */
	public static boolean toBoolean(String value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		String v = value.trim().toLowerCase();
		switch (v) {
			case "true":
			case "yes":
			case "y":
			case "t":
			case "on":
			case "1":
			case "是":
			case "真":
			case "对":
				return true;
			case "false":
			case "no":
			case "n":
			case "f":
			case "off":
			case "0":
			case "否":
			case "假":
			case "错":
				return false;
			default:
				return defaultValue;
		}
	}

	/**
	 * 字符串转 {@link Boolean}，无法识别时返回 {@code null}。
	 *
	 * @param value 字符串
	 * @return {@link Boolean} 或 {@code null}
	 */
	public static Boolean toBooleanObj(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim().toLowerCase();
		switch (v) {
			case "true":
			case "yes":
			case "y":
			case "t":
			case "on":
			case "1":
			case "是":
			case "真":
			case "对":
				return Boolean.TRUE;
			case "false":
			case "no":
			case "n":
			case "f":
			case "off":
			case "0":
			case "否":
			case "假":
			case "错":
				return Boolean.FALSE;
			default:
				return null;
		}
	}

	/**
	 * 是否为 {@code true}。
	 *
	 * @param value 布尔值
	 * @return {@code value == Boolean.TRUE}
	 */
	public static boolean isTrue(Boolean value) {
		return Boolean.TRUE.equals(value);
	}

	/**
	 * 是否为 {@code false}。
	 *
	 * @param value 布尔值
	 * @return {@code value == Boolean.FALSE}
	 */
	public static boolean isFalse(Boolean value) {
		return Boolean.FALSE.equals(value);
	}

	/**
	 * 取反。
	 *
	 * @param value 布尔值
	 * @return 取反结果
	 */
	public static boolean negate(boolean value) {
		return !value;
	}

	/**
	 * 布尔值转 int（true → 1，false → 0）。
	 *
	 * @param value 布尔值
	 * @return 1 或 0
	 */
	public static int toInt(boolean value) {
		return value ? 1 : 0;
	}

	/**
	 * 布尔值转字符串 {@code "true"/"false"}。
	 *
	 * @param value 布尔值
	 * @return 字符串
	 */
	public static String toStringTrueFalse(boolean value) {
		return value ? "true" : "false";
	}

	/**
	 * 布尔值转字符串 {@code "yes"/"no"}。
	 *
	 * @param value 布尔值
	 * @return 字符串
	 */
	public static String toStringYesNo(boolean value) {
		return value ? "yes" : "no";
	}

	/**
	 * 多个布尔值求或。
	 *
	 * @param values 布尔值数组
	 * @return 任意一个为 true 则返回 true
	 */
	public static boolean or(boolean... values) {
		if (values == null) {
			return false;
		}
		for (boolean value : values) {
			if (value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 多个布尔值求与。
	 *
	 * @param values 布尔值数组
	 * @return 全部为 true 才返回 true
	 */
	public static boolean and(boolean... values) {
		if (values == null) {
			return false;
		}
		for (boolean value : values) {
			if (!value) {
				return false;
			}
		}
		return true;
	}
}