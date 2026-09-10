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

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串分割工具类：支持限制段数、去空白、忽略空段与按长度分割，参考 Hutool 的 {@code StrSplitter} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class StrSplitter {

	private StrSplitter() {
	}

	/**
	 * 按字符分割，不去空白、不忽略空段。
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @return 段列表
	 */
	public static List<String> split(CharSequence str, char separator) {
		return split(str, separator, 0, false, false);
	}

	/**
	 * 按字符分割。
	 *
	 * @param str         字符串
	 * @param separator   分隔符
	 * @param limit       最大段数，0 或负数表示不限
	 * @param isTrim      是否去除每段首尾空白
	 * @param ignoreEmpty 是否忽略空段
	 * @return 段列表
	 */
	public static List<String> split(CharSequence str, char separator, int limit, boolean isTrim,
			boolean ignoreEmpty) {
		List<String> result = new ArrayList<>();
		if (str == null) {
			return result;
		}
		int length = str.length();
		int start = 0;
		for (int i = 0; i < length; i++) {
			if (str.charAt(i) == separator && (limit <= 0 || result.size() < limit - 1)) {
				addPart(result, str.subSequence(start, i).toString(), isTrim, ignoreEmpty);
				start = i + 1;
			}
		}
		if (start <= length) {
			addPart(result, str.subSequence(start, length).toString(), isTrim, ignoreEmpty);
		}
		return result;
	}

	/**
	 * 按字符分割为数组。
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @return 段数组
	 */
	public static String[] splitToArray(CharSequence str, char separator) {
		return split(str, separator).toArray(new String[0]);
	}

	/**
	 * 按字符串分隔符分割。
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @param isTrim    是否去除每段首尾空白
	 * @return 段列表
	 */
	public static List<String> split(CharSequence str, String separator, boolean isTrim) {
		List<String> result = new ArrayList<>();
		if (str == null) {
			return result;
		}
		if (separator == null || separator.isEmpty()) {
			result.add(str.toString());
			return result;
		}
		String text = str.toString();
		int start = 0;
		int index;
		while ((index = text.indexOf(separator, start)) >= 0) {
			addPart(result, text.substring(start, index), isTrim, false);
			start = index + separator.length();
		}
		addPart(result, text.substring(start), isTrim, false);
		return result;
	}

	/**
	 * 按长度均分，最后一段可为余数长度。
	 *
	 * @param str 字符串
	 * @param len 每段长度，须大于 0
	 * @return 段列表
	 */
	public static List<String> splitByLength(CharSequence str, int len) {
		List<String> result = new ArrayList<>();
		if (str == null || len <= 0) {
			return result;
		}
		String text = str.toString();
		int length = text.length();
		for (int i = 0; i < length; i += len) {
			result.add(text.substring(i, Math.min(i + len, length)));
		}
		return result;
	}

	/**
	 * 按路径分隔符（{@code /} 与 {@code \\}）分割。
	 *
	 * @param path 路径
	 * @return 段列表（不忽略空段，保留空路径语义）
	 */
	public static List<String> splitPath(CharSequence path) {
		return splitPath(path, 0);
	}

	/**
	 * 按路径分隔符分割。
	 *
	 * @param path  路径
	 * @param limit 最大段数，0 或负数表示不限
	 * @return 段列表
	 */
	public static List<String> splitPath(CharSequence path, int limit) {
		List<String> result = new ArrayList<>();
		if (path == null) {
			return result;
		}
		int length = path.length();
		int start = 0;
		for (int i = 0; i < length; i++) {
			char c = path.charAt(i);
			if ((c == '/' || c == '\\') && (limit <= 0 || result.size() < limit - 1)) {
				result.add(path.subSequence(start, i).toString());
				start = i + 1;
			}
		}
		if (start <= length) {
			result.add(path.subSequence(start, length).toString());
		}
		return result;
	}

	/**
	 * 按条件追加一段。
	 */
	private static void addPart(List<String> result, String part, boolean isTrim, boolean ignoreEmpty) {
		if (isTrim) {
			part = part.trim();
		}
		if (ignoreEmpty && part.isEmpty()) {
			return;
		}
		result.add(part);
	}
}
