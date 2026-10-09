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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * ReUtil 覆盖率补测：null 内容守卫与命名分组提取分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ReUtilGapTest {

	@Test
	public void testNullGuards() {
		Assert.assertFalse(ReUtil.isMatch("\\d+", null));
		Assert.assertNull(ReUtil.get("\\d+", null, 1));
		Assert.assertNull(ReUtil.get("(?<n>\\d+)", null, "n"));
		Assert.assertNull(ReUtil.getLast("\\d+", null, 1));
		Assert.assertNull(ReUtil.getLast("(?<n>\\d+)", null, "n"));
		Assert.assertTrue(ReUtil.getAllGroups("\\d+", null).isEmpty());
		Assert.assertTrue(ReUtil.getAllGroupMap("\\d+", null).isEmpty());
		Assert.assertTrue(ReUtil.findAll("\\d+", null).isEmpty());
		Assert.assertEquals(0, ReUtil.count("\\d+", null));
		Assert.assertEquals("", ReUtil.replaceAll(null, "\\d+", "#"));
		Assert.assertEquals("", ReUtil.delFirst("\\d+", null));
		Assert.assertEquals("", ReUtil.delAll("\\d+", null));
		Assert.assertNull(ReUtil.extractMulti("\\d+", null, "{1}"));
	}

	@Test
	public void testNamedGroupsAndExtract() {
		java.util.Map<String, String> m = ReUtil.getAllGroupMap("(?<year>\\d{4})-(?<month>\\d{2})", "2026-10");
		Assert.assertEquals("2026", m.get("year"));
		Assert.assertEquals("10", m.get("month"));
		Assert.assertEquals("2", ReUtil.get("\\d(\\d)", "a12b34", 1));
		Assert.assertEquals("4", ReUtil.getLast("\\d(\\d)", "a12b34", 1));
		Assert.assertEquals("ab", ReUtil.extractMulti("(\\w)(\\w)", "ab", "{1}{2}"));
	}

	@Test
	public void testGetNoMatch() {
		Assert.assertNull(ReUtil.get("(?<x>\\d)", "abc", "x"));
	}
}
