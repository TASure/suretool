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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * GraphUtil 算法测试。
 */
public class GraphUtilTest {

	/**
	 * 拓扑排序：DAG 得到合法拓扑序。
	 */
	@Test
	public void testTopologicalSort() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "C");
		g.addEdge("A", "C");
		List<String> order = GraphUtil.topologicalSort(g).orElseThrow();
		// A 必在 B、C 前，B 在 C 前
		Assert.assertEquals(List.of("A", "B", "C"), order);
	}

	/**
	 * 拓扑排序：有环返回空。
	 */
	@Test
	public void testTopologicalSortCycle() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "A");
		Assert.assertTrue(GraphUtil.topologicalSort(g).isEmpty());
	}

	/**
	 * 环检测。
	 */
	@Test
	public void testHasCycle() {
		DiGraph<String> acyclic = new DiGraph<>();
		acyclic.addEdge("A", "B");
		acyclic.addEdge("B", "C");
		Assert.assertFalse(GraphUtil.hasCycle(acyclic));

		DiGraph<String> cyclic = new DiGraph<>();
		cyclic.addEdge("A", "B");
		cyclic.addEdge("B", "C");
		cyclic.addEdge("C", "A");
		Assert.assertTrue(GraphUtil.hasCycle(cyclic));
	}

	/**
	 * DFS 遍历顺序。
	 */
	@Test
	public void testDfs() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("A", "C");
		g.addEdge("B", "D");
		List<String> order = GraphUtil.dfs(g, "A");
		Assert.assertEquals(4, order.size());
		Assert.assertEquals("A", order.get(0));
		Assert.assertTrue(order.containsAll(List.of("B", "C", "D")));
		// 起点不存在返回空
		Assert.assertTrue(GraphUtil.dfs(g, "X").isEmpty());
	}

	/**
	 * BFS 遍历顺序。
	 */
	@Test
	public void testBfs() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("A", "C");
		g.addEdge("B", "D");
		List<String> order = GraphUtil.bfs(g, "A");
		Assert.assertEquals(List.of("A", "B", "C", "D"), order);
	}

	/**
	 * 可达性。
	 */
	@Test
	public void testIsReachable() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "C");
		Assert.assertTrue(GraphUtil.isReachable(g, "A", "C"));
		Assert.assertFalse(GraphUtil.isReachable(g, "C", "A"));
		Assert.assertTrue(GraphUtil.isReachable(g, "A", "A"));
		Assert.assertFalse(GraphUtil.isReachable(g, "A", "X"));
	}

	/**
	 * 无权最短路径。
	 */
	@Test
	public void testShortestPath() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "C");
		g.addEdge("A", "C");
		g.addEdge("C", "D");
		// 存在两条路径 A->B->C->D 与 A->C->D，最短为 A->C->D
		Assert.assertEquals(List.of("A", "C", "D"), GraphUtil.shortestPath(g, "A", "D"));
		// 不可达
		Assert.assertNull(GraphUtil.shortestPath(g, "D", "A"));
		// 同点
		Assert.assertEquals(List.of("A"), GraphUtil.shortestPath(g, "A", "A"));
		// 顶点缺失
		Assert.assertNull(GraphUtil.shortestPath(g, "A", "X"));
	}
}
