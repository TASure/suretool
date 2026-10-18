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

import java.util.Collections;
import java.util.Map;

/**
 * 模板变量替换引擎，参考 Apache Commons Text {@code StrSubstitutor} 的高频子集设计。
 *
 * <p>支持：可配置定界符（前缀/后缀）、默认值语法 {@code ${key:-default}}、可配置转义符
 * （默认 {@code $}，{@code $$&#123;} 输出字面 {@code $&#123;}）、缺失变量保留占位符原样。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * // 基本替换：缺失变量保留原样，值为 null 渲染为空串
 * StrSubstitutor.substitute("hello ${name}", Map.of("name", "world"));
 * // => "hello world"
 *
 * // 默认值语法：变量缺失时使用默认值
 * StrSubstitutor.substitute("${env:-prod}", Collections.emptyMap());
 * // => "prod"
 *
 * // 自定义定界符
 * StrSubstitutor.replace("<%name%>", Map.of("name", "sure"), "<%", "%>");
 * // => "sure"
 * }</pre>
 *
 * @author suretool
 * @since 1.19.0
 */
public class StrSubstitutor {

	/** 默认前缀 */
	public static final String DEFAULT_PREFIX = "${";
	/** 默认后缀 */
	public static final String DEFAULT_SUFFIX = "}";
	/** 默认转义符 */
	public static final char DEFAULT_ESCAPE = '$';
	/** 默认值分隔符（Commons Text 兼容） */
	private static final String DEFAULT_VALUE_DELIMITER = ":-";

	private final String prefix;
	private final String suffix;
	private final char escapeChar;

