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
 * 磁盘（卷）信息：路径 / 总容量 / 可用空间 / 已用空间。
 *
 * @author suretool
 * @since 1.10.0
 */
public class DiskInfo {

	private final String path;
	private final long totalSpace;
	private final long usableSpace;
	private final long freeSpace;

	DiskInfo(String path, long totalSpace, long usableSpace, long freeSpace) {
		this.path = path;
		this.totalSpace = totalSpace;
		this.usableSpace = usableSpace;
		this.freeSpace = freeSpace;
	}

	/**
	 * 磁盘根路径（如 {@code /}、{@code C:\}）。
	 *
	 * @return 路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 总容量（字节）。
	 *
	 * @return 总容量
	 */
	public long getTotalSpace() {
		return totalSpace;
	}

	/**
	 * 当前用户可用空间（字节）。
	 *
	 * @return 可用空间
	 */
	public long getUsableSpace() {
		return usableSpace;
	}

	/**
	 * 空闲空间（字节）。
	 *
	 * @return 空闲空间
	 */
	public long getFreeSpace() {
		return freeSpace;
	}

	/**
	 * 已用空间（字节）= 总容量 - 可用空间。
	 *
	 * @return 已用空间
	 */
	public long getUsedSpace() {
		return Math.max(0, totalSpace - usableSpace);
	}

	/**
	 * 使用率（0-1）。
	 *
	 * @return 使用率；总容量为 0 时返回 0
	 */
	public double getUsageRate() {
		if (totalSpace <= 0) {
			return 0.0;
		}
		double used = getUsedSpace();
		return Math.min(1.0, used / totalSpace);
	}

	@Override
	public String toString() {
		return "DiskInfo{path=" + path + ", total=" + totalSpace + ", usable=" + usableSpace + '}';
	}
}
