package com.sure.tool.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 数组工具类。
 *
 * <p>支持对象数组与基本类型数组的统一处理，参考 Hutool 的 {@code ArrayUtil} 设计。</p>
 *
 * @author suretool
 */
public class ArrayUtil {

	private ArrayUtil() {
	}

	/**
	 * 对象是否为数组。
	 *
	 * @param obj 对象
	 * @return 是否为数组
	 */
	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}

	/**
	 * 数组是否为空。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @return 是否为空
	 */
	public static boolean isEmpty(Object array) {
		return array == null || (array.getClass().isArray() && Array.getLength(array) == 0);
	}

	/**
	 * 数组是否非空。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(Object array) {
		return !isEmpty(array);
	}

	/**
	 * 获取数组长度。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @return 长度，非数组返回 0
	 */
	public static int length(Object array) {
		return (array != null && array.getClass().isArray()) ? Array.getLength(array) : 0;
	}

	/**
	 * 数组中是否包含指定元素。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @param value 元素
	 * @return 是否包含
	 */
	public static boolean contains(Object array, Object value) {
		if (isEmpty(array)) {
			return false;
		}
		int len = Array.getLength(array);
		for (int i = 0; i < len; i++) {
			Object element = Array.get(array, i);
			if (element == null) {
				if (value == null) {
					return true;
				}
			} else if (element.equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 查找元素在数组中的索引。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @param value 元素
	 * @return 索引，未找到返回 -1
	 */
	public static int indexOf(Object array, Object value) {
		if (isEmpty(array)) {
			return -1;
		}
		int len = Array.getLength(array);
		for (int i = 0; i < len; i++) {
			Object element = Array.get(array, i);
			if (element == null) {
				if (value == null) {
					return i;
				}
			} else if (element.equals(value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取数组中指定索引的元素。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @param index 索引，负数表示从末尾倒数
	 * @return 元素，越界返回 {@code null}
	 */
	public static Object get(Object array, int index) {
		return get(array, index, null);
	}

	/**
	 * 获取数组中指定索引的元素，越界时返回默认值。
	 *
	 * @param array        数组（支持基本类型数组）
	 * @param index        索引，负数表示从末尾倒数
	 * @param defaultValue 默认值
	 * @return 元素或默认值
	 */
	public static Object get(Object array, int index, Object defaultValue) {
		if (isEmpty(array)) {
			return defaultValue;
		}
		int len = Array.getLength(array);
		if (index < 0) {
			index = len + index;
		}
		if (index >= 0 && index < len) {
			return Array.get(array, index);
		}
		return defaultValue;
	}

	/**
	 * 使用分隔符拼接数组。
	 *
	 * @param array     数组（支持基本类型数组）
	 * @param delimiter 分隔符
	 * @return 拼接结果
	 */
	public static String join(Object array, CharSequence delimiter) {
		if (isEmpty(array)) {
			return StrUtil.EMPTY;
		}
		int len = Array.getLength(array);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.append(delimiter);
			}
			sb.append(Array.get(array, i));
		}
		return sb.toString();
	}

	/**
	 * 数组转 {@link List}（支持基本类型数组，自动装箱）。
	 *
	 * @param array 数组
	 * @return List
	 */
	public static List<Object> toList(Object array) {
		if (isEmpty(array)) {
			return new ArrayList<>();
		}
		int len = Array.getLength(array);
		List<Object> list = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			list.add(Array.get(array, i));
		}
		return list;
	}

	/**
	 * 集合转数组。
	 *
	 * @param collection 集合
	 * @param clazz      数组元素类型
	 * @param <T>        元素类型
	 * @return 数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<T> collection, Class<T> clazz) {
		if (collection == null) {
			return null;
		}
		return collection.toArray((T[]) Array.newInstance(clazz, 0));
	}

	/**
	 * 反转数组。
	 *
	 * @param array 数组（支持基本类型数组），原地反转
	 */
	public static void reverse(Object array) {
		if (isEmpty(array)) {
			return;
		}
		int len = Array.getLength(array);
		for (int i = 0, j = len - 1; i < j; i++, j--) {
			Object tmp = Array.get(array, i);
			Array.set(array, i, Array.get(array, j));
			Array.set(array, j, tmp);
		}
	}

	/**
	 * 数组去重（保持原有顺序）。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @return 去重后的对象数组
	 */
	public static Object[] distinct(Object array) {
		if (isEmpty(array)) {
			return new Object[0];
		}
		Set<Object> set = new LinkedHashSet<>(toList(array));
		return set.toArray();
	}

	/**
	 * 截取数组子区间。
	 *
	 * @param array 数组（支持基本类型数组）
	 * @param from  起始索引（含）
	 * @param to    结束索引（不含）
	 * @return 子数组（对象数组）
	 */
	public static Object[] sub(Object array, int from, int to) {
		if (isEmpty(array)) {
			return new Object[0];
		}
		int len = Array.getLength(array);
		if (from < 0) {
			from = len + from;
		}
		if (from < 0) {
			from = 0;
		}
		if (to < 0) {
			to = len + to;
		}
		if (to > len) {
			to = len;
		}
		if (to < from) {
			return new Object[0];
		}
		Object[] result = new Object[to - from];
		for (int i = from; i < to; i++) {
			result[i - from] = Array.get(array, i);
		}
		return result;
	}

	/**
	 * 返回第一个非 {@code null} 的元素。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 第一个非空元素
	 */
	@SafeVarargs
	public static <T> T firstNonNull(T... array) {
		if (array == null) {
			return null;
		}
		for (T t : array) {
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	/**
	 * 对象数组最小值。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 最小值
	 */
	public static <T extends Comparable<? super T>> T min(T[] array) {
		if (isEmpty(array)) {
			return null;
		}
		T min = array[0];
		for (T t : array) {
			if (t != null && t.compareTo(min) < 0) {
				min = t;
			}
		}
		return min;
	}

	/**
	 * 对象数组最大值。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 最大值
	 */
	public static <T extends Comparable<? super T>> T max(T[] array) {
		if (isEmpty(array)) {
			return null;
		}
		T max = array[0];
		for (T t : array) {
			if (t != null && t.compareTo(max) > 0) {
				max = t;
			}
		}
		return max;
	}

	/**
	 * int 数组最小值。
	 *
	 * @param array 数组
	 * @return 最小值，空数组返回 0
	 */
	public static int min(int[] array) {
		if (isEmpty(array)) {
			return 0;
		}
		int min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * int 数组最大值。
	 *
	 * @param array 数组
	 * @return 最大值，空数组返回 0
	 */
	public static int max(int[] array) {
		if (isEmpty(array)) {
			return 0;
		}
		int max = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * long 数组最小值。
	 *
	 * @param array 数组
	 * @return 最小值，空数组返回 0
	 */
	public static long min(long[] array) {
		if (isEmpty(array)) {
			return 0L;
		}
		long min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * long 数组最大值。
	 *
	 * @param array 数组
	 * @return 最大值，空数组返回 0
	 */
	public static long max(long[] array) {
		if (isEmpty(array)) {
			return 0L;
		}
		long max = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * double 数组最小值。
	 *
	 * @param array 数组
	 * @return 最小值，空数组返回 0
	 */
	public static double min(double[] array) {
		if (isEmpty(array)) {
			return 0D;
		}
		double min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * double 数组最大值。
	 *
	 * @param array 数组
	 * @return 最大值，空数组返回 0
	 */
	public static double max(double[] array) {
		if (isEmpty(array)) {
			return 0D;
		}
		double max = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * 对象数组转字符串。
	 *
	 * @param array 数组
	 * @return 字符串
	 */
	public static String toString(Object array) {
		if (isEmpty(array)) {
			return "[]";
		}
		if (array instanceof Object[]) {
			return Arrays.toString((Object[]) array);
		}
		if (array instanceof int[]) {
			return Arrays.toString((int[]) array);
		}
		if (array instanceof long[]) {
			return Arrays.toString((long[]) array);
		}
		if (array instanceof double[]) {
			return Arrays.toString((double[]) array);
		}
		if (array instanceof boolean[]) {
			return Arrays.toString((boolean[]) array);
		}
		if (array instanceof char[]) {
			return Arrays.toString((char[]) array);
		}
		if (array instanceof byte[]) {
			return Arrays.toString((byte[]) array);
		}
		if (array instanceof short[]) {
			return Arrays.toString((short[]) array);
		}
		if (array instanceof float[]) {
			return Arrays.toString((float[]) array);
		}
		return StrUtil.toString(array);
	}
}
