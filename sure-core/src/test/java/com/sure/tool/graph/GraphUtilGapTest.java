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

import org.junit.Assert;
import org.junit.Test;

/**
 * GraphUtil 覆盖率补测：环 DFS 与 BFS 缺省顶点守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class GraphUtilGapTest {

	@Test
	public void testDfsCycleAndBfs() {
		DiGraph<Integer> g = new DiGraph<>();
		g.addEdge(1, 2);
		g.addEdge(2, 1);
		Assert.assertEquals(2, GraphUtil.dfs(g, 1).size());
		Assert.assertTrue(GraphUtil.bfs(g, 99).isEmpty());
		Assert.assertEquals(2, GraphUtil.bfs(g, 1).size());
	}
}
