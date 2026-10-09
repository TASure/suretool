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
 * StrUtil 批22 增强测试（对标 commons-text / commons-lang StringUtils）。
 */
public class StrUtilBatch22Test {

	/**
	 * abbreviate 超长尾部省略。
	 */
	@Test
	public void testAbbreviate() {
		Assert.assertEquals("hello", StrUtil.abbreviate("hello", 10));
		Assert.assertEquals("hel...", StrUtil.abbreviate("hello world", 6));
		Assert.assertEquals("hello...", StrUtil.abbreviate("hello world", 8));
		Assert.assertNull(StrUtil.abbreviate(null, 5));
		// maxWidth 小于 4 视为 4
		Assert.assertEquals("a...", StrUtil.abbreviate("abcdef", 2));
	}

	/**
	 * abbreviateMiddle 中间省略。
	 */
	@Test
	public void testAbbreviateMiddle() {
		Assert.assertEquals("abc...xyz", StrUtil.abbreviateMiddle("abcdefghijklmnopqrstuvwxyz", "...", 9));
		// 长度足够不省略
		Assert.assertEquals("abcd", StrUtil.abbreviateMiddle("abcd", "...", 6));
		// middle 长于目标长度时返回原字符串（与 commons-lang 一致）
		Assert.assertEquals("abcdef", StrUtil.abbreviateMiddle("abcdef", ".....", 3));
		Assert.assertNull(StrUtil.abbreviateMiddle(null, "...", 5));
	}

	/**
	 * substringBetween 单标签与双标签。
	 */
	@Test
	public void testSubstringBetween() {
		Assert.assertEquals("abc", StrUtil.substringBetween("tagabctag", "tag"));
		Assert.assertEquals("name", StrUtil.substringBetween("prefix [name] suffix", "[", "]"));
		Assert.assertNull(StrUtil.substringBetween("no tag here", "<a>"));
		Assert.assertNull(StrUtil.substringBetween(null, "<a>"));
		Assert.assertNull(StrUtil.substringBetween("x", "", "y"));
	}

	/**
	 * difference 首个差异起点子串。
	 */
	@Test
	public void testDifference() {
		Assert.assertEquals(" world", StrUtil.difference("hello", "hello world"));
		Assert.assertEquals("xyz", StrUtil.difference("abc", "abcxyz"));
		Assert.assertEquals("", StrUtil.difference("same", "same"));
		Assert.assertEquals("target", StrUtil.difference(null, "target"));
	}

	/**
	 * indexOfIgnoreCase 忽略大小写定位。
	 */
	@Test
	public void testIndexOfIgnoreCase() {
		Assert.assertEquals(6, StrUtil.indexOfIgnoreCase("Hello World", "world", 0));
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase("hello", "world", 0));
		Assert.assertEquals(3, StrUtil.indexOfIgnoreCase("ABCabc", "ABC", 1));
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase(null, "x", 0));
		Assert.assertEquals(0, StrUtil.indexOfIgnoreCase("abc", "", 0));
	}

	/**
	 * swapCase 大小写互换。
	 */
	@Test
	public void testSwapCase() {
		Assert.assertEquals("hELLO wORLD", StrUtil.swapCase("Hello World"));
		Assert.assertEquals("123", StrUtil.swapCase("123"));
		Assert.assertEquals("", StrUtil.swapCase(""));
		Assert.assertNull(StrUtil.swapCase(null));
	}

	/**
	 * normalizeSpace 空白压缩。
	 */
	@Test
	public void testNormalizeSpace() {
		Assert.assertEquals("a b c", StrUtil.normalizeSpace("  a   b\tc\n"));
		Assert.assertEquals("single", StrUtil.normalizeSpace("  single  "));
		Assert.assertEquals("", StrUtil.normalizeSpace("   "));
		Assert.assertNull(StrUtil.normalizeSpace(null));
	}

	/**
	 * isNumeric 纯数字判定。
	 */
	@Test
	public void testIsNumeric() {
		Assert.assertTrue(StrUtil.isNumeric("12345"));
		Assert.assertFalse(StrUtil.isNumeric("12a45"));
		Assert.assertFalse(StrUtil.isNumeric(""));
		Assert.assertFalse(StrUtil.isNumeric(null));
		Assert.assertTrue(StrUtil.isNumeric("-12.5"));
	}

	/**
	 * getCommonPrefix 公共前缀。
	 */
	@Test
	public void testGetCommonPrefix() {
		Assert.assertEquals("abc", StrUtil.getCommonPrefix("abcdef", "abcxyz", "abc123"));
		Assert.assertEquals("", StrUtil.getCommonPrefix("a", "b"));
		Assert.assertEquals("", StrUtil.getCommonPrefix());
		Assert.assertEquals("", StrUtil.getCommonPrefix("x", null));
		Assert.assertEquals("sure-", StrUtil.getCommonPrefix("sure-core", "sure-all"));
	}

	/**
	 * rotate 循环移位。
	 */
	@Test
	public void testRotate() {
		Assert.assertEquals("cab", StrUtil.rotate("abc", 1));
		Assert.assertEquals("abc", StrUtil.rotate("abc", 3));
		Assert.assertEquals("bca", StrUtil.rotate("abc", -1));
		Assert.assertEquals("abc", StrUtil.rotate("abc", 0));
		Assert.assertNull(StrUtil.rotate(null, 2));
		Assert.assertEquals("", StrUtil.rotate("", 5));
	}
}
