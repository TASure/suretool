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
package com.sure.tool.db;

import java.sql.SQLException;

/**
 * 数据库访问运行期异常。
 *
 * <p>继承 {@link RuntimeException}，将受检的 {@link SQLException} 包装为
 * 非受检异常，便于调用方无需逐层声明；根因可通过 {@link #getCause()} 获取。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class DbRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造异常。
	 *
	 * @param message 异常描述
	 * @param cause   SQLException 根因
	 */
	public DbRuntimeException(String message, SQLException cause) {
		super(message, cause);
	}

	/**
	 * 构造异常。
	 *
	 * @param message 异常描述
	 */
	public DbRuntimeException(String message) {
		super(message);
	}

	/**
	 * 便捷工厂：包装 SQLException 并携带其 message。
	 *
	 * @param e SQLException 根因
	 * @return 包装后的 DbRuntimeException
	 */
	public static DbRuntimeException of(SQLException e) {
		return new DbRuntimeException("数据库操作失败: " + e.getMessage(), e);
	}
}
