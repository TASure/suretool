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

/**
 * 进程执行结果，不可变。
 *
 * <p>包含退出码、标准输出、错误输出与是否超时四个维度。无论进程成功或失败，
 * {@link ProcessUtil} 都会返回完整的结果对象，由调用方按需判断。</p>
 *
 * @since 1.4.0
 */
public final class ProcessResult {

	private final int exitCode;
	private final String stdout;
	private final String stderr;
	private final boolean timedOut;

	/**
	 * 构造进程执行结果。
	 *
	 * @param exitCode 进程退出码
	 * @param stdout   标准输出全文
	 * @param stderr   错误输出全文
	 * @param timedOut 是否因超时被强制终止
	 */
	public ProcessResult(int exitCode, String stdout, String stderr, boolean timedOut) {
		this.exitCode = exitCode;
		this.stdout = stdout == null ? "" : stdout;
		this.stderr = stderr == null ? "" : stderr;
		this.timedOut = timedOut;
	}

	/**
	 * 获取进程退出码。
	 *
	 * @return 退出码（0 表示正常退出）
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * 获取标准输出全文。
	 *
	 * @return 标准输出，可能为空字符串，不会为 null
	 */
	public String getStdout() {
		return stdout;
	}

	/**
	 * 获取错误输出全文。
	 *
	 * @return 错误输出，可能为空字符串，不会为 null
	 */
	public String getStderr() {
		return stderr;
	}

	/**
	 * 是否因超时被强制终止。
	 *
	 * @return true 表示执行超时且进程已被强制销毁
	 */
	public boolean isTimedOut() {
		return timedOut;
	}

	/**
	 * 是否执行成功。
	 *
	 * @return 未超时且退出码为 0
	 */
	public boolean isSuccess() {
		return !timedOut && exitCode == 0;
	}

	@Override
	public String toString() {
		return "ProcessResult{exitCode=" + exitCode + ", timedOut=" + timedOut
				+ ", stdout=" + quote(stdout) + ", stderr=" + quote(stderr) + "}";
	}

	private static String quote(String s) {
		if (s.length() > 60) {
			return "\"" + s.substring(0, 60) + "...\"";
		}
		return "\"" + s + "\"";
	}
}
