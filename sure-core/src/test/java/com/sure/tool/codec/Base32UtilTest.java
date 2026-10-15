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
 * 批 40：Base32Util 编解码测试（RFC 4648 已知向量 + 往返 + 边界）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class Base32UtilTest {

	@Test
	public void testEncodeRfc4648Vectors() {
		Assert.assertEquals("", Base32Util.encode(new byte[0]));
		Assert.assertEquals("MY======", Base32Util.encode("f".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("MZXQ====", Base32Util.encode("fo".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("MZXW6===", Base32Util.encode("foo".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("MZXW6YQ=", Base32Util.encode("foob".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("MZXW6YTB", Base32Util.encode("fooba".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("MZXW6YTBOI======", Base32Util.encode("foobar".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("NBSWY3DP", Base32Util.encode("hello".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testEncodeWithoutPadding() {
		Assert.assertEquals("MZXW6", Base32Util.encode("foo".getBytes(StandardCharsets.UTF_8), false));
		Assert.assertEquals("MZXW6===", Base32Util.encode("foo".getBytes(StandardCharsets.UTF_8), true));
	}

	@Test
	public void testDecodeVectors() {
		Assert.assertArrayEquals(new byte[0], Base32Util.decode(""));
		Assert.assertArrayEquals("f".getBytes(StandardCharsets.UTF_8), Base32Util.decode("MY======"));
		Assert.assertArrayEquals("fo".getBytes(StandardCharsets.UTF_8), Base32Util.decode("MZXQ===="));
		Assert.assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), Base32Util.decode("MZXW6==="));
		Assert.assertArrayEquals("foobar".getBytes(StandardCharsets.UTF_8), Base32Util.decode("MZXW6YTBOI======"));
		// 缺失 padding 也容忍
		Assert.assertArrayEquals("foobar".getBytes(StandardCharsets.UTF_8), Base32Util.decode("MZXW6YTBOI"));
	}

	@Test
	public void testDecodeString() {
		Assert.assertEquals("hello", Base32Util.decodeToString("NBSWY3DP"));
		Assert.assertEquals("hello", Base32Util.decodeToString("NBSWY3DP", StandardCharsets.UTF_8));
		// 空白容忍
		Assert.assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), Base32Util.decode("NB SWY3DP"));
	}

	@Test
	public void testRoundTrip() {
		for (int len = 0; len <= 17; len++) {
			byte[] src = new byte[len];
			for (int i = 0; i < len; i++) {
				src[i] = (byte) (i * 31 + len);
			}
			String enc = Base32Util.encode(src);
			Assert.assertArrayEquals("len=" + len, src, Base32Util.decode(enc));
			String encNoPad = Base32Util.encode(src, false);
			Assert.assertArrayEquals("len=" + len + " nopad", src, Base32Util.decode(encNoPad));
		}
	}

	@Test
	public void testEncodeString() {
		Assert.assertEquals("NBSWY3DP", Base32Util.encode("hello", StandardCharsets.UTF_8));
		Assert.assertNull(Base32Util.encode((String) null, StandardCharsets.UTF_8));
	}

	@Test
	public void testNullSafety() {
		Assert.assertNull(Base32Util.encode((byte[]) null));
		Assert.assertNull(Base32Util.encode((byte[]) null, true));
		Assert.assertNull(Base32Util.decode(null));
		Assert.assertNull(Base32Util.decodeToString(null));
	}

	@Test
	public void testIsBase32() {
		Assert.assertTrue(Base32Util.isBase32("NBSWY3DP"));
		Assert.assertTrue(Base32Util.isBase32("NB SWY3DP"));   // 空白容忍
		Assert.assertTrue(Base32Util.isBase32("MZXW6YTBOI======"));
		Assert.assertTrue(Base32Util.isBase32("MY======"));
		Assert.assertFalse(Base32Util.isBase32(""));
		Assert.assertFalse(Base32Util.isBase32(null));
		Assert.assertFalse(Base32Util.isBase32("NBSWY3D0"));   // 0 非法
		Assert.assertTrue(Base32Util.isBase32("NBSWY3DP="));   // 尾部 = 填充合法
		Assert.assertFalse(Base32Util.isBase32("NBSWY3D=1"));  // = 后跟非 = 非法
		Assert.assertFalse(Base32Util.isBase32("NBSWY3DPl"));  // l 非法
	}

	@Test
	public void testDecodeInvalid() {
		try {
			Base32Util.decode("A");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法长度
		}
		try {
			Base32Util.decode("NBSWY3D0");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法字符
		}
	}
}
