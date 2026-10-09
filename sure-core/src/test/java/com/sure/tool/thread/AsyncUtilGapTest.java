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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * AsyncUtil 覆盖率补测：自定义执行器、anyOf 全失败、joinAll 任务失败路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class AsyncUtilGapTest {

	@Test
	public void testRunAsyncWithExecutor() throws Exception {
		ExecutorService ex = Executors.newSingleThreadExecutor();
		Future<?> ok = AsyncUtil.runAsync(() -> {
		}, ex);
		ok.get(5, TimeUnit.SECONDS);
		// 任务抛异常 -> completeExceptionally
		Future<?> bad = AsyncUtil.runAsync(() -> {
			throw new RuntimeException("boom");
		}, ex);
		try {
			bad.get(5, TimeUnit.SECONDS);
			Assert.fail("应抛异常");
		} catch (ExecutionException e) {
			Assert.assertEquals("boom", e.getCause().getMessage());
		}
		ex.shutdownNow();
	}

	@Test
	public void testAllOfFailure() throws Exception {
		try {
			AsyncUtil.allOf(() -> {
				throw new IllegalStateException("fail1");
			}, () -> 2).get(5, TimeUnit.SECONDS);
			Assert.fail("应抛异常");
		} catch (ExecutionException e) {
			Assert.assertEquals("fail1", e.getCause().getMessage());
		}
		List<Integer> r = AsyncUtil.allOf(() -> 1, () -> 2).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(List.of(1, 2), r);
	}

	@Test
	public void testAnyOf() throws Exception {
		// 快任务在前（实现按顺序 get，首个成功即胜出）
		Integer win = AsyncUtil.anyOf(() -> 2, () -> {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
			return 1;
		}).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(Integer.valueOf(2), win);
		// 成功任务在前，失败在后 -> 成功者胜（命中 ExecutionException 分支）
		Integer win2 = AsyncUtil.anyOf(() -> 7, () -> {
			throw new IllegalStateException("fail-fast");
		}).get(5, TimeUnit.SECONDS);
		Assert.assertEquals(Integer.valueOf(7), win2);
		// 全部失败 -> 传播最后一个异常
		try {
			AsyncUtil.<Integer>anyOf(() -> {
				throw new IllegalStateException("a");
			}, () -> {
				throw new IllegalStateException("b");
			}).get(5, TimeUnit.SECONDS);
			Assert.fail("应抛异常");
		} catch (ExecutionException e) {
			Assert.assertNotNull(e.getCause());
		}
	}

	@Test
	public void testJoinAllFailure() {
		try {
			AsyncUtil.joinAll(Duration.ofSeconds(5), () -> 1, () -> {
				throw new IllegalStateException("task boom");
			});
			Assert.fail("应抛异常");
		} catch (ExecutionException e) {
			Assert.assertEquals("task boom", e.getCause().getMessage());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testWithTimeoutAndAwait() throws Exception {
		Assert.assertEquals(3, AsyncUtil.withTimeout(Duration.ofSeconds(5), () -> 3).intValue());
		try {
			AsyncUtil.withTimeout(Duration.ofMillis(20), () -> {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
				return 1;
			});
			Assert.fail("应抛 TimeoutException");
		} catch (java.util.concurrent.TimeoutException e) {
			// expected
		}
		// await 任务异常解包
		try {
			AsyncUtil.await(Duration.ofSeconds(5), AsyncUtil.supplyAsync(() -> {
				throw new ArithmeticException("nope");
			}));
			Assert.fail("应抛 ExecutionException");
		} catch (ExecutionException e) {
			Assert.assertEquals("nope", e.getCause().getMessage());
		}
	}
}
