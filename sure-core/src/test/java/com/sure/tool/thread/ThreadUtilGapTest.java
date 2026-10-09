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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * ThreadUtil 覆盖率补测：null 守卫、中断恢复、限时并行超时路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ThreadUtilGapTest {

	@Test
	public void testSleepNullGuards() {
		try {
			ThreadUtil.sleep(1, null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ThreadUtil.sleep((Duration) null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		ThreadUtil.sleep(1, TimeUnit.MILLISECONDS);
		ThreadUtil.sleep(Duration.ofMillis(1));
	}

	@Test
	public void testSleepInterruptibly() throws Exception {
		// null / 零 / 负 直接返回
		ThreadUtil.sleepInterruptibly(null);
		ThreadUtil.sleepInterruptibly(Duration.ZERO);
		ThreadUtil.sleepInterruptibly(Duration.ofMillis(-1));
		ThreadUtil.sleepInterruptibly(Duration.ofMillis(1));
	}

	@Test
	public void testScheduledExecutorGuards() {
		try {
			ThreadUtil.newScheduledExecutor(0, "x");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ThreadUtil.newScheduledExecutor(1, "  ");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		ExecutorService es = ThreadUtil.newScheduledExecutor(1, "gap");
		es.shutdownNow();
	}

	@Test
	public void testJoinQuietlyInterrupt() throws Exception {
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		});
		t.setDaemon(true);
		t.start();
		Thread.currentThread().interrupt();
		ThreadUtil.joinQuietly(t);
		Assert.assertTrue(Thread.currentThread().isInterrupted());
		// 清除中断标志
		Thread.interrupted();
		ThreadUtil.joinQuietly(null);
		t.interrupt();
	}

	@Test
	public void testShutdownQuietlyInterrupt() {
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.submit(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		});
		Thread.currentThread().interrupt();
		ThreadUtil.shutdownQuietly(ex);
		Assert.assertTrue(Thread.currentThread().isInterrupted());
		Thread.interrupted();
		ThreadUtil.shutdownQuietly(null);
	}

	@Test
	public void testInvokeAllTimeout() {
		// 空输入
		Assert.assertTrue(ThreadUtil.invokeAll(null, Duration.ofSeconds(1)).isEmpty());
		Assert.assertTrue(ThreadUtil.invokeAll(List.of(), Duration.ofSeconds(1)).isEmpty());
		// 正常完成
		List<Integer> r = ThreadUtil.invokeAll(Arrays.asList(() -> 1, () -> 2), Duration.ofSeconds(5));
		Assert.assertEquals(Arrays.asList(1, 2), r);
		// 超时 -> CancellationException 包装
		try {
			ThreadUtil.invokeAll(Arrays.asList(() -> {
				Thread.sleep(10000);
				return 1;
			}), Duration.ofMillis(20));
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			// expected
		}
		// 任务抛 RuntimeException -> 直接抛出
		try {
			ThreadUtil.invokeAll(Arrays.asList(() -> {
				throw new IllegalStateException("task boom");
			}), Duration.ofSeconds(5));
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			Assert.assertEquals("task boom", e.getMessage());
		}
	}

	@Test
	public void testInvokeAllPlain() {
		Assert.assertTrue(ThreadUtil.invokeAll(null).isEmpty());
		Assert.assertTrue(ThreadUtil.invokeAll(List.of()).isEmpty());
		List<Integer> r = ThreadUtil.invokeAll(Arrays.asList(() -> 10, () -> 20));
		Assert.assertEquals(Arrays.asList(10, 20), r);
	}

	@Test
	public void testParallel() {
		ThreadUtil.parallel();
		ThreadUtil.parallel((Runnable[]) null);
		int[] holder = new int[1];
		ThreadUtil.parallel(() -> holder[0] = 1);
		Assert.assertEquals(1, holder[0]);
		try {
			ThreadUtil.parallel(() -> {
				throw new UnsupportedOperationException("x");
			});
			Assert.fail("应抛异常");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void testInterruptedBranches() {
		java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
		Thread.currentThread().interrupt();
		try {
			ThreadUtil.shutdownQuietly(exec);
		} finally {
			Thread.interrupted();
		}
		List<java.util.concurrent.Callable<Integer>> tasks = Arrays.asList(() -> {
			Thread.sleep(10000);
			return 1;
		});
		Thread.currentThread().interrupt();
		try {
			ThreadUtil.invokeAll(tasks, Duration.ofSeconds(5));
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			// expected
		} finally {
			Thread.interrupted();
		}
	}
}
