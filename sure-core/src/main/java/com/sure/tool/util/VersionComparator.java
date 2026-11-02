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

import java.io.Serializable;
import java.util.Comparator;

/**
 * 版本号比较器，按「数字段优先、语义化版本」规则比较版本字符串。
 *
 * <p>比较规则：</p>
 * <ol>
 *   <li>以 {@code .} 分段，数字段按数值比较（{@code 1.10 > 1.9}），前导零忽略；</li>
 *   <li>主干版本缺失的段视为 {@code 0}（{@code 1.0} 与 {@code 1.0.0} 相等）；</li>
 *   <li>{@code -} 之后为预发布段，预发布版本小于同主干的正式版本
 *       （{@code 1.0.0-alpha < 1.0.0}），预发布段缺失的段较小；</li>
 *   <li>{@code +} 之后为构建元数据，比较时忽略；</li>
 *   <li>字母段忽略大小写按字典序比较，数字段优先于字母段。</li>
 * </ol>
 *
 * <p>示例：</p>
 * <pre>{@code
 * VersionComparator.INSTANCE.compare("1.10.0", "1.9.0");      // > 0
 * VersionComparator.INSTANCE.compare("1.0.0-alpha", "1.0.0"); // < 0
 * VersionComparator.INSTANCE.compare("1.0", "1.0.0+build1");  // 0（构建元数据忽略）
 * }</pre>
 *
 * <p>null 语义：{@code null} 视为最小版本（空安全）；两个 {@code null} 视为相等。</p>
 *
 * @author suretool
 * @since 1.20.0
 */
public class VersionComparator implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 1L;

	/** 共享实例（无状态，线程安全） */
	public static final VersionComparator INSTANCE = new VersionComparator();

	@Override
	public int compare(String version1, String version2) {
		if (version1 == null && version2 == null) {
			return 0;
		}
		if (version1 == null) {
			return -1;
		}
		if (version2 == null) {
			return 1;
		}
		String[] left = split(version1);
		String[] right = split(version2);
		int mainCompare = compareParts(left[0], right[0], true, true);
		if (mainCompare != 0) {
			return mainCompare;
		}
		if (left[1] == null && right[1] == null) {
			return 0;
		}
		if (left[1] == null) {
			return 1;
		}
		if (right[1] == null) {
			return -1;
		}
		return compareParts(left[1], right[1], false, false);
	}

	/**
	 * 拆分版本号为「主干版本」与「预发布段」。
	 *
	 * @param version 版本字符串
	 * @return 长度 2 的数组，下标 0 为主干版本，下标 1 为预发布段（可能为 {@code null}）
	 */
	private static String[] split(String version) {
		String normalized = version.trim();
		int plusIndex = normalized.indexOf('+');
		if (plusIndex >= 0) {
			normalized = normalized.substring(0, plusIndex);
		}
		int dashIndex = normalized.indexOf('-');
		if (dashIndex < 0) {
			return new String[] {normalized, null};
		}
		String prerelease = normalized.substring(dashIndex + 1).trim();
		return new String[] {normalized.substring(0, dashIndex).trim(), prerelease.isEmpty() ? null : prerelease};
	}

	/**
	 * 比较两段版本。
	 *
	 * @param left           左版本段
	 * @param right          右版本段
	 * @param padWithZero    缺失段是否按 {@code 0} 处理（主干版本为 {@code true}）
	 * @param numericHigher  数字段是否优先于字母段
	 * @return 比较结果
	 */
	private static int compareParts(String left, String right, boolean padWithZero, boolean numericHigher) {
		String[] leftParts = left.split("\\.", -1);
		String[] rightParts = right.split("\\.", -1);
		int maxLength = Math.max(leftParts.length, rightParts.length);
		for (int i = 0; i < maxLength; i++) {
			if (i >= leftParts.length) {
				if (padWithZero) {
					int compare = compareIdentifier("0", rightParts[i], numericHigher);
					if (compare != 0) {
						return compare;
					}
					continue;
				}
				return -1;
			}
			if (i >= rightParts.length) {
				if (padWithZero) {
					int compare = compareIdentifier(leftParts[i], "0", numericHigher);
					if (compare != 0) {
						return compare;
					}
					continue;
				}
				return 1;
			}
			int compare = compareIdentifier(leftParts[i], rightParts[i], numericHigher);
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	/**
	 * 比较单个版本段（可混合数字与字母）。
	 *
	 * @param left          左段
	 * @param right         右段
	 * @param numericHigher 数字段是否优先于字母段
	 * @return 比较结果
	 */
	private static int compareIdentifier(String left, String right, boolean numericHigher) {
		String leftValue = left.trim();
		String rightValue = right.trim();
		int leftIndex = 0;
		int rightIndex = 0;
		while (leftIndex < leftValue.length() && rightIndex < rightValue.length()) {
			char leftChar = leftValue.charAt(leftIndex);
			char rightChar = rightValue.charAt(rightIndex);
			boolean leftDigit = Character.isDigit(leftChar);
			boolean rightDigit = Character.isDigit(rightChar);
			if (leftDigit && rightDigit) {
				int leftEnd = digitEnd(leftValue, leftIndex);
				int rightEnd = digitEnd(rightValue, rightIndex);
				int compare = compareNumeric(leftValue.substring(leftIndex, leftEnd),
						rightValue.substring(rightIndex, rightEnd));
				if (compare != 0) {
					return compare;
				}
				leftIndex = leftEnd;
				rightIndex = rightEnd;
			} else if (leftDigit != rightDigit) {
				return leftDigit == numericHigher ? 1 : -1;
			} else {
				int compare = Character.compare(Character.toLowerCase(leftChar), Character.toLowerCase(rightChar));
				if (compare != 0) {
					return compare;
				}
				leftIndex++;
				rightIndex++;
			}
		}
		if (leftIndex < leftValue.length()) {
			return 1;
		}
		if (rightIndex < rightValue.length()) {
			return -1;
		}
		return 0;
	}

	/**
	 * 定位数字串结束位置。
	 *
	 * @param value 版本段
	 * @param start 起始下标
	 * @return 数字串结束下标（不含）
	 */
	private static int digitEnd(String value, int start) {
		int index = start;
		while (index < value.length() && Character.isDigit(value.charAt(index))) {
			index++;
		}
		return index;
	}

	/**
	 * 比较两个纯数字串（忽略前导零）。
	 *
	 * @param left  左数字串
	 * @param right 右数字串
	 * @return 比较结果
	 */
	private static int compareNumeric(String left, String right) {
		int leftStart = 0;
		while (leftStart < left.length() - 1 && left.charAt(leftStart) == '0') {
			leftStart++;
		}
		int rightStart = 0;
		while (rightStart < right.length() - 1 && right.charAt(rightStart) == '0') {
			rightStart++;
		}
		String leftNumber = left.substring(leftStart);
		String rightNumber = right.substring(rightStart);
		if (leftNumber.length() != rightNumber.length()) {
			return leftNumber.length() < rightNumber.length() ? -1 : 1;
		}
		return leftNumber.compareTo(rightNumber);
	}
}
