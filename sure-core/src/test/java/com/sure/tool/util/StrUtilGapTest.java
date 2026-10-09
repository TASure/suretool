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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * StrUtil 覆盖率补测：专打 null / 空串 / 边界 / 异常分支，命中既有用例未触及的防御路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class StrUtilGapTest {

	@Test
	public void testHasBlankAndIsAllBlankAndHasEmpty() {
		// hasBlank(null) -> true
		Assert.assertTrue(StrUtil.hasBlank((CharSequence[]) null));
		Assert.assertTrue(StrUtil.hasBlank("abc", ""));
		Assert.assertFalse(StrUtil.hasBlank("abc", "def"));
		// isAllBlank(null) -> true
		Assert.assertTrue(StrUtil.isAllBlank((CharSequence[]) null));
		Assert.assertTrue(StrUtil.isAllBlank("  ", ""));
		Assert.assertFalse(StrUtil.isAllBlank("abc", "  "));
		// hasEmpty(null) -> true；hasEmpty 全非空 -> false
		Assert.assertTrue(StrUtil.hasEmpty((CharSequence[]) null));
		Assert.assertTrue(StrUtil.hasEmpty("abc", null));
		Assert.assertFalse(StrUtil.hasEmpty("abc", "def"));
	}

	@Test
	public void testContainsAndContainsIgnoreCaseAndAny() {
		Assert.assertFalse(StrUtil.contains(null, "a"));
		Assert.assertFalse(StrUtil.contains("abc", null));
		Assert.assertTrue(StrUtil.contains("abcdef", "cd"));
		Assert.assertFalse(StrUtil.containsIgnoreCase(null, "A"));
		Assert.assertFalse(StrUtil.containsIgnoreCase("abc", null));
		Assert.assertTrue(StrUtil.containsIgnoreCase("ABCdef", "cd"));
		Assert.assertFalse(StrUtil.containsAny(null, "a"));
		Assert.assertFalse(StrUtil.containsAny("abc", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.containsAny("abcdef", "xx", "cd"));
		Assert.assertFalse(StrUtil.containsAny("abc", "xx", "yy"));
	}

	@Test
	public void testStartWithEndWithNullAndShort() {
		Assert.assertFalse(StrUtil.startWith(null, "a"));
		Assert.assertFalse(StrUtil.startWith("a", null));
		Assert.assertFalse(StrUtil.startWith("ab", "abc"));
		Assert.assertTrue(StrUtil.startWith("abc", "ab"));
		Assert.assertTrue(StrUtil.startWithIgnoreCase("ABC", "ab"));
		Assert.assertFalse(StrUtil.endWith(null, "a"));
		Assert.assertFalse(StrUtil.endWith("a", null));
		Assert.assertFalse(StrUtil.endWith("ab", "abc"));
		Assert.assertTrue(StrUtil.endWith("abc", "bc"));
		Assert.assertTrue(StrUtil.endWithIgnoreCase("ABC", "bc"));
	}

	@Test
	public void testSubEdge() {
		Assert.assertEquals("ab", StrUtil.sub("abc", -100, 2));
		Assert.assertEquals("abc", StrUtil.sub("abc", 0, 5));
		Assert.assertEquals("c", StrUtil.sub("abc", -1));
		Assert.assertEquals("", StrUtil.sub("abc", 3, 1));
		Assert.assertEquals("", StrUtil.sub(null, 0, 1));
		Assert.assertEquals("abc", StrUtil.sub("abc", -100));
	}

	@Test
	public void testRemovePrefixSuffix() {
		// CharSequence 重载：null / 空 / 命中 / 未命中
		Assert.assertEquals("", StrUtil.removePrefix((CharSequence) null, "x"));
		Assert.assertEquals("abc", StrUtil.removePrefix((CharSequence) "abc", (CharSequence) ""));
		Assert.assertEquals("c", StrUtil.removePrefix((CharSequence) "abc", (CharSequence) "ab"));
		Assert.assertEquals("abc", StrUtil.removePrefix((CharSequence) "abc", (CharSequence) "xx"));
		Assert.assertEquals("", StrUtil.removePrefixIgnoreCase((CharSequence) null, "x"));
		Assert.assertEquals("abc", StrUtil.removePrefixIgnoreCase((CharSequence) "abc", (CharSequence) ""));
		Assert.assertEquals("C", StrUtil.removePrefixIgnoreCase((CharSequence) "ABC", (CharSequence) "ab"));
		Assert.assertEquals("abc", StrUtil.removePrefixIgnoreCase((CharSequence) "abc", (CharSequence) "xx"));
		Assert.assertEquals("", StrUtil.removeSuffix((CharSequence) null, "x"));
		Assert.assertEquals("abc", StrUtil.removeSuffix("abc", ""));
		Assert.assertEquals("a", StrUtil.removeSuffix((CharSequence) "abc", (CharSequence) "bc"));
		Assert.assertEquals("abc", StrUtil.removeSuffix((CharSequence) "abc", (CharSequence) "xx"));
		Assert.assertEquals("", StrUtil.removeSuffixIgnoreCase((CharSequence) null, "x"));
		Assert.assertEquals("abc", StrUtil.removeSuffixIgnoreCase((CharSequence) "abc", (CharSequence) ""));
		Assert.assertEquals("A", StrUtil.removeSuffixIgnoreCase((CharSequence) "ABC", (CharSequence) "bc"));
		Assert.assertEquals("abc", StrUtil.removeSuffixIgnoreCase((CharSequence) "abc", (CharSequence) "xx"));
		// String 重载：null 原样返回
		Assert.assertNull(StrUtil.removePrefix(null, "x"));
		Assert.assertNull(StrUtil.removeSuffix(null, "x"));
		Assert.assertEquals("c", StrUtil.removePrefix("abc", "ab"));
		Assert.assertEquals("a", StrUtil.removeSuffix("abc", "bc"));
	}

	@Test
	public void testReplaceBranches() {
		Assert.assertEquals("", StrUtil.replace(null, "a", "b"));
		Assert.assertEquals("abc", StrUtil.replace("abc", null, "b"));
		Assert.assertEquals("abc", StrUtil.replace("abc", "", "x"));
		Assert.assertEquals("ac", StrUtil.replace("abc", "b", null));
		Assert.assertEquals("aXXc", StrUtil.replace("abc", "b", "XX"));
		// 区间替换
		Assert.assertNull(StrUtil.replace(null, 0, 1, "x"));
		Assert.assertEquals("abc", StrUtil.replace("abc", 5, 1, "x"));
		Assert.assertEquals("aXXc", StrUtil.replace("abc", 1, 2, "XX"));
		Assert.assertEquals("ac", StrUtil.replace("abc", 1, 2, null));
		// 忽略大小写替换
		Assert.assertNull(StrUtil.replaceIgnoreCase(null, "a", "b"));
		Assert.assertEquals("abc", StrUtil.replaceIgnoreCase("abc", "", "x"));
		Assert.assertEquals("aXXc", StrUtil.replaceIgnoreCase("aBc", "b", "XX"));
		Assert.assertEquals("ac", StrUtil.replaceIgnoreCase("abc", "b", null));
	}

	@Test
	public void testJoinAndRepeatAndPad() {
		Assert.assertEquals("", StrUtil.join(",", (Object[]) null));
		Assert.assertEquals("", StrUtil.join(",", (Iterable<?>) null));
		Assert.assertEquals("a,b", StrUtil.join(",", "a", "b"));
		Assert.assertEquals("a,b", StrUtil.join(",", Arrays.asList("a", "b")));
		Assert.assertEquals("", StrUtil.repeat(null, 3));
		Assert.assertEquals("", StrUtil.repeat("a", 0));
		Assert.assertEquals("aaa", StrUtil.repeat("a", 3));
		Assert.assertEquals("", StrUtil.padPre(null, 5, '0'));
		Assert.assertEquals("abc", StrUtil.padPre("abc", 3, '0'));
		Assert.assertEquals("00abc", StrUtil.padPre("abc", 5, '0'));
		Assert.assertEquals("", StrUtil.padAfter(null, 5, '0'));
		Assert.assertEquals("abc", StrUtil.padAfter("abc", 3, '0'));
		Assert.assertEquals("abc00", StrUtil.padAfter("abc", 5, '0'));
	}

	@Test
	public void testSplitVariants() {
		Assert.assertArrayEquals(new String[0], StrUtil.split("", ','));
		Assert.assertArrayEquals(new String[] {"a", "b", ""}, StrUtil.split("a,b,", ','));
		Assert.assertArrayEquals(new String[] {""}, StrUtil.split(null, ","));
		Assert.assertArrayEquals(new String[] {"abc"}, StrUtil.split("abc", ""));
		Assert.assertArrayEquals(new String[] {"a", "b"}, StrUtil.split("a.b", "."));
		// splitTrim
		Assert.assertArrayEquals(new String[0], StrUtil.splitTrim("", ","));
		Assert.assertArrayEquals(new String[] {"abc"}, StrUtil.splitTrim("  abc  ", ""));
		Assert.assertArrayEquals(new String[] {"a", "", "b"}, StrUtil.splitTrim(" a ,,b ", ","));
	}

	@Test
	public void testDefaultAndNullConversions() {
		Assert.assertEquals("def", StrUtil.blankToDefault((CharSequence) null, "def"));
		Assert.assertEquals("abc", StrUtil.blankToDefault((CharSequence) "abc", "def"));
		Assert.assertEquals("def", StrUtil.blankToDefault("  ", "def"));
		Assert.assertEquals("abc", StrUtil.blankToDefault("abc", "def"));
		Assert.assertEquals("def", StrUtil.emptyToDefault("", "def"));
		Assert.assertEquals("abc", StrUtil.emptyToDefault("abc", "def"));
		Assert.assertEquals("", StrUtil.nullToEmpty(null));
		Assert.assertNull(StrUtil.emptyToNull(""));
		Assert.assertEquals("abc", StrUtil.emptyToNull("abc"));
	}

	@Test
	public void testSubBetweenAndAll() {
		Assert.assertNull(StrUtil.subBetween(null, "a", "b"));
		Assert.assertNull(StrUtil.subBetween("x[a]y", null, "b"));
		Assert.assertNull(StrUtil.subBetween("x[a]y", "a", null));
		Assert.assertNull(StrUtil.subBetween("x", "[", "]"));
		Assert.assertNull(StrUtil.subBetween("[abc", "[", "]"));
		Assert.assertEquals("abc", StrUtil.subBetween("[abc]", "[", "]"));
		// subBetweenAll
		Assert.assertEquals(0, StrUtil.subBetweenAll("", "[", "]").size());
		Assert.assertEquals(0, StrUtil.subBetweenAll("abc", null, "]").size());
		Assert.assertEquals(Arrays.asList("a", "b"), StrUtil.subBetweenAll("[a]-[b]", "[", "]"));
		Assert.assertEquals(Arrays.asList("a"), StrUtil.subBetweenAll("[a]-[", "[", "]"));
	}

	@Test
	public void testRemoveAllAndIndexOf() {
		Assert.assertNull(StrUtil.removeAll(null, "a"));
		Assert.assertEquals("abc", StrUtil.removeAll("abc", (CharSequence[]) null));
		Assert.assertEquals("bc", StrUtil.removeAll("abc", "a", null, ""));
		Assert.assertEquals("ace", StrUtil.removeAll("abcde", "b", "d"));
		Assert.assertEquals(-1, StrUtil.indexOf(null, "a"));
		Assert.assertEquals(-1, StrUtil.indexOf("abc", null));
		Assert.assertEquals(-1, StrUtil.indexOf("abc", ""));
		Assert.assertEquals(1, StrUtil.indexOf("abc", "b"));
		Assert.assertEquals(-1, StrUtil.lastIndexOf(null, "a"));
		Assert.assertEquals(-1, StrUtil.lastIndexOf("abc", ""));
		Assert.assertEquals(5, StrUtil.lastIndexOf("abcabc", "c"));
	}

	@Test
	public void testStartWithAnyEndWithAnyEqualsAny() {
		Assert.assertFalse(StrUtil.startWithAny("abc"));
		Assert.assertFalse(StrUtil.startWithAny("abc", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.startWithAny("abc", "xx", "ab"));
		Assert.assertFalse(StrUtil.endWithAny("abc"));
		Assert.assertFalse(StrUtil.endWithAny("abc", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.endWithAny("abc", "xx", "bc"));
		Assert.assertFalse(StrUtil.equalsAny("a"));
		Assert.assertFalse(StrUtil.equalsAny("a", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.equalsAny("a", "x", "a"));
		Assert.assertFalse(StrUtil.equalsAnyIgnoreCase("A"));
		Assert.assertFalse(StrUtil.equalsAnyIgnoreCase("A", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.equalsAnyIgnoreCase("A", "x", "a"));
	}

	@Test
	public void testSplitToNumberArrays() {
		Assert.assertArrayEquals(new int[0], StrUtil.splitToIntArray(""));
		Assert.assertArrayEquals(new int[] {1, 0, 3}, StrUtil.splitToIntArray("1 xx 3"));
		Assert.assertArrayEquals(new long[0], StrUtil.splitToLongArray(""));
		Assert.assertArrayEquals(new long[] {1L, 0L}, StrUtil.splitToLongArray("1 yy"));
		Assert.assertEquals(0, StrUtil.splitToDoubleArray("").length);
		Assert.assertArrayEquals(new double[] {1.5d, 0d}, StrUtil.splitToDoubleArray("1.5 zz"), 0.0001d);
	}

	@Test
	public void testSubSufSubPre() {
		Assert.assertNull(StrUtil.subSuf(null, 3));
		Assert.assertEquals("", StrUtil.subSuf("abc", 0));
		Assert.assertEquals("bc", StrUtil.subSuf("abc", 2));
		Assert.assertEquals("abc", StrUtil.subSuf("abc", 5));
		Assert.assertNull(StrUtil.subPre(null, 3));
		Assert.assertEquals("", StrUtil.subPre("abc", 0));
		Assert.assertEquals("ab", StrUtil.subPre("abc", 2));
		Assert.assertEquals("abc", StrUtil.subPre("abc", 5));
	}

	@Test
	public void testIsUpperLower() {
		Assert.assertFalse(StrUtil.isUpperCase(""));
		Assert.assertFalse(StrUtil.isUpperCase("abc"));
		Assert.assertTrue(StrUtil.isUpperCase("AB1"));
		Assert.assertFalse(StrUtil.isLowerCase(""));
		Assert.assertFalse(StrUtil.isLowerCase("ABC"));
		Assert.assertTrue(StrUtil.isLowerCase("ab1"));
	}

	@Test
	public void testFillBeforeAfter() {
		Assert.assertNull(StrUtil.fillBefore(null, 5, '0'));
		Assert.assertEquals("abc", StrUtil.fillBefore("abc", 3, '0'));
		Assert.assertEquals("00abc", StrUtil.fillBefore("abc", 5, '0'));
		Assert.assertNull(StrUtil.fillAfter(null, 5, '0'));
		Assert.assertEquals("abc", StrUtil.fillAfter("abc", 3, '0'));
		Assert.assertEquals("abc00", StrUtil.fillAfter("abc", 5, '0'));
	}

	@Test
	public void testToUnicodeMaxLengthStrip() {
		Assert.assertNull(StrUtil.toUnicode(null));
		Assert.assertEquals("abc", StrUtil.toUnicode("abc"));
		Assert.assertEquals("\\u4e2d", StrUtil.toUnicode("中"));
		Assert.assertNull(StrUtil.maxLength(null, 5));
		Assert.assertEquals("", StrUtil.maxLength("abcdef", 0));
		Assert.assertEquals("abcdef", StrUtil.maxLength("abcdef", 10));
		Assert.assertEquals("abc", StrUtil.maxLength("abcdef", 3));
		Assert.assertEquals("ab...", StrUtil.maxLength("abcdef", 5));
		Assert.assertNull(StrUtil.strip(null, 'x'));
		Assert.assertEquals("abc", StrUtil.strip("xxabcxx", 'x'));
		Assert.assertEquals("abc", StrUtil.strip("  abc  "));
	}

	@Test
	public void testCenterFirstNonBlankConcat() {
		Assert.assertNull(StrUtil.center(null, 10, '-'));
		Assert.assertEquals("abc", StrUtil.center("abc", 3, '-'));
		Assert.assertEquals("-abc--", StrUtil.center("abc", 6, '-'));
		Assert.assertNull(StrUtil.firstNonBlank((String[]) null));
		Assert.assertEquals("a", StrUtil.firstNonBlank("", "  ", "a", "b"));
		Assert.assertNull(StrUtil.firstNonBlank(" ", ""));
		Assert.assertNull(StrUtil.concat((String[]) null));
		Assert.assertEquals("abc", StrUtil.concat("a", "b", "c"));
	}

	@Test
	public void testSwapCaseBothOverloads() {
		Assert.assertNull(StrUtil.swapCase((String) null));
		Assert.assertEquals("aBc", StrUtil.swapCase("AbC"));
		Assert.assertEquals("123", StrUtil.swapCase("123"));
		Assert.assertNull(StrUtil.swapCase((CharSequence) null));
		Assert.assertEquals("aBc1", StrUtil.swapCase((CharSequence) "AbC1"));
		Assert.assertEquals("!", StrUtil.swapCase((CharSequence) "!"));
	}

	@Test
	public void testIsAllNotBlankEmpty() {
		Assert.assertFalse(StrUtil.isAllNotBlank((CharSequence[]) null));
		Assert.assertFalse(StrUtil.isAllNotBlank("a", ""));
		Assert.assertTrue(StrUtil.isAllNotBlank("a", "b"));
		Assert.assertTrue(StrUtil.isAllEmpty((CharSequence[]) null));
		Assert.assertFalse(StrUtil.isAllEmpty("a", ""));
		Assert.assertTrue(StrUtil.isAllEmpty(null, ""));
	}

	@Test
	public void testContainsAnyIgnoreCase() {
		Assert.assertFalse(StrUtil.containsAnyIgnoreCase(null, "a"));
		Assert.assertFalse(StrUtil.containsAnyIgnoreCase("abc", (CharSequence[]) null));
		Assert.assertTrue(StrUtil.containsAnyIgnoreCase("ABC", "x", "bC"));
		Assert.assertFalse(StrUtil.containsAnyIgnoreCase("ABC", "x", "y"));
		Assert.assertTrue(StrUtil.containsAnyIgnoreCase("ABC", (CharSequence) null, "bc"));
	}

	@Test
	public void testWrapUnWrapIsWrap() {
		Assert.assertFalse(StrUtil.isWrap(null, "[", "]"));
		Assert.assertFalse(StrUtil.isWrap("abc", "", "]"));
		Assert.assertFalse(StrUtil.isWrap("[abc", "[", "]"));
		Assert.assertTrue(StrUtil.isWrap("[abc]", "[", "]"));
		Assert.assertNull(StrUtil.wrap(null, "[", "]"));
		Assert.assertEquals("[abc]", StrUtil.wrap("abc", "[", "]"));
		Assert.assertEquals("abc", StrUtil.wrap("abc", null, null));
		Assert.assertNull(StrUtil.unWrap(null, "[", "]"));
		Assert.assertEquals("abc", StrUtil.unWrap("[abc]", "[", "]"));
		Assert.assertEquals("[abc", StrUtil.unWrap("[abc", "[", "]"));
		Assert.assertEquals("abc", StrUtil.unWrap("abc", null, null));
	}

	@Test
	public void testTotalLengthSubBeforeAfter() {
		Assert.assertEquals(0, StrUtil.totalLength());
		Assert.assertEquals(0, StrUtil.totalLength((CharSequence[]) null));
		Assert.assertEquals(3, StrUtil.totalLength("ab", null, "c"));
		Assert.assertNull(StrUtil.subBefore(null, ",", false));
		Assert.assertEquals("abc", StrUtil.subBefore("abc", ",", false));
		Assert.assertEquals("", StrUtil.subBefore(",abc", ",", false));
		Assert.assertEquals("ab,cd", StrUtil.subBefore("ab,cd,ef", ",", true));
		Assert.assertNull(StrUtil.subAfter(null, ",", false));
		Assert.assertEquals("abc", StrUtil.subAfter("abc", ",", false));
		Assert.assertEquals("", StrUtil.subAfter("abc,", ",", false));
		Assert.assertEquals("ef", StrUtil.subAfter("ab,cd,ef", ",", true));
	}

	@Test
	public void testTrimStartEndSurround() {
		Assert.assertNull(StrUtil.trimStart(null));
		Assert.assertEquals("", StrUtil.trimStart(""));
		Assert.assertEquals("abc ", StrUtil.trimStart("  abc "));
		Assert.assertEquals("abc", StrUtil.trimStart("abc"));
		Assert.assertNull(StrUtil.trimEnd(null));
		Assert.assertEquals("", StrUtil.trimEnd(""));
		Assert.assertEquals(" abc", StrUtil.trimEnd(" abc  "));
		Assert.assertEquals("abc", StrUtil.trimEnd("abc"));
		Assert.assertNull(StrUtil.surround(null, "'"));
		Assert.assertEquals("abc", StrUtil.surround("abc", null));
		Assert.assertEquals("'abc'", StrUtil.surround("abc", "'"));
	}

	@Test
	public void testAbbreviate() {
		Assert.assertNull(StrUtil.abbreviate(null, 10));
		Assert.assertEquals("abc", StrUtil.abbreviate("abc", 10));
		Assert.assertEquals("a...", StrUtil.abbreviate("abcdef", 3));
		Assert.assertEquals("abc...", StrUtil.abbreviate("abcdefghij", 6));
		Assert.assertNull(StrUtil.abbreviate(null, 0, 10));
		Assert.assertEquals("abcdef", StrUtil.abbreviate("abcdef", 0, 10));
		Assert.assertEquals("abc...", StrUtil.abbreviate("abcdefghijkl", 0, 6));
		Assert.assertEquals("...ghijkl...", StrUtil.abbreviate("abcdefghijkl", 8, 9));
		Assert.assertEquals("...fghijk...", StrUtil.abbreviate("abcdefghijklmn", 5, 9));
	}

	@Test
	public void testAbbreviateMiddle() {
		Assert.assertNull(StrUtil.abbreviateMiddle(null, "...", 10));
		Assert.assertEquals("abc", StrUtil.abbreviateMiddle("abc", "", 10));
		Assert.assertEquals("abcdef", StrUtil.abbreviateMiddle("abcdef", "...", 10));
		Assert.assertEquals("a...f", StrUtil.abbreviateMiddle("abcdef", "...", 5));
		Assert.assertEquals("abcdef", StrUtil.abbreviateMiddle("abcdef", "...", 2));
	}

	@Test
	public void testSubstringBetween() {
		Assert.assertEquals("a", StrUtil.substringBetween("<a>", "<", ">"));
		Assert.assertNull(StrUtil.substringBetween(null, "<", ">"));
		Assert.assertNull(StrUtil.substringBetween("abc", "", ">"));
		Assert.assertNull(StrUtil.substringBetween("abc", "<", null));
		Assert.assertNull(StrUtil.substringBetween("abc", "<", ">"));
		Assert.assertNull(StrUtil.substringBetween("<abc", "<", ">"));
		Assert.assertEquals("b", StrUtil.substringBetween("a-b-c", "-"));
		Assert.assertEquals("b", StrUtil.substringBetween("a-b-c", "-", "-"));
	}

	@Test
	public void testDifference() {
		Assert.assertEquals("def", StrUtil.difference(null, "def"));
		Assert.assertEquals("abc", StrUtil.difference("abc", null));
		Assert.assertEquals("", StrUtil.difference("abc", "abc"));
		Assert.assertEquals("xyz", StrUtil.difference("abc", "abxyz"));
		Assert.assertEquals("cd", StrUtil.difference("ab", "abcd"));
	}

	@Test
	public void testIndexOfIgnoreCase() {
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase(null, "a", 0));
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase("abc", null, 0));
		Assert.assertEquals(0, StrUtil.indexOfIgnoreCase("ABC", "", -5));
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase("abc", "ab", 5));
		Assert.assertEquals(1, StrUtil.indexOfIgnoreCase("aBcDeF", "bc", 0));
		Assert.assertEquals(-1, StrUtil.indexOfIgnoreCase("abc", "xx", 0));
	}

	@Test
	public void testNormalizeSpace() {
		Assert.assertNull(StrUtil.normalizeSpace(null));
		Assert.assertEquals("", StrUtil.normalizeSpace(""));
		Assert.assertEquals("a b c", StrUtil.normalizeSpace("  a \n b\t c  "));
		Assert.assertEquals("a b", StrUtil.normalizeSpace("a   b"));
	}

	@Test
	public void testGetCommonPrefix() {
		Assert.assertEquals("", StrUtil.getCommonPrefix());
		Assert.assertEquals("", StrUtil.getCommonPrefix((CharSequence[]) null));
		Assert.assertEquals("", StrUtil.getCommonPrefix("abc", null));
		Assert.assertEquals("", StrUtil.getCommonPrefix("", "abc"));
		Assert.assertEquals("ab", StrUtil.getCommonPrefix("abc", "abx", "ab"));
		Assert.assertEquals("", StrUtil.getCommonPrefix("abc", "xbc"));
	}

	@Test
	public void testRotate() {
		Assert.assertNull(StrUtil.rotate(null, 2));
		Assert.assertEquals("", StrUtil.rotate("", 2));
		Assert.assertEquals("abcde", StrUtil.rotate("abcde", 5));
		Assert.assertEquals("deabc", StrUtil.rotate("abcde", 2));
		Assert.assertEquals("cdeab", StrUtil.rotate("abcde", -2));
	}

	@Test
	public void testBytesToStringMisc() {
		Assert.assertNull(StrUtil.bytes(null, StandardCharsets.UTF_8));
		Assert.assertArrayEquals("abc".getBytes(StandardCharsets.UTF_8),
				StrUtil.bytes("abc", StandardCharsets.UTF_8));
		Assert.assertEquals("null", StrUtil.toString(null));
		Assert.assertEquals("abc", StrUtil.toString("abc"));
		Assert.assertEquals("[1, 2]", StrUtil.toString(new int[] {1, 2}));
		Assert.assertEquals("[a, b]", StrUtil.toString(new String[] {"a", "b"}));
		Assert.assertEquals("x", StrUtil.toString("x"));
		Assert.assertTrue(StrUtil.isNumeric("123.45"));
		Assert.assertFalse(StrUtil.isNumeric("abc"));
		Assert.assertFalse(StrUtil.isNumeric(""));
		Assert.assertFalse(StrUtil.isNumeric(null));
		Map<String, String> m = new HashMap<>();
		Assert.assertEquals("{}", StrUtil.toString(m));
	}

	@Test
	public void testAbbreviateOffsetAndIndexOf() {
		Assert.assertEquals("abcdefghijkl", StrUtil.abbreviate("abcdefghijkl", 20, 99));
		Assert.assertEquals(1, StrUtil.indexOfIgnoreCase("abcabc", "b", -5));
	}
}
