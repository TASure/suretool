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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sure.tool.lang.TypeReference;

/**
 * 泛型类型解析工具，参考 Hutool {@code TypeUtil} 与 Guava {@code TypeToken} 设计。
 *
 * <p>用于在运行时解析 {@code List<String>} 这类参数化类型的泛型实参，
 * 或把任意 {@link Type} 规约到可用的 {@link Class}。</p>
 *
 * @author suretool
 * @since 1.18.0
 */
public class TypeUtil {

	private TypeUtil() {
	}

	private static <T> T requireNonNullArg(T value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	/**
	 * 将任意 {@link Type} 解析为运行时类。
	 *
	 * <p>规约规则：{@link ParameterizedType} 取原始类；{@link GenericArrayType}
	 * 取组件数组类；{@link WildcardType} 取上界；{@link TypeVariable} 取首个边界；
	 * 直接为 {@link Class} 时原样返回。</p>
	 *
	 * @param type 待解析类型，null 抛 {@link IllegalArgumentException}
	 * @return 解析出的运行时类
	 * @throws IllegalArgumentException type 为 null 或无法解析（无界通配/无边界变量等）
	 */
	public static Class<?> getClass(Type type) {
		requireNonNullArg(type, "type must not be null");
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		if (type instanceof GenericArrayType) {
			Class<?> component = getClass(((GenericArrayType) type).getGenericComponentType());
			return java.lang.reflect.Array.newInstance(component, 0).getClass();
		}
		if (type instanceof WildcardType) {
			Type[] upper = ((WildcardType) type).getUpperBounds();
			if (upper.length > 0) {
				return getClass(upper[0]);
			}
		}
		if (type instanceof TypeVariable<?>) {
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			if (bounds.length > 0) {
				return getClass(bounds[0]);
			}
		}
		throw new IllegalArgumentException("无法解析类型: " + type);
	}

	/**
	 * 获取参数化类型第 index 个泛型实参对应的运行时类。
	 *
	 * <p>type 非 {@link ParameterizedType}、实参非可解析类型或下标越界时返回
	 * {@link Object Object.class}（安全兜底，不抛异常）。</p>
	 *
	 * @param type  目标类型，null 抛 {@link IllegalArgumentException}
	 * @param index 实参下标（从 0 开始）
	 * @return 实参运行时类，兜底为 Object.class
	 */
	public static Class<?> getTypeArgument(Type type, int index) {
		requireNonNullArg(type, "type must not be null");
		if (type instanceof ParameterizedType) {
			Type[] args = ((ParameterizedType) type).getActualTypeArguments();
			if (index >= 0 && index < args.length) {
				return getClass(args[index]);
			}
		}
		return Object.class;
	}

	/**
	 * 获取参数化类型的全部泛型实参运行时类（不可变列表）。
	 *
	 * <p>type 非参数化类型时返回空列表。</p>
	 *
	 * @param type 目标类型，null 抛 {@link IllegalArgumentException}
	 * @return 实参类列表（不可变）
	 */
	public static List<Class<?>> getTypeArguments(Type type) {
		requireNonNullArg(type, "type must not be null");
		if (type instanceof ParameterizedType) {
			Type[] args = ((ParameterizedType) type).getActualTypeArguments();
			List<Class<?>> result = new ArrayList<>(args.length);
			for (Type arg : args) {
				result.add(getClass(arg));
			}
			return Collections.unmodifiableList(result);
		}
		return Collections.emptyList();
	}

	/**
	 * 判断类型是否为无边界类型变量（{@code <T>} 而未声明上界）。
	 *
	 * @param type 待判断类型，null 返回 false
	 * @return true 表示无边界类型变量
	 */
	public static boolean isUnknown(Type type) {
		// Java 中 TypeVariable.getBounds() 恒非空（无约束时以 Object 兜底），
		// 故"无边界"判定为：仅一个边界且恰为 Object。
		if (type instanceof TypeVariable<?>) {
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			return bounds.length == 1 && bounds[0] == Object.class;
		}
		return false;
	}

	/**
	 * 取类型引用捕获的泛型类型。
	 *
	 * @param reference 类型引用，null 抛 {@link IllegalArgumentException}
	 * @return 捕获类型
	 */
	public static Type getType(TypeReference<?> reference) {
		requireNonNullArg(reference, "reference must not be null");
		return reference.getType();
	}
}