	/**
	 * 构造替换器，使用默认前缀 {@code ${}、后缀 {@code }}、转义符 {@code $}。
	 */
	public StrSubstitutor() {
		this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ESCAPE);
	}

	/**
	 * 构造替换器，使用默认转义符 {@code $}。
	 *
	 * @param prefix 占位符前缀
	 * @param suffix 占位符后缀
	 * @throws IllegalArgumentException 前缀或后缀为 null 或空串时抛出
	 */
	public StrSubstitutor(String prefix, String suffix) {
		this(prefix, suffix, DEFAULT_ESCAPE);
	}

	/**
	 * 构造替换器，自定义定界符与转义符。
	 *
	 * @param prefix     占位符前缀
	 * @param suffix     占位符后缀
	 * @param escapeChar 转义符；传 {@code 0} 表示禁用转义
	 * @throws IllegalArgumentException 前缀或后缀为 null 或空串时抛出
	 */
	public StrSubstitutor(String prefix, String suffix, char escapeChar) {
		if (prefix == null || prefix.isEmpty()) {
			throw new IllegalArgumentException("前缀不能为 null 或空串");
		}
		if (suffix == null || suffix.isEmpty()) {
			throw new IllegalArgumentException("后缀不能为 null 或空串");
		}
		this.prefix = prefix;
		this.suffix = suffix;
		this.escapeChar = escapeChar;
	}

	/**
	 * 一行式替换（默认前缀 {@code ${}、后缀 {@code }}、转义符 {@code $}）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String text = StrSubstitutor.substitute("订单 ${id} 已支付", Map.of("id", 10086));
	 * }</pre>
	 *
	 * @param template 模板文本；为 null 时返回 null
	 * @param vars     变量映射；为 null 时视为空映射
	 * @return 替换结果
	 */
	public static String substitute(String template, Map<String, ?> vars) {
		return new StrSubstitutor().replace(template, vars);
	}

	/**
	 * 一行式替换（自定义定界符，默认转义符 {@code $}）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String text = StrSubstitutor.replace("<%name%> 你好", Map.of("name", "sure"), "<%", "%>");
	 * }</pre>
	 *
	 * @param template 模板文本；为 null 时返回 null
	 * @param vars     变量映射；为 null 时视为空映射
	 * @param prefix   占位符前缀
	 * @param suffix   占位符后缀
	 * @return 替换结果
	 * @throws IllegalArgumentException 前缀或后缀为 null 或空串时抛出
	 */
	public static String replace(String template, Map<String, ?> vars, String prefix, String suffix) {
		return new StrSubstitutor(prefix, suffix).replace(template, vars);
	}

	/**
	 * 执行模板变量替换。
	 *
	 * <p>替换语义：</p>
	 * <ul>
	 *   <li>{@code ${key}} 解析为变量值；值为 null 渲染为空串；</li>
	 *   <li>变量缺失（不存在于映射）保留占位符原样；</li>
	 *   <li>{@code ${key:-default}} 变量缺失时渲染默认值；</li>
	 *   <li>{@code $$&#123;}（escapeChar 连续出现两次）输出字面 {@code $&#123;}；</li>
	 *   <li>未闭合占位符保留原样，空 key {@code $&#123;}} 渲染为空。</li>
	 * </ul>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * StrSubstitutor sub = new StrSubstitutor();
	 * sub.replace("${name} 缺省=${missing:-N/A}", Map.of("name", "sure"));
	 * // => "sure 缺省=N/A"
	 * }</pre>
	 *
	 * @param template 模板文本；为 null 时返回 null
	 * @param vars     变量映射；为 null 时视为空映射
	 * @return 替换结果
	 */
	public String replace(String template, Map<String, ?> vars) {
		if (template == null) {
			return null;
		}
		final Map<String, ?> values = vars == null ? Collections.emptyMap() : vars;
		final StringBuilder sb = new StringBuilder(template.length() + 16);
		final int len = template.length();
		int i = 0;
		while (i < len) {
			if (isEscapeAt(template, i)) {
				// 转义：$${ -> 输出字面 ${
				sb.append(prefix);
				i += prefix.length() + 1;
				continue;
			}
			if (startsWith(template, prefix, i)) {
				final int close = template.indexOf(suffix, i + prefix.length());
				if (close < 0) {
					// 未闭合：按普通文本处理
					sb.append(template.charAt(i));
					i++;
					continue;
				}
				final String key = template.substring(i + prefix.length(), close);
				final String replacement = resolve(key, values);
				if (replacement != null) {
					sb.append(replacement);
				} else {
					// 缺失：保留占位符原样
					sb.append(template, i, close + suffix.length());
				}
				i = close + suffix.length();
				continue;
			}
			sb.append(template.charAt(i));
			i++;
		}
		return sb.toString();
	}

	/**
	 * 解析单个占位符键：支持 {@code key:-default} 默认值语法。
	 *
	 * @return 渲染值；变量缺失且无默认值时返回 null（保留占位符）
	 */
	private String resolve(String key, Map<String, ?> values) {
		if (key.isEmpty()) {
			// 空 key：渲染为空
			return "";
		}
		final int delim = key.indexOf(DEFAULT_VALUE_DELIMITER);
		if (delim < 0) {
			if (!values.containsKey(key)) {
				// 变量缺失：保留占位符
				return null;
			}
			final Object value = values.get(key);
			// 变量存在但值为 null：渲染为空串
			return value == null ? "" : String.valueOf(value);
		}
		final String name = key.substring(0, delim);
		final Object value = name.isEmpty() ? null : values.get(name);
		if (value != null) {
			return String.valueOf(value);
		}
		// 变量缺失（或名为空）：使用默认值；默认值本身为空串也合法
		return key.substring(delim + DEFAULT_VALUE_DELIMITER.length());
	}

	/**
	 * 判断当前位置是否为转义序列：{@code escapeChar} 紧跟 {@code prefix}。
	 */
	private boolean isEscapeAt(String text, int i) {
		return escapeChar != 0 && text.charAt(i) == escapeChar
				&& i + 1 < text.length() && startsWith(text, prefix, i + 1);
	}

	private static boolean startsWith(String text, String part, int offset) {
		if (offset + part.length() > text.length()) {
			return false;
		}
		return text.regionMatches(offset, part, 0, part.length());
	}
}
