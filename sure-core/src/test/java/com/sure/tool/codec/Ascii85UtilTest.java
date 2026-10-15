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
package com.sure.tool.codec;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 40：Ascii85Util 编解码测试（已知向量 + 往返 + z 压缩 + 包裹容错）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class Ascii85UtilTest {

	@Test
	public void testEncodeKnownVectors() {
		Assert.assertEquals("", Ascii85Util.encode(new byte[0]));
		// 4 个零字节压缩为 z
		Assert.assertEquals("z", Ascii85Util.encode(new byte[4]));
		// 单字节 0x00 -> "!!"
		Assert.assertEquals("!!", Ascii85Util.encode(new byte[1]));
		// RFC 1924 示例：空字符串
		Assert.assertEquals("", Ascii85Util.encode("".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTrip() {
		for (int len = 0; len <= 18; len++) {
			byte[] src = new byte[len];
			for (int i = 0; i < len; i++) {
				src[i] = (byte) (i * 13 + len * 3);
			}
			Assert.assertArrayEquals("len=" + len, src, Ascii85Util.decode(Ascii85Util.encode(src)));
		}
	}

	@Test
	public void testEncodeDecodeText() {
		String text = "Hello world";
		String enc = Ascii85Util.encode(text, StandardCharsets.UTF_8);
		Assert.assertEquals("Hello world", Ascii85Util.decodeToString(enc));
		// 包裹容错
		Assert.assertEquals("Hello world", Ascii85Util.decodeToString("<~" + enc + "~>"));
	}

	@Test
	public void testDecodeWrapped() {
		byte[] src = "suretool".getBytes(StandardCharsets.UTF_8);
		String enc = Ascii85Util.encode(src);
		Assert.assertArrayEquals(src, Ascii85Util.decode("<~" + enc + "~>"));
		Assert.assertArrayEquals(src, Ascii85Util.decode(enc));
		// z 零块解压
		Assert.assertArrayEquals(new byte[4], Ascii85Util.decode("z"));
		Assert.assertArrayEquals(new byte[8], Ascii85Util.decode("zz"));
	}

	@Test
	public void testNullSafety() {
		Assert.assertNull(Ascii85Util.encode((byte[]) null));
		Assert.assertNull(Ascii85Util.encode((String) null, StandardCharsets.UTF_8));
		Assert.assertNull(Ascii85Util.decode(null));
		Assert.assertNull(Ascii85Util.decodeToString(null));
	}

	@Test
	public void testIsAscii85() {
		String enc = Ascii85Util.encode("suretool", StandardCharsets.UTF_8);
		Assert.assertTrue(Ascii85Util.isAscii85(enc));
		Assert.assertTrue(Ascii85Util.isAscii85("<~" + enc + "~>"));
		Assert.assertTrue(Ascii85Util.isAscii85("z"));
		Assert.assertFalse(Ascii85Util.isAscii85(""));
		Assert.assertFalse(Ascii85Util.isAscii85(null));
		Assert.assertFalse(Ascii85Util.isAscii85("a b"));  // 空格 32 非法
		Assert.assertFalse(Ascii85Util.isAscii85("~"));
	}

	@Test
	public void testDecodeInvalid() {
		try {
			Ascii85Util.decode("a b");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 空格非法
		}
	}
}
