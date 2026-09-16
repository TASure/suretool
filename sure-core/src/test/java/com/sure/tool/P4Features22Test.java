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

import java.util.List;

/**
 * P4（v0.2.0）第二十七批测试。
 */
public class P4Features22Test {

	@Test
	public void testBatch27() {
		// ObjectUtil.length
		Assert.assertEquals(0, com.sure.tool.util.ObjectUtil.length(null));
		Assert.assertEquals(3, com.sure.tool.util.ObjectUtil.length(new int[] {1, 2, 3}));
		Assert.assertEquals(5, com.sure.tool.util.ObjectUtil.length("hello"));
		Assert.assertEquals(2, com.sure.tool.util.ObjectUtil.length(List.of("a", "b")));
		Assert.assertEquals(1, com.sure.tool.util.ObjectUtil.length(42));

		// DateUtil.format LocalDate / LocalDateTime
		Assert.assertEquals("2026-09-16", com.sure.tool.date.DateUtil.format(java.time.LocalDate.of(2026, 9, 16)));
		Assert.assertEquals("2026-09-16 08:30:00", com.sure.tool.date.DateUtil.format(
				java.time.LocalDateTime.of(2026, 9, 16, 8, 30)));
		Assert.assertNull(com.sure.tool.date.DateUtil.format((java.time.LocalDate) null));

		// ConvertUtil.toBigInteger
		Assert.assertEquals(new java.math.BigInteger("123"), com.sure.tool.util.ConvertUtil.toBigInteger("123"));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toBigInteger(null));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toBigInteger("abc"));

		// StrUtil.replace 区间
		Assert.assertEquals("he---o", com.sure.tool.util.StrUtil.replace("hello", 2, 4, "---"));
		Assert.assertEquals("hello", com.sure.tool.util.StrUtil.replace("hello", 4, 2, "x"));

		// CollUtil.removeNull
		List<String> list = com.sure.tool.collection.CollUtil.removeNull(java.util.Arrays.asList("a", null, "b"));
		Assert.assertEquals(2, list.size());
		Assert.assertTrue(list.contains("a") && list.contains("b"));

		// ValidatorUtil.isBirthday
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isBirthday("20200229"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isBirthday("20210229"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isBirthday("2026-09-16"));

		// NumberUtil.pow
		Assert.assertEquals(8L, com.sure.tool.util.NumberUtil.pow(2, 3));
		Assert.assertEquals(1L, com.sure.tool.util.NumberUtil.pow(2, 0));
		Assert.assertEquals(81L, com.sure.tool.util.NumberUtil.pow(3, 4));
	}
}
