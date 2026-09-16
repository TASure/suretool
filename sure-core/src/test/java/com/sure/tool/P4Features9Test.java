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
import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.NumberUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * P4（v0.2.0）第九批增强单元测试：NumberUtil / DateUtil / ConvertUtil / StrUtil / CollUtil / ArrayUtil。
 */
public class P4Features9Test {

	// ---------------- NumberUtil 增强 ----------------

	@Test
	public void testNumberEnhance2() {
		Assert.assertEquals(6, NumberUtil.gcd(48, 18));
		Assert.assertEquals(6, NumberUtil.gcd(-48, 18));
		Assert.assertEquals(144, NumberUtil.lcm(48, 18));
		Assert.assertEquals(0, NumberUtil.lcm(0, 5));

		Assert.assertTrue(NumberUtil.isPrime(2));
		Assert.assertTrue(NumberUtil.isPrime(3));
		Assert.assertTrue(NumberUtil.isPrime(97));
		Assert.assertFalse(NumberUtil.isPrime(1));
		Assert.assertFalse(NumberUtil.isPrime(0));
		Assert.assertFalse(NumberUtil.isPrime(4));
		Assert.assertFalse(NumberUtil.isPrime(100));
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance5() throws Exception {
		Date d = DateUtil.parse("2026-09-16 15:30:45", DateUtil.NORM_DATETIME_PATTERN);
		Assert.assertEquals("2026-01-01", DateUtil.formatDate(DateUtil.beginOfYear(d)));
		Assert.assertEquals("2026-12-31", DateUtil.formatDate(DateUtil.endOfYear(d)));
		Assert.assertEquals("2026-10-16", DateUtil.formatDate(DateUtil.offsetMonth(d, 1)));
		Assert.assertEquals("2026-08-16", DateUtil.formatDate(DateUtil.offsetMonth(d, -1)));
		Assert.assertEquals("2027-01-16", DateUtil.formatDate(DateUtil.offsetMonth(d, 4)));
	}

	// ---------------- ConvertUtil 增强 ----------------

	@Test
	public void testConvertEnhance() {
		Assert.assertEquals(new BigDecimal("19.99"), ConvertUtil.toBigDecimal("19.99"));
		Assert.assertEquals(new BigDecimal("100"), ConvertUtil.toBigDecimal(100L));
		Assert.assertNull(ConvertUtil.toBigDecimal(null));
		Assert.assertNull(ConvertUtil.toBigDecimal("abc"));

		Date d1 = ConvertUtil.toDate("2026-09-16");
		Assert.assertEquals("2026-09-16", DateUtil.formatDate(d1));
		Date d2 = ConvertUtil.toDate("2026-09-16 15:30:45");
		Assert.assertEquals("2026-09-16 15:30:45", DateUtil.format(d2, DateUtil.NORM_DATETIME_PATTERN));
		Date d3 = ConvertUtil.toDate(1789000000000L);
		Assert.assertNotNull(d3);
		Assert.assertNull(ConvertUtil.toDate("not-a-date"));
		Assert.assertNull(ConvertUtil.toDate(null));
	}

	// ---------------- StrUtil 增强 ----------------

	@Test
	public void testStrEnhance3() {
		Assert.assertTrue(StrUtil.startWithAny("hello", "he", "hi"));
		Assert.assertFalse(StrUtil.startWithAny("hello", "x", "y"));
		Assert.assertFalse(StrUtil.startWithAny("hello", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.endWithAny("hello.txt", ".txt", ".md"));
		Assert.assertFalse(StrUtil.endWithAny("hello.txt", ".md", ".pdf"));
	}

	// ---------------- CollUtil / ArrayUtil 增强 ----------------

	@Test
	public void testCollAndArrayEnhance() {
		List<Integer> list = List.of(1, 2, 3, 4, 5);
		List<List<Integer>> groups = CollUtil.split(list, 2);
		Assert.assertEquals(3, groups.size());
		Assert.assertEquals(List.of(1, 2), groups.get(0));
		Assert.assertEquals(List.of(5), groups.get(2));
		Assert.assertEquals(0, CollUtil.split(new ArrayList<Integer>(), 2).size());
		try {
			CollUtil.split(list, 0);
			Assert.fail("groupSize<=0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}

		Assert.assertEquals(15, CollUtil.sum(list));
		Assert.assertEquals(15, CollUtil.sumLong(List.of(5L, 10L)));
		Assert.assertEquals(7.5, CollUtil.sumDouble(List.of(2.5, 5.0)), 1e-9);

		Integer[] arr = {1, 2, 3, 4};
		Assert.assertEquals(2, ArrayUtil.lastIndexOf(arr, 3));
		Assert.assertEquals(-1, ArrayUtil.lastIndexOf(arr, 9));
		ArrayUtil.swap(arr, 0, 3);
		Assert.assertEquals(4, (int) arr[0]);
		Assert.assertEquals(1, (int) arr[3]);
		try {
			ArrayUtil.swap(arr, 0, 99);
			Assert.fail("越界应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}
}
