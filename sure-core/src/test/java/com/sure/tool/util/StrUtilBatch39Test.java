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

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 39：StrUtil 新增高频方法测试（isAlpha/isAlphanumeric/isNumericSpace/isAsciiPrintable/
 * containsWhitespace/removeWhitespace/indexOfAny/lastIndexOfAny/replaceOnce/removeSurrounding/
 * truncate/splitPreserveAllTokens/stripStart/stripEnd）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class StrUtilBatch39Test {

	@Test
	public void testIsAlpha() {
		Assert.assertTrue(StrUtil.isAlpha("abc"));
		Assert.assertTrue(StrUtil.isAlpha("中文"));
		Assert.assertFalse(StrUtil.isAlpha("abc1"));
		Assert.assertFalse(StrUtil.isAlpha("a b"));
		Assert.assertFalse(StrUtil.isAlpha(null));
		Assert.assertFalse(StrUtil.isAlpha(""));
	}

	@Test
	public void testIsAlphanumeric() {
		Assert.assertTrue(StrUtil.isAlphanumeric("abc123"));
		Assert.assertTrue(StrUtil.isAlphanumeric("ABC"));
		Assert.assertFalse(StrUtil.isAlphanumeric("a-b"));
		Assert.assertFalse(StrUtil.isAlphanumeric("a b"));
		Assert.assertFalse(StrUtil.isAlphanumeric(null));
		Assert.assertFalse(StrUtil.isAlphanumeric(""));
	}

	@Test
	public void testIsNumericSpace() {
		Assert.assertTrue(StrUtil.isNumericSpace("12 345"));
		Assert.assertTrue(StrUtil.isNumericSpace("123"));
		Assert.assertFalse(StrUtil.isNumericSpace("12a"));
		Assert.assertFalse(StrUtil.isNumericSpace("1.2"));
		Assert.assertFalse(StrUtil.isNumericSpace(null));
		Assert.assertFalse(StrUtil.isNumericSpace(""));
	}

	@Test
	public void testIsAsciiPrintable() {
		Assert.assertTrue(StrUtil.isAsciiPrintable("Hello! 123"));
		Assert.assertFalse(StrUtil.isAsciiPrintable("中文"));
		Assert.assertFalse(StrUtil.isAsciiPrintable("a\u0000b"));
		Assert.assertFalse(StrUtil.isAsciiPrintable("a\u0080b"));
		Assert.assertFalse(StrUtil.isAsciiPrintable(null));
		Assert.assertFalse(StrUtil.isAsciiPrintable(""));
	}

	@Test
	public void testContainsWhitespace() {
		Assert.assertTrue(StrUtil.containsWhitespace("a b"));
		Assert.assertTrue(StrUtil.containsWhitespace("a\tb"));
		Assert.assertFalse(StrUtil.containsWhitespace("ab"));
		Assert.assertFalse(StrUtil.containsWhitespace(null));
		Assert.assertFalse(StrUtil.containsWhitespace(""));
	}

	@Test
	public void testRemoveWhitespace() {
		Assert.assertEquals("ab", StrUtil.removeWhitespace(" a b "));
		Assert.assertEquals("ab", StrUtil.removeWhitespace("a\t\nb"));
		Assert.assertEquals("", StrUtil.removeWhitespace("   "));
		Assert.assertNull(StrUtil.removeWhitespace(null));
		Assert.assertEquals("", StrUtil.removeWhitespace(""));
	}

	@Test
	public void testIndexOfAny() {
		Assert.assertEquals(3, StrUtil.indexOfAny("hello world", "world", "lo"));
		Assert.assertEquals(0, StrUtil.indexOfAny("hello", "he", "x"));
		Assert.assertEquals(-1, StrUtil.indexOfAny("hello", "zz"));
		Assert.assertEquals(-1, StrUtil.indexOfAny("hello", (CharSequence[]) null));
		Assert.assertEquals(-1, StrUtil.indexOfAny(null, "a"));
		Assert.assertEquals(-1, StrUtil.indexOfAny("hello", ""));
		Assert.assertEquals(1, StrUtil.indexOfAny("hello", null, "ell"));
	}

	@Test
	public void testLastIndexOfAny() {
		Assert.assertEquals(7, StrUtil.lastIndexOfAny("hello lo", "lo", "ell"));
		Assert.assertEquals(5, StrUtil.lastIndexOfAny("abcabc", "abc"));
		Assert.assertEquals(-1, StrUtil.lastIndexOfAny("hello", "zz"));
		Assert.assertEquals(-1, StrUtil.lastIndexOfAny("hello", (CharSequence[]) null));
		Assert.assertEquals(-1, StrUtil.lastIndexOfAny(null, "a"));
		Assert.assertEquals(-1, StrUtil.lastIndexOfAny("hello", ""));
	}

	@Test
	public void testReplaceOnce() {
		Assert.assertEquals("aa+bb-cc", StrUtil.replaceOnce("aa-bb-cc", "-", "+"));
		Assert.assertEquals("aa-bb-cc", StrUtil.replaceOnce("aa-bb-cc", ".", "+"));
		Assert.assertEquals("aa-bb-cc", StrUtil.replaceOnce("aa-bb-cc", "-", null));
		Assert.assertEquals("ab", StrUtil.replaceOnce("ab", null, "x"));
		Assert.assertNull(StrUtil.replaceOnce(null, "a", "b"));
		Assert.assertEquals("aB", StrUtil.replaceOnce("ab", "b", "B"));
	}

	@Test
	public void testRemoveSurrounding() {
		Assert.assertEquals("abc", StrUtil.removeSurrounding("\"abc\"", "\""));
		Assert.assertEquals("\"abc\"", StrUtil.removeSurrounding("\"abc\"", "'"));
		Assert.assertEquals("abc", StrUtil.removeSurrounding("[abc]", "[", "]"));
		Assert.assertEquals("[abc]", StrUtil.removeSurrounding("[abc]", "[", "["));
		Assert.assertNull(StrUtil.removeSurrounding(null, "[", "]"));
		Assert.assertNull(StrUtil.removeSurrounding(null, "["));
		Assert.assertEquals("abc", StrUtil.removeSurrounding("abc", null, "]"));
		Assert.assertEquals("abc", StrUtil.removeSurrounding("abc", "", "]"));
	}

	@Test
	public void testRemoveSurroundingIgnoreCase() {
		Assert.assertEquals("ABC", StrUtil.removeSurroundingIgnoreCase("[ABC]", "[", "]"));
		Assert.assertEquals("abc", StrUtil.removeSurroundingIgnoreCase("[abc]", "[", "]"));
		Assert.assertEquals("b", StrUtil.removeSurroundingIgnoreCase("abc", "A", "C"));
		Assert.assertEquals("xy", StrUtil.removeSurroundingIgnoreCase("xy", "a", "b"));
		Assert.assertNull(StrUtil.removeSurroundingIgnoreCase(null, "["));
		Assert.assertEquals("abc", StrUtil.removeSurroundingIgnoreCase("abc", null, "]"));
	}

	@Test
	public void testTruncate() {
		Assert.assertEquals("hello", StrUtil.truncate("hello world", 5));
		Assert.assertEquals("hello world", StrUtil.truncate("hello world", 11));
		Assert.assertEquals("", StrUtil.truncate("abc", 0));
		Assert.assertNull(StrUtil.truncate(null, 3));
		try {
			StrUtil.truncate("abc", -1);
			Assert.fail("应当抛 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 预期异常
		}
	}

	@Test
	public void testSplitPreserveAllTokens() {
		Assert.assertArrayEquals(new String[] {"", "a", "", "b", ""}, StrUtil.splitPreserveAllTokens(",a,,b,", ','));
		Assert.assertArrayEquals(new String[] {"a", "b"}, StrUtil.splitPreserveAllTokens("a,b", ','));
		Assert.assertArrayEquals(new String[] {"", ""}, StrUtil.splitPreserveAllTokens(",", ','));
		Assert.assertArrayEquals(new String[0], StrUtil.splitPreserveAllTokens("", ','));
		Assert.assertNull(StrUtil.splitPreserveAllTokens(null, ','));
	}

	@Test
	public void testStripStart() {
		Assert.assertEquals("abc__", StrUtil.stripStart("__abc__", "_"));
		Assert.assertEquals("abc", StrUtil.stripStart("   abc", null));
		Assert.assertEquals("__abc__", StrUtil.stripStart("__abc__", "x"));
		Assert.assertEquals("__abc__", StrUtil.stripStart("__abc__", ""));
		Assert.assertEquals("", StrUtil.stripStart("___", "_"));
		Assert.assertNull(StrUtil.stripStart(null, "_"));
	}

	@Test
	public void testStripEnd() {
		Assert.assertEquals("__abc", StrUtil.stripEnd("__abc__", "_"));
		Assert.assertEquals("abc", StrUtil.stripEnd("abc   ", null));
		Assert.assertEquals("__abc__", StrUtil.stripEnd("__abc__", "x"));
		Assert.assertEquals("__abc__", StrUtil.stripEnd("__abc__", ""));
		Assert.assertEquals("", StrUtil.stripEnd("___", "_"));
		Assert.assertNull(StrUtil.stripEnd(null, "_"));
	}
}
