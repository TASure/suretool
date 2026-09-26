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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * P0 批 1（v1.1.0）：结构化并发 StructuredTaskUtil 测试（JDK 21+）。
 */
public class StructuredTaskUtilTest {

	/**
	 * 全部任务成功后按输入顺序返回结果。
	 */
	@Test
	public void testParallelOrderedResult() {
		List<Integer> result = StructuredTaskUtil.parallel(
				() -> 1,
				() -> 2,
				() -> 3);
		Assert.assertEquals(List.of(1, 2, 3), result);
	}

	/**
	 * 任务真实并发执行（CountDownLatch 证明同时进入执行体）。
	 */
	@Test
	public void testParallelConcurrentExecution() throws Exception {
		int n = 3;
		CountDownLatch ready = new CountDownLatch(n);
		CountDownLatch start = new CountDownLatch(1);
		List<Callable<Integer>> tasks = new java.util.ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			final int value = i;
			tasks.add(() -> {
				ready.countDown();
				// 所有任务都进入执行体后才放行，验证真正并发而非串行
				start.await(5, TimeUnit.SECONDS);
				return value;
			});
		}
		java.util.concurrent.Future<List<Integer>> future = java.util.concurrent.Executors
				.newSingleThreadExecutor().submit(() -> StructuredTaskUtil.parallel(tasks));
		Assert.assertTrue("3 个任务应全部并发启动", ready.await(5, TimeUnit.SECONDS));
		start.countDown();
		Assert.assertEquals(List.of(0, 1, 2), future.get(10, TimeUnit.SECONDS));
	}

	/**
	 * 任一任务失败时抛出 RuntimeException，并携带首个任务异常。
	 */
	@Test
	public void testParallelFailurePropagates() {
		RuntimeException boom = new IllegalStateException("boom");
		try {
			StructuredTaskUtil.parallel(
					() -> 1,
					() -> { throw boom; },
					() -> 3);
			Assert.fail("应抛出异常");
		} catch (RuntimeException e) {
			Throwable cause = rootCause(e);
			Assert.assertTrue("异常链应包含任务异常", cause == boom);
		}
	}

	/**
	 * 空集合返回空列表，不抛异常。
	 */
	@Test
	public void testParallelEmptyReturnsEmpty() {
		Assert.assertEquals(List.of(), StructuredTaskUtil.parallel(java.util.List.of()));
	}

	/**
	 * 超时：任务耗时超过时限抛出超时异常。
	 */
	@Test
	public void testParallelTimeout() {
		long start = System.nanoTime();
		try {
			StructuredTaskUtil.parallel(
					java.util.List.of(() -> {
						ThreadUtil.sleep(1000);
						return 1;
					}),
					Duration.ofMillis(100));
			Assert.fail("应抛出超时异常");
		} catch (RuntimeException e) {
			Assert.assertEquals("parallel 执行超时", e.getMessage());
		}
		long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		Assert.assertTrue("超时异常应在时限附近抛出，实际 " + elapsed + "ms", elapsed < 1500);
	}

	/**
	 * anyOf 返回第一个成功结果。
	 */
	@Test
	public void testAnyOfReturnsFirstSuccess() {
		int result = StructuredTaskUtil.anyOf(
				() -> {
					ThreadUtil.sleep(300);
					return 1;
				},
				() -> 2);
		Assert.assertEquals(2, result);
	}

	/**
	 * anyOf 全部失败抛出异常。
	 */
	@Test
	public void testAnyOfAllFailed() {
		try {
			StructuredTaskUtil.anyOf(
					() -> { throw new IllegalStateException("a"); },
					() -> { throw new IllegalStateException("b"); });
			Assert.fail("应抛出异常");
		} catch (RuntimeException e) {
			Assert.assertTrue(e.getMessage().contains("全部任务失败"));
		}
	}

	/**
	 * anyOf 超时抛出异常。
	 */
	@Test
	public void testAnyOfTimeout() {
		try {
			StructuredTaskUtil.anyOf(
					java.util.List.of(() -> {
						ThreadUtil.sleep(1000);
						return 1;
					}),
					Duration.ofMillis(100));
			Assert.fail("应抛出超时异常");
		} catch (RuntimeException e) {
			Assert.assertEquals("anyOf 执行超时", e.getMessage());
		}
	}

	/**
	 * anyOf 空集合视为参数错误。
	 */
	@Test
	public void testAnyOfEmptyRejected() {
		try {
			StructuredTaskUtil.anyOf(java.util.List.of(), null);
			Assert.fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	/**
	 * ThreadUtil.invokeAll 超时版本：超时抛出并携带原因。
	 */
	@Test
	public void testInvokeAllWithTimeout() {
		long start = System.nanoTime();
		try {
			ThreadUtil.invokeAll(
					java.util.List.of(() -> {
						ThreadUtil.sleep(1000);
						return 1;
					}),
					Duration.ofMillis(100));
			Assert.fail("应抛出超时异常");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("超时"));
		}
		long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		Assert.assertTrue("应在时限附近返回，实际 " + elapsed + "ms", elapsed < 1500);
	}

	/**
	 * 递归找到最深层异常原因。
	 */
	private static Throwable rootCause(Throwable t) {
		Throwable cause = t;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		return cause;
	}
}
