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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Set 工具类，参考 Guava {@code Sets} 与 Hutool 的 {@code SetUtil} 设计。
 *
 * @author suretool
 * @since 1.17.0
 */
public class SetUtil {

	private SetUtil() {
	}

	/**
	 * 创建 HashSet。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return HashSet
	 */
	@SafeVarargs
	public static <T> Set<T> newHashSet(T... values) {
		return CollUtil.newHashSet(values);
	}

	/**
	 * 创建 LinkedHashSet（保插入序）。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return LinkedHashSet
	 */
	@SafeVarargs
	public static <T> Set<T> newLinkedHashSet(T... values) {
		return CollUtil.newLinkedHashSet(values);
	}

	/**
	 * 创建 TreeSet（自然序）。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return TreeSet
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> Set<T> newTreeSet(T... values) {
		TreeSet<T> set = new TreeSet<>();
		if (values != null) {
			Collections.addAll(set, values);
		}
		return set;
	}

	/**
	 * 创建不可变 Set（去重保序，LinkedHashSet 拷贝后包装为不可变；null 元素保留）。
	 *
	 * @param values 元素（可为 null，表示空 Set）
	 * @param <T>    元素类型
	 * @return 不可变 Set
	 * @since 1.17.0
	 */
	@SafeVarargs
	public static <T> Set<T> of(T... values) {
		LinkedHashSet<T> set = new LinkedHashSet<>();
		if (values != null) {
			Collections.addAll(set, values);
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * 并集（保序去重）。
	 *
	 * @param left  左集合（null 按空处理）
	 * @param right 右集合（null 按空处理）
	 * @param <T>   元素类型
	 * @return 并集新 Set（LinkedHashSet）
	 * @since 1.17.0
	 */
	public static <T> Set<T> union(Set<T> left, Set<T> right) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (left != null) {
			result.addAll(left);
		}
		if (right != null) {
			result.addAll(right);
		}
		return result;
	}

	/**
	 * 交集（保序，按左集合出现顺序）。
	 *
	 * @param left  左集合（null 按空处理）
	 * @param right 右集合（null 按空处理）
	 * @param <T>   元素类型
	 * @return 交集新 Set
	 * @since 1.17.0
	 */
	public static <T> Set<T> intersection(Set<T> left, Set<T> right) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (left == null || right == null) {
			return result;
		}
		for (T item : left) {
			if (right.contains(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 差集（保序）：左集合中不在右集合的元素。
	 *
	 * @param left  左集合（null 按空处理）
	 * @param right 右集合（null 按空处理）
	 * @param <T>   元素类型
	 * @return 差集新 Set
	 * @since 1.17.0
	 */
	public static <T> Set<T> subtract(Set<T> left, Set<T> right) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (left == null) {
			return result;
		}
		if (right == null) {
			result.addAll(left);
			return result;
		}
		for (T item : left) {
			if (!right.contains(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 对称差（保序）：两个集合互斥元素（左右各自独有）。
	 *
	 * @param left  左集合（null 按空处理）
	 * @param right 右集合（null 按空处理）
	 * @param <T>   元素类型
	 * @return 对称差新 Set
	 * @since 1.17.0
	 */
	public static <T> Set<T> symmetricDifference(Set<T> left, Set<T> right) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (left != null) {
			for (T item : left) {
				if (right == null || !right.contains(item)) {
					result.add(item);
				}
			}
		}
		if (right != null) {
			for (T item : right) {
				if (left == null || !left.contains(item)) {
					result.add(item);
				}
			}
		}
		return result;
	}

	/**
	 * 过滤：仅保留满足谓词的元素（保序）。
	 *
	 * @param set       集合（null 返回空 Set）
	 * @param predicate 过滤谓词（null 时抛 {@link NullPointerException}）
	 * @param <T>       元素类型
	 * @return 过滤后新 Set
	 * @since 1.17.0
	 */
	public static <T> Set<T> filter(Set<T> set, Predicate<T> predicate) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (set == null) {
			return result;
		}
		for (T item : set) {
			if (predicate.test(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 映射：对每个元素应用函数生成新 Set（保序，结果自动去重）。
	 *
	 * @param set    集合（null 返回空 Set）
	 * @param mapper 映射函数（null 时抛 {@link NullPointerException}）
	 * @param <T>    元素类型
	 * @param <R>    结果类型
	 * @return 映射后新 Set
	 * @since 1.17.0
	 */
	public static <T, R> Set<R> map(Set<T> set, Function<T, R> mapper) {
		LinkedHashSet<R> result = new LinkedHashSet<>();
		if (set == null) {
			return result;
		}
		for (T item : set) {
			result.add(mapper.apply(item));
		}
		return result;
	}

	/**
	 * 笛卡尔积：左元素序 × 右元素序的二元组列表。
	 *
	 * @param left  左集合（null 按空处理）
	 * @param right 右集合（null 按空处理）
	 * @param <T>   左元素类型
	 * @param <U>   右元素类型
	 * @return 笛卡尔积（List.of 不可变对）
	 * @since 1.17.0
	 */
	public static <T, U> List<List<Object>> cartesianProduct(Set<T> left, Set<U> right) {
		List<List<Object>> result = new ArrayList<>();
		if (left == null || right == null) {
			return result;
		}
		for (T l : left) {
			for (U r : right) {
				result.add(List.of(l, r));
			}
		}
		return result;
	}

	/**
	 * 幂集：所有子集（含空集），元素序保持，共 2^n 个。
	 *
	 * @param set 集合（null 返回含空集的集合）
	 * @param <T> 元素类型
	 * @return 幂集
	 * @since 1.17.0
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> set) {
		Set<Set<T>> result = new LinkedHashSet<>();
		if (set == null) {
			result.add(new LinkedHashSet<>());
			return result;
		}
		List<T> list = new ArrayList<>(set);
		int size = 1 << list.size();
		for (int mask = 0; mask < size; mask++) {
			LinkedHashSet<T> subset = new LinkedHashSet<>();
			for (int i = 0; i < list.size(); i++) {
				if ((mask & (1 << i)) != 0) {
					subset.add(list.get(i));
				}
			}
			result.add(subset);
		}
		return result;
	}

	/**
	 * 前者是否为后者子集（含相等）。
	 *
	 * @param subset  候选子集（null 视为空集，恒为子集）
	 * @param superset 超集（null 视为空集）
	 * @param <T>     元素类型
	 * @return 是否子集
	 * @since 1.17.0
	 */
	public static <T> boolean isSubset(Set<T> subset, Set<T> superset) {
		if (subset == null) {
			return true;
		}
		if (superset == null) {
			return false;
		}
		return superset.containsAll(subset);
	}

	/**
	 * 前者是否为后者超集（含相等）。
	 *
	 * @param superset 候选超集（null 视为空集）
	 * @param subset   子集（null 视为空集）
	 * @param <T>      元素类型
	 * @return 是否超集
	 * @since 1.17.0
	 */
	public static <T> boolean isSuperset(Set<T> superset, Set<T> subset) {
		if (superset == null) {
			return subset == null || subset.isEmpty();
		}
		if (subset == null) {
			return true;
		}
		return superset.containsAll(subset);
	}

	/**
	 * 两集合是否无交集。
	 *
	 * @param left  左集合（null 视为空集）
	 * @param right 右集合（null 视为空集）
	 * @param <T>   元素类型
	 * @return 无交集返回 true
	 * @since 1.17.0
	 */
	public static <T> boolean disjoint(Set<T> left, Set<T> right) {
		if (left == null || right == null) {
			return true;
		}
		for (T item : left) {
			if (right.contains(item)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否与任一右集合元素相交。
	 *
	 * @param left  左集合（null 视为空）
	 * @param right 右集合（null 视为空）
	 * @param <T>   元素类型
	 * @return 有交集返回 true
	 * @since 1.17.0
	 */
	public static <T> boolean containsAny(Set<T> left, Set<T> right) {
		if (left == null || right == null) {
			return false;
		}
		for (T item : left) {
			if (right.contains(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 元素拼接为字符串。
	 *
	 * @param set      集合（null 返回空串）
	 * @param delimiter 分隔符
	 * @param <T>      元素类型
	 * @return 拼接结果
	 * @since 1.17.0
	 */
	public static <T> String join(Set<T> set, CharSequence delimiter) {
		if (set == null || set.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String sep = delimiter == null ? "" : delimiter.toString();
		boolean first = true;
		for (T item : set) {
			if (!first) {
				sb.append(sep);
			}
			sb.append(item);
			first = false;
		}
		return sb.toString();
	}

	/**
	 * null 返回空 Set。
	 *
	 * @param set 集合（可为 null）
	 * @param <T> 元素类型
	 * @return 原集合或空 LinkedHashSet
	 * @since 1.17.0
	 */
	public static <T> Set<T> emptyIfNull(Set<T> set) {
		return set == null ? new LinkedHashSet<>() : set;
	}

	/**
	 * 反序新 Set（逆序拷贝）。
	 *
	 * @param set 集合（null 返回空 Set）
	 * @param <T> 元素类型
	 * @return 反序新 Set
	 * @since 1.17.0
	 */
	public static <T> Set<T> reverse(Set<T> set) {
		LinkedHashSet<T> result = new LinkedHashSet<>();
		if (set == null) {
			return result;
		}
		List<T> list = new ArrayList<>(set);
		Collections.reverse(list);
		result.addAll(list);
		return result;
	}

	/**
	 * 安全取大小（null 返回 0）。
	 *
	 * @param set 集合（可为 null）
	 * @return 元素个数
	 * @since 1.17.0
	 */
	public static int size(Set<?> set) {
		return set == null ? 0 : set.size();
	}
}
