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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 41：ListUtil 笛卡尔积 / 排列 / 组合 / 拼接测试（对标 Guava Lists）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class ListUtilBatch41Test {

	@Test
	public void testCartesianProduct() {
		List<List<Integer>> input = Arrays.asList(
				Arrays.asList(1, 2), Arrays.asList(3, 4));
		List<List<Integer>> result = ListUtil.cartesianProduct(input);
		Assert.assertEquals(4, result.size());
		Assert.assertEquals(Arrays.asList(1, 3), result.get(0));
		Assert.assertEquals(Arrays.asList(1, 4), result.get(1));
		Assert.assertEquals(Arrays.asList(2, 3), result.get(2));
		Assert.assertEquals(Arrays.asList(2, 4), result.get(3));

		// 三维
		List<List<Integer>> dims = Arrays.asList(
				Arrays.asList(1), Arrays.asList(2, 3), Arrays.asList(4, 5));
		Assert.assertEquals(4, ListUtil.cartesianProduct(dims).size());

		// 空维度 -> 空结果
		List<List<Integer>> withEmpty = Arrays.asList(Arrays.asList(1), Collections.emptyList());
		Assert.assertTrue(ListUtil.cartesianProduct(withEmpty).isEmpty());

		// 空输入 -> 含一个空组合
		Assert.assertEquals(1, ListUtil.cartesianProduct(new ArrayList<>()).size());
		Assert.assertNull(ListUtil.cartesianProduct(null));
	}

	@Test
	public void testPermutations() {
		List<String> three = Arrays.asList("a", "b", "c");
		Set<List<String>> perms = ListUtil.permutations(three);
		Assert.assertEquals(6, perms.size()); // 3!
		Assert.assertTrue(perms.contains(Arrays.asList("a", "b", "c")));
		Assert.assertTrue(perms.contains(Arrays.asList("a", "c", "b")));

		// 1 元素与空
		Assert.assertEquals(1, ListUtil.permutations(Collections.singletonList("x")).size());
		Assert.assertEquals(1, ListUtil.permutations(new ArrayList<>()).size());
		Assert.assertNull(ListUtil.permutations(null));
	}

	@Test
	public void testCombinations() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4);
		Set<List<Integer>> c2 = ListUtil.combinations(list, 2);
		Assert.assertEquals(6, c2.size()); // C(4,2)
		Assert.assertTrue(c2.contains(Arrays.asList(1, 2)));
		Assert.assertTrue(c2.contains(Arrays.asList(3, 4)));
		Assert.assertFalse(c2.contains(Arrays.asList(2, 1)));

		// k=0 与 k=n
		Assert.assertEquals(1, ListUtil.combinations(list, 0).size());
		Assert.assertEquals(1, ListUtil.combinations(list, 4).size());
		Assert.assertEquals(10, ListUtil.combinations(Arrays.asList(1, 2, 3, 4, 5), 3).size());

		try {
			ListUtil.combinations(list, 5);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 越界
		}
		Assert.assertNull(ListUtil.combinations(null, 1));
	}

	@Test
	public void testConcatTwo() {
		List<Integer> merged = ListUtil.concat(Arrays.asList(1, 2), Arrays.asList(3));
		Assert.assertEquals(Arrays.asList(1, 2, 3), merged);
		// 不可变
		try {
			merged.add(4);
			Assert.fail("should be immutable");
		} catch (UnsupportedOperationException expected) {
			// 不可变视图
		}
		// null 按空处理
		Assert.assertEquals(Arrays.asList(1), ListUtil.concat(null, Arrays.asList(1)));
		Assert.assertEquals(Arrays.asList(2), ListUtil.concat(Arrays.asList(2), null));
	}

	@Test
	public void testConcatVarargs() {
		List<Integer> merged = ListUtil.concat(Arrays.asList(1), Arrays.asList(2, 3), Arrays.asList(4));
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4), merged);
		// null 列表跳过
		Assert.assertEquals(Arrays.asList(1, 2), ListUtil.concat(Arrays.asList(1), null, Arrays.asList(2)));
		Assert.assertTrue(ListUtil.concat((List<Integer>[]) null).isEmpty());
		Assert.assertTrue(ListUtil.concat(new ArrayList<>(), new ArrayList<>()).isEmpty());
	}
}
