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

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话级 Cookie 管理器（零第三方依赖，基于 JDK {@link HttpCookie}）。
 * <p>
 * 与 {@link HttpRequest#cookieStore(CookieStore)} 配合实现登录态保持：
 * 请求自动携带已收集的 Cookie，响应中的 {@code Set-Cookie} 自动入库。
 *
 * @author suretool
 * @since 1.1.0
 */
public class CookieStore {

	private final Map<String, List<HttpCookie>> cookies = new LinkedHashMap<>();

	/**
	 * 解析并保存一个 {@code Set-Cookie} 响应头（支持逗号分隔多 cookie 的常见形式）。
	 *
	 * @param host 目标主机（如 {@code example.com}）
	 * @param setCookieHeader Set-Cookie 头原文
	 * @return 本 store
	 */
	public CookieStore add(String host, String setCookieHeader) {
		if (host == null || setCookieHeader == null || setCookieHeader.isEmpty()) {
			return this;
		}
		try {
			for (HttpCookie cookie : HttpCookie.parse(setCookieHeader)) {
				cookies.computeIfAbsent(host, k -> new ArrayList<>()).add(cookie);
			}
		} catch (IllegalArgumentException ignored) {
			// 非法 cookie 头直接忽略，不影响请求流程
		}
		return this;
	}

	/**
	 * 直接添加一个 Cookie。
	 *
	 * @param host   目标主机
	 * @param cookie Cookie 对象
	 * @return 本 store
	 */
	public CookieStore add(String host, HttpCookie cookie) {
		if (host != null && cookie != null) {
			cookies.computeIfAbsent(host, k -> new ArrayList<>()).add(cookie);
		}
		return this;
	}

	/**
	 * 构建指定主机的 Cookie 请求头（自动剔除已过期项），无可用 Cookie 时返回 {@code null}。
	 *
	 * @param host 目标主机
	 * @return Cookie 头原文或 {@code null}
	 */
	public String header(String host) {
		if (host == null) {
			return null;
		}
		List<HttpCookie> list = cookies.get(host);
		if (list == null || list.isEmpty()) {
			return null;
		}
		String value = list.stream()
				.filter(c -> c.hasExpired() == false)
				.map(c -> c.getName() + "=" + c.getValue())
				.collect(Collectors.joining("; "));
		return value.isEmpty() ? null : value;
	}

	/**
	 * 收集响应中的 Set-Cookie 头。
	 *
	 * @param url          请求 URL（用于提取主机）
	 * @param headerValues 响应头映射（key → 值列表）
	 * @return 本 store
	 */
	public CookieStore collect(String url, Map<String, List<String>> headerValues) {
		if (url == null || headerValues == null) {
			return this;
		}
		String host = hostOf(url);
		for (Map.Entry<String, List<String>> entry : headerValues.entrySet()) {
			if (entry.getKey() != null && "set-cookie".equalsIgnoreCase(entry.getKey())) {
				for (String value : entry.getValue()) {
					add(host, value);
				}
			}
		}
		return this;
	}

	/**
	 * 清空全部 Cookie。
	 */
	public void clear() {
		cookies.clear();
	}

	/**
	 * 是否为空。
	 *
	 * @return true 表示无任何 Cookie
	 */
	public boolean isEmpty() {
		return cookies.isEmpty() || cookies.values().stream().allMatch(List::isEmpty);
	}

	/**
	 * 从 URL 提取主机名。
	 *
	 * @param url URL
	 * @return 主机名
	 */
	static String hostOf(String url) {
		try {
			return new URI(url).getHost();
		} catch (Exception e) {
			return null;
		}
	}
}
