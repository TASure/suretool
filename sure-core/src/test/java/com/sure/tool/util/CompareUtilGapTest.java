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

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

/**
 * CompareUtil 覆盖率补测：比较器分支、null 守卫与极值/区间约束。
 *
 * @author suretool
 * @since 1.13.1
 */
public class CompareUtilGapTest {

	@Test
	public void testCompare() {
		Assert.assertEquals(0, CompareUtil.compare(1, 1, Comparator.naturalOrder()));
		Assert.assertEquals(-1, CompareUtil.compare(null, 1, null));
		Assert.assertEquals(1, CompareUtil.compare(1, null, null));
		Assert.assertEquals(0, CompareUtil.compareIgnoreCase(null, null));
		Assert.assertEquals(-1, CompareUtil.compareIgnoreCase(null, "a"));
		Assert.assertEquals(1, CompareUtil.compareIgnoreCase("a", null));
	}

	@Test
	public void testMinMax() {
		Assert.assertNull(CompareUtil.max(null));
		Assert.assertEquals(Integer.valueOf(3), CompareUtil.max(null, 1, 3, 2));
		Assert.assertNull(CompareUtil.min(null));
		Assert.assertEquals(Integer.valueOf(1), CompareUtil.min(null, null, 1, 2));
		Assert.assertEquals(Integer.valueOf(1), CompareUtil.clamp(1, 0, 5));
		Assert.assertEquals(Integer.valueOf(0), CompareUtil.clamp(-1, 0, 5));
		Assert.assertNull(CompareUtil.clamp(null, 0, 5));
	}
}
