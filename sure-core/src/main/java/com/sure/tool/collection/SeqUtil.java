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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Sequenced 集合工具类（JDK 21 {@link SequencedCollection} / {@link SequencedMap}）。
 *
 * <p>提供首尾安全访问、倒序快照、不可变前置/追加等能力；
 * Guava / Hutool / Apache Commons 均无等价实现。</p>
 *
 * @since 1.1.0
 */
public final class SeqUtil {

	private SeqUtil() {
	}

	/**
	 * 倒序返回集合副本（快照，非视图）。
	 *
	 * @param collection 输入集合，可为空
	 * @param <T>        元素类型
	 * @return 倒序的不可变列表；输入为空时返回空列表
	 */
	public static <T> List<T> reversed(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return List.of();
		}
		if (collection instanceof SequencedCollection<T> sequenced) {
			List<T> list = new ArrayList<>(sequenced.reversed());
			return List.copyOf(list);
		}
		List<T> list = new ArrayList<>(collection);
		Collections.reverse(list);
		return List.copyOf(list);
	}

	/**
	 * 安全取首元素。
	 *
	 * @param collection 输入集合，可为空
	 * @param <T>        元素类型
	 * @return 首元素；空集合返回 {@code null}
	 */
	public static <T> T firstOrNull(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		if (collection instanceof SequencedCollection<T> sequenced) {
			return sequenced.getFirst();
		}
		Iterator<T> iterator = collection.iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	/**
	 * 安全取尾元素。
	 *
	 * @param collection 输入集合，可为空
	 * @param <T>        元素类型
	 * @return 尾元素；空集合返回 {@code null}
	 */
	public static <T> T lastOrNull(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		if (collection instanceof SequencedCollection<T> sequenced) {
			return sequenced.getLast();
		}
		T last = null;
		for (T element : collection) {
			last = element;
		}
		return last;
	}

	/**
	 * 返回「首元素 + 原集合」的不可变新列表，原集合不变。
	 *
	 * @param collection 输入集合，可为空
	 * @param first      前置元素
	 * @param <T>        元素类型
	 * @return 不可变列表
	 */
	public static <T> List<T> withFirst(Collection<T> collection, T first) {
		List<T> list = new ArrayList<>((collection == null ? 0 : collection.size()) + 1);
		list.add(first);
		if (collection != null) {
			list.addAll(collection);
		}
		return List.copyOf(list);
	}

	/**
	 * 返回「原集合 + 尾元素」的不可变新列表，原集合不变。
	 *
	 * @param collection 输入集合，可为空
	 * @param last       追加元素
	 * @param <T>        元素类型
	 * @return 不可变列表
	 */
	public static <T> List<T> withLast(Collection<T> collection, T last) {
		List<T> list = new ArrayList<>((collection == null ? 0 : collection.size()) + 1);
		if (collection != null) {
			list.addAll(collection);
		}
		list.add(last);
		return List.copyOf(list);
	}

	/**
	 * 安全取有序 Map 的首键。
	 *
	 * @param map 输入 Map，可为空
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 首键；空 Map 返回 {@code null}
	 */
	public static <K, V> K firstKeyOrNull(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (map instanceof SequencedMap<K, V> sequenced) {
			return sequenced.firstEntry().getKey();
		}
		Iterator<K> iterator = map.keySet().iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	/**
	 * 安全取有序 Map 的尾键。
	 *
	 * @param map 输入 Map，可为空
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 尾键；空 Map 返回 {@code null}
	 */
	public static <K, V> K lastKeyOrNull(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (map instanceof SequencedMap<K, V> sequenced) {
			return sequenced.lastEntry().getKey();
		}
		K last = null;
		for (K key : map.keySet()) {
			last = key;
		}
		return last;
	}

	/**
	 * 安全取有序 Map 的首条目。
	 *
	 * @param map 输入 Map，可为空
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 首条目；空 Map 返回 {@code null}
	 */
	public static <K, V> Map.Entry<K, V> firstEntryOrNull(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (map instanceof SequencedMap<K, V> sequenced) {
			return sequenced.firstEntry();
		}
		Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	/**
	 * 安全取有序 Map 的尾条目。
	 *
	 * @param map 输入 Map，可为空
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 尾条目；空 Map 返回 {@code null}
	 */
	public static <K, V> Map.Entry<K, V> lastEntryOrNull(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (map instanceof SequencedMap<K, V> sequenced) {
			return sequenced.lastEntry();
		}
		Map.Entry<K, V> last = null;
		for (Map.Entry<K, V> entry : map.entrySet()) {
			last = entry;
		}
		return last;
	}
}
