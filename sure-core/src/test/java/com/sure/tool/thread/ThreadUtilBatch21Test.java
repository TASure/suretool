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

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * ThreadUtil 批21 增强测试（命名线程池 / 调度池 / sleep 重载 / joinQuietly）。
 */
public class ThreadUtilBatch21Test {

	/**
	 * newExecutor 创建命名守护线程池并执行任务。
	 */
	@Test
	public void testNewExecutor() throws Exception {
		ExecutorService pool = ThreadUtil.newExecutor(2);
		Assert.assertFalse(pool.isShutdown());
		AtomicInteger result = new AtomicInteger();
		pool.submit(() -> result.incrementAndGet()).get();
		pool.shutdownNow();
		Assert.assertEquals(1, result.get());
	}

	/**
	 * newExecutor 线程名带前缀且为守护线程。
	 */
	@Test
	public void testNewExecutorNaming() throws Exception {
		ExecutorService pool = ThreadUtil.newExecutor(1, "batch21");
		AtomicBoolean daemon = new AtomicBoolean();
		AtomicBoolean nameOk = new AtomicBoolean();
		pool.submit(() -> {
			Thread t = Thread.currentThread();
			daemon.set(t.isDaemon());
			nameOk.set(t.getName().startsWith("sure-batch21-"));
		}).get();
		pool.shutdownNow();
		Assert.assertTrue(daemon.get());
		Assert.assertTrue(nameOk.get());
	}

	/**
	 * newExecutor 参数校验。
	 */
	@Test
	public void testNewExecutorInvalid() {
		try {
			ThreadUtil.newExecutor(0);
			Assert.fail("size=0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
		try {
			ThreadUtil.newExecutor(1, " ");
			Assert.fail("空前缀应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	/**
	 * newScheduledExecutor 周期执行任务。
	 */
	@Test
	public void testScheduledExecutor() throws Exception {
		ScheduledExecutorService pool = ThreadUtil.newScheduledExecutor(1, "sched21");
		AtomicInteger count = new AtomicInteger();
		pool.scheduleAtFixedRate(count::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
		Thread.sleep(160);
		pool.shutdownNow();
		Assert.assertTrue("应至少执行 2 次", count.get() >= 2);
	}

	/**
	 * sleep(Duration) 生效。
	 */
	@Test
	public void testSleepDuration() {
		long start = System.nanoTime();
		ThreadUtil.sleep(Duration.ofMillis(50));
		long elapsed = System.nanoTime() - start;
		Assert.assertTrue("至少休眠 40ms", elapsed >= 40_000_000L);
	}

	/**
	 * sleep(long, TimeUnit) 生效。
	 */
	@Test
	public void testSleepTimeUnit() {
		long start = System.nanoTime();
		ThreadUtil.sleep(50, TimeUnit.MILLISECONDS);
		long elapsed = System.nanoTime() - start;
		Assert.assertTrue("至少休眠 40ms", elapsed >= 40_000_000L);
	}

	/**
	 * joinQuietly 等待线程结束。
	 */
	@Test
	public void testJoinQuietly() throws Exception {
		AtomicBoolean done = new AtomicBoolean();
		Thread t = new Thread(() -> {
			ThreadUtil.sleep(50);
			done.set(true);
		});
		t.start();
		ThreadUtil.joinQuietly(t);
		Assert.assertTrue(done.get());
	}

	/**
	 * joinQuietly 处理 null（空操作）。
	 */
	@Test
	public void testJoinQuietlyNull() {
		ThreadUtil.joinQuietly(null);
	}
}
