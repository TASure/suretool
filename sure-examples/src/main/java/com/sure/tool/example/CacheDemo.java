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
package com.sure.tool.example;

import com.sure.tool.cache.CacheUtil;
import com.sure.tool.cache.LruCache;
import com.sure.tool.cache.TimedCache;

/**
 * 缓存工具示例（CacheUtil / LruCache / TimedCache）。
 */
public class CacheDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws InterruptedException {
		System.out.println("=== CacheDemo ===");
		LruCache<String, String> lru = CacheUtil.newLruCache(2);
		lru.put("a", "1");
		lru.put("b", "2");
		lru.put("c", "3");
		System.out.println("lru contains a = " + lru.containsKey("a") + ", size = " + lru.size());

		TimedCache<String, String> timed = CacheUtil.newTimedCache(50);
		timed.put("k", "v");
		System.out.println("timed get = " + timed.get("k"));
		Thread.sleep(80);
		System.out.println("timed get after expire = " + timed.get("k"));
	}
}
