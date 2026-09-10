package com.sure.tool.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 定时过期缓存：条目可设定存活时间（TTL），读取时惰性过期，参考 Hutool 的 {@code TimedCache} 设计。
 * <p>
 * 线程安全（synchronized 保护）；默认 TTL 由构造参数指定，{@code put} 可单独覆盖；
 * 过期的条目在访问时被移除，也可调用 {@link #prune()} 主动清理。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
 */
public class TimedCache<K, V> implements Cache<K, V> {

	private final Map<K, TimedValue<V>> map = new HashMap<>();
	private final long defaultTimeout;

	/**
	 * 创建定时缓存。
	 *
	 * @param defaultTimeout 默认存活时间（毫秒），&lt;=0 表示不过期
	 */
	public TimedCache(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
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
		TimedValue<V> old = map.put(key, new TimedValue<>(value, timeout));
		return old == null ? null : old.value;
	}

	/**
	 * 读取缓存值（过期返回 {@code null} 并移除）。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	@Override
	public synchronized V get(K key) {
		TimedValue<V> entry = map.get(key);
		if (entry == null) {
			return null;
		}
		if (entry.isExpired()) {
			map.remove(key);
			return null;
		}
		return entry.value;
	}

	@Override
	public synchronized V remove(K key) {
		TimedValue<V> entry = map.remove(key);
		return entry == null ? null : entry.value;
	}

	@Override
	public synchronized void clear() {
		map.clear();
	}

	@Override
	public synchronized int size() {
		prune();
		return map.size();
	}

	@Override
	public synchronized boolean containsKey(K key) {
		TimedValue<V> entry = map.get(key);
		if (entry == null) {
			return false;
		}
		if (entry.isExpired()) {
			map.remove(key);
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 主动清理全部过期条目。
	 *
	 * @return 清理数量
	 */
	public synchronized int prune() {
		long now = System.currentTimeMillis();
		int removed = 0;
		Iterator<Map.Entry<K, TimedValue<V>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			TimedValue<V> entry = it.next().getValue();
			if (entry.timeout > 0 && now - entry.createdAt >= entry.timeout) {
				it.remove();
				removed++;
			}
		}
		return removed;
	}

	/**
	 * 带过期时间的缓存值。
	 */
	private static class TimedValue<V> {

		private final V value;
		private final long timeout;
		private final long createdAt = System.currentTimeMillis();

		TimedValue(V value, long timeout) {
			this.value = value;
			this.timeout = timeout;
		}

		boolean isExpired() {
			return timeout > 0 && System.currentTimeMillis() - createdAt >= timeout;
		}
	}
}
