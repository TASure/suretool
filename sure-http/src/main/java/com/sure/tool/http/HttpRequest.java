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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

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
	/** 默认 JSON Content-Type */
	private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	/** 默认二进制 Content-Type */
	private static final String OCTET_CONTENT_TYPE = "application/octet-stream";

	private String url;
	private String method;
	private final Map<String, String> headers = new LinkedHashMap<>();
	private final Map<String, Object> queryParams = new LinkedHashMap<>();
	private final Map<String, Object> formParams = new LinkedHashMap<>();
	private byte[] bodyBytes;
	private String contentType;
	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	private boolean followRedirects = true;
	private String userAgent = DEFAULT_USER_AGENT;

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
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 本构建器
	 */
	public HttpRequest form(String name, Object value) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("参数名不能为空");
		}
		formParams.put(name.trim(), value);
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
	 * 执行请求。网络错误抛 {@link HttpException}；非 2xx 状态码不抛异常，由 {@link HttpResponse#isOk()} 判断。
	 *
	 * @return 响应
	 */
	public HttpResponse execute() {
		HttpURLConnection conn = null;
		try {
			String target = queryParams.isEmpty() ? url : URLUtil.buildUrl(url, queryParams);
			if (!URLUtil.isHttpUrl(target)) {
				throw new IllegalArgumentException("仅支持 http/https 协议: " + target);
			}
			conn = (HttpURLConnection) new URL(target).openConnection();
			conn.setRequestMethod(method);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setInstanceFollowRedirects(followRedirects);
			conn.setRequestProperty("User-Agent", userAgent);
			conn.setRequestProperty("Accept", "*/*");
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
			String effectiveContentType = contentType;
			if (bodyBytes == null && !formParams.isEmpty() && effectiveContentType == null) {
				effectiveContentType = FORM_CONTENT_TYPE;
			}
			if (effectiveContentType != null) {
				conn.setRequestProperty("Content-Type", effectiveContentType);
			}
			byte[] payload = bodyBytes;
			if (payload == null && !formParams.isEmpty()) {
				payload = buildForm(formParams).getBytes(StandardCharsets.UTF_8);
			}
			if (payload != null) {
				conn.setDoOutput(true);
				try (OutputStream out = conn.getOutputStream()) {
					out.write(payload);
				}
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
}
