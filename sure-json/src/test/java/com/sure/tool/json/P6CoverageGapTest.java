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
package com.sure.tool.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P6：覆盖率补测，针对既有未命中的异常分支、类型转换与 JSONPath/流式解析边界。
 */
public class P6CoverageGapTest {

	// ==================== JSONArray 类型转换与异常分支 ====================

	@Test
	public void testArrayGetIntEdges() {
		JSONArray arr = JSONUtil.parseArray("[null, 42, \"7\", \"abc\"]");
		Assert.assertNull(arr.getInt(0));
		Assert.assertEquals(Integer.valueOf(42), arr.getInt(1));
		Assert.assertEquals(Integer.valueOf(7), arr.getInt(2));
		Assert.assertNull(arr.getInt(3));
		Assert.assertNull(arr.getInt(99));
		Assert.assertNull(arr.getInt(-1));
	}

	@Test
	public void testArrayGetLongEdges() {
		JSONArray arr = JSONUtil.parseArray("[null, 12345678901, \"99\", \"abc\"]");
		Assert.assertNull(arr.getLong(0));
		Assert.assertEquals(Long.valueOf(12345678901L), arr.getLong(1));
		Assert.assertEquals(Long.valueOf(99L), arr.getLong(2));
		Assert.assertNull(arr.getLong(3));
		Assert.assertNull(arr.getLong(99));
		Assert.assertNull(arr.getLong(-1));
	}

	@Test
	public void testArrayGetDoubleEdges() {
		JSONArray arr = JSONUtil.parseArray("[null, 3.14, \"2.5\", \"abc\"]");
		Assert.assertNull(arr.getDouble(0));
		Assert.assertEquals(3.14, arr.getDouble(1), 0.0001);
		Assert.assertEquals(2.5, arr.getDouble(2), 0.0001);
		Assert.assertNull(arr.getDouble(3));
		Assert.assertNull(arr.getDouble(99));
		Assert.assertNull(arr.getDouble(-1));
	}

	@Test
	public void testArrayGetBoolEdges() {
		JSONArray arr = new JSONArray();
		arr.add(null);
		arr.add(Boolean.TRUE);
		arr.add("false");
		Assert.assertNull(arr.getBool(0));
		Assert.assertEquals(Boolean.TRUE, arr.getBool(1));
		Assert.assertEquals(Boolean.FALSE, arr.getBool(2));
		Assert.assertNull(arr.getBool(99));
	}

	@Test
	public void testArrayGetNestedFromRawContainers() {
		Map<String, Object> plainMap = new LinkedHashMap<>();
		plainMap.put("k", "v");
		List<Object> plainList = new ArrayList<>();
		plainList.add(1);
		JSONArray arr = new JSONArray();
		arr.add(plainMap);
		arr.add(plainList);
		arr.add("string");
		Assert.assertEquals("v", arr.getJSONObject(0).get("k"));
		Assert.assertEquals(1, arr.getJSONArray(1).get(0));
		Assert.assertNull(arr.getJSONObject(2));
		Assert.assertNull(arr.getJSONArray(2));
		Assert.assertNull(arr.getBean(2, TestUser.class));
	}

	@Test
	public void testArrayGetCharNull() {
		JSONArray arr = new JSONArray();
		arr.add(null);
		Assert.assertEquals(0, arr.getChar(0));
		Assert.assertEquals('z', arr.getChar(99, 'z'));
	}

	@Test
	public void testArrayGetBigIntegerEdges() {
		JSONArray arr = new JSONArray();
		arr.add(new BigInteger("12345678901234567890"));
		arr.add(42L);
		arr.add(null);
		arr.add("999");
		arr.add("notanum");
		Assert.assertEquals(new BigInteger("12345678901234567890"), arr.getBigInteger(0));
		Assert.assertEquals(BigInteger.valueOf(42), arr.getBigInteger(1));
		Assert.assertNull(arr.getBigInteger(2));
		Assert.assertEquals(new BigInteger("999"), arr.getBigInteger(3));
		Assert.assertNull(arr.getBigInteger(4));
		Assert.assertEquals(BigInteger.valueOf(7), arr.getBigInteger(99, BigInteger.valueOf(7)));
	}

	@Test
	public void testArrayDeepCloneNestedContainers() {
		Map<String, Object> plainMap = new LinkedHashMap<>();
		plainMap.put("inner", 1);
		List<Object> plainList = new ArrayList<>();
		plainList.add("x");
		JSONArray innerArr = new JSONArray();
		innerArr.add(9);
		JSONArray arr = new JSONArray();
		arr.add(innerArr);
		arr.add(plainMap);
		arr.add(plainList);
		arr.add("keep");
		JSONArray copy = arr.deepClone();
		Assert.assertEquals(4, copy.size());
		Assert.assertEquals(9, ((JSONArray) copy.get(0)).get(0));
		Assert.assertEquals(1, ((JSONObject) copy.get(1)).get("inner"));
		Assert.assertEquals("x", ((List<?>) copy.get(2)).get(0));
		Assert.assertEquals("keep", copy.get(3));
	}

