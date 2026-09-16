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
 * P4（v0.2.0）第三十一批测试。
 */
public class P4Features26Test {

	@Test
	public void testCollSortAndIndex() {
		java.util.List<FillBean> list = new java.util.ArrayList<>();
		list.add(newFillBean("a", 30));
		list.add(newFillBean("b", 20));
		list.add(newFillBean("c", 25));

		java.util.List<FillBean> asc = com.sure.tool.collection.CollUtil.sortByProperty(list, "age");
		Assert.assertEquals(20, asc.get(0).getAge());
		java.util.List<FillBean> desc = com.sure.tool.collection.CollUtil.sortByPropertyDesc(list, "age");
		Assert.assertEquals(30, desc.get(0).getAge());

		java.util.List<String> sl = java.util.List.of("x", "y", "x");
		Assert.assertEquals(0, com.sure.tool.collection.CollUtil.indexOf(sl, "x"));
		Assert.assertEquals(2, com.sure.tool.collection.CollUtil.lastIndexOf(sl, "x"));
		Assert.assertEquals(-1, com.sure.tool.collection.CollUtil.indexOf(sl, "z"));
		Assert.assertEquals(-1, com.sure.tool.collection.CollUtil.indexOf(null, "x"));
		Assert.assertEquals(-1, com.sure.tool.collection.CollUtil.lastIndexOf(null, "x"));
	}

	private static FillBean newFillBean(String name, int age) {
		FillBean b = new FillBean();
		b.setName(name);
		b.setAge(age);
		return b;
	}

	@Test
	public void testMapSortByValueDesc() {
		java.util.Map<String, Integer> m = new java.util.HashMap<>();
		m.put("a", 1);
		m.put("b", 3);
		m.put("c", 2);
		java.util.Map<String, Integer> desc = com.sure.tool.collection.MapUtil.sortByValueDesc(m);
		java.util.List<String> keys = new java.util.ArrayList<>(desc.keySet());
		Assert.assertEquals("b", keys.get(0));
		Assert.assertEquals("c", keys.get(1));
		Assert.assertEquals("a", keys.get(2));
	}

	@Test
	public void testBeanFill() {
		FillBean user = new FillBean();
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("name", "Sure");
		map.put("age", 18);
		map.put("unknown", "skip");
		com.sure.tool.bean.BeanUtil.fill(user, map);
		Assert.assertEquals("Sure", user.getName());
		Assert.assertEquals(18, user.getAge());
		com.sure.tool.bean.BeanUtil.fill(user, null);
		Assert.assertNull(com.sure.tool.bean.BeanUtil.fill(null, map));
	}

	/** 测试用内部 Bean。 */
	public static class FillBean {
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
	}


}
