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
 * EnumUtil 覆盖率补测：null/未匹配守卫与序号越界。
 *
 * @author suretool
 * @since 1.13.1
 */
public class EnumUtilGapTest {

	private enum Color {
		RED, GREEN
	}

	@Test
	public void testGuards() {
		Assert.assertNull(EnumUtil.fromString(null, "x"));
		Assert.assertNull(EnumUtil.fromString(Color.class, null));
		Assert.assertNull(EnumUtil.fromString(Color.class, "BLUE"));
		Assert.assertEquals(Color.RED, EnumUtil.fromString(Color.class, "RED"));
		Assert.assertNull(EnumUtil.fromOrdinal(null, 0));
		Assert.assertNull(EnumUtil.fromOrdinal(Color.class, -1));
		Assert.assertNull(EnumUtil.fromOrdinal(Color.class, 99));
		Assert.assertEquals(Color.GREEN, EnumUtil.fromOrdinal(Color.class, 1));
		Assert.assertFalse(EnumUtil.containsName(Color.class, "BLUE", false));
		Assert.assertTrue(EnumUtil.containsName(Color.class, "red", true));
	}
}
