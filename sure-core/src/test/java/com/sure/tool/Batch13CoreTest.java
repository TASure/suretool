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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.RandomUtil;
import com.sure.tool.util.ReflectUtil;
import com.sure.tool.util.StrUtil;

/**
 * 批13：核心工具类高频方法补齐测试。
 */
public class Batch13CoreTest {

	/** 测试 Bean：带继承字段与重载方法。 */
	public static class ParentBean {
		private String parentName;
		public String getParentName() {
			return parentName;
		}
		public void setParentName(String parentName) {
			this.parentName = parentName;
		}
	}

	public static class ChildBean extends ParentBean {
		private String name;
		private int age;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public String concat(String a, String b) {
			return a + b;
		}
		public String concat(String a) {
			return "single:" + a;
		}
	}

	/** 泛型父类解析用。 */
	public static abstract class GenericBase<T, R> {
		public T value;
	}

	public static class GenericChild extends GenericBase<String, Integer> {
	}

	/** 多级路径用。 */
	public static class Address {
		private String city;
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
	}

	public static class User {
		private String name;
		private Address address;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Address getAddress() {
			return address;
		}
		public void setAddress(Address address) {
			this.address = address;
		}
	}

	// ---------- ReflectUtil ----------

	@Test
	public void testGetClassByName() {
		assertSame(int.class, ReflectUtil.getClassByName("int"));
		assertSame(Integer.class, ReflectUtil.getClassByName("Integer"));
		assertSame(String.class, ReflectUtil.getClassByName("String"));
		assertSame(StrUtil.class, ReflectUtil.getClassByName(StrUtil.class.getName()));
		try {
			ReflectUtil.getClassByName("com.sure.tool.NoSuchClass");
			fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void testGetMethodByName() {
		Method method = ReflectUtil.getMethodByName(ChildBean.class, "concat");
		assertNotNull(method);
		assertEquals("concat", method.getName());
		// 继承方法
		assertNotNull(ReflectUtil.getMethodByName(ChildBean.class, "getParentName"));
		assertNull(ReflectUtil.getMethodByName(ChildBean.class, "notExist"));
	}

	@Test
	public void testGetTypeArguments() {
		Map<String, Class<?>> map = ReflectUtil.getTypeArguments(GenericChild.class);
		assertEquals(String.class, map.get("T"));
		assertEquals(Integer.class, map.get("R"));
	}

	@Test
	public void testGetFieldMapAndHasField() {
		Map<String, Field> fields = ReflectUtil.getFieldMap(ChildBean.class);
		assertTrue(fields.containsKey("name"));
		assertTrue(fields.containsKey("parentName"));
		assertTrue(ReflectUtil.hasField(ChildBean.class, "parentName"));
		assertTrue(ReflectUtil.hasField(ChildBean.class, "PARENTNAME", true));
		assertFalse(ReflectUtil.hasField(ChildBean.class, "notExist"));
	}

	@Test
	public void testInvokeWithExplicitTypes() {
		ChildBean bean = new ChildBean();
		Object result = ReflectUtil.invoke(bean, "concat", new Class<?>[] {String.class, String.class}, "a", "b");
		assertEquals("ab", result);
	}

	@Test
	public void testSetAccessible() {
		Field field = ReflectUtil.getField(ChildBean.class, "name");
		assertNotNull(field);
		assertSame(field, ReflectUtil.setAccessible(field));
		assertNull(ReflectUtil.setAccessible(null));
	}

	// ---------- ConvertUtil ----------

	@Test
	public void testToMapKeyValues() {
		Map<String, Object> map = ConvertUtil.toMap("a", 1, "b", 2);
		assertEquals(2, map.size());
		assertEquals(1, map.get("a"));
		try {
			ConvertUtil.toMap("a", 1, "b");
			fail("奇数元素应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
		Map<String, Object> nullKeyMap = ConvertUtil.toMap(null, 1, "b", 2);
		assertEquals(1, nullKeyMap.size());
	}

	@Test
	public void testToMapByKeyField() {
		ChildBean a = new ChildBean();
		a.setName("a");
		ChildBean b = new ChildBean();
		b.setName("b");
		Map<String, Object> map = ConvertUtil.toMap(Arrays.asList(a, b), "name");
		assertEquals(2, map.size());
		assertSame(a, map.get("a"));
	}

	// ---------- RandomUtil ----------

	@Test
	public void testRandomLetter() {
		String s = RandomUtil.randomLetter(8);
		assertEquals(8, s.length());
		for (char c : s.toCharArray()) {
			assertTrue(c >= 'a' && c <= 'z');
		}
		assertEquals("", RandomUtil.randomLetter(0));
	}

	@Test
	public void testRandomChinese() {
		String s = RandomUtil.randomChinese(5);
		assertEquals(5, s.length());
		for (char c : s.toCharArray()) {
			assertTrue(c >= 0x4E00 && c <= 0x9FFF);
		}
	}

	@Test
	public void testRandomStringWithChars() {
		char[] chars = "XY".toCharArray();
		String s = RandomUtil.randomString(10, chars);
		assertEquals(10, s.length());
		for (char c : s.toCharArray()) {
			assertTrue(c == 'X' || c == 'Y');
		}
		assertNotNull(RandomUtil.randomString(4, null));
		assertEquals("", RandomUtil.randomString(0, chars));
	}

	// ---------- CollUtil ----------

	@Test
	public void testRotate() {
		List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		CollUtil.rotate(list, 2);
		assertEquals(Arrays.asList(4, 5, 1, 2, 3), list);
		CollUtil.rotate(list, -2);
		assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
	}

	@Test
	public void testRemoveNullAndEmpty() {
		List<String> list = new ArrayList<>(Arrays.asList("a", null, "", "b", " "));
		List<String> noNull = CollUtil.removeNull(list);
		assertEquals(Arrays.asList("a", "", "b", " "), noNull);
		CollUtil.removeEmpty(list);
		assertEquals(Arrays.asList("a", "b", " "), list);
	}

	@Test
	public void testRemoveAny() {
		List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
		CollUtil.removeAny(list, 2, 4);
		assertEquals(Arrays.asList(1, 3), list);
	}

	@Test
	public void testIsAllEmptyAndNotNull() {
		assertTrue(CollUtil.isAllEmpty(new ArrayList<>(), new ArrayList<>()));
		assertFalse(CollUtil.isAllEmpty(new ArrayList<>(), Arrays.asList(1)));
		assertTrue(CollUtil.isAllNotNull(Arrays.asList(1), Arrays.asList(2)));
		assertFalse(CollUtil.isAllNotNull(Arrays.asList(1), new ArrayList<>()));
	}

	// ---------- BeanUtil ----------

	@Test
	public void testCopyToList() {
		ChildBean a = new ChildBean();
		a.setName("a");
		a.setAge(1);
		List<ChildBean> list = BeanUtil.copyToList(Arrays.asList(a), ChildBean.class);
		assertEquals(1, list.size());
		assertEquals("a", list.get(0).getName());
		assertFalse(list.get(0) == a);
	}

	@Test
	public void testToBeanIgnoreNull() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("name", "x");
		map.put("age", null);
		ChildBean bean = BeanUtil.toBean(map, ChildBean.class, false);
		assertEquals("x", bean.getName());
	}

	@Test
	public void testMapToBeanIgnoreError() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("age", "not-a-number");
		ChildBean bean = BeanUtil.mapToBean(map, ChildBean.class, true);
		assertNotNull(bean);
	}

	@Test
	public void testGetPropertyMultiLevel() {
		User user = new User();
		Address address = new Address();
		address.setCity("西安");
		user.setAddress(address);
		user.setName("TASure");
		assertEquals("西安", BeanUtil.getProperty(user, "address.city"));
		assertEquals("TASure", BeanUtil.getProperty(user, "name"));
		assertNull(BeanUtil.getProperty(user, "address.province"));
		// 中间 null 安全返回 null
		User empty = new User();
		assertNull(BeanUtil.getProperty(empty, "address.city"));
	}
}
