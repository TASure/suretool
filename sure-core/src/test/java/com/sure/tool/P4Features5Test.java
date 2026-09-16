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

import com.sure.tool.text.StrSimilarity;
import com.sure.tool.util.BitUtil;
import com.sure.tool.util.BytesUtil;
import com.sure.tool.util.HtmlUtil;
import com.sure.tool.util.NumberUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * P4（v0.2.0）第五批新增/增强单元测试：StrSimilarity / HtmlUtil / BytesUtil / BitUtil / NumberUtil / StrUtil。
 */
public class P4Features5Test {

	// ---------------- StrSimilarity ----------------

	@Test
	public void testStrSimilarity() {
		Assert.assertEquals(3, StrSimilarity.levenshtein("kitten", "sitting"));
		Assert.assertEquals(0, StrSimilarity.levenshtein("abc", "abc"));
		Assert.assertEquals(3, StrSimilarity.levenshtein("", "abc"));
		Assert.assertEquals(3, StrSimilarity.levenshtein("abc", null));

		Assert.assertEquals(1.0, StrSimilarity.similarity("abc", "abc"), 1e-9);
		Assert.assertEquals(0.0, StrSimilarity.similarity("", "abc"), 1e-9);
		Assert.assertEquals(1.0, StrSimilarity.similarity("", ""), 1e-9);
		Assert.assertTrue(StrSimilarity.similarity("kitten", "sitting") > 0.5);

		Assert.assertEquals(1.0, StrSimilarity.jaccard("abc", "abc"), 1e-9);
		Assert.assertEquals(0.0, StrSimilarity.jaccard("abc", "xyz"), 1e-9);
		Assert.assertEquals(1.0, StrSimilarity.jaccard("", ""), 1e-9);
		Assert.assertEquals(1.0, StrSimilarity.cosine("abc", "abc"), 1e-9);
		Assert.assertEquals(0.0, StrSimilarity.cosine("abc", "xyz"), 1e-9);
		Assert.assertTrue(StrSimilarity.cosine("hello", "hallo") > 0.5);
	}

	// ---------------- HtmlUtil ----------------

	@Test
	public void testHtmlUtil() {
		Assert.assertEquals("&lt;a href=&quot;x&quot;&gt;&amp;&#39;&lt;/a&gt;",
				HtmlUtil.escape("<a href=\"x\">&'</a>"));
		Assert.assertEquals("<a href=\"x\">&'</a>",
				HtmlUtil.unescape("&lt;a href=&quot;x&quot;&gt;&amp;&#39;&lt;/a&gt;"));
		Assert.assertEquals("a & b", HtmlUtil.unescape("a &amp; b"));
		Assert.assertNull(HtmlUtil.escape(null));
		Assert.assertNull(HtmlUtil.unescape(null));

		Assert.assertEquals("Hello world", HtmlUtil.cleanHtmlTag("<p>Hello <b>world</b></p>"));
		Assert.assertNull(HtmlUtil.cleanHtmlTag(null));

		Assert.assertEquals("<p>keep</p>bad()", HtmlUtil.removeTag("<p>keep</p><script>bad()</script>", "script"));
		Assert.assertEquals("<p>keep</p>bad()", HtmlUtil.removeTag("<p>keep</p><SCRIPT>bad()</SCRIPT>", "script"));
	}

	// ---------------- BytesUtil ----------------

