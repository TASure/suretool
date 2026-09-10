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

import java.util.HashMap;
import java.util.Map;

/**
 * 最不经常使用缓存：容量满时淘汰访问频率最低的条目（频率相同淘汰最早写入），参考 Hutool 的 {@code LFUCache} 设计。
 * <p>
 * 线程安全（synchronized 保护）；淘汰为 O(n) 扫描，适合中小规模缓存。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 0.1.0
 */
public class LfuCache<K, V> implements Cache<K, V> {

	private final Map<K, Node<V>> map;
	private final int capacity;

	/**
	 * 创建 LFU 缓存。
	 *
	 * @param capacity 容量上限（&gt;0）
	 */
	public LfuCache(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("容量必须为正数: " + capacity);
		}
		this.capacity = capacity;
		this.map = new HashMap<>();
	}

	@Override
	public synchronized V get(K key) {
		Node<V> node = map.get(key);
		if (node == null) {
			return null;
		}
		node.count++;
		return node.value;
	}

	@Override
	public synchronized V put(K key, V value) {
		Node<V> node = map.get(key);
		if (node != null) {
			V old = node.value;
			node.value = value;
			node.count++;
			return old;
		}
		if (map.size() >= capacity) {
			evictLowestFrequency();
		}
		map.put(key, new Node<>(value));
		return null;
	}

	@Override
	public synchronized V remove(K key) {
		Node<V> node = map.remove(key);
		return node == null ? null : node.value;
	}

	@Override
	public synchronized void clear() {
		map.clear();
	}

	@Override
	public synchronized int size() {
		return map.size();
	}

	@Override
	public synchronized boolean containsKey(K key) {
		return map.containsKey(key);
	}

	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * 淘汰频率最低的条目。
	 */
	private void evictLowestFrequency() {
		long minCount = Long.MAX_VALUE;
		K victim = null;
		for (Map.Entry<K, Node<V>> entry : map.entrySet()) {
			if (entry.getValue().count < minCount) {
				minCount = entry.getValue().count;
				victim = entry.getKey();
			}
		}
		if (victim != null) {
			map.remove(victim);
		}
	}

	/**
	 * 缓存节点。
	 */
	private static class Node<V> {

		private V value;
		private long count;

		Node(V value) {
			this.value = value;
		}
	}
}
