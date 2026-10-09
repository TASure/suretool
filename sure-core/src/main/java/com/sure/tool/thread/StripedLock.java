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
package com.sure.tool.thread;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 定长分段锁：按 key 哈希映射到固定数量的锁段，避免 {@link LockUtil} 按 key 无界缓存导致的内存增长，
 * 参考 Guava {@code Striped<Lock>} 设计。
 *
 * <p>段数固定为 2 的幂（向上取整），默认 16 段；相同 key 恒映射到同一段锁（互斥），
 * 不同 key 大概率分散到不同段（可并行）。适合对热点 key 的粗粒度互斥场景。</p>
 *
 * @since 1.11.0
 */
public class StripedLock {

	/**
	 * 默认段数。
	 */
	public static final int DEFAULT_STRIPES = 16;

	/**
	 * 分段锁数组（不可变长度）。
	 */
	private final ReentrantLock[] locks;

	/**
	 * 构造默认 16 段分段锁。
	 */
	public StripedLock() {
		this(DEFAULT_STRIPES);
	}

	/**
	 * 构造指定段数分段锁（段数向上取整为 2 的幂）。
	 *
	 * @param stripes 段数，必须大于 0
	 * @throws IllegalArgumentException stripes 不大于 0
	 */
	public StripedLock(int stripes) {
		if (stripes <= 0) {
			throw new IllegalArgumentException("stripes 必须大于 0");
		}
		int size = 1;
		while (size < stripes) {
			size <<= 1;
		}
		this.locks = new ReentrantLock[size];
		for (int i = 0; i < size; i++) {
			this.locks[i] = new ReentrantLock();
		}
	}

	/**
	 * 按 key 获取对应段锁。相同 key 恒返回同一把锁。
	 *
	 * @param key 锁键，{@code null} 视为常量 0
	 * @return 段锁
	 */
	public ReentrantLock get(Object key) {
		int hash = key == null ? 0 : key.hashCode();
		hash ^= hash >>> 16;
		return locks[hash & (locks.length - 1)];
	}

	/**
	 * 按索引直接获取段锁（越界自动取模）。
	 *
	 * @param index 段索引，可为任意整数
	 * @return 段锁
	 */
	public ReentrantLock get(int index) {
		int i = index & (locks.length - 1);
		return locks[i];
	}

	/**
	 * 返回段锁总数。
	 *
	 * @return 段数
	 */
	public int lockCount() {
		return locks.length;
	}

	/**
	 * 在 key 对应段锁保护下执行动作（锁内执行，不可重入嵌套使用）。
	 *
	 * @param key    锁键
	 * @param action 动作
	 */
	public void runLocked(Object key, Runnable action) {
		ReentrantLock lock = get(key);
		lock.lock();
		try {
			action.run();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 在 key 对应段锁保护下执行并返回值。
	 *
	 * @param key    锁键
	 * @param action 动作
	 * @param <T>    返回值类型
	 * @return 动作结果
	 */
	public <T> T runLocked(Object key, Supplier<T> action) {
		ReentrantLock lock = get(key);
		lock.lock();
		try {
			return action.get();
		} finally {
			lock.unlock();
		}
	}
}
