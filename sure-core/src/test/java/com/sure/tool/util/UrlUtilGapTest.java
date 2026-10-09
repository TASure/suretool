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
 * UrlUtil 覆盖率补测：null/非法 URI 与空参数守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class UrlUtilGapTest {

	@Test
	public void testToUriGuards() {
		Assert.assertNull(UrlUtil.toUri(null));
		Assert.assertNull(UrlUtil.toUri("ht tp://bad space ::"));
		Assert.assertNull(UrlUtil.getHost(null));
		Assert.assertEquals(-1, UrlUtil.getPort(null));
		Assert.assertNull(UrlUtil.getScheme(null));
		Assert.assertNull(UrlUtil.getPath(null));
	}

	@Test
	public void testParams() {
		Assert.assertTrue(UrlUtil.getParams(null).isEmpty());
		Assert.assertTrue(UrlUtil.getParams("http://x.com/noquery").isEmpty());
		Assert.assertTrue(UrlUtil.getParams("http://x.com/?").isEmpty());
		Assert.assertEquals("v", UrlUtil.getParams("http://x.com/?a=v").get("a"));
		Assert.assertTrue(UrlUtil.isHttp("http://x.com"));
		Assert.assertFalse(UrlUtil.isHttp(null));
	}
}
