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
package com.sure.tool.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 权重随机：按权重概率随机取一个对象，零依赖。
 * <p>
 * 权重须为正数；总权重决定各元素命中概率 = 元素权重 / 总权重。
 *
 * @author suretool
 * @since 0.2.0
 */
public class WeightRandom<T> {

	/** 权重项。 */
	public record WeightObj<T>(T item, double weight) {
	}

	private final List<WeightObj<T>> items = new ArrayList<>();
	private final Random random;
	private double totalWeight;

	/**
	 * 构造（使用默认随机源）。
	 */
	public WeightRandom() {
		this(new Random());
	}

	/**
	 * 构造。
	 *
	 * @param random 随机源
	 */
	public WeightRandom(Random random) {
		this.random = random == null ? new Random() : random;
	}

	/**
	 * 添加权重项。
	 *
	 * @param item   元素
	 * @param weight 权重（&gt;0）
	 */
	public synchronized void add(T item, double weight) {
		if (weight <= 0) {
			throw new IllegalArgumentException("权重必须大于 0: " + weight);
		}
		items.add(new WeightObj<>(item, weight));
		totalWeight += weight;
	}

	/**
	 * 清空。
	 */
	public synchronized void clear() {
		items.clear();
		totalWeight = 0;
	}

	/**
	 * 当前项数量。
	 *
	 * @return 数量
	 */
	public synchronized int size() {
		return items.size();
	}

	/**
	 * 按权重随机取一个元素；无元素返回 null。
	 *
	 * @return 元素
	 */
	public synchronized T next() {
		if (items.isEmpty()) {
			return null;
		}
		double r = random.nextDouble() * totalWeight;
		double cumulative = 0;
		for (WeightObj<T> obj : items) {
			cumulative += obj.weight();
			if (r < cumulative) {
				return obj.item();
			}
		}
		return items.get(items.size() - 1).item();
	}

	/**
	 * 获取全部权重项（副本）。
	 *
	 * @return 权重项列表
	 */
	public synchronized List<WeightObj<T>> items() {
		return new ArrayList<>(items);
	}
}
