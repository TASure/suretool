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
import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.EmojiUtil;
import com.sure.tool.util.RadixUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）第十批新增/增强单元测试：RadixUtil / EmojiUtil / DateUtil / ArrayUtil / MapUtil / CollUtil。
 */
public class P4Features10Test {

	// ---------------- RadixUtil ----------------

	@Test
	public void testRadix() {
		Assert.assertEquals("1111111111111111", RadixUtil.toString(65535, 2));
		Assert.assertEquals("FFFF", RadixUtil.toString(65535, 16));
		Assert.assertEquals("0", RadixUtil.toString(0, 8));
		Assert.assertEquals("-1000", RadixUtil.toString(-8, 2));
		Assert.assertEquals("10", RadixUtil.toString(8, 8));

		Assert.assertEquals(65535, RadixUtil.toLong("ffff", 16));
		Assert.assertEquals(65535, RadixUtil.toLong("FFFF", 16));
		Assert.assertEquals(8, RadixUtil.toLong("10", 8));
		Assert.assertEquals(-2, RadixUtil.toLong("-10", 2));

		Assert.assertEquals("1111111111111111", RadixUtil.convert("FFFF", 16, 2));
		Assert.assertEquals("FFFF", RadixUtil.convert("1111111111111111", 2, 16));

		try {
			RadixUtil.toLong("2", 2);
			Assert.fail("非法数字应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			RadixUtil.toString(1, 37);
			Assert.fail("越界进制应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- EmojiUtil ----------------

	@Test
	public void testEmoji() {
		// 😀 = U+1F600 = surrogate pair
		Assert.assertEquals("\\ud83d\\ude00", EmojiUtil.toUnicode("\uD83D\uDE00"));
		Assert.assertEquals("\uD83D\uDE00", EmojiUtil.fromUnicode("\\ud83d\\ude00"));
		Assert.assertEquals("abc", EmojiUtil.toUnicode("abc"));
		Assert.assertEquals("a\\u4e2db", EmojiUtil.toUnicode("a中b"));
		Assert.assertEquals("a中b", EmojiUtil.fromUnicode("a\\u4e2db"));
		Assert.assertNull(EmojiUtil.toUnicode(null));
		Assert.assertNull(EmojiUtil.fromUnicode(null));
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance6() throws Exception {
		Date d = DateUtil.parse("2026-09-16 15:30:45", DateUtil.NORM_DATETIME_PATTERN);
		Assert.assertEquals("丙午", DateUtil.getGanzhi(d));
		Assert.assertEquals("2026-09-16 15:30:46", DateUtil.format(DateUtil.offsetSecond(d, 1), DateUtil.NORM_DATETIME_PATTERN));
		Assert.assertEquals("2027-09-16", DateUtil.formatDate(DateUtil.offsetYear(d, 1)));
		Assert.assertEquals("2025-09-16", DateUtil.formatDate(DateUtil.offsetYear(d, -1)));

		Date start = DateUtil.parse("2026-09-01", DateUtil.NORM_DATE_PATTERN);
		Date end = DateUtil.parse("2026-09-30", DateUtil.NORM_DATE_PATTERN);
		Assert.assertTrue(DateUtil.isIn(d, start, end));
		Assert.assertFalse(DateUtil.isIn(DateUtil.parse("2026-10-01", DateUtil.NORM_DATE_PATTERN), start, end));
		Assert.assertTrue(DateUtil.isIn(d, null, end));
		Assert.assertTrue(DateUtil.isIn(d, start, null));
		Assert.assertFalse(DateUtil.isIn(null, start, end));
	}

	// ---------------- ArrayUtil 增强 ----------------

	@Test
	public void testArrayEnhance2() {
		Integer[] arr = {1, 2, 3, 2};
		Object removed = ArrayUtil.remove(arr, 2);
		Assert.assertEquals(3, ArrayUtil.length(removed));
		Assert.assertArrayEquals(new Integer[] {1, 3, 2}, (Integer[]) removed);
		Assert.assertSame(arr, ArrayUtil.remove(arr, 9));

		Object appended = ArrayUtil.append(arr, 4, 5);
		Assert.assertArrayEquals(new Integer[] {1, 2, 3, 2, 4, 5}, (Integer[]) appended);

		Object inserted = ArrayUtil.insert(arr, 1, 9);
		Assert.assertArrayEquals(new Integer[] {1, 9, 2, 3, 2}, (Integer[]) inserted);

		try {
			ArrayUtil.insert(arr, 99, 9);
			Assert.fail("越界应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- MapUtil / CollUtil 增强 ----------------

	@Test
	public void testMapAndCollEnhance() {
		Map<String, Integer> map = new HashMap<>();
		map.put("b", 2);
		map.put("a", 1);
		map.put("c", 3);
		Assert.assertEquals(List.of("a", "b", "c"), new java.util.ArrayList<>(MapUtil.sort(map, true).keySet()));
		Assert.assertEquals(List.of("c", "b", "a"), new java.util.ArrayList<>(MapUtil.sort(map, false).keySet()));

		Map<String, Integer> filtered = MapUtil.filter(map, e -> e.getValue() >= 2);
		Assert.assertEquals(2, filtered.size());
		Assert.assertEquals(3, map.size());

		Map<String, Object> dateMap = new HashMap<>();
		dateMap.put("d", "2026-09-16");
		Assert.assertEquals("2026-09-16", DateUtil.formatDate(MapUtil.getDate(dateMap, "d")));
		Assert.assertNull(MapUtil.getDate(dateMap, "missing"));

		Assert.assertEquals(1, (int) CollUtil.min(List.of(3, 1, 2)));
		Assert.assertEquals(3, (int) CollUtil.max(List.of(3, 1, 2)));
		Assert.assertNull(CollUtil.<Integer>min(List.of()));
		Assert.assertEquals("a", CollUtil.min(List.of("b", "a", "c")));
	}
}
