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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 双向映射：键值互查，参考 Guava {@code BiMap} 与 Hutool {@code BiMap} 设计。
 * <p>
 * 线程不安全；写入重复键或重复值时清理旧映射（后写覆盖）。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 0.1.0
 */
public class BiMap<K, V> {

	private final Map<K, V> keyValue = new LinkedHashMap<>();
	private final Map<V, K> valueKey = new LinkedHashMap<>();

	/**
	 * 创建空双向映射。
	 */
	public BiMap() {
	}

	/**
	 * 从已有 Map 创建双向映射。
	 *
	 * @param map 初始映射
	 */
	public BiMap(Map<K, V> map) {
		if (map != null) {
			putAll(map);
		}
	}

	/**
	 * 写入映射（覆盖旧键/旧值对应的反向映射）。
	 *
	 * @param key   键
	 * @param value 值
	 * @return 被覆盖的旧值或 {@code null}
	 */
	public V put(K key, V value) {
		V oldValue = keyValue.get(key);
		if (oldValue != null) {
			valueKey.remove(oldValue);
		}
		K oldKey = valueKey.get(value);
		if (oldKey != null) {
			keyValue.remove(oldKey);
		}
		keyValue.put(key, value);
		valueKey.put(value, key);
		return oldValue;
	}

	/**
	 * 批量写入。
	 *
	 * @param map 映射
	 */
	public void putAll(Map<K, V> map) {
		if (map == null) {
			return;
		}
		for (Map.Entry<K, V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 按键取值。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	public V get(K key) {
		return keyValue.get(key);
	}

	/**
	 * 按值取键。
	 *
	 * @param value 值
	 * @return 键或 {@code null}
	 */
	public K getKey(V value) {
		return valueKey.get(value);
	}

	/**
	 * 按键移除。
	 *
	 * @param key 键
	 * @return 被移除的值或 {@code null}
	 */
	public V remove(K key) {
		V value = keyValue.remove(key);
		if (value != null) {
			valueKey.remove(value);
		}
		return value;
	}

	/**
	 * 是否包含键。
	 *
	 * @param key 键
	 * @return 是否包含
	 */
	public boolean containsKey(K key) {
		return keyValue.containsKey(key);
	}

	/**
	 * 是否包含值。
	 *
	 * @param value 值
	 * @return 是否包含
	 */
	public boolean containsValue(V value) {
		return valueKey.containsKey(value);
	}

	/**
	 * 映射数量。
	 *
	 * @return 数量
	 */
	public int size() {
		return keyValue.size();
	}

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return keyValue.isEmpty();
	}

	/**
	 * 清空。
	 */
	public void clear() {
		keyValue.clear();
		valueKey.clear();
	}

	/**
	 * 正向视图（键→值）。
	 *
	 * @return 键→值 Map
	 */
	public Map<K, V> asMap() {
		return Collections.unmodifiableMap(keyValue);
	}
}