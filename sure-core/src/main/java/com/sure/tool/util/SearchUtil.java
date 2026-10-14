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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 有序数组/列表查找工具：上下界、范围查询、二分、插值与指数查找。
 *
 * <p>Guava / Hutool 均未提供 lowerBound / upperBound / range；本类补齐并保证
 * 逆序比较器、重复元素、空集合与越界场景的行为确定性。
 */
public final class SearchUtil {

	private SearchUtil() {
	}

	/**
	 * 第一个不小于 key 的下标（插入点下界）。全部元素小于 key 时返回 {@code list.size()}。
	 *
	 * @param list 升序列表
	 * @param key  查找键
	 * @param <T>  元素类型
	 * @return 插入点下标
	 */
	public static <T extends Comparable<? super T>> int lowerBound(List<? extends T> list, T key) {
		return lowerBound(list, key, Comparator.naturalOrder());
	}

	/**
	 * 第一个大于 key 的下标（插入点上界）。全部元素不大于 key 时返回 {@code list.size()}。
	 *
	 * @param list 升序列表
	 * @param key  查找键
	 * @param <T>  元素类型
	 * @return 插入点下标
	 */
	public static <T extends Comparable<? super T>> int upperBound(List<? extends T> list, T key) {
		return upperBound(list, key, Comparator.naturalOrder());
	}

	/**
	 * 按比较器求第一个不小于 key 的下标。
	 *
	 * @param list 按比较器有序的列表
	 * @param key  查找键
	 * @param cmp  比较器
	 * @param <T>  元素类型
	 * @return 插入点下标
	 */
	public static <T> int lowerBound(List<? extends T> list, T key, Comparator<? super T> cmp) {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		if (cmp == null) {
			throw new IllegalArgumentException("cmp must not be null");
		}
		int lo = 0;
		int hi = list.size();
		while (lo < hi) {
			int mid = (lo + hi) >>> 1;
			if (cmp.compare(list.get(mid), key) < 0) {
				lo = mid + 1;
			} else {
				hi = mid;
			}
		}
		return lo;
	}

	/**
	 * 按比较器求第一个大于 key 的下标。
	 *
	 * @param list 按比较器有序的列表
	 * @param key  查找键
	 * @param cmp  比较器
	 * @param <T>  元素类型
	 * @return 插入点下标
	 */
	public static <T> int upperBound(List<? extends T> list, T key, Comparator<? super T> cmp) {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		if (cmp == null) {
			throw new IllegalArgumentException("cmp must not be null");
		}
		int lo = 0;
		int hi = list.size();
		while (lo < hi) {
			int mid = (lo + hi) >>> 1;
			if (cmp.compare(list.get(mid), key) <= 0) {
				lo = mid + 1;
			} else {
				hi = mid;
			}
		}
		return lo;
	}

	/**
	 * 范围查询：返回下标 {@code [lowerBound(from), lowerBound(to))} 的不可变子列表。
	 *
	 * @param list 升序列表
	 * @param from 区间下界（含）
	 * @param to   区间上界（不含）
	 * @param <T>  元素类型
	 * @return 不可变子列表
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> List<T> range(List<? extends T> list, T from, T to) {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		int lo = lowerBound(list, from);
		int hi = lowerBound(list, to);
		return Collections.unmodifiableList((List<T>) list.subList(lo, hi));
	}

	/**
	 * 二分查找：语义与 {@link Collections#binarySearch} 一致（命中返回下标，
	 * 未命中返回 {@code -(insertionPoint) - 1}），额外支持空列表与 {@code null} key 返回 -1。
	 *
	 * @param list 按比较器有序的列表
	 * @param key  查找键
	 * @param cmp  比较器
	 * @param <T>  元素类型
	 * @return 命中下标或 {@code -(insertionPoint) - 1}；空列表 / null key 返回 -1
	 */
	public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> cmp) {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		if (cmp == null) {
			throw new IllegalArgumentException("cmp must not be null");
		}
		if (list.isEmpty() || key == null) {
			return -1;
		}
		int lo = 0;
		int hi = list.size() - 1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			int c = cmp.compare(list.get(mid), key);
			if (c < 0) {
				lo = mid + 1;
			} else if (c > 0) {
				hi = mid - 1;
			} else {
				return mid;
			}
		}
		return -lo - 1;
	}

	/**
	 * 插值查找（适用于近似均匀分布的升序数组）；命中返回下标，否则返回 -1。
	 *
	 * @param sorted 升序数组
	 * @param key    查找键
	 * @return 命中下标或 -1
	 */
	public static int interpolationSearch(long[] sorted, long key) {
		if (sorted == null) {
			throw new IllegalArgumentException("sorted must not be null");
		}
		int lo = 0;
		int hi = sorted.length - 1;
		while (lo <= hi && key >= sorted[lo] && key <= sorted[hi]) {
			if (sorted[lo] == sorted[hi]) {
				// 区间内元素全部相等：命中即 lo，否则不存在
				return sorted[lo] == key ? lo : -1;
			}
			int pos = lo + (int) ((key - sorted[lo]) * (hi - lo) / (sorted[hi] - sorted[lo]));
			if (pos < lo || pos > hi) {
				break;
			}
			if (sorted[pos] == key) {
				return pos;
			}
			if (sorted[pos] < key) {
				lo = pos + 1;
			} else {
				hi = pos - 1;
			}
		}
		return -1;
	}

	/**
	 * 指数查找：先指数步进缩小区间，再二分定位；命中返回下标，否则返回 -1。
	 *
	 * @param sorted 升序数组
	 * @param key    查找键
	 * @return 命中下标或 -1
	 */
	public static int exponentialSearch(long[] sorted, long key) {
		if (sorted == null) {
			throw new IllegalArgumentException("sorted must not be null");
		}
		int length = sorted.length;
		if (length == 0) {
			return -1;
		}
		long bound = 1;
		while (bound < length && sorted[(int) bound] < key) {
			bound = Math.min(bound * 2, length);
		}
		int lo = (int) (bound >>> 1);
		int hi = (int) Math.min(bound, length - 1);
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			if (sorted[mid] == key) {
				return mid;
			}
			if (sorted[mid] < key) {
				lo = mid + 1;
			} else {
				hi = mid - 1;
			}
		}
		return -1;
	}
}
