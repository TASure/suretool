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

package com.sure.tool.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 缓存门面与各缓存边界补测：工厂方法、移除/清空、过期判定、访问器。
 */
public class CacheExtraTest {

	@Test
	public void 弱缓存工厂方法() {
		final WeakCache<Object, Object> c1 = CacheUtil.newWeakCache();
		assertNotNull(c1);
		final WeakCache<Object, Object> c2 = CacheUtil.newWeakCache(10, 1000L);
		assertEquals(10, c2.getCapacity());
		assertEquals(1000L, c2.getDefaultTimeout());
	}

	@Test
	public void 定时缓存移除存在项与清空() {
		final TimedCache<String, String> cache = new TimedCache<>(0);
		assertNull(cache.remove("absent"));
		cache.put("a", "v");
		assertEquals("v", cache.remove("a"));
		cache.put("b", "v");
		cache.clear();
		assertTrue(cache.isEmpty());
	}

	@Test
	public void 定时缓存过期项在containsKey时移除() throws InterruptedException {
		final TimedCache<String, String> cache = new TimedCache<>(1);
		cache.put("k", "v");
		Thread.sleep(5);
		assertFalse(cache.containsKey("k"));
		assertNull(cache.get("k"));
	}

	@Test
	public void 弱缓存清空与访问器() {
		final WeakCache<String, String> cache = new WeakCache<>(5, 1234L);
		assertEquals(5, cache.getCapacity());
		assertEquals(1234L, cache.getDefaultTimeout());
		cache.put("a", "1");
		cache.put("b", "2");
		cache.clear();
		assertTrue(cache.isEmpty());
	}
}
