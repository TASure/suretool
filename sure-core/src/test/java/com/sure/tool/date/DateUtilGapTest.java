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

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * DateUtil 覆盖率补测：null 守卫、异常分支、rangeToList 各时间单位、年龄递减分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class DateUtilGapTest {

	@Test
	public void testFormatNull() {
		Assert.assertNull(DateUtil.format((Date) null));
		Assert.assertNull(DateUtil.format(null, "yyyy-MM-dd"));
		Assert.assertNull(DateUtil.format((java.time.LocalDateTime) null));
	}

	@Test
	public void testParseBranches() {
		Assert.assertNull(DateUtil.parse(null));
		Assert.assertNotNull(DateUtil.parse("12:34:56"));
		Assert.assertNotNull(DateUtil.parse("2026-01-02 03:04:05"));
		Assert.assertNotNull(DateUtil.parse("2026-01-02T03:04:05"));
		try {
			DateUtil.parse("weird");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testAge() {
		Assert.assertEquals(0, DateUtil.age(null));
		Assert.assertEquals(0, DateUtil.age(null, new Date()));
		Assert.assertEquals(0, DateUtil.getAge(null));
		// 出生日设为今天的下个月同日 -> 年龄递减分支
		Calendar birth = Calendar.getInstance();
		birth.add(Calendar.YEAR, -30);
		birth.add(Calendar.MONTH, 1);
		Assert.assertTrue(DateUtil.age(birth.getTime()) >= 28);
	}

	@Test
	public void testDaysBetweenAndOverlap() {
		try {
			DateUtil.daysBetween(null, new Date());
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		long d = DateUtil.daysBetween(DateUtil.parse("2026-01-01"), DateUtil.parse("2026-01-11"));
		Assert.assertEquals(10L, d);
		Assert.assertTrue(DateUtil.isOverlap(new Date(0), new Date(1000), new Date(500), new Date(1500)));
		Assert.assertFalse(DateUtil.isOverlap(new Date(0), new Date(100), new Date(200), new Date(300)));
	}

	@Test
	public void testRangeToListUnits() {
		Date start = new Date(1000L);
		Date end = new Date(1000L);
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.MS).size());
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.SECOND).size());
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.MINUTE).size());
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.HOUR).size());
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.DAY).size());
		Assert.assertEquals(1, DateUtil.rangeToList(start, end, DateUnit.WEEK).size());
		Assert.assertTrue(DateUtil.rangeToList(new Date(5000L), new Date(1000L), DateUnit.DAY).isEmpty());
	}

	@Test
	public void testMiscNullGuards() {
		Assert.assertFalse(DateUtil.isSameYear(null, new Date()));
		Assert.assertEquals("一月", DateUtil.getMonthName(1));
		try {
			DateUtil.getMonthName(13);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertNull(DateUtil.getMonthName(null));
		Assert.assertNull(DateUtil.offsetQuarter(null, 1));
		Assert.assertNull(DateUtil.endOfMinute(null));
		Assert.assertNotNull(DateUtil.beginOfMinute(new Date()));
	}
}
