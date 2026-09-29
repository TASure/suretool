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
package com.sure.tool.socket;

/**
 * Socket 相关运行时异常。
 *
 * @since 1.4.0
 */
public class SocketRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造异常。
	 *
	 * @param message 错误信息
	 */
	public SocketRuntimeException(String message) {
		super(message);
	}

	/**
	 * 构造异常。
	 *
	 * @param message 错误信息
	 * @param cause   根因
	 */
	public SocketRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
