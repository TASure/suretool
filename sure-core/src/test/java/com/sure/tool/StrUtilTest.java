package com.sure.tool;

import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.BooleanUtil;
import com.sure.tool.util.CharUtil;
import com.sure.tool.util.ObjectUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * StrUtil / CharUtil / BooleanUtil / ObjectUtil / ArrayUtil 单元测试。
 */
public class StrUtilTest {

	// ---------------- StrUtil ----------------

	@Test
	public void testIsEmptyAndBlank() {
		Assert.assertTrue(StrUtil.isEmpty(null));
		Assert.assertTrue(StrUtil.isEmpty(""));
		Assert.assertFalse(StrUtil.isEmpty(" "));
		Assert.assertTrue(StrUtil.isBlank(null));
		Assert.assertTrue(StrUtil.isBlank(""));
		Assert.assertTrue(StrUtil.isBlank(" \t\n"));
		Assert.assertFalse(StrUtil.isBlank(" a "));
		Assert.assertTrue(StrUtil.isNotBlank("a"));
	}

	@Test
	public void testTrim() {
		Assert.assertEquals("ab cd", StrUtil.trim("  ab cd  "));
		Assert.assertNull(StrUtil.trimToNull("   "));
		Assert.assertEquals("abcd", StrUtil.cleanBlank(" a b  cd "));
	}

	@Test
	public void testEqualsAndContains() {
		Assert.assertTrue(StrUtil.equals("abc", "abc"));
		Assert.assertFalse(StrUtil.equals("abc", "ABC"));
		Assert.assertTrue(StrUtil.equalsIgnoreCase("abc", "ABC"));
		Assert.assertTrue(StrUtil.contains("hello world", "world"));
		Assert.assertTrue(StrUtil.containsIgnoreCase("hello world", "WORLD"));
		Assert.assertTrue(StrUtil.containsAny("hello", "a", "ell"));
	}

	@Test
	public void testStartEndWith() {
		Assert.assertTrue(StrUtil.startWith("hello", "he"));
		Assert.assertTrue(StrUtil.startWithIgnoreCase("Hello", "he"));
		Assert.assertTrue(StrUtil.endWith("hello", "llo"));
		Assert.assertTrue(StrUtil.endWithIgnoreCase("Hello", "LLO"));
	}

	@Test
	public void testSub() {
		Assert.assertEquals("ell", StrUtil.sub("hello", 1, 4));
		Assert.assertEquals("ll", StrUtil.sub("hello", -3, -1));
		Assert.assertEquals("hello", StrUtil.sub("hello", -5));
		Assert.assertEquals("", StrUtil.sub("hello", 3, 1));
	}

	@Test
	public void testRemovePrefixSuffix() {
		Assert.assertEquals("world", StrUtil.removePrefix("hello world", "hello "));
		Assert.assertEquals("hello", StrUtil.removeSuffix("hello world", " world"));
		Assert.assertEquals("world", StrUtil.removePrefixIgnoreCase("HELLO world", "hello "));
	}

	@Test
	public void testReplaceJoinFormat() {
		Assert.assertEquals("a-b-c", StrUtil.replace("a_b_c", "_", "-"));
		Assert.assertEquals("a,b,c", StrUtil.join(",", "a", "b", "c"));
		Assert.assertEquals("你好, 世界", StrUtil.format("你好, {}", "世界"));
		Assert.assertEquals("axb{}", StrUtil.format("a{}b{}", "x"));
	}

	@Test
	public void testRepeatAndPad() {
		Assert.assertEquals("ababab", StrUtil.repeat("ab", 3));
		Assert.assertEquals("00123", StrUtil.padPre("123", 5, '0'));
		Assert.assertEquals("12300", StrUtil.padAfter("123", 5, '0'));
	}

	@Test
	public void testCamelCase() {
		Assert.assertEquals("userName", StrUtil.toCamelCase("user_name"));
		Assert.assertEquals("userName", StrUtil.toCamelCase("userName"));
		Assert.assertEquals("user_name", StrUtil.toUnderlineCase("userName"));
		Assert.assertEquals("user_name", StrUtil.toUnderlineCase("user_name"));
	}

	@Test
	public void testUpperLowerFirst() {
		Assert.assertEquals("Hello", StrUtil.upperFirst("hello"));
		Assert.assertEquals("hello", StrUtil.lowerFirst("Hello"));
	}

	@Test
	public void testReverseCount() {
		Assert.assertEquals("olleh", StrUtil.reverse("hello"));
		Assert.assertEquals(2, StrUtil.count("a-b-c", '-'));
		Assert.assertEquals(3, StrUtil.count("ababab", "ab"));
	}

