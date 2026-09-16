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

import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.MapUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.CharUtil;
import com.sure.tool.util.HexUtil;
import com.sure.tool.util.TimeInterval;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）第七批新增/增强单元测试：TimeInterval / CharUtil / HexUtil / DateUtil / CollUtil / MapUtil。
 */
public class P4Features7Test {

	// ---------------- TimeInterval ----------------

	@Test
	public void testTimeInterval() throws Exception {
		TimeInterval timer = TimeInterval.start();
		Thread.sleep(30);
		Assert.assertTrue(timer.intervalMs() >= 20);
		Assert.assertTrue(timer.intervalSecond() >= 0.02);
		Assert.assertTrue(timer.intervalNs() >= 20_000_000);
		Assert.assertTrue(timer.pretty().contains("毫秒"));
		Assert.assertTrue(timer.toString().contains("elapsedMs"));

		timer.restart();
		Assert.assertTrue(timer.intervalMs() < 100);
	}

	// ---------------- CharUtil ----------------

	@Test
	public void testCharUtil() {
		Assert.assertTrue(CharUtil.isNumber('5'));
		Assert.assertFalse(CharUtil.isNumber('a'));
		Assert.assertTrue(CharUtil.isLetter('a'));
		Assert.assertTrue(CharUtil.isLetter('Z'));
		Assert.assertFalse(CharUtil.isLetter('1'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('a'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('9'));
		Assert.assertFalse(CharUtil.isLetterOrNumber('_'));
		Assert.assertTrue(CharUtil.isLowerCase('a'));
		Assert.assertFalse(CharUtil.isLowerCase('A'));
		Assert.assertTrue(CharUtil.isUpperCase('A'));
		Assert.assertFalse(CharUtil.isUpperCase('a'));
		Assert.assertTrue(CharUtil.isBlankChar(' '));
		Assert.assertTrue(CharUtil.isBlankChar('\t'));
		Assert.assertFalse(CharUtil.isBlankChar('x'));
		Assert.assertTrue(CharUtil.isFileSeparator('/'));
		Assert.assertTrue(CharUtil.isFileSeparator('\\'));
		Assert.assertFalse(CharUtil.isFileSeparator('.'));

		Assert.assertEquals('A', CharUtil.toUpper('a'));
		Assert.assertEquals('A', CharUtil.toUpper('A'));
		Assert.assertEquals('b', CharUtil.toLower('B'));
		Assert.assertEquals('1', CharUtil.toLower('1'));
	}

	// ---------------- HexUtil ----------------

	@Test
	public void testHexUtil() {
		byte[] bytes = {0x41, 0x42, (byte) 0xFF, 0x10};
		Assert.assertEquals("4142ff10", HexUtil.encodeHex(bytes));
		Assert.assertEquals("4142FF10", HexUtil.encodeHexUpper(bytes));
		Assert.assertArrayEquals(bytes, HexUtil.decodeHex("4142FF10"));
		Assert.assertArrayEquals(bytes, HexUtil.decodeHex(" 41 42 ff 10 "));
		Assert.assertNull(HexUtil.encodeHex(null));
		Assert.assertNull(HexUtil.decodeHex(null));

		Assert.assertEquals("73757265", HexUtil.encodeHexStr("sure"));
		Assert.assertEquals("sure", HexUtil.decodeHexStr("73757265"));

		try {
			HexUtil.decodeHex("abc");
			Assert.fail("奇数长度应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			HexUtil.decodeHex("zz");
			Assert.fail("非法字符应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance3() throws Exception {
		Date d = DateUtil.parse("2026-09-16 15:30:45", DateUtil.NORM_DATETIME_PATTERN);
		Assert.assertEquals(3, DateUtil.season(d));
		Assert.assertEquals("2026年9月16日", DateUtil.formatChineseDate(d));
		Assert.assertEquals(259, DateUtil.dayOfYear(d));
		Assert.assertEquals(30, DateUtil.minute(d));
		Assert.assertEquals(45, DateUtil.second(d));

		Date q1 = DateUtil.parse("2026-02-01", DateUtil.NORM_DATE_PATTERN);
		Assert.assertEquals(1, DateUtil.season(q1));
	}

	// ---------------- CollUtil / MapUtil 增强 ----------------

	@Test
	public void testCollAndMapEnhance() {
		List<String> list = List.of("a", "b", "c");
		Assert.assertTrue(CollUtil.containsAll(list, "a", "b"));
		Assert.assertFalse(CollUtil.containsAll(list, "a", "z"));
		Assert.assertTrue(CollUtil.containsAll(list, List.of("b", "c")));
		Assert.assertFalse(CollUtil.containsAll(list, List.of("a", "x")));
		Assert.assertTrue(CollUtil.containsAll(list, (String[]) null));
		Assert.assertTrue(CollUtil.containsAll(list, List.of()));

		Map<String, Object> map = new HashMap<>();
		map.put("price", "19.99");
		map.put("big", 100L);
		Assert.assertEquals(new BigDecimal("19.99"), MapUtil.getBigDecimal(map, "price"));
		Assert.assertEquals(new BigDecimal("100"), MapUtil.getBigDecimal(map, "big"));
		Assert.assertEquals(new BigDecimal("5"), MapUtil.getBigDecimal(map, "missing", new BigDecimal("5")));
		Assert.assertNull(MapUtil.getBigDecimal(map, "missing"));
	}
}
