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
package com.sure.tool.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.sure.tool.bean.BeanUtil;

/**
 * Properties 配置增强，参考 Hutool 的 {@code Props} 设计。
 *
 * <p>支持从 classpath 相对路径或文件加载 {@code .properties}，提供类型安全的读取（带默认值）、
 * 转换为 Bean 与 Map 快照。
 *
 * @author suretool
 * @since 1.9.0
 */
public class Props extends Properties {

	private static final long serialVersionUID = 1L;

	/**
	 * 空配置。
	 */
	public Props() {
	}

	/**
	 * 从 classpath 相对路径加载。
	 *
	 * @param path classpath 相对路径（如 {@code "config/app.properties"}）
	 * @throws IllegalArgumentException 资源不存在或加载失败
	 */
	public Props(String path) {
		load(path);
	}

	/**
	 * 从文件加载。
	 *
	 * @param file 文件
	 * @throws IllegalArgumentException 文件不存在或加载失败
	 */
	public Props(File file) {
		load(file);
	}

	/**
	 * 从 classpath 相对路径加载（UTF-8）。
	 *
	 * @param path classpath 相对路径
	 * @return 本对象
	 * @throws IllegalArgumentException 资源不存在或加载失败
	 */
	public Props load(String path) {
		if (path == null) {
			throw new IllegalArgumentException("路径不能为 null");
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream in = (loader == null ? ClassLoader.getSystemResourceAsStream(path)
				: loader.getResourceAsStream(path));
		if (in == null) {
			throw new IllegalArgumentException("classpath 中不存在资源: " + path);
		}
		try (InputStream is = in) {
			load(is);
		} catch (IOException e) {
			throw new IllegalArgumentException("加载配置失败: " + path, e);
		}
		return this;
	}

	/**
	 * 从文件加载（UTF-8）。
	 *
	 * @param file 文件
	 * @return 本对象
	 * @throws IllegalArgumentException 文件不存在或加载失败
	 */
	public Props load(File file) {
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("文件不存在: " + file);
		}
		try (InputStream in = new FileInputStream(file)) {
			load(in);
		} catch (IOException e) {
			throw new IllegalArgumentException("加载配置失败: " + file, e);
		}
		return this;
	}

	/**
	 * 读取字符串，不存在返回 {@code null}。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	public String getStr(String key) {
		return getProperty(key);
	}

	/**
	 * 读取字符串，不存在或为空返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public String getStr(String key, String defaultValue) {
		String value = getProperty(key);
		return (value == null || value.isEmpty()) ? defaultValue : value;
	}

	/**
	 * 读取 int，不存在或解析失败返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public int getInt(String key, int defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 读取 long，不存在或解析失败返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public long getLong(String key, long defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 读取 double，不存在或解析失败返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public double getDouble(String key, double defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 读取 boolean，不存在返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public boolean getBool(String key, boolean defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value.trim());
	}

	/**
	 * 读取 BigDecimal，不存在或解析失败返回默认值。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 读取原始对象（字符串值）。
	 *
	 * @param key 键
	 * @return 值或 {@code null}
	 */
	public Object getObj(String key) {
		return getProperty(key);
	}

	/**
	 * 转 Bean：属性名 → Bean 属性（带类型转换）。
	 *
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 实例
	 */
	public <T> T toBean(Class<T> beanClass) {
		if (beanClass == null) {
			return null;
		}
		T bean = com.sure.tool.util.ReflectUtil.invokeConstructor(beanClass);
		if (bean == null) {
			return null;
		}
		for (Map.Entry<Object, Object> entry : entrySet()) {
			BeanUtil.setProperty(bean, (String) entry.getKey(), entry.getValue());
		}
		return bean;
	}

	/**
	 * 转为 Map 快照（键值均为字符串）。
	 *
	 * @return Map 快照
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new LinkedHashMap<>();
		for (Map.Entry<Object, Object> entry : entrySet()) {
			map.put((String) entry.getKey(), (String) entry.getValue());
		}
		return map;
	}

	/**
	 * 以 UTF-8 覆盖重载：{@link Properties#load(InputStream)} 默认 ISO-8859-1。
	 *
	 * @param in 输入流
	 * @throws IOException IO 异常
	 */
	@Override
	public void load(InputStream in) throws IOException {
		Properties tmp = new Properties();
		tmp.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
		this.putAll(tmp);
	}
}
