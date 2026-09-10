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
}
