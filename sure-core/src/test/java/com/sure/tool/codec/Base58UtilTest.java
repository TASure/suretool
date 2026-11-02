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
 * 批 52：Base58Util 编解码测试（已知向量 + 往返 + 前导零 + 非法输入）。
 *
 * @author suretool
 * @since 1.20.0
 */
public class Base58UtilTest {

	@Test
	public void testEncodeKnownVectors() {
		Assert.assertEquals("", Base58Util.encode(new byte[0]));
		Assert.assertEquals("Cn8eVZg", Base58Util.encode("hello".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("bQbp", Base58Util.encode("foo".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("t1Zv2yaZ", Base58Util.encode("foobar".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testLeadingZeros() {
		Assert.assertEquals("1", Base58Util.encode(new byte[] {0}));
		Assert.assertEquals("11", Base58Util.encode(new byte[] {0, 0}));
		Assert.assertArrayEquals(new byte[] {0}, Base58Util.decode("1"));
		Assert.assertArrayEquals(new byte[] {0, 0}, Base58Util.decode("11"));
		Assert.assertArrayEquals(new byte[] {0, 0, 1}, Base58Util.decode("112"));
	}

	@Test
	public void testRoundTrip() {
		for (int length = 0; length <= 17; length++) {
			byte[] source = new byte[length];
			for (int i = 0; i < length; i++) {
				source[i] = (byte) (i * 31 + length);
			}
			String encoded = Base58Util.encode(source);
			Assert.assertArrayEquals("len=" + length, source, Base58Util.decode(encoded));
		}
		Assert.assertArrayEquals(new byte[] {(byte) 0x80}, Base58Util.decode(Base58Util.encode(new byte[] {(byte) 0x80})));
	}

	@Test
	public void testStringOverloads() {
		Assert.assertEquals("Cn8eVZg", Base58Util.encode("hello", StandardCharsets.UTF_8));
		Assert.assertEquals("Cn8eVZg", Base58Util.encode("hello", null));
		Assert.assertEquals("hello", Base58Util.decodeToString("Cn8eVZg"));
		Assert.assertEquals("hello", Base58Util.decodeToString("Cn8eVZg", null));
	}

	@Test
	public void testNullSafety() {
		Assert.assertNull(Base58Util.encode((byte[]) null));
		Assert.assertNull(Base58Util.encode((String) null, StandardCharsets.UTF_8));
		Assert.assertNull(Base58Util.decode(null));
		Assert.assertNull(Base58Util.decodeToString(null));
		Assert.assertNull(Base58Util.decodeToString(null, StandardCharsets.UTF_8));
	}

	@Test
	public void testIsBase58() {
		Assert.assertTrue(Base58Util.isBase58("Cn8eVZg"));
		Assert.assertTrue(Base58Util.isBase58("1"));
		Assert.assertFalse(Base58Util.isBase58(""));
		Assert.assertFalse(Base58Util.isBase58(null));
		Assert.assertFalse(Base58Util.isBase58("0"));   // 0 不在字母表
		Assert.assertFalse(Base58Util.isBase58("O"));   // O 不在字母表
		Assert.assertFalse(Base58Util.isBase58("I"));
		Assert.assertFalse(Base58Util.isBase58("l"));
		Assert.assertFalse(Base58Util.isBase58("Cn8e VZg"));
		Assert.assertFalse(Base58Util.isBase58("中"));
	}

	@Test
	public void testDecodeInvalid() {
		try {
			Base58Util.decode("0");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法字符
		}
		try {
			Base58Util.decode("Cn8e VZg");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 空白非法
		}
		try {
			Base58Util.decode("中");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非 ASCII 字符
		}
	}
}
