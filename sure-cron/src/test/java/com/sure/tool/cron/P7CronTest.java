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
package com.sure.tool.cron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * 批16（v1.10.0）：七段式 Cron（可选年份）+ 虚拟线程调度测试。
 *
 * @author suretool
 * @since 1.10.0
 */
public class P7CronTest {

	@Test
	public void testSixSegmentCompatibility() {
		// 6 段表达式保持兼容
		assertTrue(CronPattern.isValid("0 * * * * ?"));
		assertFalse(CronPattern.isValid("0 * * * * ? extra"));
	}

	@Test
	public void testSevenSegmentWithYear() {
		// 7 段表达式：秒 分 时 日 月 周 年
		assertTrue(CronPattern.isValid("0 0 12 1 1 ? 2027"));
		assertTrue(CronPattern.isValid("0 0 12 1 1 ? 2026-2028"));
		assertFalse(CronPattern.isValid("0 0 12 1 1 ? abc"));
	}

	@Test
	public void testYearMatch() {
		CronPattern pattern = CronPattern.of("0 0 12 1 1 ? 2027");
		Date in2027 = new Date(2027 - 1900, 0, 1, 12, 0, 0);
		assertTrue("2027-01-01 12:00 应匹配", pattern.match(in2027));
		Date in2026 = new Date(2026 - 1900, 0, 1, 12, 0, 0);
		assertFalse("2026 年不应匹配", pattern.match(in2026));
	}

	@Test
	public void testNextTimeAfterYearBoundary() {
		Date base = new Date(2026 - 1900, 11, 1, 0, 0, 0); // 2026-12-01
		Date next = CronUtil.nextTimeAfter("0 0 0 1 1 ? 2027", base);
		assertNotNull(next);
		assertEquals(2027, next.getYear() + 1900);
		assertEquals(0, next.getMonth());
		assertEquals(1, next.getDate());
	}

	@Test
	public void testYearAnyWildcard() {
		CronPattern pattern = CronPattern.of("0 0 0 1 1 ? *");
		Date any = new Date(2030 - 1900, 0, 1, 0, 0, 0);
		assertTrue("年段 * 表示任意年份", pattern.match(any));
	}

	@Test
	public void testVirtualThreadExecution() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(3);
		AtomicInteger executed = new AtomicInteger();
		CronUtil.schedule("*/1 * * * * ?", () -> {
			executed.incrementAndGet();
			latch.countDown();
		});
		CronUtil.start();
		try {
			// 每秒匹配一次，3 秒内应至少执行 3 次
			assertTrue("虚拟线程调度应在超时前执行任务", latch.await(10, TimeUnit.SECONDS));
			assertTrue(executed.get() >= 3);
		} finally {
			CronUtil.stop();
		}
	}
}
