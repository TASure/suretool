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

/**
 * P4（v0.2.0）第二十八批测试。
 */
public class P4Features23Test {

	@Test
	public void testBatch28() {
		// StrUtil.containsAnyIgnoreCase
		Assert.assertTrue(com.sure.tool.util.StrUtil.containsAnyIgnoreCase("Hello World", "WORLD", "foo"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.containsAnyIgnoreCase("Hello", "xyz"));

		// DateUtil.beginOfMinute / endOfMinute
		java.util.Date d = com.sure.tool.date.DateUtil.parse("2026-09-16 10:30:45");
		Assert.assertEquals("2026-09-16 10:30:00",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.beginOfMinute(d)));
		Assert.assertEquals("2026-09-16 10:30:59",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.endOfMinute(d)));
		Assert.assertNull(com.sure.tool.date.DateUtil.beginOfMinute(null));

		// ArrayUtil.resize
		String[] arr = {"a", "b", "c"};
		Assert.assertEquals(5, com.sure.tool.util.ArrayUtil.resize(arr, 5).length);
		Assert.assertEquals("a", com.sure.tool.util.ArrayUtil.resize(arr, 5)[0]);
		Assert.assertNull(com.sure.tool.util.ArrayUtil.resize(arr, 5)[4]);
		Assert.assertEquals(2, com.sure.tool.util.ArrayUtil.resize(arr, 2).length);

		// ConvertUtil.toLocalDate / toLocalDateTime
		Assert.assertEquals(java.time.LocalDate.of(2026, 9, 16),
				com.sure.tool.util.ConvertUtil.toLocalDate("2026-09-16"));
		Assert.assertEquals(java.time.LocalDateTime.of(2026, 9, 16, 10, 30, 45),
				com.sure.tool.util.ConvertUtil.toLocalDateTime("2026-09-16 10:30:45"));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toLocalDate("bad"));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toLocalDateTime(null));

		// ValidatorUtil.isGeneralWithChinese
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isGeneralWithChinese("汉字abc_123"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isGeneralWithChinese("abc-123"));
	}
}
