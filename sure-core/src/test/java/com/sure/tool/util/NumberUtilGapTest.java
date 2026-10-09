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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * NumberUtil 覆盖率补测：blank 守卫、边界异常、空数组返回 null、素数/分配余数分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class NumberUtilGapTest {

	@Test
	public void testParseDefaults() {
		Assert.assertEquals(5L, NumberUtil.parseLong(null, 5L));
		Assert.assertEquals(5L, NumberUtil.parseLong(" ", 5L));
		Assert.assertEquals(2.5d, NumberUtil.parseDouble(null, 2.5d), 0.0001d);
		Assert.assertEquals(2.5d, NumberUtil.parseDouble("bad", 2.5d), 0.0001d);
		Assert.assertEquals(1.5f, NumberUtil.parseFloat(null, 1.5f), 0.0001f);
		Assert.assertEquals((short) 9, NumberUtil.parseShort("", (short) 9));
		Assert.assertEquals((byte) 7, NumberUtil.parseByte(null, (byte) 7));
	}

	@Test
	public void testIsNumberGuards() {
		Assert.assertFalse(NumberUtil.isDouble(null));
		Assert.assertFalse(NumberUtil.isDouble("  "));
		Assert.assertFalse(NumberUtil.isDouble("abc"));
		Assert.assertTrue(NumberUtil.isDouble("3.14"));
		Assert.assertFalse(NumberUtil.isInteger("-"));
		Assert.assertTrue(NumberUtil.isInteger("+42"));
	}

	@Test
	public void testExceptions() {
		try {
			NumberUtil.isEquals(1d, 2d, -1d);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			NumberUtil.range(0, 10, 0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			NumberUtil.partValue(10, 0, 0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			NumberUtil.pow(2, -1);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testMinMaxAndNull() {
		Assert.assertNull(NumberUtil.min());
		Assert.assertNull(NumberUtil.min(new Number[] {null, null}));
		Assert.assertEquals(Integer.valueOf(1), NumberUtil.min(null, 3, 1, 2));
		Assert.assertNull(NumberUtil.max());
		Assert.assertEquals(null, NumberUtil.toFixed(null, 2));
		Assert.assertEquals("1.50", NumberUtil.toFixed(new BigDecimal("1.5"), 2));
	}

	@Test
	public void testIsPrimeAndPartValue() {
		Assert.assertFalse(NumberUtil.isPrime(25));
		Assert.assertTrue(NumberUtil.isPrime(17));
		int[] dist = NumberUtil.partValue(1, 1, 1);
		Assert.assertEquals(1, dist[0]);
		Assert.assertEquals(0, dist[1]);
	}
}
