package com.sure.tool.util;

import java.lang.reflect.Array;
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
}
