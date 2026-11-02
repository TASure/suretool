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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 迭代器工具类，参考 Hutool 的 {@code IterUtil} 设计。
 *
 * <p>所有方法对 {@code null} 迭代器/可迭代对象保持空安全；消耗迭代器的方法
 * （{@code toList}/{@code size}/{@code getFirst}/{@code clear} 等）会推进迭代器，
 * 调用方不应在之后继续使用同一迭代器。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * List<String> list = List.of("a", "bb", "ccc");
 * List<String> filtered = IterUtil.filterToList(list, s -> s.length() > 1); // [bb, ccc]
 * List<Integer> lengths = IterUtil.trans(list, String::length);            // [1, 2, 3]
 * String joined = IterUtil.join(list, ",");                                // "a,bb,ccc"
 * }</pre>
 *
 * <p>null 语义：迭代器/可迭代对象为 {@code null} 时视为空（空安全），
 * {@code getFirst} 返回 {@code null}，其余集合类方法返回空集合/空串。</p>
 *
 * @author suretool
 * @since 1.20.0
 */
public final class IterUtil {

	private IterUtil() {
	}

	/**
	 * 获取可迭代对象的迭代器。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @param <T>      元素类型
	 * @return 迭代器；{@code null} 返回空迭代器
	 */
	public static <T> Iterator<T> getIter(Iterable<T> iterable) {
		return iterable == null ? empty() : iterable.iterator();
	}

	/**
	 * 返回空迭代器。
	 *
	 * @param <T> 元素类型
	 * @return 空迭代器
	 */
	public static <T> Iterator<T> empty() {
		return Collections.emptyIterator();
	}

	/**
	 * 判断可迭代对象是否为空。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 无元素返回 {@code true}
	 */
	public static boolean isEmpty(Iterable<?> iterable) {
		return iterable == null || !iterable.iterator().hasNext();
	}

	/**
	 * 判断迭代器是否为空（不消耗元素）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @return 无元素返回 {@code true}
	 */
	public static boolean isEmpty(Iterator<?> iterator) {
		return iterator == null || !iterator.hasNext();
	}

	/**
	 * 判断可迭代对象是否非空。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 有元素返回 {@code true}
	 */
	public static boolean isNotEmpty(Iterable<?> iterable) {
		return !isEmpty(iterable);
	}

	/**
	 * 判断迭代器是否非空（不消耗元素）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @return 有元素返回 {@code true}
	 */
	public static boolean isNotEmpty(Iterator<?> iterator) {
		return !isEmpty(iterator);
	}

	/**
	 * 判断可迭代对象是否包含 {@code null} 元素。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 含 {@code null} 返回 {@code true}
	 */
	public static boolean hasNull(Iterable<?> iterable) {
		return hasNull(getIter(iterable));
	}

