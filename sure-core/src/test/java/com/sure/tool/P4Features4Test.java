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

import com.sure.tool.codec.HashUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.CompareUtil;
import com.sure.tool.util.MoneyUtil;
import com.sure.tool.util.NetUtil;
import com.sure.tool.util.ObjectUtil;
import com.sure.tool.util.RandomUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * P4（v0.2.0）第四批新增/增强单元测试：MoneyUtil / CompareUtil / DateUtil / NetUtil / ObjectUtil / HashUtil / RandomUtil。
 */
public class P4Features4Test {

	// ---------------- MoneyUtil ----------------

	@Test
	public void testMoneyUtil() {
		Assert.assertEquals("壹佰贰拾叁元肆角伍分", MoneyUtil.toChinese(new BigDecimal("123.45")));
		Assert.assertEquals("零元整", MoneyUtil.toChinese(new BigDecimal("0")));
		Assert.assertEquals("壹仟零壹元零壹分", MoneyUtil.toChinese(new BigDecimal("1001.01")));
		Assert.assertEquals("负壹拾元伍角", MoneyUtil.toChinese(new BigDecimal("-10.50")));
		Assert.assertEquals("壹拾万元整", MoneyUtil.toChinese(new BigDecimal("100000")));
		Assert.assertEquals("壹亿零壹元整", MoneyUtil.toChinese(new BigDecimal("100000001")));
		Assert.assertEquals("零元零伍分", MoneyUtil.toChinese(new BigDecimal("0.05")));
		Assert.assertEquals("壹佰零壹元整", MoneyUtil.toChinese(new BigDecimal("101")));
		Assert.assertEquals("壹仟零壹拾元整", MoneyUtil.toChinese(new BigDecimal("1010")));
		Assert.assertEquals("壹拾壹元整", MoneyUtil.toChinese(new BigDecimal("11")));
		Assert.assertEquals("壹万零壹元整", MoneyUtil.toChinese(new BigDecimal("10001")));
		Assert.assertEquals("壹亿零壹仟零壹元整", MoneyUtil.toChinese(new BigDecimal("100001001")));
		Assert.assertEquals("壹佰元整", MoneyUtil.toChinese(new BigDecimal("100")));
		// 重载
		Assert.assertEquals("壹佰贰拾叁元肆角伍分", MoneyUtil.toChinese(123.45));
		Assert.assertEquals("壹佰贰拾叁元肆角伍分", MoneyUtil.toChinese("123.45"));
		Assert.assertNull(MoneyUtil.toChinese((BigDecimal) null));
		Assert.assertNull(MoneyUtil.toChinese("abc"));
	}

	// ---------------- CompareUtil ----------------

	@Test
	public void testCompareUtil() {
		Assert.assertEquals(-1, CompareUtil.compare(1, 2));
		Assert.assertEquals(0, CompareUtil.compare(5L, 5L));
		Assert.assertEquals(1, CompareUtil.compare(3.5, 2.5));
		Assert.assertEquals(-1, CompareUtil.compare("a", "b", null));
		Assert.assertEquals(1, CompareUtil.compareIgnoreCase("b", "A"));
		Assert.assertEquals(0, CompareUtil.compareIgnoreCase("Ab", "aB"));
		// 空安全：null 最小
		Assert.assertEquals(-1, CompareUtil.compare(null, "x", null));
		Assert.assertEquals(1, CompareUtil.compare("x", null, null));
		Assert.assertEquals(0, CompareUtil.compare(null, null, null));
		// 最值
		Assert.assertEquals(Integer.valueOf(9), CompareUtil.max(null, 1, 9, 3));
		Assert.assertEquals(Integer.valueOf(1), CompareUtil.min(null, 1, 9, 3));
		Assert.assertNull(CompareUtil.max(null, (Integer[]) null));
		Assert.assertNull(CompareUtil.max(null, null, null, null));
		// 约束
		Assert.assertEquals(Integer.valueOf(5), CompareUtil.clamp(10, 1, 5));
		Assert.assertEquals(Integer.valueOf(1), CompareUtil.clamp(0, 1, 5));
		Assert.assertEquals(Integer.valueOf(3), CompareUtil.clamp(3, 1, 5));
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance() {
		Date march21 = date(2026, Calendar.MARCH, 21);
		Assert.assertEquals("白羊座", DateUtil.getZodiac(march21));
		Date jan15 = date(2026, Calendar.JANUARY, 15);
		Assert.assertEquals("摩羯座", DateUtil.getZodiac(jan15));
		Date dec22 = date(2026, Calendar.DECEMBER, 22);
		Assert.assertEquals("摩羯座", DateUtil.getZodiac(dec22));
		Assert.assertNull(DateUtil.getZodiac(null));

		Assert.assertEquals("马", DateUtil.getChineseZodiac(2026));
		Assert.assertEquals("鼠", DateUtil.getChineseZodiac(1900));
		Assert.assertEquals("龙", DateUtil.getChineseZodiac(2024));
		Assert.assertEquals("虎", DateUtil.getChineseZodiac(2022));
	}

	private static Date date(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month, day);
		return c.getTime();
	}

