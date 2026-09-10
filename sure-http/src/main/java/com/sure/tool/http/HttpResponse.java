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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 响应：状态码、响应头、响应体（已完整读取并缓存，连接已释放），参考 Hutool 的 {@code HttpResponse} 设计。
 * 与 {@link HttpUtil} 的"非 2xx 抛异常"语义不同：本类保留任意状态码，由调用方通过 {@link #isOk()} 判断。
 * 响应体字节、响应头均为不可变视图。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HttpResponse {

	private final int statusCode;
	private final String url;
	private final Map<String, List<String>> headers;
	private final byte[] bodyBytes;

	/**
	 * 包内构造（由 {@link HttpRequest#execute()} 创建）。
	 *
	 * @param statusCode 状态码
	 * @param url        最终 URL（重定向后）
	 * @param headers    原始响应头（过滤状态行，值深拷贝为不可变 List）
	 * @param bodyBytes  响应体字节
	 */
	HttpResponse(int statusCode, String url, Map<String, List<String>> headers, byte[] bodyBytes) {
		this.statusCode = statusCode;
		this.url = url;
		Map<String, List<String>> copy = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (entry.getKey() == null) {
				continue;
			}
			copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
		}
		this.headers = Collections.unmodifiableMap(copy);
		this.bodyBytes = bodyBytes == null ? new byte[0] : bodyBytes;
	}

	/**
	 * 响应状态码。
	 *
	 * @return 状态码
	 */
	public int getStatus() {
		return statusCode;
	}

	/**
	 * 是否 2xx 成功。
	 *
	 * @return 是否成功
	 */
	public boolean isOk() {
		return statusCode >= 200 && statusCode < 300;
	}

	/**
	 * 最终请求 URL（跟随重定向后）。
	 *
	 * @return URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * 全部响应头（大小写保持原始，值为不可变 List）。
	 *
	 * @return 响应头 Map
	 */
	public Map<String, List<String>> headers() {
		return headers;
	}

	/**
	 * 指定响应头的全部值（大小写不敏感），不存在返回空 List。
	 *
	 * @param name 头名
	 * @return 头值列表
	 */
	public List<String> headerValues(String name) {
		if (name == null) {
			return Collections.emptyList();
		}
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 指定响应头的第一个值（大小写不敏感），不存在返回 {@code null}。
	 *
	 * @param name 头名
	 * @return 头值或 {@code null}
	 */
	public String header(String name) {
		List<String> values = headerValues(name);
		return values.isEmpty() ? null : values.get(0);
	}

	/**
	 * Content-Type 响应头，无则返回 {@code null}。
	 *
	 * @return Content-Type 或 {@code null}
	 */
	public String contentType() {
		return header("Content-Type");
	}

	/**
	 * 响应字符集：从 Content-Type 解析，解析失败或无 charset 时回退 UTF-8。
	 *
	 * @return 字符集
	 */
	public Charset getCharset() {
		String contentType = contentType();
		if (contentType != null) {
			int idx = contentType.toLowerCase().indexOf("charset=");
			if (idx >= 0) {
				String charset = contentType.substring(idx + "charset=".length()).trim();
				int semi = charset.indexOf(';');
				if (semi >= 0) {
					charset = charset.substring(0, semi).trim();
				}
				if (charset.length() >= 2 && charset.startsWith("\"") && charset.endsWith("\"")) {
					charset = charset.substring(1, charset.length() - 1);
				}
				if (!charset.isEmpty()) {
					try {
						return Charset.forName(charset);
					} catch (Exception ignore) {
						// 非法 charset 名，回退 UTF-8
					}
				}
			}
		}
		return StandardCharsets.UTF_8;
	}

	/**
	 * 响应体文本（按 {@link #getCharset()} 解码）。
	 *
	 * @return 响应体
	 */
	public String body() {
		return new String(bodyBytes, getCharset());
	}

	/**
	 * 响应体字节（防御性拷贝）。
	 *
	 * @return 响应体字节
	 */
	public byte[] bodyBytes() {
		return bodyBytes.clone();
	}
}