	/**
	 * 判断迭代器是否包含 {@code null} 元素（消耗迭代器）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @return 含 {@code null} 返回 {@code true}
	 */
	public static boolean hasNull(Iterator<?> iterator) {
		if (iterator == null) {
			return false;
		}
		while (iterator.hasNext()) {
			if (iterator.next() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断可迭代对象是否全部为 {@code null}（空集合返回 {@code false}）。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 非空且全部为 {@code null} 返回 {@code true}
	 */
	public static boolean isAllNull(Iterable<?> iterable) {
		return isAllNull(getIter(iterable));
	}

	/**
	 * 判断迭代器是否全部为 {@code null}（空迭代器返回 {@code false}）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @return 非空且全部为 {@code null} 返回 {@code true}
	 */
	public static boolean isAllNull(Iterator<?> iterator) {
		if (iterator == null || !iterator.hasNext()) {
			return false;
		}
		while (iterator.hasNext()) {
			if (iterator.next() != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 统计可迭代对象元素个数。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @return 元素个数
	 */
	public static int size(Iterable<?> iterable) {
		if (iterable == null) {
			return 0;
		}
		if (iterable instanceof Collection<?> collection) {
			return collection.size();
		}
		return size(iterable.iterator());
	}

	/**
	 * 统计迭代器元素个数（消耗迭代器）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @return 元素个数
	 */
	public static int size(Iterator<?> iterator) {
		if (iterator == null) {
			return 0;
		}
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}

	/**
	 * 将可迭代对象转为 List。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @param <T>      元素类型
	 * @return List；{@code null} 返回空列表
	 */
	public static <T> List<T> toList(Iterable<T> iterable) {
		return toList(getIter(iterable));
	}

	/**
	 * 将迭代器转为 List（消耗迭代器）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @param <T>      元素类型
	 * @return List；{@code null} 返回空列表
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
	 * 将 {@link Enumeration} 转为迭代器。
	 *
	 * @param enumeration 枚举对象，可为 {@code null}
	 * @param <E>         元素类型
	 * @return 迭代器；{@code null} 返回空迭代器
	 */
	public static <E> Iterator<E> asIterator(Enumeration<E> enumeration) {
		if (enumeration == null) {
			return empty();
		}
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return enumeration.hasMoreElements();
			}

			@Override
			public E next() {
				return enumeration.nextElement();
			}
		};
	}

	/**
	 * 获取可迭代对象首元素。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @param <T>      元素类型
	 * @return 首元素；无元素返回 {@code null}
	 */
	public static <T> T getFirst(Iterable<T> iterable) {
		return getFirst(getIter(iterable));
	}

	/**
	 * 获取迭代器首元素（消耗一个元素）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @param <T>      元素类型
	 * @return 首元素；无元素返回 {@code null}
	 */
	public static <T> T getFirst(Iterator<T> iterator) {
		return iterator == null || !iterator.hasNext() ? null : iterator.next();
	}

	/**
	 * 获取可迭代对象首个非 {@code null} 元素。
	 *
	 * @param iterable 可迭代对象，可为 {@code null}
	 * @param <T>      元素类型
	 * @return 首个非 {@code null} 元素；无则返回 {@code null}
	 */
	public static <T> T getFirstNoneNull(Iterable<T> iterable) {
		return getFirstNoneNull(getIter(iterable));
	}

	/**
	 * 获取迭代器首个非 {@code null} 元素（消耗迭代器直至命中或结束）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @param <T>      元素类型
	 * @return 首个非 {@code null} 元素；无则返回 {@code null}
	 */
	public static <T> T getFirstNoneNull(Iterator<T> iterator) {
		if (iterator == null) {
			return null;
		}
		while (iterator.hasNext()) {
			T value = iterator.next();
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * 过滤可迭代对象，返回新 List。
	 *
	 * @param iterable  可迭代对象，可为 {@code null}
	 * @param predicate 过滤条件，不可为 {@code null}
	 * @param <T>       元素类型
	 * @return 过滤结果列表
	 */
	public static <T> List<T> filter(Iterable<T> iterable, Predicate<T> predicate) {
		return filterToList(getIter(iterable), predicate);
	}

	/**
	 * 过滤迭代器，返回新 List（消耗迭代器）。
	 *
	 * @param iterator  迭代器，可为 {@code null}
	 * @param predicate 过滤条件，不可为 {@code null}
	 * @param <T>       元素类型
	 * @return 过滤结果列表
	 */
	public static <T> List<T> filterToList(Iterator<T> iterator, Predicate<T> predicate) {
		Objects.requireNonNull(predicate, "predicate");
		List<T> result = new ArrayList<>();
		if (iterator != null) {
			while (iterator.hasNext()) {
				T value = iterator.next();
				if (predicate.test(value)) {
					result.add(value);
				}
			}
		}
		return result;
	}

	/**
	 * 转换迭代器元素类型（惰性求值）。
	 *
	 * @param iterator 源迭代器，可为 {@code null}
	 * @param function 转换函数，不可为 {@code null}
	 * @param <F>      源元素类型
	 * @param <T>      目标元素类型
	 * @return 转换后的迭代器；源为 {@code null} 时返回空迭代器
	 */
	public static <F, T> Iterator<T> trans(Iterator<F> iterator, Function<? super F, ? extends T> function) {
		Objects.requireNonNull(function, "function");
		if (iterator == null) {
			return empty();
		}
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return function.apply(iterator.next());
			}
		};
	}

	/**
	 * 使用分隔符拼接迭代器元素（{@code null} 元素输出 {@code "null"}）。
	 *
	 * @param iterator  迭代器，可为 {@code null}
	 * @param separator 分隔符，{@code null} 视为空串
	 * @param <T>       元素类型
	 * @return 拼接结果；无元素返回空串
	 */
	public static <T> String join(Iterator<T> iterator, CharSequence separator) {
		if (iterator == null || !iterator.hasNext()) {
			return "";
		}
		String delimiter = separator == null ? "" : separator.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(iterator.next());
		while (iterator.hasNext()) {
			sb.append(delimiter).append(iterator.next());
		}
		return sb.toString();
	}

	/**
	 * 遍历迭代器并执行消费操作（消耗迭代器）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 * @param consumer 消费操作，不可为 {@code null}
	 * @param <T>      元素类型
	 */
	public static <T> void forEach(Iterator<T> iterator, Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "consumer");
		if (iterator == null) {
			return;
		}
		while (iterator.hasNext()) {
			consumer.accept(iterator.next());
		}
	}

	/**
	 * 清空迭代器（消耗全部元素）。
	 *
	 * @param iterator 迭代器，可为 {@code null}
	 */
	public static void clear(Iterator<?> iterator) {
		if (iterator == null) {
			return;
		}
		while (iterator.hasNext()) {
			iterator.next();
		}
	}
}
