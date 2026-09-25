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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sure.tool.codec.EncodeUtil;
import com.sure.tool.io.IoUtil;

/**
 * HTTP 请求（链式构建）：URL/请求头/query/表单/body/超时/重定向，基于 {@link HttpURLConnection} 的零依赖封装，
 * 参考 Hutool 的 {@code HttpRequest} 设计。
 *
 * <pre>{@code
 * String body = HttpRequest.get("https://api.example.com/users")
 *         .query("page", 1)
 *         .header("Authorization", "Bearer xxx")
 *         .execute().body();
 * }</pre>
 *
 * 与 {@link HttpUtil} 的差异：{@link #execute()} 不因非 2xx 状态码抛异常，响应状态由 {@link HttpResponse#isOk()} 判断。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HttpRequest {

	/** 默认连接超时（毫秒） */
	public static final int DEFAULT_CONNECT_TIMEOUT = 10_000;
	/** 默认读取超时（毫秒） */
	public static final int DEFAULT_READ_TIMEOUT = 10_000;

	/** 默认 User-Agent */
	private static final String DEFAULT_USER_AGENT = "suretool/0.1";
	/** 默认表单 Content-Type */
	private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
	/** multipart Content-Type 前缀 */
	private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
	/** 默认 JSON Content-Type */
	private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	/** 默认二进制 Content-Type */
	private static final String OCTET_CONTENT_TYPE = "application/octet-stream";
	/** multipart 边界 */
	private final String boundary = "suretool-" + UUID.randomUUID().toString().replace("-", "");

	private String url;
	private String method;
	private final Map<String, String> headers = new LinkedHashMap<>();
	private final Map<String, Object> queryParams = new LinkedHashMap<>();
	private final Map<String, Object> formParams = new LinkedHashMap<>();
	private final Map<String, File> fileParams = new LinkedHashMap<>();
	private byte[] bodyBytes;
	private String contentType;
	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	private boolean followRedirects = true;
	private String userAgent = DEFAULT_USER_AGENT;
	private Proxy proxy;
	private String cookieHeader;
	private CookieStore cookieStore;
	private java.net.http.HttpClient httpClient;

	private HttpRequest(String url, String method) {
		this.url = url;
		this.method = method;
	}

	/**
	 * GET 请求。
	 *
	 * @param url URL
	 * @return 请求构建器
	 */
	public static HttpRequest get(String url) {
		return of(url, "GET");
	}

	/**
	 * POST 请求。
	 *
	 * @param url URL
	 * @return 请求构建器
	 */
	public static HttpRequest post(String url) {
		return of(url, "POST");
	}

	/**
	 * PUT 请求。
	 *
	 * @param url URL
	 * @return 请求构建器
	 */
	public static HttpRequest put(String url) {
		return of(url, "PUT");
	}

	/**
	 * DELETE 请求。
	 *
	 * @param url URL
	 * @return 请求构建器
	 */
	public static HttpRequest delete(String url) {
		return of(url, "DELETE");
	}

	/**
	 * 任意方法请求（JDK {@link HttpURLConnection} 仅支持 GET/POST/PUT/DELETE/HEAD/OPTIONS/TRACE 等标准方法，
	 * 传入 PATCH 等 JDK 不支持的方法将在 {@link #execute()} 时抛 {@link HttpException}）。
	 *
	 * @param url    URL
	 * @param method HTTP 方法（自动转大写）
	 * @return 请求构建器
	 */
	public static HttpRequest of(String url, String method) {
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL 不能为空");
		}
		if (method == null || method.trim().isEmpty()) {
			throw new IllegalArgumentException("HTTP 方法不能为空");
		}
		return new HttpRequest(url, method.trim().toUpperCase());
	}

	/**
	 * 修改 URL。
	 *
	 * @param url URL
	 * @return 本构建器
	 */
	public HttpRequest url(String url) {
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL 不能为空");
		}
		this.url = url;
		return this;
	}

	/**
	 * 修改 HTTP 方法（自动转大写）。
	 *
	 * @param method HTTP 方法
	 * @return 本构建器
	 */
	public HttpRequest method(String method) {
		if (method == null || method.trim().isEmpty()) {
			throw new IllegalArgumentException("HTTP 方法不能为空");
		}
		this.method = method.trim().toUpperCase();
		return this;
	}

	/**
	 * 添加请求头（同名覆盖）。
	 *
	 * @param name  头名
	 * @param value 头值
	 * @return 本构建器
	 */
	public HttpRequest header(String name, Object value) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("请求头名不能为空");
		}
		headers.put(name.trim(), value == null ? "" : String.valueOf(value));
		return this;
	}

	/**
	 * 批量添加请求头。
	 *
	 * @param headers 请求头
	 * @return 本构建器
	 */
	public HttpRequest header(Map<String, ?> headers) {
		if (headers != null) {
			for (Map.Entry<String, ?> entry : headers.entrySet()) {
				header(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}

	/**
	 * 添加 query 参数（GET 拼接 URL，值会 URL 编码）。
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 本构建器
	 */
	public HttpRequest query(String name, Object value) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("参数名不能为空");
		}
		queryParams.put(name.trim(), value);
		return this;
	}

	/**
	 * 批量添加 query 参数。
	 *
	 * @param params 参数
	 * @return 本构建器
	 */
	public HttpRequest query(Map<String, Object> params) {
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				query(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}

	/**
	 * 添加表单参数（POST 发送 application/x-www-form-urlencoded，值会 URL 编码）。
	 * 值为 {@link File} 时自动转入文件字段，请求升级为 multipart/form-data 编码。
	 *
	 * @param name  参数名
	 * @param value 参数值或文件
	 * @return 本构建器
	 */
	public HttpRequest form(String name, Object value) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("参数名不能为空");
		}
		if (value instanceof File file) {
			fileParams.put(name.trim(), file);
		} else {
			formParams.put(name.trim(), value);
		}
		return this;
	}

	/**
	 * 批量添加表单参数。
	 *
	 * @param params 参数
	 * @return 本构建器
	 */
	public HttpRequest form(Map<String, Object> params) {
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				form(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}

	/**
	 * 设置请求体（文本，默认 Content-Type 为 application/json；与 form 同时设置时 body 优先）。
	 *
	 * @param body 请求体
	 * @return 本构建器
	 */
	public HttpRequest body(String body) {
		return body(body, JSON_CONTENT_TYPE);
	}

	/**
	 * 设置请求体（文本 + 显式 Content-Type）。
	 *
	 * @param body        请求体
	 * @param contentType Content-Type
	 * @return 本构建器
	 */
	public HttpRequest body(String body, String contentType) {
		this.bodyBytes = body == null ? null : body.getBytes(StandardCharsets.UTF_8);
		this.contentType = contentType;
		return this;
	}

	/**
	 * 设置请求体（字节，默认 Content-Type 为 application/octet-stream）。
	 *
	 * @param body 请求体
	 * @return 本构建器
	 */
	public HttpRequest body(byte[] body) {
		return body(body, OCTET_CONTENT_TYPE);
	}

	/**
	 * 设置请求体（字节 + 显式 Content-Type）。
	 *
	 * @param body        请求体（内部防御性拷贝）
	 * @param contentType Content-Type
	 * @return 本构建器
	 */
	public HttpRequest body(byte[] body, String contentType) {
		this.bodyBytes = body == null ? null : body.clone();
		this.contentType = contentType;
		return this;
	}

	/**
	 * 显式设置 Content-Type（覆盖 body/form 推断的默认值）。
	 *
	 * @param contentType Content-Type
	 * @return 本构建器
	 */
	public HttpRequest contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * 同时设置连接与读取超时（毫秒）。
	 *
	 * @param timeoutMillis 超时（毫秒）
	 * @return 本构建器
	 */
	public HttpRequest timeout(int timeoutMillis) {
		this.connectTimeout = timeoutMillis;
		this.readTimeout = timeoutMillis;
		return this;
	}

	/**
	 * 设置连接超时（毫秒）。
	 *
	 * @param timeoutMillis 超时（毫秒）
	 * @return 本构建器
	 */
	public HttpRequest connectTimeout(int timeoutMillis) {
		this.connectTimeout = timeoutMillis;
		return this;
	}

	/**
	 * 设置读取超时（毫秒）。
	 *
	 * @param timeoutMillis 超时（毫秒）
	 * @return 本构建器
	 */
	public HttpRequest readTimeout(int timeoutMillis) {
		this.readTimeout = timeoutMillis;
		return this;
	}

	/**
	 * 是否自动跟随重定向（默认 true）。
	 *
	 * @param followRedirects 是否跟随
	 * @return 本构建器
	 */
	public HttpRequest followRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * 设置 User-Agent（默认 suretool/0.1）。
	 *
	 * @param userAgent User-Agent
	 * @return 本构建器
	 */
	public HttpRequest userAgent(String userAgent) {
		this.userAgent = userAgent == null || userAgent.trim().isEmpty() ? DEFAULT_USER_AGENT : userAgent;
		return this;
	}

	/**
	 * 设置代理（主机 + 端口，HTTP 代理）。
	 *
	 * @param host 代理主机
	 * @param port 代理端口
	 * @return 本构建器
	 */
	public HttpRequest proxy(String host, int port) {
		this.proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress(host, port));
		return this;
	}

	/**
	 * 设置代理（任意 {@link Proxy}，支持 HTTP/SOCKS）。
	 *
	 * @param proxy 代理
	 * @return 本构建器
	 */
	public HttpRequest proxy(Proxy proxy) {
		if (proxy == null) {
			this.proxy = null;
		} else {
			// 防御性拷贝，避免外部修改内部表示
			this.proxy = new Proxy(proxy.type(), proxy.address());
		}
		return this;
	}

	/**
	 * 设置 Cookie（单对键值）。
	 *
	 * @param name  Cookie 名
	 * @param value Cookie 值
	 * @return 本构建器
	 */
	public HttpRequest cookie(String name, Object value) {
		return cookie(name + "=" + (value == null ? "" : value));
	}

	/**
	 * 设置 Cookie 头（原始值，如 {@code "name=value; Path=/"}）。
	 *
	 * @param cookie Cookie 头原文
	 * @return 本构建器
	 */
	public HttpRequest cookie(String cookie) {
		this.cookieHeader = cookie;
		return this;
	}

	/**
	 * 绑定会话级 Cookie 管理器：请求自动携带 store 内 Cookie，响应中的 Set-Cookie 自动收集。
	 *
	 * @param store Cookie 管理器（可为空，表示不启用会话保持）
	 * @return 本构建器
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
	public HttpRequest cookieStore(CookieStore store) {
		this.cookieStore = store;
		return this;
	}

	/**
	 * 注入 JDK {@link java.net.http.HttpClient}（自带连接池/keep-alive 复用，虚拟线程友好）。
	 * 注入后 {@link #execute()} 走 HttpClient 引擎；未注入时走 {@link HttpURLConnection}。
	 *
	 * @param httpClient JDK HttpClient
	 * @return 本构建器
	 */
	public HttpRequest client(java.net.http.HttpClient httpClient) {
		this.httpClient = httpClient;
		return this;
	}

	/**
	 * 便捷注入：通过 {@link HttpClientBuilder} 构建带连接池的 JDK {@link java.net.http.HttpClient}。
	 *
	 * @param builder 连接池构建器
	 * @return 本构建器
	 */
	public HttpRequest pool(HttpClientBuilder builder) {
		return client(builder.build());
	}

	/**
	 * 执行请求。网络错误抛 {@link HttpException}；非 2xx 状态码不抛异常，由 {@link HttpResponse#isOk()} 判断。
	 *
	 * @return 响应
	 */
	public HttpResponse execute() {
		String target = queryParams.isEmpty() ? url : URLUtil.buildUrl(url, queryParams);
		if (!URLUtil.isHttpUrl(target)) {
			throw new IllegalArgumentException("仅支持 http/https 协议: " + target);
		}
		if (httpClient != null) {
			return executeWithHttpClient(target);
		}
		return executeWithUrlConnection(target);
	}

	/**
	 * JDK HttpClient 引擎：复用连接池、支持虚拟线程 executor、代理。
	 */
	private HttpResponse executeWithHttpClient(String target) {
		try {
			java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder(new URI(target));
			builder.method(method, bodyPublisher());
			builder.header("User-Agent", userAgent);
			builder.header("Accept", "*/*");
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.header(entry.getKey(), entry.getValue());
			}
			String sessionCookie = cookieStore == null ? null : cookieStore.header(CookieStore.hostOf(target));
			if (cookieHeader != null) {
				builder.header("Cookie", cookieHeader);
			}
			if (sessionCookie != null) {
				builder.header("Cookie", sessionCookie);
			}
			if (contentType != null) {
				builder.header("Content-Type", contentType);
			}
			builder.timeout(Duration.ofMillis(Math.max(connectTimeout, readTimeout)));
			java.net.http.HttpResponse<byte[]> resp = httpClient.send(builder.build(),
					java.net.http.HttpResponse.BodyHandlers.ofByteArray());
			Map<String, List<String>> headerMap = new LinkedHashMap<>();
			resp.headers().map().forEach(headerMap::put);
			if (cookieStore != null) {
				cookieStore.collect(target, headerMap);
			}
			return new HttpResponse(resp.statusCode(), target, headerMap, resp.body());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new HttpException("请求失败: " + url, e);
		} catch (java.io.IOException | java.net.URISyntaxException e) {
			throw new HttpException("请求失败: " + url, e);
		}
	}

	/**
	 * HttpURLConnection 引擎（默认，零依赖）。
	 */
	private HttpResponse executeWithUrlConnection(String target) {
		HttpURLConnection conn = null;
		try {
			conn = proxy == null ? (HttpURLConnection) new URL(target).openConnection()
					: (HttpURLConnection) new URL(target).openConnection(proxy);
			conn.setRequestMethod(method);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setInstanceFollowRedirects(followRedirects);
			conn.setRequestProperty("User-Agent", userAgent);
			conn.setRequestProperty("Accept", "*/*");
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
			String sessionCookie = cookieStore == null ? null : cookieStore.header(CookieStore.hostOf(target));
			if (cookieHeader != null) {
				conn.setRequestProperty("Cookie", cookieHeader);
			}
			if (sessionCookie != null) {
				conn.setRequestProperty("Cookie", sessionCookie);
			}
			String effectiveContentType = contentType;
			if (bodyBytes == null && !fileParams.isEmpty() && effectiveContentType == null) {
				effectiveContentType = MULTIPART_CONTENT_TYPE + "; boundary=" + boundary;
			}
			if (bodyBytes == null && !formParams.isEmpty() && fileParams.isEmpty() && effectiveContentType == null) {
				effectiveContentType = FORM_CONTENT_TYPE;
			}
			if (effectiveContentType != null) {
				conn.setRequestProperty("Content-Type", effectiveContentType);
			}
			byte[] payload = bodyBytes;
			if (payload == null && !fileParams.isEmpty()) {
				payload = buildMultipart();
			}
			if (payload == null && !formParams.isEmpty() && fileParams.isEmpty()) {
				payload = buildForm(formParams).getBytes(StandardCharsets.UTF_8);
			}
			if (payload != null) {
				conn.setDoOutput(true);
				try (OutputStream out = conn.getOutputStream()) {
					out.write(payload);
				}
			}
			if (cookieStore != null) {
				cookieStore.collect(target, conn.getHeaderFields());
			}
			int code = conn.getResponseCode();
			InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
			byte[] data = in == null ? new byte[0] : IoUtil.readBytes(in);
			return new HttpResponse(code, conn.getURL().toString(), conn.getHeaderFields(), data);
		} catch (IOException e) {
			throw new HttpException("请求失败: " + url, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * HttpClient 引擎的请求体：multipart / 表单 / body 三选一。
	 */
	private java.net.http.HttpRequest.BodyPublisher bodyPublisher() {
		if (bodyBytes != null) {
			return java.net.http.HttpRequest.BodyPublishers.ofByteArray(bodyBytes);
		}
		if (!fileParams.isEmpty()) {
			return java.net.http.HttpRequest.BodyPublishers.ofByteArray(buildMultipart());
		}
		if (!formParams.isEmpty()) {
			return java.net.http.HttpRequest.BodyPublishers.ofString(buildForm(formParams), StandardCharsets.UTF_8);
		}
		return java.net.http.HttpRequest.BodyPublishers.noBody();
	}

	/**
	 * 构建表单字符串（键值均 URL 编码，null 值为空串）。
	 */
	private static String buildForm(Map<String, Object> form) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : form.entrySet()) {
			if (sb.length() > 0) {
				sb.append('&');
			}
			sb.append(EncodeUtil.encode(entry.getKey())).append('=');
			if (entry.getValue() != null) {
				sb.append(EncodeUtil.encode(String.valueOf(entry.getValue())));
			}
		}
		return sb.toString();
	}

	/**
	 * 构建 multipart/form-data 请求体（文本字段 + 文件字段，UTF-8，边界分隔）。
	 */
	private byte[] buildMultipart() {
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			for (Map.Entry<String, Object> entry : formParams.entrySet()) {
				buf.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
				buf.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
				buf.write(String.valueOf(entry.getValue()).getBytes(StandardCharsets.UTF_8));
				buf.write("\r\n".getBytes(StandardCharsets.UTF_8));
			}
			for (Map.Entry<String, File> entry : fileParams.entrySet()) {
				File file = entry.getValue();
				buf.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
				buf.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
				buf.write(("Content-Type: " + contentTypeOf(file) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
				try (FileInputStream in = new FileInputStream(file)) {
					IoUtil.copy(in, buf);
				}
				buf.write("\r\n".getBytes(StandardCharsets.UTF_8));
			}
			buf.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
			return buf.toByteArray();
		} catch (IOException e) {
			throw new HttpException("multipart 构建失败", e);
		}
	}

	/**
	 * 按文件扩展名推断 Content-Type，无法识别时回退 application/octet-stream。
	 */
	private static String contentTypeOf(File file) {
		String name = file.getName().toLowerCase();
		int idx = name.lastIndexOf('.');
		if (idx >= 0) {
			String ext = name.substring(idx);
			switch (ext) {
			case ".txt":
			case ".md":
			case ".csv":
				return "text/plain";
			case ".json":
				return "application/json";
			case ".xml":
				return "application/xml";
			case ".png":
				return "image/png";
			case ".jpg":
			case ".jpeg":
				return "image/jpeg";
			case ".gif":
				return "image/gif";
			case ".pdf":
				return "application/pdf";
			case ".zip":
				return "application/zip";
			default:
				return OCTET_CONTENT_TYPE;
			}
		}
		return OCTET_CONTENT_TYPE;
	}
}
