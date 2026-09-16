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
 * P4（v0.2.0）第三十五批测试：数组转换/去重/空数组、布尔异或、空值转 null、十六进制判断、随机小数、线程数。
 */
public class P4Features30Test {

	@Test
	public void testArrayConvert() {
		java.util.List<String> list = java.util.List.of("a", "b", "c");
		String[] arr = com.sure.tool.util.ArrayUtil.toArray(list, String.class);
		Assert.assertEquals(3, arr.length);
		Assert.assertEquals("a", arr[0]);
		Assert.assertEquals("c", arr[2]);
		Assert.assertNull(com.sure.tool.util.ArrayUtil.toArray(null, String.class));

		String[] dup = new String[] { "a", "b", "a", "c", "b" };
		String[] uniq = com.sure.tool.util.ArrayUtil.distinct(dup);
		Assert.assertEquals(3, uniq.length);
		Assert.assertEquals("a", uniq[0]);
		Assert.assertEquals("b", uniq[1]);
		Assert.assertEquals("c", uniq[2]);
		Assert.assertNull(com.sure.tool.util.ArrayUtil.distinct(null));

		Integer[] empty = com.sure.tool.util.ArrayUtil.empty(Integer.class);
		Assert.assertNotNull(empty);
		Assert.assertEquals(0, empty.length);
		Assert.assertEquals(Integer.class, empty.getClass().getComponentType());
	}

	@Test
	public void testBooleanXor() {
		Assert.assertFalse(com.sure.tool.util.BooleanUtil.xor());
		Assert.assertFalse(com.sure.tool.util.BooleanUtil.xor((boolean[]) null));
		Assert.assertTrue(com.sure.tool.util.BooleanUtil.xor(true));
		Assert.assertFalse(com.sure.tool.util.BooleanUtil.xor(true, true));
		Assert.assertTrue(com.sure.tool.util.BooleanUtil.xor(true, true, true));
		Assert.assertFalse(com.sure.tool.util.BooleanUtil.xor(false, false, false));
		Assert.assertTrue(com.sure.tool.util.BooleanUtil.xor(true, false, false));
	}

	@Test
	public void testObjectEmptyToNullAndNumber() {
		Assert.assertNull(com.sure.tool.util.ObjectUtil.emptyToNull(null));
		Assert.assertNull(com.sure.tool.util.ObjectUtil.emptyToNull(""));
		Assert.assertNull(com.sure.tool.util.ObjectUtil.emptyToNull(new java.util.ArrayList<>()));
		Assert.assertNull(com.sure.tool.util.ObjectUtil.emptyToNull(new java.util.HashMap<>()));
		Assert.assertEquals("x", com.sure.tool.util.ObjectUtil.emptyToNull("x"));
		Assert.assertEquals(Integer.valueOf(1), com.sure.tool.util.ObjectUtil.emptyToNull(1));

		Assert.assertFalse(com.sure.tool.util.ObjectUtil.isValidIfNumber(null));
		Assert.assertFalse(com.sure.tool.util.ObjectUtil.isValidIfNumber(""));
		Assert.assertFalse(com.sure.tool.util.ObjectUtil.isValidIfNumber("abc"));
		Assert.assertFalse(com.sure.tool.util.ObjectUtil.isValidIfNumber("   "));
		Assert.assertTrue(com.sure.tool.util.ObjectUtil.isValidIfNumber(42));
		Assert.assertTrue(com.sure.tool.util.ObjectUtil.isValidIfNumber(3.14D));
		Assert.assertTrue(com.sure.tool.util.ObjectUtil.isValidIfNumber("123"));
		Assert.assertTrue(com.sure.tool.util.ObjectUtil.isValidIfNumber("-2.5"));
	}

	@Test
	public void testHexNumberAndMisc() {
		Assert.assertTrue(com.sure.tool.util.HexUtil.isHexNumber("1a2b"));
		Assert.assertTrue(com.sure.tool.util.HexUtil.isHexNumber("0x1A2B"));
		Assert.assertTrue(com.sure.tool.util.HexUtil.isHexNumber("0Xff"));
		Assert.assertTrue(com.sure.tool.util.HexUtil.isHexNumber("123456"));
		Assert.assertFalse(com.sure.tool.util.HexUtil.isHexNumber("1g2b"));
		Assert.assertFalse(com.sure.tool.util.HexUtil.isHexNumber("0x"));
		Assert.assertFalse(com.sure.tool.util.HexUtil.isHexNumber(""));
		Assert.assertFalse(com.sure.tool.util.HexUtil.isHexNumber(null));

		java.math.BigDecimal start = new java.math.BigDecimal("10.00");
		java.math.BigDecimal end = new java.math.BigDecimal("10.00");
		java.math.BigDecimal v = com.sure.tool.util.RandomUtil.randomBigDecimal(start, end);
		Assert.assertEquals(0, v.compareTo(start));
		java.math.BigDecimal v2 = com.sure.tool.util.RandomUtil.randomBigDecimal(
				new java.math.BigDecimal("0"), new java.math.BigDecimal("100"));
		Assert.assertTrue(v2.compareTo(java.math.BigDecimal.ZERO) >= 0);
		Assert.assertTrue(v2.compareTo(new java.math.BigDecimal("100")) <= 0);
		try {
			com.sure.tool.util.RandomUtil.randomBigDecimal(new java.math.BigDecimal("5"),
					new java.math.BigDecimal("1"));
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}

		Assert.assertTrue(com.sure.tool.util.SystemUtil.getTotalThreadCount() > 0);
	}


}
