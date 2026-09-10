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
 * 通用树节点，参考 Hutool 的 {@code TreeNode} 设计。
 *
 * @param <T> ID 类型
 * @author suretool
 * @since 0.1.0
 */
public class TreeNode<T> {

	private T id;
	private T parentId;
	private String name;
	private List<TreeNode<T>> children = new ArrayList<>();

	/**
	 * 创建树节点。
	 *
	 * @param id       节点 ID
	 * @param parentId 父节点 ID（根节点可传 {@code null}）
	 * @param name     节点名称
	 */
	public TreeNode(T id, T parentId, String name) {
		this.id = id;
		this.parentId = parentId;
		this.name = name;
	}

	/**
	 * 节点 ID。
	 *
	 * @return ID
	 */
	public T getId() {
		return id;
	}

	/**
	 * 设置 ID。
	 *
	 * @param id ID
	 * @return 当前节点
	 */
	public TreeNode<T> setId(T id) {
		this.id = id;
		return this;
	}

	/**
	 * 父节点 ID。
	 *
	 * @return 父节点 ID
	 */
	public T getParentId() {
		return parentId;
	}

	/**
	 * 设置父节点 ID。
	 *
	 * @param parentId 父节点 ID
	 * @return 当前节点
	 */
	public TreeNode<T> setParentId(T parentId) {
		this.parentId = parentId;
		return this;
	}

	/**
	 * 节点名称。
	 *
	 * @return 名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置名称。
	 *
	 * @param name 名称
	 * @return 当前节点
	 */
	public TreeNode<T> setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * 子节点列表。
	 *
	 * @return 子节点列表
	 */
	public List<TreeNode<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * 添加子节点。
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public TreeNode<T> addChild(TreeNode<T> child) {
		children.add(child);
		child.setParentId(id);
		return this;
	}

	/**
	 * 是否有子节点。
	 *
	 * @return 是否有子节点
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public String toString() {
		return "TreeNode{id=" + id + ", parentId=" + parentId + ", name='" + name + "'}";
	}
}