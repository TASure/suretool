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
 * 批 52：Base62Util 编解码测试（已知向量 + 往返 + 前导零 + 非法输入）。
 *
 * @author suretool
 * @since 1.20.0
 */
public class Base62UtilTest {

	@Test
	public void testEncodeKnownVectors() {
		Assert.assertEquals("", Base62Util.encode(new byte[0]));
		Assert.assertEquals("7tQLFHz", Base62Util.encode("hello".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("SAPP", Base62Util.encode("foo".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("VytN8Wjy", Base62Util.encode("foobar".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testLeadingZeros() {
		Assert.assertEquals("0", Base62Util.encode(new byte[] {0}));
		Assert.assertEquals("00", Base62Util.encode(new byte[] {0, 0}));
		Assert.assertArrayEquals(new byte[] {0}, Base62Util.decode("0"));
		Assert.assertArrayEquals(new byte[] {0, 0}, Base62Util.decode("00"));
		Assert.assertArrayEquals(new byte[] {0, 0, 1}, Base62Util.decode("001"));
	}

	@Test
	public void testRoundTrip() {
		for (int length = 0; length <= 17; length++) {
			byte[] source = new byte[length];
			for (int i = 0; i < length; i++) {
				source[i] = (byte) (i * 29 + length);
			}
			String encoded = Base62Util.encode(source);
			Assert.assertArrayEquals("len=" + length, source, Base62Util.decode(encoded));
		}
		Assert.assertArrayEquals(new byte[] {(byte) 0x80}, Base62Util.decode(Base62Util.encode(new byte[] {(byte) 0x80})));
	}

	@Test
	public void testStringOverloads() {
		Assert.assertEquals("7tQLFHz", Base62Util.encode("hello", StandardCharsets.UTF_8));
		Assert.assertEquals("7tQLFHz", Base62Util.encode("hello", null));
		Assert.assertEquals("hello", Base62Util.decodeToString("7tQLFHz"));
		Assert.assertEquals("hello", Base62Util.decodeToString("7tQLFHz", null));
	}

	@Test
	public void testNullSafety() {
		Assert.assertNull(Base62Util.encode((byte[]) null));
		Assert.assertNull(Base62Util.encode((String) null, StandardCharsets.UTF_8));
		Assert.assertNull(Base62Util.decode(null));
		Assert.assertNull(Base62Util.decodeToString(null));
		Assert.assertNull(Base62Util.decodeToString(null, StandardCharsets.UTF_8));
	}

	@Test
	public void testIsBase62() {
		Assert.assertTrue(Base62Util.isBase62("7tQLFHz"));
		Assert.assertTrue(Base62Util.isBase62("0"));
		Assert.assertFalse(Base62Util.isBase62(""));
		Assert.assertFalse(Base62Util.isBase62(null));
		Assert.assertFalse(Base62Util.isBase62("abc-def"));
		Assert.assertFalse(Base62Util.isBase62("abc_def"));
		Assert.assertFalse(Base62Util.isBase62("abc def"));
		Assert.assertFalse(Base62Util.isBase62("中"));
	}

	@Test
	public void testDecodeInvalid() {
		try {
			Base62Util.decode("abc-def");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法字符
		}
		try {
			Base62Util.decode("abc def");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 空白非法
		}
		try {
			Base62Util.decode("中");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非 ASCII 字符
		}
	}
}
