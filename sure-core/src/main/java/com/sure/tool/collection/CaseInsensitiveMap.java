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
import java.util.Locale;
import java.util.Map;

/**
 * 大小写不敏感的 Map，继承 {@link LinkedHashMap} 保持插入顺序，参考 Hutool 的 {@code CaseInsensitiveMap} 设计。
 * <p>
 * 键以字符串形式归一化为小写存储：{@code put("Key", 1)} 与 {@code put("key", 2)} 视为同一键（后者覆盖）。
 * {@code null} 键仅允许一个且不适用大小写规则。
 *
 * @param <K> key 类型
 * @param <V> value 类型
 * @author suretool
 * @since 0.1.0
 */
public class CaseInsensitiveMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建空 Map。
	 */
	public CaseInsensitiveMap() {
	}

	/**
	 * 创建带初始容量的 Map。
	 *
	 * @param initialCapacity 初始容量
	 */
	public CaseInsensitiveMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 从既有 Map 创建（键冲突时后者覆盖）。
	 *
	 * @param map 源 Map
	 */
	public CaseInsensitiveMap(Map<? extends K, ? extends V> map) {
		this();
		putAll(map);
	}

	@Override
	public V put(K key, V value) {
		return super.put(normalize(key), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		if (map == null) {
			return;
		}
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V get(Object key) {
		return super.get(normalize(key));
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(normalize(key));
	}

	@Override
	public V remove(Object key) {
		return super.remove(normalize(key));
	}

	/**
	 * 键归一化为小写；{@code null} 原样返回。
	 *
	 * @param key 键
	 * @return 归一化键
	 */
	@SuppressWarnings("unchecked")
	private K normalize(Object key) {
		if (key == null) {
			return null;
		}
		return (K) key.toString().toLowerCase(Locale.ROOT);
	}
}
