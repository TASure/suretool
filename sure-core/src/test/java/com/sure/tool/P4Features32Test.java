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
 * P4（v0.2.0）第三十七批测试：Emoji 检测/移除、IPv6 脱敏、字符集转换、字节位读取。
 */
public class P4Features32Test {

	@Test
	public void testEmoji() {
		Assert.assertTrue(com.sure.tool.util.EmojiUtil.isEmoji("\uD83D\uDE00"));
		Assert.assertTrue(com.sure.tool.util.EmojiUtil.isEmoji("\u2603"));
		Assert.assertFalse(com.sure.tool.util.EmojiUtil.isEmoji("abc"));
		Assert.assertFalse(com.sure.tool.util.EmojiUtil.isEmoji(""));
		Assert.assertFalse(com.sure.tool.util.EmojiUtil.isEmoji(null));

		Assert.assertTrue(com.sure.tool.util.EmojiUtil.containsEmoji("hello \uD83D\uDE00 world"));
		Assert.assertTrue(com.sure.tool.util.EmojiUtil.containsEmoji("\u2B50"));
		Assert.assertFalse(com.sure.tool.util.EmojiUtil.containsEmoji("plain text"));
		Assert.assertFalse(com.sure.tool.util.EmojiUtil.containsEmoji(null));

		Assert.assertEquals("hello  world", com.sure.tool.util.EmojiUtil.removeAllEmojis("hello \uD83D\uDE00 world"));
		Assert.assertEquals("abc", com.sure.tool.util.EmojiUtil.removeAllEmojis("\u2603abc\u2B50"));
		Assert.assertEquals("x", com.sure.tool.util.EmojiUtil.removeAllEmojis("x"));
		Assert.assertNull(com.sure.tool.util.EmojiUtil.removeAllEmojis(null));
	}

	@Test
	public void testDesensitizedIpv6() {
		Assert.assertEquals("240e:****:6789",
				com.sure.tool.util.DesensitizedUtil.ipv6("240e:390:1234:5678:abcd:ef01:2345:6789"));
		Assert.assertEquals("::1", com.sure.tool.util.DesensitizedUtil.ipv6("::1"));
		Assert.assertNull(com.sure.tool.util.DesensitizedUtil.ipv6(null));
		Assert.assertEquals("", com.sure.tool.util.DesensitizedUtil.ipv6(""));
	}

	@Test
	public void testCharsetConvert() {
		Assert.assertEquals("abc",
				com.sure.tool.util.CharsetUtil.convert("abc", "UTF-8", "ISO-8859-1"));
		String gbk = new String("中文".getBytes(java.nio.charset.StandardCharsets.UTF_8),
				java.nio.charset.Charset.forName("GBK"));
		Assert.assertEquals("中文",
				com.sure.tool.util.CharsetUtil.convert(gbk, "GBK", "UTF-8"));
		Assert.assertEquals("same",
				com.sure.tool.util.CharsetUtil.convert("same", "UTF-8", "UTF-8"));
		Assert.assertNull(com.sure.tool.util.CharsetUtil.convert(null, "UTF-8", "GBK"));
	}

	@Test
	public void testBitGetFromBytes() {
		byte[] data = new byte[] { (byte) 0b1010_0000, 0b0000_0001 };
		Assert.assertEquals(1, com.sure.tool.util.BitUtil.get(data, 0));
		Assert.assertEquals(0, com.sure.tool.util.BitUtil.get(data, 1));
		Assert.assertEquals(1, com.sure.tool.util.BitUtil.get(data, 2));
		Assert.assertEquals(0, com.sure.tool.util.BitUtil.get(data, 7));
		Assert.assertEquals(0, com.sure.tool.util.BitUtil.get(data, 8));
		Assert.assertEquals(1, com.sure.tool.util.BitUtil.get(data, 15));
		try {
			com.sure.tool.util.BitUtil.get(data, 16);
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}
}
