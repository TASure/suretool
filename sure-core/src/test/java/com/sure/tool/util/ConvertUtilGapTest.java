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
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * ConvertUtil 覆盖率补测：类型直返、空字符串、数组与 Map 转换分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ConvertUtilGapTest {

	public static class Pojo {
		private final String id;

		public Pojo(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	@Test
	public void testDirectReturns() {
		BigDecimal bd = new BigDecimal("1.5");
		Assert.assertSame(bd, ConvertUtil.toBigDecimal(bd));
		java.util.Date d = new java.util.Date(0L);
		Assert.assertSame(d, ConvertUtil.toDate(d));
		Assert.assertNull(ConvertUtil.toDate("  "));
		BigInteger bi = BigInteger.TEN;
		Assert.assertSame(bi, ConvertUtil.toBigInteger(bi));
		Assert.assertNull(ConvertUtil.toBigInteger("  "));
		java.time.LocalDateTime ldt = java.time.LocalDateTime.now();
		Assert.assertSame(ldt, ConvertUtil.toLocalDateTime(ldt));
		Assert.assertNull(ConvertUtil.toLocalDateTime("  "));
		Assert.assertNotNull(ConvertUtil.toLocalDateTime(java.time.LocalDate.of(2020, 1, 1)));
		Assert.assertNotNull(ConvertUtil.toLocalDateTime(0L));
	}

	@Test
	public void testArrays() {
		double[] da = new double[] {1.0d, 2.0d};
		Assert.assertSame(da, ConvertUtil.toDoubleArray(da));
		Assert.assertEquals(0, ConvertUtil.toDoubleArray(null).length);
		Assert.assertArrayEquals(new double[] {1, 2}, ConvertUtil.toDoubleArray("1,2"), 0.0001d);
		boolean[] ba = new boolean[] {true};
		Assert.assertSame(ba, ConvertUtil.toBooleanArray(ba));
		Assert.assertEquals(0, ConvertUtil.toBooleanArray(null).length);
		Assert.assertArrayEquals(new boolean[] {true, false}, ConvertUtil.toBooleanArray("true,false"));
	}

	@Test
	public void testToMap() {
		Map<String, Object> m = ConvertUtil.toMap((Object[]) null);
		Assert.assertTrue(m.isEmpty());
		try {
			ConvertUtil.toMap("a", 1, "b");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals(2, ConvertUtil.toMap("a", 1, "b", 2).size());
		Assert.assertTrue(ConvertUtil.toMap(null, "id").isEmpty());
		Map<String, Object> m2 = ConvertUtil.toMap(List.of(new Pojo("p1")), "id");
		Assert.assertTrue(m2.containsKey("p1"));
		try {
			ConvertUtil.toMap(List.of(new Pojo(null)), "id");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals(0, ConvertUtil.toDoubleArray(null).length);
		Assert.assertEquals(0, ConvertUtil.toBooleanArray(null).length);
	}
}
