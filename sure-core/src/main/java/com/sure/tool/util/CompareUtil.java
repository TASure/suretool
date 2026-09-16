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

import java.util.Comparator;

/**
 * 比较工具类：基础类型比较、空安全比较、取最值与范围约束，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class CompareUtil {

	private CompareUtil() {
	}

	/**
	 * 整数比较。
	 *
	 * @param a 第一个
	 * @param b 第二个
	 * @return -1/0/1
	 */
	public static int compare(int a, int b) {
		return Integer.compare(a, b);
	}

	/**
	 * 长整数比较。
	 *
	 * @param a 第一个
	 * @param b 第二个
	 * @return -1/0/1
	 */
	public static int compare(long a, long b) {
		return Long.compare(a, b);
	}

	/**
	 * 双精度比较（含 NaN 处理：NaN 视为最大）。
	 *
	 * @param a 第一个
	 * @param b 第二个
	 * @return -1/0/1
	 */
	public static int compare(double a, double b) {
		return Double.compare(a, b);
	}

	/**
	 * 空安全比较：null 视为最小；两值相等或同为 null 返回 0。
	 *
	 * @param a          第一个
	 * @param b          第二个
	 * @param comparator 比较器（null 时要求值实现 Comparable）
	 * @param <T>        类型
	 * @return -1/0/1
	 */
	public static <T> int compare(T a, T b, Comparator<? super T> comparator) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return -1;
		}
		if (b == null) {
			return 1;
		}
		if (comparator != null) {
			return comparator.compare(a, b);
		}
		@SuppressWarnings("unchecked")
		Comparable<? super T> ca = (Comparable<? super T>) a;
		return ca.compareTo(b);
	}

	/**
	 * 忽略大小写比较字符串。
	 *
	 * @param a 第一个
	 * @param b 第二个
	 * @return -1/0/1
	 */
	public static int compareIgnoreCase(String a, String b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return -1;
		}
		if (b == null) {
			return 1;
		}
		return a.compareToIgnoreCase(b);
	}

	/**
	 * 取最大值（空安全，比较器可空）。
	 *
	 * @param comparator 比较器
	 * @param values     值数组
	 * @param <T>        类型
	 * @return 最大值；全部 null 返回 null
	 */
	@SafeVarargs
	public static <T> T max(Comparator<? super T> comparator, T... values) {
		if (values == null || values.length == 0) {
			return null;
		}
		T max = null;
		boolean found = false;
		for (T v : values) {
			if (v == null) {
				continue;
			}
			if (!found || compare(v, max, comparator) > 0) {
				max = v;
				found = true;
			}
		}
		return max;
	}

	/**
	 * 取最小值（空安全，比较器可空）。
	 *
	 * @param comparator 比较器
	 * @param values     值数组
	 * @param <T>        类型
	 * @return 最小值；全部 null 返回 null
	 */
	@SafeVarargs
	public static <T> T min(Comparator<? super T> comparator, T... values) {
		if (values == null || values.length == 0) {
			return null;
		}
		T min = null;
		boolean found = false;
		for (T v : values) {
			if (v == null) {
				continue;
			}
			if (!found || compare(v, min, comparator) < 0) {
				min = v;
				found = true;
			}
		}
		return min;
	}

	/**
	 * 值约束在 [min, max] 区间（空安全）。
	 *
	 * @param value 值
	 * @param min   下界
	 * @param max   上界
	 * @param <T>   类型
	 * @return 约束后的值
	 */
	public static <T extends Comparable<? super T>> T clamp(T value, T min, T max) {
		if (value == null) {
			return null;
		}
		if (min != null && value.compareTo(min) < 0) {
			return min;
		}
		if (max != null && value.compareTo(max) > 0) {
			return max;
		}
		return value;
	}
}
