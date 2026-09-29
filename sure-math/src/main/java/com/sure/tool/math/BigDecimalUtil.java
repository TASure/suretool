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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal 精确运算工具门面。
 *
 * <p>提供加减乘除（默认 10 位 HALF_UP）、指定精度舍入、忽略尾零判等与字符串解析。
 * 除法默认采用 {@link RoundingMode#HALF_UP}，避免除不尽时的 {@link ArithmeticException}。</p>
 *
 * @since 1.4.0
 */
public final class BigDecimalUtil {

	/** 默认除法保留小数位。 */
	public static final int DEFAULT_SCALE = 10;

	private BigDecimalUtil() {
	}

	/**
	 * 加法。
	 *
	 * @param a 加数
	 * @param b 加数
	 * @return a + b
	 */
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		require(a, "a");
		require(b, "b");
		return a.add(b);
	}

	/**
	 * 减法。
	 *
	 * @param a 被减数
	 * @param b 减数
	 * @return a - b
	 */
	public static BigDecimal sub(BigDecimal a, BigDecimal b) {
		require(a, "a");
		require(b, "b");
		return a.subtract(b);
	}

	/**
	 * 乘法。
	 *
	 * @param a 乘数
	 * @param b 乘数
	 * @return a × b
	 */
	public static BigDecimal mul(BigDecimal a, BigDecimal b) {
		require(a, "a");
		require(b, "b");
		return a.multiply(b);
	}

	/**
	 * 除法，默认保留 {@value #DEFAULT_SCALE} 位、HALF_UP 舍入。
	 *
	 * @param a 被除数
	 * @param b 除数
	 * @return a ÷ b
	 * @throws ArithmeticException b 为 0
	 */
	public static BigDecimal div(BigDecimal a, BigDecimal b) {
		return div(a, b, DEFAULT_SCALE, RoundingMode.HALF_UP);
	}

	/**
	 * 除法，指定保留小数位，HALF_UP 舍入。
	 *
	 * @param a     被除数
	 * @param b     除数
	 * @param scale 保留小数位（≥ 0）
	 * @return a ÷ b
	 * @throws ArithmeticException b 为 0 或 scale 为负
	 */
	public static BigDecimal div(BigDecimal a, BigDecimal b, int scale) {
		return div(a, b, scale, RoundingMode.HALF_UP);
	}

	/**
	 * 除法，指定保留小数位与舍入模式。
	 *
	 * @param a     被除数
	 * @param b     除数
	 * @param scale 保留小数位（≥ 0）
	 * @param mode  舍入模式
	 * @return a ÷ b
	 * @throws ArithmeticException b 为 0 或 scale 为负
	 */
	public static BigDecimal div(BigDecimal a, BigDecimal b, int scale, RoundingMode mode) {
		require(a, "a");
		require(b, "b");
		if (mode == null) {
			throw new IllegalArgumentException("mode must not be null");
		}
		if (scale < 0) {
			throw new IllegalArgumentException("scale must be >= 0, got " + scale);
		}
		return a.divide(b, scale, mode);
	}

	/**
	 * 四舍五入（HALF_UP）到指定小数位。
	 *
	 * @param v     数值
	 * @param scale 保留小数位
	 * @return 舍入结果
	 */
	public static BigDecimal roundHalfUp(BigDecimal v, int scale) {
		return round(v, scale, RoundingMode.HALF_UP);
	}

	/**
	 * 按指定模式舍入到指定小数位。
	 *
	 * @param v     数值
	 * @param scale 保留小数位
	 * @param mode  舍入模式
	 * @return 舍入结果
	 */
	public static BigDecimal round(BigDecimal v, int scale, RoundingMode mode) {
		require(v, "v");
		if (mode == null) {
			throw new IllegalArgumentException("mode must not be null");
		}
		return v.setScale(scale, mode);
	}

	/**
	 * 判等：忽略尾随零后数值相等即视为相等（1.0 与 1.00 相等）。
	 *
	 * @param a 数值
	 * @param b 数值
	 * @return 数值是否相等
	 */
	public static boolean equals(BigDecimal a, BigDecimal b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.stripTrailingZeros().compareTo(b.stripTrailingZeros()) == 0;
	}

	/**
	 * 判断字符串是否为合法 BigDecimal（含 null/空）。
	 *
	 * @param s 字符串
	 * @return true 表示可解析
	 */
	public static boolean isNumber(String s) {
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			new BigDecimal(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 解析为 BigDecimal；null 或空串返回 null。
	 *
	 * @param s 字符串
	 * @return BigDecimal 或 null
	 * @throws NumberFormatException 字符串非法
	 */
	public static BigDecimal toBigDecimal(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return new BigDecimal(s);
	}

	private static void require(BigDecimal v, String name) {
		if (v == null) {
			throw new IllegalArgumentException(name + " must not be null");
		}
	}
}
