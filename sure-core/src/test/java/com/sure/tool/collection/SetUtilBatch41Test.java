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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 41：SetUtil 组合测试（对标 Guava Sets.combinations）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class SetUtilBatch41Test {

	@Test
	public void testCombinations() {
		Set<Integer> set = new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4));
		Set<Set<Integer>> c2 = SetUtil.combinations(set, 2);
		Assert.assertEquals(6, c2.size()); // C(4,2)
		Assert.assertTrue(c2.contains(new LinkedHashSet<>(Arrays.asList(1, 2))));
		Assert.assertTrue(c2.contains(new LinkedHashSet<>(Arrays.asList(3, 4))));

		// k=0 / k=n
		Set<Set<Integer>> c0 = SetUtil.combinations(set, 0);
		Assert.assertEquals(1, c0.size());
		Assert.assertTrue(c0.contains(new LinkedHashSet<>()));
		Assert.assertEquals(1, SetUtil.combinations(set, 4).size());

		// C(5,3) = 10
		Set<Integer> five = new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		Assert.assertEquals(10, SetUtil.combinations(five, 3).size());

		try {
			SetUtil.combinations(set, 5);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 越界
		}
		try {
			SetUtil.combinations(set, -1);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 越界
		}
		Assert.assertNull(SetUtil.combinations(null, 1));
	}
}
