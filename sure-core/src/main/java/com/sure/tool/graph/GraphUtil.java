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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * 有向图算法工具：拓扑排序 / 环检测 / DFS / BFS / 可达性 / 无权最短路径。
 *
 * @since 1.11.0
 */
public final class GraphUtil {

	private GraphUtil() {
	}

	/**
	 * Kahn 拓扑排序。
	 *
	 * @param graph 有向图
	 * @param <V>   顶点类型
	 * @return 拓扑序；图有环返回 {@link Optional#empty()}
	 */
	public static <V> Optional<List<V>> topologicalSort(DiGraph<V> graph) {
		Objects.requireNonNull(graph, "graph must not be null");
		Map<V, Integer> inDegree = new HashMap<>();
		for (V vertex : graph.vertices()) {
			inDegree.put(vertex, graph.inDegree(vertex));
		}
		Queue<V> queue = new ArrayDeque<>();
		inDegree.forEach((v, d) -> {
			if (d == 0) {
				queue.add(v);
			}
		});
		List<V> result = new ArrayList<>(graph.vertexCount());
		while (!queue.isEmpty()) {
			V current = queue.poll();
			result.add(current);
			for (V next : graph.successors(current)) {
				int d = inDegree.get(next) - 1;
				inDegree.put(next, d);
				if (d == 0) {
					queue.add(next);
				}
			}
		}
		return result.size() == graph.vertexCount() ? Optional.of(result) : Optional.empty();
	}

	/**
	 * 环检测（Kahn 计数法：能完成全量拓扑排序则无环）。
	 *
	 * @param graph 有向图
	 * @param <V>   顶点类型
	 * @return true 表示存在环
	 */
	public static <V> boolean hasCycle(DiGraph<V> graph) {
		return topologicalSort(graph).isEmpty();
	}

	/**
	 * 深度优先遍历（从起点开始，按邻接表顺序）。
	 *
	 * @param graph 有向图
	 * @param start 起点（不存在时返回空列表）
	 * @param <V>   顶点类型
	 * @return 访问顺序
	 */
	public static <V> List<V> dfs(DiGraph<V> graph, V start) {
		Objects.requireNonNull(graph, "graph must not be null");
		List<V> result = new ArrayList<>();
		if (!graph.containsVertex(start)) {
			return result;
		}
		Set<V> visited = new HashSet<>();
		java.util.Deque<V> stack = new ArrayDeque<>();
		stack.push(start);
		while (!stack.isEmpty()) {
			V current = stack.pop();
			if (!visited.add(current)) {
				continue;
			}
			result.add(current);
			// 逆序入栈保证按邻接表顺序访问
			List<V> succ = new ArrayList<>(graph.successors(current));
			Collections.reverse(succ);
			for (V next : succ) {
				if (!visited.contains(next)) {
					stack.push(next);
				}
			}
		}
		return result;
	}

	/**
	 * 广度优先遍历（从起点开始，按邻接表顺序）。
	 *
	 * @param graph 有向图
	 * @param start 起点（不存在时返回空列表）
	 * @param <V>   顶点类型
	 * @return 访问顺序
	 */
	public static <V> List<V> bfs(DiGraph<V> graph, V start) {
		Objects.requireNonNull(graph, "graph must not be null");
		List<V> result = new ArrayList<>();
		if (!graph.containsVertex(start)) {
			return result;
		}
		Set<V> visited = new HashSet<>();
		Queue<V> queue = new ArrayDeque<>();
		queue.add(start);
		visited.add(start);
		while (!queue.isEmpty()) {
			V current = queue.poll();
			result.add(current);
			for (V next : graph.successors(current)) {
				if (visited.add(next)) {
					queue.add(next);
				}
			}
		}
		return result;
	}

	/**
	 * 可达性判断（BFS，源可到目标则 true）。
	 *
	 * @param graph 有向图
	 * @param from  源顶点
	 * @param to    目标顶点
	 * @param <V>   顶点类型
	 * @return true 表示可达（from == to 且存在顶点时视为可达）
	 */
	public static <V> boolean isReachable(DiGraph<V> graph, V from, V to) {
		Objects.requireNonNull(graph, "graph must not be null");
		if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
			return false;
		}
		if (from.equals(to)) {
			return true;
		}
		Set<V> visited = new HashSet<>();
		Queue<V> queue = new ArrayDeque<>();
		queue.add(from);
		visited.add(from);
		while (!queue.isEmpty()) {
			V current = queue.poll();
			for (V next : graph.successors(current)) {
				if (next.equals(to)) {
					return true;
				}
				if (visited.add(next)) {
					queue.add(next);
				}
			}
		}
		return false;
	}

	/**
	 * 无权图最短路径（BFS），返回路径顶点序列（含起点与终点）。
	 *
	 * @param graph 有向图
	 * @param from  源顶点
	 * @param to    目标顶点
	 * @param <V>   顶点类型
	 * @return 最短路径；不可达或顶点缺失返回 null
	 */
	public static <V> List<V> shortestPath(DiGraph<V> graph, V from, V to) {
		Objects.requireNonNull(graph, "graph must not be null");
		if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
			return null;
		}
		if (from.equals(to)) {
			return List.of(from);
		}
		Map<V, V> predecessor = new HashMap<>();
		Queue<V> queue = new ArrayDeque<>();
		Set<V> visited = new HashSet<>();
		queue.add(from);
		visited.add(from);
		while (!queue.isEmpty()) {
			V current = queue.poll();
			for (V next : graph.successors(current)) {
				if (visited.add(next)) {
					predecessor.put(next, current);
					if (next.equals(to)) {
						return buildPath(predecessor, from, to);
					}
					queue.add(next);
				}
			}
		}
		return null;
	}

	private static <V> List<V> buildPath(Map<V, V> predecessor, V from, V to) {
		List<V> path = new ArrayList<>();
		V current = to;
		while (current != null) {
			path.add(current);
			current = predecessor.get(current);
		}
		Collections.reverse(path);
		// 防御：保证路径从 from 开始
		if (path.isEmpty() || !path.get(0).equals(from)) {
			path.add(0, from);
		}
		return path;
	}
}
