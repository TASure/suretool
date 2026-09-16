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
package com.sure.tool;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十五批测试。
 */
public class P4Features20Test {

	@Test
	public void testBatch25() {
		// StrUtil.swapCase
		Assert.assertEquals("hELLO", StrUtil.swapCase("Hello"));
		Assert.assertEquals("A1B2", StrUtil.swapCase("a1b2"));
		Assert.assertNull(StrUtil.swapCase(null));

		// NumberUtil.parseNumber
		Assert.assertEquals(Integer.valueOf(42), com.sure.tool.util.NumberUtil.parseNumber("42"));
		Assert.assertEquals(Long.valueOf(9999999999L), com.sure.tool.util.NumberUtil.parseNumber("9999999999"));
		Assert.assertEquals(new java.math.BigDecimal("3.14"), com.sure.tool.util.NumberUtil.parseNumber("3.14"));
		Assert.assertNull(com.sure.tool.util.NumberUtil.parseNumber("abc"));
		Assert.assertNull(com.sure.tool.util.NumberUtil.parseNumber(null));

		// DateUtil.offsetQuarter / 四件套
		java.util.Date q1 = com.sure.tool.date.DateUtil.parse("2026-01-15");
		Assert.assertEquals("2026-04-15 00:00:00",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.offsetQuarter(q1, 1),
						"yyyy-MM-dd HH:mm:ss"));
		Assert.assertEquals("2025-10-15 00:00:00",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.offsetQuarter(q1, -1),
						"yyyy-MM-dd HH:mm:ss"));
		java.util.Date d = com.sure.tool.date.DateUtil.parse("2026-09-16 14:30:00");
		Assert.assertEquals(2026, com.sure.tool.date.DateUtil.getYear(d));
		Assert.assertEquals(9, com.sure.tool.date.DateUtil.getMonth(d));
		Assert.assertEquals(16, com.sure.tool.date.DateUtil.getDayOfMonth(d));
		Assert.assertEquals(14, com.sure.tool.date.DateUtil.getHour(d));

		// ValidatorUtil.isTime
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isTime("23:59:59"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isTime("08:05:01"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isTime("25:00:00"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isTime("ab:cd:ef"));
	}
}
