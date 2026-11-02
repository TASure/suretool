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
package com.sure.tool.util;

/**
 * 版本号比较工具，封装 {@link VersionComparator}。
 *
 * <p>示例：</p>
 * <pre>{@code
 * VersionUtil.isGreaterThan("1.10.0", "1.9.0"); // true
 * VersionUtil.compare("1.0.0-alpha", "1.0.0");  // 负数
 * VersionUtil.isGreaterThanOrEqual("1.0", "1.0.0"); // true
 * }</pre>
 *
 * @author suretool
 * @since 1.20.0
 */
public final class VersionUtil {

	/** 共享版本比较器 */
	public static final VersionComparator VERSION_COMPARATOR = VersionComparator.INSTANCE;

	private VersionUtil() {
	}

	/**
	 * 比较两个版本号。
	 *
	 * @param version1 版本 1，可为 {@code null}（视为最小）
	 * @param version2 版本 2，可为 {@code null}（视为最小）
	 * @return 负数表示 {@code version1 < version2}，0 表示相等，正数表示更大
	 */
	public static int compare(String version1, String version2) {
		return VERSION_COMPARATOR.compare(version1, version2);
	}

	/**
	 * 判断 {@code version1} 是否大于 {@code version2}。
	 *
	 * @param version1 版本 1
	 * @param version2 版本 2
	 * @return 大于返回 {@code true}
	 */
	public static boolean isGreaterThan(String version1, String version2) {
		return compare(version1, version2) > 0;
	}

	/**
	 * 判断 {@code version1} 是否大于或等于 {@code version2}。
	 *
	 * @param version1 版本 1
	 * @param version2 版本 2
	 * @return 大于或等于返回 {@code true}
	 */
	public static boolean isGreaterThanOrEqual(String version1, String version2) {
		return compare(version1, version2) >= 0;
	}

	/**
	 * 判断 {@code version1} 是否小于 {@code version2}。
	 *
	 * @param version1 版本 1
	 * @param version2 版本 2
	 * @return 小于返回 {@code true}
	 */
	public static boolean isLessThan(String version1, String version2) {
		return compare(version1, version2) < 0;
	}

	/**
	 * 判断 {@code version1} 是否小于或等于 {@code version2}。
	 *
	 * @param version1 版本 1
	 * @param version2 版本 2
	 * @return 小于或等于返回 {@code true}
	 */
	public static boolean isLessThanOrEqual(String version1, String version2) {
		return compare(version1, version2) <= 0;
	}

	/**
	 * 判断两个版本号是否相等（忽略构建元数据与前导零）。
	 *
	 * @param version1 版本 1
	 * @param version2 版本 2
	 * @return 相等返回 {@code true}
	 */
	public static boolean isEqual(String version1, String version2) {
		return compare(version1, version2) == 0;
	}
}
