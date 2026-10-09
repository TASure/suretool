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
package com.sure.tool.math;

import java.util.Arrays;

/**
 * 基础统计工具（对标 commons-math StatUtils 高频子集）。
 *
 * @since 1.11.0
 */
public final class StatUtil {

	private StatUtil() {
	}

	/**
	 * 均值。
	 *
	 * @param values 样本（空返回 0）
	 * @return 算术均值
	 */
	public static double mean(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum / values.length;
	}

	/**
	 * 中位数（排序后取中间值；偶数个取中间两数均值）。
	 *
	 * @param values 样本（空返回 0）
	 * @return 中位数
	 */
	public static double median(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double[] copy = values.clone();
		Arrays.sort(copy);
		int n = copy.length;
		if ((n & 1) == 1) {
			return copy[n / 2];
		}
		return (copy[n / 2 - 1] + copy[n / 2]) / 2.0;
	}

	/**
	 * 样本方差（n-1 分母，无偏估计）。
	 *
	 * @param values 样本（少于 2 个返回 0）
	 * @return 样本方差
	 */
	public static double variance(double[] values) {
		if (values == null || values.length < 2) {
			return 0.0;
		}
		double m = mean(values);
		double sum = 0;
		for (double v : values) {
			double diff = v - m;
			sum += diff * diff;
		}
		return sum / (values.length - 1);
	}

	/**
	 * 样本标准差（方差平方根）。
	 *
	 * @param values 样本（少于 2 个返回 0）
	 * @return 样本标准差
	 */
	public static double stdDev(double[] values) {
		return Math.sqrt(variance(values));
	}

	/**
	 * 最小值。
	 *
	 * @param values 样本（空返回 0）
	 * @return 最小值
	 */
	public static double min(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double min = values[0];
		for (double v : values) {
			min = Math.min(min, v);
		}
		return min;
	}

	/**
	 * 最大值。
	 *
	 * @param values 样本（空返回 0）
	 * @return 最大值
	 */
	public static double max(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double max = values[0];
		for (double v : values) {
			max = Math.max(max, v);
		}
		return max;
	}

	/**
	 * 总和。
	 *
	 * @param values 样本（空返回 0）
	 * @return 总和
	 */
	public static double sum(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum;
	}
}
