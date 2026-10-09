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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 弱引用缓存：值以 {@link WeakReference} 持有，对象被 GC 回收后自动清理键；
 * 支持可选 TTL（默认不过期）与容量上限（0 表示无上限），参考 Hutool 的 {@code WeakCache} 设计。
 * <p>
 * 所有操作线程安全（synchronized），可安全用于虚拟线程高并发场景。
 * 值对象仅被本缓存弱引用时，JVM 可在任意时刻回收，读取时返回 {@code null} 并自动移除。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 * @since 1.10.0
 */
public class WeakCache<K, V> implements Cache<K, V> {

	private final Map<K, WeakEntry<V>> map = new HashMap<>();
	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
	private final long defaultTimeout;
	private final int capacity;

	/**
	 * 创建弱引用缓存：无容量上限、默认不过期。
	 */
	public WeakCache() {
		this(0, 0);
	}

	/**
	 * 创建弱引用缓存：无容量上限、指定默认 TTL。
	 *
	 * @param defaultTimeout 默认存活时间（毫秒），&lt;=0 表示不过期
	 */
	public WeakCache(long defaultTimeout) {
		this(0, defaultTimeout);
	}

	/**
	 * 创建弱引用缓存。
	 *
	 * @param capacity       容量上限，&lt;=0 表示无上限；达到上限时优先清理已回收/过期项，仍超则按写入顺序淘汰
	 * @param defaultTimeout 默认存活时间（毫秒），&lt;=0 表示不过期
	 */
	public WeakCache(int capacity, long defaultTimeout) {
		this.capacity = Math.max(0, capacity);
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * 读取缓存值；已过期或已被 GC 回收返回 {@code null} 并移除。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	@Override
	public synchronized V get(K key) {
		clean();
		WeakEntry<V> entry = map.get(key);
		if (entry == null) {
			return null;
		}
		if (entry.isExpired()) {
			map.remove(key);
			return null;
		}
		V value = entry.get();
		if (value == null) {
			map.remove(key);
		}
		return value;
	}

	/**
	 * 写入缓存（使用默认 TTL）。
	 *
	 * @param key   键
	 * @param value 值
	 * @return 被覆盖的旧值或 {@code null}
	 */
	@Override
	public synchronized V put(K key, V value) {
		return put(key, value, defaultTimeout);
	}

	/**
	 * 写入缓存并指定 TTL。
	 *
	 * @param key     键
	 * @param value   值
	 * @param timeout 存活时间（毫秒），&lt;=0 表示不过期
	 * @return 被覆盖的旧值或 {@code null}
	 */
	public synchronized V put(K key, V value, long timeout) {
		clean();
		WeakEntry<V> old = map.put(key, new WeakEntry<>(key, value, queue, timeout));
		evictIfNeeded();
		return old == null ? null : old.get();
	}

	/**
	 * 移除缓存项。
	 *
	 * @param key 键
	 * @return 被移除的值或 {@code null}
	 */
	@Override
	public synchronized V remove(K key) {
		clean();
		WeakEntry<V> entry = map.remove(key);
		return entry == null ? null : entry.get();
	}

	/**
	 * 清空缓存。
	 */
	@Override
	public synchronized void clear() {
		map.clear();
		drainQueue();
	}

	/**
	 * 缓存项数量（含待清理的已回收项，实际读取时自动收敛）。
	 *
	 * @return 数量
	 */
	@Override
	public synchronized int size() {
		clean();
		return map.size();
	}

	/**
	 * 是否包含键（已过期或已回收视为不存在）。
	 *
	 * @param key 键
	 * @return 是否包含
	 */
	@Override
	public synchronized boolean containsKey(K key) {
		return get(key) != null;
	}

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	@Override
	public synchronized boolean isEmpty() {
		clean();
		return map.isEmpty();
	}

	/**
	 * 容量上限。
	 *
	 * @return 容量上限，0 表示无上限
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * 默认 TTL（毫秒），&lt;=0 表示不过期。
	 *
	 * @return 默认 TTL
	 */
	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	/**
	 * 从引用队列回收已失效条目（仅移除仍是当前条目的键，避免覆盖后误删）。
	 */
	private void clean() {
		Reference<?> ref;
		while ((ref = queue.poll()) != null) {
			if (ref instanceof WeakEntry<?> entry && map.get(entry.key) == entry) {
				map.remove(entry.key);
			}
		}
	}

	/**
	 * 清空引用队列。
	 */
	private void drainQueue() {
		while (queue.poll() != null) {
			// drain
		}
	}

	/**
	 * 容量淘汰：清理后仍超过容量上限时按写入顺序移除最旧条目。
	 */
	private void evictIfNeeded() {
		if (capacity <= 0 || map.size() <= capacity) {
			return;
		}
		var iterator = map.entrySet().iterator();
		while (map.size() > capacity && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * 弱引用条目：持有键与过期时间，值被回收时进入引用队列。
	 */
	private static class WeakEntry<V> extends WeakReference<V> {

		private final Object key;
		private final long expireAt;

		WeakEntry(Object key, V value, ReferenceQueue<Object> queue, long timeout) {
			super(value, queue);
			this.key = key;
			this.expireAt = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
		}

		boolean isExpired() {
			return System.currentTimeMillis() > expireAt;
		}
	}
}
