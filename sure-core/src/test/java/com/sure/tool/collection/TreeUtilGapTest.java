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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * TreeUtil 覆盖率补测：null 守卫与空列表分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class TreeUtilGapTest {

	@Test
	public void testNullGuards() {
		TreeUtil.walk(null, n -> { });
		Assert.assertTrue(TreeUtil.flatten(null).isEmpty());
		Assert.assertNull(TreeUtil.findNode(null, 1));
		TreeNode<Integer> root = new TreeNode<>(1, null, "root");
		TreeUtil.walk(root, n -> { });
		Assert.assertEquals(1, TreeUtil.flatten(Collections.singletonList(root)).size());
		Assert.assertEquals(Integer.valueOf(1), TreeUtil.findNode(root, 1).getId());
		Assert.assertNull(TreeUtil.findNode(root, 99));
	}
}
