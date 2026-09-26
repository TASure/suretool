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

import com.sure.tool.util.StrUtil;

/**
 * 日志接口的抽象模板实现：统一处理占位符格式化与异常堆栈提取。
 *
 * <p>子类仅需实现 {@link #isEnabled(String)} 与 {@link #handle(String, String, Throwable)}，
 * 级别开关默认全部开启，子类可按后端能力覆盖。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public abstract class AbstractLog implements Log {

	/**
	 * 日志名称。
	 */
	private final String name;

	/**
	 * 构造器。
	 *
	 * @param name 日志名称
	 */
	protected AbstractLog(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isTraceEnabled() {
		return isEnabled("trace");
	}

	@Override
	public void trace(String template, Object... args) {
		logIfEnabled("trace", template, args);
	}

	@Override
	public boolean isDebugEnabled() {
		return isEnabled("debug");
	}

	@Override
	public void debug(String template, Object... args) {
		logIfEnabled("debug", template, args);
	}

	@Override
	public boolean isInfoEnabled() {
		return isEnabled("info");
	}

	@Override
	public void info(String template, Object... args) {
		logIfEnabled("info", template, args);
	}

	@Override
	public boolean isWarnEnabled() {
		return isEnabled("warn");
	}

	@Override
	public void warn(String template, Object... args) {
		logIfEnabled("warn", template, args);
	}

	@Override
	public boolean isErrorEnabled() {
		return isEnabled("error");
	}

	@Override
	public void error(String template, Object... args) {
		logIfEnabled("error", template, args);
	}

	/**
	 * 级别是否开启，默认全部开启，子类可覆盖。
	 *
	 * @param level 级别名（trace/debug/info/warn/error）
	 * @return 开启返回 {@code true}
	 */
	protected boolean isEnabled(String level) {
		return true;
	}

	/**
	 * 级别开启时格式化并输出日志。
	 *
	 * @param level    级别名
	 * @param template 模板
	 * @param args     参数（末位为 {@link Throwable} 且无对应占位符时视为异常）
	 */
	private void logIfEnabled(String level, String template, Object... args) {
		if (!isEnabled(level)) {
			return;
		}
		Throwable throwable = null;
		Object[] params = args;
		if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable t
				&& countPlaceholders(template) < args.length) {
			// 末位 Throwable 无对应占位符时视为异常，不入消息
			throwable = t;
			params = trimLast(args);
		}
		handle(level, StrUtil.format(template, params), throwable);
	}

	/**
	 * 子类输出日志。
	 *
	 * @param level     级别名
	 * @param message   已格式化的消息
	 * @param throwable 异常（可能为 {@code null}）
	 */
	protected abstract void handle(String level, String message, Throwable throwable);

	/**
	 * 统计模板中的占位符数量（连续花括号对）。
	 *
	 * @param template 模板
	 * @return 占位符个数
	 */
	private static int countPlaceholders(String template) {
		if (template == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < template.length() - 1; i++) {
			if (template.charAt(i) == '{' && template.charAt(i + 1) == '}') {
				count++;
				i++;
			}
		}
		return count;
	}

	/**
	 * 去除参数末位元素。
	 *
	 * @param args 参数
	 * @return 去除末位后的新数组
	 */
	private static Object[] trimLast(Object[] args) {
		Object[] result = new Object[args.length - 1];
		System.arraycopy(args, 0, result, 0, result.length);
		return result;
	}
}
