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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 区间集合（RangeSet）：维护一组互不重叠的区间，添加时自动合并重叠与首尾相接区间，
 * 对标 Guava {@code RangeSet} 的常用子集，零依赖。
 *
 * <p>区间语义为<b>左闭右开</b> {@code [lower, upper)}，与 {@code subList}/{@code substring}
 * 一致；删除区间时端点不会被保留。典型场景：日期段合并、IP/数值段配额、连续可用区间管理。</p>
 *
 * <p><b>线程安全</b>：本类非线程安全；并发场景需外部同步。</p>
 *
 * @param <T> 区间端点类型（自然序或构造时给定比较器）
 * @since 1.10.0
 */
public class RangeSet<T extends Comparable<T>> {

	/**
	 * 左闭右开区间 [lower, upper)。
	 *
	 * @param <T> 端点类型
	 */
	public static final class Range<T extends Comparable<T>> {
		private final T lower;
		private final T upper;

		Range(T lower, T upper) {
			this.lower = lower;
			this.upper = upper;
		}

		/**
		 * 区间下界。
		 *
		 * @return 下界
		 */
		public T getLower() {
			return lower;
		}

		/**
		 * 区间上界。
		 *
		 * @return 上界
		 */
		public T getUpper() {
			return upper;
		}

		/**
		 * 是否包含指定值（左闭右开：含下界、不含上界）。
		 *
		 * @param value 值
		 * @return 是否包含
		 */
		public boolean contains(T value) {
			return lower.compareTo(value) <= 0 && upper.compareTo(value) > 0;
		}

		@Override
		public String toString() {
			return "[" + lower + ", " + upper + ")";
		}
	}

	private final NavigableMap<T, T> ranges = new TreeMap<>();

	/**
	 * 添加左闭右开区间 [lower, upper)，自动合并重叠与首尾相接区间。
	 *
	 * @param lower 下界（含）
	 * @param upper 上界（不含）
	 * @return 本对象，支持链式调用
	 * @throws IllegalArgumentException lower &gt; upper
	 */
	public RangeSet<T> add(T lower, T upper) {
		if (lower.compareTo(upper) > 0) {
			throw new IllegalArgumentException("lower must be <= upper: " + lower + " > " + upper);
		}
		T newLower = lower;
		T newUpper = upper;

		Map.Entry<T, T> floor = ranges.floorEntry(lower);
		if (floor != null && floor.getValue().compareTo(newLower) >= 0) {
			// 前一个区间已覆盖 lower，合并其下界
			newLower = floor.getKey();
			if (floor.getValue().compareTo(newUpper) > 0) {
				newUpper = floor.getValue();
			}
			ranges.remove(floor.getKey());
		}
		Map.Entry<T, T> ceiling = ranges.ceilingEntry(lower);
		while (ceiling != null && ceiling.getKey().compareTo(newUpper) <= 0) {
			if (ceiling.getValue().compareTo(newUpper) > 0) {
				newUpper = ceiling.getValue();
			}
			ranges.remove(ceiling.getKey());
			ceiling = ranges.ceilingEntry(lower);
		}
		ranges.put(newLower, newUpper);
		return this;
	}

	/**
	 * 移除左闭右开区间 [lower, upper)（与已有区间求差集，可能拆出 0~2 个子区间）。
	 *
	 * @param lower 下界（含）
	 * @param upper 上界（不含）
	 * @return 本对象，支持链式调用
	 * @throws IllegalArgumentException lower &gt; upper
	 */
	public RangeSet<T> remove(T lower, T upper) {
		if (lower.compareTo(upper) > 0) {
			throw new IllegalArgumentException("lower must be <= upper: " + lower + " > " + upper);
		}
		Map.Entry<T, T> floor = ranges.floorEntry(lower);
		if (floor != null && floor.getValue().compareTo(lower) >= 0) {
			// 前一个区间与待删区间相交
			T keepStart = floor.getKey();
			T keepEnd = floor.getValue();
			ranges.remove(floor.getKey());
			if (keepStart.compareTo(lower) < 0) {
				ranges.put(keepStart, lower);
			}
			if (keepEnd.compareTo(upper) > 0) {
				ranges.put(upper, keepEnd);
			}
		}
		Map.Entry<T, T> ceiling = ranges.ceilingEntry(lower);
		while (ceiling != null && ceiling.getKey().compareTo(upper) <= 0) {
			T keepEnd = ceiling.getValue();
			ranges.remove(ceiling.getKey());
			if (keepEnd.compareTo(upper) > 0) {
				// 尾部超出待删区间：保留 [upper, keepEnd]，其 key=upper 无需再遍历
				ranges.put(upper, keepEnd);
				break;
			}
			ceiling = ranges.ceilingEntry(lower);
		}
		return this;
	}

	/**
	 * 是否包含指定值。
	 *
	 * @param value 值
	 * @return 是否包含
	 */
	public boolean contains(T value) {
		Map.Entry<T, T> floor = ranges.floorEntry(value);
		return floor != null && floor.getValue().compareTo(value) > 0;
	}

	/**
	 * 合并后的全部区间（只读，按下界升序）。
	 *
	 * @return 区间列表
	 */
	public List<Range<T>> ranges() {
		List<Range<T>> list = new ArrayList<>();
		for (Map.Entry<T, T> e : ranges.entrySet()) {
			list.add(new Range<>(e.getKey(), e.getValue()));
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * 覆盖全集的最小区间（从最小下界到最大上界）；集合为空返回 {@code null}。
	 *
	 * @return 覆盖区间
	 */
	public Range<T> span() {
		if (ranges.isEmpty()) {
			return null;
		}
		return new Range<>(ranges.firstKey(), ranges.lastEntry().getValue());
	}

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	/**
	 * 清空所有区间。
	 */
	public void clear() {
		ranges.clear();
	}

	@Override
	public String toString() {
		return ranges().toString();
	}
}
