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
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 缓存实现覆盖率补强：LruCache/LfuCache/FifoCache/TimedCache/CacheUtil 行为路径。
 */
public class CacheGapTest {

	@Test
	public void lru_evictsLeastRecentlyUsed() {
		LruCache<String, String> cache = new LruCache<>(2);
		assertNull(cache.put("a", "1"));
		assertNull(cache.put("b", "2"));
		assertEquals(2, cache.size());
		cache.put("c", "3");
		assertFalse(cache.containsKey("a"));
		assertTrue(cache.containsKey("b"));
		assertTrue(cache.containsKey("c"));
	}

	@Test
	public void lru_getRefreshesOrder() {
		LruCache<String, String> cache = new LruCache<>(2);
		cache.put("a", "1");
		cache.put("b", "2");
		assertEquals("1", cache.get("a"));
		cache.put("c", "3");
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.containsKey("b"));
	}

	@Test
	public void lru_ops() {
		LruCache<String, String> cache = new LruCache<>(3);
		assertNull(cache.put("a", "1"));
		assertEquals("1", cache.put("a", "2"));
		assertEquals("2", cache.get("a"));
		assertEquals("2", cache.remove("a"));
		assertNull(cache.remove("a"));
		assertTrue(cache.isEmpty());
		cache.put("a", "1");
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.containsKey("b"));
		cache.clear();
		assertEquals(0, cache.size());
		try {
			new LruCache<>(0);
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
	}

	@Test
	public void lfu_evictsLowestFrequency() {
		LfuCache<String, String> cache = new LfuCache<>(2);
		cache.put("a", "1");
		cache.put("b", "2");
		assertEquals("1", cache.get("a"));
		assertEquals("1", cache.get("a"));
		cache.put("c", "3");
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.containsKey("b"));
		assertTrue(cache.containsKey("c"));
	}

	@Test
	public void lfu_putExistingCounts() {
		LfuCache<String, String> cache = new LfuCache<>(2);
		cache.put("a", "1");
		assertEquals("1", cache.put("a", "2"));
		assertEquals("2", cache.get("a"));
		cache.put("b", "3");
		assertEquals("3", cache.get("b"));
		cache.put("c", "4");
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.containsKey("b"));
	}

	@Test
	public void lfu_ops() {
		LfuCache<String, String> cache = new LfuCache<>(3);
		cache.put("a", "1");
		assertEquals("1", cache.remove("a"));
		assertNull(cache.remove("a"));
		assertTrue(cache.isEmpty());
		cache.put("a", "1");
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.containsKey("b"));
		cache.clear();
		assertEquals(0, cache.size());
		try {
			new LfuCache<>(-1);
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
	}

	@Test
	public void fifo_evictsOldest() {
		FifoCache<String, String> cache = new FifoCache<>(2);
		cache.put("a", "1");
		cache.put("b", "2");
		assertEquals("1", cache.get("a"));
		cache.put("c", "3");
		assertFalse(cache.containsKey("a"));
		assertTrue(cache.containsKey("b"));
		assertTrue(cache.containsKey("c"));
	}

	@Test
	public void fifo_ops() {
		FifoCache<String, String> cache = new FifoCache<>(3);
		cache.put("a", "1");
		assertEquals("1", cache.put("a", "2"));
		assertEquals("2", cache.remove("a"));
		assertNull(cache.remove("a"));
		assertTrue(cache.isEmpty());
		cache.put("a", "1");
		assertTrue(cache.containsKey("a"));
		cache.clear();
		assertEquals(0, cache.size());
		try {
			new FifoCache<>(0);
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
	}

	@Test
	public void timed_basic() throws InterruptedException {
		TimedCache<String, String> cache = new TimedCache<>(1000);
		assertNull(cache.put("a", "1"));
		assertEquals("1", cache.get("a"));
		assertEquals(1, cache.size());
		assertTrue(cache.containsKey("a"));
		assertFalse(cache.isEmpty());
		assertEquals(0, cache.prune());
	}

	@Test
	public void timed_expiredOnAccess() throws InterruptedException {
		TimedCache<String, String> cache = new TimedCache<>(1);
		cache.put("a", "1");
		Thread.sleep(30);
		assertNull(cache.get("a"));
		assertFalse(cache.containsKey("a"));
		assertEquals(0, cache.size());
		assertTrue(cache.isEmpty());
	}

	@Test
	public void timed_customTtl() throws InterruptedException {
		TimedCache<String, String> cache = new TimedCache<>(1);
		cache.put("a", "1", 0);
		cache.put("b", "2", -1);
		Thread.sleep(30);
		assertEquals("1", cache.get("a"));
		assertEquals("2", cache.get("b"));
		assertEquals(2, cache.size());
	}

	@Test
	public void timed_pruneRemovesExpired() throws InterruptedException {
		TimedCache<String, String> cache = new TimedCache<>(1);
		cache.put("a", "1");
		cache.put("b", "2");
		cache.put("c", "3", 0);
		Thread.sleep(30);
		assertEquals(2, cache.prune());
		assertEquals(1, cache.size());
		assertEquals("3", cache.get("c"));
	}

	@Test
	public void cacheUtil_factories() {
		assertNotNull(CacheUtil.newFifoCache(2));
		assertNotNull(CacheUtil.newLruCache(2));
		assertNotNull(CacheUtil.newLfuCache(2));
		assertNotNull(CacheUtil.newTimedCache(1000));
	}
}
