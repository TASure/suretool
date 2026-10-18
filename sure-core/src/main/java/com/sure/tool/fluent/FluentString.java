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
package com.sure.tool.fluent;

import java.util.Locale;
import java.util.Objects;

/**
 * 链式字符串管道（Fluent API 全面化首项）。
 *
 * <p>与 {@code StrUtil} 静态方法形态并存：Fluent 形态支持「校验 → 变换 → 终端」
 * 一条链完成，中间操作不断流、每次返回新的 {@code FluentString}（内部 String
 * 不可变，仿 Guava Immutable 思路）。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String s = Fluent.string("  hello world  ")
 *         .trim().upperCase().replace("WORLD", "suretool").get();
 * // "HELLO suretool"
 * }</pre>
 *
 * @since 1.19.0
 */
public final class FluentString {

	/** 内部不可变字符串。 */
	private final String value;

	FluentString(String value) {
		this.value = value;
	}

	/**
	 * 去除首尾空白。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("  x  ").trim().get(); // "x"
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentString trim() {
		return new FluentString(value.trim());
	}

	/**
	 * 转大写（默认 Locale.ROOT，避免土耳其 i 问题）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("abc").upperCase().get(); // "ABC"
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentString upperCase() {
		return new FluentString(value.toUpperCase(Locale.ROOT));
	}

	/**
	 * 转小写（默认 Locale.ROOT）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("ABC").lowerCase().get(); // "abc"
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentString lowerCase() {
		return new FluentString(value.toLowerCase(Locale.ROOT));
	}

	/**
	 * 反转字符串。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("abc").reverse().get(); // "cba"
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentString reverse() {
		return new FluentString(new StringBuilder(value).reverse().toString());
	}

	/**
	 * 字面替换（非正则）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("a.b.c").replace(".", "-").get(); // "a-b-c"
	 * }</pre>
	 *
	 * @param oldChar 被替换的字面串，不能为 null
	 * @param newChar 替换串，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException oldChar 或 newChar 为 null
	 */
	public FluentString replace(String oldChar, String newChar) {
		Objects.requireNonNull(oldChar, "oldChar must not be null");
		Objects.requireNonNull(newChar, "newChar must not be null");
		return new FluentString(value.replace(oldChar, newChar));
	}

	/**
	 * 正则替换。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("a1b2").replaceAll("\\d", "#").get(); // "a#b#"
	 * }</pre>
	 *
	 * @param regex       正则表达式，不能为 null
	 * @param replacement 替换串，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException      regex 或 replacement 为 null
	 * @throws IllegalArgumentException  regex 非法
	 */
	public FluentString replaceAll(String regex, String replacement) {
		Objects.requireNonNull(regex, "regex must not be null");
		Objects.requireNonNull(replacement, "replacement must not be null");
		return new FluentString(value.replaceAll(regex, replacement));
	}

	/**
	 * 截取子串（从 begin 到末尾）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("suretool").substring(4).get(); // "tool"
	 * }</pre>
	 *
	 * @param begin 起始索引（含），越界抛 StringIndexOutOfBoundsException
	 * @return 新的管道实例
	 */
	public FluentString substring(int begin) {
		return new FluentString(value.substring(begin));
	}

	/**
	 * 截取子串（[begin, end)）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("suretool").substring(0, 4).get(); // "sure"
	 * }</pre>
	 *
	 * @param begin 起始索引（含）
	 * @param end   结束索引（不含），越界抛 StringIndexOutOfBoundsException
	 * @return 新的管道实例
	 */
	public FluentString substring(int begin, int end) {
		return new FluentString(value.substring(begin, end));
	}

	/**
	 * 左侧补齐到指定长度。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("7").padLeft(3, '0').get(); // "007"
	 * }</pre>
	 *
	 * @param length  目标长度，小于当前长度时返回原串
	 * @param padChar 补齐字符
	 * @return 新的管道实例
	 */
	public FluentString padLeft(int length, char padChar) {
		if (value.length() >= length) {
			return this;
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = value.length(); i < length; i++) {
			sb.append(padChar);
		}
		return new FluentString(sb.append(value).toString());
	}

