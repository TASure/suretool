package com.sure.tool.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 最近最少使用缓存：容量满时淘汰最久未访问的条目，参考 Hutool 的 {@code LRUCache} 设计。
 * <p>
 * 线程安全（synchronized 保护）；{@code get} 会刷新访问顺序。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 0.1.0
 */
public class LruCache<K, V> implements Cache<K, V> {

	private final LinkedHashMap<K, V> map;
	private final int capacity;

	/**
	 * 创建 LRU 缓存。
	 *
	 * @param capacity 容量上限（&gt;0）
	 */
	public LruCache(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("容量必须为正数: " + capacity);
		}
		this.capacity = capacity;
		this.map = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > capacity;
			}
		};
	}

	@Override
	public synchronized V get(K key) {
		return map.get(key);
	}

	@Override
	public synchronized V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public synchronized V remove(K key) {
		return map.remove(key);
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
}
