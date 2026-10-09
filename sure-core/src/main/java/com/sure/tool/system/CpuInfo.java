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

/**
 * CPU 信息：处理器数 / 系统负载 / CPU 使用率（进程级与系统级），参考 Hutool 的 {@code CpuInfo} 设计。
 *
 * @author suretool
 * @since 1.10.0
 */
public class CpuInfo {

	private final int cores;
	private final double systemLoadAverage;
	private final double processCpuLoad;
	private final double systemCpuLoad;
	private final long processCpuTime;

	CpuInfo(Builder builder) {
		this.cores = builder.cores;
		this.systemLoadAverage = builder.systemLoadAverage;
		this.processCpuLoad = builder.processCpuLoad;
		this.systemCpuLoad = builder.systemCpuLoad;
		this.processCpuTime = builder.processCpuTime;
	}

	/**
	 * 逻辑处理器核数。
	 *
	 * @return 核数
	 */
	public int getCores() {
		return cores;
	}

	/**
	 * 系统最近 1 分钟平均负载（Unix），Windows 返回 -1。
	 *
	 * @return 负载或 -1
	 */
	public double getSystemLoadAverage() {
		return systemLoadAverage;
	}

	/**
	 * 本进程 CPU 使用率（0-1，采样窗口），无数据返回 -1。
	 *
	 * @return 使用率或 -1
	 */
	public double getProcessCpuLoad() {
		return processCpuLoad;
	}

	/**
	 * 系统整体 CPU 使用率（0-1），无数据返回 -1。
	 *
	 * @return 使用率或 -1
	 */
	public double getSystemCpuLoad() {
		return systemCpuLoad;
	}

	/**
	 * 本进程累计 CPU 时间（纳秒）。
	 *
	 * @return CPU 时间
	 */
	public long getProcessCpuTime() {
		return processCpuTime;
	}

	@Override
	public String toString() {
		return "CpuInfo{cores=" + cores + ", systemLoadAverage=" + systemLoadAverage + '}';
	}

	/**
	 * 构建器。
	 */
	static class Builder {

		int cores;
		double systemLoadAverage;
		double processCpuLoad;
		double systemCpuLoad;
		long processCpuTime;

		Builder cores(int value) {
			this.cores = value;
			return this;
		}

		Builder systemLoadAverage(double value) {
			this.systemLoadAverage = value;
			return this;
		}

		Builder processCpuLoad(double value) {
			this.processCpuLoad = value;
			return this;
		}

		Builder systemCpuLoad(double value) {
			this.systemCpuLoad = value;
			return this;
		}

		Builder processCpuTime(long value) {
			this.processCpuTime = value;
			return this;
		}

		CpuInfo build() {
			return new CpuInfo(this);
		}
	}
}
