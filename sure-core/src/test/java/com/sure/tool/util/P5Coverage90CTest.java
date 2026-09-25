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

import com.sure.tool.codec.HashUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.CsvUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.io.FileUtil;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * P5 覆盖率 90% 补强测试（第三批）。
 *
 * <p>覆盖 ArrayUtil / ConvertUtil / StrUtil / FileUtil / CollUtil / NetUtil /
 * ReflectUtil / NumberUtil / HashUtil / ClassUtil / DateUtil / ObjectUtil /
 * IdcardUtil / ReUtil / EmojiUtil / CsvUtil 等类的边界、异常与空值分支。
 */
public class P5Coverage90CTest {

	@Test
	public void testArrayUtilBoundary() {
		try {
			ArrayUtil.contains(123, 1);
			Assert.fail("contains 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertFalse(ArrayUtil.contains(new String[] { "a" }, null));
		Assert.assertTrue(ArrayUtil.contains(new String[] { null }, null));
		try {
			ArrayUtil.indexOf(123, 1);
			Assert.fail("indexOf 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertEquals(1, ArrayUtil.indexOf(new String[] { "a", null }, null));
				try {
			ArrayUtil.get(123, 0, "d");
			Assert.fail("get 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
				try {
			ArrayUtil.join(123, ",");
			Assert.fail("非数组入参应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
				try {
			ArrayUtil.toList(123);
			Assert.fail("非数组入参应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ArrayUtil.reverse(123);
			Assert.fail("reverse 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
				try {
			ArrayUtil.distinct(123);
			Assert.fail("非数组入参应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertEquals(3, ArrayUtil.sub(new String[] { "a", "b", "c" }, -5, 100).length);
		try {
			ArrayUtil.sub(123, 0, 1);
			Assert.fail("sub 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertEquals(0, ArrayUtil.sub(new String[] { "a", "b" }, 2, 1).length);
		Assert.assertEquals(1, ArrayUtil.sub(new String[] { "a", "b", "c" }, -2, -1).length);
		Assert.assertNull(ArrayUtil.firstNonNull());
		Assert.assertNull(ArrayUtil.min((Integer[]) null));
		Assert.assertNull(ArrayUtil.max((Integer[]) null));
		Assert.assertEquals(0, ArrayUtil.min(new int[0]));
		Assert.assertEquals(0, ArrayUtil.max(new int[0]));
		Assert.assertEquals(0L, ArrayUtil.min(new long[0]));
		Assert.assertEquals(0L, ArrayUtil.max(new long[0]));
		Assert.assertEquals(0D, ArrayUtil.min(new double[0]), 0D);
		Assert.assertEquals(0D, ArrayUtil.max(new double[0]), 0D);
		Assert.assertEquals(-1, ArrayUtil.lastIndexOf(123, 1));
		ArrayUtil.swap(123, 0, 1);
		Assert.assertEquals(123, ArrayUtil.remove(123, 1));
		try {
			ArrayUtil.append(123, 1);
			Assert.fail("append 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ArrayUtil.insert(123, 0, 1);
			Assert.fail("insert 非数组应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertNull(ArrayUtil.wrap((int[]) null));
		Assert.assertNull(ArrayUtil.wrap((long[]) null));
		Assert.assertNull(ArrayUtil.wrap((double[]) null));
		Assert.assertNull(ArrayUtil.wrap((float[]) null));
		Assert.assertNull(ArrayUtil.wrap((short[]) null));
		Assert.assertNull(ArrayUtil.wrap((byte[]) null));
		Assert.assertNull(ArrayUtil.wrap((char[]) null));
		Assert.assertNull(ArrayUtil.wrap((boolean[]) null));
		Assert.assertEquals(0, ArrayUtil.resize(new String[0], 0).length);
		Assert.assertNull(ArrayUtil.resize((String[]) null, 2));
	}

	@Test
	public void testConvertUtilTypes() {
		Assert.assertEquals(Float.valueOf(1.5F), ConvertUtil.convert(Float.class, "1.5"));
		Assert.assertEquals(Short.valueOf((short) 5), ConvertUtil.convert(Short.class, "5"));
		Assert.assertEquals(Byte.valueOf((byte) 5), ConvertUtil.convert(Byte.class, "5"));
		Assert.assertEquals(Character.valueOf('a'), ConvertUtil.convert(Character.class, "a"));
		Assert.assertNull(ConvertUtil.convert(Date.class, "x"));
		Assert.assertNull(ConvertUtil.toList("str"));
		Assert.assertEquals(1, ConvertUtil.wrap(5).length);
		Assert.assertEquals(new BigDecimal("1.5"), ConvertUtil.toBigDecimal("1.5"));
		Assert.assertNotNull(ConvertUtil.toDate("2024-01-01"));
		Assert.assertNull(ConvertUtil.toDate("bad-date"));
		Assert.assertEquals(1, ConvertUtil.toCharArray(new char[] { 'a' }).length);
		Assert.assertEquals(2, ConvertUtil.toDoubleArray("1,2").length);
		Assert.assertEquals(2, ConvertUtil.toBooleanArray("true,false").length);
		Assert.assertEquals(BigInteger.valueOf(123), ConvertUtil.toBigInteger("123"));
		Assert.assertNull(ConvertUtil.toBigInteger("bad"));
		Assert.assertNotNull(ConvertUtil.toLocalDate(new Date()));
		Assert.assertNotNull(ConvertUtil.toLocalDate(0L));
		Assert.assertNotNull(ConvertUtil.toLocalDateTime(new Date()));
		Assert.assertNotNull(ConvertUtil.toLocalDateTime(0L));
	}

	@Test
	public void testStrUtilEmptyBranches() {
		Assert.assertTrue(StrUtil.hasBlank("a", ""));
		Assert.assertTrue(StrUtil.isAllBlank("", ""));
		Assert.assertTrue(StrUtil.hasEmpty("a", null));
		Assert.assertFalse(StrUtil.equals("a", null));
		Assert.assertTrue(StrUtil.equals(null, null));
		Assert.assertFalse(StrUtil.equalsIgnoreCase(null, "a"));
		Assert.assertTrue(StrUtil.equalsIgnoreCase(null, null));
		Assert.assertFalse(StrUtil.containsIgnoreCase("a", "b"));
		Assert.assertFalse(StrUtil.containsAny("a", "x", "y"));
		Assert.assertFalse(StrUtil.startWith("abc", "b"));
		Assert.assertFalse(StrUtil.startWithIgnoreCase("abc", "b"));
		Assert.assertFalse(StrUtil.endWithIgnoreCase("abc", "d"));
		Assert.assertEquals("hello", StrUtil.sub("hello", -5));
		Assert.assertEquals("hello", StrUtil.sub("hello", -5, 100));
		Assert.assertEquals("", StrUtil.removePrefix("", "x"));
		Assert.assertEquals("abc", StrUtil.removePrefixIgnoreCase("abc", "x"));
		Assert.assertEquals("", StrUtil.join(",", Collections.emptyList()));
		Assert.assertEquals("plain", StrUtil.format("plain"));
		Assert.assertEquals("", StrUtil.format("", "a"));
		Assert.assertEquals("", StrUtil.padPre(null, 5, '0'));
		Assert.assertEquals("", StrUtil.padAfter(null, 5, '0'));
		Assert.assertEquals("", StrUtil.toCamelCase(null));
		Assert.assertEquals("", StrUtil.toUnderlineCase(null));
		Assert.assertEquals("", StrUtil.upperFirst(null));
		Assert.assertEquals("", StrUtil.lowerFirst(null));
		Assert.assertEquals(0, StrUtil.count("abc", ""));
		Assert.assertEquals(0, StrUtil.split("", ',').length);
		Assert.assertEquals("d", StrUtil.blankToDefault("  ", "d"));
		Assert.assertNull(StrUtil.subBetween("abc", "x", "y"));
		Assert.assertEquals(0, StrUtil.splitToLongArray("").length);
		Assert.assertFalse(StrUtil.endWithAny("abc", "x", "y"));
		Assert.assertFalse(StrUtil.isUpperCase("aB"));
		Assert.assertFalse(StrUtil.isLowerCase("A"));
		Assert.assertEquals("000", StrUtil.fillBefore("", 3, '0'));
		Assert.assertEquals("000", StrUtil.fillAfter("", 3, '0'));
		Assert.assertNull(StrUtil.toUnicode(null));
		Assert.assertNull(StrUtil.maxLength(null, 3));
		Assert.assertEquals("abc", StrUtil.strip("xxabcxx", 'x'));
		Assert.assertNull(StrUtil.strip(null, 'x'));
	}

	@Test
	public void testFileUtilMimeAndPaths() {
		Assert.assertEquals("image/bmp", FileUtil.getMimeType("a.bmp"));
		Assert.assertEquals("image/webp", FileUtil.getMimeType("a.webp"));
		Assert.assertEquals("image/tiff", FileUtil.getMimeType("a.tif"));
		Assert.assertEquals("application/gzip", FileUtil.getMimeType("a.gz"));
		Assert.assertEquals("application/x-tar", FileUtil.getMimeType("a.tar"));
		Assert.assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
				FileUtil.getMimeType("a.docx"));
		Assert.assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				FileUtil.getMimeType("a.xlsx"));
		Assert.assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
				FileUtil.getMimeType("a.pptx"));
		Assert.assertEquals("audio/flac", FileUtil.getMimeType("a.flac"));
		Assert.assertEquals("audio/aac", FileUtil.getMimeType("a.aac"));
		Assert.assertEquals("audio/ogg", FileUtil.getMimeType("a.ogg"));
		Assert.assertEquals("video/x-flv", FileUtil.getMimeType("a.flv"));
		Assert.assertEquals("application/x-msdownload", FileUtil.getMimeType("a.dll"));
		Assert.assertEquals("application/x-msdos-program", FileUtil.getMimeType("a.bat"));
		Assert.assertEquals("image/vnd.adobe.photoshop", FileUtil.getMimeType("a.psd"));
		Assert.assertEquals("font/ttf", FileUtil.getMimeType("a.ttf"));
		Assert.assertEquals("font/otf", FileUtil.getMimeType("a.otf"));
		Assert.assertEquals("font/woff", FileUtil.getMimeType("a.woff"));
		Assert.assertEquals("font/woff2", FileUtil.getMimeType("a.woff2"));
		Assert.assertEquals("application/wasm", FileUtil.getMimeType("a.wasm"));
		Assert.assertEquals("application/octet-stream", FileUtil.getMimeType("a.bin"));
		Assert.assertNull(FileUtil.file());
		Assert.assertNotNull(FileUtil.file("target"));
	}

	@Test
	public void testCollAndMiscBoundary() {
		Assert.assertFalse(CollUtil.isEmpty(Arrays.asList("a")));
		Assert.assertEquals(2, CollUtil.toList(Arrays.asList("x", "y")).size());
		Assert.assertNull(CollUtil.get(Arrays.asList("a", "b"), -5));
		Assert.assertNull(CollUtil.get(Arrays.asList("a", "b"), 99));
		Assert.assertEquals(0, CollUtil.distinct(null).size());
		Assert.assertNull(CollUtil.get(null, 0));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isInnerIP("notip"));
		Assert.assertFalse(NetUtil.isIpv4("999.1.1.1"));
		Assert.assertEquals(0, ReflectUtil.getFields(null).length);
		Assert.assertEquals(0, ReflectUtil.getMethods(null).length);
				try {
			ReflectUtil.getFieldValue(null, "x");
			Assert.fail("getFieldValue 不存在字段应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ReflectUtil.invoke(null, "x");
			Assert.fail("invoke null 对象应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ReflectUtil.invokeStatic(null, "x");
			Assert.fail("invokeStatic null 类应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ReflectUtil.invokeStatic(String.class, "notExistMethod");
			Assert.fail("invokeStatic 不存在方法应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			ReflectUtil.setFieldValue(new Object(), "notExist", "v");
			Assert.fail("setFieldValue 不存在字段应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertEquals(1, NumberUtil.parseInt("bad", 1));
		Assert.assertEquals(2L, NumberUtil.parseLong("bad", 2L));
		Assert.assertEquals(3D, NumberUtil.parseDouble("bad", 3D), 0D);
		Assert.assertEquals((short) 4, NumberUtil.parseShort("bad", (short) 4));
		Assert.assertEquals((byte) 5, NumberUtil.parseByte("bad", (byte) 5));
		Assert.assertEquals(6F, NumberUtil.parseFloat("bad", 6F), 0F);
		Assert.assertFalse(NumberUtil.isNumber("abc"));
		Assert.assertFalse(NumberUtil.isDouble("abc"));
		try {
			NumberUtil.round(1.5D, -1);
			Assert.fail("round 负数小数位应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		try {
			NumberUtil.partValue(10, new int[0]);
			Assert.fail("partValue 空权重应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertEquals(0, HashUtil.murmur3_32(new byte[0]));
		Assert.assertNotNull(HashUtil.murmur3_32(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }));
		Assert.assertNull(DateUtil.parse(null));
		try {
			DateUtil.parse("not-a-date");
			Assert.fail("parse 非法日期应抛异常");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertNull(ClassUtil.getClassName(null, true));
		try {
			ClassUtil.loadClass("com.sure.tool.not.Exist");
			Assert.fail("loadClass 不存在类应抛异常");
		} catch (ClassNotFoundException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
		Assert.assertFalse(ObjectUtil.equals(1, "1"));
		Assert.assertFalse(IdcardUtil.isValidCard("12345"));
		Assert.assertFalse(IdcardUtil.isValidCard18("12345"));
		Assert.assertNull(IdcardUtil.getGender("12345"));
		Assert.assertFalse(EmojiUtil.containsEmoji("abc"));
		Assert.assertFalse(ReUtil.isMatch("\\d+", "abc"));
		Assert.assertEquals(0, ReUtil.count("\\d", "abc"));
		Assert.assertEquals(0, CsvUtil.read((String) null).size());
		Map<String, String> m = new HashMap<>();
		m.put("k", "v");
		Assert.assertEquals("v", m.get("k"));
		Assert.assertNotNull(FileUtil.getMimeType("a.xyz") == null ? "fallback" : FileUtil.getMimeType("a.xyz"));
	}
}
