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

import java.io.Serializable;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * ObjectUtil 覆盖率补测：数组相等、null 守卫、序列化失败、长度与数字判断分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ObjectUtilGapTest {

	public static class NotSerializable {
	}

	public static class Ser implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String v;

		public Ser(String v) {
			this.v = v;
		}
	}

	@Test
	public void testEqualsArrays() {
		Assert.assertFalse(ObjectUtil.equals(new int[] {1, 2}, new int[] {1}));
		Assert.assertTrue(ObjectUtil.equals(new int[] {1, 2}, new int[] {1, 2}));
		Assert.assertTrue(ObjectUtil.equals(new String[] {"a"}, new String[] {"a"}));
	}

	@Test
	public void testAllNullNotNull() {
		Assert.assertFalse(ObjectUtil.isAllNotNull((Object[]) null));
		Assert.assertFalse(ObjectUtil.isAllNotNull("a", null));
		Assert.assertTrue(ObjectUtil.isAllNotNull("a", "b"));
		Assert.assertTrue(ObjectUtil.isAllNull((Object[]) null));
		Assert.assertTrue(ObjectUtil.isAllNull(null, null));
		Assert.assertFalse(ObjectUtil.isAllNull(null, "x"));
	}

	@Test
	public void testBasicTypeIdentity() {
		Assert.assertFalse(ObjectUtil.isBasicType((Class<?>) null));
		Assert.assertTrue(ObjectUtil.isBasicType(String.class));
		Assert.assertEquals("null", ObjectUtil.identityToString(null));
		Assert.assertTrue(ObjectUtil.identityToString("x").startsWith("java.lang.String@"));
	}

	@Test
	public void testSerializeDeserialize() {
		Assert.assertNull(ObjectUtil.serialize(new NotSerializable()));
		Assert.assertNull(ObjectUtil.deserialize(null));
		Assert.assertNull(ObjectUtil.deserialize(new byte[0]));
		Assert.assertNull(ObjectUtil.deserialize(new byte[] {1, 2, 3}));
		byte[] bytes = ObjectUtil.serialize(new Ser("hi"));
		Assert.assertNotNull(bytes);
		Object back = ObjectUtil.deserialize(bytes);
		Assert.assertTrue(back instanceof Ser);
	}

	@Test
	public void testLengthAndValidNumber() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		Assert.assertEquals(1, ObjectUtil.length(map));
		Assert.assertEquals(0, ObjectUtil.length(null));
		Assert.assertFalse(ObjectUtil.isValidIfNumber(new Object()));
		Assert.assertFalse(ObjectUtil.isValidIfNumber(null));
		Assert.assertTrue(ObjectUtil.isValidIfNumber("3.14"));
		Assert.assertFalse(ObjectUtil.isValidIfNumber("abc"));
		Assert.assertEquals("def", ObjectUtil.defaultIfEmpty("", "def"));
		Assert.assertEquals("x", ObjectUtil.defaultIfNull(null, "x"));
	}

	@Test
	public void testNotSerializable() {
		Object ns = new Object();
		Assert.assertNull(ObjectUtil.cloneByStream(ns));
		Assert.assertNull(ObjectUtil.serialize(ns));
	}

	public static class NoPublicClone implements Cloneable {
	}

	@Test
	public void testCloneNoPublicMethod() {
		Assert.assertNull(ObjectUtil.clone(new NoPublicClone()));
	}
}
