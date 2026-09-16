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

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第三十六批测试：字符转字符串/Emoji 判断、URL 规范化。
 */
public class P4Features31Test {

	@Test
	public void testCharToStringAndEmoji() {
		Assert.assertEquals("a", com.sure.tool.util.CharUtil.toString('a'));
		Assert.assertEquals("中", com.sure.tool.util.CharUtil.toString('中'));
		Assert.assertEquals(" ", com.sure.tool.util.CharUtil.toString(' '));

		Assert.assertTrue(com.sure.tool.util.CharUtil.isEmoji('\u2B50'));
		Assert.assertTrue(com.sure.tool.util.CharUtil.isEmoji('\u2603'));
		Assert.assertFalse(com.sure.tool.util.CharUtil.isEmoji('a'));
		Assert.assertFalse(com.sure.tool.util.CharUtil.isEmoji('1'));
		Assert.assertFalse(com.sure.tool.util.CharUtil.isEmoji('中'));

		Assert.assertTrue(com.sure.tool.util.CharUtil.isEmoji(0x1F600));
		Assert.assertTrue(com.sure.tool.util.CharUtil.isEmoji(0x1F300));
		Assert.assertTrue(com.sure.tool.util.CharUtil.isEmoji(0x2B50));
		Assert.assertFalse(com.sure.tool.util.CharUtil.isEmoji(0x4E2D));
	}

	@Test
	public void testUrlNormalize() {
		Assert.assertEquals("http://example.com", com.sure.tool.util.UrlUtil.normalize("example.com"));
		Assert.assertEquals("http://example.com", com.sure.tool.util.UrlUtil.normalize("example.com/"));
		Assert.assertEquals("https://example.com/a/b", com.sure.tool.util.UrlUtil.normalize("https://example.com/a/b/"));
		Assert.assertEquals("http://example.com/a", com.sure.tool.util.UrlUtil.normalize("  example.com/a/ "));
		Assert.assertNull(com.sure.tool.util.UrlUtil.normalize(null));
		Assert.assertEquals("", com.sure.tool.util.UrlUtil.normalize(""));
		Assert.assertEquals("ftp://example.com", com.sure.tool.util.UrlUtil.normalize("ftp://example.com/"));
	}


}
