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
package com.sure.tool.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 40：DateUtil java.time 互转/解析 + isSameWeek + truncate/round/ceiling 测试。
 *
 * @author suretool
 * @since 1.19.0
 */
public class DateUtilBatch40Test {

	@Test
	public void testToLocalDate() {
		Date d = DateUtil.parse("2026-10-15", "yyyy-MM-dd");
		Assert.assertEquals(LocalDate.of(2026, 10, 15), DateUtil.toLocalDate(d));
		Assert.assertNull(DateUtil.toLocalDate(null));
	}

	@Test
	public void testToLocalDateTime() {
		Date d = DateUtil.parse("2026-10-15 08:30:45", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30, 45), DateUtil.toLocalDateTime(d));
		Assert.assertNull(DateUtil.toLocalDateTime(null));
	}

	@Test
	public void testToDateFromLocalDate() {
		Date d = DateUtil.toDate(LocalDate.of(2026, 10, 15));
		Assert.assertEquals("2026-10-15", DateUtil.format(d, "yyyy-MM-dd"));
		Assert.assertNull(DateUtil.toDate((LocalDate) null));
	}

	@Test
	public void testToDateFromLocalDateTime() {
		Date d = DateUtil.toDate(LocalDateTime.of(2026, 10, 15, 8, 30, 45));
		Assert.assertEquals("2026-10-15 08:30:45", DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));
		Assert.assertNull(DateUtil.toDate((LocalDateTime) null));
	}

	@Test
	public void testParseLocalDate() {
		Assert.assertEquals(LocalDate.of(2026, 10, 15), DateUtil.parseLocalDate("2026-10-15"));
		Assert.assertNull(DateUtil.parseLocalDate(null));
		Assert.assertNull(DateUtil.parseLocalDate("  "));
	}

	@Test
	public void testParseLocalDateTimeAuto() {
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30, 45),
				DateUtil.parseLocalDateTime("2026-10-15 08:30:45"));
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30),
				DateUtil.parseLocalDateTime("2026-10-15 08:30"));
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30, 45, 123000000),
				DateUtil.parseLocalDateTime("2026-10-15 08:30:45.123"));
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
				DateUtil.parseLocalDateTime("2026-10-15"));
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30, 45),
				DateUtil.parseLocalDateTime("2026-10-15T08:30:45"));
		Assert.assertNull(DateUtil.parseLocalDateTime(null));
		Assert.assertNull(DateUtil.parseLocalDateTime(" "));
	}

	@Test
	public void testParseLocalDateTimePattern() {
		Assert.assertEquals(LocalDateTime.of(2026, 10, 15, 8, 30),
				DateUtil.parseLocalDateTime("2026/10/15 08:30", "yyyy/MM/dd HH:mm"));
		Assert.assertNull(DateUtil.parseLocalDateTime(null, "yyyy-MM-dd"));
	}

	@Test
	public void testIsSameWeek() {
		Date monday = DateUtil.parse("2026-10-12", "yyyy-MM-dd");
		Date friday = DateUtil.parse("2026-10-16", "yyyy-MM-dd");
		Date sunday = DateUtil.parse("2026-10-18", "yyyy-MM-dd");
		Assert.assertTrue(DateUtil.isSameWeek(monday, friday));
		Assert.assertFalse(DateUtil.isSameWeek(friday, sunday));
		Assert.assertFalse(DateUtil.isSameWeek(monday, null));
		Assert.assertFalse(DateUtil.isSameWeek(null, monday));
	}

	@Test
	public void testTruncate() {
		Date d = DateUtil.parse("2026-10-15 08:30:45.123", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-15 00:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.DAY_OF_MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-10-15 08:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.HOUR_OF_DAY), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-10-01 00:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-01-01 00:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertNull(DateUtil.truncate(null, Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testRound() {
		Date d1 = DateUtil.parse("2026-10-15 08:30:29.999", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-15 08:30:00.000",
				DateUtil.format(DateUtil.round(d1, Calendar.MINUTE), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date d2 = DateUtil.parse("2026-10-15 08:30:30.000", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-15 08:31:00.000",
				DateUtil.format(DateUtil.round(d2, Calendar.MINUTE), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date d3 = DateUtil.parse("2026-10-15 12:00:00", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-10-16 00:00:00.000",
				DateUtil.format(DateUtil.round(d3, Calendar.DAY_OF_MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date d4 = DateUtil.parse("2026-10-15 11:59:59", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-10-15 00:00:00.000",
				DateUtil.format(DateUtil.round(d4, Calendar.DAY_OF_MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date d5 = DateUtil.parse("2026-10-15 08:00:00", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-10-15 08:00:00.000",
				DateUtil.format(DateUtil.round(d5, Calendar.SECOND), "yyyy-MM-dd HH:mm:ss.SSS"));
		// HOUR 进位（分钟 >= 30）
		Date h1 = DateUtil.parse("2026-10-15 08:29:59", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-10-15 08:00:00.000",
				DateUtil.format(DateUtil.round(h1, Calendar.HOUR_OF_DAY), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date h2 = DateUtil.parse("2026-10-15 08:30:00", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-10-15 09:00:00.000",
				DateUtil.format(DateUtil.round(h2, Calendar.HOUR_OF_DAY), "yyyy-MM-dd HH:mm:ss.SSS"));
		// MONTH / YEAR 进位
		Date m1 = DateUtil.parse("2026-10-15", "yyyy-MM-dd");
		Assert.assertEquals("2026-10-01 00:00:00.000",
				DateUtil.format(DateUtil.round(m1, Calendar.MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date m2 = DateUtil.parse("2026-10-16", "yyyy-MM-dd");
		Assert.assertEquals("2026-11-01 00:00:00.000",
				DateUtil.format(DateUtil.round(m2, Calendar.MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date y1 = DateUtil.parse("2026-07-01", "yyyy-MM-dd");
		Assert.assertEquals("2026-01-01 00:00:00.000",
				DateUtil.format(DateUtil.round(y1, Calendar.YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		Date y2 = DateUtil.parse("2026-07-02", "yyyy-MM-dd");
		Assert.assertEquals("2027-01-01 00:00:00.000",
				DateUtil.format(DateUtil.round(y2, Calendar.YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertNull(DateUtil.round(null, Calendar.MINUTE));
	}

	@Test
	public void testCeiling() {
		Date d1 = DateUtil.parse("2026-10-15 08:30:45.123", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-16 00:00:00.000",
				DateUtil.format(DateUtil.ceiling(d1, Calendar.DAY_OF_MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-10-15 09:00:00.000",
				DateUtil.format(DateUtil.ceiling(d1, Calendar.HOUR_OF_DAY), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-10-15 08:31:00.000",
				DateUtil.format(DateUtil.ceiling(d1, Calendar.MINUTE), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-10-15 08:30:46.000",
				DateUtil.format(DateUtil.ceiling(d1, Calendar.SECOND), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-11-01 00:00:00.000",
				DateUtil.format(DateUtil.ceiling(d1, Calendar.MONTH), "yyyy-MM-dd HH:mm:ss.SSS"));
		// 恰好对齐字段时不进位
		Date exact = DateUtil.parse("2026-10-15 08:00:00.000", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-15 08:00:00.000",
				DateUtil.format(DateUtil.ceiling(exact, Calendar.HOUR_OF_DAY), "yyyy-MM-dd HH:mm:ss.SSS"));
		// WEEK 分支：走 truncate 周首且不额外进位（default 分支）
		Date w = DateUtil.parse("2026-10-15", "yyyy-MM-dd");
		Assert.assertEquals("2026-10-12 00:00:00.000",
				DateUtil.format(DateUtil.ceiling(w, Calendar.WEEK_OF_YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		// YEAR 进位
		Date y = DateUtil.parse("2026-07-01", "yyyy-MM-dd");
		Assert.assertEquals("2027-01-01 00:00:00.000",
				DateUtil.format(DateUtil.ceiling(y, Calendar.YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertNull(DateUtil.ceiling(null, Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testTruncateWeekAndDefault() {
		Date d = DateUtil.parse("2026-10-15", "yyyy-MM-dd");
		// WEEK_OF_YEAR 截断到周一
		Assert.assertEquals("2026-10-12 00:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.WEEK_OF_YEAR), "yyyy-MM-dd HH:mm:ss.SSS"));
		// SECOND 截断保留秒
		Date s = DateUtil.parse("2026-10-15 08:30:45.123", "yyyy-MM-dd HH:mm:ss.SSS");
		Assert.assertEquals("2026-10-15 08:30:45.000",
				DateUtil.format(DateUtil.truncate(s, Calendar.SECOND), "yyyy-MM-dd HH:mm:ss.SSS"));
		// 未知字段走 default 原样返回
		Assert.assertEquals("2026-10-15 00:00:00.000",
				DateUtil.format(DateUtil.truncate(d, Calendar.DATE), "yyyy-MM-dd HH:mm:ss.SSS"));
	}
}
