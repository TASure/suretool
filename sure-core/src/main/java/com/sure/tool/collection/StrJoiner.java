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
package com.sure.tool.collection;

/**
 * 字符串拼接器：带分隔符、前缀、后缀，参考 Hutool 的 {@code StrJoiner} 设计。
 * <p>
 * {@code null} 值默认跳过；无内容时 {@link #toString()} 返回空串。
 *
 * @author suretool
 * @since 0.1.0
 */
public class StrJoiner {

	private final String delimiter;
	private final String prefix;
	private final String suffix;
	private final StringBuilder builder = new StringBuilder();
	private boolean hasContent = false;

	/**
	 * 创建拼接器（仅分隔符）。
	 *
	 * @param delimiter 分隔符
	 * @return 拼接器
	 */
	public static StrJoiner of(String delimiter) {
		return new StrJoiner(delimiter, "", "");
	}

	/**
	 * 创建拼接器。
	 *
	 * @param delimiter 分隔符
	 * @param prefix    前缀
	 * @param suffix    后缀
	 * @return 拼接器
	 */
	public static StrJoiner of(String delimiter, String prefix, String suffix) {
		return new StrJoiner(delimiter, prefix, suffix);
	}

	private StrJoiner(String delimiter, String prefix, String suffix) {
		this.delimiter = delimiter == null ? "" : delimiter;
		this.prefix = prefix == null ? "" : prefix;
		this.suffix = suffix == null ? "" : suffix;
	}

	/**
	 * 追加值（{@code null} 跳过）。
	 *
	 * @param value 值
	 * @return 当前拼接器
	 */
	public StrJoiner append(Object value) {
		if (value == null) {
			return this;
		}
		if (hasContent) {
			builder.append(delimiter);
		}
		builder.append(value);
		hasContent = true;
		return this;
	}

	/**
	 * 追加数组（{@code null} 元素跳过）。
	 *
	 * @param values 值数组
	 * @return 当前拼接器
	 */
	public StrJoiner appendAll(Object... values) {
		if (values != null) {
			for (Object value : values) {
				append(value);
			}
		}
		return this;
	}

	/**
	 * 追加可迭代集合。
	 *
	 * @param values 集合
	 * @return 当前拼接器
	 */
	public StrJoiner appendAll(Iterable<?> values) {
		if (values != null) {
			for (Object value : values) {
				append(value);
			}
		}
		return this;
	}

	/**
	 * 是否有内容。
	 *
	 * @return 是否有内容
	 */
	public boolean hasContent() {
		return hasContent;
	}

	/**
	 * 拼接结果（无内容返回空串）。
	 *
	 * @return 结果字符串
	 */
	@Override
	public String toString() {
		return hasContent ? prefix + builder + suffix : "";
	}
}