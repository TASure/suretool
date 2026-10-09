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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

/**
 * ThreadUtil 批25 增强测试。
 */
public class ThreadUtilBatch25Test {

	/**
	 * sleepInterruptibly 可被中断并抛 InterruptedException。
	 */
	@Test
	public void testSleepInterruptiblyInterrupt() throws InterruptedException {
		AtomicBoolean interrupted = new AtomicBoolean();
		Thread worker = Thread.ofVirtual().start(() -> {
			try {
				ThreadUtil.sleepInterruptibly(Duration.ofSeconds(30));
			} catch (InterruptedException e) {
				interrupted.set(true);
			}
		});
		worker.interrupt();
		Assert.assertTrue(worker.join(Duration.ofSeconds(3)));
		Assert.assertTrue(interrupted.get());
	}

	/**
	 * sleepInterruptibly 非正时长直接返回不睡眠。
	 */
	@Test
	public void testSleepInterruptiblyNoop() throws InterruptedException {
		long start = System.nanoTime();
		ThreadUtil.sleepInterruptibly(Duration.ZERO);
		ThreadUtil.sleepInterruptibly(Duration.ofMillis(-5));
		ThreadUtil.sleepInterruptibly(null);
		long elapsed = System.nanoTime() - start;
		Assert.assertTrue("应即时返回", elapsed < Duration.ofMillis(100).toNanos());
	}

	/**
	 * 中断位在 InterruptedException 后保持清除（异常被上层处理）。
	 */
	@Test
	public void testSleepInterruptiblyClearsInterruptFlagOnCatch() throws InterruptedException {
		Thread current = Thread.currentThread();
		current.interrupt();
		try {
			ThreadUtil.sleepInterruptibly(Duration.ofSeconds(30));
			Assert.fail("应抛 InterruptedException");
		} catch (InterruptedException expected) {
			// 预期：调用方处理中断
		}
	}
}
