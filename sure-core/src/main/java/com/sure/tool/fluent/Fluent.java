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

import java.util.Collection;
import java.util.Objects;

/**
 * Fluent API 统一工厂入口（Fluent API 全面化门面）。
 *
 * <p>按目标类型分发：字符串走 {@link FluentString}，集合走 {@link FluentList}，
 * Map 走 {@link FluentMap}。命名采用无歧义策略：列表构建用 {@link #listOf(Object...)}
 * （避免与 {@link #of(String)} / {@link #of(Object, Object)} 的泛型重载冲突），
 * 单键值 Map 用 {@link #mapOf(Object, Object)}。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String s = Fluent.of(" abc ").trim().upperCase().get();            // "ABC"
 * List<Integer> l = Fluent.listOf(3, 1, 2).sorted().toList();        // [1, 2, 3]
 * Map<String, Integer> m = Fluent.mapOf("a", 1).put("b", 2).toMap(); // {a=1, b=2}
 * }</pre>
 *
 * @since 1.19.0
 */
public final class Fluent {

	private Fluent() {
	}

	/**
	 * 创建字符串管道。
	 *
	 * @param value 初始字符串，不能为 null
	 * @return FluentString 实例
	 * @throws NullPointerException value 为 null
	 */
	public static FluentString string(String value) {
		return new FluentString(Objects.requireNonNull(value, "value must not be null"));
	}

	/**
	 * 创建字符串管道（{@link #string(String)} 别名）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentString f = Fluent.of("hello");
	 * }</pre>
	 *
	 * @param value 初始字符串，不能为 null
	 * @return FluentString 实例
	 * @throws NullPointerException value 为 null
	 */
	public static FluentString of(String value) {
		return string(value);
	}

	/**
	 * 创建空列表管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<String> f = Fluent.list();
	 * }</pre>
	 *
	 * @param <E> 元素类型
	 * @return 空 FluentList 实例
	 */
	public static <E> FluentList<E> list() {
		return FluentList.of();
	}

	/**
	 * 以若干元素创建列表管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<Integer> f = Fluent.listOf(3, 1, 2);
	 * }</pre>
	 *
	 * @param items 初始元素，不能含 null
	 * @param <E>   元素类型
	 * @return FluentList 实例（内部不可变）
	 * @throws IllegalArgumentException 任一元素为 null
	 */
	@SafeVarargs
	public static <E> FluentList<E> listOf(E... items) {
		return FluentList.of(items);
	}

	/**
	 * 从既有集合创建列表管道（防御拷贝）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<String> f = Fluent.of(existingList);
	 * }</pre>
	 *
	 * @param collection 源集合，不能为 null，不能含 null 元素
	 * @param <E>        元素类型
	 * @return FluentList 实例（内部不可变副本）
	 * @throws NullPointerException      collection 为 null
	 * @throws IllegalArgumentException collection 含 null 元素
	 */
	public static <E> FluentList<E> of(Collection<? extends E> collection) {
		return FluentList.from(collection);
	}

	/**
	 * 创建空 Map 管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentMap<String, Integer> m = Fluent.map();
	 * }</pre>
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 空 FluentMap 实例
	 */
	public static <K, V> FluentMap<K, V> map() {
		return FluentMap.of();
	}

	/**
	 * 创建含单个键值对的 Map 管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentMap<String, Integer> m = Fluent.mapOf("a", 1);
	 * }</pre>
	 *
	 * @param key   键，不能为 null
	 * @param value 值，不能为 null
	 * @param <K>   键类型
	 * @param <V>   值类型
	 * @return FluentMap 实例
	 * @throws IllegalArgumentException key 或 value 为 null
	 */
	public static <K, V> FluentMap<K, V> mapOf(K key, V value) {
		return FluentMap.of(key, value);
	}
}
