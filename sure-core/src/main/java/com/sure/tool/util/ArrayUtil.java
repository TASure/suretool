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
 * @since 0.1.0
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

	/**
	 * 末次出现位置。
	 *
	 * @param array 数组
	 * @param value 值
	 * @return 位置；未找到返回 -1
	 */
	public static int lastIndexOf(Object array, Object value) {
		if (!isArray(array)) {
			return -1;
		}
		int len = length(array);
		for (int i = len - 1; i >= 0; i--) {
			Object item = get(array, i);
			if (java.util.Objects.equals(item, value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 交换数组中两位置元素（原数组修改）。
	 *
	 * @param array 数组
	 * @param i     位置一
	 * @param j     位置二
	 */
	public static void swap(Object array, int i, int j) {
		if (!isArray(array)) {
			return;
		}
		int len = length(array);
		if (i < 0 || j < 0 || i >= len || j >= len) {
			throw new IllegalArgumentException("下标越界: i=" + i + ", j=" + j);
		}
		Object tmp = get(array, i);
		Array.set(array, i, get(array, j));
		Array.set(array, j, tmp);
	}



	/**
	 * 移除首个匹配元素（返回新数组）。
	 *
	 * @param array 数组
	 * @param value 要移除的值
	 * @return 新数组
	 */
	public static Object remove(Object array, Object value) {
		if (!isArray(array) || isEmpty(array)) {
			return array;
		}
		int idx = indexOf(array, value);
		if (idx < 0) {
			return array;
		}
		int len = length(array);
		Object result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), len - 1);
		System.arraycopy(array, 0, result, 0, idx);
		System.arraycopy(array, idx + 1, result, idx, len - idx - 1);
		return result;
	}

	/**
	 * 追加元素（返回新数组）。
	 *
	 * @param array  原数组
	 * @param values 追加值
	 * @return 新数组
	 */
	public static Object append(Object array, Object... values) {
		if (!isArray(array)) {
			throw new IllegalArgumentException("非数组对象");
		}
		int base = length(array);
		Object result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), base + values.length);
		System.arraycopy(array, 0, result, 0, base);
		for (int i = 0; i < values.length; i++) {
			java.lang.reflect.Array.set(result, base + i, values[i]);
		}
		return result;
	}

	/**
	 * 指定位置插入元素（返回新数组）。
	 *
	 * @param array  原数组
	 * @param index  插入位置（0~length）
	 * @param values 插入值
	 * @return 新数组
	 */
	public static Object insert(Object array, int index, Object... values) {
		if (!isArray(array)) {
			throw new IllegalArgumentException("非数组对象");
		}
		int len = length(array);
		if (index < 0 || index > len) {
			throw new IllegalArgumentException("插入位置越界: " + index);
		}
		Object result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), len + values.length);
		System.arraycopy(array, 0, result, 0, index);
		for (int i = 0; i < values.length; i++) {
			java.lang.reflect.Array.set(result, index + i, values[i]);
		}
		System.arraycopy(array, index, result, index + values.length, len - index);
		return result;
	}



	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Integer[] wrap(int[] array) {
		if (array == null) {
			return null;
		}
		Integer[] result = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Long[] wrap(long[] array) {
		if (array == null) {
			return null;
		}
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Double[] wrap(double[] array) {
		if (array == null) {
			return null;
		}
		Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}



	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Float[] wrap(float[] array) {
		if (array == null) {
			return null;
		}
		Float[] result = new Float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Short[] wrap(short[] array) {
		if (array == null) {
			return null;
		}
		Short[] result = new Short[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Byte[] wrap(byte[] array) {
		if (array == null) {
			return null;
		}
		Byte[] result = new Byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Character[] wrap(char[] array) {
		if (array == null) {
			return null;
		}
		Character[] result = new Character[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基本类型数组转包装类型数组。
	 *
	 * @param array 基本类型数组
	 * @return 包装类型数组
	 */
	public static Boolean[] wrap(boolean[] array) {
		if (array == null) {
			return null;
		}
		Boolean[] result = new Boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}



	/**
	 * 判断数组是否升序（自然顺序）。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 是否升序
	 */
	public static <T extends Comparable<T>> boolean isSorted(T[] array) {
		return isSorted(array, true);
	}

	/**
	 * 判断数组是否有序。
	 *
	 * @param array    数组
	 * @param asc      是否升序
	 * @param <T>      元素类型
	 * @return 是否有序
	 */
	public static <T extends Comparable<T>> boolean isSorted(T[] array, boolean asc) {
		if (array == null || array.length < 2) {
			return true;
		}
		for (int i = 1; i < array.length; i++) {
			int cmp = array[i - 1].compareTo(array[i]);
			if (asc && cmp > 0) {
				return false;
			}
			if (!asc && cmp < 0) {
				return false;
			}
		}
		return true;
	}



	/**
	 * null 数组转换为指定类型的空数组。
	 *
	 * @param array         数组
	 * @param componentType 数组元素类型
	 * @param <T>           元素类型
	 * @return 原数组；null 时返回空数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] nullToEmpty(T[] array, Class<T> componentType) {
		if (array != null) {
			return array;
		}
		return (T[]) java.lang.reflect.Array.newInstance(componentType, 0);
	}



	/**
	 * 调整数组长度（扩容补 null，缩容截断）。
	 *
	 * @param array   原数组
	 * @param newSize 新长度
	 * @param <T>     元素类型
	 * @return 新数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] resize(T[] array, int newSize) {
		if (array == null) {
			return null;
		}
		if (newSize <= 0) {
			return (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), 0);
		}
		T[] result = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), newSize);
		System.arraycopy(array, 0, result, 0, Math.min(array.length, newSize));
		return result;
	}



	/**
	 * 安全读取数组元素，越界或 null 返回 {@code null}。
	 *
	 * @param array 数组
	 * @param index 索引
	 * @param <T>   元素类型
	 * @return 元素或 {@code null}
	 */
	public static <T> T get(T[] array, int index) {
		if (array == null || index < 0 || index >= array.length) {
			return null;
		}
		return array[index];
	}

	/**
	 * 安全读取数组元素，越界或 null 返回默认值。
	 *
	 * @param array        数组
	 * @param index        索引
	 * @param defaultValue 默认值
	 * @param <T>          元素类型
	 * @return 元素或默认值
	 */
	public static <T> T get(T[] array, int index, T defaultValue) {
		T value = get(array, index);
		return value == null ? defaultValue : value;
	}

	/**
	 * 数组去重（保持首次出现顺序）。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 去重后数组；原数组为 {@code null} 返回 {@code null}
	 */
	public static <T> T[] distinct(T[] array) {
		if (array == null) {
			return null;
		}
		java.util.LinkedHashSet<T> set = new java.util.LinkedHashSet<>();
		for (T t : array) {
			set.add(t);
		}
		return toArray(set, (Class<T>) (array.getClass().getComponentType()));
	}

	/**
	 * 创建指定类型的空数组。
	 *
	 * @param componentType 数组元素类型
	 * @param <T>           元素类型
	 * @return 空数组
	 */
	public static <T> T[] empty(Class<T> componentType) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) java.lang.reflect.Array.newInstance(componentType, 0);
		return array;
	}

}