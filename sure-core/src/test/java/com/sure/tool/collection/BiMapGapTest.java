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

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * BiMap 覆盖率补测：从 Map 构造与 putAll(null) 守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class BiMapGapTest {

	@Test
	public void testConstructors() {
		HashMap<String, Integer> m = new HashMap<>();
		m.put("a", 1);
		BiMap<String, Integer> bm = new BiMap<>(m);
		Assert.assertEquals(Integer.valueOf(1), bm.get("a"));
		Assert.assertEquals("a", bm.getKey(1));
		BiMap<String, Integer> empty = new BiMap<>(null);
		empty.putAll(null);
		Assert.assertNull(empty.get("x"));
	}
}
