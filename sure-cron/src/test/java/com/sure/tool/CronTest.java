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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.sure.tool.date.DateUtil;
import com.sure.tool.thread.ThreadUtil;

/**
 * CronPattern / CronUtil 测试。
 */
public class CronTest {

	private static Date at(String time) {
		return DateUtil.parse(time);
	}

	private static String fmt(Date date) {
		return DateUtil.format(date);
	}

	@Test
	public void testInvalidExpressions() {
		String[] invalid = {
				"", "0 0 * * *", "0 0 24 * * ?", "0 0 0 32 * ?", "0 0 0 * 13 ?",
				"? * * * * ?", "0 0 0 * * ? junk", "0 0 0 5-1 * ?", "0 0 0 */0 * ?",
				"abc def ghi jkl mno pqr"
		};
		for (String cron : invalid) {
			try {
				new CronPattern(cron);
				fail("应解析失败: " + cron);
			} catch (IllegalArgumentException e) {
				// 预期
			}
			assertFalse(CronPattern.isValid(cron));
		}
	}

	@Test
	public void testValidExpressions() {
		assertTrue(CronPattern.isValid("0 0 12 * * ?"));
		assertTrue(CronPattern.isValid("*/5 * * * * ?"));
		assertTrue(CronPattern.isValid("0 0 9 ? * MON"));
	}

	@Test
	public void testMatch() {
		CronPattern noon = CronPattern.of("0 0 12 * * ?");
		assertTrue(noon.match(at("2026-01-15 12:00:00")));
		assertFalse(noon.match(at("2026-01-15 12:00:01")));
		assertFalse(noon.match(at("2026-01-15 11:59:59")));

		CronPattern every5 = CronPattern.of("*/5 * * * * ?");
		assertTrue(every5.match(at("2026-01-15 12:00:05")));
		assertTrue(every5.match(at("2026-01-15 12:00:10")));
		assertFalse(every5.match(at("2026-01-15 12:00:03")));
	}

	@Test
	public void testNextEvery5Seconds() {
		CronPattern every5 = CronPattern.of("*/5 * * * * ?");
		assertEquals("2026-01-15 12:00:05", fmt(every5.getNextTimeAfter(at("2026-01-15 12:00:03"))));
		assertEquals("2026-01-15 12:00:10", fmt(every5.getNextTimeAfter(at("2026-01-15 12:00:05"))));
	}

	@Test
	public void testNextEvery15Minutes() {
		CronPattern every15 = CronPattern.of("0 */15 * * * ?");
		assertEquals("2026-01-15 10:15:00", fmt(every15.getNextTimeAfter(at("2026-01-15 10:07:30"))));
		assertEquals("2026-01-15 10:30:00", fmt(every15.getNextTimeAfter(at("2026-01-15 10:15:00"))));
	}

	@Test
	public void testNextHourlyAt30() {
		CronPattern at30 = CronPattern.of("0 30 * * * ?");
		assertEquals("2026-01-15 10:30:00", fmt(at30.getNextTimeAfter(at("2026-01-15 10:15:00"))));
		assertEquals("2026-01-15 11:30:00", fmt(at30.getNextTimeAfter(at("2026-01-15 10:31:00"))));
	}

	@Test
	public void testNextDailyNoon() {
		CronPattern noon = CronPattern.of("0 0 12 * * ?");
		assertEquals("2026-01-16 12:00:00", fmt(noon.getNextTimeAfter(at("2026-01-15 13:00:00"))));
		assertEquals("2026-01-15 12:00:00", fmt(noon.getNextTimeAfter(at("2026-01-15 11:59:00"))));
	}

	@Test
	public void testNextMonthFirstDay() {
		CronPattern firstDay = CronPattern.of("0 0 0 1 * ?");
		assertEquals("2026-02-01 00:00:00", fmt(firstDay.getNextTimeAfter(at("2026-01-15 00:00:00"))));
		// 跨年
		assertEquals("2026-01-01 00:00:00", fmt(firstDay.getNextTimeAfter(at("2025-12-31 23:59:59"))));
	}

	@Test
	public void testNextYearFirstDay() {
		CronPattern jan1 = CronPattern.of("0 0 0 1 1 ?");
		assertEquals("2027-01-01 00:00:00", fmt(jan1.getNextTimeAfter(at("2026-06-15 12:00:00"))));
	}

	@Test
	public void testNextSpecifiedDay() {
		CronPattern day15 = CronPattern.of("0 0 12 15 * ?");
		assertEquals("2026-01-15 12:00:00", fmt(day15.getNextTimeAfter(at("2026-01-10 00:00:00"))));
		assertEquals("2026-02-15 12:00:00", fmt(day15.getNextTimeAfter(at("2026-01-15 12:00:01"))));
	}

	@Test
	public void testNextWeekday() {
		// 每周一 9 点（Calendar.MONDAY = 2）
		CronPattern monday9 = CronPattern.of("0 0 9 ? * 2");
		// 2026-01-13 是周二
		assertEquals("2026-01-19 09:00:00", fmt(monday9.getNextTimeAfter(at("2026-01-13 10:00:00"))));
		// 2026-01-12 是周一，当天 8 点 → 当天 9 点
		assertEquals("2026-01-12 09:00:00", fmt(monday9.getNextTimeAfter(at("2026-01-12 08:00:00"))));
	}

	@Test
	public void testNextDayOrWeek() {
		// 每月 15 日 或 每周一 12 点（或关系）
		CronPattern or = CronPattern.of("0 0 12 15 * 2");
		// 2026-01-10 周六，最近的是 2026-01-12 周一
		assertEquals("2026-01-12 12:00:00", fmt(or.getNextTimeAfter(at("2026-01-10 00:00:00"))));
		// 2026-01-15 是周四，12 点匹配（日命中）
		assertEquals("2026-01-15 12:00:00", fmt(or.getNextTimeAfter(at("2026-01-14 12:00:00"))));
	}

	@Test
	public void testNextImpossible() {
		// 2 月 30 日不存在 → 无下一次
		CronPattern impossible = CronPattern.of("0 0 0 30 2 ?");
		assertNull(impossible.getNextTimeAfter(at("2026-01-01 00:00:00")));
	}

	@Test
	public void testCronUtilSchedule() throws Exception {
		AtomicInteger counter = new AtomicInteger();
		CronUtil.schedule("*/1 * * * * ?", counter::incrementAndGet);
		assertEquals(1, CronUtil.getJobCount());

		CronUtil.start();
		ThreadUtil.sleep(2500);
		CronUtil.stop();

		assertTrue("每秒任务应在 2.5 秒内至少执行 2 次，实际 " + counter.get(), counter.get() >= 2);
		assertEquals(0, CronUtil.getJobCount());
	}

	@Test
	public void testCronUtilIsValid() {
		assertTrue(CronUtil.isValid("0 0 12 * * ?"));
		assertFalse(CronUtil.isValid("0 0 99 * * ?"));
		assertNotNull(CronUtil.parse("0 0 12 * * ?"));
	}
}