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
package com.sure.tool.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 资源读取工具，统一支持 classpath、文件系统与 HTTP(S) 资源。
 *
 * <p>位置解析顺序：</p>
 * <ol>
 *   <li>{@code classpath:} 前缀：从类路径读取，如 {@code classpath:config/app.properties}；</li>
 *   <li>{@code file:} 前缀：文件系统 URL，如 {@code file:/tmp/data.txt}；</li>
 *   <li>{@code http:} / {@code https:} 前缀：远程 URL；</li>
 *   <li>无前缀：先按类路径查找，未找到再按文件系统相对/绝对路径查找。</li>
 * </ol>
 *
 * <p>示例：</p>
 * <pre>{@code
 * byte[] data = ResourceUtil.readBytes("classpath:config/app.properties");
 * String text = ResourceUtil.readUtf8String("/tmp/data.txt");
 * boolean found = ResourceUtil.exists("https://example.com/health");
 * }</pre>
 *
 * <p>null 语义：位置为 {@code null} 或空白串时相关方法返回 {@code null}/{@code false}（空安全）；
 * 需要数据但资源不存在时抛 {@link IllegalArgumentException}。</p>
 *
 * @author suretool
 * @since 1.20.0
 */
public final class ResourceUtil {

	private static final String CLASSPATH_PREFIX = "classpath:";
	private static final String FILE_PREFIX = "file:";
	private static final String HTTP_PREFIX = "http:";
	private static final String HTTPS_PREFIX = "https:";

	private ResourceUtil() {
	}

	/**
	 * 解析资源位置为 URL。
	 *
	 * @param location 资源位置，可为 {@code null}
	 * @return 资源 URL；未找到或位置为空返回 {@code null}
	 * @throws IllegalArgumentException 显式 URL 格式非法
	 */
	public static URL getResource(String location) {
		if (location == null) {
			return null;
		}
		String value = location.trim();
		if (value.isEmpty()) {
			return null;
		}
		if (startsWithIgnoreCase(value, CLASSPATH_PREFIX)) {
			return classpathResource(value.substring(CLASSPATH_PREFIX.length()));
		}
		if (startsWithIgnoreCase(value, FILE_PREFIX) || startsWithIgnoreCase(value, HTTP_PREFIX)
				|| startsWithIgnoreCase(value, HTTPS_PREFIX)) {
			return toUrl(value);
		}
		URL classpath = classpathResource(value);
		if (classpath != null) {
			return classpath;
		}
		File file = new File(value);
		return file.exists() ? toUrl(file.toURI().toString()) : null;
	}

	/**
	 * 判断资源是否存在。
	 *
	 * @param location 资源位置，可为 {@code null}
	 * @return 存在返回 {@code true}
	 */
	public static boolean exists(String location) {
		return getResource(location) != null;
	}

	/**
	 * 打开资源输入流，调用方负责关闭。
	 *
	 * @param location 资源位置，可为 {@code null}
	 * @return 输入流；资源不存在返回 {@code null}
	 * @throws IOException 打开资源失败
	 */
	public static InputStream getStream(String location) throws IOException {
		URL url = getResource(location);
		if (url == null) {
			return null;
		}
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		return connection.getInputStream();
	}

	/**
	 * 读取资源全部字节。
	 *
	 * @param location 资源位置
	 * @return 资源字节内容
	 * @throws IOException 资源不存在或读取失败
	 */
	public static byte[] readBytes(String location) throws IOException {
		try (InputStream input = requireStream(location)) {
			return IoUtil.readBytes(input);
		}
	}

	/**
	 * 读取资源为 UTF-8 字符串。
	 *
	 * @param location 资源位置
	 * @return 资源文本
	 * @throws IOException 资源不存在或读取失败
	 */
	public static String readUtf8String(String location) throws IOException {
		return readString(location, StandardCharsets.UTF_8);
	}

	/**
	 * 读取资源为字符串。
	 *
	 * @param location 资源位置
	 * @param charset  字符集，{@code null} 时使用 UTF-8
	 * @return 资源文本
	 * @throws IOException 资源不存在或读取失败
	 */
	public static String readString(String location, Charset charset) throws IOException {
		try (InputStream input = requireStream(location)) {
			return IoUtil.read(input, charset == null ? StandardCharsets.UTF_8 : charset);
		}
	}

	/**
	 * 获取资源输入流，不存在时抛出异常。
	 *
	 * @param location 资源位置
	 * @return 输入流
	 * @throws IOException 资源不存在
	 */
	private static InputStream requireStream(String location) throws IOException {
		InputStream input = getStream(location);
		if (input == null) {
			throw new IOException("Resource not found: " + location);
		}
		return input;
	}

	/**
	 * 按类路径查找资源。
	 *
	 * @param path 类路径资源路径
	 * @return 资源 URL；未找到返回 {@code null}
	 */
	private static URL classpathResource(String path) {
		String normalized = path.trim();
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		if (normalized.isEmpty()) {
			return null;
		}
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		URL url = contextLoader == null ? null : contextLoader.getResource(normalized);
		if (url == null) {
			url = ResourceUtil.class.getClassLoader().getResource(normalized);
		}
		if (url == null) {
			url = ResourceUtil.class.getResource("/" + normalized);
		}
		return url;
	}

	/**
	 * 将显式 URL 位置转为 URL。
	 *
	 * @param location URL 位置
	 * @return URL 对象
	 * @throws IllegalArgumentException URL 格式非法
	 */
	private static URL toUrl(String location) {
		try {
			return new URI(location).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalArgumentException("Invalid resource location: " + location, e);
		}
	}

	/**
	 * 忽略大小写判断前缀。
	 *
	 * @param value  待检查字符串
	 * @param prefix 前缀
	 * @return 匹配返回 {@code true}
	 */
	private static boolean startsWithIgnoreCase(String value, String prefix) {
		return value.regionMatches(true, 0, prefix, 0, prefix.length());
	}
}
