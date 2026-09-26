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

import java.util.function.Supplier;

/**
 * 日志静态便捷门面，符合项目 {@code XxxUtil} 命名约定。
 *
 * <p>用法：{@code LogUtil.debug("处理 {}", id)}。默认日志名取调用者类全名
 * （基于 JDK 21 {@link StackWalker}，零栈快照开销）。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public final class LogUtil {

	/**
	 * 用于获取调用者类名的 StackWalker。
	 */
	private static final StackWalker CALLER_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	private LogUtil() {
	}

	/**
	 * 获取当前调用者的日志实例（名称=调用者类全名）。
	 *
	 * @return 日志实例
	 */
	public static Log get() {
		return LogFactory.get(callerClass());
	}

	/**
	 * 按名称获取日志实例。
	 *
	 * @param name 日志名称
	 * @return 日志实例
	 */
	public static Log get(String name) {
		return LogFactory.get(name);
	}

	/**
	 * 按类获取日志实例。
	 *
	 * @param clazz 类
	 * @return 日志实例
	 */
	public static Log get(Class<?> clazz) {
		return LogFactory.get(clazz);
	}

	/**
	 * 输出 trace 级别日志。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void trace(String template, Object... args) {
		get().trace(template, args);
	}

	/**
	 * 输出 debug 级别日志。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void debug(String template, Object... args) {
		get().debug(template, args);
	}

	/**
	 * 输出 info 级别日志。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void info(String template, Object... args) {
		get().info(template, args);
	}

	/**
	 * 输出 warn 级别日志。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void warn(String template, Object... args) {
		get().warn(template, args);
	}

	/**
	 * 输出 error 级别日志。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void error(String template, Object... args) {
		get().error(template, args);
	}

	/**
	 * 惰性输出 debug 级别日志（仅开启时才求值消息）。
	 *
	 * @param supplier 消息提供者
	 */
	public static void debug(Supplier<String> supplier) {
		Log log = get();
		if (log.isDebugEnabled()) {
			log.debug(supplier.get());
		}
	}

	/**
	 * 通过 StackWalker 获取调用者类名。
	 *
	 * @return 调用者类全名
	 */
	private static String callerClass() {
		return CALLER_WALKER.walk(frames -> frames.skip(2).findFirst()
				.map(StackWalker.StackFrame::getClassName)
				.orElse(LogUtil.class.getName()));
	}
}
