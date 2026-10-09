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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * 批17（v1.10.0）：系统监控增强测试——CPU / JVM / 磁盘 / 进程快照与值对象。
 *
 * @author suretool
 * @since 1.10.0
 */
public class SystemGapTest {

	@Test
	public void testGetCpuInfo() {
		CpuInfo cpu = SystemInfo.getCpuInfo();
		assertTrue("核数应大于 0", cpu.getCores() > 0);
		assertTrue(cpu.getProcessCpuLoad() >= -1.0 && cpu.getProcessCpuLoad() <= 1.0);
		assertTrue(cpu.getSystemCpuLoad() >= -1.0 && cpu.getSystemCpuLoad() <= 1.0);
		assertTrue("CPU 时间不应为负", cpu.getProcessCpuTime() >= 0);
		assertTrue(cpu.toString().contains("CpuInfo"));
	}

	@Test
	public void testGetRuntimeInfo() {
		RuntimeInfo info = SystemInfo.getRuntimeInfo();
		assertNotNull(info.getJvmName());
		assertNotNull(info.getJvmVersion());
		assertNotNull(info.getJvmVendor());
		assertTrue(info.getStartTime() > 0);
		assertTrue(info.getUptime() > 0);
		assertTrue(info.getHeapUsed() > 0);
		assertTrue(info.getHeapCommitted() >= info.getHeapUsed());
		assertTrue("最大堆应不小于已用", info.getHeapMax() >= info.getHeapUsed());
		assertTrue(info.getNonHeapUsed() > 0);
		assertTrue(info.getLiveThreadCount() > 0);
		assertTrue(info.getThreadCount() >= info.getLiveThreadCount());
		assertTrue(info.toString().contains("RuntimeInfo"));
	}

	@Test
	public void testGetDisks() {
		List<DiskInfo> disks = SystemInfo.getDisks();
		assertFalse("至少应有一个磁盘根", disks.isEmpty());
		DiskInfo disk = disks.get(0);
		assertNotNull(disk.getPath());
		assertTrue(disk.getTotalSpace() > 0);
		assertTrue(disk.getUsableSpace() >= 0);
		assertTrue(disk.getFreeSpace() >= 0);
		assertEquals(disk.getUsedSpace(), Math.max(0, disk.getTotalSpace() - disk.getUsableSpace()));
		assertTrue(disk.getUsageRate() >= 0.0 && disk.getUsageRate() <= 1.0);
		assertTrue(disk.toString().contains("DiskInfo"));
	}

	@Test
	public void testGetProcessesAndCount() {
		assertTrue(SystemInfo.getPid() > 0);
		assertTrue("进程数应为正", SystemInfo.getProcessCount() > 0);
		List<ProcessInfo> processes = SystemInfo.getProcesses();
		assertFalse(processes.isEmpty());
		boolean foundSelf = processes.stream().anyMatch(p -> p.getPid() == SystemInfo.getPid());
		assertTrue("当前进程应在列表中", foundSelf);
	}

	@Test
	public void testGetProcessesLimitedAndFields() {
		List<ProcessInfo> limited = SystemInfo.getProcesses(3);
		assertTrue(limited.size() <= 3);
		for (ProcessInfo process : limited) {
			assertNotNull(process.getStatus());
			assertTrue(process.getCpuDurationNanos() >= 0);
			assertTrue(process.toString().contains("ProcessInfo"));
		}
		ProcessInfo self = SystemInfo.getProcesses().stream()
				.filter(p -> p.getPid() == SystemInfo.getPid())
				.findFirst()
				.orElse(null);
		assertNotNull(self);
		assertEquals("ALIVE", self.getStatus());
		assertTrue("当前进程应有命令", self.getCommand().isPresent());
		assertTrue(self.getStartTime().isPresent());
		assertNotNull(self.getStartTimeText());
	}
}
