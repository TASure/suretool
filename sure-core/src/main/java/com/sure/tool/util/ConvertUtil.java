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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 类型转换工具类，参考 Hutool 的 {@code Convert} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ConvertUtil {

	private ConvertUtil() {
	}

	/**
	 * 转 int。
	 *
	 * @param value 值
	 * @return int 值，失败返回 0
	 */
	public static int toInt(Object value) {
		return toInt(value, 0);
	}

	/**
	 * 转 int。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return int 值或默认值
	 */
	public static int toInt(Object value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return NumberUtil.parseInt(value.toString(), defaultValue);
	}

	/**
	 * 转 long。
	 *
	 * @param value 值
	 * @return long 值，失败返回 0
	 */
	public static long toLong(Object value) {
		return toLong(value, 0L);
	}

	/**
	 * 转 long。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return long 值或默认值
	 */
	public static long toLong(Object value, long defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return NumberUtil.parseLong(value.toString(), defaultValue);
	}

	/**
	 * 转 double。
	 *
	 * @param value 值
	 * @return double 值，失败返回 0
	 */
	public static double toDouble(Object value) {
		return toDouble(value, 0D);
	}

	/**
	 * 转 double。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return double 值或默认值
	 */
	public static double toDouble(Object value, double defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return NumberUtil.parseDouble(value.toString(), defaultValue);
	}

	/**
	 * 转 float。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return float 值或默认值
	 */
	public static float toFloat(Object value, float defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		return NumberUtil.parseFloat(value.toString(), defaultValue);
	}

	/**
	 * 转 boolean。
	 *
	 * @param value 值
	 * @return boolean 值，失败返回 {@code false}
	 */
	public static boolean toBoolean(Object value) {
		return toBoolean(value, false);
	}

	/**
	 * 转 boolean。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return boolean 值或默认值
	 */
	public static boolean toBoolean(Object value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return BooleanUtil.toBoolean(value.toString(), defaultValue);
	}

	/**
	 * 转字符串。
	 *
	 * @param value 值
	 * @return 字符串，{@code null} 返回 {@code null}
	 */
	public static String toStr(Object value) {
		return toStr(value, null);
	}

	/**
	 * 转字符串。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return 字符串或默认值
	 */
	public static String toStr(Object value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return value.toString();
	}

	/**
	 * 通用类型转换：支持基础类型、包装类型、String 及数组/集合转 List。
	 *
	 * @param type  目标类型
	 * @param value 值
	 * @param <T>   目标类型
	 * @return 转换后的值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Class<T> type, Object value) {
		if (type == null || value == null) {
			return null;
		}
		if (type.isInstance(value)) {
			return (T) value;
		}
		if (type == String.class) {
			return (T) toStr(value);
		}
		if (type == int.class || type == Integer.class) {
			return (T) Integer.valueOf(toInt(value));
		}
		if (type == long.class || type == Long.class) {
			return (T) Long.valueOf(toLong(value));
		}
		if (type == double.class || type == Double.class) {
			return (T) Double.valueOf(toDouble(value));
		}
		if (type == float.class || type == Float.class) {
			return (T) Float.valueOf(toFloat(value, 0F));
		}
		if (type == boolean.class || type == Boolean.class) {
			return (T) Boolean.valueOf(toBoolean(value));
		}
		if (type == short.class || type == Short.class) {
			return (T) Short.valueOf((short) toInt(value));
		}
		if (type == byte.class || type == Byte.class) {
			return (T) Byte.valueOf((byte) toInt(value));
		}
		if (type == char.class || type == Character.class) {
			String s = toStr(value);
			return (T) Character.valueOf(s.isEmpty() ? '\0' : s.charAt(0));
		}
		return null;
	}

	/**
	 * 数组或集合转 List。
	 *
	 * @param value 数组（含基本类型数组）或集合
	 * @return List，不支持的输入返回 {@code null}
	 */
	public static List<Object> toList(Object value) {
		if (value == null) {
			return null;
		}
		if (value.getClass().isArray()) {
			return ArrayUtil.toList(value);
		}
		if (value instanceof Collection) {
			return new ArrayList<>((Collection<?>) value);
		}
		return null;
	}

	/**
	 * 数组或集合转 String 数组。
	 *
	 * @param value 数组（含基本类型数组）或集合
	 * @return String 数组
	 */
	public static String[] toStrArray(Object value) {
		List<Object> list = toList(value);
		if (list == null) {
			return null;
		}
		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = toStr(list.get(i));
		}
		return result;
	}

	/**
	 * 数组或集合转 int 数组。
	 *
	 * @param value 数组（含基本类型数组）或集合
	 * @return int 数组
	 */
	public static int[] toIntArray(Object value) {
		List<Object> list = toList(value);
		if (list == null) {
			return null;
		}
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = toInt(list.get(i));
		}
		return result;
	}

	/**
	 * 数组或集合转 long 数组。
	 *
	 * @param value 数组（含基本类型数组）或集合
	 * @return long 数组
	 */
	public static long[] toLongArray(Object value) {
		List<Object> list = toList(value);
		if (list == null) {
			return null;
		}
		long[] result = new long[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = toLong(list.get(i));
		}
		return result;
	}

	/**
	 * 对象数组（含基本类型数组）装箱为包装类型数组。
	 *
	 * @param array 数组
	 * @return 包装类型数组
	 */
	public static Object[] wrap(Object array) {
		if (array == null) {
			return null;
		}
		if (!array.getClass().isArray()) {
			Object[] single = new Object[1];
			single[0] = array;
			return single;
		}
		int len = Array.getLength(array);
		Object[] result = new Object[len];
		for (int i = 0; i < len; i++) {
			result[i] = Array.get(array, i);
		}
		return result;
	}

	/**
	 * 转 BigDecimal。
	 *
	 * @param value 值
	 * @return BigDecimal；null 或转换失败返回 null
	 */
	public static BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		try {
			return new BigDecimal(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 转 Date（支持 yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、时间戳）。
	 *
	 * @param value 值
	 * @return Date；无法解析返回 null
	 */
	public static java.util.Date toDate(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof java.util.Date date) {
			return date;
		}
		String str = String.valueOf(value).trim();
		if (str.isEmpty()) {
			return null;
		}
		try {
			return new java.util.Date(Long.parseLong(str));
		} catch (NumberFormatException ignored) {
			// 非时间戳，尝试日期格式
		}
		String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
		for (String pattern : patterns) {
			try {
				return new SimpleDateFormat(pattern).parse(str);
			} catch (ParseException ignored) {
				// 继续尝试下一格式
			}
		}
		return null;
	}



	/**
	 * 转 short。
	 *
	 * @param value 值
	 * @return short；转换失败返回 0
	 */
	public static short toShort(Object value) {
		return toShort(value, (short) 0);
	}

	/**
	 * 转 short。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return short；转换失败返回默认值
	 */
	public static short toShort(Object value, short defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number num) {
			return num.shortValue();
		}
		try {
			return Short.parseShort(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转 byte。
	 *
	 * @param value 值
	 * @return byte；转换失败返回 0
	 */
	public static byte toByte(Object value) {
		return toByte(value, (byte) 0);
	}

	/**
	 * 转 byte。
	 *
	 * @param value        值
	 * @param defaultValue 默认值
	 * @return byte；转换失败返回默认值
	 */
	public static byte toByte(Object value, byte defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number num) {
			return num.byteValue();
		}
		try {
			return Byte.parseByte(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 转 float。
	 *
	 * @param value 值
	 * @return float；转换失败返回 0
	 */
	public static float toFloat(Object value) {
		return toFloat(value, 0F);
	}

	/**
	 * 转字符数组。
	 *
	 * @param value 值
	 * @return 字符数组
	 */
	public static char[] toCharArray(Object value) {
		if (value == null) {
			return new char[0];
		}
		if (value.getClass().isArray()) {
			if (value instanceof char[] chars) {
				return chars;
			}
			int len = java.lang.reflect.Array.getLength(value);
			char[] result = new char[len];
			for (int i = 0; i < len; i++) {
				Object item = java.lang.reflect.Array.get(value, i);
				result[i] = item == null ? 0 : String.valueOf(item).charAt(0);
			}
			return result;
		}
		return String.valueOf(value).toCharArray();
	}



	/**
	 * 字符串转枚举。
	 *
	 * @param value   枚举名
	 * @param enumClass 枚举类
	 * @param <E>     枚举类型
	 * @return 枚举值；无效返回 null
	 */
	public static <E extends Enum<E>> E toEnum(String value, Class<E> enumClass) {
		if (value == null || enumClass == null) {
			return null;
		}
		try {
			return Enum.valueOf(enumClass, value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}



	/**
	 * 转换为 double 数组（集合/数组/逗号分隔字符串）。
	 *
	 * @param value 值
	 * @return double 数组；null 返回空数组
	 */
	public static double[] toDoubleArray(Object value) {
		if (value == null) {
			return new double[0];
		}
		if (value instanceof double[] arr) {
			return arr;
		}
		String[] strArr = (value instanceof String str) ? str.split(",", -1) : toStrArray(value);
		if (strArr == null) {
			return new double[0];
		}
		double[] result = new double[strArr.length];
		for (int i = 0; i < strArr.length; i++) {
			result[i] = Double.parseDouble(strArr[i].trim());
		}
		return result;
	}

	/**
	 * 转换为 boolean 数组（集合/数组/逗号分隔字符串）。
	 *
	 * @param value 值
	 * @return boolean 数组；null 返回空数组
	 */
	public static boolean[] toBooleanArray(Object value) {
		if (value == null) {
			return new boolean[0];
		}
		if (value instanceof boolean[] arr) {
			return arr;
		}
		String[] strArr = (value instanceof String str) ? str.split(",", -1) : toStrArray(value);
		if (strArr == null) {
			return new boolean[0];
		}
		boolean[] result = new boolean[strArr.length];
		for (int i = 0; i < strArr.length; i++) {
			result[i] = Boolean.parseBoolean(strArr[i].trim());
		}
		return result;
	}


}