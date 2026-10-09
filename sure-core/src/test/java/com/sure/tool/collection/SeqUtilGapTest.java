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
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * SeqUtil 覆盖率补测：null/空守卫与非 SequencedMap（HashMap）回退分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class SeqUtilGapTest {

	private Map<String, Integer> sequencedMap() {
		LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
		m.put("a", 1);
		m.put("b", 2);
		return m;
	}

	private Map<String, Integer> plainMap() {
		Map<String, Integer> m = new HashMap<>();
		m.put("a", 1);
		m.put("b", 2);
		return m;
	}

	@Test
	public void testKeys() {
		Assert.assertNull(SeqUtil.firstKeyOrNull(null));
		Assert.assertNull(SeqUtil.firstKeyOrNull(new HashMap<>()));
		Assert.assertEquals("a", SeqUtil.firstKeyOrNull(sequencedMap()));
		Assert.assertNotNull(SeqUtil.firstKeyOrNull(plainMap()));
		Assert.assertNull(SeqUtil.lastKeyOrNull(null));
		Assert.assertNull(SeqUtil.lastKeyOrNull(new HashMap<>()));
		Assert.assertEquals("b", SeqUtil.lastKeyOrNull(sequencedMap()));
		Assert.assertNotNull(SeqUtil.lastKeyOrNull(plainMap()));
	}

	@Test
	public void testEntries() {
		Assert.assertNull(SeqUtil.firstEntryOrNull(null));
		Assert.assertNull(SeqUtil.firstEntryOrNull(new HashMap<>()));
		Assert.assertEquals("a", SeqUtil.firstEntryOrNull(sequencedMap()).getKey());
		Assert.assertNotNull(SeqUtil.firstEntryOrNull(plainMap()));
		Assert.assertNull(SeqUtil.lastEntryOrNull(null));
		Assert.assertNull(SeqUtil.lastEntryOrNull(new HashMap<>()));
		Assert.assertEquals("b", SeqUtil.lastEntryOrNull(sequencedMap()).getKey());
		Assert.assertNotNull(SeqUtil.lastEntryOrNull(plainMap()));
	}

	@Test
	public void testCollectionFallback() {
		// 非 SequencedCollection 的 Set 回退
		Assert.assertNull(SeqUtil.firstOrNull(null));
		Assert.assertEquals("x", SeqUtil.firstOrNull(new java.util.LinkedHashSet<>(java.util.Arrays.asList("x", "y"))));
		Assert.assertEquals("y", SeqUtil.lastOrNull(new java.util.LinkedHashSet<>(java.util.Arrays.asList("x", "y"))));
		Assert.assertEquals(java.util.List.of(1, 2), SeqUtil.reversed(java.util.List.of(2, 1)));
	}
}
