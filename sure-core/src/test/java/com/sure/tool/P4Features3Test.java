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
package com.sure.tool;

import com.sure.tool.collection.BloomFilterUtil;
import com.sure.tool.collection.BoundedPriorityQueue;
import com.sure.tool.image.ImageUtil;
import com.sure.tool.lang.Pair;
import com.sure.tool.lang.Triple;
import com.sure.tool.lang.WeightRandom;
import com.sure.tool.util.UrlUtil;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * P4（v0.2.0）第三批新增工具单元测试：BloomFilterUtil / UrlUtil / ImageUtil / WeightRandom / BoundedPriorityQueue / Pair / Triple。
 */
public class P4Features3Test {

	// ---------------- BloomFilterUtil ----------------

	@Test
	public void testBloomFilter() {
		BloomFilterUtil filter = new BloomFilterUtil(1000, 0.01);
		for (int i = 0; i < 1000; i++) {
			filter.put("item-" + i);
		}
		// 已加入元素绝不漏判
		for (int i = 0; i < 1000; i++) {
			Assert.assertTrue("不应漏判 item-" + i, filter.mightContain("item-" + i));
		}
		// 未加入元素误判率应较低
		int falsePositive = 0;
		for (int i = 1000; i < 10000; i++) {
			if (filter.mightContain("item-" + i)) {
				falsePositive++;
			}
		}
		Assert.assertTrue("误判率过高: " + falsePositive, falsePositive < 100); // < 1.1%
		Assert.assertTrue(filter.bitSize() > 0);
		Assert.assertTrue(filter.hashFunctions() >= 1);
		try {
			new BloomFilterUtil(0, 0.01);
			Assert.fail("expectedInsertions=0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- UrlUtil ----------------

	@Test
	public void testUrlUtil() {
		String encoded = UrlUtil.encode("a b&c=中文");
		Assert.assertEquals("a+b%26c%3D%E4%B8%AD%E6%96%87", encoded);
		Assert.assertEquals("a b&c=中文", UrlUtil.decode(encoded));
		Assert.assertEquals("a%20b%26c%3D%E4%B8%AD%E6%96%87", UrlUtil.encodePath("a b&c=中文"));
		Assert.assertNull(UrlUtil.encode(null));
		Assert.assertNull(UrlUtil.decode(null));

		String url = "https://example.com:8443/path/to?name=张三&age=30";
		Assert.assertEquals("example.com", UrlUtil.getHost(url));
		Assert.assertEquals(8443, UrlUtil.getPort(url));
		Assert.assertEquals("https", UrlUtil.getScheme(url));
		Assert.assertEquals("/path/to", UrlUtil.getPath(url));
		Assert.assertTrue(UrlUtil.isHttp(url));
		Assert.assertFalse(UrlUtil.isHttp("ftp://x.com"));

		Map<String, String> params = UrlUtil.getParams(url);
		Assert.assertEquals("张三", params.get("name"));
		Assert.assertEquals("30", params.get("age"));
		Assert.assertEquals("30", UrlUtil.getParam(url, "age"));
		Assert.assertNull(UrlUtil.getParam(url, "nope"));

		Map<String, String> more = new HashMap<>();
		more.put("k", "v 1");
		Assert.assertEquals("https://x.com?a=1&k=v+1", UrlUtil.appendParams("https://x.com?a=1", more));
		Assert.assertEquals("https://x.com", UrlUtil.appendParams("https://x.com", null));
	}

	// ---------------- ImageUtil ----------------

	@Test
	public void testImageUtil() throws Exception {
		BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
		img.createGraphics().dispose();

		BufferedImage scaled = ImageUtil.scale(img, 200, 100);
		Assert.assertEquals(200, scaled.getWidth());
		Assert.assertEquals(100, scaled.getHeight());

		BufferedImage cropped = ImageUtil.crop(img, 10, 10, 20, 20);
		Assert.assertEquals(20, cropped.getWidth());
		Assert.assertEquals(20, cropped.getHeight());
		try {
			ImageUtil.crop(img, 90, 40, 20, 20);
			Assert.fail("越界裁剪应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}

		BufferedImage gray = ImageUtil.gray(img);
		Assert.assertEquals(100, gray.getWidth());

		BufferedImage rotated = ImageUtil.rotate(img, 90);
		Assert.assertEquals(50, rotated.getWidth());
		Assert.assertEquals(100, rotated.getHeight());
		Assert.assertSame(img, ImageUtil.rotate(img, 360));

		Assert.assertEquals("100x50", ImageUtil.size(img));

		Path png = Files.createTempFile("sure-img", ".png");
		try {
			ImageUtil.write(img, png, "png");
			BufferedImage back = ImageUtil.read(png);
			Assert.assertNotNull(back);
			Assert.assertEquals(100, back.getWidth());
		} finally {
			Files.deleteIfExists(png);
		}
	}

	// ---------------- WeightRandom ----------------

	@Test
	public void testWeightRandom() {
		WeightRandom<String> wr = new WeightRandom<>();
		wr.add("A", 9);
		wr.add("B", 1);
		Assert.assertEquals(2, wr.size());
		int a = 0;
		int b = 0;
		for (int i = 0; i < 10000; i++) {
			if ("A".equals(wr.next())) {
				a++;
			} else {
				b++;
			}
		}
		Assert.assertTrue("A 应占绝对多数: " + a, a > 8000);
		Assert.assertTrue("B 应偶尔命中: " + b, b > 100);

		wr.clear();
		Assert.assertEquals(0, wr.size());
		Assert.assertNull(wr.next());
		try {
			wr.add("C", 0);
			Assert.fail("权重 0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- BoundedPriorityQueue ----------------

	@Test
	public void testBoundedPriorityQueue() {
		BoundedPriorityQueue<Integer> queue = new BoundedPriorityQueue<>(3);
		queue.offer(5);
		queue.offer(1);
		queue.offer(3);
		queue.offer(4); // 淘汰 1
		queue.offer(2); // 淘汰 2
		Assert.assertEquals(3, queue.size());
		Assert.assertTrue(queue.contains(5));
		Assert.assertTrue(queue.contains(4));
		Assert.assertTrue(queue.contains(3));
		Assert.assertFalse(queue.contains(1));
		Assert.assertFalse(queue.contains(2));
		Assert.assertEquals(3, queue.capacity());
		Assert.assertEquals(3, queue.toList().size());

		// 不大于队首的元素被拒绝
		BoundedPriorityQueue<Integer> q2 = new BoundedPriorityQueue<>(2);
		Assert.assertTrue(q2.offer(5));
		Assert.assertTrue(q2.offer(3));
		Assert.assertFalse(q2.offer(1)); // 1 不比 3 优
		Assert.assertTrue(q2.offer(9));
		Assert.assertEquals(2, q2.size());
		Assert.assertTrue(q2.contains(9));
		Assert.assertFalse(q2.contains(3));
	}

	// ---------------- Pair / Triple ----------------

	@Test
	public void testPairTriple() {
		Pair<String, Integer> pair = Pair.of("key", 42);
		Assert.assertEquals("key", pair.getLeft());
		Assert.assertEquals(Integer.valueOf(42), pair.getRight());
		Assert.assertEquals("key", pair.left());
		Assert.assertEquals(42, pair.right().intValue());

		Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
		Assert.assertEquals("a", triple.getLeft());
		Assert.assertEquals(Integer.valueOf(1), triple.getMiddle());
		Assert.assertEquals(Boolean.TRUE, triple.getRight());
	}
}
