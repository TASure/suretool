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

/**
 * HTTP 请求异常，携带响应状态码。
 *
 * @author suretool
 * @since 0.1.0
 */
public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int statusCode;

	/**
	 * 创建异常（无状态码）。
	 *
	 * @param message 错误信息
	 */
	public HttpException(String message) {
		super(message);
		this.statusCode = -1;
	}

	/**
	 * 创建异常。
	 *
	 * @param message    错误信息
	 * @param statusCode 响应状态码
	 */
	public HttpException(String message, int statusCode) {
		super(message + "（HTTP " + statusCode + "）");
		this.statusCode = statusCode;
	}

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = -1;
	}

	/**
	 * 响应状态码，无响应时为 -1。
	 *
	 * @return 状态码
	 */
	public int getStatusCode() {
		return statusCode;
	}
}