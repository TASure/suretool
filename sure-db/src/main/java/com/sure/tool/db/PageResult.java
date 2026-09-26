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
package com.sure.tool.db;

import java.util.List;
import java.util.Objects;

/**
 * 分页查询结果。
 *
 * @author suretool
 * @since 1.2.0
 */
public class PageResult {

	/** 页码，从 1 开始 */
	private final int pageNo;

	/** 每页条数 */
	private final int pageSize;

	/** 总条数 */
	private final long total;

	/** 当前页数据 */
	private final List<Entity> list;

	/**
	 * 构造分页结果。
	 *
	 * @param pageNo   页码
	 * @param pageSize 每页条数
	 * @param total    总条数
	 * @param list     当前页数据
	 * @return 分页结果
	 */
	public static PageResult of(int pageNo, int pageSize, long total, List<Entity> list) {
		return new PageResult(pageNo, pageSize, total, list);
	}

	/**
	 * 构造分页结果。
	 *
	 * @param pageNo   页码
	 * @param pageSize 每页条数
	 * @param total    总条数
	 * @param list     当前页数据
	 */
	public PageResult(int pageNo, int pageSize, long total, List<Entity> list) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.total = total;
		// 防御性拷贝：存储不可变视图，避免外部修改影响分页结果
		this.list = java.util.Collections.unmodifiableList(
				java.util.List.copyOf(Objects.requireNonNull(list, "list 不能为 null")));
	}

	/**
	 * 获取页码。
	 *
	 * @return 页码
	 */
	public int getPageNo() {
		return pageNo;
	}

	/**
	 * 获取每页条数。
	 *
	 * @return 每页条数
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 获取总条数。
	 *
	 * @return 总条数
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * 获取当前页数据。
	 *
	 * @return 实体列表
	 */
	public List<Entity> getList() {
		return list;
	}

	/**
	 * 计算总页数。
	 *
	 * @return 总页数，total 为 0 时返回 0
	 */
	public int getPageCount() {
		if (total == 0) {
			return 0;
		}
		return (int) Math.ceil((double) total / pageSize);
	}
}
