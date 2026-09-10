package com.sure.tool.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 先进先出缓存：容量满时淘汰最早写入的条目，参考 Hutool 的 {@code FIFOCache} 设计。
 * <p>
 * 线程安全（synchronized 保护）；{@code get} 不改变淘汰顺序。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 0.1.0
 */
public class FifoCache<K, V> implements Cache<K, V> {

	private final LinkedHashMap<K, V> map;
	private final int capacity;

	/**
	 * 创建 FIFO 缓存。
	 *
	 * @param capacity 容量上限（&gt;0）
	 */
	public FifoCache(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("容量必须为正数: " + capacity);
		}
		this.capacity = capacity;
		this.map = new LinkedHashMap<>();
	}

	@Override
	public synchronized V get(K key) {
		return map.get(key);
	}

	@Override
	public synchronized V put(K key, V value) {
		V old = map.put(key, value);
		evictIfNeeded();
		return old;
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

	/**
	 * 超出容量时淘汰最旧条目。
	 */
	private void evictIfNeeded() {
		while (map.size() > capacity) {
			Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
			if (it.hasNext()) {
				it.next();
				it.remove();
			}
		}
	}
}
