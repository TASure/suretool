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

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字符串工具类，参考 Hutool 的 {@code StrUtil} 设计。
 *
 * <p>提供判空、去空白、截取、拼接、格式化、驼峰转换等常用静态方法。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class StrUtil {

	/** 空字符串 */
	public static final String EMPTY = "";
	/** 空格 */
	public static final String SPACE = " ";

	private StrUtil() {
	}

	// ---------------- 判空 ----------------

	/**
	 * 字符串是否为空：{@code null} 或长度为 0。
	 *
	 * @param str 字符串
	 * @return 是否为空
	 */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 字符串是否非空。
	 *
	 * @param str 字符串
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(CharSequence str) {
		return !isEmpty(str);
	}

	/**
	 * 字符串是否为空白：{@code null}、空串或全为空白字符。
	 *
	 * @param str 字符串
	 * @return 是否空白
	 */
	public static boolean isBlank(CharSequence str) {
		int length;
		if ((str == null) || ((length = str.length()) == 0)) {
			return true;
		}
		for (int i = 0; i < length; i++) {
			if (!CharUtil.isBlankChar(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 字符串是否非空白。
	 *
	 * @param str 字符串
	 * @return 是否非空白
	 */
	public static boolean isNotBlank(CharSequence str) {
		return !isBlank(str);
	}

	/**
	 * 多个字符串中是否存在空白。
	 *
	 * @param strs 字符串数组
	 * @return 是否存在空白
	 */
	public static boolean hasBlank(CharSequence... strs) {
		if (strs == null) {
			return true;
		}
		for (CharSequence str : strs) {
			if (isBlank(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 多个字符串是否全部为空白。
	 *
	 * @param strs 字符串数组
	 * @return 是否全部空白
	 */
	public static boolean isAllBlank(CharSequence... strs) {
		if (strs == null) {
			return true;
		}
		for (CharSequence str : strs) {
			if (isNotBlank(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 多个字符串中是否存在空串（{@code null} 或长度为 0）。
	 *
	 * @param strs 字符串数组
	 * @return 是否存在空串
	 */
	public static boolean hasEmpty(CharSequence... strs) {
		if (strs == null) {
			return true;
		}
		for (CharSequence str : strs) {
			if (isEmpty(str)) {
				return true;
			}
		}
		return false;
	}

	// ---------------- 去空白 ----------------

	/**
	 * 去除字符串首尾空白（使用 {@link String#trim()} 语义）。
	 *
	 * @param str 字符串
	 * @return 去空白后的字符串，{@code null} 时返回空串
	 */
	public static String trim(CharSequence str) {
		return (str == null) ? EMPTY : str.toString().trim();
	}

	/**
	 * 去除首尾空白，结果为空白时返回 {@code null}。
	 *
	 * @param str 字符串
	 * @return 去空白后的字符串或 {@code null}
	 */
	public static String trimToNull(CharSequence str) {
		String s = trim(str);
		return s.isEmpty() ? null : s;
	}

	/**
	 * 去除首尾空白，结果为空白时返回空串。
	 *
	 * @param str 字符串
	 * @return 去空白后的字符串
	 */
	public static String trimToEmpty(CharSequence str) {
		return trim(str);
	}

	/**
	 * 去除字符串中所有空白字符（包括空格、制表符、换行等）。
	 *
	 * @param str 字符串
	 * @return 去除所有空白后的字符串
	 */
	public static String cleanBlank(CharSequence str) {
		if (str == null) {
			return EMPTY;
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!CharUtil.isBlankChar(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// ---------------- 比较与包含 ----------------

	/**
	 * 比较两个字符串是否相等。
	 *
	 * @param str1 字符串 1
	 * @param str2 字符串 2
	 * @return 是否相等
	 */
	public static boolean equals(CharSequence str1, CharSequence str2) {
		if (str1 == null) {
			return str2 == null;
		}
		if (str2 == null) {
			return false;
		}
		return str1.toString().equals(str2.toString());
	}

	/**
	 * 忽略大小写比较两个字符串是否相等。
	 *
	 * @param str1 字符串 1
	 * @param str2 字符串 2
	 * @return 是否相等
	 */
	public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
		if (str1 == null) {
			return str2 == null;
		}
		if (str2 == null) {
			return false;
		}
		return str1.toString().equalsIgnoreCase(str2.toString());
	}

	/**
	 * 是否包含指定子串。
	 *
	 * @param str       原字符串
	 * @param searchStr 要查找的子串
	 * @return 是否包含
	 */
	public static boolean contains(CharSequence str, CharSequence searchStr) {
		if (str == null || searchStr == null) {
			return false;
		}
		return str.toString().contains(searchStr);
	}

	/**
	 * 忽略大小写是否包含指定子串。
	 *
	 * @param str       原字符串
	 * @param searchStr 要查找的子串
	 * @return 是否包含
	 */
	public static boolean containsIgnoreCase(CharSequence str, CharSequence searchStr) {
		if (str == null || searchStr == null) {
			return false;
		}
		return str.toString().toLowerCase().contains(searchStr.toString().toLowerCase());
	}

	/**
	 * 是否包含任一指定子串。
	 *
	 * @param str       原字符串
	 * @param searchStr 要查找的子串数组
	 * @return 是否包含任意一个
	 */
	public static boolean containsAny(CharSequence str, CharSequence... searchStr) {
		if (str == null || searchStr == null) {
			return false;
		}
		for (CharSequence s : searchStr) {
			if (contains(str, s)) {
				return true;
			}
		}
		return false;
	}

	// ---------------- 前后缀 ----------------

	/**
	 * 是否以指定前缀开头。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @return 是否以该前缀开头
	 */
	public static boolean startWith(CharSequence str, CharSequence prefix) {
		return startWith(str, prefix, false);
	}

	/**
	 * 忽略大小写是否以指定前缀开头。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @return 是否以该前缀开头
	 */
	public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
		return startWith(str, prefix, true);
	}

	private static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
		if (str == null || prefix == null || str.length() < prefix.length()) {
			return false;
		}
		String head = str.subSequence(0, prefix.length()).toString();
		return ignoreCase ? head.equalsIgnoreCase(prefix.toString()) : head.equals(prefix.toString());
	}

	/**
	 * 是否以指定后缀结尾。
	 *
	 * @param str    字符串
	 * @param suffix 后缀
	 * @return 是否以该后缀结尾
	 */
	public static boolean endWith(CharSequence str, CharSequence suffix) {
		return endWith(str, suffix, false);
	}

	/**
	 * 忽略大小写是否以指定后缀结尾。
	 *
	 * @param str    字符串
	 * @param suffix 后缀
	 * @return 是否以该后缀结尾
	 */
	public static boolean endWithIgnoreCase(CharSequence str, CharSequence suffix) {
		return endWith(str, suffix, true);
	}

	private static boolean endWith(CharSequence str, CharSequence suffix, boolean ignoreCase) {
		if (str == null || suffix == null || str.length() < suffix.length()) {
			return false;
		}
		String tail = str.subSequence(str.length() - suffix.length(), str.length()).toString();
		return ignoreCase ? tail.equalsIgnoreCase(suffix.toString()) : tail.equals(suffix.toString());
	}

	// ---------------- 截取与替换 ----------------

	/**
	 * 安全截取字符串，支持负索引（从末尾倒数）。
	 *
	 * @param str       字符串
	 * @param fromIndex 起始索引（含），负数表示从末尾倒数
	 * @param toIndex   结束索引（不含），负数表示从末尾倒数
	 * @return 截取结果，越界自动收敛
	 */
	public static String sub(CharSequence str, int fromIndex, int toIndex) {
		if (isEmpty(str)) {
			return EMPTY;
		}
		int len = str.length();
		if (fromIndex < 0) {
			fromIndex = len + fromIndex;
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (toIndex < 0) {
			toIndex = len + toIndex;
		}
		if (toIndex > len) {
			toIndex = len;
		}
		if (toIndex < fromIndex) {
			return EMPTY;
		}
		return str.subSequence(fromIndex, toIndex).toString();
	}

	/**
	 * 从起始索引截取到字符串末尾。
	 *
	 * @param str       字符串
	 * @param fromIndex 起始索引（含）
	 * @return 截取结果
	 */
	public static String sub(CharSequence str, int fromIndex) {
		return sub(str, fromIndex, (str == null) ? 0 : str.length());
	}

	/**
	 * 去除字符串开头的指定前缀。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @return 去除前缀后的字符串
	 */
	public static String removePrefix(CharSequence str, CharSequence prefix) {
		if (isEmpty(str) || isEmpty(prefix)) {
			return (str == null) ? EMPTY : str.toString();
		}
		if (startWith(str, prefix)) {
			return sub(str, prefix.length(), str.length());
		}
		return str.toString();
	}

	/**
	 * 忽略大小写去除字符串开头的指定前缀。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @return 去除前缀后的字符串
	 */
	public static String removePrefixIgnoreCase(CharSequence str, CharSequence prefix) {
		if (isEmpty(str) || isEmpty(prefix)) {
			return (str == null) ? EMPTY : str.toString();
		}
		if (startWithIgnoreCase(str, prefix)) {
			return sub(str, prefix.length(), str.length());
		}
		return str.toString();
	}

	/**
	 * 去除字符串结尾的指定后缀。
	 *
	 * @param str    字符串
	 * @param suffix 后缀
	 * @return 去除后缀后的字符串
	 */
	public static String removeSuffix(CharSequence str, CharSequence suffix) {
		if (isEmpty(str) || isEmpty(suffix)) {
			return (str == null) ? EMPTY : str.toString();
		}
		if (endWith(str, suffix)) {
			return sub(str, 0, str.length() - suffix.length());
		}
		return str.toString();
	}

	/**
	 * 忽略大小写去除字符串结尾的指定后缀。
	 *
	 * @param str    字符串
	 * @param suffix 后缀
	 * @return 去除后缀后的字符串
	 */
	public static String removeSuffixIgnoreCase(CharSequence str, CharSequence suffix) {
		if (isEmpty(str) || isEmpty(suffix)) {
			return (str == null) ? EMPTY : str.toString();
		}
		if (endWithIgnoreCase(str, suffix)) {
			return sub(str, 0, str.length() - suffix.length());
		}
		return str.toString();
	}

	/**
	 * 替换字符串中的全部指定子串。
	 *
	 * @param str         原字符串
	 * @param searchStr   被替换的子串
	 * @param replacement 替换后的子串
	 * @return 替换后的字符串
	 */
	public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement) {
		if (isEmpty(str) || searchStr == null) {
			return (str == null) ? EMPTY : str.toString();
		}
		if (searchStr.length() == 0) {
			return str.toString();
		}
		return str.toString().replace(searchStr, (replacement == null) ? EMPTY : replacement.toString());
	}

	// ---------------- 拼接与格式化 ----------------

	/**
	 * 使用分隔符拼接对象数组。
	 *
	 * @param delimiter 分隔符
	 * @param objs      对象数组
	 * @return 拼接结果
	 */
	public static String join(CharSequence delimiter, Object... objs) {
		if (objs == null) {
			return EMPTY;
		}
		return join(delimiter, Arrays.asList(objs));
	}

	/**
	 * 使用分隔符拼接可迭代集合。
	 *
	 * @param delimiter 分隔符
	 * @param iterable  可迭代集合
	 * @return 拼接结果
	 */
	public static String join(CharSequence delimiter, Iterable<?> iterable) {
		if (iterable == null) {
			return EMPTY;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object obj : iterable) {
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(obj);
			first = false;
		}
		return sb.toString();
	}

	/**
	 * 字符串格式化，使用 {@code {}} 作为占位符，如 {@code format("你好，{}", "世界")}。
	 *
	 * @param template 模板
	 * @param args     参数
	 * @return 格式化结果
	 */
	public static String format(CharSequence template, Object... args) {
		if (isEmpty(template)) {
			return EMPTY;
		}
		if (args == null || args.length == 0) {
			return template.toString();
		}
		String str = template.toString();
		StringBuilder sb = new StringBuilder(str.length() + args.length * 16);
		int argIndex = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '{' && i + 1 < str.length() && str.charAt(i + 1) == '}') {
				if (argIndex < args.length) {
					sb.append(args[argIndex++]);
				} else {
					sb.append("{}");
				}
				i++;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// ---------------- 重复与补齐 ----------------

	/**
	 * 重复字符串指定次数。
	 *
	 * @param str   字符串
	 * @param count 重复次数
	 * @return 重复后的字符串
	 */
	public static String repeat(CharSequence str, int count) {
		if (str == null) {
			return EMPTY;
		}
		if (count <= 0) {
			return EMPTY;
		}
		StringBuilder sb = new StringBuilder(str.length() * count);
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 字符串前补齐至指定长度。
	 *
	 * @param str       字符串
	 * @param minLength 最小长度
	 * @param padChar   补齐字符
	 * @return 补齐后的字符串
	 */
	public static String padPre(CharSequence str, int minLength, char padChar) {
		if (str == null) {
			return EMPTY;
		}
		int len = str.length();
		if (len >= minLength) {
			return str.toString();
		}
		return repeat(String.valueOf(padChar), minLength - len) + str;
	}

	/**
	 * 字符串后补齐至指定长度。
	 *
	 * @param str       字符串
	 * @param minLength 最小长度
	 * @param padChar   补齐字符
	 * @return 补齐后的字符串
	 */
	public static String padAfter(CharSequence str, int minLength, char padChar) {
		if (str == null) {
			return EMPTY;
		}
		int len = str.length();
		if (len >= minLength) {
			return str.toString();
		}
		return str + repeat(String.valueOf(padChar), minLength - len);
	}

	// ---------------- 命名风格转换 ----------------

	/**
	 * 下划线命名转驼峰命名，如 {@code user_name} → {@code userName}。
	 *
	 * @param name 下划线命名
	 * @return 驼峰命名
	 */
	public static String toCamelCase(CharSequence name) {
		if (name == null) {
			return EMPTY;
		}
		String s = name.toString();
		if (s.indexOf('_') < 0) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s.length());
		boolean upperNext = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '_') {
				upperNext = true;
				continue;
			}
			if (upperNext) {
				sb.append(Character.toUpperCase(c));
				upperNext = false;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 驼峰命名转下划线命名，如 {@code userName} → {@code user_name}。
	 *
	 * @param str 驼峰命名
	 * @return 下划线命名
	 */
	public static String toUnderlineCase(CharSequence str) {
		if (str == null) {
			return EMPTY;
		}
		String s = str.toString();
		StringBuilder sb = new StringBuilder(s.length() + 4);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) {
					sb.append('_');
				}
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 首字母大写。
	 *
	 * @param str 字符串
	 * @return 首字母大写后的字符串
	 */
	public static String upperFirst(CharSequence str) {
		if (isEmpty(str)) {
			return EMPTY;
		}
		return Character.toUpperCase(str.charAt(0)) + sub(str, 1);
	}

	/**
	 * 首字母小写。
	 *
	 * @param str 字符串
	 * @return 首字母小写后的字符串
	 */
	public static String lowerFirst(CharSequence str) {
		if (isEmpty(str)) {
			return EMPTY;
		}
		return Character.toLowerCase(str.charAt(0)) + sub(str, 1);
	}

	// ---------------- 其他 ----------------

	/**
	 * 反转字符串。
	 *
	 * @param str 字符串
	 * @return 反转后的字符串
	 */
	public static String reverse(CharSequence str) {
		return new StringBuilder((str == null) ? EMPTY : str).reverse().toString();
	}

	/**
	 * 统计字符在字符串中出现的次数。
	 *
	 * @param str 字符串
	 * @param c   字符
	 * @return 出现次数
	 */
	public static int count(CharSequence str, char c) {
		if (isEmpty(str)) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 统计子串在字符串中出现的次数。
	 *
	 * @param str 字符串
	 * @param sub 子串
	 * @return 出现次数
	 */
	public static int count(CharSequence str, CharSequence sub) {
		if (isEmpty(str) || isEmpty(sub)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		String s = str.toString();
		String target = sub.toString();
		while ((idx = s.indexOf(target, idx)) != -1) {
			count++;
			idx += target.length();
		}
		return count;
	}

	/**
	 * 按单个字符分割字符串。
	 *
	 * @param str       字符串
	 * @param separator 分隔字符
	 * @return 分割后的字符串数组
	 */
	public static String[] split(CharSequence str, char separator) {
		if (isEmpty(str)) {
			return new String[0];
		}
		List<String> list = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == separator) {
				list.add(sb.toString());
				sb.setLength(0);
			} else {
				sb.append(c);
			}
		}
		list.add(sb.toString());
		return list.toArray(new String[0]);
	}

	/**
	 * 按字符串分割（分隔符中的正则特殊字符会被转义）。
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @return 分割后的字符串数组
	 */
	public static String[] split(CharSequence str, String separator) {
		if (isEmpty(str) || isEmpty(separator)) {
			return new String[]{ (str == null) ? EMPTY : str.toString() };
		}
		return str.toString().split(Pattern.quote(separator));
	}

	/**
	 * 对象转字符串，数组会展开输出，{@code null} 转为 {@code "null"}。
	 *
	 * @param obj 对象
	 * @return 字符串
	 */
	public static String toString(Object obj) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof CharSequence) {
			return obj.toString();
		}
		if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < len; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(toString(Array.get(obj, i)));
			}
			return sb.append(']').toString();
		}
		return obj.toString();
	}

	/**
	 * 字符串转字节数组。
	 *
	 * @param str     字符串
	 * @param charset 字符集
	 * @return 字节数组
	 */
	public static byte[] bytes(CharSequence str, Charset charset) {
		if (str == null) {
			return null;
		}
		return str.toString().getBytes(charset);
	}

	/**
	 * {@code null} 转空串。
	 *
	 * @param str 字符串
	 * @return 空串或原字符串
	 */
	public static String nullToEmpty(CharSequence str) {
		return (str == null) ? EMPTY : str.toString();
	}

	/**
	 * 空串转 {@code null}。
	 *
	 * @param str 字符串
	 * @return {@code null} 或原字符串
	 */
	public static String emptyToNull(CharSequence str) {
		return isEmpty(str) ? null : str.toString();
	}

	/**
	 * 空白字符串使用默认值替换。
	 *
	 * @param str         字符串
	 * @param defaultStr  默认值
	 * @return 非空白时返回原串，否则返回默认值
	 */
	public static String blankToDefault(CharSequence str, String defaultStr) {
		return isBlank(str) ? defaultStr : str.toString();
	}

	/**
	 * 空字符串使用默认值替换。
	 *
	 * @param str         字符串
	 * @param defaultStr  默认值
	 * @return 非空时返回原串，否则返回默认值
	 */
	public static String emptyToDefault(CharSequence str, String defaultStr) {
		return isEmpty(str) ? defaultStr : str.toString();
	}
}