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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.sure.tool.util.TypeUtil;

/**
 * 泛型类型引用，用于在运行时捕获并保存泛型实参类型。
 *
 * <p>典型用法：<pre>{@code
 * TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
 * TypeUtil.getTypeArgument(ref.getType(), 0); // String.class
 * }</pre>
 *
 * <p>参考 Hutool {@code TypeReference} 与 Jackson {@code TypeReference} 设计，
 * 仅支持单个泛型实参的子类化捕获。</p>
 *
 * @author suretool
 * @param <T> 目标类型
 * @since 1.18.0
 */
public abstract class TypeReference<T> {

	private final Type type;

	/**
	 * 构造器：解析子类的泛型实参类型。
	 *
	 * <p>要求子类以具体类型实例化（如 {@code new TypeReference<List<String>>() {}}），
	 * 且直接父类的泛型参数恰好一个；否则抛出 {@link IllegalArgumentException}。</p>
	 *
	 * @throws IllegalArgumentException 父类无泛型参数、参数个数不为 1 或父类非法
	 */
	protected TypeReference() {
		Type superType = getClass().getGenericSuperclass();
		if (!(superType instanceof ParameterizedType)
				|| ((ParameterizedType) superType).getActualTypeArguments().length != 1) {
			throw new IllegalArgumentException(
					"TypeReference 必须以单个具体泛型实参子类化: " + getClass().getName());
		}
		this.type = ((ParameterizedType) superType).getActualTypeArguments()[0];
	}

	/**
	 * 返回捕获的泛型类型。
	 *
	 * @return 泛型实参类型，恒非 null
	 */
	public Type getType() {
		return type;
	}

	/**
	 * 返回捕获泛型对应的运行时类。
	 *
	 * <p>若实参为类型变量等无法直接解析的场景，由 {@link TypeUtil#getClass(Type)} 推导。
	 * （命名 {@code getRawClass} 以避开 {@link Object#getClass()} 的 final 限制。）</p>
	 *
	 * @return 运行时类
	 */
	public Class<?> getRawClass() {
		return TypeUtil.getClass(type);
	}
}
