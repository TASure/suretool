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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 流工具类，参考 Hutool 的 {@code StreamUtil} 设计。
 *
 * <p>提供数组 / 集合 / 迭代器 / Optional 转流、多流合并、空安全等常用静态方法，
 * 全部方法对 {@code null} 输入安全（不会抛 NPE）。</p>
 *
 * @author suretool
 * @since 1.19.0
 */
public final class StreamUtil {

	private StreamUtil() {
	}

	/**
	 * 变参或数组转流（{@code null} 安全）。
	 *
	 * <p>示例：{@code StreamUtil.of("a", "b", "c")}；{@code StreamUtil.of(null)} 返回空流。</p>
	 *
	 * @param values 元素数组，可为 {@code null}
	 * @return 元素流；{@code values} 为 {@code null} 返回空流
	 */
	@SafeVarargs
	public static <T> Stream<T> of(T... values) {
		if (values == null) {
			return Stream.empty();
		}
		return java.util.Arrays.stream(values);
	}

	/**
	 * 集合转流（{@code null} 安全）。
	 *
	 * <p>示例：{@code StreamUtil.of(list)}；{@code StreamUtil.of(null)} 返回空流。</p>
	 *
	 * @param collection 集合，可为 {@code null}
	 * @return 集合元素流；{@code null} 返回空流
	 */
	public static <T> Stream<T> of(Collection<T> collection) {
		if (collection == null) {
			return Stream.empty();
		}
		return collection.stream();
	}

	/**
	 * 迭代器转流（{@code null} 安全）。
	 *
	 * <p>示例：{@code StreamUtil.of(iterable)}；{@code StreamUtil.of(null)} 返回空流。</p>
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 元素流；{@code null} 返回空流
	 */
	public static <T> Stream<T> of(Iterable<T> iterable) {
		if (iterable == null) {
			return Stream.empty();
		}
		java.util.Iterator<T> it = iterable.iterator();
		// 惰性拆分迭代器：仅在终端操作时消费
		return java.util.stream.StreamSupport.stream(
				java.util.Spliterators.spliteratorUnknownSize(it,
						java.util.Spliterator.ORDERED | java.util.Spliterator.NONNULL), false);
	}

	/**
	 * Optional 转流：有值则单元素流，否则空流（Java 9 {@code Stream.ofNullable} 语义）。
	 *
	 * <p>示例：{@code StreamUtil.of(Optional.of("x"))}；{@code StreamUtil.of(Optional.empty())} 返回空流。</p>
	 *
	 * @param optional Optional 值，可为 {@code null}
	 * @return 元素流；{@code null} 或空 Optional 返回空流
	 */
	public static <T> Stream<T> of(Optional<T> optional) {
		if (optional == null || !optional.isPresent()) {
			return Stream.empty();
		}
		return Stream.of(optional.get());
	}

	/**
	 * 合并多个流（{@code null} 流自动跳过）。
	 *
	 * <p>示例：{@code StreamUtil.concat(Stream.of(1), null, Stream.of(2))} 产生 1、2。</p>
	 *
	 * @param streams 流数组，可为 {@code null}，元素可为 {@code null}
	 * @return 合并后的流；全部为空返回空流
	 */
	@SafeVarargs
	public static <T> Stream<T> concat(Stream<? extends T>... streams) {
		if (streams == null) {
			return Stream.empty();
		}
		Stream<T> result = Stream.empty();
		for (Stream<? extends T> s : streams) {
			if (s != null) {
				result = Stream.concat(result, s);
			}
		}
		return result;
	}

	/**
	 * 空安全：{@code null} 流转换为空流。
	 *
	 * <p>示例：{@code StreamUtil.nullToEmpty(null)} 返回空流。</p>
	 *
	 * @param stream 流，可为 {@code null}
	 * @return 原流；{@code null} 返回空流
	 */
	public static <T> Stream<T> nullToEmpty(Stream<T> stream) {
		return stream == null ? Stream.empty() : stream;
	}

	/**
	 * 返回空流。
	 *
	 * <p>示例：{@code StreamUtil.empty()} 等价于 {@code Stream.empty()}。</p>
	 *
	 * @return 空流
	 */
	public static <T> Stream<T> empty() {
		return Stream.empty();
	}
}
