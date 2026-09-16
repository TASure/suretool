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
}
