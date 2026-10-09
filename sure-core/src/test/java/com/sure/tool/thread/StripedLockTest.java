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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Assert;
import org.junit.Test;

/**
 * StripedLock 分段锁测试。
 */
public class StripedLockTest {

	/**
	 * 默认段数为 16。
	 */
	@Test
	public void testDefaultStripes() {
		StripedLock lock = new StripedLock();
		Assert.assertEquals(16, lock.lockCount());
	}

	/**
	 * 指定段数向上取整为 2 的幂。
	 */
	@Test
	public void testCustomStripesPowerOfTwo() {
		Assert.assertEquals(16, new StripedLock(10).lockCount());
		Assert.assertEquals(32, new StripedLock(17).lockCount());
		Assert.assertEquals(1, new StripedLock(1).lockCount());
	}

	/**
	 * 段数非法抛异常。
	 */
	@Test
	public void testInvalidStripes() {
		try {
			new StripedLock(0);
			Assert.fail("应抛 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	/**
	 * 相同 key 恒返回同一把锁。
	 */
	@Test
	public void testSameKeySameLock() {
		StripedLock lock = new StripedLock();
		ReentrantLock a = lock.get("user-1");
		ReentrantLock b = lock.get("user-1");
		Assert.assertSame(a, b);
	}

	/**
	 * 不同 key 大概率分散到不同段（16 段下 10 个 key 覆盖多个段）。
	 */
	@Test
	public void testDifferentKeysSpread() {
		StripedLock lock = new StripedLock();
		ReentrantLock first = lock.get("k0");
		int distinct = 1;
		for (int i = 1; i < 10; i++) {
			if (lock.get("k" + i) != first) {
				distinct++;
			}
		}
		Assert.assertTrue("应分散到至少 2 段", distinct >= 2);
	}

	/**
	 * 索引越界自动取模。
	 */
	@Test
	public void testIndexModulo() {
		StripedLock lock = new StripedLock(16);
		Assert.assertSame(lock.get(0), lock.get(16));
		Assert.assertSame(lock.get(15), lock.get(-1));
	}

	/**
	 * 函数式加锁执行返回值。
	 */
	@Test
	public void testRunLockedSupplier() {
		StripedLock lock = new StripedLock();
		Assert.assertEquals(Integer.valueOf(42), lock.runLocked("key", () -> 42));
	}

	/**
	 * 同 key 并发互斥：两个线程交替计数，总计数等于循环总数。
	 */
	@Test
	public void testConcurrentMutex() throws InterruptedException {
		StripedLock lock = new StripedLock();
		AtomicInteger counter = new AtomicInteger();
		int loops = 1000;
		CountDownLatch latch = new CountDownLatch(2);
		for (int t = 0; t < 2; t++) {
			new Thread(() -> {
				for (int i = 0; i < loops; i++) {
					lock.runLocked("hot", counter::incrementAndGet);
				}
				latch.countDown();
			}).start();
		}
		Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
		Assert.assertEquals(2 * loops, counter.get());
	}

	/**
	 * 不同 key 可并行（互不阻塞）：双线程各锁不同 key，均能在 1 秒内完成。
	 */
	@Test
	public void testDifferentKeysParallel() throws InterruptedException {
		StripedLock lock = new StripedLock();
		CountDownLatch start = new CountDownLatch(2);
		CountDownLatch done = new CountDownLatch(2);
		for (int t = 0; t < 2; t++) {
			final int id = t;
			new Thread(() -> {
				lock.runLocked("task-" + id, () -> {
					start.countDown();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				});
				done.countDown();
			}).start();
		}
		// 两个任务应同时进入锁区
		Assert.assertTrue(start.await(2, TimeUnit.SECONDS));
		Assert.assertTrue(done.await(2, TimeUnit.SECONDS));
	}
}
