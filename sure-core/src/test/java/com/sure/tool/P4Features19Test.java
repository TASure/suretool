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

import java.math.BigDecimal;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十四批测试。
 */
public class P4Features19Test {

	@Test
	public void testBatch24() {
		// NumberUtil.toFixed
		Assert.assertEquals("3.14", com.sure.tool.util.NumberUtil.toFixed(3.14159, 2));
		Assert.assertEquals("2.68", com.sure.tool.util.NumberUtil.toFixed(2.675, 2));
		Assert.assertEquals("1.000", com.sure.tool.util.NumberUtil.toFixed(new BigDecimal("1"), 3));

		// StrUtil.firstNonBlank / concat
		Assert.assertEquals("abc", StrUtil.firstNonBlank("", " ", "abc"));
		Assert.assertEquals("x", StrUtil.firstNonBlank("x", "y"));
		Assert.assertNull(StrUtil.firstNonBlank("", " "));
		Assert.assertEquals("abc", StrUtil.concat("a", "b", "c"));
		Assert.assertNull(StrUtil.concat((String[]) null));

		// DateUtil.getMonthName
		Assert.assertEquals("一月", com.sure.tool.date.DateUtil.getMonthName(1));
		Assert.assertEquals("十二月", com.sure.tool.date.DateUtil.getMonthName(12));
		Assert.assertEquals("九月", com.sure.tool.date.DateUtil.getMonthName(
				com.sure.tool.date.DateUtil.parse("2026-09-16")));

		// ArrayUtil.nullToEmpty
		String[] empty = com.sure.tool.util.ArrayUtil.nullToEmpty((String[]) null, String.class);
		Assert.assertEquals(0, empty.length);
		Assert.assertEquals(1, com.sure.tool.util.ArrayUtil.nullToEmpty(new String[] {"a"}, String.class).length);
	}
}
