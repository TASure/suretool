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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 保持插入顺序的 Map 增强，继承 {@link LinkedHashMap}，提供首尾元素快捷访问与按索引操作。
 *
 * @param <K> key 类型
 * @param <V> value 类型
 * @author suretool
 * @since 0.1.0
 */
public class OrderedMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建空 Map。
	 */
	public OrderedMap() {
	}

	/**
	 * 创建带初始容量的 Map。
	 *
	 * @param initialCapacity 初始容量
	 */
	public OrderedMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 从既有 Map 创建。
	 *
	 * @param map 源 Map
	 */
	public OrderedMap(Map<? extends K, ? extends V> map) {
		super(map);
	}

	/**
	 * 第一个键，空 Map 返回 {@code null}。
	 *
	 * @return 第一个键或 {@code null}
	 */
	public K firstKey() {
		return isEmpty() ? null : keySet().iterator().next();
	}

	/**
	 * 最后一个键，空 Map 返回 {@code null}。
	 *
	 * @return 最后一个键或 {@code null}
	 */
	public K lastKey() {
		if (isEmpty()) {
			return null;
		}
		K last = null;
		for (K key : keySet()) {
			last = key;
		}
		return last;
	}

	/**
	 * 移除并返回第一个键值对，空 Map 返回 {@code null}。
	 *
	 * @return 第一个键值对或 {@code null}
	 */
	public Entry<K, V> removeFirst() {
		if (isEmpty()) {
			return null;
		}
		K first = firstKey();
		return new java.util.AbstractMap.SimpleEntry<>(first, remove(first));
	}

	/**
	 * 移除并返回最后一个键值对，空 Map 返回 {@code null}。
	 *
	 * @return 最后一个键值对或 {@code null}
	 */
	public Entry<K, V> removeLast() {
		if (isEmpty()) {
			return null;
		}
		K last = lastKey();
		return new java.util.AbstractMap.SimpleEntry<>(last, remove(last));
	}

	/**
	 * 按插入顺序读取第 index 个值，越界返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 值或 {@code null}
	 */
	public V get(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		int i = 0;
		for (V value : values()) {
			if (i++ == index) {
				return value;
			}
		}
		return null;
	}
}
