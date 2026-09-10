package com.sure.tool.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sure.tool.bean.BeanUtil;

/**
 * JSON 对象，继承 {@link LinkedHashMap} 保证顺序，提供链式设置与类型化读取，参考 Hutool 的 {@code JSONObject} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JSONObject extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建空 JSON 对象。
	 */
	public JSONObject() {
	}

	/**
	 * 解析 JSON 字符串创建 JSON 对象。
	 *
	 * @param json JSON 字符串
	 */
	public JSONObject(String json) {
		putAll(JSONUtil.parseObj(json));
	}

	/**
	 * 从 Map 创建 JSON 对象。
	 *
	 * @param map Map
	 */
	public JSONObject(Map<?, ?> map) {
		if (map != null) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				put(String.valueOf(entry.getKey()), entry.getValue());
			}
		}
	}

	/**
	 * 从 Bean 创建 JSON 对象（不忽略 null 值）。
	 *
	 * @param bean Bean 对象
	 */
	public JSONObject(Object bean) {
		this(bean, false);
	}

	/**
	 * 从 Bean 创建 JSON 对象。
	 *
	 * @param bean            Bean 对象
	 * @param ignoreNullValue 是否忽略 {@code null} 值
	 */
	public JSONObject(Object bean, boolean ignoreNullValue) {
		this(BeanUtil.beanToMap(bean, ignoreNullValue));
	}

	/**
	 * 解析 JSON 字符串。
	 *
	 * @param json JSON 字符串
	 * @return JSON 对象
	 */
	public static JSONObject parse(String json) {
		return JSONUtil.parseObj(json);
	}

	/**
	 * 链式设置值。
	 *
	 * @param key   键
	 * @param value 值
	 * @return 当前对象
	 */
	public JSONObject set(String key, Object value) {
		put(key, value);
		return this;
	}

	/**
	 * 读取字符串值，缺失或类型不符返回 {@code null}。
	 *
	 * @param key 键
	 * @return 字符串值或 {@code null}
	 */
	public String getStr(String key) {
		Object value = get(key);
		return value == null ? null : String.valueOf(value);
	}

	/**
	 * 读取字符串值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 字符串值或默认值
	 */
	public String getStr(String key, String defaultValue) {
		String value = getStr(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * 读取整数，缺失或无法解析返回 {@code null}。
	 *
	 * @param key 键
	 * @return 整数值或 {@code null}
	 */
	public Integer getInt(String key) {
		Object value = get(key);
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
	 * 读取长整数，缺失或无法解析返回 {@code null}。
	 *
	 * @param key 键
	 * @return 长整数值或 {@code null}
	 */
	public Long getLong(String key) {
		Object value = get(key);
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
	 * 读取浮点数，缺失或无法解析返回 {@code null}。
	 *
	 * @param key 键
	 * @return 浮点值或 {@code null}
	 */
	public Double getDouble(String key) {
		Object value = get(key);
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
	 * 读取布尔值，缺失返回 {@code null}。
	 *
	 * @param key 键
	 * @return 布尔值或 {@code null}
	 */
	public Boolean getBool(String key) {
		Object value = get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.valueOf(String.valueOf(value));
	}

	/**
	 * 读取嵌套 JSON 对象，值为 Map 时自动转换。
	 *
	 * @param key 键
	 * @return JSON 对象或 {@code null}
	 */
	public JSONObject getJSONObject(String key) {
		Object value = get(key);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		if (value instanceof Map) {
			return new JSONObject((Map<?, ?>) value);
		}
		return null;
	}

	/**
	 * 读取嵌套 JSON 数组。
	 *
	 * @param key 键
	 * @return JSON 数组或 {@code null}
	 */
	public JSONArray getJSONArray(String key) {
		Object value = get(key);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		if (value instanceof java.util.Collection) {
			return new JSONArray((java.util.Collection<?>) value);
		}
		return null;
	}

	/**
	 * 读取并转换为 Bean。
	 *
	 * @param key       键
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 或 {@code null}
	 */
	public <T> T getBean(String key, Class<T> beanClass) {
		JSONObject jsonObject = getJSONObject(key);
		if (jsonObject == null) {
			return null;
		}
		return JSONUtil.toBean(jsonObject, beanClass);
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
