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
package com.sure.tool.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 链式集合构建与变换（Fluent API 全面化第二项）。
 *
 * <p>与 {@code CollUtil} 静态方法形态并存：支持「构建 → 变换 → 终端」一条链完成。
 * 中间操作返回新的 {@code FluentList}（内部集合不可变，原集合不受影响）；
 * 终端 {@link #toList()} 等返回不可变结果（对标 Guava ImmutableList 理念，零依赖实现）。
 * 元素不允许为 null：任何途径引入 null 元素都会抛 {@link IllegalArgumentException}。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * List<String> top = Fluent.of("c", "a", "b", "a")
 *         .distinct().sorted().limit(2).toList();
 * // ["a", "b"]，不可变
 * }</pre>
 *
 * @param <E> 元素类型
 * @since 1.19.0
 */
public final class FluentList<E> {

	/** 内部不可变元素列表（非 null 元素）。 */
	private final List<E> items;

	FluentList(List<E> items) {
		this.items = items;
	}

	/**
	 * 创建空列表管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<String> f = Fluent.list();
	 * }</pre>
	 *
	 * @param <E> 元素类型
	 * @return 空管道实例
	 */
	public static <E> FluentList<E> of() {
		return new FluentList<>(List.of());
	}

	/**
	 * 以指定元素创建列表管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<Integer> f = Fluent.of(3, 1, 2);
	 * }</pre>
	 *
	 * @param items 初始元素，不能含 null
	 * @param <E>   元素类型
	 * @return 管道实例（内部不可变）
	 * @throws IllegalArgumentException 任一元素为 null
	 */
	@SafeVarargs
	public static <E> FluentList<E> of(E... items) {
		Objects.requireNonNull(items, "items must not be null");
		for (E e : items) {
			if (e == null) {
				throw new IllegalArgumentException("list element must not be null");
			}
		}
		return new FluentList<>(List.of(items));
	}

	/**
	 * 从既有集合创建列表管道（防御拷贝，原集合修改不影响管道）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentList<String> f = Fluent.of(existingList);
	 * }</pre>
	 *
	 * @param source 源集合，不能为 null，不能含 null 元素
	 * @param <E>    元素类型
	 * @return 管道实例（内部不可变副本）
	 * @throws NullPointerException      source 为 null
	 * @throws IllegalArgumentException source 含 null 元素
	 */
	public static <E> FluentList<E> from(Collection<? extends E> source) {
		Objects.requireNonNull(source, "source must not be null");
		for (E e : source) {
			if (e == null) {
				throw new IllegalArgumentException("list element must not be null");
			}
		}
		return new FluentList<>(List.copyOf(source));
	}

	/**
	 * 追加一个元素。
	 *
	 * @param item 新元素，不能为 null
	 * @return 新的管道实例
	 * @throws IllegalArgumentException item 为 null
	 */
	public FluentList<E> add(E item) {
		if (item == null) {
			throw new IllegalArgumentException("list element must not be null");
		}
		List<E> next = new ArrayList<>(items.size() + 1);
		next.addAll(items);
		next.add(item);
		return new FluentList<>(List.copyOf(next));
	}

	/**
	 * 追加一批元素。
	 *
	 * @param extra 新元素集合，不能为 null，不能含 null 元素
	 * @return 新的管道实例
	 * @throws NullPointerException      extra 为 null
	 * @throws IllegalArgumentException extra 含 null 元素
	 */
	public FluentList<E> addAll(Collection<? extends E> extra) {
		Objects.requireNonNull(extra, "extra must not be null");
		List<E> next = new ArrayList<>(items.size() + extra.size());
		next.addAll(items);
		for (E e : extra) {
			if (e == null) {
				throw new IllegalArgumentException("list element must not be null");
			}
			next.add(e);
		}
		return new FluentList<>(List.copyOf(next));
	}

	/**
	 * 元素映射（类型可变化）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<Integer> lens = Fluent.of("a", "abc").map(String::length).toList();
	 * // [1, 3]
	 * }</pre>
	 *
	 * @param mapper 映射函数，不能为 null；结果不能为 null
	 * @param <E2>   目标元素类型
	 * @return 新的管道实例
	 * @throws NullPointerException      mapper 为 null
	 * @throws IllegalArgumentException 映射结果含 null
	 */
	public <E2> FluentList<E2> map(Function<? super E, ? extends E2> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		List<E2> mapped = new ArrayList<>(items.size());
		for (E e : items) {
			E2 r = mapper.apply(e);
			if (r == null) {
				throw new IllegalArgumentException("map result must not be null");
			}
			mapped.add(r);
		}
		return new FluentList<>(List.copyOf(mapped));
	}