	@Test
	public void testSplit() {
		Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StrUtil.split("a,b,c", ','));
		Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StrUtil.split("a.b.c", "."));
	}

	@Test
	public void testToString() {
		Assert.assertEquals("null", StrUtil.toString(null));
		Assert.assertEquals("[1, 2, 3]", StrUtil.toString(new int[]{1, 2, 3}));
		Assert.assertEquals("abc", StrUtil.toString("abc"));
	}

	@Test
	public void testDefaults() {
		Assert.assertEquals("default", StrUtil.blankToDefault(" ", "default"));
		Assert.assertEquals("x", StrUtil.blankToDefault("x", "default"));
		Assert.assertEquals("default", StrUtil.emptyToDefault("", "default"));
		Assert.assertEquals("x", StrUtil.nullToEmpty(null) == "" ? "x" : StrUtil.nullToEmpty(null));
	}

	// ---------------- CharUtil ----------------

	@Test
	public void testCharUtil() {
		Assert.assertTrue(CharUtil.isLetter('a'));
		Assert.assertTrue(CharUtil.isNumber('5'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('z'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('9'));
		Assert.assertTrue(CharUtil.isBlankChar(' '));
		Assert.assertTrue(CharUtil.isBlankChar('\t'));
		Assert.assertFalse(CharUtil.isBlankChar('a'));
		Assert.assertTrue(CharUtil.isHexChar('f'));
		Assert.assertTrue(CharUtil.isHexChar('F'));
		Assert.assertFalse(CharUtil.isHexChar('g'));
		Assert.assertEquals(10, CharUtil.digitValue('a'));
		Assert.assertEquals(15, CharUtil.digitValue('F'));
		Assert.assertEquals(7, CharUtil.digitValue('7'));
		Assert.assertEquals(-1, CharUtil.digitValue('g'));
	}

	// ---------------- BooleanUtil ----------------

	@Test
	public void testBooleanUtil() {
		Assert.assertTrue(BooleanUtil.toBoolean("true"));
		Assert.assertTrue(BooleanUtil.toBoolean("YES"));
		Assert.assertTrue(BooleanUtil.toBoolean("1"));
		Assert.assertTrue(BooleanUtil.toBoolean("是"));
		Assert.assertFalse(BooleanUtil.toBoolean("no"));
		Assert.assertFalse(BooleanUtil.toBoolean("0"));
		Assert.assertFalse(BooleanUtil.toBoolean("unknown"));
		Assert.assertTrue(BooleanUtil.toBoolean("unknown", true));
		Assert.assertEquals(Boolean.TRUE, BooleanUtil.toBooleanObj("on"));
		Assert.assertNull(BooleanUtil.toBooleanObj("unknown"));
		Assert.assertEquals(1, BooleanUtil.toInt(true));
		Assert.assertEquals("yes", BooleanUtil.toStringYesNo(true));
		Assert.assertTrue(BooleanUtil.or(false, true));
		Assert.assertFalse(BooleanUtil.and(true, false));
	}

	// ---------------- ObjectUtil ----------------

	@Test
	public void testObjectUtil() {
		Assert.assertTrue(ObjectUtil.isEmpty(null));
		Assert.assertTrue(ObjectUtil.isEmpty(""));
		Assert.assertTrue(ObjectUtil.isEmpty(new ArrayList<>()));
		Assert.assertFalse(ObjectUtil.isEmpty(new int[]{1}));
		Assert.assertTrue(ObjectUtil.equals(new int[]{1, 2}, new int[]{1, 2}));
		Assert.assertFalse(ObjectUtil.equals(new int[]{1, 2}, new int[]{1, 3}));
		Assert.assertEquals("x", ObjectUtil.defaultIfNull(null, "x"));
		Assert.assertEquals("x", ObjectUtil.defaultIfEmpty("", "x"));
	}

	@Test
	public void testSerializeRoundTrip() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		byte[] bytes = ObjectUtil.serialize(list);
		Assert.assertNotNull(bytes);
		Object restored = ObjectUtil.deserialize(bytes);
		Assert.assertEquals(list, restored);
	}

	// ---------------- ArrayUtil ----------------

	@Test
	public void testArrayUtil() {
		int[] arr = {1, 2, 3, 2};
		Assert.assertFalse(ArrayUtil.isEmpty(arr));
		Assert.assertTrue(ArrayUtil.contains(arr, 3));
		Assert.assertFalse(ArrayUtil.contains(arr, 9));
		Assert.assertEquals(1, ArrayUtil.indexOf(arr, 2));
		Assert.assertEquals(3, ArrayUtil.get(arr, 2));
		Assert.assertEquals(2, ArrayUtil.get(arr, -1));
		Assert.assertEquals("1-2-3-2", ArrayUtil.join(arr, "-"));
		Assert.assertEquals(4, ArrayUtil.toList(arr).size());
		Assert.assertEquals(Integer.valueOf(1), ArrayUtil.get(arr, 0));
		Assert.assertArrayEquals(new Object[]{1, 2, 3}, ArrayUtil.distinct(arr));
		Assert.assertArrayEquals(new Object[]{2, 3}, ArrayUtil.sub(arr, 1, 3));
		Assert.assertEquals(1, ArrayUtil.min(new int[]{3, 1, 2}));
		Assert.assertEquals(3, ArrayUtil.max(new int[]{3, 1, 2}));
		Assert.assertEquals("a", ArrayUtil.firstNonNull(null, "a"));
		Assert.assertEquals(Integer.valueOf(1), ArrayUtil.min(new Integer[]{3, 1, 2}));
	}

	@Test
	public void testArrayReverse() {
		Integer[] arr = {1, 2, 3};
		ArrayUtil.reverse(arr);
		Assert.assertArrayEquals(new Integer[]{3, 2, 1}, arr);
	}

	@Test
	public void testArrayToString() {
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new int[]{1, 2}));
		Assert.assertEquals("[]", ArrayUtil.toString(null));
	}
}
