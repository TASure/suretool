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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.sure.tool.io.FileUtil;

/**
 * 分层配置读取工具类，参考 Hutool {@code Setting} 设计。
 *
 * <p>配置来源优先级（高 → 低）：</p>
 * <ol>
 *   <li>系统属性（{@code -Dkey=value}）</li>
 *   <li>环境变量（{@code key} 中的点转为下划线并大写，如 {@code server.port} → {@code SERVER_PORT}）</li>
 *   <li>配置文件（UTF-8、{@code key=value}、{@code #} 注释、空行忽略）</li>
 *   <li>调用方提供的默认值</li>
 * </ol>
 *
 * <p>状态为 Copy-on-Write 快照，{@link #load} 系列方法整体替换配置，
 * 并发读写安全。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class SettingUtil {

	/**
	 * 当前配置快照（不可变），通过 load 原子替换。
	 */
	private static volatile Map<String, String> SNAPSHOT = Map.of();

	private SettingUtil() {
	}

	/**
	 * 从文件加载配置（UTF-8），整体替换当前快照。
	 *
	 * @param path 文件路径
	 */
	public static void load(String path) {
		load(FileUtil.file(path));
	}

	/**
	 * 从文件加载配置（UTF-8），整体替换当前快照。
	 *
	 * @param file 文件
	 */
	public static void load(File file) {
		try {
			load(file.toURI().toURL());
		} catch (IOException e) {
			throw new IllegalStateException("加载配置文件失败：" + file, e);
		}
	}

	/**
	 * 从 URL 加载配置（UTF-8），整体替换当前快照。
	 *
	 * @param url 配置 URL
	 */
	public static void load(URL url) {
		try (InputStream in = url.openStream()) {
			load(in);
		} catch (IOException e) {
			throw new IllegalStateException("加载配置失败：" + url, e);
		}
	}

	/**
	 * 从输入流加载配置（UTF-8），整体替换当前快照。
	 *
	 * @param in 输入流
	 */
	public static void load(InputStream in) {
		SNAPSHOT = Map.copyOf(parse(new InputStreamReader(in, StandardCharsets.UTF_8)));
	}

	/**
	 * 获取字符串配置（分层查找，无默认值）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static String getString(String key) {
		return resolve(key, null);
	}

	/**
	 * 获取字符串配置（分层查找）。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值，未找到返回默认值
	 */
	public static String getString(String key, String defaultValue) {
		return resolve(key, defaultValue);
	}

	/**
	 * 获取整型配置（分层查找）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static Integer getInt(String key) {
		String value = resolve(key, null);
		return value == null ? null : toNumber(key, value, Integer::parseInt, "int");
	}

	/**
	 * 获取整型配置（分层查找）。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值，未找到返回默认值
	 */
	public static int getInt(String key, int defaultValue) {
		String value = resolve(key, null);
		return value == null ? defaultValue : toNumber(key, value, Integer::parseInt, "int");
	}

	/**
	 * 获取长整型配置（分层查找）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static Long getLong(String key) {
		String value = resolve(key, null);
		return value == null ? null : toNumber(key, value, Long::parseLong, "long");
	}

	/**
	 * 获取长整型配置（分层查找）。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值，未找到返回默认值
	 */
	public static long getLong(String key, long defaultValue) {
		String value = resolve(key, null);
		return value == null ? defaultValue : toNumber(key, value, Long::parseLong, "long");
	}

	/**
	 * 获取双精度配置（分层查找）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static Double getDouble(String key) {
		String value = resolve(key, null);
		return value == null ? null : toNumber(key, value, Double::parseDouble, "double");
	}

	/**
	 * 获取双精度配置（分层查找）。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值，未找到返回默认值
	 */
	public static double getDouble(String key, double defaultValue) {
		String value = resolve(key, null);
		return value == null ? defaultValue : toNumber(key, value, Double::parseDouble, "double");
	}

	/**
	 * 获取布尔配置（分层查找）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static Boolean getBoolean(String key) {
		String value = resolve(key, null);
		return value == null ? null : parseBoolean(key, value);
	}

	/**
	 * 获取布尔配置（分层查找）。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值，未找到返回默认值
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		String value = resolve(key, null);
		return value == null ? defaultValue : parseBoolean(key, value);
	}

	/**
	 * 获取原始配置值（分层查找）。
	 *
	 * @param key 配置键
	 * @return 配置值，未找到返回 {@code null}
	 */
	public static Object get(String key) {
		return resolve(key, null);
	}

	/**
	 * 是否包含指定键（分层查找命中任意一层均视为包含）。
	 *
	 * @param key 配置键
	 * @return 命中返回 {@code true}
	 */
	public static boolean contains(String key) {
		return resolve(key, null) != null;
	}

	/**
	 * 返回当前配置快照的全部键。
	 *
	 * @return 配置键集合
	 */
	public static Set<String> keys() {
		return SNAPSHOT.keySet();
	}

	/**
	 * 清空当前配置快照。
	 */
	public static void clear() {
		SNAPSHOT = Map.of();
	}

	/**
	 * 分层查找配置值。
	 *
	 * @param key          配置键
	 * @param defaultValue 默认值
	 * @return 配置值
	 */
	private static String resolve(String key, String defaultValue) {
		if (key == null) {
			throw new IllegalArgumentException("key 不能为 null");
		}
		String value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		value = System.getenv(envName(key));
		if (value != null) {
			return value;
		}
		value = SNAPSHOT.get(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * 将配置键转换为环境变量名（点转下划线并大写）。
	 *
	 * @param key 配置键
	 * @return 环境变量名
	 */
	private static String envName(String key) {
		return key.replace('.', '_').toUpperCase(Locale.ROOT);
	}

	/**
	 * 解析配置文件内容为有序 Map。
	 *
	 * @param reader 读取器
	 * @return 配置 Map
	 */
	private static Map<String, String> parse(Reader reader) {
		Map<String, String> result = new LinkedHashMap<>();
		try (BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				int eq = trimmed.indexOf('=');
				if (eq < 0) {
					continue;
				}
				String key = trimmed.substring(0, eq).trim();
				String value = trimmed.substring(eq + 1).trim();
				if (!key.isEmpty()) {
					result.put(key, value);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("解析配置失败", e);
		}
		return result;
	}

	/**
	 * 数字解析并包装异常信息。
	 *
	 * @param key     配置键
	 * @param value   配置值
	 * @param parser  解析函数
	 * @param type    类型名（int/long/double）
	 * @param <T>     数值类型
	 * @return 解析结果
	 */
	private static <T> T toNumber(String key, String value, java.util.function.Function<String, T> parser, String type) {
		try {
			return parser.apply(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("配置项 [" + key + "] 的值 '" + value + "' 无法转换为 " + type, e);
		}
	}

	/**
	 * 布尔解析（true/false/1/0/yes/no 忽略大小写）。
	 *
	 * @param key   配置键
	 * @param value 配置值
	 * @return 解析结果
	 */
	private static boolean parseBoolean(String key, String value) {
		String v = value.trim().toLowerCase(Locale.ROOT);
		return switch (v) {
			case "true", "1", "yes", "y", "on" -> true;
			case "false", "0", "no", "n", "off" -> false;
			default -> throw new IllegalArgumentException("配置项 [" + key + "] 的值 '" + value + "' 无法转换为 boolean");
		};
	}
}
