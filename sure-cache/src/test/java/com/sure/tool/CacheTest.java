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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.sure.tool.thread.ThreadUtil;

/**
 * 缓存域测试：FIFO/LRU/LFU/Timed 逐出策略、过期、门面与并发冒烟。
 */
public class CacheTest {

	@Test
	public void testFifoEvictsOldest() {
		FifoCache<String, Integer> cache = new FifoCache<>(2);
		cache.put("a", 1);
		cache.put("b", 2);
		cache.put("c", 3);
		assertNull(cache.get("a"));
		assertEquals(Integer.valueOf(2), cache.get("b"));
		assertEquals(Integer.valueOf(3), cache.get("c"));
		assertEquals(2, cache.size());
	}

	@Test
	public void testFifoGetDoesNotRefresh() {
		FifoCache<String, Integer> cache = new FifoCache<>(2);
		cache.put("a", 1);
		cache.put("b", 2);
		cache.get("a");
		cache.put("c", 3);
		// FIFO 不看访问频率：a 仍是最旧，先被逐出
		assertNull(cache.get("a"));
		assertEquals(Integer.valueOf(2), cache.get("b"));
	}

	@Test
	public void testLruKeepsRecent() {
		LruCache<String, Integer> cache = new LruCache<>(2);
		cache.put("a", 1);
		cache.put("b", 2);
		cache.get("a");
		cache.put("c", 3);
		// a 刚被访问，b 被逐出
		assertEquals(Integer.valueOf(1), cache.get("a"));
		assertNull(cache.get("b"));
		assertEquals(Integer.valueOf(3), cache.get("c"));
	}

	@Test
	public void testLruEvictsEldestWithoutAccess() {
		LruCache<String, Integer> cache = new LruCache<>(2);
		cache.put("a", 1);
		cache.put("b", 2);
		cache.put("c", 3);
		assertNull(cache.get("a"));
		assertEquals(Integer.valueOf(2), cache.get("b"));
	}

	@Test
	public void testLfuEvictsLowestFrequency() {
		LfuCache<String, Integer> cache = new LfuCache<>(2);
		cache.put("a", 1);
		cache.put("b", 2);
		cache.get("a");
		cache.get("a");
		cache.get("b");
		cache.put("c", 3);
		// b 频率最低（1 次），a 2 次
		assertNull(cache.get("b"));
		assertEquals(Integer.valueOf(1), cache.get("a"));
		assertEquals(Integer.valueOf(3), cache.get("c"));
	}

	@Test
	public void testTimedExpires() throws Exception {
		TimedCache<String, Integer> cache = new TimedCache<>(1000);
		cache.put("k", 1, 100);
		assertEquals(Integer.valueOf(1), cache.get("k"));
		ThreadUtil.sleep(150);
		assertNull(cache.get("k"));
		assertFalse(cache.containsKey("k"));
		assertTrue(cache.isEmpty());
	}

	@Test
	public void testTimedDefaultTimeout() throws Exception {
		TimedCache<String, Integer> cache = new TimedCache<>(50);
		cache.put("k", 1);
		ThreadUtil.sleep(80);
		assertNull(cache.get("k"));
	}

	@Test
	public void testTimedNotExpired() throws Exception {
		TimedCache<String, Integer> cache = new TimedCache<>(5000);
		cache.put("k", 1, 5000);
		ThreadUtil.sleep(50);
		assertEquals(Integer.valueOf(1), cache.get("k"));
	}

	@Test
	public void testTimedPutWithTimeoutZeroNeverExpires() throws Exception {
		TimedCache<String, Integer> cache = new TimedCache<>(0);
		cache.put("k", 1, 0);
		ThreadUtil.sleep(80);
		assertEquals(Integer.valueOf(1), cache.get("k"));
	}

	@Test
	public void testCacheUtilFactories() {
		assertTrue(CacheUtil.newFifoCache(2).isEmpty());
		assertTrue(CacheUtil.newLruCache(2).isEmpty());
		assertTrue(CacheUtil.newLfuCache(2).isEmpty());
		LruCache<String, String> lru = CacheUtil.newLruCache(2);
		lru.put("a", "1");
		assertEquals("1", lru.get("a"));
		assertEquals(1, lru.size());
		TimedCache<String, String> timed = CacheUtil.newTimedCache(1000);
		timed.put("a", "b");
		assertEquals("b", timed.get("a"));
		assertEquals(1, timed.size());
	}

	@Test
	public void testConcurrentLru() throws Exception {
		final LruCache<Integer, Integer> cache = new LruCache<>(100);
		int threads = 8;
		int rounds = 100;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		final CountDownLatch latch = new CountDownLatch(threads);
		final AtomicReference<Throwable> error = new AtomicReference<>();
		for (int t = 0; t < threads; t++) {
			final int base = t;
			pool.submit(() -> {
				try {
					for (int i = 0; i < rounds; i++) {
						int key = base * rounds + i;
						cache.put(key, i);
						cache.get(key);
					}
				} catch (Throwable e) {
					error.set(e);
				} finally {
					latch.countDown();
				}
			});
		}
		assertTrue(latch.await(10, TimeUnit.SECONDS));
		pool.shutdown();
		assertNull("并发访问不应抛异常", error.get());
		assertTrue("容量 100 不应超过", cache.size() <= 100);
	}

	@Test
	public void testInvalidCapacity() {
		try {
			new LruCache<Integer, Integer>(0);
			assertTrue("应抛异常", false);
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}
}
