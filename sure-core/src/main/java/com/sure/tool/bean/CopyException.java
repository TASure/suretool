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

package com.sure.tool.bean;

/**
 * 属性拷贝失败异常：读取/写入属性失败、类型转换失败、目标只读、目标实例化失败。
 *
 * @since 1.18.0
 */
public class CopyException extends RuntimeException {

	/**
	 * 构造异常。
	 *
	 * @param message 异常信息
	 */
	public CopyException(String message) {
		super(message);
	}

	/**
	 * 构造异常。
	 *
	 * @param message 异常信息
	 * @param cause   根因
	 */
	public CopyException(String message, Throwable cause) {
		super(message, cause);
	}
}