	/**
	 * 右侧补齐到指定长度。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("7").padRight(3, '0').get(); // "700"
	 * }</pre>
	 *
	 * @param length  目标长度，小于当前长度时返回原串
	 * @param padChar 补齐字符
	 * @return 新的管道实例
	 */
	public FluentString padRight(int length, char padChar) {
		if (value.length() >= length) {
			return this;
		}
		StringBuilder sb = new StringBuilder(length);
		sb.append(value);
		for (int i = value.length(); i < length; i++) {
			sb.append(padChar);
		}
		return new FluentString(sb.toString());
	}

	/**
	 * 重复当前字符串 count 次。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("ab").repeat(3).get(); // "ababab"
	 * }</pre>
	 *
	 * @param count 重复次数，负数抛 IAE；0 返回空串
	 * @return 新的管道实例
	 * @throws IllegalArgumentException count &lt; 0
	 */
	public FluentString repeat(int count) {
		if (count < 0) {
			throw new IllegalArgumentException("count must not be negative: " + count);
		}
		return new FluentString(value.repeat(count));
	}

	/**
	 * 去除指定前缀（不匹配返回原串）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("io.github.tasure").stripPrefix("io.").get(); // "github.tasure"
	 * }</pre>
	 *
	 * @param prefix 前缀，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException prefix 为 null
	 */
	public FluentString stripPrefix(String prefix) {
		Objects.requireNonNull(prefix, "prefix must not be null");
		if (value.startsWith(prefix)) {
			return new FluentString(value.substring(prefix.length()));
		}
		return this;
	}

	/**
	 * 去除指定后缀（不匹配返回原串）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.string("report.md").stripSuffix(".md").get(); // "report"
	 * }</pre>
	 *
	 * @param suffix 后缀，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException suffix 为 null
	 */
	public FluentString stripSuffix(String suffix) {
		Objects.requireNonNull(suffix, "suffix must not be null");
		if (value.endsWith(suffix)) {
			return new FluentString(value.substring(0, value.length() - suffix.length()));
		}
		return this;
	}

	/**
	 * 链中校验点：当前值为空白（null 语义由创建时拒绝）抛 IAE。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String name = Fluent.string(rawName).trim().notBlank().get();
	 * }</pre>
	 *
	 * @return 当前管道实例（便于继续链式）
	 * @throws IllegalArgumentException 当前值为空串或纯空白
	 */
	public FluentString notBlank() {
		if (value.isBlank()) {
			throw new IllegalArgumentException("value must not be blank, but was: [" + value + "]");
		}
		return this;
	}

	/**
	 * 终端：返回当前字符串。
	 *
	 * @return 当前字符串
	 */
	public String get() {
		return value;
	}

	/**
	 * 终端：字符串长度。
	 *
	 * @return 长度
	 */
	public int length() {
		return value.length();
	}

	/**
	 * 终端：是否为空串。
	 *
	 * @return 空串返回 true
	 */
	public boolean isEmpty() {
		return value.isEmpty();
	}

	/**
	 * 终端：是否为空白（空串或全空白）。
	 *
	 * @return 空白返回 true
	 */
	public boolean isBlank() {
		return value.isBlank();
	}

	/**
	 * 终端：是否以指定前缀开头。
	 *
	 * @param prefix 前缀，不能为 null
	 * @return 是返回 true
	 * @throws NullPointerException prefix 为 null
	 */
	public boolean startsWith(String prefix) {
		return value.startsWith(Objects.requireNonNull(prefix, "prefix must not be null"));
	}

	/**
	 * 终端：是否以指定后缀结尾。
	 *
	 * @param suffix 后缀，不能为 null
	 * @return 是返回 true
	 * @throws NullPointerException suffix 为 null
	 */
	public boolean endsWith(String suffix) {
		return value.endsWith(Objects.requireNonNull(suffix, "suffix must not be null"));
	}

	/**
	 * 终端：是否包含指定子串。
	 *
	 * @param fragment 子串，不能为 null
	 * @return 包含返回 true
	 * @throws NullPointerException fragment 为 null
	 */
	public boolean contains(String fragment) {
		return value.contains(Objects.requireNonNull(fragment, "fragment must not be null"));
	}

	/**
	 * 终端：忽略大小写比较。
	 *
	 * @param other 比较对象，null 返回 false
	 * @return 忽略大小写相等返回 true
	 */
	public boolean equalsIgnoreCase(String other) {
		return value.equalsIgnoreCase(other);
	}

	@Override
	public String toString() {
		return value;
	}
}