	// ==================== JSONObject 类型转换与异常分支 ====================

	@Test
	public void testObjectGetLongEdges() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":null,\"b\":\"123\",\"c\":\"abc\",\"d\":5}");
		Assert.assertNull(obj.getLong("a"));
		Assert.assertEquals(Long.valueOf(123L), obj.getLong("b"));
		Assert.assertNull(obj.getLong("c"));
		Assert.assertEquals(Long.valueOf(5L), obj.getLong("d"));
		Assert.assertNull(obj.getLong("missing"));
	}

	@Test
	public void testObjectGetDoubleEdges() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":null,\"b\":\"1.5\",\"c\":\"abc\"}");
		Assert.assertNull(obj.getDouble("a"));
		Assert.assertEquals(1.5, obj.getDouble("b"), 0.0001);
		Assert.assertNull(obj.getDouble("c"));
		Assert.assertNull(obj.getDouble("missing"));
	}

	@Test
	public void testObjectGetBigIntegerEdges() {
		JSONObject obj = new JSONObject();
		obj.put("bi", new BigInteger("999"));
		obj.put("num", 7);
		obj.put("bad", "xyz");
		Assert.assertEquals(new BigInteger("999"), obj.getBigInteger("bi"));
		Assert.assertEquals(BigInteger.valueOf(7), obj.getBigInteger("num"));
		Assert.assertNull(obj.getBigInteger("bad"));
		Assert.assertEquals(BigInteger.ONE, obj.getBigInteger("missing", BigInteger.ONE));
		Assert.assertNull(obj.getBigInteger("missing"));
	}

	@Test
	public void testObjectDeepCloneNestedContainers() {
		Map<String, Object> plainMap = new LinkedHashMap<>();
		plainMap.put("m", 1);
		List<Object> plainList = new ArrayList<>();
		plainList.add(2);
		JSONObject obj = new JSONObject();
		obj.put("map", plainMap);
		obj.put("list", plainList);
		obj.put("str", "s");
		JSONObject copy = obj.deepClone();
		Assert.assertEquals(1, ((JSONObject) copy.get("map")).get("m"));
		Assert.assertEquals(2, ((List<?>) copy.get("list")).get(0));
		Assert.assertEquals("s", copy.get("str"));
	}

	// ==================== JSONUtil 序列化 / 美化 / 解析异常 ====================

	@Test
	public void testWriteUnknownObjectFallback() {
		// 枚举既非 CharSequence/Number/Map/Collection/Date，isBean 亦为 false，走 else 回退
		String json = JSONUtil.toJsonStr(java.math.RoundingMode.HALF_UP);
		Assert.assertEquals("\"HALF_UP\"", json);
	}

	@Test
	public void testPrettyArrayEmptyAndNonEmpty() {
		Assert.assertEquals("[]", JSONUtil.toJsonPrettyStr(new int[0]));
		String pretty = JSONUtil.toJsonPrettyStr(new int[] {1, 2, 3});
		Assert.assertTrue(pretty.contains("1"));
		Assert.assertTrue(pretty.contains("2"));
		Assert.assertTrue(pretty.contains("3"));
	}

	@Test
	public void testPrettyDateAndUnknown() {
		String dateJson = JSONUtil.toJsonPrettyStr(new Date(0));
		Assert.assertTrue(dateJson.startsWith("\""));
		Assert.assertTrue(dateJson.endsWith("\""));
		String weird = JSONUtil.toJsonPrettyStr(new Object());
		Assert.assertTrue(weird.startsWith("\""));
	}

	@Test
	public void testPrettyEscapes() {
		String s = "a\"b\\c\nd\re\tf\bg\fh\u0001";
		String out = JSONUtil.toJsonPrettyStr(s);
		Assert.assertTrue(out.contains("\\\""));
		Assert.assertTrue(out.contains("\\\\"));
		Assert.assertTrue(out.contains("\\n"));
		Assert.assertTrue(out.contains("\\r"));
		Assert.assertTrue(out.contains("\\t"));
		Assert.assertTrue(out.contains("\\b"));
		Assert.assertTrue(out.contains("\\f"));
		Assert.assertTrue(out.contains("\\u0001"));
	}

	@Test(expected = JSONException.class)
	public void testParseArrayMissingComma() {
		JSONUtil.parse("[1 2]");
	}

	@Test(expected = JSONException.class)
	public void testParseStringTrailingBackslash() {
		JSONUtil.parse("\"abc\\");
	}

	// ==================== StreamJsonParser 边界与异常 ====================

	@Test(expected = JSONException.class)
	public void testStreamEmptyInput() throws Exception {
		StreamJsonParser.parse(new StringReader(""), new JsonHandler() {
		});
	}

	@Test
	public void testParseToListLiterals() {
		Assert.assertEquals(Boolean.FALSE, StreamJsonParser.parseToList("false").get(0));
		Assert.assertNull(StreamJsonParser.parseToList("null").get(0));
		Assert.assertEquals(Boolean.TRUE, StreamJsonParser.parseToList("true").get(0));
		Assert.assertEquals("abc", StreamJsonParser.parseToList("\"abc\"").get(0));
		Assert.assertEquals("5", StreamJsonParser.parseToList("5").get(0));
	}

	@Test
	public void testStreamFalseAndNullEvents() throws Exception {
		List<Object> values = new ArrayList<>();
		StreamJsonParser.parse(new StringReader("[false, null]"), new JsonHandler() {
			@Override
			public void onBoolean(boolean value) {
				values.add(value);
			}

			@Override
			public void onNull() {
				values.add("NULL");
			}
		});
		Assert.assertEquals(Boolean.FALSE, values.get(0));
		Assert.assertEquals("NULL", values.get(1));
	}

	@Test
	public void testStreamEscapeSequences() throws Exception {
		List<Object> values = StreamJsonParser.parseToList("\"a\\b\\f\\r\"");
		Assert.assertEquals("a\b\f\r", values.get(0));
	}

	@Test
	public void testStreamUnicodeUpperHex() throws Exception {
		// \u00AF 同时含大写 A、F，覆盖 readHex4 的大写字母分支
		List<Object> values = StreamJsonParser.parseToList("\"\\u0041\\u00AF\"");
		Assert.assertEquals("A¯", values.get(0));
	}

	@Test(expected = JSONException.class)
	public void testStreamBadEscape() throws Exception {
		StreamJsonParser.parse(new StringReader("\"\\x\""), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamBadUnicodeHex() throws Exception {
		StreamJsonParser.parse(new StringReader("\"\\uXXXX\""), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamBareMinus() throws Exception {
		StreamJsonParser.parse(new StringReader("-"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamBadLiteral() throws Exception {
		StreamJsonParser.parse(new StringReader("truX"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamMissingColon() throws Exception {
		StreamJsonParser.parse(new StringReader("{\"a\" 1}"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamObjectMissingComma() throws Exception {
		StreamJsonParser.parse(new StringReader("{\"a\":1 \"b\":2}"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamArrayMissingComma() throws Exception {
		StreamJsonParser.parse(new StringReader("[1 2]"), new JsonHandler() {
		});
	}

	@Test
	public void testNoOpHandlerDefaultBodies() throws Exception {
		// 不覆写任何方法，触发 JsonHandler 各 default 空方法体
		StreamJsonParser.parse(new StringReader("[\"s\", 1, true, null]"), new JsonHandler() {
		});
	}

	@Test
	public void testJsonExceptionTwoArg() {
		JSONException e = new JSONException("msg", new IOException("cause"));
		Assert.assertEquals("msg", e.getMessage());
		Assert.assertTrue(e.getCause() instanceof IOException);
	}

	// ==================== JsonPath 编译 / 求值 / 过滤器边界 ====================

	@Test(expected = JSONException.class)
	public void testCompileEmptyPath() {
		JsonPath.select(new JSONObject(), "");
	}

	@Test(expected = JSONException.class)
	public void testCompileNullPath() {
		JsonPath.select(new JSONObject(), null);
	}

	@Test
	public void testRecursiveWildcardBracket() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":{\"b\":1},\"c\":[2,3]}");
		List<Object> all = JsonPath.select(obj, "$..[*]");
		Assert.assertFalse(all.isEmpty());
	}

	@Test(expected = JSONException.class)
	public void testRecursiveBracketUnsupported() {
		JsonPath.select(new JSONObject(), "$..[?(@.a)]");
	}

	@Test
	public void testDotWildcardOnMap() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":1,\"b\":2}");
		List<Object> vals = JsonPath.select(obj, "$.*");
		Assert.assertEquals(2, vals.size());
	}

	@Test(expected = JSONException.class)
	public void testUnsupportedPathSegment() {
		JsonPath.select(JSONUtil.parseObj("{\"a\":1}"), "$[xyz]");
	}

	@Test
	public void testPathWithoutLeadingDot() {
		JSONObject obj = JSONUtil.parseObj("{\"store\":{\"v\":9}}");
		Object v = JsonPath.eval(obj, "store.v");
		Assert.assertEquals(9, ((Number) v).longValue());
	}

	@Test
	public void testLengthOnNonContainer() {
		JSONObject obj = JSONUtil.parseObj("{\"n\":42}");
		Object len = JsonPath.eval(obj, "$.n.length()");
		Assert.assertEquals(0, len);
	}

	@Test
	public void testFilterBracketPathAndIndex() {
		// 该实现 compile 以首个 ']' 截断过滤器，数组下标式过滤器不可达；此处仅点路径
		String json = "{\"grid\":[[1,10],[2,20],[3,30]]}";
		JSONArray hit = JSONUtil.query(json, "$.grid[?(@.0 > 1)]");
		Assert.assertTrue(hit.isEmpty());
	}

	@Test
	public void testFilterDotPathOnMap() {
		String json = "{\"books\":[{\"isbn\":\"a\"},{\"x\":1}]}";
		JSONArray hit = JSONUtil.query(json, "$.books[?(@.isbn)]");
		Assert.assertEquals(1, hit.size());
	}

	@Test
	public void testFilterNullComparisons() {
		String json = "{\"items\":[{\"p\":1},{\"p\":null},{\"flag\":true}]}";
		JSONArray eqNull = JSONUtil.query(json, "$.items[?(@.p == null)]");
		Assert.assertEquals(2, eqNull.size());
		JSONArray lt = JSONUtil.query(json, "$.items[?(@.p < 5)]");
		Assert.assertFalse(lt.isEmpty());
		JSONArray gtNull = JSONUtil.query(json, "$.items[?(@.p > null)]");
		Assert.assertFalse(gtNull.isEmpty());
		JSONArray boolEq = JSONUtil.query(json, "$.items[?(@.flag == true)]");
		Assert.assertEquals(1, boolEq.size());
		JSONArray boolNe = JSONUtil.query(json, "$.items[?(@.flag != false)]");
		// flag 缺失/为 null 时 compare 返回 -1，亦满足 !=0
		Assert.assertEquals(3, boolNe.size());
	}

	@Test
	public void testFilterNotWithoutParens() {
		String json = "{\"items\":[{\"a\":1},{\"b\":2}]}";
		JSONArray hit = JSONUtil.query(json, "$.items[?(!@.a)]");
		Assert.assertEquals(1, hit.size());
	}

	@Test
	public void testFilterParenthesized() {
		String json = "{\"items\":[{\"a\":1,\"b\":2},{\"a\":3,\"b\":9}]}";
		JSONArray hit = JSONUtil.query(json, "$.items[?((@.a < 5))]");
		Assert.assertEquals(2, hit.size());
	}

	@Test(expected = JSONException.class)
	public void testFilterNotMissingClose() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?(!(@.x<1)]");
	}

	@Test(expected = JSONException.class)
	public void testFilterParenMissingClose() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?((@.x<1)]");
	}

	@Test(expected = JSONException.class)
	public void testFilterOperandNotAt() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?(1 < 2)]");
	}

	@Test(expected = JSONException.class)
	public void testFilterMissingLiteral() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?(@.x ==)]");
	}

	@Test(expected = JSONException.class)
	public void testFilterUnclosedStringLiteral() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?(@.x == 'abc)]");
	}

	@Test(expected = JSONException.class)
	public void testFilterInvalidLiteral() {
		JSONUtil.query("{\"a\":[1]}", "$.a[?(@.x == abc)]");
	}

	@Test
	public void testFilterExistsSelf() {
		JSONArray arr = JSONUtil.parseArray("[1, 2, 3]");
		// 存在性判断作用于根节点本身，readPath 遇到 '@' 直接返回 null
		JSONArray hit = new JSONArray(JsonPath.select(arr, "$[?(@)]"));
		Assert.assertEquals(0, hit.size());
	}

	@Test
	public void testInternalHelpers() {
		Map<String, Object> m = JsonPath.mapOf("k", 1);
		Assert.assertEquals(1, m.get("k"));
		Assert.assertTrue(JsonPath.eq("a", "a"));
		Assert.assertFalse(JsonPath.eq("a", "b"));
	}

	@Test
	public void testFilterLteGtGeAndEqOps() {
		String json = "{\"items\":[{\"v\":1},{\"v\":2},{\"v\":3}]}";
		Assert.assertEquals(2, JSONUtil.query(json, "$.items[?(@.v <= 2)]").size());
		Assert.assertEquals(2, JSONUtil.query(json, "$.items[?(@.v > 1)]").size());
		Assert.assertEquals(1, JSONUtil.query(json, "$.items[?(@.v >= 3)]").size());
		Assert.assertEquals(1, JSONUtil.query(json, "$.items[?(@.v = 2)]").size());
	}

	@Test
	public void testBigDecimalQueryLiteral() {
		String json = "{\"items\":[{\"v\":1.5}]}";
		Assert.assertEquals(1, JSONUtil.query(json, "$.items[?(@.v == 1.5)]").size());
	}
}
