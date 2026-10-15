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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 43：UrlUtil 深度规范化 / 协议剥离 / 同源 / 查询与锚点提取测试。
 *
 * @author suretool
 * @since 1.19.0
 */
public class UrlUtilBatch43Test {

	@Test
	public void testRemoveProtocol() {
		Assert.assertEquals("example.com/a", UrlUtil.removeProtocol("https://example.com/a"));
		Assert.assertEquals("example.com/a", UrlUtil.removeProtocol("http://example.com/a"));
		Assert.assertEquals("example.com/a", UrlUtil.removeProtocol("example.com/a"));
		Assert.assertNull(UrlUtil.removeProtocol(null));
	}

	@Test
	public void testNormalizePath() {
		Assert.assertEquals("/a/b", UrlUtil.normalizePath("/a/./b"));
		Assert.assertEquals("/b", UrlUtil.normalizePath("/a/../b"));
		Assert.assertEquals("/a/b", UrlUtil.normalizePath("/a//b"));
		Assert.assertEquals("/", UrlUtil.normalizePath("/"));
		Assert.assertEquals("/", UrlUtil.normalizePath("/a/.."));
		Assert.assertEquals("a/b", UrlUtil.normalizePath("a/b"));
		// 越界 .. 保持（栈空不再上移）
		Assert.assertEquals("../a", UrlUtil.normalizePath("../a"));
		Assert.assertEquals("/a", UrlUtil.normalizePath("/../../a"));
		Assert.assertNull(UrlUtil.normalizePath(null));
	}

	@Test
	public void testIsSameOrigin() {
		Assert.assertTrue(UrlUtil.isSameOrigin("https://a.com/x", "https://a.com/y"));
		Assert.assertTrue(UrlUtil.isSameOrigin("https://a.com:443/x", "https://a.com/y")); // 默认端口等价
		Assert.assertTrue(UrlUtil.isSameOrigin("http://a.com:80/x", "http://a.com/y"));
		Assert.assertFalse(UrlUtil.isSameOrigin("https://a.com", "https://b.com"));
		Assert.assertFalse(UrlUtil.isSameOrigin("https://a.com", "http://a.com"));
		Assert.assertFalse(UrlUtil.isSameOrigin("https://a.com:8080", "https://a.com"));
		Assert.assertFalse(UrlUtil.isSameOrigin("not-a-url", "https://a.com"));
		Assert.assertFalse(UrlUtil.isSameOrigin(null, "https://a.com"));
	}

	@Test
	public void testGetQuery() {
		Assert.assertEquals("b=1&c=2", UrlUtil.getQuery("https://example.com/a?b=1&c=2"));
		Assert.assertEquals("", UrlUtil.getQuery("https://example.com/a"));
		Assert.assertEquals("", UrlUtil.getQuery("https://example.com/a?"));
		Assert.assertNull(UrlUtil.getQuery(null));
	}

	@Test
	public void testGetFragment() {
		Assert.assertEquals("section2", UrlUtil.getFragment("https://example.com/a#section2"));
		Assert.assertEquals("", UrlUtil.getFragment("https://example.com/a"));
		Assert.assertEquals("", UrlUtil.getFragment("https://example.com/a#"));
		Assert.assertNull(UrlUtil.getFragment(null));
	}
}
