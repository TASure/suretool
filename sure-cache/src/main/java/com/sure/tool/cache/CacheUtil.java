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
 * 缓存门面：快速创建各种策略缓存，参考 Hutool 的 {@code CacheUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CacheUtil {

	private CacheUtil() {
	}

	/**
	 * 创建 FIFO 缓存。
	 *
	 * @param capacity 容量
	 * @param <K>      键类型
	 * @param <V>      值类型
	 * @return 缓存
	 */
	public static <K, V> FifoCache<K, V> newFifoCache(int capacity) {
		return new FifoCache<>(capacity);
	}

	/**
	 * 创建 LRU 缓存。
	 *
	 * @param capacity 容量
	 * @param <K>      键类型
	 * @param <V>      值类型
	 * @return 缓存
	 */
	public static <K, V> LruCache<K, V> newLruCache(int capacity) {
		return new LruCache<>(capacity);
	}

	/**
	 * 创建 LFU 缓存。
	 *
	 * @param capacity 容量
	 * @param <K>      键类型
	 * @param <V>      值类型
	 * @return 缓存
	 */
	public static <K, V> LfuCache<K, V> newLfuCache(int capacity) {
		return new LfuCache<>(capacity);
	}

	/**
	 * 创建定时过期缓存。
	 *
	 * @param defaultTimeout 默认存活时间（毫秒）
	 * @param <K>            键类型
	 * @param <V>            值类型
	 * @return 缓存
	 */
	public static <K, V> TimedCache<K, V> newTimedCache(long defaultTimeout) {
		return new TimedCache<>(defaultTimeout);
	}
}
