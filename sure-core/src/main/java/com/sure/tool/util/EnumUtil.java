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

/**
 * 枚举工具类：按名称/序号/toString 转换枚举，支持忽略大小写，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class EnumUtil {

	private EnumUtil() {
	}

	/**
	 * 按名称获取枚举（精确匹配）。
	 *
	 * @param enumType 枚举类型
	 * @param name     名称
	 * @param <E>      枚举类型
	 * @return 枚举值；未匹配返回 null
	 */
	public static <E extends Enum<E>> E fromName(Class<E> enumType, String name) {
		return fromName(enumType, name, false);
	}

	/**
	 * 按名称获取枚举。
	 *
	 * @param enumType   枚举类型
	 * @param name       名称
	 * @param ignoreCase 是否忽略大小写
	 * @param <E>        枚举类型
	 * @return 枚举值；未匹配返回 null
	 */
	public static <E extends Enum<E>> E fromName(Class<E> enumType, String name, boolean ignoreCase) {
		if (enumType == null || name == null) {
			return null;
		}
		for (E constant : enumType.getEnumConstants()) {
			if (ignoreCase ? constant.name().equalsIgnoreCase(name) : constant.name().equals(name)) {
				return constant;
			}
		}
		return null;
	}

	/**
	 * 按 toString 获取枚举。
	 *
	 * @param enumType 枚举类型
	 * @param value    toString 值
	 * @param <E>      枚举类型
	 * @return 枚举值；未匹配返回 null
	 */
	public static <E extends Enum<E>> E fromString(Class<E> enumType, String value) {
		if (enumType == null || value == null) {
			return null;
		}
		for (E constant : enumType.getEnumConstants()) {
			if (constant.toString().equals(value)) {
				return constant;
			}
		}
		return null;
	}

	/**
	 * 按序号获取枚举。
	 *
	 * @param enumType 枚举类型
	 * @param ordinal  序号（从 0 开始）
	 * @param <E>      枚举类型
	 * @return 枚举值；越界返回 null
	 */
	public static <E extends Enum<E>> E fromOrdinal(Class<E> enumType, int ordinal) {
		if (enumType == null || ordinal < 0) {
			return null;
		}
		E[] constants = enumType.getEnumConstants();
		return ordinal < constants.length ? constants[ordinal] : null;
	}

	/**
	 * 名称是否属于该枚举（忽略大小写）。
	 *
	 * @param enumType 枚举类型
	 * @param name     名称
	 * @return 是否匹配
	 */
	public static boolean containsName(Class<? extends Enum<?>> enumType, String name) {
		return containsName(enumType, name, true);
	}

	/**
	 * 名称是否属于该枚举。
	 *
	 * @param enumType   枚举类型
	 * @param name       名称
	 * @param ignoreCase 是否忽略大小写
	 * @return 是否匹配
	 */
	public static boolean containsName(Class<? extends Enum<?>> enumType, String name, boolean ignoreCase) {
		if (enumType == null || name == null) {
			return false;
		}
		for (Enum<?> constant : enumType.getEnumConstants()) {
			if (ignoreCase ? constant.name().equalsIgnoreCase(name) : constant.name().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 按名称获取枚举（忽略大小写），未匹配抛出 IllegalArgumentException。
	 *
	 * @param enumType 枚举类型
	 * @param name     名称
	 * @param <E>      枚举类型
	 * @return 枚举值
	 */
	public static <E extends Enum<E>> E valueOfIgnoreCase(Class<E> enumType, String name) {
		E result = fromName(enumType, name, true);
		if (result == null) {
			throw new IllegalArgumentException("未找到枚举 " + enumType.getSimpleName()
					+ " 的常量: " + name);
		}
		return result;
	}
}
