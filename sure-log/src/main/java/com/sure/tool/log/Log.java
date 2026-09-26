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
package com.sure.tool.log;

/**
 * 日志门面接口，参考 SLF4J {@code Logger} 与 Hutool {@code Log} 设计。
 *
 * <p>方法语义与 SLF4J 一致：模板使用一对花括号 {@code {}} 占位符；
 * 若参数末位为 {@link Throwable} 且无对应占位符，则作为异常自动输出堆栈。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public interface Log {

	/**
	 * 返回当前日志的名称（通常为类全名）。
	 *
	 * @return 日志名称
	 */
	String getName();

	/**
	 * 是否开启 trace 级别。
	 *
	 * @return 开启返回 {@code true}
	 */
	boolean isTraceEnabled();

	/**
	 * 输出 trace 级别日志。
	 *
	 * @param template 模板，如 {@code "处理 {}"}
	 * @param args     占位符参数
	 */
	void trace(String template, Object... args);

	/**
	 * 是否开启 debug 级别。
	 *
	 * @return 开启返回 {@code true}
	 */
	boolean isDebugEnabled();

	/**
	 * 输出 debug 级别日志。
	 *
	 * @param template 模板
	 * @param args     占位符参数
	 */
	void debug(String template, Object... args);

	/**
	 * 是否开启 info 级别。
	 *
	 * @return 开启返回 {@code true}
	 */
	boolean isInfoEnabled();

	/**
	 * 输出 info 级别日志。
	 *
	 * @param template 模板
	 * @param args     占位符参数
	 */
	void info(String template, Object... args);

	/**
	 * 是否开启 warn 级别。
	 *
	 * @return 开启返回 {@code true}
	 */
	boolean isWarnEnabled();

	/**
	 * 输出 warn 级别日志。
	 *
	 * @param template 模板
	 * @param args     占位符参数
	 */
	void warn(String template, Object... args);

	/**
	 * 是否开启 error 级别。
	 *
	 * @return 开启返回 {@code true}
	 */
	boolean isErrorEnabled();

	/**
	 * 输出 error 级别日志。
	 *
	 * @param template 模板
	 * @param args     占位符参数
	 */
	void error(String template, Object... args);
}
