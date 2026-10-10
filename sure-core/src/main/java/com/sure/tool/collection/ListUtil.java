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
package com.sure.tool.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 列表工具类，参考 Hutool 的 {@code ListUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ListUtil {

	private ListUtil() {
	}

	/**
	 * 创建列表。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return 列表
	 */
	@SafeVarargs
	public static <T> List<T> toList(T... values) {
		return CollUtil.newArrayList(values);
	}

	/**
	 * 列表切分：按固定大小切分为多个子列表。
	 *
	 * @param list 列表
	 * @param size 每份大小
	 * @param <T>  元素类型
	 * @return 切分后的子列表集合
	 */
	public static <T> List<List<T>> partition(List<T> list, int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("每份大小必须大于 0: " + size);
		}
		List<List<T>> result = new ArrayList<>();
		if (CollUtil.isEmpty(list)) {
			return result;
		}
		int total = list.size();
		for (int from = 0; from < total; from += size) {
			int to = Math.min(from + size, total);
			result.add(new ArrayList<>(list.subList(from, to)));
		}
		return result;
	}

	/**
	 * 分页截取：返回第 pageIndex 页（从 0 开始）的 pageSize 条数据。
	 *
	 * @param pageIndex 页码（从 0 开始）
	 * @param pageSize  每页条数
	 * @param list      数据列表
	 * @param <T>       元素类型
	 * @return 当页数据
	 */
	public static <T> List<T> page(int pageIndex, int pageSize, List<T> list) {
		if (pageIndex < 0) {
			pageIndex = 0;
		}
		if (pageSize <= 0 || CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		int from = pageIndex * pageSize;
		if (from >= list.size()) {
			return new ArrayList<>();
		}
		int to = Math.min(from + pageSize, list.size());
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * 截取子列表。
	 *
	 * @param list      列表
	 * @param fromIndex 起始索引（含）
	 * @param toIndex   结束索引（不含）
	 * @param <T>       元素类型
	 * @return 子列表
	 */
	public static <T> List<T> sub(List<T> list, int fromIndex, int toIndex) {
		return CollUtil.sub(list, fromIndex, toIndex);
	}

	/**
	 * 反转列表（原地）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 反转后的列表
	 */
	public static <T> List<T> reverse(List<T> list) {
		return CollUtil.reverse(list);
	}

	/**
	 * 反转列表（返回新列表，不影响原列表）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 反转后的新列表
	 */
	public static <T> List<T> reverseNew(List<T> list) {
		if (CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		List<T> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return copy;
	}

	/**
	 * 列表是否为空。
	 *
	 * @param list 列表
	 * @return 是否为空
	 */
	public static boolean isEmpty(List<?> list) {
		return CollUtil.isEmpty(list);
	}

	/**
	 * 列表是否非空。
	 *
	 * @param list 列表
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(List<?> list) {
		return CollUtil.isNotEmpty(list);
	}

	/**
	 * 越界安全取值：索引越界时返回 {@code null}，负数从尾部定位（-1 表示末尾元素）。
	 *
	 * @param list  列表（null 返回 null）
	 * @param index 下标（支持负数尾部定位）
	 * @param <T>   元素类型
	 * @return 对应元素，越界或列表为 null 时返回 null
	 * @since 1.16.0
	 */
	public static <T> T get(List<T> list, int index) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		int size = list.size();
		if (index < 0) {
			index += size;
		}
		if (index < 0 || index >= size) {
			return null;
		}
		return list.get(index);
	}

	/**
	 * 越界安全截取：起始与结束下标被钳制到合法范围，起始大于结束时返回空列表。
	 *
	 * @param list      列表（null 返回空列表）
	 * @param fromIndex 起始下标（含，可越界）
	 * @param toIndex   结束下标（不含，可越界）
	 * @param <T>       元素类型
	 * @return 子列表副本
	 * @since 1.16.0
	 */
	public static <T> List<T> subListSafe(List<T> list, int fromIndex, int toIndex) {
		if (list == null || list.isEmpty()) {
			return new ArrayList<>();
		}
		int size = list.size();
		int from = Math.max(0, fromIndex);
		int to = Math.min(size, toIndex);
		if (from >= to) {
			return new ArrayList<>();
		}
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * 去重并保持原有顺序（首个出现位置保留）。
	 *
	 * @param list 列表（null 返回空列表）
	 * @param <T>  元素类型
	 * @return 去重后的新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> distinct(List<T> list) {
		if (CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	/**
	 * 映射：对每个元素应用函数生成新列表。
	 *
	 * @param list     列表（null 返回空列表）
	 * @param mapper   映射函数（null 时抛 {@link NullPointerException}）
	 * @param <T>      元素类型
	 * @param <R>      结果类型
	 * @return 映射后的新列表
	 * @since 1.16.0
	 */
	public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
		List<R> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (T item : list) {
			result.add(mapper.apply(item));
		}
		return result;
	}

	/**
	 * 过滤：仅保留满足谓词的元素（保序）。
	 *
	 * @param list      列表（null 返回空列表）
	 * @param predicate 过滤谓词（null 时抛 {@link NullPointerException}）
	 * @param <T>       元素类型
	 * @return 过滤后的新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
		List<T> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (T item : list) {
			if (predicate.test(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 扁平映射：将每个元素展开为其子列表并依次拼接，内层 null 列表跳过。
	 *
	 * @param list    列表（null 返回空列表）
	 * @param mapper  展开函数（null 时抛 {@link NullPointerException}）
	 * @param <T>     元素类型
	 * @param <R>     结果元素类型
	 * @return 扁平化后的新列表
	 * @since 1.16.0
	 */
	public static <T, R> List<R> flatMap(List<T> list, Function<T, List<R>> mapper) {
		List<R> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (T item : list) {
			List<R> inner = mapper.apply(item);
			if (inner != null) {
				result.addAll(inner);
			}
		}
		return result;
	}

	/**
	 * 两列表按最短长度配对（zip），返回不可变二元组列表。
	 *
	 * @param left  左列表（null 按空处理）
	 * @param right 右列表（null 按空处理）
	 * @param <T>   左元素类型
	 * @param <U>   右元素类型
	 * @return 配对结果
	 * @since 1.16.0
	 */
	public static <T, U> List<List<Object>> zip(List<T> left, List<U> right) {
		List<List<Object>> result = new ArrayList<>();
		if (left == null || right == null) {
			return result;
		}
		int size = Math.min(left.size(), right.size());
		for (int i = 0; i < size; i++) {
			result.add(List.of(left.get(i), right.get(i)));
		}
		return result;
	}

	/**
	 * 原地洗牌（Fisher-Yates，可注入随机源）。
	 *
	 * @param list 列表（null 不操作）
	 * @param random 随机源（null 时使用默认）
	 * @param <T>   元素类型
	 * @return 洗牌后的同一列表
	 * @since 1.16.0
	 */
	public static <T> List<T> shuffle(List<T> list, Random random) {
		if (list != null && !list.isEmpty()) {
			Collections.shuffle(list, random != null ? random : new Random());
		}
		return list;
	}

	/**
	 * 洗牌副本：返回洗牌后的新列表，原列表不变。
	 *
	 * @param list 列表（null 返回空列表）
	 * @param random 随机源（null 时使用默认）
	 * @param <T>   元素类型
	 * @return 洗牌后的新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> shuffleCopy(List<T> list, Random random) {
		if (CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		List<T> copy = new ArrayList<>(list);
		Collections.shuffle(copy, random != null ? random : new Random());
		return copy;
	}

	/**
	 * 随机取样：返回不重复的 count 个元素（保原顺序的随机子集）；count 不小于列表大小时返回全量洗牌副本。
	 *
	 * @param list  列表（null 返回空列表）
	 * @param count 取样个数（&lt;=0 返回空列表）
	 * @param <T>   元素类型
	 * @return 取样结果新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> sample(List<T> list, int count) {
		if (CollUtil.isEmpty(list) || count <= 0) {
			return new ArrayList<>();
		}
		List<T> copy = new ArrayList<>(list);
		if (count >= copy.size()) {
			Collections.shuffle(copy);
			return copy;
		}
		Collections.shuffle(copy);
		return new ArrayList<>(copy.subList(0, count));
	}

	/**
	 * 均分块：按固定大小切分为多个子列表（与 {@link #partition} 同语义，别名友好）。
	 *
	 * @param list 列表（null 返回空）
	 * @param size 每块大小（&lt;=0 抛 {@link IllegalArgumentException}）
	 * @param <T>  元素类型
	 * @return 分块结果
	 * @since 1.16.0
	 */
	public static <T> List<List<T>> chunk(List<T> list, int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("每块大小必须大于 0: " + size);
		}
		List<List<T>> result = new ArrayList<>();
		if (CollUtil.isEmpty(list)) {
			return result;
		}
		int total = list.size();
		for (int from = 0; from < total; from += size) {
			int to = Math.min(from + size, total);
			result.add(new ArrayList<>(list.subList(from, to)));
		}
		return result;
	}

	/**
	 * 并集（保序去重）：先左后右，右侧重复元素不追加。
	 *
	 * @param left  左列表（null 按空处理）
	 * @param right 右列表（null 按空处理）
	 * @param <T>   元素类型
	 * @return 并集新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> union(List<T> left, List<T> right) {
		LinkedHashSet<T> set = new LinkedHashSet<>();
		if (left != null) {
			set.addAll(left);
		}
		if (right != null) {
			set.addAll(right);
		}
		return new ArrayList<>(set);
	}

	/**
	 * 交集（保序去重）：按左列表出现顺序。
	 *
	 * @param left  左列表（null 按空处理）
	 * @param right 右列表（null 按空处理）
	 * @param <T>   元素类型
	 * @return 交集新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> intersection(List<T> left, List<T> right) {
		List<T> result = new ArrayList<>();
		if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
			return result;
		}
		Set<T> rightSet = new HashSet<>(right);
		LinkedHashSet<T> seen = new LinkedHashSet<>();
		for (T item : left) {
			if (rightSet.contains(item) && seen.add(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 差集（保序去重）：左列表中不在右列表的元素。
	 *
	 * @param left  左列表（null 按空处理）
	 * @param right 右列表（null 按空处理）
	 * @param <T>   元素类型
	 * @return 差集新列表
	 * @since 1.16.0
	 */
	public static <T> List<T> subtract(List<T> left, List<T> right) {
		List<T> result = new ArrayList<>();
		if (left == null || left.isEmpty()) {
			return result;
		}
		Set<T> rightSet = right == null ? new HashSet<>() : new HashSet<>(right);
		LinkedHashSet<T> seen = new LinkedHashSet<>();
		for (T item : left) {
			if (!rightSet.contains(item) && seen.add(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 自然序最小值（空列表抛 {@link java.util.NoSuchElementException}）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 最小元素
	 * @since 1.16.0
	 */
	public static <T extends Comparable<? super T>> T min(List<T> list) {
		return Collections.min(list);
	}

	/**
	 * 自然序最大值（空列表抛 {@link java.util.NoSuchElementException}）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 最大元素
	 * @since 1.16.0
	 */
	public static <T extends Comparable<? super T>> T max(List<T> list) {
		return Collections.max(list);
	}

	/**
	 * 按比较器取最小值（空列表抛 {@link java.util.NoSuchElementException}）。
	 *
	 * @param list       列表
	 * @param comparator 比较器
	 * @param <T>        元素类型
	 * @return 最小元素
	 * @since 1.16.0
	 */
	public static <T> T min(List<T> list, Comparator<? super T> comparator) {
		return Collections.min(list, comparator);
	}

	/**
	 * 按比较器取最大值（空列表抛 {@link java.util.NoSuchElementException}）。
	 *
	 * @param list       列表
	 * @param comparator 比较器
	 * @param <T>        元素类型
	 * @return 最大元素
	 * @since 1.16.0
	 */
	public static <T> T max(List<T> list, Comparator<? super T> comparator) {
		return Collections.max(list, comparator);
	}

	/**
	 * 数值求和（null 元素跳过；空或全 null 返回 0）。
	 *
	 * @param list 数值列表（null 按空处理）
	 * @return 求和结果
	 * @since 1.16.0
	 */
	public static long sum(List<? extends Number> list) {
		long result = 0;
		if (list == null) {
			return result;
		}
		for (Number n : list) {
			if (n != null) {
				result += n.longValue();
			}
		}
		return result;
	}

	/**
	 * 数值均值（null 元素跳过；空或全 null 返回 0.0）。
	 *
	 * @param list 数值列表（null 按空处理）
	 * @return 均值
	 * @since 1.16.0
	 */
	public static double average(List<? extends Number> list) {
		if (list == null || list.isEmpty()) {
			return 0.0;
		}
		double sum = 0;
		int count = 0;
		for (Number n : list) {
			if (n != null) {
				sum += n.doubleValue();
				count++;
			}
		}
		return count == 0 ? 0.0 : sum / count;
	}

	/**
	 * 元素移动到新下标（其余元素顺移，与 {@code List.move} 语义一致）。
	 *
	 * @param list   列表
	 * @param from   原下标（越界抛 {@link IndexOutOfBoundsException}）
	 * @param to     目标下标
	 * @param <T>    元素类型
	 * @return 操作后的同一列表
	 * @since 1.16.0
	 */
	public static <T> List<T> move(List<T> list, int from, int to) {
		list.add(to, list.remove(from));
		return list;
	}
}