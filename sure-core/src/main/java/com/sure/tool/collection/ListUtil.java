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

/**
 * 列表工具类，参考 Hutool 的 {@code ListUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ListUtil {

	private ListUtil() {
	}

	/**
	 * 创建列表。
	 *
	 * @param values 元素
	 * @param <T>    元素类型
	 * @return 列表
	 */
	@SafeVarargs
	public static <T> List<T> toList(T... values) {
		return CollUtil.newArrayList(values);
	}

	/**
	 * 列表切分：按固定大小切分为多个子列表。
	 *
	 * @param list 列表
	 * @param size 每份大小
	 * @param <T>  元素类型
	 * @return 切分后的子列表集合
	 */
	public static <T> List<List<T>> partition(List<T> list, int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("每份大小必须大于 0: " + size);
		}
		List<List<T>> result = new ArrayList<>();
		if (CollUtil.isEmpty(list)) {
			return result;
		}
		int total = list.size();
		for (int from = 0; from < total; from += size) {
			int to = Math.min(from + size, total);
			result.add(new ArrayList<>(list.subList(from, to)));
		}
		return result;
	}

	/**
	 * 分页截取：返回第 pageIndex 页（从 0 开始）的 pageSize 条数据。
	 *
	 * @param pageIndex 页码（从 0 开始）
	 * @param pageSize  每页条数
	 * @param list      数据列表
	 * @param <T>       元素类型
	 * @return 当页数据
	 */
	public static <T> List<T> page(int pageIndex, int pageSize, List<T> list) {
		if (pageIndex < 0) {
			pageIndex = 0;
		}
		if (pageSize <= 0 || CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		int from = pageIndex * pageSize;
		if (from >= list.size()) {
			return new ArrayList<>();
		}
		int to = Math.min(from + pageSize, list.size());
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * 截取子列表。
	 *
	 * @param list      列表
	 * @param fromIndex 起始索引（含）
	 * @param toIndex   结束索引（不含）
	 * @param <T>       元素类型
	 * @return 子列表
	 */
	public static <T> List<T> sub(List<T> list, int fromIndex, int toIndex) {
		return CollUtil.sub(list, fromIndex, toIndex);
	}

	/**
	 * 反转列表（原地）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 反转后的列表
	 */
	public static <T> List<T> reverse(List<T> list) {
		return CollUtil.reverse(list);
	}

	/**
	 * 反转列表（返回新列表，不影响原列表）。
	 *
	 * @param list 列表
	 * @param <T>  元素类型
	 * @return 反转后的新列表
	 */
	public static <T> List<T> reverseNew(List<T> list) {
		if (CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		List<T> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return copy;
	}

	/**
	 * 列表是否为空。
	 *
	 * @param list 列表
	 * @return 是否为空
	 */
	public static boolean isEmpty(List<?> list) {
		return CollUtil.isEmpty(list);
	}

	/**
	 * 列表是否非空。
	 *
	 * @param list 列表
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(List<?> list) {
		return CollUtil.isNotEmpty(list);
	}
}