	/**
	 * 按条件过滤。
	 *
	 * @param predicate 过滤条件，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException predicate 为 null
	 */
	public FluentList<E> filter(Predicate<? super E> predicate) {
		Objects.requireNonNull(predicate, "predicate must not be null");
		return new FluentList<>(items.stream().filter(predicate).toList());
	}

	/**
	 * 去重（保持首次出现顺序）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<String> d = Fluent.of("a", "b", "a").distinct().toList(); // ["a", "b"]
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentList<E> distinct() {
		return new FluentList<>(items.stream().distinct().toList());
	}

	/**
	 * 自然顺序排序（元素需实现 {@link Comparable}，否则 ClassCastException）。
	 *
	 * @return 新的管道实例
	 */
	public FluentList<E> sorted() {
		return new FluentList<>(items.stream().sorted().toList());
	}

	/**
	 * 按指定比较器排序。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<String> s = Fluent.of("b", "a").sorted(Comparator.reverseOrder()).toList();
	 * // ["b", "a"]
	 * }</pre>
	 *
	 * @param comparator 比较器，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException comparator 为 null
	 */
	public FluentList<E> sorted(Comparator<? super E> comparator) {
		Objects.requireNonNull(comparator, "comparator must not be null");
		return new FluentList<>(items.stream().sorted(comparator).toList());
	}

	/**
	 * 限制为前 maxSize 个元素。
	 *
	 * @param maxSize 最大数量，负数抛 IAE；超过原长返回全量
	 * @return 新的管道实例
	 * @throws IllegalArgumentException maxSize &lt; 0
	 */
	public FluentList<E> limit(int maxSize) {
		if (maxSize < 0) {
			throw new IllegalArgumentException("maxSize must not be negative: " + maxSize);
		}
		return new FluentList<>(items.stream().limit(maxSize).toList());
	}

	/**
	 * 跳过前 n 个元素。
	 *
	 * @param n 跳过的数量，负数抛 IAE；超过原长返回空
	 * @return 新的管道实例
	 * @throws IllegalArgumentException n &lt; 0
	 */
	public FluentList<E> skip(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n must not be negative: " + n);
		}
		return new FluentList<>(items.stream().skip(n).toList());
	}

	/**
	 * 反转顺序。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<String> r = Fluent.of("a", "b").reverse().toList(); // ["b", "a"]
	 * }</pre>
	 *
	 * @return 新的管道实例
	 */
	public FluentList<E> reverse() {
		List<E> next = new ArrayList<>(items);
		Collections.reverse(next);
		return new FluentList<>(List.copyOf(next));
	}

	/**
	 * 终端：返回不可变列表。
	 *
	 * <p>返回结果不可修改（{@link UnsupportedOperationException}），
	 * 且为独立副本，与管道内部互不影响。</p>
	 *
	 * @return 不可变 List
	 */
	public List<E> toList() {
		return List.copyOf(items);
	}

	/**
	 * 终端：返回独立副本数组。
	 *
	 * <p>与 JDK {@link java.util.stream.Stream#toArray()} 语义一致返回 {@code Object[]}
	 * （泛型擦除限制）。需要强类型数组时使用
	 * {@code pipe.toList().toArray(new String[0])}。</p>
	 *
	 * @return 元素数组副本（Object[]）
	 */
	public Object[] toArray() {
		return items.toArray();
	}

	/**
	 * 终端：元素个数。
	 *
	 * @return 个数
	 */
	public int count() {
		return items.size();
	}

	/**
	 * 终端：是否为空。
	 *
	 * @return 空返回 true
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * 终端：是否包含指定元素（null 返回 false）。
	 *
	 * @param o 目标元素，可为 null（恒返回 false）
	 * @return 包含返回 true
	 */
	public boolean contains(Object o) {
		if (o == null) {
			return false;
		}
		return items.contains(o);
	}

	/**
	 * 终端：按索引取元素（越界抛 IndexOutOfBoundsException）。
	 *
	 * @param index 索引
	 * @return 该位置元素
	 */
	public E get(int index) {
		return items.get(index);
	}

	/**
	 * 终端：以分隔符拼接。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * String s = Fluent.of("a", "b", "c").join("-"); // "a-b-c"
	 * }</pre>
	 *
	 * @param delimiter 分隔符，不能为 null
	 * @return 拼接串（空列表返回空串）
	 * @throws NullPointerException delimiter 为 null
	 */
	public String join(String delimiter) {
		Objects.requireNonNull(delimiter, "delimiter must not be null");
		StringJoiner joiner = new StringJoiner(delimiter);
		for (E e : items) {
			joiner.add(String.valueOf(e));
		}
		return joiner.toString();
	}

	@Override
	public String toString() {
		return items.toString();
	}
}
