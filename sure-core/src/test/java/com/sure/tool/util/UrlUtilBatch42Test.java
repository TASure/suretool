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

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 42：UrlUtil HTTPS / 校验 / URL 对象 / 域名 / 协议补全测试。
 *
 * @author suretool
 * @since 1.19.0
 */
public class UrlUtilBatch42Test {

	@Test
	public void testIsHttps() {
		Assert.assertTrue(UrlUtil.isHttps("https://example.com"));
		Assert.assertTrue(UrlUtil.isHttps("HTTPS://example.com"));
		Assert.assertFalse(UrlUtil.isHttps("http://example.com"));
		Assert.assertFalse(UrlUtil.isHttps(null));
	}

	@Test
	public void testIsValidUrl() {
		Assert.assertTrue(UrlUtil.isValidUrl("https://example.com/a?b=1"));
		Assert.assertTrue(UrlUtil.isValidUrl("http://example.com"));
		Assert.assertFalse(UrlUtil.isValidUrl("ftp://example.com"));
		Assert.assertFalse(UrlUtil.isValidUrl("not a url"));
		Assert.assertFalse(UrlUtil.isValidUrl(""));
		Assert.assertFalse(UrlUtil.isValidUrl(null));
	}

	@Test
	public void testToUrl() throws Exception {
		URL url = UrlUtil.toUrl("https://example.com/path");
		Assert.assertEquals("https", url.getProtocol());
		Assert.assertEquals("example.com", url.getHost());
		Assert.assertNull(UrlUtil.toUrl(null));
		try {
			UrlUtil.toUrl("not a url");
			Assert.fail("should throw");
		} catch (RuntimeException expected) {
			// 非法 URL
		}
	}

	@Test
	public void testGetDomain() {
		Assert.assertEquals("www.example.com",
				UrlUtil.getDomain("https://www.example.com:8080/path?a=1"));
		Assert.assertEquals("example.com", UrlUtil.getDomain("http://example.com/"));
		Assert.assertNull(UrlUtil.getDomain(null));
	}

	@Test
	public void testToHttpUrl() {
		Assert.assertEquals("http://example.com/a", UrlUtil.toHttpUrl("example.com/a"));
		Assert.assertEquals("https://example.com", UrlUtil.toHttpUrl("https://example.com"));
		Assert.assertEquals("http://example.com", UrlUtil.toHttpUrl("http://example.com"));
		Assert.assertNull(UrlUtil.toHttpUrl(null));
		Assert.assertEquals("", UrlUtil.toHttpUrl("  "));
	}
}
