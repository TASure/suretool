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
 * P4（v0.2.0）第十三批：JSONObject/JSONArray 类型取值补齐测试。
 */
public class P4JsonTypeTest {

	@Test
	public void testObjectTypeGet() {
		JSONObject obj = new JSONObject();
		obj.set("c", 'A');
		obj.set("s", 12);
		obj.set("b", 7);
		obj.set("f", 2.5F);
		obj.set("str", "X");

		Assert.assertEquals('A', obj.getChar("c"));
		Assert.assertEquals('X', obj.getChar("str"));
		Assert.assertEquals((char) 0, obj.getChar("missing"));
		Assert.assertEquals('Z', obj.getChar("missing", 'Z'));
		Assert.assertEquals((short) 12, obj.getShort("s"));
		Assert.assertEquals((short) 9, obj.getShort("missing", (short) 9));
		Assert.assertEquals((byte) 7, obj.getByte("b"));
		Assert.assertEquals((byte) 3, obj.getByte("missing", (byte) 3));
		Assert.assertEquals(2.5F, obj.getFloat("f"), 1e-6);
		Assert.assertEquals(1.5F, obj.getFloat("missing", 1.5F), 1e-6);
	}

	@Test
	public void testArrayTypeGet() {
		JSONArray arr = new JSONArray();
		arr.add("A");
		arr.add(12);
		arr.add(7);
		arr.add(2.5F);

		Assert.assertEquals('A', arr.getChar(0));
		Assert.assertEquals((char) 0, arr.getChar(9));
		Assert.assertEquals('Z', arr.getChar(9, 'Z'));
		Assert.assertEquals((short) 12, arr.getShort(1));
		Assert.assertEquals((short) 9, arr.getShort(9, (short) 9));
		Assert.assertEquals((byte) 7, arr.getByte(2));
		Assert.assertEquals((byte) 3, arr.getByte(9, (byte) 3));
		Assert.assertEquals(2.5F, arr.getFloat(3), 1e-6);
		Assert.assertEquals(1.5F, arr.getFloat(9, 1.5F), 1e-6);
	}

	@Test
	public void testGetDate() {
		JSONObject obj = new JSONObject();
		obj.set("ts", 1768550400000L); // 2026-01-16 08:00:00 UTC+8
		obj.set("str", "2026-01-16");
		java.util.Date d1 = obj.getDate("ts");
		java.util.Date d2 = obj.getDate("str");
		Assert.assertNotNull(d1);
		Assert.assertNotNull(d2);
		Assert.assertEquals(2026, 1900 + d1.getYear());
		Assert.assertEquals(2026, 1900 + d2.getYear());
		Assert.assertNull(obj.getDate("missing"));
		Assert.assertNull(obj.getDate("bad"));

		JSONArray arr = new JSONArray();
		arr.add(1768550400000L);
		Assert.assertNotNull(arr.getDate(0));
		Assert.assertNull(arr.getDate(9));
	}



	@Test
	public void testBigDecimal() {
		JSONObject obj = new JSONObject();
		obj.set("price", "19.90");
		Assert.assertEquals(new java.math.BigDecimal("19.90"), obj.getBigDecimal("price"));
		Assert.assertNull(obj.getBigDecimal("missing"));

		JSONArray arr = new JSONArray();
		arr.add(100);
		Assert.assertEquals(new java.math.BigDecimal("100"), arr.getBigDecimal(0));
		Assert.assertNull(arr.getBigDecimal(9));
	}



	@Test
	public void testToArrayAndList() {
		JSONArray arr = new JSONArray();
		arr.add(new JSONObject().set("name", "张三"));
		arr.add(new JSONObject().set("name", "李四"));
		java.util.List<com.sure.tool.json.TestUser> list = arr.toList(com.sure.tool.json.TestUser.class);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("张三", list.get(0).getName());
		com.sure.tool.json.TestUser[] array = arr.toArray(com.sure.tool.json.TestUser.class);
		Assert.assertEquals(2, array.length);
		Assert.assertEquals("李四", array[1].getName());
	}



	@Test
	public void testIsJsonKind() {
		Assert.assertTrue(JSONUtil.isJsonObj("{\"a\":1}"));
		Assert.assertFalse(JSONUtil.isJsonObj("[1,2]"));
		Assert.assertTrue(JSONUtil.isJsonArray("[1,2]"));
		Assert.assertFalse(JSONUtil.isJsonArray("{\"a\":1}"));
		Assert.assertFalse(JSONUtil.isJsonObj("not json"));
	}



	@Test
	public void testDeepClone() {
		JSONObject obj = new JSONObject();
		obj.set("name", "suretool");
		JSONObject nested = new JSONObject();
		nested.set("x", 1);
		obj.set("nested", nested);
		JSONArray arr = new JSONArray();
		arr.add(1);
		arr.add(2);
		obj.set("arr", arr);

		JSONObject copy = obj.deepClone();
		Assert.assertEquals("suretool", copy.getStr("name"));
		Assert.assertEquals(Integer.valueOf(1), copy.getJSONObject("nested").getInt("x"));
		Assert.assertEquals(2, copy.getJSONArray("arr").size());
		// 修改副本不影响原对象
		copy.getJSONObject("nested").set("x", 99);
		Assert.assertEquals(Integer.valueOf(1), obj.getJSONObject("nested").getInt("x"));
	}



	@Test
	public void testArrayDeepClone() {
		JSONArray arr = new JSONArray();
		JSONObject inner = new JSONObject();
		inner.set("k", "v");
		arr.add(inner);
		arr.add(1);

		JSONArray copy = arr.deepClone();
		Assert.assertEquals(2, copy.size());
		// 修改副本内层不影响原对象
		copy.getJSONObject(0).set("k", "changed");
		Assert.assertEquals("v", arr.getJSONObject(0).getStr("k"));
	}


}
