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

import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 集合工具类，参考 Hutool 的 {@code CollUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CollUtil {

	private CollUtil() {
	}

	// ---------------- 判空 ----------------

	/**
	 * 集合是否为空。
	 *
	 * @param collection 集合
	 * @return 是否为空
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * 集合是否非空。
	 *
	 * @param collection 集合
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * Map 是否为空。
	 *
	 * @param map Map
	 * @return 是否为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Map 是否非空。
	 *
	 * @param map Map
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * 迭代器是否为空。
	 *
	 * @param iterator 迭代器
	 * @return 是否为空
	 */
	public static boolean isEmpty(Iterator<?> iterator) {
		return iterator == null || !iterator.hasNext();
	}

	/**
	 * 枚举是否为空。
	 *
	 * @param enumeration 枚举
	 * @return 是否为空
	 */
	public static boolean isEmpty(Enumeration<?> enumeration) {
		return enumeration == null || !enumeration.hasMoreElements();
	}

	/**
	 * 数组是否为空。
	 *
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(Object array) {
		return ArrayUtil.isEmpty(array);
	}

	// ---------------- 创建 ----------------

	/**
	 * 创建 ArrayList。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return ArrayList
	 */
	@SafeVarargs
	public static <T> List<T> newArrayList(T... values) {
		List<T> list = new ArrayList<>((values == null) ? 0 : values.length);
		if (values != null) {
			Collections.addAll(list, values);
		}
		return list;
	}

	/**
	 * 创建 HashSet。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return HashSet
	 */
	@SafeVarargs
	public static <T> HashSet<T> newHashSet(T... values) {
		HashSet<T> set = new HashSet<>((values == null) ? 0 : values.length);
		if (values != null) {
			Collections.addAll(set, values);
		}
		return set;
	}

	/**
	 * 创建 LinkedHashSet。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return LinkedHashSet
	 */
	@SafeVarargs
	public static <T> LinkedHashSet<T> newLinkedHashSet(T... values) {
		LinkedHashSet<T> set = new LinkedHashSet<>((values == null) ? 0 : values.length);
		if (values != null) {
			Collections.addAll(set, values);
		}
		return set;
	}

	// ---------------- 包含 ----------------

	/**
	 * 集合是否包含指定元素。
	 *
	 * @param collection 集合
	 * @param value      元素
	 * @return 是否包含
	 */
	public static boolean contains(Collection<?> collection, Object value) {
		return collection != null && collection.contains(value);
	}

	/**
	 * 集合是否包含任一指定元素。
	 *
	 * @param collection 集合
	 * @param values     元素数组
	 * @return 是否包含任一元素
	 */
	public static boolean containsAny(Collection<?> collection, Object... values) {
		if (collection == null || values == null) {
			return false;
		}
		for (Object value : values) {
			if (collection.contains(value)) {
				return true;
			}
		}
		return false;
	}

	// ---------------- 集合运算 ----------------

	/**
	 * 并集（去重）。
	 *
	 * @param c1 集合 1
	 * @param c2 集合 2
	 * @param <T> 元素类型
	 * @return 并集
	 */
	public static <T> List<T> union(Collection<T> c1, Collection<T> c2) {
		Set<T> set = new LinkedHashSet<>();
		if (c1 != null) {
			set.addAll(c1);
		}
		if (c2 != null) {
			set.addAll(c2);
		}
		return new ArrayList<>(set);
	}

	/**
	 * 交集（去重）。
	 *
	 * @param c1 集合 1
	 * @param c2 集合 2
	 * @param <T> 元素类型
	 * @return 交集
	 */
	public static <T> List<T> intersection(Collection<T> c1, Collection<T> c2) {
		List<T> result = new ArrayList<>();
		if (c1 == null || c2 == null) {
			return result;
		}
		Set<T> seen = new HashSet<>();
		for (T t : c1) {
			if (c2.contains(t) && seen.add(t)) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * 差集（属于 c1 但不属于 c2）。
	 *
	 * @param c1 集合 1
	 * @param c2 集合 2
	 * @param <T> 元素类型
	 * @return 差集
	 */
	public static <T> List<T> disjunction(Collection<T> c1, Collection<T> c2) {
		List<T> result = new ArrayList<>();
		if (c1 == null) {
			return result;
		}
		Set<T> c2Set = (c2 == null) ? Collections.<T>emptySet() : new HashSet<>(c2);
		for (T t : c1) {
			if (!c2Set.contains(t) && !result.contains(t)) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * 对称差集（属于 c1 或 c2 但不同时属于两者）。
	 *
	 * @param c1 集合 1
	 * @param c2 集合 2
	 * @param <T> 元素类型
	 * @return 对称差集
	 */
	public static <T> List<T> xor(Collection<T> c1, Collection<T> c2) {
		List<T> union = union(c1, c2);
		List<T> intersection = intersection(c1, c2);
		union.removeAll(intersection);
		return union;
	}

	// ---------------- 转换 ----------------

	/**
	 * 拼接集合元素。
	 *
	 * @param collection 集合
	 * @param delimiter  分隔符
	 * @return 拼接结果
	 */
	public static String join(Collection<?> collection, CharSequence delimiter) {
		return StrUtil.join(delimiter, collection);
	}

	/**
	 * 集合转 List（安全拷贝）。
	 *
	 * @param iterable 可迭代集合
	 * @param <T>      元素类型
	 * @return List，空集合返回空列表
	 */
	public static <T> List<T> toList(Iterable<T> iterable) {
		if (iterable == null) {
			return new ArrayList<>();
		}
		if (iterable instanceof Collection) {
			return new ArrayList<>((Collection<T>) iterable);
		}
		List<T> list = new ArrayList<>();
		for (T t : iterable) {
			list.add(t);
		}
		return list;
	}

	/**
	 * 迭代器转 List。
	 *
	 * @param iterator 迭代器
	 * @param <T>      元素类型
	 * @return List
	 */
	public static <T> List<T> toList(Iterator<T> iterator) {
		List<T> list = new ArrayList<>();
		if (iterator != null) {
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}
		return list;
	}

	/**
	 * 枚举转 List。
	 *
	 * @param enumeration 枚举
	 * @param <T>         元素类型
	 * @return List
	 */
	public static <T> List<T> toList(Enumeration<T> enumeration) {
		List<T> list = new ArrayList<>();
		if (enumeration != null) {
			while (enumeration.hasMoreElements()) {
				list.add(enumeration.nextElement());
			}
		}
		return list;
	}

	// ---------------- 获取元素 ----------------

	/**
	 * 获取指定索引的元素（负数从末尾倒数）。
	 *
	 * @param collection 集合
	 * @param index      索引
	 * @param <T>        元素类型
	 * @return 元素，越界返回 {@code null}
	 */
	public static <T> T get(Collection<T> collection, int index) {
		if (isEmpty(collection)) {
			return null;
		}
		if (collection instanceof List) {
			List<T> list = (List<T>) collection;
			if (index < 0) {
				index = list.size() + index;
			}
			return (index >= 0 && index < list.size()) ? list.get(index) : null;
		}
		if (index < 0) {
			index = collection.size() + index;
		}
		if (index < 0 || index >= collection.size()) {
			return null;
		}
		Iterator<T> iterator = collection.iterator();
		for (int i = 0; i < index; i++) {
			iterator.next();
		}
		return iterator.next();
	}

	/**
	 * 获取第一个元素。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 第一个元素
	 */
	public static <T> T getFirst(Collection<T> collection) {
		return get(collection, 0);
	}

	/**
	 * 获取最后一个元素。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 最后一个元素
	 */
	public static <T> T getLast(Collection<T> collection) {
		return get(collection, -1);
	}

	// ---------------- 函数式操作 ----------------

	/**
	 * 过滤集合（返回新列表）。
	 *
	 * @param collection 集合
	 * @param predicate  过滤条件
	 * @param <T>        元素类型
	 * @return 过滤后的列表
	 */
	public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
		List<T> result = new ArrayList<>();
		if (collection == null || predicate == null) {
			return result;
		}
		for (T t : collection) {
			if (predicate.test(t)) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * 映射集合元素（返回新列表）。
	 *
	 * @param collection 集合
	 * @param mapper     映射函数
	 * @param <T>        源元素类型
	 * @param <R>        目标元素类型
	 * @return 映射后的列表
	 */
	public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
		List<R> result = new ArrayList<>();
		if (collection == null || mapper == null) {
			return result;
		}
		for (T t : collection) {
			result.add(mapper.apply(t));
		}
		return result;
	}

	/**
	 * 按 key 分组。
	 *
	 * @param collection 集合
	 * @param keyMapper  key 提取函数
	 * @param <T>        元素类型
	 * @param <K>        key 类型
	 * @return 分组 Map
	 */
	public static <T, K> Map<K, List<T>> groupByKey(Collection<T> collection, Function<T, K> keyMapper) {
		Map<K, List<T>> result = new LinkedHashMap<>();
		if (collection == null || keyMapper == null) {
			return result;
		}
		for (T t : collection) {
			K key = keyMapper.apply(t);
			result.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
		}
		return result;
	}

	/**
	 * 去重（保持原有顺序）。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 去重后的列表
	 */
	public static <T> List<T> distinct(Collection<T> collection) {
		if (collection == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(new LinkedHashSet<>(collection));
	}

	/**
	 * 排序（返回新列表）。
	 *
	 * @param collection 集合
	 * @param comparator 比较器
	 * @param <T>        元素类型
	 * @return 排序后的列表
	 */
	public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
		List<T> list = new ArrayList<>(collection);
		list.sort(comparator);
		return list;
	}

	// ---------------- 其他 ----------------

	/**
	 * 集合大小。
	 *
	 * @param collection 集合
	 * @return 大小，空集合返回 0
	 */
	public static int size(Collection<?> collection) {
		return (collection == null) ? 0 : collection.size();
	}

	/**
	 * 截取列表子区间（负数索引从末尾倒数）。
	 *
	 * @param list  列表
	 * @param from  起始索引（含）
	 * @param to    结束索引（不含）
	 * @param <T>   元素类型
	 * @return 子列表
	 */
	public static <T> List<T> sub(List<T> list, int from, int to) {
		if (isEmpty(list)) {
			return new ArrayList<>();
		}
		int size = list.size();
		if (from < 0) {
			from = size + from;
		}
		if (to < 0) {
			to = size + to;
		}
		if (from < 0) {
			from = 0;
		}
		if (to > size) {
			to = size;
		}
		if (from >= to) {
			return new ArrayList<>();
		}
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * 反转列表（原地反转）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 反转后的列表
	 */
	public static <T> List<T> reverse(List<T> list) {
		if (list != null) {
			Collections.reverse(list);
		}
		return list;
	}

	/**
	 * 空集合返回空列表。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 非空返回原列表，否则返回空列表
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyIfNull(Collection<T> collection) {
		if (collection == null) {
			return Collections.emptyList();
		}
		if (collection instanceof List) {
			return (List<T>) collection;
		}
		return new ArrayList<>(collection);
	}

	// ---------------- P4 增强：分页/洗牌/频次/差集/映射/合并 ----------------

	/**
	 * Iterable 是否为空。
	 *
	 * @param iterable 可迭代对象
	 * @return 是否为空
	 */
	public static boolean isEmpty(Iterable<?> iterable) {
		return iterable == null || !iterable.iterator().hasNext();
	}

	/**
	 * 分页（页码从 1 开始），越界时返回空列表。
	 *
	 * @param list 原列表
	 * @param page 页码（&gt;=1）
	 * @param size 每页条数（&gt;0）
	 * @param <T>  元素类型
	 * @return 当前页元素列表
	 */
	public static <T> List<T> page(List<T> list, int page, int size) {
		if (list == null || list.isEmpty() || page < 1 || size < 1) {
			return Collections.emptyList();
		}
		int from = Math.min((page - 1) * size, list.size());
		int to = Math.min(from + size, list.size());
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * 就地洗牌（Fisher-Yates），返回原列表以便链式调用。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 洗牌后的原列表
	 */
	public static <T> List<T> shuffle(List<T> list) {
		if (list != null) {
			Collections.shuffle(list);
		}
		return list;
	}

	/**
	 * 统计元素出现频次。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 元素到频次的映射
	 */
	public static <T> Map<T, Integer> countMap(Collection<T> collection) {
		Map<T, Integer> map = new LinkedHashMap<>();
		if (collection == null) {
			return map;
		}
		for (T item : collection) {
			map.merge(item, 1, Integer::sum);
		}
		return map;
	}

	/**
	 * 单向差集：返回 c1 中存在但 c2 中不存在的元素。
	 *
	 * @param c1 左集合
	 * @param c2 右集合
	 * @param <T> 元素类型
	 * @return c1 - c2 的结果列表
	 */
	public static <T> List<T> subtract(Collection<T> c1, Collection<T> c2) {
		List<T> result = new ArrayList<>();
		if (isEmpty(c1)) {
			return result;
		}
		Set<T> set = (c2 == null || c2.isEmpty()) ? Set.of() : new HashSet<>(c2);
		for (T item : c1) {
			if (!set.contains(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 列表转映射。
	 *
	 * @param collection   集合
	 * @param keyMapper    key 提取函数
	 * @param valueMapper  value 提取函数
	 * @param <T>          元素类型
	 * @param <K>          key 类型
	 * @param <V>          value 类型
	 * @return 映射（重复 key 时后者覆盖前者）
	 */
	public static <T, K, V> Map<K, V> toMap(Collection<T> collection, Function<T, K> keyMapper, Function<T, V> valueMapper) {
		Map<K, V> map = new LinkedHashMap<>();
		if (collection == null) {
			return map;
		}
		for (T item : collection) {
			map.put(keyMapper.apply(item), valueMapper.apply(item));
		}
		return map;
	}

	/**
	 * 将 source 全部追加到 target 并返回 target。
	 *
	 * @param target 目标集合
	 * @param source 源集合
	 * @param <T>    元素类型
	 * @return target
	 */
	public static <T> Collection<T> addAll(Collection<T> target, Collection<T> source) {
		if (target == null) {
			throw new IllegalArgumentException("target 不能为 null");
		}
		if (source != null) {
			target.addAll(source);
		}
		return target;
	}

}