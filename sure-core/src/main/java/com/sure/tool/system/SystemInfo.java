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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * 系统运行时信息：CPU、内存、JVM、OS 家族与进程信息。
 * <p>
 * 全部基于 JDK 内置 API（无第三方依赖），参考 Hutool 的 {@code SystemUtil/HostInfo} 设计，
 * 补充了 JDK9+ 的 {@link ProcessHandle} 与 OperatingSystemMXBean 内存指标。
 *
 * @author suretool
 * @since 0.2.0
 */
public class SystemInfo {

	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();
	private static final RuntimeMXBean RUNTIME_BEAN = ManagementFactory.getRuntimeMXBean();
	private static final Runtime RUNTIME = Runtime.getRuntime();
	private static final java.lang.management.MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
	private static final java.lang.management.ThreadMXBean THREAD_BEAN = ManagementFactory.getThreadMXBean();

	private SystemInfo() {
	}

	// ---------------- CPU ----------------

	/**
	 * 可用处理器核数。
	 *
	 * @return 核数
	 */
	public static int getAvailableProcessors() {
		return RUNTIME.availableProcessors();
	}

	/**
	 * 系统平均负载（Unix 系），Windows 上不可用。
	 *
	 * @return 1 分钟平均负载，不可用时为 -1
	 */
	public static double getSystemLoadAverage() {
		return OS_BEAN.getSystemLoadAverage();
	}

	/**
	 * CPU 架构。
	 *
	 * @return 如 amd64 / aarch64
	 */
	public static String getOsArch() {
		return OS_BEAN.getArch();
	}

	// ---------------- 内存 ----------------

	/**
	 * JVM 堆内存总量。
	 *
	 * @return 字节数
	 */
	public static long getJvmTotalMemory() {
		return RUNTIME.totalMemory();
	}

	/**
	 * JVM 堆空闲内存。
	 *
	 * @return 字节数
	 */
	public static long getJvmFreeMemory() {
		return RUNTIME.freeMemory();
	}

	/**
	 * JVM 堆已用内存（总量 - 空闲）。
	 *
	 * @return 字节数
	 */
	public static long getJvmUsedMemory() {
		return RUNTIME.totalMemory() - RUNTIME.freeMemory();
	}

	/**
	 * JVM 最大堆内存。
	 *
	 * @return 字节数
	 */
	public static long getJvmMaxMemory() {
		return RUNTIME.maxMemory();
	}

	/**
	 * 系统物理内存总量（OS MXBean 支持时）。
	 *
	 * @return 字节数，不可用时为 -1
	 */
	public static long getTotalMemory() {
		if (OS_BEAN instanceof com.sun.management.OperatingSystemMXBean sunBean) {
			return sunBean.getTotalMemorySize();
		}
		return -1;
	}

	/**
	 * 系统可用物理内存（OS MXBean 支持时）。
	 *
	 * @return 字节数，不可用时为 -1
	 */
	public static long getFreeMemory() {
		if (OS_BEAN instanceof com.sun.management.OperatingSystemMXBean sunBean) {
			return sunBean.getFreeMemorySize();
		}
		return -1;
	}

	// ---------------- JVM ----------------

	/**
	 * JVM 名称（如 OpenJDK 64-Bit Server VM）。
	 *
	 * @return JVM 名称
	 */
	public static String getJvmName() {
		return RUNTIME_BEAN.getVmName();
	}

	/**
	 * JVM 供应商。
	 *
	 * @return 供应商
	 */
	public static String getJvmVendor() {
		return RUNTIME_BEAN.getVmVendor();
	}

	/**
	 * JVM 版本。
	 *
	 * @return 版本
	 */
	public static String getJvmVersion() {
		return RUNTIME_BEAN.getVmVersion();
	}