	// ---------------- NetUtil 增强 ----------------

	@Test
	public void testNetEnhance() {
		Assert.assertEquals(3232235777L, NetUtil.ipv4ToLong("192.168.1.1"));
		Assert.assertEquals("192.168.1.1", NetUtil.longToIpv4(3232235777L));
		Assert.assertEquals(0x7f000001L, NetUtil.ipv4ToLong("127.0.0.1"));
		Assert.assertEquals("127.0.0.1", NetUtil.longToIpv4(0x7f000001L));

		Assert.assertTrue(NetUtil.isIpv4("0.0.0.0"));
		Assert.assertTrue(NetUtil.isIpv4("255.255.255.255"));
		Assert.assertFalse(NetUtil.isIpv4("256.1.1.1"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.3"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.3.a"));
		Assert.assertFalse(NetUtil.isIpv4("01.2.3.4"));
		Assert.assertFalse(NetUtil.isIpv4(null));
		try {
			NetUtil.ipv4ToLong("999.1.1.1");
			Assert.fail("非法地址应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- ObjectUtil 增强 ----------------

	@Test
	public void testObjectEnhance() {
		Assert.assertTrue(ObjectUtil.isAllNotNull("a", "b"));
		Assert.assertFalse(ObjectUtil.isAllNotNull("a", null));
		Assert.assertTrue(ObjectUtil.isAllNull(null, null));
		Assert.assertFalse(ObjectUtil.isAllNull("a", null));

		Assert.assertEquals(-1, ObjectUtil.compare("a", "b"));
		Assert.assertEquals(0, ObjectUtil.compare(null, null));
		Assert.assertEquals(-1, ObjectUtil.compare(null, "b"));
		Assert.assertEquals(1, ObjectUtil.compare("b", null));
	}

	// ---------------- HashUtil 增强 ----------------

	@Test
	public void testHashEnhance() {
		Assert.assertEquals(0, HashUtil.murmur3_32(""));
		Assert.assertEquals(0xcbf29ce484222325L, HashUtil.fnv1a64(""));
		Assert.assertEquals(177670L, HashUtil.djb2("a"));
		// 稳定性：同一输入结果一致；不同输入大概率不同
		Assert.assertEquals(HashUtil.murmur3_32("suretool"), HashUtil.murmur3_32("suretool"));
		Assert.assertNotEquals(HashUtil.murmur3_32("suretool"), HashUtil.murmur3_32("suretoom"));
		Assert.assertEquals(HashUtil.fnv1a64("hello"), HashUtil.fnv1a64("hello"));
		Assert.assertEquals(HashUtil.djb2("hello"), HashUtil.djb2("hello"));
		Assert.assertEquals(0, HashUtil.murmur3_32((String) null));
		Assert.assertEquals(0L, HashUtil.fnv1a64((String) null));
		Assert.assertEquals(0L, HashUtil.djb2(null));
	}

	// ---------------- RandomUtil 增强 ----------------

	@Test
	public void testRandomEnhance() {
		List<String> list = Arrays.asList("a", "b", "c", "d", "e");
		for (int i = 0; i < 20; i++) {
			Assert.assertTrue(list.contains(RandomUtil.randomEle(list)));
		}
		String[] array = {"x", "y", "z"};
		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(Arrays.asList(array).contains(RandomUtil.randomEle(array)));
		}
		Assert.assertNull(RandomUtil.randomEle(new ArrayList<String>()));
		Assert.assertNull(RandomUtil.randomEle((String[]) null));

		Set<String> subset = RandomUtil.randomEleSet(list, 3);
		Assert.assertEquals(3, subset.size());
		Assert.assertTrue(list.containsAll(subset));
		Set<String> all = RandomUtil.randomEleSet(list, 10);
		Assert.assertEquals(5, all.size());
		Assert.assertEquals(0, RandomUtil.randomEleSet(new ArrayList<String>(), 2).size());
	}
}
