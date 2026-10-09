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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * AsyncUtil 批25 测试（虚拟线程一等公民）。
 */
public class AsyncUtilBatch25Test {

	/**
	 * runAsync 完成并传播结果。
	 */
	@Test
	public void testRunAsync() throws Exception {
		AtomicInteger executed = new AtomicInteger();
		AsyncUtil.runAsync(executed::incrementAndGet).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(1, executed.get());
	}

	/**
	 * runAsync 异常传播。
	 */
	@Test
	public void testRunAsyncFailure() {
		try {
			AsyncUtil.runAsync(() -> {
				throw new IllegalStateException("boom");
			}).get(5, TimeUnit.SECONDS);
			Assert.fail("应传播异常");
		} catch (ExecutionException e) {
			Assert.assertEquals("boom", e.getCause().getMessage());
		} catch (Exception e) {
			Assert.fail("不应是其他异常: " + e);
		}
	}

	/**
	 * supplyAsync 返回 CompletableFuture 值。
	 */
	@Test
	public void testSupplyAsync() throws Exception {
		CompletableFuture<String> cf = AsyncUtil.supplyAsync(() -> "hello");
		Assert.assertEquals("hello", cf.get(5, TimeUnit.SECONDS));
	}

	/**
	 * allOf 聚合全部结果（保持输入顺序）。
	 */
	@Test
	public void testAllOf() throws Exception {
		List<Integer> values = AsyncUtil.allOf(
				() -> 1,
				() -> 2,
				() -> 3).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(List.of(1, 2, 3), values);
	}

	/**
	 * allOf 任一失败取消其余并传播首个异常。
	 */
	@Test
	public void testAllOfFailureCancelsOthers() throws Exception {
		AtomicInteger slowRan = new AtomicInteger();
		CompletableFuture<List<Integer>> cf = AsyncUtil.allOf(
				() -> {
					throw new IllegalStateException("first-fail");
				},
				() -> {
					slowRan.incrementAndGet();
					Thread.sleep(1000);
					return 42;
				});
		try {
			cf.get(5, TimeUnit.SECONDS);
			Assert.fail("应传播异常");
		} catch (ExecutionException e) {
			Assert.assertEquals("first-fail", e.getCause().getMessage());
		}
	}

	/**
	 * anyOf 首个成功即返回。
	 */
	@Test
	public void testAnyOf() throws Exception {
		Integer winner = AsyncUtil.anyOf(
				() -> 42,
				() -> {
					Thread.sleep(500);
					return -1;
				}).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(Integer.valueOf(42), winner);
	}

	/**
	 * withTimeout 正常返回。
	 */
	@Test
	public void testWithTimeoutSuccess() throws Exception {
		Integer value = AsyncUtil.withTimeout(Duration.ofSeconds(3), () -> 7);
		Assert.assertEquals(Integer.valueOf(7), value);
	}

	/**
	 * withTimeout 超时抛 TimeoutException。
	 */
	@Test
	public void testWithTimeoutExpired() {
		try {
			AsyncUtil.withTimeout(Duration.ofMillis(100), () -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
				return 1;
			});
			Assert.fail("应抛 TimeoutException");
		} catch (TimeoutException expected) {
			// 预期
		} catch (Exception e) {
			Assert.fail("不应是其他异常: " + e);
		}
	}

	/**
	 * await 解包 ExecutionException 为真实 cause。
	 */
	@Test
	public void testAwaitUnwrap() {
		CompletableFuture<Integer> cf = AsyncUtil.supplyAsync(() -> {
			throw new IllegalStateException("inner-fail");
		});
		try {
			AsyncUtil.await(Duration.ofSeconds(3), cf);
			Assert.fail("应抛 ExecutionException");
		} catch (ExecutionException e) {
			Assert.assertEquals("inner-fail", e.getCause().getMessage());
		} catch (Exception e) {
			Assert.fail("不应是其他异常: " + e);
		}
	}

	/**
	 * joinAll 限时并行聚合。
	 */
	@Test
	public void testJoinAll() throws Exception {
		List<String> values = AsyncUtil.joinAll(Duration.ofSeconds(3),
				() -> "a",
				() -> "b");
		Assert.assertEquals(List.of("a", "b"), values);
	}

	/**
	 * joinAll 超时抛 TimeoutException。
	 */
	@Test
	public void testJoinAllTimeout() {
		try {
			AsyncUtil.joinAll(Duration.ofMillis(100),
					() -> {
						Thread.sleep(1000);
						return 1;
					});
			Assert.fail("应抛 TimeoutException");
		} catch (TimeoutException expected) {
			// 预期
		} catch (Exception e) {
			Assert.fail("不应是其他异常: " + e);
		}
	}
}
