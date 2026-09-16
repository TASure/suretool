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
 * P4（v0.2.0）第三十三批测试：等值判断/路径操作/时间段重叠/日期区间/Map 便捷与构建器。
 */
public class P4Features28Test {

	@Test
	public void testStrEqualsAnyAndLength() {
		Assert.assertTrue(com.sure.tool.util.StrUtil.equalsAny("a", "x", "a", "y"));
		Assert.assertTrue(com.sure.tool.util.StrUtil.equalsAny(null, null, "x"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.equalsAny("a", "x", "y"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.equalsAny("a", new CharSequence[0]));
		Assert.assertFalse(com.sure.tool.util.StrUtil.equalsAny("a", (CharSequence[]) null));

		Assert.assertTrue(com.sure.tool.util.StrUtil.equalsAnyIgnoreCase("AbC", "abc", "x"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.equalsAnyIgnoreCase("abc", "abd", "abe"));
		Assert.assertTrue(com.sure.tool.util.StrUtil.equalsAnyIgnoreCase("abc", null, "ABC"));

		Assert.assertEquals(6, com.sure.tool.util.StrUtil.totalLength("ab", "cd", "ef"));
		Assert.assertEquals(4, com.sure.tool.util.StrUtil.totalLength("ab", null, "cd"));
		Assert.assertEquals(0, com.sure.tool.util.StrUtil.totalLength());
		Assert.assertEquals(0, com.sure.tool.util.StrUtil.totalLength((CharSequence[]) null));
		Assert.assertEquals(0, com.sure.tool.util.StrUtil.totalLength("", null));
	}

	@Test
	public void testFileParentAndDir() throws Exception {
		char sep = java.io.File.separatorChar;
		String path = sep + "a" + sep + "b" + sep + "c.txt";
		Assert.assertEquals(sep + "a" + sep + "b", com.sure.tool.io.FileUtil.getParent(path));
		Assert.assertNull(com.sure.tool.io.FileUtil.getParent("c.txt"));
		Assert.assertNull(com.sure.tool.io.FileUtil.getParent((String) null));

		java.io.File tmp = new java.io.File(com.sure.tool.io.FileUtil.getTmpDir(),
				"sure-p4-dir-" + System.nanoTime());
		com.sure.tool.io.FileUtil.mkdir(tmp);
		Assert.assertTrue(com.sure.tool.io.FileUtil.isDirEmpty(tmp));
		java.io.File inner = com.sure.tool.io.FileUtil.writeUtf8String("x", new java.io.File(tmp, "f.txt"));
		Assert.assertFalse(com.sure.tool.io.FileUtil.isDirEmpty(tmp));
		Assert.assertEquals(tmp.getAbsolutePath(), com.sure.tool.io.FileUtil.getParent(inner));
		Assert.assertFalse(com.sure.tool.io.FileUtil.isDirEmpty(null));
		Assert.assertFalse(com.sure.tool.io.FileUtil.isDirEmpty(inner));
		com.sure.tool.io.FileUtil.delete(tmp);

		Assert.assertTrue(com.sure.tool.io.FileUtil.pathEquals(sep + "a" + sep + "b" + sep + ".." + sep + "c",
				sep + "a" + sep + "c"));
		Assert.assertTrue(com.sure.tool.io.FileUtil.pathEquals(sep + "a" + sep + "b" + sep + sep + "c",
				sep + "a" + sep + "b" + sep + "c"));
		Assert.assertFalse(com.sure.tool.io.FileUtil.pathEquals(sep + "a", sep + "b"));
		Assert.assertTrue(com.sure.tool.io.FileUtil.pathEquals(null, null));
		Assert.assertFalse(com.sure.tool.io.FileUtil.pathEquals(null, sep + "a"));
	}

	@Test
	public void testDateOverlapAndRange() {
		java.util.Date d1 = com.sure.tool.date.DateUtil.parse("2026-09-01 10:00:00");
		java.util.Date d2 = com.sure.tool.date.DateUtil.parse("2026-09-01 12:00:00");
		java.util.Date d3 = com.sure.tool.date.DateUtil.parse("2026-09-01 11:00:00");
		java.util.Date d4 = com.sure.tool.date.DateUtil.parse("2026-09-01 13:00:00");
		Assert.assertTrue(com.sure.tool.date.DateUtil.isOverlap(d1, d2, d3, d4));
		Assert.assertTrue(com.sure.tool.date.DateUtil.isOverlap(d3, d4, d1, d2));
		Assert.assertFalse(com.sure.tool.date.DateUtil.isOverlap(d1, d3, d3, d4));
		try {
			com.sure.tool.date.DateUtil.isOverlap(null, d2, d3, d4);
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}

		java.util.Date s = com.sure.tool.date.DateUtil.parse("2026-09-01 00:00:00");
		java.util.Date e = com.sure.tool.date.DateUtil.parse("2026-09-03 00:00:00");
		java.util.List<java.util.Date> days = com.sure.tool.date.DateUtil.rangeToList(s, e,
				com.sure.tool.date.DateUnit.DAY);
		Assert.assertEquals(3, days.size());
		Assert.assertEquals("2026-09-01", com.sure.tool.date.DateUtil.formatDate(days.get(0)));
		Assert.assertEquals("2026-09-03", com.sure.tool.date.DateUtil.formatDate(days.get(2)));

		java.util.Date h1 = com.sure.tool.date.DateUtil.parse("2026-09-01 08:00:00");
		java.util.Date h2 = com.sure.tool.date.DateUtil.parse("2026-09-01 10:00:00");
		java.util.List<java.util.Date> hours = com.sure.tool.date.DateUtil.rangeToList(h1, h2,
				com.sure.tool.date.DateUnit.HOUR);
		Assert.assertEquals(3, hours.size());

		java.util.List<java.util.Date> empty = com.sure.tool.date.DateUtil.rangeToList(e, s,
				com.sure.tool.date.DateUnit.DAY);
		Assert.assertTrue(empty.isEmpty());
		try {
			com.sure.tool.date.DateUtil.rangeToList(null, e, com.sure.tool.date.DateUnit.DAY);
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void testMapConvenienceAndBuilder() {
		java.util.Map<String, Object> m = new java.util.HashMap<>();
		m.put("i", 42);
		m.put("l", 99L);
		m.put("d", 3.14);
		m.put("b", true);
		Assert.assertEquals(42, com.sure.tool.collection.MapUtil.getInt(m, "i"));
		Assert.assertEquals(0, com.sure.tool.collection.MapUtil.getInt(m, "missing"));
		Assert.assertEquals(99L, com.sure.tool.collection.MapUtil.getLong(m, "l"));
		Assert.assertEquals(0L, com.sure.tool.collection.MapUtil.getLong(m, "missing"));
		Assert.assertEquals(3.14D, com.sure.tool.collection.MapUtil.getDouble(m, "d"), 0.0001D);
		Assert.assertEquals(0D, com.sure.tool.collection.MapUtil.getDouble(m, "missing"), 0.0001D);
		Assert.assertTrue(com.sure.tool.collection.MapUtil.getBool(m, "b"));
		Assert.assertFalse(com.sure.tool.collection.MapUtil.getBool(m, "missing"));

		java.util.Map<String, Integer> built = com.sure.tool.collection.MapUtil.MapBuilder.<String, Integer>create()
				.put("a", 1)
				.put("b", 2)
				.putAll(java.util.Map.of("c", 3))
				.build();
		Assert.assertEquals(3, built.size());
		Assert.assertEquals(Integer.valueOf(1), built.get("a"));
		Assert.assertEquals(Integer.valueOf(3), built.get("c"));

		java.util.Map<String, Integer> imm = com.sure.tool.collection.MapUtil.<String, Integer>builder()
				.put("k", 1)
				.build(true);
		Assert.assertEquals(Integer.valueOf(1), imm.get("k"));
		try {
			imm.put("x", 2);
			Assert.fail("不可变 Map 应当抛异常");
		} catch (UnsupportedOperationException expected) {
			// 预期
		}
	}


}
