package com.sure.tool;

import com.sure.tool.date.DateUnit;
import com.sure.tool.date.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * DateUtil / DateUnit 单元测试。
 */
public class DateUtilTest {

	@Test
	public void testFormatAndParse() {
		Date date = DateUtil.parse("2026-09-09 16:30:00");
		Assert.assertEquals("2026-09-09 16:30:00", DateUtil.format(date));
		Assert.assertEquals("2026-09-09", DateUtil.formatDate(date));
		Assert.assertEquals("16:30:00", DateUtil.formatTime(date));
		Assert.assertEquals("2026-09-09", DateUtil.format(date, "yyyy-MM-dd"));
	}

	@Test
	public void testParseAuto() {
		Date d1 = DateUtil.parse("2026-09-09");
		Assert.assertEquals(2026, DateUtil.year(d1));
		Assert.assertEquals(9, DateUtil.month(d1));
		Assert.assertEquals(9, DateUtil.day(d1));
		Date d2 = DateUtil.parse("2026-09-09T16:30:00");
		Assert.assertEquals("2026-09-09 16:30:00", DateUtil.format(d2));
		Assert.assertNull(DateUtil.parse("  "));
	}

	@Test
	public void testOffset() {
		Date date = DateUtil.parse("2026-09-09 10:00:00");
		Assert.assertEquals("2026-09-10", DateUtil.formatDate(DateUtil.offsetDay(date, 1)));
		Assert.assertEquals("2026-09-08", DateUtil.formatDate(DateUtil.offsetDay(date, -1)));
		Assert.assertEquals("2026-09-09 11:00:00", DateUtil.format(DateUtil.offsetHour(date, 1)));
		Assert.assertEquals("2026-09-09 10:01:00", DateUtil.format(DateUtil.offsetMinute(date, 1)));
	}

	@Test
	public void testDayBounds() {
		Date date = DateUtil.parse("2026-09-09 15:30:00");
		Assert.assertEquals("2026-09-09 00:00:00", DateUtil.format(DateUtil.beginOfDay(date)));
		Assert.assertEquals("2026-09-09 23:59:59", DateUtil.format(DateUtil.endOfDay(date)));
		Assert.assertEquals("2026-09-01 00:00:00", DateUtil.format(DateUtil.beginOfMonth(date)));
		Assert.assertEquals("2026-09-30 23:59:59", DateUtil.format(DateUtil.endOfMonth(date)));
	}

	@Test
	public void testBetween() {
		Date start = DateUtil.parse("2026-09-01 00:00:00");
		Date end = DateUtil.parse("2026-09-10 00:00:00");
		Assert.assertEquals(9, DateUtil.between(start, end, DateUnit.DAY));
		Assert.assertEquals(216, DateUtil.between(start, end, DateUnit.HOUR));
		Assert.assertEquals(9 * 24 * 60 * 60 * 1000L, DateUtil.between(start, end, DateUnit.MS));
	}

	@Test
	public void testAge() {
		Date birth = DateUtil.parse("2000-05-20");
		int age = DateUtil.age(birth);
		// 2026 年 9 月，2000-05-20 出生应为 26 岁
		Assert.assertEquals(26, age);
	}

	@Test
	public void testIsSameDayAndLeapYear() {
		Date d1 = DateUtil.parse("2026-09-09 08:00:00");
		Date d2 = DateUtil.parse("2026-09-09 23:00:00");
		Assert.assertTrue(DateUtil.isSameDay(d1, d2));
		Assert.assertTrue(DateUtil.isLeapYear(2024));
		Assert.assertFalse(DateUtil.isLeapYear(2026));
		Assert.assertTrue(DateUtil.isLeapYear(2000));
		Assert.assertFalse(DateUtil.isLeapYear(1900));
	}

	@Test
	public void testCalendar() {
		Date date = DateUtil.parse("2026-09-09 10:00:00");
		Calendar calendar = DateUtil.toCalendar(date);
		Assert.assertEquals(2026, calendar.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.WEDNESDAY, DateUtil.dayOfWeek(date));
	}
}
