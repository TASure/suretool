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
}
