package com.sure.tool.lang;

import com.sure.tool.util.StrUtil;

/**
 * 控制台打印工具类，参考 Hutool 的 {@code Console} 设计。
 *
 * @author suretool
 */
public class Console {

	private Console() {
	}

	/**
	 * 格式化输出并换行（{@code {}} 占位符风格）。
	 *
	 * @param template 模板，如 {@code "你好，{}"}
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
