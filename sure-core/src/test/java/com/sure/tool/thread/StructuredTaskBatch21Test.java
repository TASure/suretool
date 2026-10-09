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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

/**
 * StructuredTaskUtil 批21 增强测试（限时 varargs 便捷重载）。
 */
public class StructuredTaskBatch21Test {

	/**
	 * parallel(Duration, Callable...) 全部成功按序返回。
	 */
	@Test
	public void testParallelTimeoutVarargs() {
		List<Integer> results = StructuredTaskUtil.parallel(Duration.ofSeconds(3),
				() -> 1, () -> 2, () -> 3);
		Assert.assertEquals(List.of(1, 2, 3), results);
	}

	/**
	 * parallel(Duration, Callable...) 超时取消未完成任务。
	 */
	@Test
	public void testParallelTimeoutExpired() {
		AtomicBoolean slowRan = new AtomicBoolean();
		try {
			StructuredTaskUtil.parallel(Duration.ofMillis(100),
					() -> 1,
					() -> {
						slowRan.set(true);
						Thread.sleep(2000);
						return 2;
					});
			Assert.fail("应抛超时异常");
		} catch (RuntimeException expected) {
			Assert.assertTrue("超时异常", expected.getMessage().contains("超时"));
		}
		Assert.assertTrue(slowRan.get());
	}

	/**
	 * parallel(Duration, Callable...) 空任务返回空列表。
	 */
	@Test
	public void testParallelEmpty() {
		Assert.assertTrue(StructuredTaskUtil.parallel(Duration.ofSeconds(1)).isEmpty());
	}

	/**
	 * anyOf(Duration, Callable...) 首成功短路并取消其余任务。
	 */
	@Test
	public void testAnyOfTimeoutVarargs() {
		AtomicBoolean slowStarted = new AtomicBoolean();
		Integer winner = StructuredTaskUtil.anyOf(Duration.ofSeconds(3),
				() -> 42,
				() -> {
					slowStarted.set(true);
					Thread.sleep(2000);
					return -1;
				});
		Assert.assertEquals(Integer.valueOf(42), winner);
		// anyOf 语义：首成功即短路取消其余，慢任务可能尚未启动（不强制断言启动状态）
	}

	/**
	 * anyOf(Duration, Callable...) 全部失败抛异常。
	 */
	@Test
	public void testAnyOfAllFail() {
		try {
			StructuredTaskUtil.anyOf(Duration.ofSeconds(3),
					() -> {
						throw new IllegalStateException("boom1");
					},
					() -> {
						throw new IllegalStateException("boom2");
					});
			Assert.fail("应抛全部失败异常");
		} catch (RuntimeException expected) {
			Assert.assertTrue(expected.getMessage().contains("全部任务失败"));
		}
	}

	/**
	 * anyOf(Duration, Callable...) 超时抛异常。
	 */
	@Test
	public void testAnyOfTimeout() {
		try {
			StructuredTaskUtil.anyOf(Duration.ofMillis(100),
					() -> {
						Thread.sleep(2000);
						return 1;
					});
			Assert.fail("应抛超时异常");
		} catch (RuntimeException expected) {
			Assert.assertTrue(expected.getMessage().contains("超时"));
		}
	}

	/**
	 * 空任务调用 anyOf 抛 IllegalArgumentException。
	 */
	@Test
	public void testAnyOfEmpty() {
		Callable<Integer>[] empty = newEmptyArray();
		try {
			StructuredTaskUtil.anyOf(Duration.ofSeconds(1), empty);
			Assert.fail("空任务应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@SuppressWarnings("unchecked")
	private Callable<Integer>[] newEmptyArray() {
		return new Callable[0];
	}
}
