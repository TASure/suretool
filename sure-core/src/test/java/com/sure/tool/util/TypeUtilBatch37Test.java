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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sure.tool.lang.TypeReference;

/**
 * {@link TypeUtil} 测试：六种 Type 规约、泛型实参解析、边界类型变量判断。
 */
public class TypeUtilBatch37Test {

	/** 带泛型参数的测试父类。 */
	static class Holder<T> {
	}

	/** {@code Holder<List<String>>}。 */
	static class StringListHolder extends Holder<List<String>> {
	}

	/** 无边界类型变量方法签名。 */
	static <T> T generic(T t) {
		return t;
	}

	@Test
	public void 直接Class原样返回() {
		assertEquals(String.class, TypeUtil.getClass(String.class));
	}

	@Test
	public void 参数化类型取原始类() {
		Type type = new TypeReference<List<String>>() {
		}.getType();
		assertEquals(List.class, TypeUtil.getClass(type));
	}

	@Test
	public void 泛型数组取组件数组类() {
		Type type = new TypeReference<List<String>[]>() {
		}.getType();
		Class<?> clazz = TypeUtil.getClass(type);
		assertTrue(clazz.isArray());
		assertEquals(List.class, clazz.getComponentType());
	}

	@Test
	public void 通配符取上界() {
		Type wild = new TypeReference<java.util.List<? extends Number>>() {
		}.getType();
		// List 的实参是 ? extends Number
		Class<?> arg = TypeUtil.getTypeArgument(wild, 0);
		assertEquals(Number.class, arg);
	}

	@Test
	public void 类型变量取首边界() throws Exception {
		java.lang.reflect.Method m = TypeUtilBatch37Test.class.getDeclaredMethod("generic", Object.class);
		TypeVariable<?> tv = m.getTypeParameters()[0];
		assertEquals(Object.class, TypeUtil.getClass(tv));
	}

	@Test
	public void null类型抛异常() {
		assertThrows(IllegalArgumentException.class, () -> TypeUtil.getClass(null));
	}

	@Test
	public void 取泛型实参() {
		Type type = new TypeReference<List<String>>() {
		}.getType();
		assertEquals(String.class, TypeUtil.getTypeArgument(type, 0));
	}

	@Test
	public void 越界实参兜底Object() {
		Type type = new TypeReference<List<String>>() {
		}.getType();
		assertEquals(Object.class, TypeUtil.getTypeArgument(type, 5));
		assertEquals(Object.class, TypeUtil.getTypeArgument(type, -1));
	}

	@Test
	public void 非参数化实参兜底Object() {
		assertEquals(Object.class, TypeUtil.getTypeArgument(String.class, 0));
	}

	@Test
	public void 取全部实参() {
		Type type = new TypeReference<Map<String, Integer>>() {
		}.getType();
		assertArrayEquals(new Class<?>[] {String.class, Integer.class},
				TypeUtil.getTypeArguments(type).toArray());
	}

	@Test
	public void 非参数化全部实参为空() {
		assertTrue(TypeUtil.getTypeArguments(String.class).isEmpty());
	}

	@Test
	public void 类型变量判断() throws Exception {
		java.lang.reflect.Method m = TypeUtilBatch37Test.class.getDeclaredMethod("generic", Object.class);
		TypeVariable<?> tv = m.getTypeParameters()[0];
		assertTrue(TypeUtil.isUnknown(tv));
		assertFalse(TypeUtil.isUnknown(String.class));
		assertFalse(TypeUtil.isUnknown(null));
	}

	@Test
	public void getType取引用() {
		TypeReference<List<String>> ref = new TypeReference<List<String>>() {
		};
		Type type = TypeUtil.getType(ref);
		assertEquals(List.class, TypeUtil.getClass(type));
		assertThrows(IllegalArgumentException.class, () -> TypeUtil.getType(null));
	}
}
