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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * ArrayUtil 覆盖率补测：null 数组守卫与默认值/去重分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ArrayUtilGapTest {

	@Test
	public void testNullGuards() {
		Assert.assertFalse(ArrayUtil.contains(null, "a"));
		Assert.assertEquals(-1, ArrayUtil.indexOf(null, "a"));
		Assert.assertEquals("def", ArrayUtil.get(null, 0, "def"));
		Assert.assertNull(ArrayUtil.distinct(null));
		Assert.assertFalse(ArrayUtil.contains(new int[] {1, 2}, 3));
		Assert.assertEquals(1, ArrayUtil.indexOf(new int[] {1, 2}, 2));
		Assert.assertEquals("x", ArrayUtil.get(new String[] {"x"}, 0, "def"));
		Assert.assertEquals(2, ArrayUtil.distinct(new int[] {1, 1, 2}).length);
		Assert.assertEquals(0, ArrayUtil.distinct(new int[0]).length);
		Assert.assertEquals("def", ArrayUtil.get(new int[0], 0, "def"));
	}
}
