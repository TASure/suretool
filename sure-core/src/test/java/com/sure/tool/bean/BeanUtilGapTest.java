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
package com.sure.tool.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * BeanUtil 覆盖率补测：null 守卫、无 getter/setter 字段、无参构造缺失、类型转换异常路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class BeanUtilGapTest {

	/** 标准 Bean。 */
	public static class SimpleBean {
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

	/** 只有字段、无 getter/setter。 */
	public static class FieldOnlyBean {
		@SuppressWarnings("unused")
		private String secret = "s";
	}

	/** 无无参构造。 */
	public static class NoNoArgCtor {
		@SuppressWarnings("unused")
		private final String x;

		public NoNoArgCtor(String x) {
			this.x = x;
		}
	}

	/** setter 抛异常，用于命中 invoke 的 ReflectiveOperationException 包装。 */
	public static class ThrowingBean {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			if ("boom".equals(name)) {
				throw new IllegalStateException("x");
			}
			this.name = name;
		}
	}

	@Test
	public void testCopyPropertiesNull() {
		SimpleBean target = new SimpleBean();
		Assert.assertSame(target, BeanUtil.copyProperties(null, target));
		Assert.assertNull(BeanUtil.copyProperties(new SimpleBean(), null));
	}

	@Test
	public void testBeanToMapNull() {
		Assert.assertTrue(BeanUtil.beanToMap(null).isEmpty());
		SimpleBean b = new SimpleBean();
		b.setName("n");
		Map<String, Object> m = BeanUtil.beanToMap(b, true);
		Assert.assertEquals("n", m.get("name"));
	}

	@Test
	public void testMapToBeanNulls() {
		Assert.assertNull(BeanUtil.mapToBean(null, SimpleBean.class));
		Assert.assertNull(BeanUtil.mapToBean(new HashMap<>(), null));
		Map<String, Object> map = new HashMap<>();
		map.put("name", "n");
		map.put("age", 7);
		SimpleBean b = BeanUtil.mapToBean(map, SimpleBean.class);
		Assert.assertEquals("n", b.getName());
		Assert.assertEquals(7, b.getAge());
	}

	@Test
	public void testGetPropertyNullAndFieldFallback() {
		Assert.assertNull(BeanUtil.getProperty(null, "name"));
		Assert.assertNull(BeanUtil.getProperty(new SimpleBean(), null));
		// 字段回退：无 getter
		FieldOnlyBean fb = new FieldOnlyBean();
		Assert.assertEquals("s", BeanUtil.getProperty(fb, "secret"));
		Assert.assertNull(BeanUtil.getProperty(fb, "noSuch"));
		// 多级路径
		SimpleBean parent = new SimpleBean();
		parent.setName("p");
		Assert.assertEquals("p", BeanUtil.getProperty(parent, "name"));
		Assert.assertNull(BeanUtil.getProperty(parent, "a.b.c"));
	}

	@Test
	public void testSetPropertyNullAndFieldFallback() {
		SimpleBean b = new SimpleBean();
		BeanUtil.setProperty(null, "name", "x");
		BeanUtil.setProperty(b, null, "x");
		BeanUtil.setProperty(b, "name", "viaSetter");
		Assert.assertEquals("viaSetter", b.getName());
		// 字段回退：无 setter
		FieldOnlyBean fb = new FieldOnlyBean();
		BeanUtil.setProperty(fb, "secret", "changed");
		Assert.assertEquals("changed", BeanUtil.getProperty(fb, "secret"));
	}

	@Test
	public void testToBean() {
		Assert.assertNull(BeanUtil.toBean(null, SimpleBean.class));
		Assert.assertNull(BeanUtil.toBean(new SimpleBean(), null));
		Map<String, Object> map = new HashMap<>();
		map.put("name", "m");
		SimpleBean fromMap = BeanUtil.toBean(map, SimpleBean.class);
		Assert.assertEquals("m", fromMap.getName());
		SimpleBean src = new SimpleBean();
		src.setName("copy");
		SimpleBean copied = BeanUtil.toBean(src, SimpleBean.class);
		Assert.assertEquals("copy", copied.getName());
		Assert.assertNull(BeanUtil.toBean(42, SimpleBean.class));
		// 三参重载
		Assert.assertNull(BeanUtil.toBean(null, SimpleBean.class, true));
		SimpleBean copied2 = BeanUtil.toBean(src, SimpleBean.class, true);
		Assert.assertEquals("copy", copied2.getName());
	}

	@Test
	public void testIsBean() {
		Assert.assertFalse(BeanUtil.isBean(null));
		Assert.assertFalse(BeanUtil.isBean(int.class));
		Assert.assertFalse(BeanUtil.isBean(String.class));
		Assert.assertFalse(BeanUtil.isBean(Integer.class));
		Assert.assertTrue(BeanUtil.isBean(SimpleBean.class));
	}

	@Test
	public void testDeepCopy() {
		Assert.assertNull(BeanUtil.deepCopy(null));
		Assert.assertEquals("x", BeanUtil.deepCopy("x"));
		List<String> list = new ArrayList<>(List.of("a", "b"));
		List<String> copy = BeanUtil.deepCopy(list);
		Assert.assertEquals(list, copy);
		SimpleBean b = new SimpleBean();
		b.setName("d");
		SimpleBean bc = BeanUtil.deepCopy(b);
		Assert.assertEquals("d", bc.getName());
		// 无无参构造 -> IAE
		try {
			BeanUtil.deepCopy(new NoNoArgCtor("z"));
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getCause());
		}
	}

	@Test
	public void testFill() {
		SimpleBean b = new SimpleBean();
		Assert.assertNull(BeanUtil.fill(null, new HashMap<>()));
		Assert.assertSame(b, BeanUtil.fill(b, null));
		Map<String, Object> map = new HashMap<>();
		map.put("name", "f");
		map.put("age", 9);
		BeanUtil.fill(b, map);
		Assert.assertEquals("f", b.getName());
		Assert.assertEquals(9, b.getAge());
	}

	@Test
	public void testCopyToList() {
		Assert.assertNull(BeanUtil.copyToList(null, SimpleBean.class));
		Assert.assertNull(BeanUtil.copyToList(new ArrayList<>(), null));
		List<Object> src = new ArrayList<>();
		src.add(new SimpleBean());
		src.add(null);
		List<SimpleBean> out = BeanUtil.copyToList(src, SimpleBean.class);
		Assert.assertEquals(2, out.size());
		Assert.assertNull(out.get(1));
		// 三参
		List<SimpleBean> out2 = BeanUtil.copyToList(src, SimpleBean.class, false);
		Assert.assertEquals(2, out2.size());
	}

	@Test
	public void testMapToBeanIgnoreError() {
		Assert.assertNull(BeanUtil.mapToBean(null, SimpleBean.class, false));
		Assert.assertNull(BeanUtil.mapToBean(new HashMap<>(), null, true));
		Map<String, Object> bad = new HashMap<>();
		bad.put("name", "boom");
		// ignoreError=false -> setter 抛 RuntimeException，直接重抛（L501）
		try {
			BeanUtil.mapToBean(bad, ThrowingBean.class, false);
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e.getCause());
		}
		// ignoreError=true -> 捕获并跳过错误属性（L499-500）
		ThrowingBean ok = BeanUtil.mapToBean(bad, ThrowingBean.class, true);
		Assert.assertNotNull(ok);
		// 正常属性不受影响
		Map<String, Object> good = new HashMap<>();
		good.put("name", "fine");
		ThrowingBean fine = BeanUtil.mapToBean(good, ThrowingBean.class, true);
		Assert.assertEquals("fine", fine.getName());
	}

	/** 仅 public 字段、无 setter，命中 setProperty 字段回退。 */
	public static class FieldBean {
		@SuppressWarnings("unused")
		public String publicField;
	}

	@Test
	public void testNoNoArgCtorPaths() {
		// mapToBean 无无参构造 -> invokeConstructor 抛异常
		try {
			BeanUtil.mapToBean(new HashMap<>(), NoNoArgCtor.class);
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			// expected
		}
		// copyToList 含 null 元素 -> 列表中加入 null
		List<SimpleBean> src = new ArrayList<>();
		src.add(null);
		List<SimpleBean> r = BeanUtil.copyToList(src, SimpleBean.class, false);
		Assert.assertNull(r.get(0));
		Assert.assertEquals(1, r.size());
		// toBean 非 Bean 源 -> null
		Assert.assertNull(BeanUtil.toBean("str", SimpleBean.class));
	}

	@Test
	public void testSetPropertyFieldFallback() {
		FieldBean fb = new FieldBean();
		BeanUtil.setProperty(fb, "publicField", "v");
		Assert.assertEquals("v", fb.publicField);
	}
}
