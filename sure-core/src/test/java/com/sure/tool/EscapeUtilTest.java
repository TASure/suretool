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

import com.sure.tool.codec.EncodeUtil;
import com.sure.tool.util.EscapeUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * EscapeUtil / EncodeUtil 单元测试。
 */
public class EscapeUtilTest {

	@Test
	public void testEscape() {
		Assert.assertEquals("&lt;a&gt;&amp;&quot;&#39;", EscapeUtil.escape("<a>&\"'"));
		Assert.assertNull(EscapeUtil.escape(null));
		Assert.assertEquals("abc", EscapeUtil.escape("abc"));
		Assert.assertEquals("", EscapeUtil.escape(""));
	}

	@Test
	public void testUnescape() {
		Assert.assertEquals("<a>&\"'", EscapeUtil.unescape("&lt;a&gt;&amp;&quot;&#39;"));
		Assert.assertEquals("<b>", EscapeUtil.unescape("&lt;b&gt;"));
		Assert.assertNull(EscapeUtil.unescape(null));
	}

	@Test
	public void testEncode() {
		Assert.assertEquals("hello%20world", EncodeUtil.encode("hello world"));
		Assert.assertEquals("%E4%BD%A0%E5%A5%BD", EncodeUtil.encode("你好"));
		Assert.assertEquals("a-b_c.d~e", EncodeUtil.encode("a-b_c.d~e"));
		Assert.assertNull(EncodeUtil.encode(null));
	}

	@Test
	public void testDecode() {
		Assert.assertEquals("hello world", EncodeUtil.decode("hello%20world"));
		Assert.assertEquals("你好", EncodeUtil.decode("%E4%BD%A0%E5%A5%BD"));
		Assert.assertEquals("a b", EncodeUtil.decode("a+b"));
		Assert.assertNull(EncodeUtil.decode(null));
	}

	@Test
	public void testRoundTrip() {
		String[] samples = { "suretool", "中文混合 ABC-123", "a/b?c=d&e=f", "空格 与+号" };
		for (String sample : samples) {
			Assert.assertEquals(sample, EncodeUtil.decode(EncodeUtil.encode(sample)));
		}
	}
}