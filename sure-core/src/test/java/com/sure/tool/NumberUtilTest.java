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

import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.NumberUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * NumberUtil / ConvertUtil 单元测试。
 */
public class NumberUtilTest {

	@Test
	public void testParseWithDefault() {
		Assert.assertEquals(123, NumberUtil.parseInt("123"));
		Assert.assertEquals(0, NumberUtil.parseInt("abc", 0));
		Assert.assertEquals(9, NumberUtil.parseInt("abc", 9));
		Assert.assertEquals(123L, NumberUtil.parseLong("123"));
		Assert.assertEquals(1.5, NumberUtil.parseDouble("1.5"), 0.0001);
		Assert.assertEquals(2.5, NumberUtil.parseDouble("x", 2.5), 0.0001);
	}

	@Test(expected = NumberFormatException.class)
	public void testParseIntThrows() {
		NumberUtil.parseInt("not-a-number");
	}

	@Test
	public void testIsNumber() {
		Assert.assertTrue(NumberUtil.isNumber("123"));
		Assert.assertTrue(NumberUtil.isNumber("-12.5"));
		Assert.assertTrue(NumberUtil.isNumber("1e3"));
		Assert.assertFalse(NumberUtil.isNumber("12a"));
		Assert.assertTrue(NumberUtil.isInteger("123"));
		Assert.assertTrue(NumberUtil.isInteger("-45"));
		Assert.assertFalse(NumberUtil.isInteger("1.5"));
		Assert.assertTrue(NumberUtil.isDouble("1.5"));
	}

	@Test
	public void testRound() {
		BigDecimal value = NumberUtil.round(3.14159, 2);
		Assert.assertEquals(0, value.compareTo(new BigDecimal("3.14")));
		Assert.assertEquals(0, NumberUtil.round(2.675, 2).compareTo(new BigDecimal("2.68")));
	}

	@Test
	public void testMath() {
		Assert.assertEquals(0.3, NumberUtil.add(0.1, 0.2), 0.0000001);
		Assert.assertEquals(0.1, NumberUtil.sub(0.3, 0.2), 0.0000001);
		Assert.assertEquals(6.0, NumberUtil.mul(2.0, 3.0), 0.0001);
		Assert.assertEquals(2.5, NumberUtil.div(5.0, 2.0, 2), 0.0001);
		Assert.assertEquals(10L, NumberUtil.add(4L, 6L));
		Assert.assertTrue(NumberUtil.isEquals(0.1 + 0.2, 0.3, 0.000001));
		Assert.assertTrue(NumberUtil.isEven(4));
		Assert.assertTrue(NumberUtil.isOdd(7));
	}

	@Test(expected = ArithmeticException.class)
	public void testDivByZero() {
		NumberUtil.div(1.0, 0.0, 2);
	}

	@Test
	public void testDecimalFormat() {
		Assert.assertEquals("12,345.68", NumberUtil.decimalFormat(12345.678, "#,##0.00"));
	}

	// ---------------- ConvertUtil ----------------

	@Test
	public void testConvert() {
		Assert.assertEquals(42, ConvertUtil.toInt("42"));
		Assert.assertEquals(0, ConvertUtil.toInt(null));
		Assert.assertEquals(7, ConvertUtil.toInt("x", 7));
		Assert.assertEquals(42L, ConvertUtil.toLong("42"));
		Assert.assertEquals(3.14, ConvertUtil.toDouble("3.14"), 0.0001);
		Assert.assertEquals(5.0, ConvertUtil.toDouble(5), 0.0001);
		Assert.assertTrue(ConvertUtil.toBoolean("yes"));
		Assert.assertEquals("abc", ConvertUtil.toStr("abc"));
		Assert.assertNull(ConvertUtil.toStr(null));
		Assert.assertEquals("def", ConvertUtil.toStr(null, "def"));
		Assert.assertEquals("5", ConvertUtil.toStr(5));
	}

	@Test
	public void testConvertGeneric() {
		Assert.assertEquals(Integer.valueOf(42), ConvertUtil.convert(Integer.class, "42"));
		Assert.assertEquals("42", ConvertUtil.convert(String.class, 42));
		Assert.assertEquals(Double.valueOf(1.5), ConvertUtil.convert(Double.class, "1.5"));
		Assert.assertEquals(Boolean.TRUE, ConvertUtil.convert(Boolean.class, "on"));
	}

	@Test
	public void testConvertArrays() {
		Assert.assertArrayEquals(new String[]{"1", "2", "3"}, ConvertUtil.toStrArray(new int[]{1, 2, 3}));
		Assert.assertArrayEquals(new int[]{1, 2, 3}, ConvertUtil.toIntArray(new String[]{"1", "2", "3"}));
		Assert.assertArrayEquals(new long[]{1L, 2L}, ConvertUtil.toLongArray(new Integer[]{1, 2}));
		Assert.assertArrayEquals(new Object[]{1, "a"}, ConvertUtil.wrap(new Object[]{1, "a"}));
	}
}