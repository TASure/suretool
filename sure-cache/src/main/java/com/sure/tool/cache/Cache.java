package com.sure.tool.cache;

/**
 * 缓存接口，参考 Hutool 的 {@code Cache} 设计。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author suretool
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
