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
package com.sure.tool.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 简单有向图（邻接表实现），顶点去重基于 {@link Object#equals}/{@link Object#hashCode}。
 *
 * <p>非线程安全；顶点/边均为轻量操作，适合小规模图建模与算法演示。
 * 对标 Guava Graph 的核心子集。</p>
 *
 * @param <V> 顶点类型
 * @since 1.11.0
 */
public class DiGraph<V> {

	/**
	 * 有向边（源 → 目标）。
	 *
	 * @param <V> 顶点类型
	 * @since 1.11.0
	 */
	public record Edge<V>(V source, V target) {
	}

	private final Map<V, Set<V>> adjacency = new HashMap<>();

	/**
	 * 添加顶点。
	 *
	 * @param vertex 顶点
	 * @return 新顶点返回 true；已存在返回 false
	 */
	public boolean addVertex(V vertex) {
		Objects.requireNonNull(vertex, "vertex must not be null");
		if (adjacency.containsKey(vertex)) {
			return false;
		}
		adjacency.put(vertex, new HashSet<>());
		return true;
	}

	/**
	 * 添加顶点（已存在时忽略）。
	 *
	 * @param vertex 顶点
	 * @return 当前图（便于链式调用）
	 */
	public DiGraph<V> addVertexIfAbsent(V vertex) {
		addVertex(vertex);
		return this;
	}

	/**
	 * 添加有向边（源 → 目标），两端顶点自动补齐。
	 *
	 * @param source 源顶点
	 * @param target 目标顶点
	 * @return 新边返回 true；已存在返回 false
	 */
	public boolean addEdge(V source, V target) {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(target, "target must not be null");
		addVertexIfAbsent(source);
		addVertexIfAbsent(target);
		return adjacency.get(source).add(target);
	}

	/**
	 * 移除顶点及其全部关联边。
	 *
	 * @param vertex 顶点
	 * @return 存在并移除返回 true
	 */
	public boolean removeVertex(V vertex) {
		if (!adjacency.containsKey(vertex)) {
			return false;
		}
		adjacency.remove(vertex);
		adjacency.values().forEach(neighbors -> neighbors.remove(vertex));
		return true;
	}

	/**
	 * 移除有向边。
	 *
	 * @param source 源顶点
	 * @param target 目标顶点
	 * @return 存在并移除返回 true
	 */
	public boolean removeEdge(V source, V target) {
		Set<V> neighbors = adjacency.get(source);
		return neighbors != null && neighbors.remove(target);
	}

	/**
	 * 是否包含顶点。
	 *
	 * @param vertex 顶点
	 * @return true 表示存在
	 */
	public boolean containsVertex(V vertex) {
		return adjacency.containsKey(vertex);
	}

	/**
	 * 是否包含有向边（源 → 目标）。
	 *
	 * @param source 源顶点
	 * @param target 目标顶点
	 * @return true 表示存在
	 */
	public boolean containsEdge(V source, V target) {
		Set<V> neighbors = adjacency.get(source);
		return neighbors != null && neighbors.contains(target);
	}

	/**
	 * 顶点数量。
	 *
	 * @return 顶点数
	 */
	public int vertexCount() {
		return adjacency.size();
	}

	/**
	 * 边数量。
	 *
	 * @return 边数
	 */
	public int edgeCount() {
		return adjacency.values().stream().mapToInt(Set::size).sum();
	}

	/**
	 * 全部顶点。
	 *
	 * @return 顶点集合
	 */
	public Set<V> vertices() {
		return new HashSet<>(adjacency.keySet());
	}

	/**
	 * 全部有向边。
	 *
	 * @return 边列表
	 */
	public List<Edge<V>> edges() {
		List<Edge<V>> result = new ArrayList<>();
		adjacency.forEach((source, neighbors) -> {
			for (V target : neighbors) {
				result.add(new Edge<>(source, target));
			}
		});
		return result;
	}

	/**
	 * 后继顶点集合（出边目标）。
	 *
	 * @param vertex 顶点
	 * @return 后继集合；顶点不存在返回空集合
	 */
	public Set<V> successors(V vertex) {
		Set<V> neighbors = adjacency.get(vertex);
		return neighbors == null ? new HashSet<>() : new HashSet<>(neighbors);
	}

	/**
	 * 前驱顶点集合（入边来源）。
	 *
	 * @param vertex 顶点
	 * @return 前驱集合；顶点不存在返回空集合
	 */
	public Set<V> predecessors(V vertex) {
		Set<V> result = new HashSet<>();
		adjacency.forEach((source, neighbors) -> {
			if (neighbors.contains(vertex)) {
				result.add(source);
			}
		});
		return result;
	}

	/**
	 * 邻接顶点集合（后继 ∪ 前驱）。
	 *
	 * @param vertex 顶点
	 * @return 邻接集合
	 */
	public Set<V> adjacentNodes(V vertex) {
		Set<V> result = successors(vertex);
		result.addAll(predecessors(vertex));
		return result;
	}

	/**
	 * 出度。
	 *
	 * @param vertex 顶点
	 * @return 出度；顶点不存在返回 0
	 */
	public int outDegree(V vertex) {
		Set<V> neighbors = adjacency.get(vertex);
		return neighbors == null ? 0 : neighbors.size();
	}

	/**
	 * 入度。
	 *
	 * @param vertex 顶点
	 * @return 入度；顶点不存在返回 0
	 */
	public int inDegree(V vertex) {
		return predecessors(vertex).size();
	}

	/**
	 * 度数（出度 + 入度）。
	 *
	 * @param vertex 顶点
	 * @return 度数；顶点不存在返回 0
	 */
	public int degree(V vertex) {
		return outDegree(vertex) + inDegree(vertex);
	}

	/**
	 * 邻接表（只读视图）。
	 *
	 * @return 邻接映射
	 */
	Map<V, Set<V>> adjacency() {
		return adjacency;
	}
}
