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
package com.sure.tool.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 进程管理门面。
 *
 * <p>提供外部进程的启动、同步等待（含超时强杀）与标准输出/错误输出的并发捕获。
 * 输出读取使用虚拟线程并发消费管道，避免大输出时因管道写满造成死锁。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * ProcessResult r = ProcessUtil.exec("java", "-version");
 * System.out.println(r.getStderr());
 * </pre>
 *
 * @since 1.4.0
 */
public final class ProcessUtil {

	private ProcessUtil() {
	}

	/**
	 * 执行命令并等待完成（不设超时）。
	 *
	 * @param command 命令及参数，非空
	 * @return 执行结果
	 * @throws ProcessRuntimeException 启动或读取输出失败
	 * @throws IllegalArgumentException command 为 null/空
	 */
	public static ProcessResult exec(String... command) {
		if (command == null || command.length == 0) {
			throw new IllegalArgumentException("command must not be null or empty");
		}
		return exec(new ProcessBuilder(command), null);
	}

	/**
	 * 执行命令并等待完成（不设超时）。
	 *
	 * @param command 命令及参数，非空
	 * @return 执行结果
	 * @throws ProcessRuntimeException 启动或读取输出失败
	 * @throws IllegalArgumentException command 为 null/空
	 */
	public static ProcessResult exec(List<String> command) {
		if (command == null || command.isEmpty()) {
			throw new IllegalArgumentException("command must not be null or empty");
		}
		return exec(new ProcessBuilder(command), null);
	}

	/**
	 * 将命令字符串按空白拆分为参数并执行（不设超时）。
	 *
	 * <p>支持双引号包裹含空格的参数，如 {@code git commit -m "fix bug"}。</p>
	 *
	 * @param command 命令字符串，非空
	 * @return 执行结果
	 * @throws ProcessRuntimeException 启动或读取输出失败
	 * @throws IllegalArgumentException command 为 null/空
	 */
	public static ProcessResult exec(String command) {
		return exec(new ProcessBuilder(splitCommand(command)), null);
	}

	/**
	 * 基于自定义 {@link ProcessBuilder} 执行并等待完成（不设超时）。
	 *
	 * <p>可通过 builder 设置环境变量、工作目录、重定向等。</p>
	 *
	 * @param builder 进程构建器，非空
	 * @return 执行结果
	 * @throws ProcessRuntimeException 启动或读取输出失败
	 * @throws IllegalArgumentException builder 为 null
	 */
	public static ProcessResult exec(ProcessBuilder builder) {
		return exec(builder, null);
	}

	/**
	 * 执行命令并等待完成，超时则强制终止进程。
	 *
	 * @param command 命令及参数，非空
	 * @param timeout 超时时长，非 null
	 * @param unit    时间单位，非 null
	 * @return 执行结果；超时被杀时 {@link ProcessResult#isTimedOut()} 为 true
	 * @throws ProcessRuntimeException 启动或读取输出失败
	 * @throws IllegalArgumentException 参数校验失败
	 */
	public static ProcessResult execWithTimeout(List<String> command, long timeout, TimeUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit must not be null");
		}
		return exec(new ProcessBuilder(command), unit.toMillis(timeout));
	}

	/**
	 * 启动进程（异步），不等待完成。
	 *
	 * @param command 命令及参数，非空
	 * @return 已启动的 {@link Process}
	 * @throws ProcessRuntimeException 启动失败
	 * @throws IllegalArgumentException command 为 null/空
	 */
	public static Process start(List<String> command) {
		if (command == null || command.isEmpty()) {
			throw new IllegalArgumentException("command must not be null or empty");
		}
		return start(new ProcessBuilder(command));
	}

	/**
	 * 基于自定义 {@link ProcessBuilder} 启动进程（异步），不等待完成。
	 *
	 * @param builder 进程构建器，非空
	 * @return 已启动的 {@link Process}
	 * @throws ProcessRuntimeException 启动失败
	 * @throws IllegalArgumentException builder 为 null
	 */
	public static Process start(ProcessBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException("builder must not be null");
		}
		try {
			return builder.start();
		} catch (IOException e) {
			throw new ProcessRuntimeException("启动进程失败: " + builder.command(), e);
		}
	}

	private static ProcessResult exec(ProcessBuilder builder, Long timeout) {
		if (builder == null) {
			throw new IllegalArgumentException("builder must not be null");
		}
		Process process = start(builder);
		try {
			boolean timedOut = false;
			if (timeout != null) {
				timedOut = !process.waitFor(timeout, TimeUnit.MILLISECONDS);
				if (timedOut) {
					process.destroyForcibly();
				}
			} else {
				process.waitFor();
			}
			String stdout = readStream(process.getInputStream());
			String stderr = readStream(process.getErrorStream());
			return new ProcessResult(process.exitValue(), stdout, stderr, timedOut);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
			throw new ProcessRuntimeException("等待进程完成被中断", e);
		}
	}

	/**
	 * 并发读取输入流全文（虚拟线程），避免管道死锁。
	 */
	private static String readStream(InputStream in) {
		try {
			if (in == null) {
				return "";
			}
			final byte[][] holder = new byte[1][];
			Thread reader = Thread.ofVirtual().start(() -> {
				try (InputStream is = in) {
					holder[0] = is.readAllBytes();
				} catch (IOException e) {
					throw new ProcessRuntimeException("读取进程输出失败", e);
				}
			});
			reader.join();
			return holder[0] == null ? "" : new String(holder[0]);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ProcessRuntimeException("读取进程输出被中断", e);
		}
	}

	/**
	 * 将命令字符串按空白拆分，支持双引号包裹的含空格参数。
	 */
	private static List<String> splitCommand(String command) {
		if (command == null || command.isBlank()) {
			throw new IllegalArgumentException("command must not be null or blank");
		}
		List<String> parts = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean inQuote = false;
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (c == '"') {
				inQuote = !inQuote;
			} else if (Character.isWhitespace(c) && !inQuote) {
				if (!cur.isEmpty()) {
					parts.add(cur.toString());
					cur.setLength(0);
				}
			} else {
				cur.append(c);
			}
		}
		if (inQuote) {
			throw new IllegalArgumentException("命令字符串存在未闭合的双引号: " + command);
		}
		if (!cur.isEmpty()) {
			parts.add(cur.toString());
		}
		if (parts.isEmpty()) {
			throw new IllegalArgumentException("command must not be null or blank");
		}
		return parts;
	}
}
