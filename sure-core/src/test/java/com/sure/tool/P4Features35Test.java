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
 * P4（v0.2.0）第四十批测试：Bean 统一转换、Dict 大数取值。
 */
public class P4Features35Test {

	public static class Person {
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

	@Test
	public void testBeanToBean() {
		Person source = new Person();
		source.setName("张三");
		source.setAge(30);
		Person target = com.sure.tool.bean.BeanUtil.toBean(source, Person.class);
		Assert.assertNotNull(target);
		Assert.assertEquals("张三", target.getName());
		Assert.assertEquals(30, target.getAge());
		Assert.assertNotSame(source, target);

		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("name", "李四");
		map.put("age", 25);
		Person fromMap = com.sure.tool.bean.BeanUtil.toBean(map, Person.class);
		Assert.assertEquals("李四", fromMap.getName());
		Assert.assertEquals(25, fromMap.getAge());

		Assert.assertNull(com.sure.tool.bean.BeanUtil.toBean(null, Person.class));
		Assert.assertNull(com.sure.tool.bean.BeanUtil.toBean("str", Person.class));
	}

	@Test
	public void testDictBigNumber() {
		com.sure.tool.lang.Dict dict = com.sure.tool.lang.Dict.of()
				.set("amount", "1234.56")
				.set("big", "99999999999999999999999999")
				.set("num", 42);

		Assert.assertEquals(new java.math.BigDecimal("1234.56"), dict.getBigDecimal("amount"));
		Assert.assertEquals(new java.math.BigDecimal("9.99"),
				dict.getBigDecimal("missing", new java.math.BigDecimal("9.99")));
		Assert.assertNull(dict.getBigDecimal("missing"));

		Assert.assertEquals(new java.math.BigInteger("99999999999999999999999999"), dict.getBigInteger("big"));
		Assert.assertEquals(java.math.BigInteger.valueOf(42), dict.getBigInteger("num"));
		Assert.assertEquals(java.math.BigInteger.TEN,
				dict.getBigInteger("missing", java.math.BigInteger.TEN));
		Assert.assertNull(dict.getBigInteger("missing"));
	}
}
