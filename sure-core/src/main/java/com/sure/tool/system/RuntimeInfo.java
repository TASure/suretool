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
 * JVM 运行时信息：版本 / 启动与运行时长 / 堆与非堆内存 / 线程数，参考 Hutool 的 {@code RuntimeInfo} 设计。
 *
 * @author suretool
 * @since 1.10.0
 */
public class RuntimeInfo {

	private final String jvmName;
	private final String jvmVersion;
	private final String jvmVendor;
	private final long startTime;
	private final long uptime;
	private final long heapUsed;
	private final long heapCommitted;
	private final long heapMax;
	private final long nonHeapUsed;
	private final long threadCount;
	private final long liveThreadCount;

	RuntimeInfo(Builder builder) {
		this.jvmName = builder.jvmName;
		this.jvmVersion = builder.jvmVersion;
		this.jvmVendor = builder.jvmVendor;
		this.startTime = builder.startTime;
		this.uptime = builder.uptime;
		this.heapUsed = builder.heapUsed;
		this.heapCommitted = builder.heapCommitted;
		this.heapMax = builder.heapMax;
		this.nonHeapUsed = builder.nonHeapUsed;
		this.threadCount = builder.threadCount;
		this.liveThreadCount = builder.liveThreadCount;
	}

	/**
	 * JVM 名称（如 OpenJDK 64-Bit Server VM）。
	 *
	 * @return 名称
	 */
	public String getJvmName() {
		return jvmName;
	}

	/**
	 * JVM 版本。
	 *
	 * @return 版本
	 */
	public String getJvmVersion() {
		return jvmVersion;
	}

	/**
	 * JVM 厂商。
	 *
	 * @return 厂商
	 */
	public String getJvmVendor() {
		return jvmVendor;
	}

	/**
	 * JVM 启动时间（epoch 毫秒）。
	 *
	 * @return 启动时间
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * JVM 已运行时长（毫秒）。
	 *
	 * @return 运行时长
	 */
	public long getUptime() {
		return uptime;
	}

	/**
	 * 堆已用内存（字节）。
	 *
	 * @return 已用堆
	 */
	public long getHeapUsed() {
		return heapUsed;
	}

	/**
	 * 堆已提交内存（字节）。
	 *
	 * @return 提交堆
	 */
	public long getHeapCommitted() {
		return heapCommitted;
	}

	/**
	 * 堆最大内存（字节）。
	 *
	 * @return 最大堆
	 */
	public long getHeapMax() {
		return heapMax;
	}

	/**
	 * 非堆已用内存（字节）。
	 *
	 * @return 已用非堆
	 */
	public long getNonHeapUsed() {
		return nonHeapUsed;
	}

	/**
	 * 已启动线程总数。
	 *
	 * @return 线程总数
	 */
	public long getThreadCount() {
		return threadCount;
	}

	/**
	 * 当前存活线程数（含守护线程）。
	 *
	 * @return 存活线程数
	 */
	public long getLiveThreadCount() {
		return liveThreadCount;
	}

	@Override
	public String toString() {
		return "RuntimeInfo{jvm=" + jvmName + ", uptime=" + uptime + "ms}";
	}

	/**
	 * 构建器。
	 */
	static class Builder {

		String jvmName;
		String jvmVersion;
		String jvmVendor;
		long startTime;
		long uptime;
		long heapUsed;
		long heapCommitted;
		long heapMax;
		long nonHeapUsed;
		long threadCount;
		long liveThreadCount;

		Builder jvmName(String value) {
			this.jvmName = value;
			return this;
		}

		Builder jvmVersion(String value) {
			this.jvmVersion = value;
			return this;
		}

		Builder jvmVendor(String value) {
			this.jvmVendor = value;
			return this;
		}

		Builder startTime(long value) {
			this.startTime = value;
			return this;
		}

		Builder uptime(long value) {
			this.uptime = value;
			return this;
		}

		Builder heapUsed(long value) {
			this.heapUsed = value;
			return this;
		}

		Builder heapCommitted(long value) {
			this.heapCommitted = value;
			return this;
		}

		Builder heapMax(long value) {
			this.heapMax = value;
			return this;
		}

		Builder nonHeapUsed(long value) {
			this.nonHeapUsed = value;
			return this;
		}

		Builder threadCount(long value) {
			this.threadCount = value;
			return this;
		}

		Builder liveThreadCount(long value) {
			this.liveThreadCount = value;
			return this;
		}

		RuntimeInfo build() {
			return new RuntimeInfo(this);
		}
	}
}
