package com.sure.tool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 运行时命令执行工具类，参考 Hutool 的 {@code RuntimeUtil} 设计。
 *
 * @author suretool
 */
public class RuntimeUtil {

	private RuntimeUtil() {
	}

	/**
	 * 执行命令，返回进程对象（标准输出与错误输出合并）。
	 *
	 * @param command 命令及参数
	 * @return 进程对象
	 * @throws IOException IO 异常
	 */
	public static Process exec(String... command) throws IOException {
		return new ProcessBuilder(command).redirectErrorStream(true).start();
	}

	/**
	 * 执行命令并读取全部标准输出为字符串（UTF-8，去除末尾换行）。
	 *
	 * @param command 命令及参数
	 * @return 标准输出
	 * @throws IOException IO 异常
	 */
	public static String execForStr(String... command) throws IOException {
		return execForStr(CharsetUtil.UTF_8, command);
	}

	/**
	 * 执行命令并读取全部标准输出为字符串（去除末尾换行）。
	 *
	 * @param charset 字符集
	 * @param command 命令及参数
	 * @return 标准输出
	 * @throws IOException IO 异常
	 */
	public static String execForStr(Charset charset, String... command) throws IOException {
		Process process = exec(command);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			process.destroy();
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 执行命令并读取全部标准输出为行列表。
	 *
	 * @param command 命令及参数
	 * @return 行列表
	 * @throws IOException IO 异常
	 */
	public static List<String> execForLines(String... command) throws IOException {
		String result = execForStr(command);
		List<String> lines = new ArrayList<>();
		if (result != null && !result.isEmpty()) {
			for (String line : result.split("\n")) {
				lines.add(line);
			}
		}
		return lines;
	}
}
