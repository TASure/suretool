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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多值映射：一个键关联多个值（有序、保持插入顺序），对标 Guava {@code Multimap} 与
 * commons-collections4 {@code MultiValuedMap} 的常用子集，零依赖。
 *
 * <p>典型场景：标签分组、一对多关联、按组收集数据。内部使用
 * {@link LinkedHashMap} 与 {@link ArrayList}，迭代顺序稳定。</p>
 *
 * <p><b>线程安全</b>：本类非线程安全；并发场景需外部同步。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @since 1.10.0
 */
public class MultiMap<K, V> {

	private final Map<K, List<V>> map = new LinkedHashMap<>();

	/**
	 * 添加一个值到指定键，键不存在时自动创建列表。
	 *
	 * @param key   键
	 * @param value 值（可为 null）
	 * @return 若该键此前未包含此值返回 {@code true}
	 */
	public boolean put(K key, V value) {
		return map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
	}

	/**
	 * 批量添加多个值到指定键。
	 *
	 * @param key    键
	 * @param values 值集合（可为空）
	 * @return 本对象，支持链式调用
	 */
	public MultiMap<K, V> putAll(K key, Collection<? extends V> values) {
		if (values == null || values.isEmpty()) {
			return this;
		}
		map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
		return this;
	}

	/**
	 * 获取指定键关联的所有值；键不存在时返回空不可变列表（不抛异常）。
	 *
	 * @param key 键
	 * @return 值列表（只读视图）
	 */
	public List<V> get(K key) {
		List<V> list = map.get(key);
		if (list == null) {
			return List.of();
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * 移除指定键下的所有值。
	 *
	 * @param key 键
	 * @return 被移除的值列表；键不存在返回空列表
	 */
	public List<V> removeAll(K key) {
		List<V> removed = map.remove(key);
		return removed == null ? List.of() : removed;
	}

	/**
	 * 移除指定键下的单个值。
	 *
	 * @param key   键
	 * @param value 值
	 * @return 移除成功返回 {@code true}
	 */
	public boolean remove(K key, V value) {
		List<V> list = map.get(key);
		if (list == null) {
			return false;
		}
		boolean removed = list.remove(value);
		if (list.isEmpty()) {
			map.remove(key);
		}
		return removed;
	}

	/**
	 * 是否包含指定键。
	 *
	 * @param key 键
	 * @return 是否包含
	 */
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	/**
	 * 是否存在至少一个值与 {@code value} 相等。
	 *
	 * @param value 值
	 * @return 是否存在
	 */
	public boolean containsValue(V value) {
		return values().contains(value);
	}

	/**
	 * 键的数量（去重）。
	 *
	 * @return 键数量
	 */
	public int keyCount() {
		return map.size();
	}

	/**
	 * 全部值的总数（含重复）。
	 *
	 * @return 值总数
	 */
	public int size() {
		int total = 0;
		for (List<V> list : map.values()) {
			total += list.size();
		}
		return total;
	}

	/**
	 * 全部值的扁平集合。
	 *
	 * @return 值集合
	 */
	public List<V> values() {
		List<V> all = new ArrayList<>();
		for (List<V> list : map.values()) {
			all.addAll(list);
		}
		return all;
	}

	/**
	 * 全部键集合（保持插入顺序）。
	 *
	 * @return 键集合
	 */
	public Set<K> keys() {
		return map.keySet();
	}

	/**
	 * 键值对条目集合（key → 其全部值列表）。
	 *
	 * @return 条目集合
	 */
	public Set<Map.Entry<K, List<V>>> entries() {
		return map.entrySet();
	}

	/**
	 * 转为普通 Map 视图（值为只读列表）。
	 *
	 * @return Map 视图
	 */
	public Map<K, List<V>> asMap() {
		Map<K, List<V>> copy = new LinkedHashMap<>();
		for (Map.Entry<K, List<V>> e : map.entrySet()) {
			copy.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
		}
		return copy;
	}

	/**
	 * 清空所有键值。
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
