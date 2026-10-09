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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * MapUtil 覆盖率补测：成对参数校验、null 守卫、reverse/join/inverse。
 *
 * @author suretool
 * @since 1.13.1
 */
public class MapUtilGapTest {

	@Test
	public void testOfValidation() {
		try {
			MapUtil.of("a", 1, "b");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Map<String, Object> m = MapUtil.of("a", 1, "b", 2);
		Assert.assertEquals(2, m.size());
	}

	@Test
	public void testNullGuards() {
		Assert.assertEquals("def", MapUtil.get(null, "k", "def"));
		Assert.assertTrue(MapUtil.reverse(null).isEmpty());
		Assert.assertEquals("", MapUtil.join(null, ",", "="));
		Assert.assertNull(MapUtil.inverse(null));
		Map<String, Object> in = new HashMap<>();
		in.put("a", new BigDecimal("1.5"));
		Assert.assertEquals(new BigDecimal("1.5"), MapUtil.getBigDecimal(in, "a", BigDecimal.ZERO));
		Assert.assertTrue(MapUtil.filter(new HashMap<>(), e -> true).isEmpty());
	}
}