	@Test
	public void testBytesUtil() {
		long l = 0x123456789ABCDEF0L;
		Assert.assertEquals(l, BytesUtil.bytesToLong(BytesUtil.longToBytes(l)));
		Assert.assertEquals("123456789abcdef0", BytesUtil.toHex(BytesUtil.longToBytes(l)));

		int i = 0x12345678;
		Assert.assertEquals(i, BytesUtil.bytesToInt(BytesUtil.intToBytes(i)));
		Assert.assertEquals("12345678", BytesUtil.toHex(BytesUtil.intToBytes(i)));

		short s = 0x1234;
		Assert.assertEquals(s, BytesUtil.bytesToShort(BytesUtil.shortToBytes(s)));

		byte[] a = {1, 2, 3};
		byte[] b = {4, 5};
		Assert.assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, BytesUtil.concat(a, b));
		Assert.assertArrayEquals(new byte[] {2, 3}, BytesUtil.slice(a, 1, 3));
		Assert.assertArrayEquals(new byte[] {3, 2, 1}, BytesUtil.reverse(a));
		Assert.assertEquals("4142", BytesUtil.toHex(new byte[] {0x41, 0x42}));
		Assert.assertEquals("sure", BytesUtil.toString(BytesUtil.toBytes("sure")));
		Assert.assertEquals("sure", new String(BytesUtil.toBytes("sure"), StandardCharsets.UTF_8));
		Assert.assertNull(BytesUtil.toString(null));
		Assert.assertNull(BytesUtil.toBytes(null));
		Assert.assertNull(BytesUtil.toHex(null));
	}

	// ---------------- BitUtil ----------------

	@Test
	public void testBitUtil() {
		int value = 0b1010; // 位1 和位3
		Assert.assertEquals(1, BitUtil.get(value, 1));
		Assert.assertEquals(0, BitUtil.get(value, 0));
		Assert.assertEquals(0b1110, BitUtil.set(value, 2));
		Assert.assertEquals(0b0010, BitUtil.clear(value, 3));
		Assert.assertEquals(0b1110, BitUtil.toggle(value, 2));
		Assert.assertEquals(0b0010, BitUtil.toggle(value, 3));
		Assert.assertEquals(0b01, BitUtil.getRange(0b11010, 1, 3)); // 0b11010 位[1,3)=位1(1),位2(0) → 0b01

		long lv = 1L << 40;
		Assert.assertEquals(1, BitUtil.get(lv, 40));
		Assert.assertEquals(lv | (1L << 3), BitUtil.set(lv, 3));
		Assert.assertEquals(0b101L, BitUtil.getRange(0b1101L, 0, 3));

		try {
			BitUtil.get(value, 32);
			Assert.fail("越界应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			BitUtil.getRange(value, 2, 1);
			Assert.fail("非法范围应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- NumberUtil 增强 ----------------

	@Test
	public void testNumberEnhance() {
		Assert.assertEquals("0.0001", NumberUtil.toStr(0.0001));
		Assert.assertEquals("123.45", NumberUtil.toStr(123.45));
		Assert.assertArrayEquals(new int[] {1, 2, 3}, NumberUtil.range(1, 4));
		Assert.assertArrayEquals(new int[] {1, 3, 5}, NumberUtil.range(1, 7, 2));
		Assert.assertArrayEquals(new int[0], NumberUtil.range(5, 5));
		Assert.assertEquals(120, NumberUtil.factorial(5));
		Assert.assertEquals(1, NumberUtil.factorial(0));
		try {
			NumberUtil.factorial(-1);
			Assert.fail("负数阶乘应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------- StrUtil 增强 ----------------

	@Test
	public void testStrEnhance() {
		Assert.assertEquals("138****5678", StrUtil.hide("13812345678", 3, 7, '*'));
		Assert.assertEquals("abc", StrUtil.hide("abc", 1, 1, '*'));
		Assert.assertNull(StrUtil.hide(null, 0, 2, '*'));
		Assert.assertEquals("a", StrUtil.subBetween("{a}", "{", "}"));
		Assert.assertNull(StrUtil.subBetween("abc", "{", "}"));
		Assert.assertEquals("", StrUtil.subBetween("{}", "{", "}"));

		Assert.assertTrue(StrUtil.isNumeric("3.14"));
		Assert.assertTrue(StrUtil.isNumeric("-10"));
		Assert.assertTrue(StrUtil.isNumeric("100"));
		Assert.assertFalse(StrUtil.isNumeric("abc"));
		Assert.assertFalse(StrUtil.isNumeric(""));
		Assert.assertFalse(StrUtil.isNumeric(null));

		Assert.assertEquals("abc3", StrUtil.removeAll("a1b2c3", "1", "2"));
		Assert.assertEquals("abc", StrUtil.removeAll("abc", (CharSequence[]) null));
		Assert.assertNull(StrUtil.removeAll(null, "x"));
	}
}
