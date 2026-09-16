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
package com.sure.tool.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类：堆栈转字符串、根源异常定位等，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class ExceptionUtil {

	private ExceptionUtil() {
	}

	/**
	 * 将异常堆栈转为多行字符串。
	 *
	 * @param throwable 异常
	 * @return 堆栈字符串
	 */
	public static String getStackTrace(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		StringWriter sw = new StringWriter(256);
		try (PrintWriter pw = new PrintWriter(sw)) {
			throwable.printStackTrace(pw);
		}
		return sw.toString();
	}

	/**
	 * 定位最根本的异常（cause 链末端）。
	 *
	 * @param throwable 异常
	 * @return 根源异常（无 cause 时返回自身）
	 */
	public static Throwable getRootCause(Throwable throwable) {
		Throwable root = throwable;
		if (root == null) {
			return null;
		}
		Throwable cause;
		while ((cause = root.getCause()) != null) {
			root = cause;
		}
		return root;
	}

	/**
	 * 获取异常的简要信息（类型 + 消息）。
	 *
	 * @param throwable 异常
	 * @return 如 {@code java.io.IOException: 文件不存在}
	 */
	public static String getMessage(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		String msg = throwable.getMessage();
		return msg == null || msg.isEmpty() ? throwable.getClass().getName()
				: throwable.getClass().getName() + ": " + msg;
	}

	/**
	 * 是否由指定类型异常导致（沿 cause 链查找）。
	 *
	 * @param throwable 异常
	 * @param type      目标类型
	 * @return 是否匹配
	 */
	public static boolean isCausedBy(Throwable throwable, Class<? extends Throwable> type) {
		if (throwable == null || type == null) {
			return false;
		}
		for (Throwable t = throwable; t != null; t = t.getCause()) {
			if (type.isInstance(t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将受检异常包装为运行时异常（已是运行时异常则直接返回）。
	 *
	 * @param throwable 异常
	 * @return RuntimeException
	 */
	public static RuntimeException wrap(Throwable throwable) {
		if (throwable instanceof RuntimeException re) {
			return re;
		}
		return new RuntimeException(throwable);
	}
}
