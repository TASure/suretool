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
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.NumberUtil;
import com.sure.tool.util.ObjectUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）第十一批增强单元测试：ObjectUtil / DateUtil / StrUtil / NumberUtil / ConvertUtil / CollUtil。
 */
public class P4Features11Test {

	// ---------------- ObjectUtil 增强 ----------------

	@Test
	public void testObjectEnhance() {
		Assert.assertTrue(ObjectUtil.isBasicType(1));
		Assert.assertTrue(ObjectUtil.isBasicType("abc"));
		Assert.assertTrue(ObjectUtil.isBasicType(new BigDecimal("1.5")));
		Assert.assertTrue(ObjectUtil.isBasicType(int.class));
		Assert.assertFalse(ObjectUtil.isBasicType(new Object()));
		Assert.assertFalse(ObjectUtil.isBasicType((Object) null));

		Assert.assertEquals("java.lang.String", ObjectUtil.getClassName("x"));
		Assert.assertEquals("int[]", ObjectUtil.getClassName(new int[3]));
		Assert.assertEquals("java.lang.String[][]", ObjectUtil.getClassName(new String[2][2]));
		Assert.assertNull(ObjectUtil.getClassName(null));
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance7() throws Exception {
		Date d1 = DateUtil.parse("2026-09-16 08:00:00", DateUtil.NORM_DATETIME_PATTERN);
		Date d2 = DateUtil.parse("2026-09-16 20:00:00", DateUtil.NORM_DATETIME_PATTERN);
		Date d3 = DateUtil.parse("2026-09-17", DateUtil.NORM_DATE_PATTERN);
		Assert.assertTrue(DateUtil.isSameDay(d1, d2));
		Assert.assertFalse(DateUtil.isSameDay(d1, d3));
		Assert.assertFalse(DateUtil.isSameDay(null, d2));

		Assert.assertTrue(DateUtil.isToday(new Date()));
		Assert.assertFalse(DateUtil.isToday(DateUtil.offsetDay(new Date(), -1)));
		Assert.assertTrue(DateUtil.isYesterday(DateUtil.offsetDay(new Date(), -1)));
		Assert.assertFalse(DateUtil.isYesterday(new Date()));
	}

	// ---------------- StrUtil 增强 ----------------

	@Test
	public void testStrEnhance4() {
		Assert.assertArrayEquals(new int[] {1, 2, 3}, StrUtil.splitToIntArray("1 2 3"));
		Assert.assertArrayEquals(new int[] {1, 0, 3}, StrUtil.splitToIntArray("1 x 3"));
		Assert.assertArrayEquals(new int[0], StrUtil.splitToIntArray(null));
		Assert.assertArrayEquals(new long[] {10, 20}, StrUtil.splitToLongArray("10 20"));
		Assert.assertArrayEquals(new long[] {10, 0}, StrUtil.splitToLongArray("10 2.5"));
	}

	// ---------------- NumberUtil 增强 ----------------

	@Test
	public void testNumberEnhance3() {
		Assert.assertArrayEquals(new int[] {3, 1, 1}, NumberUtil.partValue(5, 3, 1, 1));
		Assert.assertArrayEquals(new int[] {5, 5}, NumberUtil.partValue(10, 1, 1));
		Assert.assertEquals(3, NumberUtil.partValue(5, 3, 1, 1).length);

		Assert.assertTrue(NumberUtil.isInteger("123"));
		Assert.assertTrue(NumberUtil.isInteger("-45"));
		Assert.assertFalse(NumberUtil.isInteger("12.5"));
		Assert.assertFalse(NumberUtil.isInteger(""));
		Assert.assertFalse(NumberUtil.isInteger(null));
		Assert.assertTrue(NumberUtil.isLong("99999999999"));
		Assert.assertTrue(NumberUtil.isDouble("3.14"));
		Assert.assertFalse(NumberUtil.isDouble("abc"));
		Assert.assertTrue(NumberUtil.isDouble("-0.5"));
	}

	// ---------------- ConvertUtil 增强 ----------------

	@Test
	public void testConvertEnhance2() {
		Assert.assertEquals((short) 12, ConvertUtil.toShort("12"));
		Assert.assertEquals((short) -1, ConvertUtil.toShort("-1"));
		Assert.assertEquals((short) 0, ConvertUtil.toShort("abc"));
		Assert.assertEquals((short) 9, ConvertUtil.toShort("abc", (short) 9));

		Assert.assertEquals((byte) 7, ConvertUtil.toByte("7"));
		Assert.assertEquals((byte) 0, ConvertUtil.toByte("x"));
		Assert.assertEquals((byte) 5, ConvertUtil.toByte(5L));
		Assert.assertEquals((byte) 3, ConvertUtil.toByte("x", (byte) 3));

		Assert.assertEquals(2.5F, ConvertUtil.toFloat("2.5"), 1e-6);
		Assert.assertEquals(0F, ConvertUtil.toFloat("x"), 1e-6);

		Assert.assertArrayEquals(new char[] {'a', 'b', 'c'}, ConvertUtil.toCharArray("abc"));
		Assert.assertArrayEquals(new char[] {'1', '2'}, ConvertUtil.toCharArray(new String[] {"1", "2"}));
		Assert.assertArrayEquals(new char[0], ConvertUtil.toCharArray(null));
	}

	// ---------------- CollUtil 增强 ----------------

	@Test
	public void testCollEnhance2() {
		Assert.assertEquals(List.of(1, 2, 3), CollUtil.distinct(Arrays.asList(1, 2, 2, 3, 1)));
		Assert.assertEquals(0, CollUtil.distinct(List.of()).size());

		Map<String, String> map = CollUtil.listToMap(List.of("a", "bb", "ccc"), s -> String.valueOf(s.length()));
		Assert.assertEquals("ccc", map.get("3"));
		Assert.assertEquals(3, map.size());
	}

	@Test
	public void testSubBetweenAll() {
		Assert.assertEquals(java.util.List.of("b", "c"), StrUtil.subBetweenAll("a[b]a[c]", "[", "]"));
		Assert.assertEquals(0, StrUtil.subBetweenAll("no brackets", "[", "]").size());
		Assert.assertEquals(0, StrUtil.subBetweenAll(null, "[", "]").size());
	}



	@Test
	public void testMapAndStrEnhance2() {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("f", 2.5F);
		map.put("c", 'A');
		map.put("b", 7);
		map.put("s", 12);

		Assert.assertEquals(2.5F, com.sure.tool.collection.MapUtil.getFloat(map, "f", 0F), 1e-6);
		Assert.assertEquals(1.5F, com.sure.tool.collection.MapUtil.getFloat(map, "x", 1.5F), 1e-6);
		Assert.assertEquals('A', com.sure.tool.collection.MapUtil.getChar(map, "c", 'Z'));
		Assert.assertEquals('Z', com.sure.tool.collection.MapUtil.getChar(map, "x", 'Z'));
		Assert.assertEquals((byte) 7, com.sure.tool.collection.MapUtil.getByte(map, "b", (byte) 3));
		Assert.assertEquals((byte) 3, com.sure.tool.collection.MapUtil.getByte(map, "x", (byte) 3));
		Assert.assertEquals((short) 12, com.sure.tool.collection.MapUtil.getShort(map, "s", (short) 9));
		Assert.assertEquals((short) 9, com.sure.tool.collection.MapUtil.getShort(map, "x", (short) 9));

		Assert.assertArrayEquals(new double[] {1.5, 2.5}, StrUtil.splitToDoubleArray("1.5 2.5"), 1e-9);
		Assert.assertArrayEquals(new double[] {1.5, 0.0}, StrUtil.splitToDoubleArray("1.5 abc"), 1e-9);
		Assert.assertArrayEquals(new double[0], StrUtil.splitToDoubleArray(null), 1e-9);
	}


}
