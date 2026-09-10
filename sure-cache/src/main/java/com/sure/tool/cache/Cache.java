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
package com.sure.tool.cache;

/**
 * 缓存接口，参考 Hutool 的 {@code Cache} 设计。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 0.1.0
 */
public interface Cache<K, V> {

	/**
	 * 读取缓存值，不存在或已过期返回 {@code null}。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	V get(K key);

	/**
	 * 写入缓存。
	 *
	 * @param key   键
	 * @param value 值
	 * @return 被覆盖的旧值或 {@code null}
	 */
	V put(K key, V value);

	/**
	 * 移除缓存项。
	 *
	 * @param key 键
	 * @return 被移除的值或 {@code null}
	 */
	V remove(K key);

	/**
	 * 清空缓存。
	 */
	void clear();

	/**
	 * 缓存项数量。
	 *
	 * @return 数量
	 */
	int size();

	/**
	 * 是否包含键（已过期视为不存在）。
	 *
	 * @param key 键
	 * @return 是否包含
	 */
	boolean containsKey(K key);

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	boolean isEmpty();
}
