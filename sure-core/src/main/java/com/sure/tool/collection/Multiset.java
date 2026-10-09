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

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 计数集合（Multiset）：记录每个元素出现的次数，对标 Guava {@code Multiset} 与
 * commons-collections4 {@code Bag} 的常用子集，零依赖。
 *
 * <p>典型场景：词频统计、元素频次统计、投票计数。{@code size()} 返回含重复的总数，
 * {@code uniqueSize()} 返回去重元素数。</p>
 *
 * <p><b>线程安全</b>：本类非线程安全；并发场景需外部同步。</p>
 *
 * @param <T> 元素类型
 * @since 1.10.0
 */
public class Multiset<T> {

	/**
	 * 元素与计数条目。
	 *
	 * @param <T> 元素类型
	 */
	public static final class Entry<T> {
		private final T element;
		private final int count;

		Entry(T element, int count) {
			this.element = element;
			this.count = count;
		}

		/**
		 * 元素。
		 *
		 * @return 元素
		 */
		public T getElement() {
			return element;
		}

		/**
		 * 该元素出现次数。
		 *
		 * @return 次数
		 */
		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return element + " x" + count;
		}
	}

	private final Map<T, Integer> counts = new HashMap<>();
	private int total;

	/**
	 * 添加一个元素，计数 +1。
	 *
	 * @param element 元素（可为 null）
	 * @return 本对象，支持链式调用
	 */
	public Multiset<T> add(T element) {
		return add(element, 1);
	}

	/**
	 * 批量添加元素，计数 +{@code occurrences}。
	 *
	 * @param element      元素
	 * @param occurrences  次数（必须为正数）
	 * @return 本对象，支持链式调用
	 * @throws IllegalArgumentException occurrences 非正
	 */
	public Multiset<T> add(T element, int occurrences) {
		if (occurrences <= 0) {
			throw new IllegalArgumentException("occurrences must be positive: " + occurrences);
		}
		counts.merge(element, occurrences, Integer::sum);
		total += occurrences;
		return this;
	}

	/**
	 * 移除一个元素，计数 -1（计数归零时删除元素）。
	 *
	 * @param element 元素
	 * @return 是否发生移除
	 */
	public boolean remove(T element) {
		return remove(element, 1) > 0;
	}

	/**
	 * 批量移除元素。
	 *
	 * @param element     元素
	 * @param occurrences 次数
	 * @return 实际移除次数（受已有计数限制）
	 */
	public int remove(T element, int occurrences) {
		Integer current = counts.get(element);
		if (current == null || occurrences <= 0) {
			return 0;
		}
		int removed = Math.min(current, occurrences);
		int left = current - removed;
		if (left == 0) {
			counts.remove(element);
		} else {
			counts.put(element, left);
		}
		total -= removed;
		return removed;
	}

	/**
	 * 设置元素计数（0 表示移除该元素）。
	 *
	 * @param element 元素
	 * @param count   新计数（≥0）
	 * @return 本对象，支持链式调用
	 * @throws IllegalArgumentException count 为负
	 */
	public Multiset<T> setCount(T element, int count) {
		if (count < 0) {
			throw new IllegalArgumentException("count must be >= 0: " + count);
		}
		Integer old = counts.remove(element);
		int oldCount = old == null ? 0 : old;
		if (count > 0) {
			counts.put(element, count);
		}
		total += count - oldCount;
		return this;
	}

	/**
	 * 指定元素出现次数。
	 *
	 * @param element 元素
	 * @return 次数（不存在为 0）
	 */
	public int count(T element) {
		Integer c = counts.get(element);
		return c == null ? 0 : c;
	}

	/**
	 * 去重元素集合（视图）。
	 *
	 * @return 元素集合
	 */
	public Set<T> elementSet() {
		return counts.keySet();
	}

	/**
	 * 元素与计数条目集合。
	 *
	 * @return 条目集合
	 */
	public Set<Entry<T>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public Iterator<Entry<T>> iterator() {
				return counts.entrySet().stream()
						.map(e -> new Entry<>(e.getKey(), e.getValue()))
						.iterator();
			}

			@Override
			public int size() {
				return counts.size();
			}
		};
	}

	/**
	 * 含重复计数的总元素数。
	 *
	 * @return 总数
	 */
	public int size() {
		return total;
	}

	/**
	 * 去重元素数。
	 *
	 * @return 去重数
	 */
	public int uniqueSize() {
		return counts.size();
	}

	/**
	 * 迭代器：按计数展开元素（如 a x2、b x1 → a、a、b）。
	 *
	 * @return 展开迭代器
	 */
	public Iterator<T> iterator() {
		return new Iterator<>() {
			private final Iterator<Map.Entry<T, Integer>> it = counts.entrySet().iterator();
			private Map.Entry<T, Integer> current;
			private int remaining;

			@Override
			public boolean hasNext() {
				return remaining > 0 || it.hasNext();
			}

			@Override
			public T next() {
				if (remaining <= 0) {
					if (!it.hasNext()) {
						throw new NoSuchElementException();
					}
					current = it.next();
					remaining = current.getValue();
				}
				remaining--;
				return current.getKey();
			}
		};
	}

	/**
	 * 是否为空。
	 *
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return total == 0;
	}

	/**
	 * 清空所有元素与计数。
	 */
	public void clear() {
		counts.clear();
		total = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Entry<T> e : entrySet()) {
			sb.append(e.getElement()).append(" x").append(e.getCount()).append(", ");
		}
		if (sb.length() > 1) {
			sb.setLength(sb.length() - 2);
		}
		return sb.append(']').toString();
	}
}
