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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Properties 配置文件增强工具：UTF-8 加载、强类型读取、默认值，零依赖。
 *
 * <p>覆盖 {@code .properties} 中文乱码场景：加载时按指定字符集读取（默认 UTF-8）。</p>
 *
 * @author suretool
 * @since 0.2.0
 */
public class Props {

	private final Properties properties;

	/**
	 * 构造空配置。
	 */
	public Props() {
		this.properties = new Properties();
	}

	/**
	 * 从路径加载（UTF-8）。
	 *
	 * @param path 文件路径
	 * @throws IOException IO 异常
	 */
	public Props(String path) throws IOException {
		this(new File(path));
	}

	/**
	 * 从文件加载（UTF-8）。
	 *
	 * @param file 文件
	 * @throws IOException IO 异常
	 */
	public Props(File file) throws IOException {
		this.properties = new Properties();
		load(file);
	}

	/**
	 * 从输入流加载（UTF-8）。
	 *
	 * @param in 输入流
	 * @throws IOException IO 异常
	 */
	public Props(InputStream in) throws IOException {
		this.properties = new Properties();
		load(in);
	}

	/**
	 * 加载文件（UTF-8）。
	 *
	 * @param file 文件
	 * @return this
	 * @throws IOException IO 异常
	 */
	public Props load(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			load(in);
		}
		return this;
	}

	/**
	 * 加载输入流（UTF-8）。
	 *
	 * @param in 输入流
	 * @return this
	 * @throws IOException IO 异常
	 */
	public Props load(InputStream in) throws IOException {
		return load(in, StandardCharsets.UTF_8);
	}

	/**
	 * 加载输入流（指定字符集，避免中文乱码）。
	 *
	 * @param in      输入流
	 * @param charset 字符集
	 * @return this
	 * @throws IOException IO 异常
	 */
	public Props load(InputStream in, Charset charset) throws IOException {
		try (Reader reader = new InputStreamReader(in, charset)) {
			properties.load(reader);
		}
		return this;
	}

	/**
	 * 取字符串。
	 *
	 * @param key 键
	 * @return 值；不存在返回 null
	 */
	public String getStr(String key) {
		return properties.getProperty(key);
	}

	/**
	 * 取字符串（带默认值）。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值
	 */
	public String getStr(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * 取 int。
	 *
	 * @param key 键
	 * @return int 值；缺失返回 0
	 */
	public int getInt(String key) {
		return getInt(key, 0);
	}

	/**
	 * 取 int（带默认值）。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return int 值
	 */
	public int getInt(String key, int defaultValue) {
		String value = properties.getProperty(key);
		return value == null ? defaultValue : Integer.parseInt(value.trim());
	}

	/**
	 * 取 long。
	 *
	 * @param key 键
	 * @return long 值；缺失返回 0
	 */
	public long getLong(String key) {
		return getLong(key, 0L);
	}

	/**
	 * 取 long（带默认值）。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return long 值
	 */
	public long getLong(String key, long defaultValue) {
		String value = properties.getProperty(key);
		return value == null ? defaultValue : Long.parseLong(value.trim());
	}

	/**
	 * 取 double。
	 *
	 * @param key 键
	 * @return double 值；缺失返回 0
	 */
	public double getDouble(String key) {
		return getDouble(key, 0D);
	}

	/**
	 * 取 double（带默认值）。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return double 值
	 */
	public double getDouble(String key, double defaultValue) {
		String value = properties.getProperty(key);
		return value == null ? defaultValue : Double.parseDouble(value.trim());
	}

	/**
	 * 取 boolean（true/1/yes 视为 true，忽略大小写）。
	 *
	 * @param key 键
	 * @return boolean 值；缺失返回 false
	 */
	public boolean getBool(String key) {
		return getBool(key, false);
	}

	/**
	 * 取 boolean（带默认值）。
	 *
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return boolean 值
	 */
	public boolean getBool(String key, boolean defaultValue) {
		String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		String v = value.trim();
		return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
	}

	/**
	 * 是否包含键。
	 *
	 * @param key 键
	 * @return 是否包含
	 */
	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	/**
	 * 设置键值。
	 *
	 * @param key   键
	 * @param value 值
	 * @return this
	 */
	public Props set(String key, Object value) {
		properties.setProperty(key, String.valueOf(value));
		return this;
	}

	/**
	 * 获取底层 Properties。
	 *
	 * @return Properties
	 */
	public Properties getProperties() {
		return (Properties) properties.clone();
	}

	@Override
	public String toString() {
		return properties.toString();
	}
}
