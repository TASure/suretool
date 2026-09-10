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
package com.sure.tool.lang;

import com.sure.tool.util.StrUtil;

/**
 * 控制台打印工具类，参考 Hutool 的 {@code Console} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class Console {

	private Console() {
	}

	/**
	 * 格式化输出并换行（一对花括号占位符风格）。
	 *
	 * @param template 模板，如 {@code "你好，&#123;&#125;"}
	 * @param args     参数
	 */
	public static void log(String template, Object... args) {
		System.out.println(StrUtil.format(template, args));
	}

	/**
	 * 输出对象并换行。
	 *
	 * @param obj 对象
	 */
	public static void log(Object obj) {
		System.out.println(String.valueOf(obj));
	}

	/**
	 * 输出对象，不换行。
	 *
	 * @param obj 对象
	 */
	public static void print(Object obj) {
		System.out.print(String.valueOf(obj));
	}

	/**
	 * 格式化错误输出并换行。
	 *
	 * @param template 模板
	 * @param args     参数
	 */
	public static void error(String template, Object... args) {
		System.err.println(StrUtil.format(template, args));
	}

	/**
	 * 输出错误对象并换行。
	 *
	 * @param obj 对象
	 */
	public static void error(Object obj) {
		System.err.println(String.valueOf(obj));
	}
}