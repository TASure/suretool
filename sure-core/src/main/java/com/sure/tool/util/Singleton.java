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

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例池工具：按类持有全局唯一实例，线程安全，零依赖。
 *
 * <p>替代手写单例模板：{@code Singleton.get(MyClass.class)} 即可获得全局唯一实例，
 * 实例通过类默认构造器或指定参数构造器创建，可主动 {@link #remove(Class)} 释放。</p>
 *
 * @author suretool
 * @since 0.2.0
 */
public class Singleton {

	private static final ConcurrentHashMap<Class<?>, Object> POOL = new ConcurrentHashMap<>();

	private Singleton() {
	}

	/**
	 * 获取类单例（默认构造器创建）。
	 *
	 * @param <T>  类型
	 * @param type 类
	 * @return 全局唯一实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> type) {
		return (T) POOL.computeIfAbsent(type, Singleton::createDefault);
	}

	/**
	 * 获取类单例（按参数构造器创建）。
	 *
	 * @param <T>    类型
	 * @param type   类
	 * @param params 构造参数
	 * @return 全局唯一实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> type, Object... params) {
		if (params == null || params.length == 0) {
			return get(type);
		}
		return (T) POOL.computeIfAbsent(type, cls -> createWithParams(cls, params));
	}

	/**
	 * 手动放入单例（覆盖已有实例）。
	 *
	 * @param <T>    类型
	 * @param type   类
	 * @param object 实例
	 * @return 旧实例（不存在返回 null）
	 */
	@SuppressWarnings("unchecked")
	public static <T> T put(Class<T> type, T object) {
		return (T) POOL.put(type, object);
	}

	/**
	 * 移除指定类单例。
	 *
	 * @param type 类
	 * @return 被移除的实例；不存在返回 null
	 */
	public static Object remove(Class<?> type) {
		return POOL.remove(type);
	}

	/**
	 * 清空全部单例。
	 */
	public static void destroy() {
		POOL.clear();
	}

	/**
	 * 是否已持有指定类单例。
	 *
	 * @param type 类
	 * @return 是否持有
	 */
	public static boolean contains(Class<?> type) {
		return POOL.containsKey(type);
	}

	private static <T> T createDefault(Class<T> type) {
		try {
			Constructor<T> ctor = type.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("单例创建失败（无默认构造器或构造器不可访问）: " + type.getName(), e);
		}
	}

	private static Object createWithParams(Class<?> type, Object[] params) {
		try {
			Class<?>[] paramTypes = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				paramTypes[i] = params[i] == null ? Object.class : params[i].getClass();
			}
			Constructor<?> ctor = type.getDeclaredConstructor(paramTypes);
			ctor.setAccessible(true);
			return ctor.newInstance(params);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("单例创建失败（参数构造器不匹配）: " + type.getName(), e);
		}
	}
}
