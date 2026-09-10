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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 锁工具类：按 key 获取全局读写锁 + 函数式读写执行，参考 Hutool 的 {@code LockUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class LockUtil {

	/**
	 * 全局读写锁缓存。
	 */
	private static final Map<String, ReentrantReadWriteLock> READ_WRITE_LOCKS = new ConcurrentHashMap<>();

	/**
	 * 全局互斥锁缓存。
	 */
	private static final Map<String, Lock> LOCKS = new ConcurrentHashMap<>();

	private LockUtil() {
	}

	/**
	 * 获取（或创建）按 key 区分的全局读写锁。
	 *
	 * @param key 锁标识
	 * @return 读写锁
	 */
	public static ReentrantReadWriteLock createReadWriteLock(String key) {
		return READ_WRITE_LOCKS.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
	}

	/**
	 * 获取（或创建）按 key 区分的全局互斥锁。
	 *
	 * @param key 锁标识
	 * @return 互斥锁
	 */
	public static Lock getLock(String key) {
		return LOCKS.computeIfAbsent(key, k -> createReadWriteLock(k).writeLock());
	}

	/**
	 * 在读锁保护下执行并返回值。
	 *
	 * @param lock   读写锁
	 * @param action 动作
	 * @param <T>    返回值类型
	 * @return 动作结果
	 */
	public static <T> T read(ReentrantReadWriteLock lock, Supplier<T> action) {
		lock.readLock().lock();
		try {
			return action.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 在写锁保护下执行并返回值。
	 *
	 * @param lock   读写锁
	 * @param action 动作
	 * @param <T>    返回值类型
	 * @return 动作结果
	 */
	public static <T> T write(ReentrantReadWriteLock lock, Supplier<T> action) {
		lock.writeLock().lock();
		try {
			return action.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 在读锁保护下执行。
	 *
	 * @param lock   读写锁
	 * @param action 动作
	 */
	public static void runRead(ReentrantReadWriteLock lock, Runnable action) {
		lock.readLock().lock();
		try {
			action.run();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 在写锁保护下执行。
	 *
	 * @param lock   读写锁
	 * @param action 动作
	 */
	public static void runWrite(ReentrantReadWriteLock lock, Runnable action) {
		lock.writeLock().lock();
		try {
			action.run();
		} finally {
			lock.writeLock().unlock();
		}
	}
}
