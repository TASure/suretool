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
package com.sure.tool.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

/**
 * {@link TypeReference} 测试：泛型捕获、类型解析与非法子类化。
 */
public class TypeReferenceBatch37Test {

	@Test
	public void 捕获List泛型实参() {
		TypeReference<List<String>> ref = new TypeReference<List<String>>() {
		};
		Type type = ref.getType();
		assertNotNull(type);
		assertEquals("java.util.List<java.lang.String>", type.getTypeName());
		// getRawClass 推导
		assertEquals(List.class, ref.getRawClass());
	}

	@Test
	public void 捕获Map双参数取第一个() {
		TypeReference<java.util.Map<String, Integer>> ref = new TypeReference<java.util.Map<String, Integer>>() {
		};
		assertEquals(java.util.Map.class, ref.getRawClass());
	}

	@Test
	public void 无泛型实参子类化抛异常() {
		// 以 raw 类型子类化（getGenericSuperclass 非 ParameterizedType）
		assertThrows(IllegalArgumentException.class, TypeReferenceBare::new);
	}

	/** 无泛型实参的裸子类（raw 继承）。 */
	@SuppressWarnings("rawtypes")
	static class TypeReferenceBare extends TypeReference {
		TypeReferenceBare() {
			super();
		}
	}
}
