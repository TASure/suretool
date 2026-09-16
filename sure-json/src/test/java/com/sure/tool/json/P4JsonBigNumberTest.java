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
package com.sure.tool.json;

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第三十六批：JSONObject/JSONArray BigDecimal/BigInteger 取值（含默认值）。
 */
public class P4JsonBigNumberTest {

	@Test
	public void testObjectBigDecimalAndBigInteger() {
		JSONObject obj = new JSONObject()
				.set("amount", "1234.56")
				.set("big", "99999999999999999999999999")
				.set("num", 42);

		Assert.assertEquals(new java.math.BigDecimal("1234.56"), obj.getBigDecimal("amount"));
		Assert.assertEquals(new java.math.BigDecimal("1234.56"),
				obj.getBigDecimal("amount", new java.math.BigDecimal("9.99")));
		Assert.assertEquals(new java.math.BigDecimal("9.99"),
				obj.getBigDecimal("missing", new java.math.BigDecimal("9.99")));
		Assert.assertNull(obj.getBigDecimal("missing"));

		Assert.assertEquals(new java.math.BigInteger("99999999999999999999999999"), obj.getBigInteger("big"));
		Assert.assertEquals(java.math.BigInteger.valueOf(42), obj.getBigInteger("num"));
		Assert.assertEquals(java.math.BigInteger.TEN, obj.getBigInteger("missing", java.math.BigInteger.TEN));
		Assert.assertNull(obj.getBigInteger("missing"));
	}

	@Test
	public void testArrayBigDecimalAndBigInteger() {
		JSONArray arr = JSONArray.parse("[1234.56, \"99999999999999999999999999\", 7]");

		Assert.assertEquals(new java.math.BigDecimal("1234.56"), arr.getBigDecimal(0));
		Assert.assertEquals(new java.math.BigDecimal("1.00"), arr.getBigDecimal(99, new java.math.BigDecimal("1.00")));
		Assert.assertEquals(new java.math.BigDecimal("1234.56"),
				arr.getBigDecimal(0, new java.math.BigDecimal("1.00")));
		Assert.assertNull(arr.getBigDecimal(99));

		Assert.assertEquals(new java.math.BigInteger("99999999999999999999999999"), arr.getBigInteger(1));
		Assert.assertEquals(java.math.BigInteger.valueOf(7), arr.getBigInteger(2));
		Assert.assertEquals(java.math.BigInteger.ONE, arr.getBigInteger(-1, java.math.BigInteger.ONE));
		Assert.assertNull(arr.getBigInteger(-1));
	}

	@Test
	public void testBigNumberRoundTrip() {
		JSONObject obj = new JSONObject();
		obj.set("amount", new java.math.BigDecimal("3.14"));
		obj.set("big", new java.math.BigInteger("123456789012345678901234567890"));
		JSONObject parsed = JSONObject.parse(obj.toJsonString());
		Assert.assertEquals(new java.math.BigDecimal("3.14"), parsed.getBigDecimal("amount"));
		Assert.assertEquals(new java.math.BigInteger("123456789012345678901234567890"),
				parsed.getBigInteger("big"));
	}
}
