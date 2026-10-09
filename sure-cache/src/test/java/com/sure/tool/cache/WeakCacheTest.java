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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * WeakCache 单元测试：GC 回收自动清理 / TTL 过期 / 容量淘汰 / 覆盖不误删 / 并发安全。
 *
 * @author suretool
 * @since 1.10.0
 */
public class WeakCacheTest {

	/** 值对象：仅用于可回收测试。 */
	private static class Payload {
		byte[] memory = new byte[1024];
	}

	@Test
	public void testGetPutBasic() {
		WeakCache<String, String> cache = new WeakCache<>();
		assertNull(cache.put("a", "1"));
		assertEquals("1", cache.get("a"));
		assertTrue(cache.containsKey("a"));
		assertEquals(1, cache.size());
		assertEquals("1", cache.remove("a"));
		assertNull(cache.get("a"));
		assertTrue(cache.isEmpty());
	}

	@Test
	public void testValueCollectedAfterGc() {
		WeakCache<String, Payload> cache = new WeakCache<>();
		cache.put("k", new Payload());
		assertNotNull(cache.get("k"));
		// 移除强引用，多次触发 GC 后值应被回收并从缓存自动清理
		for (int i = 0; i < 10; i++) {
			System.gc();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (cache.get("k") == null) {
				break;
			}
		}
		assertNull(cache.get("k"));
		assertEquals(0, cache.size());
	}

	@Test
	public void testTtlExpiry() throws InterruptedException {
		WeakCache<String, String> cache = new WeakCache<>(50);
		cache.put("k", "v");
		assertEquals("v", cache.get("k"));
		Thread.sleep(120);
		assertNull(cache.get("k"));
		assertFalse(cache.containsKey("k"));
	}

	@Test
	public void testCapacityEviction() {
		WeakCache<String, String> cache = new WeakCache<>(2, 0);
		cache.put("a", "1");
		cache.put("b", "2");
		cache.put("c", "3");
		assertTrue("容量淘汰后应不超过上限", cache.size() <= 2);
		assertNull(cache.get("a"));
	}

	@Test
	public void testOverwriteDoesNotRemoveNewValue() {
		WeakCache<String, Payload> cache = new WeakCache<>();
		cache.put("k", new Payload());
		// 新值保持强引用，仅旧值可被回收
		Payload kept = new Payload();
		cache.put("k", kept);
		for (int i = 0; i < 10; i++) {
			System.gc();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		// 旧值被回收触发引用队列，但不得误删被覆盖的新条目
		assertSame("覆盖后的新值不应被旧引用清理误删", kept, cache.get("k"));
	}

	@Test
	public void testVirtualThreadConcurrency() throws InterruptedException {
		WeakCache<Integer, String> cache = new WeakCache<>(1_000, 0);
		Thread[] threads = new Thread[16];
		for (int t = 0; t < threads.length; t++) {
			final int base = t;
			threads[t] = Thread.ofVirtual().start(() -> {
				for (int i = 0; i < 200; i++) {
					int key = (base * 200 + i) % 1_000;
					cache.put(key, "v" + key);
					String value = cache.get(key);
					assertNotNull("并发读写不得丢失数据", value);
				}
			});
		}
		for (Thread thread : threads) {
			thread.join();
		}
		assertTrue(cache.size() <= 1_000);
	}
}
