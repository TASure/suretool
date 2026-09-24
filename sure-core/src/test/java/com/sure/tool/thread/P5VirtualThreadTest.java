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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * P5（v1.1.0）：Virtual Threads 并发工具测试（JDK 21+）。
 */
public class P5VirtualThreadTest {

	@Test
	public void testStartVirtualThread() throws Exception {
		AtomicInteger flag = new AtomicInteger();
		Thread thread = ThreadUtil.startVirtualThread(flag::incrementAndGet);
		thread.join(TimeUnit.SECONDS.toMillis(5));
		Assert.assertFalse(thread.isAlive());
		Assert.assertTrue(thread.isVirtual());
		Assert.assertEquals(1, flag.get());
	}

	@Test
	public void testNewVirtualThreadNotStarted() throws Exception {
		AtomicInteger flag = new AtomicInteger();
		Thread thread = ThreadUtil.newVirtualThread(flag::incrementAndGet, "vt-test");
		Assert.assertEquals("vt-test", thread.getName());
		Assert.assertFalse(thread.isAlive());
		thread.start();
		thread.join(TimeUnit.SECONDS.toMillis(5));
		Assert.assertEquals(1, flag.get());
	}

	@Test
	public void testVirtualExecutor() throws Exception {
		ExecutorService executor = ThreadUtil.virtualExecutor();
		Assert.assertTrue(executor.submit(() -> Thread.currentThread().isVirtual()).get());
		executor.shutdown();
	}

	@Test
	public void testVirtualExecutorNamed() throws Exception {
		ExecutorService executor = ThreadUtil.virtualExecutor("vt-pool");
		String name = executor.submit(() -> Thread.currentThread().getName()).get();
		Assert.assertTrue(name.startsWith("vt-pool-"));
		executor.shutdown();
	}

	@Test
	public void testVirtualThreadFactory() throws Exception {
		Thread thread = ThreadUtil.virtualThreadFactory("vt-fac").newThread(() -> {
		});
		Assert.assertTrue(thread.isVirtual());
		Assert.assertTrue(thread.getName().startsWith("vt-fac-"));
	}

	@Test
	public void testIsVirtual() {
		Assert.assertFalse(ThreadUtil.isVirtual(Thread.currentThread()));
		Assert.assertTrue(ThreadUtil.isVirtual(ThreadUtil.startVirtualThread(() -> {
		})));
		Assert.assertFalse(ThreadUtil.isVirtual(null));
		Assert.assertFalse(ThreadUtil.isVirtual());
	}

	@Test
	public void testParallelAllDone() {
		int taskCount = 20;
		AtomicInteger counter = new AtomicInteger();
		Runnable[] tasks = new Runnable[taskCount];
		for (int i = 0; i < taskCount; i++) {
			tasks[i] = counter::incrementAndGet;
		}
		ThreadUtil.parallel(tasks);
		Assert.assertEquals(taskCount, counter.get());
	}

	@Test
	public void testParallelEmpty() {
		ThreadUtil.parallel(new Runnable[0]);
		ThreadUtil.parallel((Runnable[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParallelAggregateException() {
		ThreadUtil.parallel(() -> {
			throw new IllegalArgumentException("boom");
		});
	}

	@Test
	public void testInvokeAllResultsInOrder() throws Exception {
		List<Callable<Integer>> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			final int value = i;
			tasks.add(() -> {
				Thread.sleep(1);
				return value;
			});
		}
		List<Integer> results = ThreadUtil.invokeAll(tasks);
		Assert.assertEquals(10, results.size());
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals(Integer.valueOf(i), results.get(i));
		}
	}

	@Test
	public void testInvokeAllEmpty() {
		Assert.assertTrue(ThreadUtil.invokeAll(null).isEmpty());
		Assert.assertTrue(ThreadUtil.invokeAll(new ArrayList<>()).isEmpty());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvokeAllAggregateException() {
		List<Callable<Integer>> tasks = new ArrayList<>();
		tasks.add(() -> 1);
		tasks.add(() -> {
			throw new IllegalStateException("fail");
		});
		ThreadUtil.invokeAll(tasks);
	}

	@Test
	public void testInterruptionRestoresFlag() throws Exception {
		// 任务阻塞在 latch 上，主线程中断后 get() 必然感知中断
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		Thread.currentThread().interrupt();
		try {
			ThreadUtil.parallel(() -> {
				try {
					release.await();
				} catch (InterruptedException ignored) {
					// 虚拟线程任务被中断，退出
				}
			});
			Assert.fail("中断时 parallel 应抛异常");
		} catch (IllegalStateException expected) {
			Assert.assertTrue("应保留中断标志", Thread.currentThread().isInterrupted());
		} finally {
			release.countDown();
			Thread.interrupted();
		}
	}
}
