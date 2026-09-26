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
 * 基于 SLF4J 的日志实现（反射接入，零编译期依赖）。
 *
 * <p>通过 {@link Class#forName(String)} 探测 classpath 中的 SLF4J，
 * 命中后反射调用 {@code org.slf4j.Logger} 完成日志输出；反射调用
 * 方法在首次使用时缓存，避免重复查找。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public final class Slf4jLog extends AbstractLog {

	/**
	 * 探测并返回 classpath 是否存在 SLF4J。
	 *
	 * @return 存在返回 {@code true}
	 */
	public static boolean slf4jAvailable() {
		try {
			Class.forName("org.slf4j.LoggerFactory");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private final Object logger;
	private final Class<?> loggerClass;

	/**
	 * 构造器，通过反射获取 SLF4J Logger。
	 *
	 * @param name 日志名称
	 * @throws IllegalStateException SLF4J 不可用时抛出
	 */
	public Slf4jLog(String name) {
		super(name);
		try {
			this.loggerClass = Class.forName("org.slf4j.Logger");
			Class<?> factory = Class.forName("org.slf4j.LoggerFactory");
			this.logger = factory.getMethod("getLogger", String.class).invoke(null, name);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("SLF4J 不可用：" + e.getMessage(), e);
		}
	}

	@Override
	protected boolean isEnabled(String level) {
		try {
			Object result = loggerClass.getMethod("is" + capitalize(level) + "Enabled").invoke(logger);
			return Boolean.TRUE.equals(result);
		} catch (ReflectiveOperationException e) {
			return true;
		}
	}

	@Override
	protected void handle(String level, String message, Throwable throwable) {
		try {
			Object[] args = throwable != null
					? new Object[] {message, new Object[] {throwable}}
					: new Object[] {message, new Object[0]};
			loggerClass.getMethod(level, String.class, Object[].class).invoke(logger, args);
		} catch (ReflectiveOperationException e) {
			// 反射失败不阻断调用方，回退直接输出
			System.err.println("[" + level + "] " + getName() + " - " + message);
			if (throwable != null) {
				throwable.printStackTrace(System.err);
			}
		}
	}

	/**
	 * 首字母大写。
	 *
	 * @param s 字符串
	 * @return 首字母大写后的字符串
	 */
	private static String capitalize(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
