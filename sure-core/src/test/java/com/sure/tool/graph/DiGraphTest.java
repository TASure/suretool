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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * DiGraph 基础行为测试。
 */
public class DiGraphTest {

	/**
	 * 顶点增删与计数。
	 */
	@Test
	public void testVertices() {
		DiGraph<String> g = new DiGraph<>();
		Assert.assertTrue(g.addVertex("A"));
		Assert.assertFalse(g.addVertex("A"));
		Assert.assertEquals(1, g.vertexCount());
		Assert.assertTrue(g.containsVertex("A"));
		Assert.assertFalse(g.containsVertex("B"));
		Assert.assertTrue(g.removeVertex("A"));
		Assert.assertFalse(g.removeVertex("A"));
		Assert.assertEquals(0, g.vertexCount());
	}

	/**
	 * 边增删自动补顶点。
	 */
	@Test
	public void testEdges() {
		DiGraph<String> g = new DiGraph<>();
		Assert.assertTrue(g.addEdge("A", "B"));
		Assert.assertFalse(g.addEdge("A", "B"));
		Assert.assertTrue(g.containsEdge("A", "B"));
		Assert.assertFalse(g.containsEdge("B", "A"));
		Assert.assertEquals(2, g.vertexCount());
		Assert.assertEquals(1, g.edgeCount());
		Assert.assertTrue(g.removeEdge("A", "B"));
		Assert.assertFalse(g.containsEdge("A", "B"));
		Assert.assertEquals(0, g.edgeCount());
	}

	/**
	 * 邻接查询：successors/predecessors/adjacentNodes/度。
	 */
	@Test
	public void testAdjacency() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("A", "C");
		g.addEdge("B", "C");
		Assert.assertEquals(Set.of("B", "C"), g.successors("A"));
		Assert.assertEquals(Set.of("A"), g.predecessors("B"));
		Assert.assertEquals(Set.of("A", "C"), g.adjacentNodes("B"));
		Assert.assertEquals(2, g.outDegree("A"));
		Assert.assertEquals(1, g.inDegree("B"));
		Assert.assertEquals(2, g.degree("B"));
		Assert.assertEquals(0, g.inDegree("X"));
	}

	/**
	 * 移除顶点级联删除关联边。
	 */
	@Test
	public void testRemoveVertexCascade() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "C");
		g.removeVertex("B");
		Assert.assertEquals(2, g.vertexCount());
		Assert.assertFalse(g.containsEdge("A", "B"));
		Assert.assertFalse(g.containsEdge("B", "C"));
		Assert.assertEquals(0, g.edgeCount());
	}

	/**
	 * edges()/vertices() 返回全部边与顶点。
	 */
	@Test
	public void testViews() {
		DiGraph<String> g = new DiGraph<>();
		g.addEdge("A", "B");
		g.addEdge("B", "C");
		Assert.assertEquals(3, g.vertices().size());
		List<DiGraph.Edge<String>> edges = g.edges();
		Assert.assertEquals(2, edges.size());
		Assert.assertTrue(edges.contains(new DiGraph.Edge<>("A", "B")));
		Assert.assertTrue(edges.contains(new DiGraph.Edge<>("B", "C")));
	}

	/**
	 * 自环与 null 防御。
	 */
	@Test
	public void testSelfLoopAndNull() {
		DiGraph<String> g = new DiGraph<>();
		Assert.assertTrue(g.addEdge("A", "A"));
		Assert.assertEquals(1, g.edgeCount());
		Assert.assertEquals(1, g.outDegree("A"));
		try {
			g.addVertex(null);
			Assert.fail("null 顶点应抛异常");
		} catch (NullPointerException expected) {
			// 预期
		}
	}
}
