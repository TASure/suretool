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

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 53a：摩斯电码编解码测试。
 *
 * @author suretool
 * @since 1.21.0
 */
public class MorseUtilTest {

	@Test
	public void testEncodeSos() {
		Assert.assertEquals("... --- ...", MorseUtil.encode("SOS"));
	}

	@Test
	public void testDecodeSos() {
		Assert.assertEquals("SOS", MorseUtil.decode("... --- ..."));
	}

	@Test
	public void testRoundtrip() {
		// Morse 本质不分大小写，encode 统一大写、decode 也返回大写
		String plain = "HELLO WORLD 123";
		String encoded = MorseUtil.encode(plain);
		Assert.assertEquals(plain, MorseUtil.decode(encoded));
	}

	@Test
	public void testEncodeEmpty() {
		Assert.assertEquals("", MorseUtil.encode(""));
	}

	@Test
	public void testDecodeEmpty() {
		Assert.assertEquals("", MorseUtil.decode(""));
	}

	@Test
	public void testNullSafety() {
		Assert.assertNull(MorseUtil.encode(null));
		Assert.assertNull(MorseUtil.decode(null));
	}

	@Test
	public void testMixedCaseRoundtrip() {
		String plain = "SureTool 2026!";
		// 编码时统一大写，解码返回大写
		String expectedUpper = plain.toUpperCase();
		String encoded = MorseUtil.encode(plain);
		Assert.assertEquals(expectedUpper, MorseUtil.decode(encoded));
	}

	@Test
	public void testUnicodeVariant() {
		// ● 和 — 应归一
		String variant = "●●● ——— ●●●";
		Assert.assertEquals("SOS", MorseUtil.decode(variant));
	}

	@Test
	public void testWordSeparator() {
		String plain = "HELLO WORLD";
		String encoded = MorseUtil.encode(plain);
		Assert.assertTrue(encoded.contains("   ")); // 三空格单词分隔
		Assert.assertEquals(plain, MorseUtil.decode(encoded));
	}
}
