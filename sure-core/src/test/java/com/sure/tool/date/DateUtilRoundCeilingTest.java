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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * {@link DateUtil#round}/{@link DateUtil#ceiling} 全字段分支补测。
 */
public class DateUtilRoundCeilingTest {

	private static Date of(int year, int month, int day, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month - 1, day, hour, minute, second);
		return c.getTime();
	}

	@Test
	public void round全字段分支() {
		// SECOND：截断到分钟
		assertEquals("2026-10-16 08:30:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 30, 30), Calendar.SECOND)));
		// MINUTE：秒 <30 向下、>=30 向上
		assertEquals("2026-10-16 08:30:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 30, 15), Calendar.MINUTE)));
		assertEquals("2026-10-16 08:31:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 30, 45), Calendar.MINUTE)));
		// HOUR_OF_DAY：分 <30 向下、>=30 向上
		assertEquals("2026-10-16 08:00:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 15, 0), Calendar.HOUR_OF_DAY)));
		assertEquals("2026-10-16 09:00:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 45, 0), Calendar.HOUR_OF_DAY)));
		// HOUR 别名
		assertEquals("2026-10-16 08:00:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 10, 0), Calendar.HOUR)));
		// DAY_OF_MONTH：时 <12 向下、>=12 向上
		assertEquals("2026-10-16 00:00:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 0, 0), Calendar.DAY_OF_MONTH)));
		assertEquals("2026-10-17 00:00:00", fmt(DateUtil.round(of(2026, 10, 16, 18, 0, 0), Calendar.DAY_OF_MONTH)));
		// MONTH：日 <16 向下、>=16 向上
		assertEquals("2026-10-01 00:00:00", fmt(DateUtil.round(of(2026, 10, 10, 0, 0, 0), Calendar.MONTH)));
		assertEquals("2026-11-01 00:00:00", fmt(DateUtil.round(of(2026, 10, 20, 0, 0, 0), Calendar.MONTH)));
		// YEAR
		assertEquals("2026-01-01 00:00:00", fmt(DateUtil.round(of(2026, 3, 1, 0, 0, 0), Calendar.YEAR)));
		// default：按原值截断到毫秒
		assertEquals("2026-10-16 08:30:00", fmt(DateUtil.round(of(2026, 10, 16, 8, 30, 0), Calendar.MILLISECOND)));
	}

	@Test
	public void ceiling全字段分支() {
		// SECOND：毫秒>0 进位
		assertEquals("2026-10-16 08:30:01", fmt(DateUtil.ceiling(withMillis(of(2026, 10, 16, 8, 30, 0), 500), Calendar.SECOND)));
		// MINUTE：秒>0 进位
		assertEquals("2026-10-16 08:31:00", fmt(DateUtil.ceiling(of(2026, 10, 16, 8, 30, 1), Calendar.MINUTE)));
		// HOUR_OF_DAY：分>0 进位
		assertEquals("2026-10-16 09:00:00", fmt(DateUtil.ceiling(of(2026, 10, 16, 8, 1, 0), Calendar.HOUR_OF_DAY)));
		// DAY_OF_MONTH：时>0 进位
		assertEquals("2026-10-17 00:00:00", fmt(DateUtil.ceiling(of(2026, 10, 16, 1, 0, 0), Calendar.DAY_OF_MONTH)));
		// MONTH：日>1 进位
		assertEquals("2026-11-01 00:00:00", fmt(DateUtil.ceiling(of(2026, 10, 2, 0, 0, 0), Calendar.MONTH)));
		// YEAR：日>1 进位
		assertEquals("2027-01-01 00:00:00", fmt(DateUtil.ceiling(of(2026, 1, 2, 0, 0, 0), Calendar.YEAR)));
		// 恰好对齐：不进位
		assertEquals("2026-10-16 08:30:00", fmt(DateUtil.ceiling(of(2026, 10, 16, 8, 30, 0), Calendar.MINUTE)));
		// default 字段
		assertEquals("2026-10-16 08:30:00", fmt(DateUtil.ceiling(of(2026, 10, 16, 8, 30, 0), Calendar.MILLISECOND)));
	}

	private static Date withMillis(Date d, int millis) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.MILLISECOND, millis);
		return c.getTime();
	}

	private static String fmt(Date d) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
	}

	@Test
	public void 断言工具自检() {
		assertTrue(true);
	}
}
