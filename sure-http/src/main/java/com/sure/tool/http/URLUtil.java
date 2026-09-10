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
package com.sure.tool.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sure.tool.codec.EncodeUtil;

/**
 * URL 工具类：域名/路径/参数提取与拼接，参考 Hutool 的 {@code URLUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class URLUtil {

	private URLUtil() {
	}

	/**
	 * 是否 http/https 链接。
	 *
	 * @param url URL
	 * @return 是否 http(s) 链接
	 */
	public static boolean isHttpUrl(String url) {
		if (url == null) {
			return false;
		}
		String lower = url.trim().toLowerCase();
		return lower.startsWith("http://") || lower.startsWith("https://");
	}

	/**
	 * 获取主机（含端口，非默认端口时）。
	 *
	 * @param url URL
	 * @return 主机名:端口 或主机名
	 */
	public static String getHost(String url) {
		URI uri = toUri(url);
		int port = uri.getPort();
		if (port > 0 && port != 80 && port != 443) {
			return uri.getHost() + ":" + port;
		}
		return uri.getHost();
	}

	/**
	 * 获取域名（不含端口）。
	 *
	 * @param url URL
	 * @return 域名
	 */
	public static String getDomain(String url) {
		return toUri(url).getHost();
	}

	/**
	 * 获取路径（不含 query）。
	 *
	 * @param url URL
	 * @return 路径，可能为空
	 */
	public static String getPath(String url) {
		return toUri(url).getPath();
	}

	/**
	 * 获取 query 字符串（不含问号），无 query 返回 {@code null}。
	 *
	 * @param url URL
	 * @return query 字符串或 {@code null}
	 */
	public static String getQuery(String url) {
		return toUri(url).getQuery();
	}

	/**
	 * 解析 query 参数为 Map（键值均解码）。
	 *
	 * @param url URL
	 * @return 参数 Map（空 query 返回空 Map）
	 */
	public static Map<String, String> getParams(String url) {
		Map<String, String> params = new LinkedHashMap<>();
		String query = getQuery(url);
		if (query == null || query.isEmpty()) {
			return params;
		}
		for (String pair : query.split("&")) {
			int eq = pair.indexOf('=');
			if (eq < 0) {
				params.put(EncodeUtil.decode(pair), "");
			} else {
				params.put(EncodeUtil.decode(pair.substring(0, eq)),
						EncodeUtil.decode(pair.substring(eq + 1)));
			}
		}
		return params;
	}

	/**
	 * 获取单个 query 参数值，不存在返回 {@code null}。
	 *
	 * @param url  URL
	 * @param name 参数名
	 * @return 参数值或 {@code null}
	 */
	public static String getParam(String url, String name) {
		if (name == null) {
			return null;
		}
		return getParams(url).get(name);
	}

	/**
	 * 拼接 URL 与参数（值会 URL 编码），自动处理已有 query。
	 *
	 * @param base   基础 URL
	 * @param params 参数
	 * @return 拼接后的 URL
	 */
	public static String buildUrl(String base, Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return base;
		}
		StringBuilder sb = new StringBuilder(base);
		boolean hasQuery = base.contains("?");
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			sb.append(hasQuery ? '&' : '?');
			hasQuery = true;
			sb.append(EncodeUtil.encode(entry.getKey())).append('=');
			if (entry.getValue() != null) {
				sb.append(EncodeUtil.encode(String.valueOf(entry.getValue())));
			}
		}
		return sb.toString();
	}

	/**
	 * 解析 URI，失败抛出参数异常。
	 */
	private static URI toUri(String url) {
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL 不能为空");
		}
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("非法 URL: " + url, e);
		}
	}
}