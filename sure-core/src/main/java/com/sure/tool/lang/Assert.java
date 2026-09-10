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

import com.sure.tool.collection.CollUtil;
import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.ReUtil;
import com.sure.tool.util.StrUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类，参考 Hutool 的 {@code Assert} 与 Spring 的 {@code Assert} 设计。
 *
 * <p>断言失败时抛出 {@link IllegalArgumentException}。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class Assert {

	private Assert() {
	}

	/**
	 * 断言表达式为真。
	 *
	 * @param expression      表达式
	 * @param errorMsgTemplate 错误信息模板，支持一对花括号占位符
	 * @param params           占位符参数
	 */
	public static void isTrue(boolean expression, String errorMsgTemplate, Object... params) {
		if (!expression) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
	}

	/**
	 * 断言表达式为真。
	 *
	 * @param expression 表达式
	 */
	public static void isTrue(boolean expression) {
		isTrue(expression, "[断言失败] - 表达式必须为 true");
	}

	/**
	 * 断言表达式为假。
	 *
	 * @param expression       表达式
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 */
	public static void isFalse(boolean expression, String errorMsgTemplate, Object... params) {
		isTrue(!expression, errorMsgTemplate, params);
	}

	/**
	 * 断言对象为 {@code null}。
	 *
	 * @param object           对象
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 */
	public static void isNull(Object object, String errorMsgTemplate, Object... params) {
		if (object != null) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
	}

	/**
	 * 断言对象非 {@code null}。
	 *
	 * @param object           对象
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              对象类型
	 * @return 原对象
	 */
	public static <T> T isNotNull(T object, String errorMsgTemplate, Object... params) {
		if (object == null) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return object;
	}

	/**
	 * 断言对象非 {@code null}。
	 *
	 * @param object           对象
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              对象类型
	 * @return 原对象
	 */
	public static <T> T notNull(T object, String errorMsgTemplate, Object... params) {
		return isNotNull(object, errorMsgTemplate, params);
	}

	/**
	 * 断言字符串非空（{@code null} 或空串均失败）。
	 *
	 * @param text             字符串
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              字符串类型
	 * @return 原字符串
	 */
	public static <T extends CharSequence> T notEmpty(T text, String errorMsgTemplate, Object... params) {
		if (StrUtil.isEmpty(text)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return text;
	}

	/**
	 * 断言字符串非空白。
	 *
	 * @param text             字符串
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              字符串类型
	 * @return 原字符串
	 */
	public static <T extends CharSequence> T notBlank(T text, String errorMsgTemplate, Object... params) {
		if (StrUtil.isBlank(text)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return text;
	}

	/**
	 * 断言数组非空。
	 *
	 * @param array            数组
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              元素类型
	 * @return 原数组
	 */
	public static <T> T[] notEmpty(T[] array, String errorMsgTemplate, Object... params) {
		if (ArrayUtil.isEmpty(array)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return array;
	}

	/**
	 * 断言集合非空。
	 *
	 * @param collection       集合
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <T>              元素类型
	 * @return 原集合
	 */
	public static <T> Collection<T> notEmpty(Collection<T> collection, String errorMsgTemplate, Object... params) {
		if (CollUtil.isEmpty(collection)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return collection;
	}

	/**
	 * 断言 Map 非空。
	 *
	 * @param map              Map
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 * @param <K>              key 类型
	 * @param <V>              value 类型
	 * @return 原 Map
	 */
	public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String errorMsgTemplate, Object... params) {
		if (CollUtil.isEmpty(map)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
		return map;
	}

	/**
	 * 断言对象是指定类型实例。
	 *
	 * @param type             目标类型
	 * @param obj              对象
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 */
	public static void isInstanceOf(Class<?> type, Object obj, String errorMsgTemplate, Object... params) {
		if (type == null || !type.isInstance(obj)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
	}

	/**
	 * 断言字符串匹配正则。
	 *
	 * @param regex            正则表达式
	 * @param content          字符串
	 * @param errorMsgTemplate 错误信息模板
	 * @param params           占位符参数
	 */
	public static void match(String regex, CharSequence content, String errorMsgTemplate, Object... params) {
		if (!ReUtil.isMatch(regex, content)) {
			throw new IllegalArgumentException(StrUtil.format(errorMsgTemplate, params));
		}
	}
}