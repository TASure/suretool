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
import java.util.Map;
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
	 * 字符串格式化，使用一对花括号作为占位符，如 {@code format("你好，&#123;&#125;", "世界")}。
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

	/**
	 * 模板变量替换（默认前缀 {@code ${}、后缀 {@code }}、转义符 {@code $}），委托 {@link StrSubstitutor}。
	 *
	 * <p>支持默认值语法 {@code ${key:-default}}；变量缺失保留占位符原样；
	 * 值为 {@code null} 渲染为空串；{@code $$&#123;} 输出字面 {@code $&#123;}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String text = StrUtil.substitute("hi ${name}, env=${env:-dev}", Map.of("name", "sure"));
	 * // => "hi sure, env=dev"
	 * }</pre>
	 *
	 * @param template 模板文本；为 null 时返回 null
	 * @param vars     变量映射；为 null 时视为空映射
	 * @return 替换结果
	 * @since 1.19.0
	 */
	public static String substitute(String template, Map<String, ?> vars) {
		return StrSubstitutor.substitute(template, vars);
	}

	/**
	 * 模板变量替换（自定义定界符，默认转义符 {@code $}），委托 {@link StrSubstitutor}。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String text = StrUtil.substitute("&lt;%name%&gt; 你好", Map.of("name", "sure"), "&lt;%", "%&gt;");
	 * // => "sure 你好"
	 * }</pre>
	 *
	 * @param template 模板文本；为 null 时返回 null
	 * @param vars     变量映射；为 null 时视为空映射
	 * @param prefix   占位符前缀
	 * @param suffix   占位符后缀
	 * @return 替换结果
	 * @throws IllegalArgumentException 前缀或后缀为 null 或空串时抛出
	 * @since 1.19.0
	 */
	public static String substitute(String template, Map<String, ?> vars, String prefix, String suffix) {
		return StrSubstitutor.replace(template, vars, prefix, suffix);
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

	/**
	 * 掩码：将 [fromInclude, toExclude) 区间字符替换为掩码符。
	 *
	 * @param str        原字符串
	 * @param fromInclude 起始（含）
	 * @param toExclude   结束（不含）
	 * @param maskChar    掩码符
	 * @return 掩码后字符串；null 返回 null
	 */
	public static String hide(CharSequence str, int fromInclude, int toExclude, char maskChar) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		int from = Math.max(0, fromInclude);
		int to = Math.min(len, toExclude);
		if (from >= to) {
			return str.toString();
		}
		StringBuilder sb = new StringBuilder(len);
		sb.append(str, 0, from);
		sb.append(String.valueOf(maskChar).repeat(to - from));
		sb.append(str, to, len);
		return sb.toString();
	}

	/**
	 * 取两标记之间的内容（第一个匹配）。
	 *
	 * @param str    原字符串
	 * @param prefix 前标记
	 * @param suffix 后标记
	 * @return 中间内容；未匹配返回 null
	 */
	public static String subBetween(CharSequence str, CharSequence prefix, CharSequence suffix) {
		if (str == null || prefix == null || suffix == null) {
			return null;
		}
		String text = str.toString();
		String pre = prefix.toString();
		String suf = suffix.toString();
		int start = text.indexOf(pre);
		if (start < 0) {
			return null;
		}
		start += pre.length();
		int end = text.indexOf(suf, start);
		if (end < 0) {
			return null;
		}
		return text.substring(start, end);
	}

	/**
	 * 是否为数字字符串（允许正负号、小数点）。
	 *
	 * @param str 字符串
	 * @return 是否数字
	 */
	public static boolean isNumeric(CharSequence str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		String s = str.toString();
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 移除所有指定子串。
	 *
	 * @param str    原字符串
	 * @param values 要移除的子串
	 * @return 移除后字符串；null 返回 null
	 */
	public static String removeAll(CharSequence str, CharSequence... values) {
		if (str == null) {
			return null;
		}
		String result = str.toString();
		if (values == null) {
			return result;
		}
		for (CharSequence v : values) {
			if (v == null || v.isEmpty()) {
				continue;
			}
			result = result.replace(v.toString(), "");
		}
		return result;
	}

	/**
	 * 查找子串首次出现位置。
	 *
	 * @param str    原字符串
	 * @param search 子串
	 * @return 位置；未找到或参数为空返回 -1
	 */
	public static int indexOf(CharSequence str, CharSequence search) {
		if (str == null || search == null || search.isEmpty()) {
			return -1;
		}
		return str.toString().indexOf(search.toString());
	}

	/**
	 * 查找子串末次出现位置。
	 *
	 * @param str    原字符串
	 * @param search 子串
	 * @return 位置；未找到或参数为空返回 -1
	 */
	public static int lastIndexOf(CharSequence str, CharSequence search) {
		if (str == null || search == null || search.isEmpty()) {
			return -1;
		}
		return str.toString().lastIndexOf(search.toString());
	}

	/**
	 * 是否以任一前缀开头。
	 *
	 * @param str    字符串
	 * @param prefixes 前缀组
	 * @return 是否命中任一
	 */
	public static boolean startWithAny(CharSequence str, CharSequence... prefixes) {
		if (prefixes == null || prefixes.length == 0) {
			return false;
		}
		for (CharSequence prefix : prefixes) {
			if (startWith(str, prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否以任一后缀结尾。
	 *
	 * @param str    字符串
	 * @param suffixes 后缀组
	 * @return 是否命中任一
	 */
	public static boolean endWithAny(CharSequence str, CharSequence... suffixes) {
		if (suffixes == null || suffixes.length == 0) {
			return false;
		}
		for (CharSequence suffix : suffixes) {
			if (endWith(str, suffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 分隔为 int 数组（空白分隔），非法元素按 0 处理。
	 *
	 * @param str 数字字符串
	 * @return int 数组
	 */
	public static int[] splitToIntArray(String str) {
		if (isEmpty(str)) {
			return new int[0];
		}
		String[] parts = str.split("\\s+");
		int[] result = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				result[i] = Integer.parseInt(parts[i].trim());
			} catch (NumberFormatException e) {
				result[i] = 0;
			}
		}
		return result;
	}

	/**
	 * 分隔为 long 数组（空白分隔），非法元素按 0 处理。
	 *
	 * @param str 数字字符串
	 * @return long 数组
	 */
	public static long[] splitToLongArray(String str) {
		if (isEmpty(str)) {
			return new long[0];
		}
		String[] parts = str.split("\\s+");
		long[] result = new long[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				result[i] = Long.parseLong(parts[i].trim());
			} catch (NumberFormatException e) {
				result[i] = 0;
			}
		}
		return result;
	}

	/**
	 * 提取所有两两之间的内容。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 提取结果列表
	 */
	public static List<String> subBetweenAll(String str, String prefix, String suffix) {
		List<String> result = new ArrayList<>();
		if (isEmpty(str) || prefix == null || suffix == null) {
			return result;
		}
		int from = 0;
		while (true) {
			int start = str.indexOf(prefix, from);
			if (start < 0) {
				break;
			}
			int end = str.indexOf(suffix, start + prefix.length());
			if (end < 0) {
				break;
			}
			result.add(str.substring(start + prefix.length(), end));
			from = end + suffix.length();
		}
		return result;
	}

	/**
	 * 分隔为 double 数组（空白分隔），非法元素按 0 处理。
	 *
	 * @param str 数字字符串
	 * @return double 数组
	 */
	public static double[] splitToDoubleArray(String str) {
		if (isEmpty(str)) {
			return new double[0];
		}
		String[] parts = str.split("\\s+");
		double[] result = new double[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				result[i] = Double.parseDouble(parts[i].trim());
			} catch (NumberFormatException e) {
				result[i] = 0;
			}
		}
		return result;
	}

	/**
	 * 去除指定前缀（无前缀则原样返回）。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @return 结果
	 */
	public static String removePrefix(String str, String prefix) {
		if (isEmpty(str) || isEmpty(prefix)) {
			return str;
		}
		return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
	}

	/**
	 * 去除指定后缀（无后缀则原样返回）。
	 *
	 * @param str    字符串
	 * @param suffix 后缀
	 * @return 结果
	 */
	public static String removeSuffix(String str, String suffix) {
		if (isEmpty(str) || isEmpty(suffix)) {
			return str;
		}
		return str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str;
	}

	/**
	 * 截取尾部 n 个字符。
	 *
	 * @param str 字符串
	 * @param len 长度
	 * @return 尾部字符串
	 */
	public static String subSuf(String str, int len) {
		if (isEmpty(str)) {
			return str;
		}
		return len <= 0 ? "" : str.substring(Math.max(0, str.length() - len));
	}

	/**
	 * 截取头部 n 个字符。
	 *
	 * @param str 字符串
	 * @param len 长度
	 * @return 头部字符串
	 */
	public static String subPre(String str, int len) {
		if (isEmpty(str)) {
			return str;
		}
		return len <= 0 ? "" : str.substring(0, Math.min(str.length(), len));
	}

	/**
	 * 按分隔符拆分并去除每个元素首尾空白（保留空元素）。
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @return 拆分结果
	 */
	public static String[] splitTrim(String str, String separator) {
		if (isEmpty(str)) {
			return new String[0];
		}
		if (isEmpty(separator)) {
			return new String[] {str.trim()};
		}
		String[] parts = str.split(java.util.regex.Pattern.quote(separator), -1);
		String[] result = new String[parts.length];
		for (int i = 0; i < parts.length; i++) {
			result[i] = parts[i].trim();
		}
		return result;
	}

	/**
	 * 空字符串（含空白）返回默认值，否则原样返回。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return 结果
	 */
	public static String blankToDefault(String str, String defaultValue) {
		return isBlank(str) ? defaultValue : str;
	}

	/**
	 * 判断是否全部为大写字母（忽略非字母字符）。
	 *
	 * @param str 字符串
	 * @return 是否全大写；空串返回 false
	 */
	public static boolean isUpperCase(String str) {
		if (isEmpty(str)) {
			return false;
		}
		boolean hasLetter = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetter(c)) {
				hasLetter = true;
				if (!Character.isUpperCase(c)) {
					return false;
				}
			}
		}
		return hasLetter;
	}

	/**
	 * 判断是否全部为小写字母（忽略非字母字符）。
	 *
	 * @param str 字符串
	 * @return 是否全小写；空串返回 false
	 */
	public static boolean isLowerCase(String str) {
		if (isEmpty(str)) {
			return false;
		}
		boolean hasLetter = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetter(c)) {
				hasLetter = true;
				if (!Character.isLowerCase(c)) {
					return false;
				}
			}
		}
		return hasLetter;
	}

	/**
	 * 前补位：字符串不足指定长度时在前面填充字符。
	 *
	 * @param str     字符串
	 * @param minLen  最小长度
	 * @param padChar 填充字符
	 * @return 补位后的字符串
	 */
	public static String fillBefore(String str, int minLen, char padChar) {
		if (str == null) {
			return null;
		}
		if (str.length() >= minLen) {
			return str;
		}
		StringBuilder sb = new StringBuilder(minLen);
		for (int i = str.length(); i < minLen; i++) {
			sb.append(padChar);
		}
		return sb.append(str).toString();
	}

	/**
	 * 后补位：字符串不足指定长度时在后面填充字符。
	 *
	 * @param str     字符串
	 * @param minLen  最小长度
	 * @param padChar 填充字符
	 * @return 补位后的字符串
	 */
	public static String fillAfter(String str, int minLen, char padChar) {
		if (str == null) {
			return null;
		}
		if (str.length() >= minLen) {
			return str;
		}
		StringBuilder sb = new StringBuilder(minLen);
		sb.append(str);
		for (int i = str.length(); i < minLen; i++) {
			sb.append(padChar);
		}
		return sb.toString();
	}

	/**
	 * 转 Unicode 字符串（中文转 \u005cuXXXX，ASCII 保留）。
	 *
	 * @param str 字符串
	 * @return Unicode 表示
	 */
	public static String toUnicode(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c > 127) {
				sb.append('\\').append('u').append(String.format("%04x", (int) c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 限制字符串最大长度，超出截断并追加省略号。
	 *
	 * @param str     字符串
	 * @param maxLength 最大长度（含省略号）
	 * @return 截断后的字符串
	 */
	public static String maxLength(String str, int maxLength) {
		if (str == null) {
			return null;
		}
		if (maxLength <= 0) {
			return "";
		}
		if (str.length() <= maxLength) {
			return str;
		}
		if (maxLength <= 3) {
			return str.substring(0, maxLength);
		}
		return str.substring(0, maxLength - 3) + "...";
	}

	/**
	 * 去除字符串首尾的指定字符。
	 *
	 * @param str      字符串
	 * @param stripChar 要去除的字符
	 * @return 处理后的字符串
	 */
	public static String strip(String str, char stripChar) {
		if (str == null) {
			return null;
		}
		int start = 0;
		int end = str.length();
		while (start < end && str.charAt(start) == stripChar) {
			start++;
		}
		while (end > start && str.charAt(end - 1) == stripChar) {
			end--;
		}
		return str.substring(start, end);
	}

	/**
	 * 去除字符串首尾的空白字符。
	 *
	 * @param str 字符串
	 * @return 处理后的字符串
	 */
	public static String strip(String str) {
		return strip(str, ' ');
	}

	/**
	 * 将字符串居中对齐，两侧以指定字符填充。
	 *
	 * @param str      字符串
	 * @param size     目标长度
	 * @param fillChar 填充字符
	 * @return 居中对齐后的字符串
	 */
	public static String center(String str, int size, char fillChar) {
		return center(str, size, String.valueOf(fillChar));
	}

	/**
	 * 将字符串居中对齐，两侧以指定字符串填充。
	 *
	 * @param str  字符串
	 * @param size 目标长度
	 * @param fill 填充串
	 * @return 居中对齐后的字符串
	 */
	public static String center(String str, int size, String fill) {
		if (str == null) {
			return null;
		}
		if (size <= str.length()) {
			return str;
		}
		int total = size - str.length();
		int left = total / 2;
		int right = total - left;
		return repeat(fill, left) + str + repeat(fill, right);
	}

	/**
	 * 返回第一个非空白字符串。
	 *
	 * @param values 候选字符串
	 * @return 第一个非空白值；全为空返回 null
	 */
	public static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (isNotBlank(value)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * 拼接多个字符串为一个字符串。
	 *
	 * @param values 字符串数组
	 * @return 拼接结果
	 */
	public static String concat(String... values) {
		if (values == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			sb.append(value);
		}
		return sb.toString();
	}

	/**
	 * 交换字符串中字母的大小写。
	 *
	 * @param str 字符串
	 * @return 大小写互换后的字符串
	 */
	public static String swapCase(String str) {
		if (str == null) {
			return null;
		}
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c)) {
				chars[i] = Character.toLowerCase(c);
			} else if (Character.isLowerCase(c)) {
				chars[i] = Character.toUpperCase(c);
			}
		}
		return new String(chars);
	}

	/**
	 * 判断字符串数组中是否全部非空白。
	 *
	 * @param strs 字符串数组
	 * @return 是否全部非空白
	 */
	public static boolean isAllNotBlank(CharSequence... strs) {
		if (strs == null) {
			return false;
		}
		for (CharSequence str : strs) {
			if (isBlank(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断字符串数组中是否全部为空。
	 *
	 * @param strs 字符串数组
	 * @return 是否全部为空
	 */
	public static boolean isAllEmpty(CharSequence... strs) {
		if (strs == null) {
			return true;
		}
		for (CharSequence str : strs) {
			if (!isEmpty(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 替换字符串中指定区间（含 begin，不含 end）的内容。
	 *
	 * @param str         字符串
	 * @param beginIndex  起始索引（含）
	 * @param endIndex    结束索引（不含）
	 * @param replacement 替换串
	 * @return 替换后的字符串
	 */
	public static String replace(CharSequence str, int beginIndex, int endIndex, CharSequence replacement) {
		if (str == null) {
			return null;
		}
		String s = str.toString();
		int begin = Math.max(0, beginIndex);
		int end = Math.min(s.length(), endIndex);
		if (begin >= end) {
			return s;
		}
		String rep = replacement == null ? "" : replacement.toString();
		return s.substring(0, begin) + rep + s.substring(end);
	}

	/**
	 * 忽略大小写判断字符串是否包含任意给定子串。
	 *
	 * @param str        字符串
	 * @param searchStrs 子串列表
	 * @return 是否包含任意一个
	 */
	public static boolean containsAnyIgnoreCase(CharSequence str, CharSequence... searchStrs) {
		if (str == null || searchStrs == null) {
			return false;
		}
		String lower = str.toString().toLowerCase();
		for (CharSequence search : searchStrs) {
			if (search != null && lower.contains(search.toString().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 忽略大小写替换所有匹配子串。
	 *
	 * @param str         字符串
	 * @param searchStr   查找子串
	 * @param replacement 替换串
	 * @return 替换后的字符串
	 */
	public static String replaceIgnoreCase(CharSequence str, CharSequence searchStr, CharSequence replacement) {
		if (str == null) {
			return null;
		}
		if (searchStr == null || searchStr.isEmpty()) {
			return str.toString();
		}
		String s = str.toString();
		String search = searchStr.toString();
		String rep = replacement == null ? "" : replacement.toString();
		String lower = s.toLowerCase();
		String lowerSearch = search.toLowerCase();
		StringBuilder sb = new StringBuilder();
		int from = 0;
		int idx;
		while ((idx = lower.indexOf(lowerSearch, from)) >= 0) {
			sb.append(s, from, idx).append(rep);
			from = idx + search.length();
		}
		sb.append(s, from, s.length());
		return sb.toString();
	}

	// ---------------- 包裹处理 ----------------

	/**
	 * 判断字符串是否被指定前后缀包裹。
	 *
	 * <p>例如 {@code isWrap("[abc]", "[", "]")} 返回 {@code true}。</p>
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 是否被包裹
	 */
	public static boolean isWrap(CharSequence str, String prefix, String suffix) {
		if (str == null || prefix == null || prefix.isEmpty() || suffix == null || suffix.isEmpty()) {
			return false;
		}
		String s = str.toString();
		return s.startsWith(prefix) && s.endsWith(suffix);
	}

	/**
	 * 使用指定前后缀包裹字符串。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 包裹后的字符串；{@code str} 为 {@code null} 时返回 {@code null}
	 */
	public static String wrap(CharSequence str, CharSequence prefix, CharSequence suffix) {
		if (str == null) {
			return null;
		}
		String pre = prefix == null ? "" : prefix.toString();
		String suf = suffix == null ? "" : suffix.toString();
		return pre + str + suf;
	}

	/**
	 * 去除字符串包裹（仅当首尾实际匹配时去除，否则原样返回）。
	 *
	 * @param str    字符串
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 去除包裹后的字符串；{@code str} 为 {@code null} 时返回 {@code null}
	 */
	public static String unWrap(CharSequence str, CharSequence prefix, CharSequence suffix) {
		if (str == null) {
			return null;
		}
		String s = str.toString();
		String pre = prefix == null ? "" : prefix.toString();
		String suf = suffix == null ? "" : suffix.toString();
		if (s.startsWith(pre) && s.endsWith(suf)) {
			return s.substring(pre.length(), s.length() - suf.length());
		}
		return s;
	}

	// ---------------- 等值判断与长度 ----------------

	/**
	 * 判断字符串是否等于给定值中的任意一个。
	 *
	 * @param str    字符串
	 * @param values 候选值
	 * @return 是否等于任意一个
	 */
	public static boolean equalsAny(CharSequence str, CharSequence... values) {
		if (values == null || values.length == 0) {
			return false;
		}
		for (CharSequence v : values) {
			if (equals(str, v)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 忽略大小写判断字符串是否等于给定值中的任意一个。
	 *
	 * @param str    字符串
	 * @param values 候选值
	 * @return 是否等于任意一个
	 */
	public static boolean equalsAnyIgnoreCase(CharSequence str, CharSequence... values) {
		if (values == null || values.length == 0) {
			return false;
		}
		for (CharSequence v : values) {
			if (equalsIgnoreCase(str, v)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 计算多个字符串的总长度（{@code null} 视为空串）。
	 *
	 * @param strs 字符串数组
	 * @return 总长度
	 */
	public static int totalLength(CharSequence... strs) {
		int total = 0;
		if (strs != null) {
			for (CharSequence s : strs) {
				total += (s == null) ? 0 : s.length();
			}
		}
		return total;
	}

	/**
	 * 截取指定分隔符之前的子串。
	 *
	 * @param str         原字符串
	 * @param separator   分隔符
	 * @param isLastMatch 是否匹配最后一个分隔符
	 * @return 分隔符之前的子串；{@code str} 为 {@code null} 返回 {@code null}；无分隔符返回原串
	 * @since 1.0.1
	 */
	public static String subBefore(String str, String separator, boolean isLastMatch) {
		if (str == null || isEmpty(separator)) {
			return str;
		}
		int pos = isLastMatch ? str.lastIndexOf(separator) : str.indexOf(separator);
		if (pos == -1) {
			return str;
		}
		if (pos == 0) {
			return "";
		}
		return str.substring(0, pos);
	}

	/**
	 * 截取指定分隔符之后的子串。
	 *
	 * @param str         原字符串
	 * @param separator   分隔符
	 * @param isLastMatch 是否匹配最后一个分隔符
	 * @return 分隔符之后的子串；{@code str} 为 {@code null} 返回 {@code null}；无分隔符返回原串
	 * @since 1.0.1
	 */
	public static String subAfter(String str, String separator, boolean isLastMatch) {
		if (str == null || isEmpty(separator)) {
			return str;
		}
		int pos = isLastMatch ? str.lastIndexOf(separator) : str.indexOf(separator);
		if (pos == -1) {
			return str;
		}
		if (pos == str.length() - separator.length()) {
			return "";
		}
		return str.substring(pos + separator.length());
	}

	/**
	 * 去除字符串首部空白字符。
	 *
	 * @param str 原字符串
	 * @return 去除首部空白后的字符串；{@code null} 返回 {@code null}
	 * @since 1.0.1
	 */
	public static String trimStart(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		int len = str.length();
		int start = 0;
		while (start < len && Character.isWhitespace(str.charAt(start))) {
			start++;
		}
		return start == 0 ? str : str.substring(start);
	}

	/**
	 * 去除字符串尾部空白字符。
	 *
	 * @param str 原字符串
	 * @return 去除尾部空白后的字符串；{@code null} 返回 {@code null}
	 * @since 1.0.1
	 */
	public static String trimEnd(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		int end = str.length();
		while (end > 0 && Character.isWhitespace(str.charAt(end - 1))) {
			end--;
		}
		return end == str.length() ? str : str.substring(0, end);
	}

	/**
	 * 用指定包裹字符串将目标字符串首尾包裹（如用引号包裹）。
	 *
	 * @param str  目标字符串
	 * @param wrap 包裹字符串
	 * @return 包裹后的字符串；{@code str} 为 {@code null} 返回 {@code null}
	 * @since 1.0.1
	 */
	public static String surround(CharSequence str, CharSequence wrap) {
		if (str == null) {
			return null;
		}
		if (wrap == null) {
			return str.toString();
		}
		return wrap + str.toString() + wrap;
	}

	/**
	 * 截断字符串：超过 maxWidth 时保留前 maxWidth-3 个字符并以 {@code ...} 结尾。
	 *
	 * @param str      原字符串
	 * @param maxWidth 最大宽度，小于 4 视为 4
	 * @return 截断后的字符串；str 为 null 返回 null
	 */
	public static String abbreviate(CharSequence str, int maxWidth) {
		return abbreviate(str, 0, maxWidth);
	}

	/**
	 * 截断字符串：从指定偏移开始，超过 maxWidth 时以 {@code ...} 结尾。
	 *
	 * @param str      原字符串
	 * @param offset   起始偏移（越界自动归位）
	 * @param maxWidth 最大宽度，小于 4 视为 4
	 * @return 截断后的字符串；str 为 null 返回 null
	 */
	public static String abbreviate(CharSequence str, int offset, int maxWidth) {
		if (str == null) {
			return null;
		}
		if (maxWidth < 4) {
			maxWidth = 4;
		}
		if (str.length() <= maxWidth) {
			return str.toString();
		}
		if (offset > str.length()) {
			offset = str.length();
		}
		if (str.length() - offset < maxWidth - 3) {
			offset = str.length() - (maxWidth - 3);
		}
		if (offset <= 4) {
			return str.subSequence(0, maxWidth - 3) + "...";
		}
		return "..." + str.subSequence(offset, offset + maxWidth - 3) + "...";
	}

	/**
	 * 中间省略：保留首尾字符，中间以指定省略串连接（参考 commons-lang {@code StringUtils.abbreviateMiddle}）。
	 *
	 * @param str    原字符串
	 * @param middle 省略串（如 {@code "..."}）
	 * @param length 目标总长度（含省略串），小于 middle 长度时直接返回 middle
	 * @return 中间省略后的字符串；str 为 null 返回 null
	 */
	public static String abbreviateMiddle(CharSequence str, String middle, int length) {
		if (str == null) {
			return null;
		}
		if (isEmpty(middle)) {
			return str.toString();
		}
		if (length >= str.length() || length < middle.length()) {
			return str.toString();
		}
		int targetSting = length - middle.length();
		int startOffset = (targetSting + 1) / 2;
		int endOffset = str.length() - targetSting / 2;
		return str.subSequence(0, startOffset) + middle
				+ str.subSequence(endOffset, str.length());
	}

	/**
	 * 取标签之间的文本（单标签，首次匹配）：形如 {@code <a>text</a>} 取 {@code text}。
	 *
	 * @param str 原字符串
	 * @param tag 标签文本
	 * @return 标签间文本；无匹配返回 null
	 */
	public static String substringBetween(CharSequence str, CharSequence tag) {
		return substringBetween(str, tag, tag);
	}

	/**
	 * 取开始与结束标签之间的文本（首次匹配）。
	 *
	 * @param str   原字符串
	 * @param open  开始标签
	 * @param close 结束标签
	 * @return 标签间文本；无匹配返回 null
	 */
	public static String substringBetween(CharSequence str, CharSequence open, CharSequence close) {
		if (str == null || open == null || close == null || open.length() == 0 || close.length() == 0) {
			return null;
		}
		String text = str.toString();
		int start = text.indexOf(open.toString());
		if (start < 0) {
			return null;
		}
		int from = start + open.length();
		int end = text.indexOf(close.toString(), from);
		if (end < 0) {
			return null;
		}
		return text.substring(from, end);
	}

	/**
	 * 返回首个差异位置起的子串（参考 commons-lang {@code StringUtils.difference}）。
	 *
	 * @param str1 字符串一
	 * @param str2 字符串二
	 * @return 从首个不同字符到 str2 末尾的子串；str1 为 null 返回 str2
	 */
	public static String difference(CharSequence str1, CharSequence str2) {
		if (str1 == null) {
			return toString(str2);
		}
		if (str2 == null) {
			return str1.toString();
		}
		int i = 0;
		int limit = Math.min(str1.length(), str2.length());
		while (i < limit && str1.charAt(i) == str2.charAt(i)) {
			i++;
		}
		if (str1.length() == str2.length() && i == limit) {
			return "";
		}
		return str2.subSequence(i, str2.length()).toString();
	}

	/**
	 * 忽略大小写查找子串位置。
	 *
	 * @param str    原字符串
	 * @param search 查找目标
	 * @param from   起始索引（负数按 0）
	 * @return 首次命中的索引；str 为 null 返回 -1
	 */
	public static int indexOfIgnoreCase(CharSequence str, CharSequence search, int from) {
		if (str == null || search == null) {
			return -1;
		}
		if (search.length() == 0) {
			return Math.max(0, from);
		}
		String text = str.toString();
		String needle = search.toString();
		int max = text.length() - needle.length();
		if (from < 0) {
			from = 0;
		}
		if (from > max) {
			return -1;
		}
		for (int i = from; i <= max; i++) {
			if (text.regionMatches(true, i, needle, 0, needle.length())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 大小写互换：大写转小写、小写转大写（参考 commons-lang {@code StringUtils.swapCase}）。
	 *
	 * @param str 原字符串
	 * @return 互换后的字符串；str 为 null 返回 null
	 */
	public static String swapCase(CharSequence str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append(Character.toLowerCase(c));
			} else if (Character.isLowerCase(c)) {
				sb.append(Character.toUpperCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 空白规范化：首尾去空白，内部连续空白（含换行制表）压缩为单个空格。
	 *
	 * @param str 原字符串
	 * @return 规范化结果；str 为 null 返回 null
	 */
	public static String normalizeSpace(CharSequence str) {
		if (str == null) {
			return null;
		}
		if (str.length() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(str.length());
		boolean lastWasSpace = true;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (!lastWasSpace) {
					sb.append(' ');
					lastWasSpace = true;
				}
			} else {
				sb.append(c);
				lastWasSpace = false;
			}
		}
		int end = sb.length();
		while (end > 0 && sb.charAt(end - 1) == ' ') {
			end--;
		}
		return sb.substring(0, end);
	}

	/**
	 * 多个字符串的公共前缀（参考 commons-lang {@code StringUtils.getCommonPrefix}）。
	 *
	 * @param strs 字符串数组
	 * @return 公共前缀；数组为空或含 null 返回空串
	 */
	public static String getCommonPrefix(CharSequence... strs) {
		if (strs == null || strs.length == 0) {
			return "";
		}
		int shortest = Integer.MAX_VALUE;
		for (CharSequence s : strs) {
			if (s == null) {
				return "";
			}
			shortest = Math.min(shortest, s.length());
		}
		if (shortest == 0) {
			return "";
		}
		int i = 0;
		outer:
		while (i < shortest) {
			char c = strs[0].charAt(i);
			for (int j = 1; j < strs.length; j++) {
				if (strs[j].charAt(i) != c) {
					break outer;
				}
			}
			i++;
		}
		return strs[0].subSequence(0, i).toString();
	}

	/**
	 * 循环移位：正数右移、负数左移（参考 commons-lang {@code StringUtils.rotate}）。
	 *
	 * @param str   原字符串
	 * @param shift 移位位数（可为负）
	 * @return 移位后的字符串；str 为 null 返回 null
	 */
	public static String rotate(CharSequence str, int shift) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return "";
		}
		int n = shift % len;
		if (n == 0) {
			return str.toString();
		}
		if (n < 0) {
			n += len;
		}
		// 右移 n 位
		return str.subSequence(len - n, len) + str.subSequence(0, len - n).toString();
	}

	/**
	 * 是否为纯字母（仅含字母字符）。
	 *
	 * <p>示例：{@code StrUtil.isAlpha("abc") -> true}，{@code StrUtil.isAlpha("a1") -> false}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 全为字母返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isAlpha(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isLetter(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否仅含字母或数字。
	 *
	 * <p>示例：{@code StrUtil.isAlphanumeric("abc123") -> true}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 全为字母或数字返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isAlphanumeric(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isLetterOrDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否仅含数字或空格。
	 *
	 * <p>示例：{@code StrUtil.isNumericSpace("12 345") -> true}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 全为数字或空格返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isNumericSpace(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch != ' ' && !Character.isDigit(ch)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否全为可打印 ASCII 字符（码位 32–126，含空格）。
	 *
	 * <p>示例：{@code StrUtil.isAsciiPrintable("Hello!") -> true}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 全为可打印 ASCII 返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isAsciiPrintable(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch < 32 || ch > 126) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否包含任意空白字符（{@link Character#isWhitespace(char)}）。
	 *
	 * <p>示例：{@code StrUtil.containsWhitespace("a b") -> true}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 含空白返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean containsWhitespace(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 移除字符串中的全部空白字符。
	 *
	 * <p>示例：{@code StrUtil.removeWhitespace(" a b ") -> "ab"}。</p>
	 *
	 * @param str 字符串，可为 {@code null}
	 * @return 去空白后的字符串；{@code null} 返回 {@code null}
	 */
	public static String removeWhitespace(CharSequence str) {
		if (str == null) {
			return null;
		}
		if (str.length() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (!Character.isWhitespace(ch)) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * 返回任一给定子串在原字符串中最早出现的位置。
	 *
	 * <p>示例：{@code StrUtil.indexOfAny("hello world", "world", "lo") -> 3}。</p>
	 *
	 * @param str        原字符串，可为 {@code null}
	 * @param searchStrs 待查找子串集合
	 * @return 最早出现位置；未找到或参数为 {@code null} 返回 -1
	 */
	public static int indexOfAny(CharSequence str, CharSequence... searchStrs) {
		if (isEmpty(str) || searchStrs == null || searchStrs.length == 0) {
			return -1;
		}
		int best = -1;
		for (CharSequence s : searchStrs) {
			if (s == null || s.length() == 0) {
				continue;
			}
			int idx = indexOf(str, s);
			if (idx >= 0 && (best < 0 || idx < best)) {
				best = idx;
			}
		}
		return best;
	}

	/**
	 * 返回任一给定子串在原字符串中最后出现的位置（以子串结尾为参照）。
	 *
	 * <p>示例：{@code StrUtil.lastIndexOfAny("hello lo", "lo", "ell") -> 7}。</p>
	 *
	 * @param str        原字符串，可为 {@code null}
	 * @param searchStrs 待查找子串集合
	 * @return 最后出现位置；未找到或参数为 {@code null} 返回 -1
	 */
	public static int lastIndexOfAny(CharSequence str, CharSequence... searchStrs) {
		if (isEmpty(str) || searchStrs == null || searchStrs.length == 0) {
			return -1;
		}
		int best = -1;
		for (CharSequence s : searchStrs) {
			if (s == null || s.length() == 0) {
				continue;
			}
			int idx = lastIndexOf(str, s);
			if (idx >= 0 && idx + s.length() - 1 > best) {
				best = idx + s.length() - 1;
			}
		}
		return best;
	}

	/**
	 * 仅替换第一次出现的搜索子串。
	 *
	 * <p>示例：{@code StrUtil.replaceOnce("aa-bb-cc", "-", "+") -> "aa+bb-cc"}。</p>
	 *
	 * @param str         原字符串，可为 {@code null}
	 * @param searchStr   搜索子串，可为 {@code null}
	 * @param replacement 替换子串，可为 {@code null}
	 * @return 替换结果；{@code str} 为 {@code null} 返回 {@code null}
	 */
	public static String replaceOnce(CharSequence str, CharSequence searchStr, CharSequence replacement) {
		if (str == null) {
			return null;
		}
		if (searchStr == null || searchStr.length() == 0) {
			return str.toString();
		}
		int idx = indexOf(str, searchStr);
		if (idx < 0) {
			return str.toString();
		}
		// 对齐 Commons：replacement 为 null 时不替换，返回原串
		if (replacement == null) {
			return str.toString();
		}
		String repl = replacement.toString();
		return str.subSequence(0, idx) + repl + str.subSequence(idx + searchStr.length(), str.length());
	}

	/**
	 * 当字符串以给定前后缀同时包裹时，一次性剥去首尾（前后缀相同字符串）。
	 *
	 * <p>示例：{@code StrUtil.removeSurrounding("\"abc\"", "\"") -> "abc"}。</p>
	 *
	 * @param str  字符串，可为 {@code null}
	 * @param wrap 同时作为前缀与后缀的字符串
	 * @return 剥去包裹后的字符串；{@code null} 返回 {@code null}
	 */
	public static String removeSurrounding(CharSequence str, CharSequence wrap) {
		if (str == null) {
			return null;
		}
		if (wrap == null || wrap.length() == 0) {
			return str.toString();
		}
		return removeSurrounding(str, wrap, wrap);
	}

	/**
	 * 当字符串以给定前后缀同时包裹时，一次性剥去首尾。
	 *
	 * <p>示例：{@code StrUtil.removeSurrounding("[abc]", "[", "]") -> "abc"}。</p>
	 *
	 * @param str    字符串，可为 {@code null}
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 剥去包裹后的字符串；{@code str} 为 {@code null} 返回 {@code null}
	 */
	public static String removeSurrounding(CharSequence str, CharSequence prefix, CharSequence suffix) {
		if (str == null) {
			return null;
		}
		if (prefix == null || suffix == null || prefix.length() == 0 || suffix.length() == 0) {
			return str.toString();
		}
		if (!startWith(str, prefix) || !endWith(str, suffix)) {
			return str.toString();
		}
		return str.subSequence(prefix.length(), str.length() - suffix.length()).toString();
	}

	/**
	 * 忽略大小写：当字符串以给定前后缀同时包裹时，一次性剥去首尾（前后缀相同字符串）。
	 *
	 * @param str  字符串，可为 {@code null}
	 * @param wrap 同时作为前缀与后缀的字符串
	 * @return 剥去包裹后的字符串；{@code null} 返回 {@code null}
	 */
	public static String removeSurroundingIgnoreCase(CharSequence str, CharSequence wrap) {
		if (str == null) {
			return null;
		}
		if (wrap == null || wrap.length() == 0) {
			return str.toString();
		}
		return removeSurroundingIgnoreCase(str, wrap, wrap);
	}

	/**
	 * 忽略大小写：当字符串以给定前后缀同时包裹时，一次性剥去首尾。
	 *
	 * <p>示例：{@code StrUtil.removeSurroundingIgnoreCase("[ABC]", "[", "]") -> "ABC"}。</p>
	 *
	 * @param str    字符串，可为 {@code null}
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 剥去包裹后的字符串；{@code str} 为 {@code null} 返回 {@code null}
	 */
	public static String removeSurroundingIgnoreCase(CharSequence str, CharSequence prefix, CharSequence suffix) {
		if (str == null) {
			return null;
		}
		if (prefix == null || suffix == null || prefix.length() == 0 || suffix.length() == 0) {
			return str.toString();
		}
		if (!startWithIgnoreCase(str, prefix) || !endWithIgnoreCase(str, suffix)) {
			return str.toString();
		}
		return str.subSequence(prefix.length(), str.length() - suffix.length()).toString();
	}

	/**
	 * 精确截断字符串：长度超过 {@code maxWidth} 时截取前 {@code maxWidth} 个字符（不附加省略号）。
	 *
	 * <p>语义对齐 Commons {@code StringUtils.truncate}，与 Hutool {@code maxLength}（保留省略号）不同：
	 * 示例：{@code StrUtil.truncate("hello world", 5) -> "hello"}。</p>
	 *
	 * @param str      字符串，可为 {@code null}
	 * @param maxWidth 最大长度，负数抛 {@link IllegalArgumentException}
	 * @return 截断后的字符串；{@code null} 返回 {@code null}
	 */
	public static String truncate(CharSequence str, int maxWidth) {
		if (maxWidth < 0) {
			throw new IllegalArgumentException("maxWidth must be zero or larger");
		}
		if (str == null) {
			return null;
		}
		if (str.length() <= maxWidth) {
			return str.toString();
		}
		return str.subSequence(0, maxWidth).toString();
	}

	/**
	 * 按分隔字符分割并保留全部空 token（连续分隔符、首尾分隔符产生的空串均保留）。
	 *
	 * <p>示例：{@code StrUtil.splitPreserveAllTokens(",a,,b,", ',') -> ["", "a", "", "b", ""]}。</p>
	 *
	 * @param str           字符串，可为 {@code null}
	 * @param separatorChar 分隔字符
	 * @return 分割结果数组；{@code null} 返回 {@code null}
	 */
	public static String[] splitPreserveAllTokens(CharSequence str, char separatorChar) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		List<String> tokens = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < len; i++) {
			if (str.charAt(i) == separatorChar) {
				tokens.add(str.subSequence(start, i).toString());
				start = i + 1;
			}
		}
		// 结尾 token（含尾部分隔符产生的空 token）
		tokens.add(str.subSequence(start, len).toString());
		return tokens.toArray(new String[0]);
	}

	/**
	 * 剥离开头出现的字符集合字符（{@code stripChars} 为 {@code null} 时剥离空白）。
	 *
	 * <p>示例：{@code StrUtil.stripStart("__abc__", "_") -> "abc__"}。</p>
	 *
	 * @param str        字符串，可为 {@code null}
	 * @param stripChars 待剥离的字符集合，{@code null} 表示剥离空白
	 * @return 剥离后的字符串；{@code null} 返回 {@code null}
	 */
	public static String stripStart(String str, String stripChars) {
		if (str == null) {
			return null;
		}
		int start = 0;
		int len = str.length();
		if (stripChars == null) {
			while (start < len && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (stripChars.length() == 0) {
			return str;
		} else {
			while (start < len && stripChars.indexOf(str.charAt(start)) >= 0) {
				start++;
			}
		}
		return str.substring(start);
	}

	/**
	 * 剥离结尾出现的字符集合字符（{@code stripChars} 为 {@code null} 时剥离空白）。
	 *
	 * <p>示例：{@code StrUtil.stripEnd("__abc__", "_") -> "__abc"}。</p>
	 *
	 * @param str        字符串，可为 {@code null}
	 * @param stripChars 待剥离的字符集合，{@code null} 表示剥离空白
	 * @return 剥离后的字符串；{@code null} 返回 {@code null}
	 */
	public static String stripEnd(String str, String stripChars) {
		if (str == null) {
			return null;
		}
		int end = str.length();
		if (stripChars == null) {
			while (end > 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.length() == 0) {
			return str;
		} else {
			while (end > 0 && stripChars.indexOf(str.charAt(end - 1)) >= 0) {
				end--;
			}
		}
		return str.substring(0, end);
	}

}
