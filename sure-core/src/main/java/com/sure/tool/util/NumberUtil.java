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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 数字工具类，参考 Hutool 的 {@code NumberUtil} 设计。
 *
 * <p>提供安全的数值解析、高精度运算、格式化等静态方法。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class NumberUtil {

	private NumberUtil() {
	}

	/**
	 * 字符串转 int，解析失败抛出 {@link NumberFormatException}。
	 *
	 * @param str 字符串
	 * @return int 值
	 */
	public static int parseInt(String str) {
		return Integer.parseInt(StrUtil.trim(str));
	}

	/**
	 * 字符串转 int，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return int 值或默认值
	 */
	public static int parseInt(String str, int defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转 long，解析失败抛出 {@link NumberFormatException}。
	 *
	 * @param str 字符串
	 * @return long 值
	 */
	public static long parseLong(String str) {
		return Long.parseLong(StrUtil.trim(str));
	}

	/**
	 * 字符串转 long，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return long 值或默认值
	 */
	public static long parseLong(String str, long defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Long.parseLong(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转 double，解析失败抛出 {@link NumberFormatException}。
	 *
	 * @param str 字符串
	 * @return double 值
	 */
	public static double parseDouble(String str) {
		return Double.parseDouble(StrUtil.trim(str));
	}

	/**
	 * 字符串转 double，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return double 值或默认值
	 */
	public static double parseDouble(String str, double defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转 float，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return float 值或默认值
	 */
	public static float parseFloat(String str, float defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转 short，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return short 值或默认值
	 */
	public static short parseShort(String str, short defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Short.parseShort(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转 byte，解析失败返回默认值。
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return byte 值或默认值
	 */
	public static byte parseByte(String str, byte defaultValue) {
		if (StrUtil.isBlank(str)) {
			return defaultValue;
		}
		try {
			return Byte.parseByte(StrUtil.trim(str));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串是否为数字（支持小数、负数、科学计数法）。
	 *
	 * @param str 字符串
	 * @return 是否为数字
	 */
	public static boolean isNumber(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		try {
			new BigDecimal(StrUtil.trim(str));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 字符串是否为整数。
	 *
	 * @param str 字符串
	 * @return 是否为整数
	 */
	public static boolean isInteger(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		String s = StrUtil.trim(str);
		if (s.startsWith("+") || s.startsWith("-")) {
			s = s.substring(1);
		}
		if (s.isEmpty()) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!CharUtil.isNumber(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 字符串是否为浮点数（含小数或指数形式）。
	 *
	 * @param str 字符串
	 * @return 是否为浮点数
	 */
	public static boolean isDouble(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		try {
			Double.parseDouble(StrUtil.trim(str));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 四舍五入保留指定小数位。
	 *
	 * @param value 数值
	 * @param scale 小数位
	 * @return 四舍五入后的 {@link BigDecimal}
	 */
	public static BigDecimal round(double value, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("小数位不能为负数: " + scale);
		}
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
	}

	/**
	 * 字符串数值四舍五入保留指定小数位。
	 *
	 * @param value 数值字符串
	 * @param scale 小数位
	 * @return 四舍五入后的 {@link BigDecimal}
	 */
	public static BigDecimal round(String value, int scale) {
		return round(parseDouble(value), scale);
	}

	/**
	 * 加法（基于 {@link BigDecimal}，避免浮点误差）。
	 *
	 * @param v1 加数 1
	 * @param v2 加数 2
	 * @return 和
	 */
	public static double add(double v1, double v2) {
		return BigDecimal.valueOf(v1).add(BigDecimal.valueOf(v2)).doubleValue();
	}

	/**
	 * 减法（基于 {@link BigDecimal}）。
	 *
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 差
	 */
	public static double sub(double v1, double v2) {
		return BigDecimal.valueOf(v1).subtract(BigDecimal.valueOf(v2)).doubleValue();
	}

	/**
	 * 乘法（基于 {@link BigDecimal}）。
	 *
	 * @param v1 乘数 1
	 * @param v2 乘数 2
	 * @return 积
	 */
	public static double mul(double v1, double v2) {
		return BigDecimal.valueOf(v1).multiply(BigDecimal.valueOf(v2)).doubleValue();
	}

	/**
	 * 除法（基于 {@link BigDecimal}，按指定小数位四舍五入）。
	 *
	 * @param v1    被除数
	 * @param v2    除数
	 * @param scale 小数位
	 * @return 商
	 */
	public static double div(double v1, double v2, int scale) {
		if (v2 == 0) {
			throw new ArithmeticException("除数不能为 0");
		}
		return BigDecimal.valueOf(v1).divide(BigDecimal.valueOf(v2), scale, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * long 加法（溢出时抛出 {@link ArithmeticException}）。
	 *
	 * @param v1 加数 1
	 * @param v2 加数 2
	 * @return 和
	 */
	public static long add(long v1, long v2) {
		return Math.addExact(v1, v2);
	}

	/**
	 * 两个 double 在指定误差范围内是否相等。
	 *
	 * @param v1    值 1
	 * @param v2    值 2
	 * @param delta 误差范围
	 * @return 是否相等
	 */
	public static boolean isEquals(double v1, double v2, double delta) {
		if (delta < 0) {
			throw new IllegalArgumentException("误差范围不能为负数: " + delta);
		}
		return Math.abs(v1 - v2) <= delta;
	}

	/**
	 * 是否为偶数。
	 *
	 * @param value 数值
	 * @return 是否为偶数
	 */
	public static boolean isEven(long value) {
		return (value & 1) == 0;
	}

	/**
	 * 是否为奇数。
	 *
	 * @param value 数值
	 * @return 是否为奇数
	 */
	public static boolean isOdd(long value) {
		return (value & 1) == 1;
	}

	/**
	 * 数字格式化，如 {@code decimalFormat(12345.678, "#,##0.00")} → {@code "12,345.68"}。
	 *
	 * @param value  数值
	 * @param format 格式
	 * @return 格式化后的字符串
	 */
	public static String decimalFormat(double value, String format) {
		return new DecimalFormat(format).format(value);
	}

	/**
	 * int 转二进制字符串（含符号位）。
	 *
	 * @param value 数值
	 * @return 二进制字符串
	 */
	public static String toBinaryStr(int value) {
		return Integer.toBinaryString(value);
	}

	/**
	 * int 转十六进制字符串。
	 *
	 * @param value 数值
	 * @return 十六进制字符串
	 */
	public static String toHexStr(int value) {
		return Integer.toHexString(value);
	}

	/**
	 * 数值转普通字符串（避免科学计数法，如 1.0E-4 → 0.0001）。
	 *
	 * @param value 数值
	 * @return 普通十进制字符串
	 */
	public static String toStr(double value) {
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}

	/**
	 * 生成整数序列 [start, end)。
	 *
	 * @param start 起始（含）
	 * @param end   结束（不含）
	 * @return 序列数组
	 */
	public static int[] range(int start, int end) {
		return range(start, end, 1);
	}

	/**
	 * 生成整数序列 [start, end)（指定步长）。
	 *
	 * @param start 起始（含）
	 * @param end   结束（不含）
	 * @param step  步长（&gt;0）
	 * @return 序列数组
	 */
	public static int[] range(int start, int end, int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("步长必须大于 0");
		}
		int size = Math.max(0, (int) Math.ceil((end - start) / (double) step));
		int[] result = new int[size];
		int v = start;
		for (int i = 0; i < size; i++) {
			result[i] = v;
			v += step;
		}
		return result;
	}

	/**
	 * 阶乘。
	 *
	 * @param n 非负整数
	 * @return n!
	 */
	public static long factorial(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n 不能为负: " + n);
		}
		long result = 1;
		for (int i = 2; i <= n; i++) {
			result *= i;
		}
		return result;
	}



	/**
	 * 最大公约数（欧几里得算法）。
	 *
	 * @param a 整数
	 * @param b 整数
	 * @return 最大公约数（非负）
	 */
	public static long gcd(long a, long b) {
		a = Math.abs(a);
		b = Math.abs(b);
		while (b != 0) {
			long tmp = a % b;
			a = b;
			b = tmp;
		}
		return a;
	}

	/**
	 * 最小公倍数。
	 *
	 * @param a 整数
	 * @param b 整数
	 * @return 最小公倍数（非负）
	 */
	public static long lcm(long a, long b) {
		if (a == 0 || b == 0) {
			return 0;
		}
		return Math.abs(a) / gcd(a, b) * Math.abs(b);
	}

	/**
	 * 素数判断（质数）。
	 *
	 * @param n 正整数
	 * @return 是否素数；n &lt; 2 返回 false
	 */
	public static boolean isPrime(int n) {
		if (n < 2) {
			return false;
		}
		if (n == 2 || n == 3) {
			return true;
		}
		if (n % 2 == 0 || n % 3 == 0) {
			return false;
		}
		for (int i = 5; (long) i * i <= n; i += 6) {
			if (n % i == 0 || n % (i + 2) == 0) {
				return false;
			}
		}
		return true;
	}



	/**
	 * 整数按权重分配（总和不变，余数按权重余数依次+1）。
	 *
	 * @param total   总数（≥0）
	 * @param weights 权重
	 * @return 分配结果（长度与权重一致）
	 */
	public static int[] partValue(int total, int... weights) {
		if (weights == null || weights.length == 0) {
			throw new IllegalArgumentException("权重不能为空");
		}
		long weightSum = 0;
		for (int w : weights) {
			weightSum += Math.max(w, 0);
		}
		if (weightSum == 0) {
			throw new IllegalArgumentException("权重和必须大于 0");
		}
		int[] result = new int[weights.length];
		int assigned = 0;
		for (int i = 0; i < weights.length; i++) {
			result[i] = (int) ((long) total * Math.max(weights[i], 0) / weightSum);
			assigned += result[i];
		}
		int remain = total - assigned;
		for (int i = 0; remain > 0 && i < weights.length; i++) {
			result[i]++;
			remain--;
		}
		return result;
	}



	/**
	 * 是否为 long 字符串。
	 *
	 * @param str 字符串
	 * @return 是否为 long
	 */
	public static boolean isLong(String str) {
		return isInteger(str);
	}


	/**
	 * 转为百分比字符串（如 0.3456 -> "34.56%"）。
	 *
	 * @param value 比率（0-1）
	 * @param scale 保留小数位
	 * @return 百分比字符串
	 */
	public static String percent(double value, int scale) {
		return String.format("%." + scale + "f%%", value * 100);
	}



	/**
	 * 保留指定小数位（四舍五入）。
	 *
	 * @param value 数值
	 * @param scale 小数位数
	 * @return 格式化后的字符串
	 */
	public static String toFixed(double value, int scale) {
		return java.math.BigDecimal.valueOf(value)
				.setScale(scale, java.math.RoundingMode.HALF_UP)
				.toPlainString();
	}

	/**
	 * 保留指定小数位（四舍五入）。
	 *
	 * @param value 数值
	 * @param scale 小数位数
	 * @return 格式化后的字符串
	 */
	public static String toFixed(java.math.BigDecimal value, int scale) {
		if (value == null) {
			return null;
		}
		return value.setScale(scale, java.math.RoundingMode.HALF_UP).toPlainString();
	}



	/**
	 * 解析字符串为数字类型（Integer/Long/BigDecimal）。
	 *
	 * @param value 字符串
	 * @return Number；无法解析返回 null
	 */
	public static Number parseNumber(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trim = value.trim();
		try {
			if (!trim.contains(".")) {
				if (trim.length() <= 9) {
					return Integer.valueOf(trim);
				}
				return Long.valueOf(trim);
			}
			return new java.math.BigDecimal(trim);
		} catch (NumberFormatException e) {
			return null;
		}
	}



	/**
	 * 向下取整。
	 *
	 * @param value 数值
	 * @return 不大于该数的最大整数
	 */
	public static long floor(double value) {
		return (long) Math.floor(value);
	}

	/**
	 * 向上取整。
	 *
	 * @param value 数值
	 * @return 不小于该数的最小整数
	 */
	public static long ceil(double value) {
		return (long) Math.ceil(value);
	}



	/**
	 * 幂运算。
	 *
	 * @param base     底数
	 * @param exponent 指数（非负）
	 * @return base 的 exponent 次幂
	 */
	public static long pow(long base, int exponent) {
		if (exponent < 0) {
			throw new IllegalArgumentException("exponent must be >= 0");
		}
		if (exponent == 0) {
			return 1;
		}
		long result = 1;
		while (exponent > 0) {
			if ((exponent & 1) == 1) {
				result *= base;
			}
			base *= base;
			exponent >>= 1;
		}
		return result;
	}


}