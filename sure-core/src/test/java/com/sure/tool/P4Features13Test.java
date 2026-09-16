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
 * P4（v0.2.0）第十八批测试。
 */
public class P4Features13Test {

	@Test
	public void testBatch18() {
		// ConvertUtil.toDoubleArray / toBooleanArray
		Assert.assertArrayEquals(new double[] {1.5, 2.5}, com.sure.tool.util.ConvertUtil.toDoubleArray("1.5,2.5"), 1e-9);
		Assert.assertArrayEquals(new boolean[] {true, false}, com.sure.tool.util.ConvertUtil.toBooleanArray("true,false"));
		Assert.assertArrayEquals(new double[0], com.sure.tool.util.ConvertUtil.toDoubleArray(null), 1e-9);

		// ValidatorUtil.isIpv6 / isMac
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isIpv6("2001:db8::1"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isIpv6("2001:0db8:0000:0000:0000:0000:0000:0001"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isIpv6("192.168.1.1"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isMac("00:1A:2B:3C:4D:5E"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isMac("00-1A-2B-3C-4D-5E"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isMac("00:1A:2B"));

		// StrUtil.isUpperCase / isLowerCase
		Assert.assertTrue(StrUtil.isUpperCase("HELLO123"));
		Assert.assertFalse(StrUtil.isUpperCase("Hello"));
		Assert.assertTrue(StrUtil.isLowerCase("hello123"));
		Assert.assertFalse(StrUtil.isLowerCase("helloWorld"));
		Assert.assertFalse(StrUtil.isUpperCase(""));

		// DateUtil.isSameMonth
		Assert.assertTrue(com.sure.tool.date.DateUtil.isSameMonth(
				com.sure.tool.date.DateUtil.parse("2026-09-01", "yyyy-MM-dd"),
				com.sure.tool.date.DateUtil.parse("2026-09-30", "yyyy-MM-dd")));
		Assert.assertFalse(com.sure.tool.date.DateUtil.isSameMonth(
				com.sure.tool.date.DateUtil.parse("2026-09-01", "yyyy-MM-dd"),
				com.sure.tool.date.DateUtil.parse("2025-09-01", "yyyy-MM-dd")));
		Assert.assertFalse(com.sure.tool.date.DateUtil.isSameMonth(null, new java.util.Date()));

		// ObjectUtil.toString
		Assert.assertEquals("abc", com.sure.tool.util.ObjectUtil.toString("abc"));
		Assert.assertEquals("", com.sure.tool.util.ObjectUtil.toString(null));
		Assert.assertEquals("def", com.sure.tool.util.ObjectUtil.toString(null, "def"));
	}
}
