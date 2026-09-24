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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sure.tool.codec.EncodeUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;

/**
 * HTTP 请求工具类，基于 {@link HttpURLConnection} 的零依赖封装，参考 Hutool 的 {@code HttpUtil} 设计。
 * 仅支持 http/https 协议，2xx/3xx 返回响应体，其余状态码抛出 {@link HttpException}。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HttpUtil {

	/** 默认连接超时（毫秒） */
	public static final int DEFAULT_CONNECT_TIMEOUT = 10_000;
	/** 默认读取超时（毫秒） */
	public static final int DEFAULT_READ_TIMEOUT = 10_000;
	/** JSON Content-Type */
	private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	private HttpUtil() {
	}

	/**
	 * GET 请求。
	 *
	 * @param url URL
	 * @return 响应文本（UTF-8）
	 */
	public static String get(String url) {
		return execute("GET", url, null, null, null, null, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * GET 请求（参数拼接到 query）。
	 *
	 * @param url    URL
	 * @param params 参数（值会做 URL 编码）
	 * @return 响应文本（UTF-8）
	 */
	public static String get(String url, Map<String, Object> params) {
		return get(url, params, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * GET 请求。
	 *
	 * @param url           URL
	 * @param params        参数（拼接到 query，值会 URL 编码）
	 * @param timeoutMillis 超时（毫秒，连接与读取共用）
	 * @return 响应文本（UTF-8）
	 */
	public static String get(String url, Map<String, Object> params, int timeoutMillis) {
		return execute("GET", url, params, null, null, null, timeoutMillis);
	}

	/**
	 * POST 表单请求（application/x-www-form-urlencoded）。
	 *
	 * @param url  URL
	 * @param form 表单参数
	 * @return 响应文本（UTF-8）
	 */
	public static String post(String url, Map<String, Object> form) {
		return post(url, form, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * POST 表单请求。
	 *
	 * @param url           URL
	 * @param form          表单参数
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String post(String url, Map<String, Object> form, int timeoutMillis) {
		return execute("POST", url, null, form, null, "application/x-www-form-urlencoded; charset=UTF-8",
				timeoutMillis);
	}

	/**
	 * POST JSON 请求。
	 *
	 * @param url  URL
	 * @param json JSON 字符串
	 * @return 响应文本（UTF-8）
	 */
	public static String postJson(String url, String json) {
		return postJson(url, json, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * POST JSON 请求。
	 *
	 * @param url           URL
	 * @param json          JSON 字符串
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String postJson(String url, String json, int timeoutMillis) {
		return execute("POST", url, null, null, json, "application/json; charset=UTF-8", timeoutMillis);
	}

	/**
	 * POST 原始内容请求。
	 *
	 * @param url         URL
	 * @param body        请求体
	 * @param contentType Content-Type（可含 charset）
	 * @return 响应文本（UTF-8）
	 */
	public static String post(String url, String body, String contentType) {
		return execute("POST", url, null, null, body, contentType, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * 获取响应字节。
	 *
	 * @param url URL
	 * @return 响应字节
	 */
	public static byte[] getBytes(String url) {
		return getBytes(url, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * 获取响应字节。
	 *
	 * @param url           URL
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应字节
	 */
	public static byte[] getBytes(String url, int timeoutMillis) {
		HttpURLConnection conn = null;
		try {
			conn = openConnection("GET", url, null, timeoutMillis);
			InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
			if (in == null) {
				throw new HttpException("无响应内容", conn.getResponseCode());
			}
			return IoUtil.readBytes(in);
		} catch (IOException e) {
			throw new HttpException("请求失败: " + url, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * 下载文件。
	 *
	 * @param url     URL
	 * @param destDir 目标目录（不存在则创建）
	 * @return 下载后的文件
	 */
	public static File download(String url, File destDir) {
		return download(url, destDir, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * 下载文件。
	 *
	 * @param url           URL
	 * @param destDir       目标目录
	 * @param timeoutMillis 超时（毫秒）
	 * @return 下载后的文件
	 */
	public static File download(String url, File destDir, int timeoutMillis) {
		if (destDir == null) {
			throw new IllegalArgumentException("目标目录不能为 null");
		}
		if (!destDir.exists()) {
			FileUtil.mkdir(destDir);
		}
		String fileName = URLUtil.getPath(url);
		int slash = fileName.lastIndexOf('/');
		fileName = slash >= 0 ? fileName.substring(slash + 1) : fileName;
		if (fileName.isEmpty()) {
			throw new HttpException("无法从 URL 推断文件名: " + url);
		}
		File dest = new File(destDir, fileName);
		HttpURLConnection conn = null;
		try {
			conn = openConnection("GET", url, null, timeoutMillis);
			int code = conn.getResponseCode();
			if (code >= 400) {
				throw new HttpException("下载失败: " + url, code);
			}
			try (InputStream in = conn.getInputStream();
					OutputStream out = new FileOutputStream(dest)) {
				IoUtil.copy(in, out);
			}
			return dest;
		} catch (IOException e) {
			throw new HttpException("下载失败: " + url, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * 执行请求。
	 *
	 * @param method        HTTP 方法
	 * @param url           URL
	 * @param params        GET 查询参数（拼接到 query）
	 * @param form          POST 表单参数
	 * @param body          POST 原始请求体（优先级高于 form）
	 * @param contentType   Content-Type
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	private static String execute(String method, String url, Map<String, Object> params, Map<String, Object> form,
			String body, String contentType, int timeoutMillis) {
		byte[] bytes = executeBytes(method, url, params, form, body, contentType, timeoutMillis);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * 执行请求并返回字节。
	 */
	private static byte[] executeBytes(String method, String url, Map<String, Object> params, Map<String, Object> form,
			String body, String contentType, int timeoutMillis) {
		HttpURLConnection conn = null;
		try {
			conn = openConnection(method, url, params, timeoutMillis);
			if (contentType != null) {
				conn.setRequestProperty("Content-Type", contentType);
			}
			String payload = body;
			if (payload == null && form != null && !form.isEmpty()) {
				payload = buildForm(form);
			}
			if (payload != null) {
				conn.setDoOutput(true);
				try (OutputStream out = conn.getOutputStream()) {
					out.write(payload.getBytes(StandardCharsets.UTF_8));
				}
			}
			int code = conn.getResponseCode();
			InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
			if (in == null) {
				throw new HttpException("无响应内容", code);
			}
			byte[] data = IoUtil.readBytes(in);
			if (code >= 400) {
				throw new HttpException("请求失败: " + url + "，" + new String(data, StandardCharsets.UTF_8), code);
			}
			return data;
		} catch (IOException e) {
			throw new HttpException("请求失败: " + url, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * 打开连接并校验协议。
	 */
	private static HttpURLConnection openConnection(String method, String url, Map<String, Object> params,
			int timeoutMillis) throws IOException {
		String target = url;
		if (params != null && !params.isEmpty() && "GET".equals(method)) {
			target = URLUtil.buildUrl(url, params);
		}
		if (!URLUtil.isHttpUrl(target)) {
			throw new IllegalArgumentException("仅支持 http/https 协议: " + target);
		}
		HttpURLConnection conn = (HttpURLConnection) new URL(target).openConnection();
		conn.setRequestMethod(method);
		conn.setConnectTimeout(timeoutMillis);
		conn.setReadTimeout(timeoutMillis);
		conn.setInstanceFollowRedirects(true);
		conn.setRequestProperty("User-Agent", "suretool/0.1");
		conn.setRequestProperty("Accept", "*/*");
		return conn;
	}

	/**
	 * 构建表单字符串。
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
	 * PUT JSON 请求。
	 *
	 * @param url  URL
	 * @param json JSON 字符串
	 * @return 响应文本（UTF-8）
	 */
	public static String putJson(String url, String json) {
		return putJson(url, json, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * PUT JSON 请求。
	 *
	 * @param url           URL
	 * @param json          JSON 字符串
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String putJson(String url, String json, int timeoutMillis) {
		return execute("PUT", url, null, null, json, "application/json; charset=UTF-8", timeoutMillis);
	}

	/**
	 * DELETE JSON 请求。
	 *
	 * @param url  URL
	 * @param json JSON 字符串
	 * @return 响应文本（UTF-8）
	 */
	public static String deleteJson(String url, String json) {
		return deleteJson(url, json, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * DELETE JSON 请求。
	 *
	 * @param url           URL
	 * @param json          JSON 字符串
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String deleteJson(String url, String json, int timeoutMillis) {
		return execute("DELETE", url, null, null, json, "application/json; charset=UTF-8", timeoutMillis);
	}

	/**
	 * DELETE 请求（无请求体）。
	 *
	 * @param url URL
	 * @return 响应文本（UTF-8）
	 */
	public static String delete(String url) {
		return delete(url, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * DELETE 请求（无请求体）。
	 *
	 * @param url           URL
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String delete(String url, int timeoutMillis) {
		return execute("DELETE", url, null, null, null, null, timeoutMillis);
	}

	/**
	 * PUT 表单请求。
	 *
	 * @param url  URL
	 * @param form 表单参数
	 * @return 响应文本（UTF-8）
	 */
	public static String put(String url, Map<String, Object> form) {
		return put(url, form, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * PUT 表单请求。
	 *
	 * @param url           URL
	 * @param form          表单参数
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String put(String url, Map<String, Object> form, int timeoutMillis) {
		return execute("PUT", url, null, form, null, null, timeoutMillis);
	}



	/**
	 * PATCH JSON 请求。
	 *
	 * @param url  URL
	 * @param json JSON 字符串
	 * @return 响应文本（UTF-8）
	 */
	public static String patchJson(String url, String json) {
		return patchJson(url, json, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * PATCH JSON 请求（基于 JDK HttpClient，HttpURLConnection 不支持 PATCH）。
	 *
	 * @param url           URL
	 * @param json          JSON 字符串
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String patchJson(String url, String json, int timeoutMillis) {
		try {
			java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
					.connectTimeout(java.time.Duration.ofMillis(timeoutMillis))
					.build();
			java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
					.uri(java.net.URI.create(url))
					.timeout(java.time.Duration.ofMillis(timeoutMillis))
					.header("Content-Type", "application/json; charset=UTF-8")
					.method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
					.build();
			java.net.http.HttpResponse<String> response = client.send(request,
					java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			return response.body();
		} catch (IOException e) {
			throw new HttpException("PATCH 请求失败: " + url, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new HttpException("PATCH 请求被中断: " + url, e);
		}
	}



	/**
	 * HEAD 请求，返回响应头。
	 *
	 * @param url URL
	 * @return 响应头（键为头部名称，值为值列表）
	 */
	public static Map<String, java.util.List<String>> head(String url) {
		return head(url, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * HEAD 请求，返回响应头。
	 *
	 * @param url           URL
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应头（键为头部名称，值为值列表）
	 */
	public static Map<String, java.util.List<String>> head(String url, int timeoutMillis) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("HEAD");
			conn.setConnectTimeout(timeoutMillis);
			conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
			conn.getResponseCode();
			return conn.getHeaderFields();
		} catch (IOException e) {
			throw new HttpException("HEAD 请求失败: " + url, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}



	/**
	 * multipart/form-data 文件上传。
	 *
	 * @param url       URL
	 * @param form      附加表单字段（可空）
	 * @param fileField 文件字段名
	 * @param file      文件
	 * @return 响应文本（UTF-8）
	 */
	public static String upload(String url, Map<String, Object> form, String fileField, File file) {
		return upload(url, form, fileField, file, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * multipart/form-data 文件上传。
	 *
	 * @param url           URL
	 * @param form          附加表单字段（可空）
	 * @param fileField     文件字段名
	 * @param file          文件
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应文本（UTF-8）
	 */
	public static String upload(String url, Map<String, Object> form, String fileField, File file, int timeoutMillis) {
		String boundary = "----SureToolBoundary" + System.nanoTime();
		try {
			java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
			if (form != null) {
				for (Map.Entry<String, Object> entry : form.entrySet()) {
					writeMultipartField(body, boundary, entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
			if (file != null && file.isFile()) {
				body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
				body.write(("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + file.getName()
						+ "\"\r\n").getBytes(StandardCharsets.UTF_8));
				body.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
				try (InputStream in = new java.io.FileInputStream(file)) {
					in.transferTo(body);
				}
				body.write("\r\n".getBytes(StandardCharsets.UTF_8));
			}
			body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

			HttpURLConnection conn = openConnection("POST", url, null, timeoutMillis);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setFixedLengthStreamingMode(body.size());
			try (OutputStream out = conn.getOutputStream()) {
				body.writeTo(out);
			}
			int code = conn.getResponseCode();
			InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
			if (in == null) {
				throw new HttpException("无响应内容", code);
			}
			byte[] data = IoUtil.readBytes(in);
			if (code >= 400) {
				throw new HttpException("请求失败: " + url + "，" + new String(data, StandardCharsets.UTF_8), code);
			}
			return new String(data, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new HttpException("上传失败: " + url, e);
		}
	}

	private static void writeMultipartField(java.io.ByteArrayOutputStream body, String boundary, String name,
			String value) throws IOException {
		body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		body.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		body.write(value.getBytes(StandardCharsets.UTF_8));
		body.write("\r\n".getBytes(StandardCharsets.UTF_8));
	}



	/**
	 * 下载 URL 内容为字节数组。
	 *
	 * @param url 请求地址
	 * @return 响应字节
	 * @throws java.io.IOException IO 异常
	 */
	public static byte[] downloadBytes(String url) throws java.io.IOException {
		return downloadBytes(url, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * 下载 URL 内容为字节数组。
	 *
	 * @param url          请求地址
	 * @param timeoutMillis 超时毫秒
	 * @return 响应字节
	 * @throws java.io.IOException IO 异常
	 */
	public static byte[] downloadBytes(String url, int timeoutMillis) throws java.io.IOException {
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
		conn.setConnectTimeout(timeoutMillis);
		conn.setReadTimeout(timeoutMillis);
		conn.setRequestMethod("GET");
		int code = conn.getResponseCode();
		if (code >= 200 && code < 300) {
			try (java.io.InputStream in = conn.getInputStream()) {
				return in.readAllBytes();
			} finally {
				conn.disconnect();
			}
		}
		conn.disconnect();
		throw new HttpException("HTTP " + code + " for " + url);
	}



	/**
	 * GET JSON 请求。
	 *
	 * @param url URL
	 * @return 响应体
	 */
	public static String getJson(String url) {
		return getJson(url, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * GET JSON 请求。
	 *
	 * @param url           URL
	 * @param timeoutMillis 超时（毫秒）
	 * @return 响应体
	 */
	public static String getJson(String url, int timeoutMillis) {
		return execute("GET", url, null, null, null, "application/json; charset=UTF-8", timeoutMillis);
	}

	/**
	 * GET 请求（走代理）。
	 *
	 * @param url   URL
	 * @param proxy 代理（可为 {@code null}）
	 * @return 响应文本（UTF-8）
	 */
	public static String get(String url, Proxy proxy) {
		return HttpRequest.get(url).proxy(proxy).execute().body();
	}

	/**
	 * POST 表单请求（走代理）。
	 *
	 * @param url   URL
	 * @param form  表单
	 * @param proxy 代理（可为 {@code null}）
	 * @return 响应文本（UTF-8）
	 */
	public static String post(String url, Map<String, Object> form, Proxy proxy) {
		return HttpRequest.post(url).proxy(proxy).form(form).execute().body();
	}

	/**
	 * POST JSON 请求（走代理）。
	 *
	 * @param url   URL
	 * @param json  JSON 字符串
	 * @param proxy 代理（可为 {@code null}）
	 * @return 响应文本（UTF-8）
	 */
	public static String postJson(String url, String json, Proxy proxy) {
		return HttpRequest.post(url).proxy(proxy).body(json, JSON_CONTENT_TYPE).execute().body();
	}

	/**
	 * multipart/form-data 文件上传（走代理）。
	 *
	 * @param url       URL
	 * @param form      附加表单字段（可空）
	 * @param fileField 文件字段名
	 * @param file      文件
	 * @param proxy     代理（可为 {@code null}）
	 * @return 响应文本（UTF-8）
	 */
	public static String upload(String url, Map<String, Object> form, String fileField, File file, Proxy proxy) {
		HttpRequest request = HttpRequest.post(url).proxy(proxy);
		if (form != null) {
			request.form(form);
		}
		if (file != null && file.isFile()) {
			request.form(fileField, file);
		}
		return request.execute().body();
	}

}