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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 有界优先队列：容量固定，超出容量时自动淘汰「最不重要」的元素（依据比较器），零依赖。
 * <p>
 * 内部使用小顶堆：比较器判定为"更大/更优"的元素保留，队满时优先淘汰最小元素。
 *
 * @param <E> 元素类型
 * @author suretool
 * @since 0.2.0
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {

	private static final long serialVersionUID = 1L;

	private final int capacity;
	private final Comparator<? super E> comparator;

	/**
	 * 构造（自然序，元素须实现 Comparable）。
	 *
	 * @param capacity 容量（&gt;0）
	 */
	public BoundedPriorityQueue(int capacity) {
		this(capacity, null);
	}

	/**
	 * 构造。
	 *
	 * @param capacity   容量（&gt;0）
	 * @param comparator 比较器（null 表示自然序）
	 */
	public BoundedPriorityQueue(int capacity, Comparator<? super E> comparator) {
		super(capacity, comparator == null ? naturalOrdering() : comparator);
		this.capacity = capacity;
		this.comparator = comparator;
	}

	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> naturalOrdering() {
		return (Comparator<? super E>) Comparator.naturalOrder();
	}

	/**
	 * 入队；队满时若新元素比队首更优则替换队首。
	 *
	 * @param e 元素
	 * @return 是否入队成功
	 */
	@Override
	public boolean offer(E e) {
		if (e == null) {
			throw new NullPointerException("元素不能为 null");
		}
		if (size() >= capacity) {
			E head = peek();
			if (head != null && compare(e, head) <= 0) {
				return false; // 新元素不比队首优，拒绝
			}
			poll(); // 淘汰队首
		}
		return super.offer(e);
	}

	private int compare(E a, E b) {
		if (comparator != null) {
			return comparator.compare(a, b);
		}
		@SuppressWarnings("unchecked")
		Comparable<? super E> ca = (Comparable<? super E>) a;
		return ca.compareTo(b);
	}

	/**
	 * 容量。
	 *
	 * @return 容量
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * 转为列表（无序）。
	 *
	 * @return 列表
	 */
	public List<E> toList() {
		return new ArrayList<>(this);
	}

	/**
	 * 批量加入并返回自身。
	 *
	 * @param c 集合
	 * @return 自身
	 */
	public BoundedPriorityQueue<E> addAllAndReturn(Collection<? extends E> c) {
		addAll(c);
		return this;
	}
}
