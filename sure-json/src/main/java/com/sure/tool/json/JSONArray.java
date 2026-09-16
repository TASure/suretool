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
package com.sure.tool.json;

import java.util.ArrayList;
import java.util.Collection;

/**
 * JSON 数组，继承 {@link ArrayList}，提供类型化读取，参考 Hutool 的 {@code JSONArray} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JSONArray extends ArrayList<Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建空 JSON 数组。
	 */
	public JSONArray() {
	}

	/**
	 * 解析 JSON 字符串创建 JSON 数组。
	 *
	 * @param json JSON 字符串
	 */
	public JSONArray(String json) {
		addAll(JSONUtil.parseArray(json));
	}

	/**
	 * 从集合创建 JSON 数组。
	 *
	 * @param collection 集合
	 */
	public JSONArray(Collection<?> collection) {
		if (collection != null) {
			addAll(collection);
		}
	}

	/**
	 * 从可变参数创建 JSON 数组。
	 *
	 * @param items 元素
	 */
	public JSONArray(Object... items) {
		if (items != null) {
			for (Object item : items) {
				add(item);
			}
		}
	}

	/**
	 * 解析 JSON 字符串。
	 *
	 * @param json JSON 字符串
	 * @return JSON 数组
	 */
	public static JSONArray parse(String json) {
		return JSONUtil.parseArray(json);
	}

	/**
	 * 读取字符串值，越界或为 null 返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 字符串值或 {@code null}
	 */
	public String getStr(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		return value == null ? null : String.valueOf(value);
	}

	/**
	 * 读取整数，越界或无法解析返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 整数值或 {@code null}
	 */
	public Integer getInt(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return Integer.valueOf(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 读取长整数，越界或无法解析返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 长整数值或 {@code null}
	 */
	public Long getLong(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		try {
			return Long.valueOf(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 读取浮点数，越界或无法解析返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 浮点值或 {@code null}
	 */
	public Double getDouble(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		try {
			return Double.valueOf(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 读取布尔值，越界返回 {@code null}。
	 *
	 * @param index 索引
	 * @return 布尔值或 {@code null}
	 */
	public Boolean getBool(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.valueOf(String.valueOf(value));
	}

	/**
	 * 读取嵌套 JSON 对象。
	 *
	 * @param index 索引
	 * @return JSON 对象或 {@code null}
	 */
	public JSONObject getJSONObject(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		if (value instanceof java.util.Map) {
			return new JSONObject((java.util.Map<?, ?>) value);
		}
		return null;
	}

	/**
	 * 读取嵌套 JSON 数组。
	 *
	 * @param index 索引
	 * @return JSON 数组或 {@code null}
	 */
	public JSONArray getJSONArray(int index) {
		if (!inRange(index)) {
			return null;
		}
		Object value = get(index);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		if (value instanceof Collection) {
			return new JSONArray((Collection<?>) value);
		}
		return null;
	}

	/**
	 * 读取并转换为 Bean。
	 *
	 * @param index     索引
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 或 {@code null}
	 */
	public <T> T getBean(int index, Class<T> beanClass) {
		if (!inRange(index)) {
			return null;
		}
		JSONObject jsonObject = getJSONObject(index);
		if (jsonObject == null) {
			return null;
		}
		return JSONUtil.toBean(jsonObject, beanClass);
	}

	/**
	 * 索引是否在有效范围内。
	 *
	 * @param index 索引
	 * @return 是否有效
	 */
	private boolean inRange(int index) {
		return index >= 0 && index < size();
	}

	/**
	 * 序列化为 JSON 字符串。
	 *
	 * @return JSON 字符串
	 */
	public String toJsonString() {
		return JSONUtil.toJsonStr(this);
	}

	@Override
	public String toString() {
		return toJsonString();
	}

	/**
	 * 取 char 值。
	 *
	 * @param index 下标
	 * @return char；缺失返回 0
	 */
	public char getChar(int index) {
		return getChar(index, (char) 0);
	}

	/**
	 * 取 char 值。
	 *
	 * @param index        下标
	 * @param defaultValue 默认值
	 * @return char；缺失返回默认值
	 */
	public char getChar(int index, char defaultValue) {
		if (index < 0 || index >= size()) {
			return defaultValue;
		}
		Object value = get(index);
		if (value == null) {
			return defaultValue;
		}
		String s = String.valueOf(value);
		return s.isEmpty() ? defaultValue : s.charAt(0);
	}

	/**
	 * 取 short 值。
	 *
	 * @param index 下标
	 * @return short；缺失返回 0
	 */
	public short getShort(int index) {
		return getShort(index, (short) 0);
	}

	/**
	 * 取 short 值。
	 *
	 * @param index        下标
	 * @param defaultValue 默认值
	 * @return short；缺失返回默认值
	 */
	public short getShort(int index, short defaultValue) {
		if (index < 0 || index >= size()) {
			return defaultValue;
		}
		return com.sure.tool.util.ConvertUtil.toShort(get(index), defaultValue);
	}

	/**
	 * 取 byte 值。
	 *
	 * @param index 下标
	 * @return byte；缺失返回 0
	 */
	public byte getByte(int index) {
		return getByte(index, (byte) 0);
	}

	/**
	 * 取 byte 值。
	 *
	 * @param index        下标
	 * @param defaultValue 默认值
	 * @return byte；缺失返回默认值
	 */
	public byte getByte(int index, byte defaultValue) {
		if (index < 0 || index >= size()) {
			return defaultValue;
		}
		return com.sure.tool.util.ConvertUtil.toByte(get(index), defaultValue);
	}

	/**
	 * 取 float 值。
	 *
	 * @param index 下标
	 * @return float；缺失返回 0
	 */
	public float getFloat(int index) {
		return getFloat(index, 0F);
	}

	/**
	 * 取 float 值。
	 *
	 * @param index        下标
	 * @param defaultValue 默认值
	 * @return float；缺失返回默认值
	 */
	public float getFloat(int index, float defaultValue) {
		if (index < 0 || index >= size()) {
			return defaultValue;
		}
		return com.sure.tool.util.ConvertUtil.toFloat(get(index), defaultValue);
	}



	/**
	 * 取日期值（支持时间戳与常见日期字符串，转换失败返回 null）。
	 *
	 * @param index 下标
	 * @return 日期；缺失或转换失败返回 null
	 */
	public java.util.Date getDate(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return com.sure.tool.util.ConvertUtil.toDate(get(index));
	}



	/**
	 * 取 BigDecimal 值（金额场景）。
	 *
	 * @param index 下标
	 * @return BigDecimal；缺失或转换失败返回 null
	 */
	public java.math.BigDecimal getBigDecimal(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return com.sure.tool.util.ConvertUtil.toBigDecimal(get(index));
	}



	/**
	 * 转为对象列表（元素为 JSONObject 时按 beanClass 转换）。
	 *
	 * @param beanClass 目标类型
	 * @param <T>       泛型
	 * @return 对象列表
	 */
	public <T> java.util.List<T> toList(Class<T> beanClass) {
		java.util.List<T> result = new java.util.ArrayList<>();
		for (int i = 0; i < size(); i++) {
			result.add(getBean(i, beanClass));
		}
		return result;
	}

	/**
	 * 转为对象数组（元素为 JSONObject 时按 beanClass 转换）。
	 *
	 * @param beanClass 目标类型
	 * @param <T>       泛型
	 * @return 对象数组
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Class<T> beanClass) {
		java.util.List<T> list = toList(beanClass);
		return list.toArray((T[]) java.lang.reflect.Array.newInstance(beanClass, list.size()));
	}



	/**
	 * 深拷贝（嵌套 JSONObject/JSONArray/Map/List 均递归复制）。
	 *
	 * @return 深拷贝副本
	 */
	public JSONArray deepClone() {
		JSONArray copy = new JSONArray();
		for (Object item : this) {
			copy.add(deepCopyValue(item));
		}
		return copy;
	}

	private static Object deepCopyValue(Object value) {
		if (value instanceof JSONObject obj) {
			return obj.deepClone();
		}
		if (value instanceof JSONArray arr) {
			return arr.deepClone();
		}
		if (value instanceof java.util.Map<?, ?> map) {
			JSONObject copy = new JSONObject();
			for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
				copy.set(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
			}
			return copy;
		}
		if (value instanceof java.util.List<?> list) {
			java.util.List<Object> copy = new java.util.ArrayList<>();
			for (Object item : list) {
				copy.add(deepCopyValue(item));
			}
			return copy;
		}
		return value;
	}


}