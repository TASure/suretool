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

import java.util.Map;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十六批测试。
 */
public class P4Features21Test {

	@Test
	public void testBatch26() {
		// StrUtil.isAllNotBlank / isAllEmpty
		Assert.assertTrue(StrUtil.isAllNotBlank("a", "b", "c"));
		Assert.assertFalse(StrUtil.isAllNotBlank("a", "", "c"));
		Assert.assertTrue(StrUtil.isAllEmpty("", ""));
		Assert.assertFalse(StrUtil.isAllEmpty("", " "));
		Assert.assertFalse(StrUtil.isAllEmpty("", "a"));

		// DateUtil.getWeekOfYear
		Assert.assertTrue(com.sure.tool.date.DateUtil.getWeekOfYear(
				com.sure.tool.date.DateUtil.parse("2026-01-01")) >= 1);
		Assert.assertTrue(com.sure.tool.date.DateUtil.getWeekOfYear(
				com.sure.tool.date.DateUtil.parse("2026-12-31")) >= 50);
		Assert.assertEquals(0, com.sure.tool.date.DateUtil.getWeekOfYear(null));

		// MapUtil.merge
		Map<String, Integer> m1 = Map.of("a", 1, "b", 2);
		Map<String, Integer> m2 = Map.of("b", 3, "c", 4);
		Map<String, Integer> merged = com.sure.tool.collection.MapUtil.merge(m1, m2);
		Assert.assertEquals(Integer.valueOf(1), merged.get("a"));
		Assert.assertEquals(Integer.valueOf(3), merged.get("b"));
		Assert.assertEquals(Integer.valueOf(4), merged.get("c"));

		// ValidatorUtil.isDecimal
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isDecimal("3.14"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isDecimal("-0.5"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isDecimal("abc"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isDecimal(null));

		// NumberUtil.floor / ceil
		Assert.assertEquals(3L, com.sure.tool.util.NumberUtil.floor(3.7));
		Assert.assertEquals(4L, com.sure.tool.util.NumberUtil.ceil(3.2));
		Assert.assertEquals(-4L, com.sure.tool.util.NumberUtil.floor(-3.2));
	}
}
