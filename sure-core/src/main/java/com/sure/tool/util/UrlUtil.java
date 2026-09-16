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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL 工具类：编码/解码、解析、参数提取、拼接，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class UrlUtil {

	/** 默认字符集 UTF-8。 */
	public static final Charset DEFAULT_CHARSET = CharsetUtil.UTF_8;

	private UrlUtil() {
	}

	/**
	 * URL 编码（空格编码为 +，表单风格）。
	 *
	 * @param url URL 或参数值
	 * @return 编码结果
	 */
	public static String encode(String url) {
		return encode(url, DEFAULT_CHARSET);
	}

	/**
	 * URL 编码（表单风格，空格→+）。
	 *
	 * @param url     URL 或参数值
	 * @param charset 字符集
	 * @return 编码结果
	 */
	public static String encode(String url, Charset charset) {
		if (url == null) {
			return null;
		}
		try {
			return URLEncoder.encode(url, charset.name());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("不支持的字符集: " + charset, e);
		}
	}

	/**
	 * URL 编码路径片段（空格→%20）。
	 *
	 * @param path 路径
	 * @return 编码结果
	 */
	public static String encodePath(String path) {
		return encode(path).replace("+", "%20");
	}

	/**
	 * URL 解码。
	 *
	 * @param url URL 或参数值
	 * @return 解码结果
	 */
	public static String decode(String url) {
		return decode(url, DEFAULT_CHARSET);
	}

	/**
	 * URL 解码。
	 *
	 * @param url     URL 或参数值
	 * @param charset 字符集
	 * @return 解码结果
	 */
	public static String decode(String url, Charset charset) {
		if (url == null) {
			return null;
		}
		try {
			return URLDecoder.decode(url, charset.name());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("不支持的字符集: " + charset, e);
		}
	}

	/**
	 * 解析 URL 为 URI（非法返回 null）。
	 *
	 * @param url URL
	 * @return URI；解析失败返回 null
	 */
	public static URI toUri(String url) {
		if (url == null) {
			return null;
		}
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * 获取主机。
	 *
	 * @param url URL
	 * @return 主机名
	 */
	public static String getHost(String url) {
		URI uri = toUri(url);
		return uri == null ? null : uri.getHost();
	}

	/**
	 * 获取端口（未指定时返回 -1）。
	 *
	 * @param url URL
	 * @return 端口
	 */
	public static int getPort(String url) {
		URI uri = toUri(url);
		return uri == null ? -1 : uri.getPort();
	}

	/**
	 * 获取协议（scheme）。
	 *
	 * @param url URL
	 * @return 协议
	 */
	public static String getScheme(String url) {
		URI uri = toUri(url);
		return uri == null ? null : uri.getScheme();
	}

	/**
	 * 获取路径。
	 *
	 * @param url URL
	 * @return 路径
	 */
	public static String getPath(String url) {
		URI uri = toUri(url);
		return uri == null ? null : uri.getPath();
	}

	/**
	 * 是否为 HTTP 或 HTTPS 协议。
	 *
	 * @param url URL
	 * @return 是否 http(s)
	 */
	public static boolean isHttp(String url) {
		String scheme = getScheme(url);
		return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
	}

	/**
	 * 获取查询参数。
	 *
	 * @param url URL
	 * @return 参数 Map（保持出现顺序）
	 */
	public static Map<String, String> getParams(String url) {
		Map<String, String> params = new LinkedHashMap<>();
		if (url == null) {
			return params;
		}
		int idx = url.indexOf('?');
		if (idx < 0 || idx == url.length() - 1) {
			return params;
		}
		String query = url.substring(idx + 1);
		for (String pair : query.split("&")) {
			int eq = pair.indexOf('=');
			if (eq >= 0) {
				String key = decode(pair.substring(0, eq));
				String value = decode(pair.substring(eq + 1));
				if (key != null && !key.isEmpty()) {
					params.put(key, value);
				}
			}
		}
		return params;
	}

	/**
	 * 获取单个查询参数。
	 *
	 * @param url  URL
	 * @param name 参数名
	 * @return 参数值；不存在返回 null
	 */
	public static String getParam(String url, String name) {
		return getParams(url).get(name);
	}

	/**
	 * 拼接查询参数到 URL。
	 *
	 * @param url    基础 URL（可含已有 ?query）
	 * @param params 参数
	 * @return 拼接后的 URL
	 */
	public static String appendParams(String url, Map<String, String> params) {
		if (url == null || params == null || params.isEmpty()) {
			return url;
		}
		StringBuilder sb = new StringBuilder(url);
		boolean hasQuery = url.contains("?");
		for (Map.Entry<String, String> e : params.entrySet()) {
			sb.append(hasQuery ? '&' : '?');
			hasQuery = true;
			sb.append(encode(e.getKey())).append('=').append(encode(e.getValue()));
		}
		return sb.toString();
	}

	/**
	 * URL 规范化：缺协议补全 {@code http://}、去除末尾斜杠（保留根路径）。
	 *
	 * @param url URL
	 * @return 规范化后的 URL；{@code null} 返回 {@code null}
	 */
	public static String normalize(String url) {
		if (url == null) {
			return null;
		}
		String s = url.trim();
		if (s.isEmpty()) {
			return s;
		}
		if (!s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
			s = "http://" + s;
		}
		if (s.length() > 1 && s.endsWith("/") && !s.endsWith("://")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
}
