package com.sure.tool.lang;

import com.sure.tool.util.StrUtil;

import java.util.LinkedHashMap;

/**
 * 便捷字典类，参考 Hutool 的 {@code Dict} 设计。
 * 以字符串为键的 {@link LinkedHashMap}，提供类型化取值方法，常用于组装参数或临时数据载体。
 *
 * @author suretool
 * @since 0.1.0
 */
public class Dict extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建字典并快速填充键值对（键值交替）。
	 *
	 * @param keyValues 键值对，如 {@code of("name", "sure", "age", 18)}
	 * @return 字典
	 */
	public static Dict of(Object... keyValues) {
		Dict dict = new Dict();
		if (keyValues != null) {
			for (int i = 0; i + 1 < keyValues.length; i += 2) {
				dict.set(String.valueOf(keyValues[i]), keyValues[i + 1]);
			}
		}
		return dict;
	}

	/**
	 * 设置键值对并返回自身（支持链式调用）。
	 *
	 * @param key   键
	 * @param value 值
	 * @return this
	 */
	public Dict set(String key, Object value) {
		put(key, value);
		return this;
	}

	/**
	 * 获取字符串值。
	 *
	 * @param key 键
	 * @return 字符串值，不存在返回 {@code null}
	 */
	public String getStr(String key) {
		Object value = get(key);
		return (value == null) ? null : String.valueOf(value);
	}

	/**
	 * 获取字符串值，不存在或为空返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 字符串值
	 */
	public String getStr(String key, String defaultValue) {
		String value = getStr(key);
		return StrUtil.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * 获取整数值（支持数字类型与字符串转换）。
	 *
	 * @param key 键
	 * @return 整数值，不存在或无法转换返回 {@code null}
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
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	/**
	 * 获取长整数值。
	 *
	 * @param key 键
	 * @return 长整数值，不存在或无法转换返回 {@code null}
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
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	/**
	 * 获取浮点值。
	 *
	 * @param key 键
	 * @return 浮点值，不存在或无法转换返回 {@code null}
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
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	/**
	 * 获取布尔值（支持 {@code true/false}、{@code 1/0}、{@code yes/no}、{@code 是/否} 等）。
	 *
	 * @param key 键
	 * @return 布尔值，不存在或无法识别返回 {@code null}
	 */
	public Boolean getBool(String key) {
		Object value = get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		String str = String.valueOf(value).trim().toLowerCase();
		if ("true".equals(str) || "1".equals(str) || "yes".equals(str) || "y".equals(str)
				|| "on".equals(str) || "是".equals(str)) {
			return Boolean.TRUE;
		}
		if ("false".equals(str) || "0".equals(str) || "no".equals(str) || "n".equals(str)
				|| "off".equals(str) || "否".equals(str)) {
			return Boolean.FALSE;
		}
		return null;
	}
}
