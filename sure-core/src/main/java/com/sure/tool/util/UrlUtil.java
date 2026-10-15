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
	 * <p>示例：{@code UrlUtil.encode("a b") -> "a+b"}。</p>
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
	 * <p>示例：{@code UrlUtil.encode("你好", CharsetUtil.UTF_8) -> "%E4%BD%A0%E5%A5%BD"}。</p>
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
	 * <p>示例：{@code UrlUtil.encodePath("a b/c") -> "a%20b%2Fc"}。</p>
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
	 * <p>示例：{@code UrlUtil.decode("a+b") -> "a b"}。</p>
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
	 * <p>示例：{@code UrlUtil.decode("%E4%BD%A0", CharsetUtil.UTF_8) -> "你"}。</p>
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
	 * <p>示例：{@code UrlUtil.toUri("https://example.com/a?b=1")} 返回
	 * {@code URI("https://example.com/a?b=1")}。</p>
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
	 * <p>示例：{@code UrlUtil.getHost("https://example.com:8080/a") -> "example.com"}。</p>
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
	 * <p>示例：{@code UrlUtil.getPort("https://example.com:8080/a") -> 8080}；
	 * {@code UrlUtil.getPort("https://example.com/a") -> -1}。</p>
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
	 * <p>示例：{@code UrlUtil.getScheme("https://example.com") -> "https"}。</p>
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
	 * <p>示例：{@code UrlUtil.getPath("https://example.com/a/b?x=1") -> "/a/b"}。</p>
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
	 * <p>示例：{@code UrlUtil.isHttp("https://example.com") -> true}；
	 * {@code UrlUtil.isHttp("ftp://example.com") -> false}。</p>
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
	 * <p>示例：{@code UrlUtil.getParams("https://example.com?a=1&b=%E4%BD%A0") ->
	 * {"a":"1", "b":"你"}}。</p>
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
	 * <p>示例：{@code UrlUtil.getParam("https://example.com?a=1", "a") -> "1"}；
	 * {@code UrlUtil.getParam("https://example.com", "a") -> null}。</p>
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
	 * <p>示例：{@code UrlUtil.appendParams("https://example.com/a", {"b":"2"}) ->
	 * "https://example.com/a?b=2"}。</p>
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
	 * <p>示例：{@code UrlUtil.normalize("example.com/a/") -> "http://example.com/a"}；
	 * {@code UrlUtil.normalize("https://example.com") -> "https://example.com"}。</p>
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

	/**
	 * 判断是否为 https 协议。
	 *
	 * <p>示例：{@code UrlUtil.isHttps("https://example.com") -> true}；
	 * {@code UrlUtil.isHttps("http://example.com") -> false}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 是否 https；{@code null} 或无法解析返回 {@code false}
	 * @since 1.19.0
	 */
	public static boolean isHttps(String url) {
		return "https".equalsIgnoreCase(getScheme(url));
	}

	/**
	 * 校验是否为合法 http/https URL。
	 *
	 * <p>示例：{@code UrlUtil.isValidUrl("https://example.com/a?b=1") -> true}；
	 * {@code UrlUtil.isValidUrl("not a url") -> false}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 是否合法；{@code null} 返回 {@code false}
	 * @since 1.19.0
	 */
	public static boolean isValidUrl(String url) {
		if (url == null || url.trim().isEmpty()) {
			return false;
		}
		try {
			java.net.URL u = new java.net.URL(url);
			String scheme = u.getProtocol();
			return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
		} catch (java.net.MalformedURLException e) {
			return false;
		}
	}

	/**
	 * 转为 {@link java.net.URL} 对象；非法 URL 抛 {@link RuntimeException}（包装
	 * {@link java.net.MalformedURLException}）。
	 *
	 * <p>示例：{@code UrlUtil.toUrl("https://example.com")} 返回 {@code URL} 对象。</p>
	 *
	 * @param url URL 字符串，可为 {@code null}
	 * @return URL 对象；{@code null} 返回 {@code null}
	 * @throws RuntimeException 非法 URL
	 * @since 1.19.0
	 */
	public static java.net.URL toUrl(String url) {
		if (url == null) {
			return null;
		}
		try {
			return new java.net.URL(url);
		} catch (java.net.MalformedURLException e) {
			throw new RuntimeException("Invalid URL: " + url, e);
		}
	}

	/**
	 * 提取主机域名（去协议、端口、路径与查询参数）。
	 *
	 * <p>示例：{@code UrlUtil.getDomain("https://www.example.com:8080/path?a=1") -> "www.example.com"}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 域名；{@code null} 或无法解析返回 {@code null}
	 * @since 1.19.0
	 */
	public static String getDomain(String url) {
		String host = getHost(url);
		if (host == null) {
			return null;
		}
		// 去除端口
		int colon = host.indexOf(':');
		return colon >= 0 ? host.substring(0, colon) : host;
	}

	/**
	 * 无协议时补全 {@code http://}（有协议或不可识别时原样返回）。
	 *
	 * <p>示例：{@code UrlUtil.toHttpUrl("example.com/a") -> "http://example.com/a"}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 补全后的 URL；{@code null} 返回 {@code null}
	 * @since 1.19.0
	 */
	public static String toHttpUrl(String url) {
		if (url == null) {
			return null;
		}
		String s = url.trim();
		if (s.isEmpty() || s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
			return s;
		}
		return "http://" + s;
	}

	/**
	 * 剥离协议前缀。
	 *
	 * <p>示例：{@code UrlUtil.removeProtocol("https://example.com/a") -> "example.com/a"}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 去协议后的字符串；{@code null} 返回 {@code null}
	 * @since 1.19.0
	 */
	public static String removeProtocol(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.indexOf("://");
		return idx >= 0 ? url.substring(idx + 3) : url;
	}

	/**
	 * 路径规范化：解析 {@code .} 与 {@code ..}、合并连续斜杠（RFC 3986 §5.2.4）。
	 *
	 * <p>示例：{@code UrlUtil.normalizePath("/a/./b") -> "/a/b"}；
	 * {@code UrlUtil.normalizePath("/a/../b") -> "/b"}。</p>
	 *
	 * @param path 路径（含或不含前导斜杠），可为 {@code null}
	 * @return 规范化后的路径；{@code null} 返回 {@code null}
	 * @since 1.19.0
	 */
	public static String normalizePath(String path) {
		if (path == null) {
			return null;
		}
		boolean leadingSlash = path.startsWith("/");
		String[] segments = path.split("/");
		java.util.Deque<String> stack = new java.util.ArrayDeque<>();
		int excess = 0; // 越界 .. 层级（栈空时遇到 ..）
		for (String segment : segments) {
			if (segment.isEmpty() || ".".equals(segment)) {
				continue;
			}
			if ("..".equals(segment)) {
				if (!stack.isEmpty()) {
					stack.removeLast();
				} else if (!leadingSlash) {
					// 相对路径越界 .. 保留；绝对路径（/ 开头）不能越过根，丢弃
					excess++;
				}
				continue;
			}
			stack.addLast(segment);
		}
		StringBuilder sb = new StringBuilder();
		if (leadingSlash) {
			sb.append('/');
		}
		for (int i = 0; i < excess; i++) {
			sb.append("../");
		}
		boolean first = true;
		for (String segment : stack) {
			if (!first) {
				sb.append('/');
			}
			sb.append(segment);
			first = false;
		}
		if (sb.length() == 0) {
			return leadingSlash ? "/" : "";
		}
		// 去掉 excess 补全后可能产生的尾部多余斜杠
		return excess > 0 && sb.charAt(sb.length() - 1) == '/' ? sb.substring(0, sb.length() - 1) : sb.toString();
	}

	/**
	 * 判断两个 URL 是否同源（协议 + 主机 + 端口一致，忽略默认端口差异）。
	 *
	 * <p>示例：{@code UrlUtil.isSameOrigin("https://a.com/x", "https://a.com/y") -> true}；
	 * {@code UrlUtil.isSameOrigin("https://a.com", "https://b.com") -> false}。</p>
	 *
	 * @param url1 URL1，可为 {@code null}
	 * @param url2 URL2，可为 {@code null}
	 * @return 是否同源；任一无法解析返回 {@code false}
	 * @since 1.19.0
	 */
	public static boolean isSameOrigin(String url1, String url2) {
		URI u1 = toUri(url1);
		URI u2 = toUri(url2);
		if (u1 == null || u2 == null || u1.getHost() == null || u2.getHost() == null) {
			return false;
		}
		if (!u1.getScheme().equalsIgnoreCase(u2.getScheme())) {
			return false;
		}
		if (!u1.getHost().equalsIgnoreCase(u2.getHost())) {
			return false;
		}
		int p1 = effectivePort(u1);
		int p2 = effectivePort(u2);
		return p1 == p2;
	}

	private static int effectivePort(URI uri) {
		int port = uri.getPort();
		if (port != -1) {
			return port;
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	/**
	 * 提取查询串（不含 {@code ?}；无查询返回空串）。
	 *
	 * <p>示例：{@code UrlUtil.getQuery("https://example.com/a?b=1&c=2") -> "b=1&c=2"}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 查询串；{@code null} 返回 {@code null}
	 * @since 1.19.0
	 */
	public static String getQuery(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.indexOf('?');
		if (idx < 0 || idx == url.length() - 1) {
			return "";
		}
		return url.substring(idx + 1);
	}

	/**
	 * 提取锚点 fragment（不含 {@code #}；无锚点返回空串）。
	 *
	 * <p>示例：{@code UrlUtil.getFragment("https://example.com/a#section2") -> "section2"}。</p>
	 *
	 * @param url URL，可为 {@code null}
	 * @return 锚点；{@code null} 返回 {@code null}
	 * @since 1.19.0
	 */
	public static String getFragment(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.indexOf('#');
		if (idx < 0 || idx == url.length() - 1) {
			return "";
		}
		return url.substring(idx + 1);
	}
}
