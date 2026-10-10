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
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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
	 * 获取集合中指定索引的元素，越界或空集合时返回默认值。
	 *
	 * @param collection   集合
	 * @param index        索引（支持负索引，-1 表示倒数第一个）
	 * @param defaultValue 默认值
	 * @param <T>          元素类型
	 * @return 元素或默认值
	 */
	public static <T> T get(Collection<T> collection, int index, T defaultValue) {
		T value = get(collection, index);
		return (value == null) ? defaultValue : value;
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


	/**
	 * 是否包含全部指定元素。
	 *
	 * @param collection 集合
	 * @param values     要检查的元素
	 * @return 是否全部包含
	 */
	@SafeVarargs
	public static <T> boolean containsAll(Collection<T> collection, T... values) {
		if (values == null || values.length == 0) {
			return true;
		}
		for (T value : values) {
			if (!contains(collection, value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否包含另一集合全部元素。
	 *
	 * @param collection 集合
	 * @param target     目标集合
	 * @return 是否全部包含
	 */
	public static boolean containsAll(Collection<?> collection, Collection<?> target) {
		if (isEmpty(target)) {
			return true;
		}
		for (Object item : target) {
			if (!contains(collection, item)) {
				return false;
			}
		}
		return true;
	}



	/**
	 * 按 Bean 属性排序（反射读取 getter），返回新列表。
	 *
	 * @param <T>         元素类型
	 * @param collection  集合
	 * @param property    属性名（如 "name"）
	 * @param isAscending 是否升序
	 * @return 排序后新列表
	 */
	/**
	 * 按属性升序排序。
	 *
	 * @param collection 集合
	 * @param property   属性名
	 * @param <T>        元素类型
	 * @return 排序后的列表
	 */
	public static <T> List<T> sortByProperty(Collection<T> collection, String property) {
		return sortByProperty(collection, property, true);
	}

	public static <T> List<T> sortByProperty(Collection<T> collection, String property, boolean isAscending) {
		if (isEmpty(collection)) {
			return new java.util.ArrayList<>();
		}
		List<T> list = new java.util.ArrayList<>(collection);
		String getter = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
		list.sort((a, b) -> {
			try {
				Object va = a.getClass().getMethod(getter).invoke(a);
				Object vb = b.getClass().getMethod(getter).invoke(b);
				int cmp;
				if (va instanceof Comparable && vb instanceof Comparable) {
					@SuppressWarnings("unchecked")
					Comparable<Object> ca = (Comparable<Object>) va;
					cmp = ca.compareTo(vb);
				} else if (va == null && vb == null) {
					cmp = 0;
				} else if (va == null) {
					cmp = -1;
				} else {
					cmp = 1;
				}
				return isAscending ? cmp : -cmp;
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("属性读取失败: " + property, e);
			}
		});
		return list;
	}



	/**
	 * 按固定大小切分为多组。
	 *
	 * @param <T>      元素类型
	 * @param list     列表
	 * @param groupSize 每组大小（&gt;0）
	 * @return 分组列表
	 */
	public static <T> List<List<T>> split(List<T> list, int groupSize) {
		if (groupSize <= 0) {
			throw new IllegalArgumentException("groupSize 必须大于 0");
		}
		List<List<T>> result = new java.util.ArrayList<>();
		if (isEmpty(list)) {
			return result;
		}
		for (int i = 0; i < list.size(); i += groupSize) {
			result.add(new java.util.ArrayList<>(list.subList(i, Math.min(i + groupSize, list.size()))));
		}
		return result;
	}

	/**
	 * int 集合求和。
	 *
	 * @param coll 集合
	 * @return 和
	 */
	public static int sum(Collection<Integer> coll) {
		int sum = 0;
		for (Integer v : coll) {
			sum += v == null ? 0 : v;
		}
		return sum;
	}

	/**
	 * long 集合求和。
	 *
	 * @param coll 集合
	 * @return 和
	 */
	public static long sumLong(Collection<Long> coll) {
		long sum = 0;
		for (Long v : coll) {
			sum += v == null ? 0 : v;
		}
		return sum;
	}

	/**
	 * double 集合求和。
	 *
	 * @param coll 集合
	 * @return 和
	 */
	public static double sumDouble(Collection<Double> coll) {
		double sum = 0;
		for (Double v : coll) {
			sum += v == null ? 0 : v;
		}
		return sum;
	}



	/**
	 * 最小值（Comparable 集合，null 安全）。
	 *
	 * @param <T>        元素类型（Comparable）
	 * @param collection 集合
	 * @return 最小值；空集合返回 null
	 */
	public static <T extends Comparable<? super T>> T min(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}
		T min = null;
		for (T item : collection) {
			if (item == null) {
				continue;
			}
			if (min == null || item.compareTo(min) < 0) {
				min = item;
			}
		}
		return min;
	}

	/**
	 * 最大值（Comparable 集合，null 安全）。
	 *
	 * @param <T>        元素类型（Comparable）
	 * @param collection 集合
	 * @return 最大值；空集合返回 null
	 */
	public static <T extends Comparable<? super T>> T max(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}
		T max = null;
		for (T item : collection) {
			if (item == null) {
				continue;
			}
			if (max == null || item.compareTo(max) > 0) {
				max = item;
			}
		}
		return max;
	}



	/**
	 * List 转 Map（key 由函数提取，重复 key 后值覆盖）。
	 *
	 * @param <T>         元素类型
	 * @param <K>         键类型
	 * @param list        列表
	 * @param keyFunction 键提取函数
	 * @return Map
	 */
	public static <T, K> Map<K, T> listToMap(Collection<T> list, java.util.function.Function<T, K> keyFunction) {
		Map<K, T> result = new java.util.LinkedHashMap<>();
		if (isEmpty(list)) {
			return result;
		}
		for (T item : list) {
			if (item != null) {
				result.put(keyFunction.apply(item), item);
			}
		}
		return result;
	}



	/**
	 * 按提取函数取最小值。
	 *
	 * @param collection 集合
	 * @param keyMapper  提取函数
	 * @param <T>        元素类型
	 * @param <R>        键类型
	 * @return 最小元素；空集合返回 null
	 */
	public static <T, R extends Comparable<R>> T minBy(Collection<T> collection, java.util.function.Function<T, R> keyMapper) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		T best = null;
		R bestKey = null;
		for (T item : collection) {
			R key = keyMapper.apply(item);
			if (best == null || key.compareTo(bestKey) < 0) {
				best = item;
				bestKey = key;
			}
		}
		return best;
	}

	/**
	 * 按提取函数取最大值。
	 *
	 * @param collection 集合
	 * @param keyMapper  提取函数
	 * @param <T>        元素类型
	 * @param <R>        键类型
	 * @return 最大元素；空集合返回 null
	 */
	public static <T, R extends Comparable<R>> T maxBy(Collection<T> collection, java.util.function.Function<T, R> keyMapper) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		T best = null;
		R bestKey = null;
		for (T item : collection) {
			R key = keyMapper.apply(item);
			if (best == null || key.compareTo(bestKey) > 0) {
				best = item;
				bestKey = key;
			}
		}
		return best;
	}



	/**
	 * 将两个集合按顺序配对为 Map。
	 *
	 * @param keys   键集合
	 * @param values 值集合
	 * @param <K>    键类型
	 * @param <V>    值类型
	 * @return 配对后的 Map（LinkedHashMap，取较短长度）
	 */
	public static <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
		Map<K, V> map = new LinkedHashMap<>();
		if (keys == null || values == null) {
			return map;
		}
		java.util.Iterator<K> keyIt = keys.iterator();
		java.util.Iterator<V> valueIt = values.iterator();
		while (keyIt.hasNext() && valueIt.hasNext()) {
			map.put(keyIt.next(), valueIt.next());
		}
		return map;
	}



	/**
	 * 移除集合中的 null 元素，返回新列表。
	 *
	 * @param coll 集合
	 * @param <T>  元素类型
	 * @return 不含 null 的新列表
	 */
	public static <T> List<T> removeNull(java.util.Collection<T> coll) {
		if (coll == null || coll.isEmpty()) {
			return new java.util.ArrayList<>();
		}
		List<T> result = new java.util.ArrayList<>(coll.size());
		for (T item : coll) {
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}



	/**
	 * 按属性降序排序。
	 *
	 * @param collection 集合
	 * @param property   属性名
	 * @param <T>        元素类型
	 * @return 排序后的列表
	 */
	public static <T> List<T> sortByPropertyDesc(Collection<T> collection, String property) {
		return sortByProperty(collection, property, false);
	}

	/**
	 * 在列表中查找元素下标。
	 *
	 * @param list  列表
	 * @param value 元素
	 * @param <T>   元素类型
	 * @return 下标或 -1
	 */
	public static <T> int indexOf(List<T> list, Object value) {
		if (list == null) {
			return -1;
		}
		for (int i = 0; i < list.size(); i++) {
			if (java.util.Objects.equals(list.get(i), value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 在列表中查找元素最后出现的下标。
	 *
	 * @param list  列表
	 * @param value 元素
	 * @param <T>   元素类型
	 * @return 下标或 -1
	 */
	public static <T> int lastIndexOf(List<T> list, Object value) {
		if (list == null) {
			return -1;
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			if (java.util.Objects.equals(list.get(i), value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 将集合按指定大小分片。
	 *
	 * @param collection 输入集合，可为空
	 * @param size       每片大小，必须大于 0
	 * @param <T>        元素类型
	 * @return 分片列表；输入为空时返回空列表
	 * @throws IllegalArgumentException size 不大于 0
	 * @since 1.1.0
	 */
	public static <T> List<List<T>> chunk(Collection<T> collection, int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("chunk size 必须大于 0");
		}
		if (collection == null || collection.isEmpty()) {
			return List.of();
		}
		List<T> list = collection instanceof List<T> l ? l : new ArrayList<>(collection);
		List<List<T>> chunks = new ArrayList<>((list.size() + size - 1) / size);
		for (int i = 0; i < list.size(); i += size) {
			chunks.add(List.copyOf(list.subList(i, Math.min(list.size(), i + size))));
		}
		return List.copyOf(chunks);
	}

	/**
	 * 从集合中随机取一个元素。
	 *
	 * @param collection 输入集合，不允许为空
	 * @param <T>        元素类型
	 * @return 随机元素
	 * @throws IllegalArgumentException 集合为空
	 * @since 1.1.0
	 */
	public static <T> T randomItem(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("randomItem 集合不允许为空");
		}
		if (collection instanceof List<T> list) {
			return list.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(list.size()));
		}
		List<T> list = new ArrayList<>(collection);
		return list.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(list.size()));
	}

	/**
	 * 从集合中随机取指定数量的元素（不重复）。
	 *
	 * @param collection 输入集合，可为空
	 * @param count      取样数量
	 * @param <T>        元素类型
	 * @return 随机取样列表；count 不大于 0 时返回空列表
	 * @since 1.1.0
	 */
	public static <T> List<T> randomItems(Collection<T> collection, int count) {
		if (count <= 0 || collection == null || collection.isEmpty()) {
			return List.of();
		}
		List<T> list = new ArrayList<>(collection);
		if (count >= list.size()) {
			Collections.shuffle(list);
			return List.copyOf(list);
		}
		Collections.shuffle(list);
		return List.copyOf(list.subList(0, count));
	}

	/**
	 * 返回集合的不可变副本。
	 *
	 * @param collection 输入集合，可为空
	 * @param <T>        元素类型
	 * @return 不可变列表；输入为空时返回空列表
	 * @since 1.1.0
	 */
	public static <T> List<T> toImmutable(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return List.of();
		}
		return List.copyOf(collection);
	}

	/**
	 * 统计元素在集合中出现的次数（基于 {@link java.util.Objects#equals}）。
	 *
	 * @param collection 输入集合，可为空
	 * @param value      目标元素
	 * @param <T>        元素类型
	 * @return 出现次数
	 * @since 1.1.0
	 */
	public static <T> int frequency(Collection<T> collection, T value) {
		if (collection == null || collection.isEmpty()) {
			return 0;
		}
		return Collections.frequency(collection, value);
	}

	/**
	 * 旋转列表元素（正数为右旋，负数为左旋）。
	 *
	 * @param list  列表
	 * @param steps 旋转步数（可为负）
	 * @param <T>   元素类型
	 * @return 原列表（原地操作）
	 */
	public static <T> List<T> rotate(List<T> list, int steps) {
		if (list != null && list.size() > 1) {
			Collections.rotate(list, steps);
		}
		return list;
	}

	/**
	 * 移除集合中的 null 与空元素（空字符串/空集合/空 Map）。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 过滤后的集合（原地移除）
	 */
	public static <T> Collection<T> removeEmpty(Collection<T> collection) {
		if (collection == null) {
			return null;
		}
		collection.removeIf(CollUtil::isEmptyValue);
		return collection;
	}

	/**
	 * 移除集合中的指定元素。
	 *
	 * @param collection 集合
	 * @param values     要移除的元素
	 * @param <T>        元素类型
	 * @return 过滤后的集合（原地移除）
	 */
	@SafeVarargs
	public static <T> Collection<T> removeAny(Collection<T> collection, T... values) {
		if (collection == null || values == null || values.length == 0) {
			return collection;
		}
		Set<T> removeSet = new HashSet<>(java.util.Arrays.asList(values));
		collection.removeIf(removeSet::contains);
		return collection;
	}

	/**
	 * 判断多个集合是否全部为空（null 或空集合视为空）。
	 *
	 * @param collections 集合数组
	 * @return 全部为空返回 {@code true}
	 */
	@SafeVarargs
	public static boolean isAllEmpty(Collection<?>... collections) {
		if (collections == null || collections.length == 0) {
			return false;
		}
		for (Collection<?> collection : collections) {
			if (!isEmpty(collection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断多个集合是否全部非空（非 null 且非空集合）。
	 *
	 * @param collections 集合数组
	 * @return 全部非空返回 {@code true}
	 */
	@SafeVarargs
	public static boolean isAllNotNull(Collection<?>... collections) {
		if (collections == null || collections.length == 0) {
			return false;
		}
		for (Collection<?> collection : collections) {
			if (isEmpty(collection)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isEmptyValue(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof CharSequence str) {
			return str.isEmpty();
		}
		if (value instanceof Collection<?> collection) {
			return collection.isEmpty();
		}
		if (value instanceof Map<?, ?> map) {
			return map.isEmpty();
		}
		return false;
	}

	/**
	 * 创建带比较器的有序 Set（TreeSet），并填充元素。
	 *
	 * @param comparator 比较器（可空，空则自然序）
	 * @param values     元素
	 * @param <T>        元素类型
	 * @return TreeSet
	 * @since 1.10.0
	 */
	@SafeVarargs
	public static <T> TreeSet<T> newTreeSet(Comparator<? super T> comparator, T... values) {
		TreeSet<T> set = comparator == null ? new TreeSet<>() : new TreeSet<>(comparator);
		if (values != null) {
			Collections.addAll(set, values);
		}
		return set;
	}

	/**
	 * 创建线程安全的 Set（基于 ConcurrentHashMap），并填充元素。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return 并发 Set
	 * @since 1.10.0
	 */
	@SafeVarargs
	public static <T> Set<T> newConcurrentHashSet(T... values) {
		Set<T> set = ConcurrentHashMap.newKeySet();
		if (values != null) {
			Collections.addAll(set, values);
		}
		return set;
	}

	/**
	 * 展平一层嵌套集合。
	 *
	 * @param nested 嵌套集合（内层可为空）
	 * @param <T>    元素类型
	 * @return 展平后的列表
	 * @since 1.10.0
	 */
	public static <T> List<T> flatMap(Collection<? extends Collection<? extends T>> nested) {
		List<T> result = new ArrayList<>();
		if (nested == null) {
			return result;
		}
		for (Collection<? extends T> inner : nested) {
			if (inner != null) {
				result.addAll(inner);
			}
		}
		return result;
	}

	/**
	 * 交换列表中两个位置的元素。
	 *
	 * @param list  列表
	 * @param index1 位置一
	 * @param index2 位置二
	 * @param <T>   元素类型
	 * @return 本列表，支持链式调用
	 * @throws IndexOutOfBoundsException 下标越界
	 * @since 1.10.0
	 */
	public static <T> List<T> swap(List<T> list, int index1, int index2) {
		Collections.swap(list, index1, index2);
		return list;
	}

	/**
	 * 集合转数组。
	 *
	 * @param iterable 可迭代对象
	 * @param type     数组元素类型
	 * @param <T>      元素类型
	 * @return 数组
	 * @since 1.10.0
	 */
	public static <T> T[] toArray(Iterable<T> iterable, Class<T> type) {
		if (iterable == null) {
			return null;
		}
		List<T> list = toList(iterable);
		@SuppressWarnings("unchecked")
		T[] array = (T[]) java.lang.reflect.Array.newInstance(type, list.size());
		return list.toArray(array);
	}

	/**
	 * 返回集合中出现次数最多的元素（众数）；并列时取先出现的元素。
	 *
	 * @param collection 集合
	 * @param <T>        元素类型
	 * @return 众数元素；集合为空返回 {@code null}
	 * @since 1.10.0
	 */
	public static <T> T maxCount(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		Map<T, Integer> freq = countMap(collection);
		T mode = null;
		int max = 0;
		for (T item : collection) {
			int c = freq.getOrDefault(item, 0);
			if (c > max) {
				max = c;
				mode = item;
			}
		}
		return mode;
	}

	/**
	 * 两个集合是否存在交集。
	 *
	 * @param c1 集合一
	 * @param c2 集合二
	 * @return 存在交集返回 {@code true}
	 * @since 1.10.0
	 */
	public static boolean containsAny(Collection<?> c1, Collection<?> c2) {
		if (c1 == null || c2 == null || c1.isEmpty() || c2.isEmpty()) {
			return false;
		}
		for (Object o : c1) {
			if (c2.contains(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 按谓词将集合分为「匹配」与「不匹配」两部分（均保序），等价于一次性完成 filter 与其补集。
	 *
	 * @param <T> 元素类型
	 * @param collection 待分集合（null 按空集合处理）
	 * @param predicate 匹配谓词（null 时抛 {@link NullPointerException}）
	 * @return 二分结果 {@link Partition}
	 * @since 1.15.0
	 */
	public static <T> Partition<T> partitionBy(Collection<T> collection, Predicate<T> predicate) {
		List<T> matched = new ArrayList<>();
		List<T> unmatched = new ArrayList<>();
		if (collection == null || collection.isEmpty()) {
			return new Partition<>(matched, unmatched);
		}
		for (T item : collection) {
			if (predicate.test(item)) {
				matched.add(item);
			} else {
				unmatched.add(item);
			}
		}
		return new Partition<>(matched, unmatched);
	}

	/**
	 * 按两级键进行嵌套分组：先按 keyMapper1 分组，组内再按 keyMapper2 分组（均保序）。
	 *
	 * @param <T> 元素类型
	 * @param <K1> 一级键类型
	 * @param <K2> 二级键类型
	 * @param collection 待分组集合（null 按空集合处理）
	 * @param keyMapper1 一级键映射（null 时抛 {@link NullPointerException}）
	 * @param keyMapper2 二级键映射（null 时抛 {@link NullPointerException}）
	 * @return 两级嵌套分组（LinkedHashMap 保序）
	 * @since 1.15.0
	 */
	public static <T, K1, K2> Map<K1, Map<K2, List<T>>> groupBy2(
			Collection<T> collection, Function<T, K1> keyMapper1, Function<T, K2> keyMapper2) {
		Map<K1, Map<K2, List<T>>> result = new LinkedHashMap<>();
		if (collection == null) {
			return result;
		}
		for (T item : collection) {
			K1 k1 = keyMapper1.apply(item);
			K2 k2 = keyMapper2.apply(item);
			Map<K2, List<T>> inner = result.computeIfAbsent(k1, k -> new LinkedHashMap<>());
			inner.computeIfAbsent(k2, k -> new ArrayList<>()).add(item);
		}
		return result;
	}

	/**
	 * 原地去重合并：将 source 中 target 尚不存在的元素追加到 target 末尾（保 source 顺序）。
	 *
	 * @param <T> 元素类型
	 * @param target 目标集合（null 时不操作，返回 {@code false}）
	 * @param source 来源可迭代对象（null 时不操作，返回 {@code false}）
	 * @return 发生变更（有新增元素）返回 {@code true}
	 * @since 1.15.0
	 */
	public static <T> boolean addAllDistinct(Collection<T> target, Iterable<T> source) {
		if (target == null || source == null) {
			return false;
		}
		Set<T> seen = new HashSet<>(target);
		boolean changed = false;
		for (T item : source) {
			if (seen.add(item)) {
				target.add(item);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * 原地差集：从 target 中移除 source 出现的全部元素（重复元素全部移除，语义对齐 {@link #subtract}）。
	 * source 与 target 为同一对象时同样安全（内部先复制）。
	 *
	 * @param <T> 元素类型
	 * @param target 目标集合（null 时不操作，返回 {@code false}）
	 * @param source 待移除元素来源（null 时不操作，返回 {@code false}）
	 * @return 发生变更（有元素被移除）返回 {@code true}
	 * @since 1.15.0
	 */
	public static <T> boolean removeAll(Collection<T> target, Iterable<T> source) {
		if (target == null || source == null) {
			return false;
		}
		List<T> src = new ArrayList<>();
		for (T item : source) {
			src.add(item);
		}
		boolean changed = false;
		for (T item : src) {
			while (target.remove(item)) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * 原地交集：仅保留 target 中同时存在于 source 的元素（保 target 原有顺序）。
	 *
	 * @param <T> 元素类型
	 * @param target 目标集合（null 时不操作，返回 {@code false}）
	 * @param source 保留元素来源（null 时不操作，返回 {@code false}）
	 * @return 发生变更（有元素被移除）返回 {@code true}
	 * @since 1.15.0
	 */
	public static <T> boolean retainAll(Collection<T> target, Iterable<T> source) {
		if (target == null || source == null) {
			return false;
		}
		Set<T> keep = new HashSet<>();
		for (T item : source) {
			keep.add(item);
		}
		return target.retainAll(keep);
	}

	/**
	 * 两级扁平化：将「集合的集合」展开为单层列表，内层 null 集合跳过。
	 *
	 * @param <T> 元素类型
	 * @param nested 嵌套集合（null 按空集合处理）
	 * @return 扁平化后的列表（保外层与内层顺序）
	 * @since 1.15.0
	 */
	public static <T> List<T> flatten(Collection<? extends Collection<T>> nested) {
		List<T> result = new ArrayList<>();
		if (nested == null) {
			return result;
		}
		for (Collection<T> inner : nested) {
			if (inner != null) {
				result.addAll(inner);
			}
		}
		return result;
	}

	/**
	 * 相邻配对：返回相邻元素的二元组列表，长度为 size-1；少于 2 个元素时返回空列表。
	 *
	 * @param <T> 元素类型
	 * @param collection 待配对集合（null 按空集合处理）
	 * @return 相邻二元组列表（每对为不可变 {@link List}）
	 * @since 1.15.0
	 */
	public static <T> List<List<T>> pairwise(Collection<T> collection) {
		List<List<T>> result = new ArrayList<>();
		if (collection == null) {
			return result;
		}
		List<T> list = collection instanceof List ? (List<T>) collection : new ArrayList<>(collection);
		for (int i = 0; i + 1 < list.size(); i++) {
			result.add(List.of(list.get(i), list.get(i + 1)));
		}
		return result;
	}

	/**
	 * 从头取满足谓词的元素前缀，遇到第一个不满足的元素即停止（Stream.takeWhile 的集合版）。
	 *
	 * @param <T> 元素类型
	 * @param collection 待取集合（null 按空集合处理）
	 * @param predicate 满足条件（null 时抛 {@link NullPointerException}）
	 * @return 满足谓词的前缀列表（保序）
	 * @since 1.15.0
	 */
	public static <T> List<T> takeWhile(Collection<T> collection, Predicate<T> predicate) {
		List<T> result = new ArrayList<>();
		if (collection == null) {
			return result;
		}
		for (T item : collection) {
			if (!predicate.test(item)) {
				break;
			}
			result.add(item);
		}
		return result;
	}

}