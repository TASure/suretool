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
package com.sure.tool.system;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 进程信息（基于 {@link ProcessHandle}）：PID / 命令 / 参数 / 启动时间 / CPU 时长 / 状态 / 父进程。
 *
 * @author suretool
 * @since 1.10.0
 */
public class ProcessInfo {

	private static final DateTimeFormatter START_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

	private final long pid;
	private final String command;
	private final String args;
	private final String user;
	private final Instant startTime;
	private final long cpuDurationNanos;
	private final String status;
	private final long parentPid;

	ProcessInfo(Builder builder) {
		this.pid = builder.pid;
		this.command = builder.command;
		this.args = builder.args;
		this.user = builder.user;
		this.startTime = builder.startTime;
		this.cpuDurationNanos = builder.cpuDurationNanos;
		this.status = builder.status;
		this.parentPid = builder.parentPid;
	}

	/**
	 * 进程 PID。
	 *
	 * @return PID
	 */
	public long getPid() {
		return pid;
	}

	/**
	 * 可执行命令（可为空）。
	 *
	 * @return 命令
	 */
	public Optional<String> getCommand() {
		return Optional.ofNullable(command);
	}

	/**
	 * 启动参数（可为空）。
	 *
	 * @return 参数
	 */
	public Optional<String> getArgs() {
		return Optional.ofNullable(args);
	}

	/**
	 * 启动用户（可为空）。
	 *
	 * @return 用户
	 */
	public Optional<String> getUser() {
		return Optional.ofNullable(user);
	}

	/**
	 * 启动时间（可为空）。
	 *
	 * @return 启动时间
	 */
	public Optional<Instant> getStartTime() {
		return Optional.ofNullable(startTime);
	}

	/**
	 * 累计 CPU 时长（纳秒）。
	 *
	 * @return CPU 时长
	 */
	public long getCpuDurationNanos() {
		return cpuDurationNanos;
	}

	/**
	 * 进程状态（如 ALIVE/ZOMBIE/TERMINATED）。
	 *
	 * @return 状态
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 父进程 PID（可为空）。
	 *
	 * @return 父 PID
	 */
	public Optional<Long> getParentPid() {
		return parentPid >= 0 ? Optional.of(parentPid) : Optional.empty();
	}

	/**
	 * 启动时间格式化（本地时区），无启动时间返回空串。
	 *
	 * @return 格式化时间
	 */
	public String getStartTimeText() {
		return startTime == null ? "" : START_FORMAT.format(startTime);
	}

	@Override
	public String toString() {
		return "ProcessInfo{pid=" + pid + ", command=" + command + ", status=" + status + '}';
	}

	/**
	 * 构建器。
	 */
	static class Builder {

		long pid;
		String command;
		String args;
		String user;
		Instant startTime;
		long cpuDurationNanos;
		String status;
		long parentPid = -1;

		Builder pid(long value) {
			this.pid = value;
			return this;
		}

		Builder command(String value) {
			this.command = value;
			return this;
		}

		Builder args(String value) {
			this.args = value;
			return this;
		}

		Builder user(String value) {
			this.user = value;
			return this;
		}

		Builder startTime(Instant value) {
			this.startTime = value;
			return this;
		}

		Builder cpuDurationNanos(long value) {
			this.cpuDurationNanos = value;
			return this;
		}

		Builder status(String value) {
			this.status = value;
			return this;
		}

		Builder parentPid(long value) {
			this.parentPid = value;
			return this;
		}

		ProcessInfo build() {
			return new ProcessInfo(this);
		}
	}
}
