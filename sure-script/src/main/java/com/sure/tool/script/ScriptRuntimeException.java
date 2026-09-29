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

package com.sure.tool.script;

/**
 * 脚本执行异常。
 *
 * <p>当脚本引擎缺失、脚本编译或执行失败时抛出，携带可操作的提示信息。</p>
 *
 * @since 1.3.0
 */
public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造异常。
	 *
	 * @param message 异常消息
	 */
	public ScriptRuntimeException(String message) {
		super(message);
	}

	/**
	 * 构造异常。
	 *
	 * @param message 异常消息
	 * @param cause   根因
	 */
	public ScriptRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