	/**
	 * Java 运行时版本（如 21.0.1）。
	 *
	 * @return 版本
	 */
	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}

	/**
	 * JVM 启动时间。
	 *
	 * @return 启动时间
	 */
	public static Instant getStartTime() {
		return Instant.ofEpochMilli(RUNTIME_BEAN.getStartTime());
	}

	/**
	 * JVM 已运行时长。
	 *
	 * @return 时长
	 */
	public static Duration getUptime() {
		return Duration.ofMillis(RUNTIME_BEAN.getUptime());
	}

	// ---------------- 进程 ----------------

	/**
	 * 当前进程 PID。
	 *
	 * @return PID
	 */
	public static long getPid() {
		return ProcessHandle.current().pid();
	}

	/**
	 * 当前进程命令行。
	 *
	 * @return 命令行参数列表
	 */
	public static java.util.List<String> getCommandLine() {
		return ProcessHandle.current().info().arguments()
				.map(java.util.List::of)
				.orElseGet(java.util.List::of);
	}

	// ---------------- 批17：CPU / JVM / 磁盘 / 进程快照 ----------------

	/**
	 * CPU 快照：核数 / 平均负载 / 进程级与系统级使用率（com.sun.management 扩展）。
	 *
	 * @return CPU 信息
	 */
	public static CpuInfo getCpuInfo() {
		double systemLoad = OS_BEAN.getSystemLoadAverage();
		double processCpuLoad = -1.0;
		double systemCpuLoad = -1.0;
		long processCpuTime = 0L;
		if (OS_BEAN instanceof com.sun.management.OperatingSystemMXBean sun) {
			processCpuLoad = clamp(sun.getProcessCpuLoad());
			systemCpuLoad = clamp(sun.getSystemCpuLoad());
			processCpuTime = sun.getProcessCpuTime();
		}
		return new CpuInfo.Builder()
				.cores(RUNTIME.availableProcessors())
				.systemLoadAverage(systemLoad)
				.processCpuLoad(processCpuLoad)
				.systemCpuLoad(systemCpuLoad)
				.processCpuTime(processCpuTime)
				.build();
	}

	/**
	 * JVM 运行时快照：版本 / 启动 / 堆与非堆内存 / 线程数。
	 *
	 * @return 运行时信息
	 */
	public static RuntimeInfo getRuntimeInfo() {
		java.lang.management.MemoryUsage heap = MEMORY_BEAN.getHeapMemoryUsage();
		java.lang.management.MemoryUsage nonHeap = MEMORY_BEAN.getNonHeapMemoryUsage();
		return new RuntimeInfo.Builder()
				.jvmName(RUNTIME_BEAN.getVmName())
				.jvmVersion(RUNTIME_BEAN.getVmVersion())
				.jvmVendor(RUNTIME_BEAN.getVmVendor())
				.startTime(RUNTIME_BEAN.getStartTime())
				.uptime(RUNTIME_BEAN.getUptime())
				.heapUsed(heap.getUsed())
				.heapCommitted(heap.getCommitted())
				.heapMax(heap.getMax())
				.nonHeapUsed(nonHeap.getUsed())
				.threadCount(THREAD_BEAN.getTotalStartedThreadCount())
				.liveThreadCount(THREAD_BEAN.getThreadCount())
				.build();
	}

	/**
	 * 所有磁盘根卷快照。
	 *
	 * @return 磁盘列表
	 */
	public static java.util.List<DiskInfo> getDisks() {
		java.util.List<DiskInfo> disks = new java.util.ArrayList<>();
		for (java.io.File root : java.io.File.listRoots()) {
			disks.add(new DiskInfo(root.getPath(), root.getTotalSpace(), root.getUsableSpace(), root.getFreeSpace()));
		}
		return disks;
	}

	/**
	 * 当前 JVM 可见的全部进程快照。
	 *
	 * @return 进程列表
	 */
	public static java.util.List<ProcessInfo> getProcesses() {
		java.util.List<ProcessInfo> result = new java.util.ArrayList<>();
		ProcessHandle.allProcesses().forEach(handle -> result.add(toProcessInfo(handle)));
		return result;
	}

	/**
	 * 取进程快照（按启动时间降序）。
	 *
	 * @param limit 上限（&lt;=0 返回全部）
	 * @return 进程列表
	 */
	public static java.util.List<ProcessInfo> getProcesses(int limit) {
		java.util.List<ProcessInfo> all = getProcesses();
		all.sort((a, b) -> Long.compare(
				b.getStartTime().map(Instant::toEpochMilli).orElse(0L),
				a.getStartTime().map(Instant::toEpochMilli).orElse(0L)));
		if (limit > 0 && all.size() > limit) {
			return all.subList(0, limit);
		}
		return all;
	}

	/**
	 * 当前 JVM 可见进程数。
	 *
	 * @return 进程数
	 */
	public static long getProcessCount() {
		return ProcessHandle.allProcesses().count();
	}

	private static ProcessInfo toProcessInfo(ProcessHandle handle) {
		ProcessHandle.Info info = handle.info();
		String args = String.join(" ", info.arguments().orElse(new String[0]));
		return new ProcessInfo.Builder()
				.pid(handle.pid())
				.command(info.command().orElse(null))
				.args(args.isBlank() ? null : args)
				.user(info.user().orElse(null))
				.startTime(info.startInstant().orElse(null))
				.cpuDurationNanos(info.totalCpuDuration().map(d -> d.toNanos()).orElse(0L))
				.status(handle.isAlive() ? "ALIVE" : "TERMINATED")
				.parentPid(handle.parent().map(ProcessHandle::pid).orElse(-1L))
				.build();
	}

	private static double clamp(double value) {
		return (value >= 0.0 && value <= 1.0) ? value : -1.0;
	}

	// ---------------- OS 家族 ----------------

	/**
	 * 操作系统名称。
	 *
	 * @return 名称
	 */
	public static String getOsName() {
		return OS_BEAN.getName();
	}

	/**
	 * 操作系统版本。
	 *
	 * @return 版本
	 */
	public static String getOsVersion() {
		return OS_BEAN.getVersion();
	}

	/**
	 * 是否 Windows。
	 *
	 * @return 是否 Windows
	 */
	public static boolean isWindows() {
		return osStartsWith("windows");
	}

	/**
	 * 是否 Linux。
	 *
	 * @return 是否 Linux
	 */
	public static boolean isLinux() {
		return osStartsWith("linux");
	}

	/**
	 * 是否 macOS。
	 *
	 * @return 是否 macOS
	 */
	public static boolean isMac() {
		return osStartsWith("mac");
	}

	/**
	 * 是否 Unix 系（Linux/macOS/BSD 等）。
	 *
	 * @return 是否 Unix
	 */
	public static boolean isUnix() {
		String name = getOsName().toLowerCase(Locale.ROOT);
		return name.contains("nix") || name.contains("nux") || name.contains("aix")
				|| name.contains("mac") || name.contains("bsd") || name.contains("sunos");
	}

	private static boolean osStartsWith(String prefix) {
		return getOsName().toLowerCase(Locale.ROOT).startsWith(prefix);
	}
}
