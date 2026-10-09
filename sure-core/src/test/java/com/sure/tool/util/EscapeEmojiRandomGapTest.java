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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * EscapeUtil / EmojiUtil / RandomUtil 覆盖率补测：null 守卫与日期区间。
 *
 * @author suretool
 * @since 1.13.1
 */
public class EscapeEmojiRandomGapTest {

	@Test
	public void testNullGuards() {
		Assert.assertNull(EscapeUtil.escapeXml(null));
		Assert.assertNull(EscapeUtil.unescapeXml(null));
		Assert.assertEquals("&lt;a&gt;", EscapeUtil.escapeXml("<a>"));
		Assert.assertEquals("<a>", EscapeUtil.unescapeXml("&lt;a&gt;"));
		Assert.assertFalse(EmojiUtil.containsEmoji(null));
		Assert.assertFalse(EmojiUtil.containsEmoji("plain"));
		Date now = new Date();
		Date r = RandomUtil.randomDay(new Date(now.getTime() - 10000), now);
		Assert.assertNotNull(r);
	}
}
