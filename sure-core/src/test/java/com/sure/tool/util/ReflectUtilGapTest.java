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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * ReflectUtil 覆盖率补测：null 守卫、继承链、泛型解析、静态/实例方法调用异常路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ReflectUtilGapTest {

	/** 父类：含私有字段、实例 final 字段（set 抛 IllegalAccessException）、抛异常方法。 */
	public static class Parent {
		/** 实例 final：field.set 即使 setAccessible(true) 仍抛 IllegalAccessException */
		@SuppressWarnings("unused")
		public final String finalField = "f";
		private String parentField = "p";

		public void throwsOnCall() {
			throw new IllegalStateException("boom");
		}

		public static String staticOk() {
			return "ok";
		}

		public static void staticThrows() {
			throw new IllegalStateException("boom");
		}

		public int primitiveArg(int x) {
			return x;
		}

		public String stringArg(String s) {
			return s;
		}
	}

	/** 子类：继承父类字段。 */
	public static class Child extends Parent {
		@SuppressWarnings("unused")
		private String childField = "c";
	}

	/** 接口默认方法。 */
	public interface Face {
		default String faceMethod() {
			return "face";
		}
	}

	/** 实现接口但不重写默认方法。 */
	public static class FaceImpl implements Face {
	}

	/** 泛型父类（Class 实际参数）。 */
	public static class GenBase<T> {
	}

	/** 直链 Class 实际参数。 */
	public static class Concrete extends GenBase<String> {
	}

	/** 泛型父类（ParameterizedType 实际参数）。 */
	public static class PBase<T> {
	}

	/** List<String> 作为实际参数。 */
	public static class ConcreteP extends PBase<List<String>> {
	}

	/** record：final 字段即使 setAccessible(true) 仍禁止 set。 */
	public record Rec(int x) {
	}

	@Test
	public void testGetField() {
		Assert.assertNull(ReflectUtil.getField(null, "x"));
		Assert.assertNull(ReflectUtil.getField(Child.class, null));
		Assert.assertNotNull(ReflectUtil.getField(Child.class, "childField"));
		// 继承父类字段
		Assert.assertNotNull(ReflectUtil.getField(Child.class, "parentField"));
		Assert.assertNull(ReflectUtil.getField(Child.class, "noSuch"));
	}

	@Test
	public void testGetSetFieldValue() {
		Child c = new Child();
		Assert.assertEquals("c", ReflectUtil.getFieldValue(c, "childField"));
		Assert.assertEquals("p", ReflectUtil.getFieldValue(c, "parentField"));
		try {
			ReflectUtil.getFieldValue(c, "noSuch");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		ReflectUtil.setFieldValue(c, "childField", "newC");
		Assert.assertEquals("newC", ReflectUtil.getFieldValue(c, "childField"));
		// record 的 final 字段：set 时抛 IllegalAccessException 包装为 IAE
		try {
			ReflectUtil.setFieldValue(new Rec(1), "x", 99);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getCause() instanceof IllegalAccessException);
		}
	}

	@Test
	public void testGetFieldsMethods() {
		Assert.assertEquals(0, ReflectUtil.getFields(null).length);
		Assert.assertTrue(ReflectUtil.getFields(Child.class).length >= 2);
		Assert.assertEquals(0, ReflectUtil.getMethods(null).length);
		Assert.assertTrue(ReflectUtil.getMethods(Child.class).length > 0);
		Assert.assertNull(ReflectUtil.getMethod(null, "x"));
		Assert.assertNull(ReflectUtil.getMethod(Child.class, null));
		Assert.assertNull(ReflectUtil.getMethod(Child.class, "noSuch"));
		Assert.assertNotNull(ReflectUtil.getMethod(Child.class, "stringArg", String.class));
	}

	@Test
	public void testInvokeAndStatic() {
		Child c = new Child();
		Assert.assertEquals("ab", ReflectUtil.invoke(c, "stringArg", "ab"));
		try {
			ReflectUtil.invoke(null, "x");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ReflectUtil.invoke(c, "noSuch");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		// 目标方法抛异常 -> InvocationTargetException 包装
		try {
			ReflectUtil.invoke(c, "throwsOnCall");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getCause());
		}
		Assert.assertEquals("ok", ReflectUtil.invokeStatic(Parent.class, "staticOk"));
		try {
			ReflectUtil.invokeStatic(null, "x");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		// 静态方法不存在或非静态
		try {
			ReflectUtil.invokeStatic(Parent.class, "stringArg", "a");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ReflectUtil.invokeStatic(Parent.class, "staticThrows");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getCause());
		}
	}

	@Test
	public void testInvokeExactParamTypes() {
		Child c = new Child();
		try {
			ReflectUtil.invoke(null, "x", new Class<?>[0]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ReflectUtil.invoke(c, "noSuch", new Class<?>[0]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals("ab", ReflectUtil.invoke(c, "stringArg", new Class<?>[] {String.class}, "ab"));
		try {
			ReflectUtil.invoke(c, "throwsOnCall", new Class<?>[0]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getCause());
		}
	}

	@Test
	public void testFindMethodBranches() {
		Child c = new Child();
		// 参数个数不匹配
		try {
			ReflectUtil.invoke(c, "stringArg", "a", "b");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		// 基本类型参数收到 null -> 不匹配
		try {
			ReflectUtil.invoke(c, "primitiveArg", new Object[] {null});
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		// 参数类型不可赋值 -> 不匹配
		try {
			ReflectUtil.invoke(c, "stringArg", new Object[] {123});
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals(5, ReflectUtil.invoke(c, "primitiveArg", 5));
	}

	@Test
	public void testGetClassByName() {
		try {
			ReflectUtil.getClassByName(null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals(int.class, ReflectUtil.getClassByName("int"));
		Assert.assertEquals(long.class, ReflectUtil.getClassByName("long"));
		Assert.assertEquals(double.class, ReflectUtil.getClassByName("double"));
		Assert.assertEquals(float.class, ReflectUtil.getClassByName("float"));
		Assert.assertEquals(boolean.class, ReflectUtil.getClassByName("boolean"));
		Assert.assertEquals(char.class, ReflectUtil.getClassByName("char"));
		Assert.assertEquals(byte.class, ReflectUtil.getClassByName("byte"));
		Assert.assertEquals(short.class, ReflectUtil.getClassByName("short"));
		Assert.assertEquals(void.class, ReflectUtil.getClassByName("void"));
		Assert.assertEquals(Integer.class, ReflectUtil.getClassByName("Integer"));
		Assert.assertEquals(Long.class, ReflectUtil.getClassByName("Long"));
		Assert.assertEquals(Double.class, ReflectUtil.getClassByName("Double"));
		Assert.assertEquals(Float.class, ReflectUtil.getClassByName("Float"));
		Assert.assertEquals(Boolean.class, ReflectUtil.getClassByName("Boolean"));
		Assert.assertEquals(Character.class, ReflectUtil.getClassByName("Character"));
		Assert.assertEquals(Byte.class, ReflectUtil.getClassByName("Byte"));
		Assert.assertEquals(Short.class, ReflectUtil.getClassByName("Short"));
		Assert.assertEquals(Void.class, ReflectUtil.getClassByName("Void"));
		Assert.assertEquals(String.class, ReflectUtil.getClassByName("String"));
		Assert.assertEquals(Runnable.class, ReflectUtil.getClassByName("java.lang.Runnable"));
		try {
			ReflectUtil.getClassByName("no.such.Class");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getCause());
		}
	}

	@Test
	public void testGetMethodByName() {
		Assert.assertNull(ReflectUtil.getMethodByName(null, "x"));
		Assert.assertNull(ReflectUtil.getMethodByName(Child.class, null));
		Assert.assertNotNull(ReflectUtil.getMethodByName(Child.class, "stringArg"));
		Assert.assertNull(ReflectUtil.getMethodByName(Child.class, "noSuch"));
		// 接口默认方法递归查找
		Assert.assertNotNull(ReflectUtil.getMethodByName(FaceImpl.class, "faceMethod"));
	}

	@Test
	public void testGetTypeArguments() {
		Assert.assertTrue(ReflectUtil.getTypeArguments(null).isEmpty());
		Map<String, Class<?>> m = ReflectUtil.getTypeArguments(Concrete.class);
		Assert.assertEquals(String.class, m.get("T"));
		Map<String, Class<?>> m2 = ReflectUtil.getTypeArguments(ConcreteP.class);
		Assert.assertEquals(List.class, m2.get("T"));
		Assert.assertTrue(ReflectUtil.getTypeArguments(Child.class).isEmpty());
	}

	@Test
	public void testGetFieldMapHasField() {
		Assert.assertTrue(ReflectUtil.getFieldMap(null).isEmpty());
		Assert.assertTrue(ReflectUtil.getFieldMap(Child.class).containsKey("childField"));
		Assert.assertFalse(ReflectUtil.hasField(null, "x"));
		Assert.assertFalse(ReflectUtil.hasField(Child.class, null));
		Assert.assertTrue(ReflectUtil.hasField(Child.class, "childField"));
		Assert.assertTrue(ReflectUtil.hasField(Child.class, "CHILDFIELD", true));
		Assert.assertFalse(ReflectUtil.hasField(Child.class, "noSuch"));
	}

	@Test
	public void testSetAccessible() throws Exception {
		Field f = Parent.class.getDeclaredField("parentField");
		Assert.assertFalse(f.isAccessible());
		ReflectUtil.setAccessible(f);
		Assert.assertTrue(f.isAccessible());
		// 已 accessible 直接返回
		ReflectUtil.setAccessible(f);
		Assert.assertNull(ReflectUtil.setAccessible(null));
	}

	@Test
	public void testInvokeNullObj() {
		try {
			ReflectUtil.invoke(null, "x", new Class<?>[0], new Object[0]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
