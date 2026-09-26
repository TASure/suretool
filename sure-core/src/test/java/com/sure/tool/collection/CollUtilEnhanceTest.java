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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * P0 批 2（v1.1.0）：CollUtil 增补方法测试（chunk/random/toImmutable/frequency）。
 */
public class CollUtilEnhanceTest {

	/**
	 * 分片数量与尺寸正确。
	 */
	@Test
	public void testChunk() {
		List<Integer> source = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			source.add(i);
		}
		List<List<Integer>> chunks = CollUtil.chunk(source, 3);
		Assert.assertEquals(4, chunks.size());
		Assert.assertEquals(List.of(0, 1, 2), chunks.get(0));
		Assert.assertEquals(List.of(9), chunks.get(3));
	}

	/**
	 * 分片边界：空集合与非法 size。
	 */
	@Test
	public void testChunkBoundary() {
		Assert.assertEquals(List.of(), CollUtil.chunk(List.of(), 3));
		Assert.assertEquals(List.of(), CollUtil.chunk(null, 3));
		try {
			CollUtil.chunk(List.of(1), 0);
			Assert.fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	/**
	 * 随机取一：样本必须来自集合。
	 */
	@Test
	public void testRandomItem() {
		Set<Integer> source = new HashSet<>(List.of(1, 2, 3, 4, 5));
		for (int i = 0; i < 50; i++) {
			Integer item = CollUtil.randomItem(source);
			Assert.assertTrue("样本应属于集合", source.contains(item));
		}
	}

	/**
	 * 随机取 n：数量正确且不重复。
	 */
	@Test
	public void testRandomItems() {
		List<Integer> source = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
		List<Integer> sample = CollUtil.randomItems(source, 3);
		Assert.assertEquals(3, sample.size());
		Assert.assertEquals(new HashSet<>(sample).size(), sample.size());
		// count 超过集合大小时返回全量
		Assert.assertEquals(6, CollUtil.randomItems(source, 10).size());
		Assert.assertEquals(List.of(), CollUtil.randomItems(source, 0));
	}

	/**
	 * 不可变副本：结果不可修改，原集合不受影响。
	 */
	@Test
	public void testToImmutable() {
		List<String> source = new ArrayList<>(List.of("a", "b"));
		List<String> immutable = CollUtil.toImmutable(source);
		source.add("c");
		Assert.assertEquals(List.of("a", "b"), immutable);
		try {
			immutable.add("x");
			Assert.fail("应抛出 UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// 预期
		}
	}

	/**
	 * 频次统计。
	 */
	@Test
	public void testFrequency() {
		List<String> source = List.of("a", "b", "a", "c", "a");
		Assert.assertEquals(3, CollUtil.frequency(source, "a"));
		Assert.assertEquals(1, CollUtil.frequency(source, "c"));
		Assert.assertEquals(0, CollUtil.frequency(source, "z"));
		Assert.assertEquals(0, CollUtil.frequency(List.of(), "a"));
	}
}
