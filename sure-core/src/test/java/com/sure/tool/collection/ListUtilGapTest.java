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

import org.junit.Assert;
import org.junit.Test;

/**
 * ListUtil 覆盖率补测：分区/分页边界与空列表分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ListUtilGapTest {

	@Test
	public void testPartitionAndPage() {
		try {
			ListUtil.partition(Arrays.asList(1, 2), 0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertTrue(ListUtil.partition(null, 2).isEmpty());
		Assert.assertEquals(2, ListUtil.partition(Arrays.asList(1, 2, 3), 2).size());
		Assert.assertTrue(ListUtil.page(-1, 2, Arrays.asList(1, 2)).size() > 0);
		Assert.assertTrue(ListUtil.page(0, 0, Arrays.asList(1)).isEmpty());
		Assert.assertTrue(ListUtil.page(5, 2, Arrays.asList(1, 2)).isEmpty());
		Assert.assertEquals(Arrays.asList(2), ListUtil.page(1, 1, Arrays.asList(1, 2)));
		Assert.assertTrue(ListUtil.reverseNew(null).isEmpty());
	}
}
