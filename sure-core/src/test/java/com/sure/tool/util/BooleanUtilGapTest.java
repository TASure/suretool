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
 * BooleanUtil 覆盖率补测：parse 假值分支与 or/and null 守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class BooleanUtilGapTest {

	@Test
	public void testParseFalse() {
		Assert.assertEquals(Boolean.FALSE, BooleanUtil.toBooleanObj("off"));
		Assert.assertEquals(Boolean.FALSE, BooleanUtil.toBooleanObj("否"));
		Assert.assertNull(BooleanUtil.toBooleanObj("maybe"));
	}

	@Test
	public void testOrAnd() {
		Assert.assertFalse(BooleanUtil.or((boolean[]) null));
		Assert.assertFalse(BooleanUtil.or(false, false));
		Assert.assertTrue(BooleanUtil.or(false, true));
		Assert.assertFalse(BooleanUtil.and((boolean[]) null));
		Assert.assertTrue(BooleanUtil.and(true, true));
	}
}
