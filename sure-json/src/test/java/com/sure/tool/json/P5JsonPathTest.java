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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * P5（v1.1.0）：JSONPath 查询与流式解析测试。
 */
public class P5JsonPathTest {

	private static final String BOOKS_JSON = "{"
			+ "\"store\": {"
			+ "\"book\": ["
			+ "{\"category\": \"reference\", \"author\": \"Nigel Rees\", \"title\": \"Sayings of the Century\", \"price\": 8.95},"
			+ "{\"category\": \"fiction\", \"author\": \"Evelyn Waugh\", \"title\": \"Sword of Honour\", \"price\": 12.99},"
			+ "{\"category\": \"fiction\", \"author\": \"Herman Melville\", \"title\": \"Moby Dick\", \"isbn\": \"0-553-21311-3\", \"price\": 8.99},"
			+ "{\"category\": \"fiction\", \"author\": \"J. R. R. Tolkien\", \"title\": \"The Lord of the Rings\", \"isbn\": \"0-395-19395-8\", \"price\": 22.99}"
			+ "],"
			+ "\"bicycle\": {\"color\": \"red\", \"price\": 19.95}"
			+ "}"
			+ "}";

	@Test
	public void testSelectRootKey() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		Object store = JsonPath.eval(obj, "$.store");
		Assert.assertTrue(store instanceof JSONObject);
		JSONObject storeObj = (JSONObject) store;
		Assert.assertTrue(storeObj.containsKey("book"));
		Assert.assertTrue(storeObj.containsKey("bicycle"));
	}

	@Test
	public void testSelectArrayIndex() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		JSONObject first = (JSONObject) JsonPath.eval(obj, "$.store.book[0]");
		Assert.assertEquals("Sayings of the Century", first.get("title"));
		Assert.assertEquals("Nigel Rees", first.get("author"));
	}

	@Test
	public void testSelectWildcardArray() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		JSONArray titles = JSONUtil.query(BOOKS_JSON, "$.store.book[*].title");
		Assert.assertEquals(4, titles.size());
		Assert.assertEquals("Moby Dick", titles.get(2));
	}

	@Test
	public void testSelectBracketKey() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		Object bicycle = JsonPath.eval(obj, "$['store']['bicycle']");
		Assert.assertTrue(bicycle instanceof JSONObject);
		Assert.assertEquals("red", ((JSONObject) bicycle).get("color"));
	}

	@Test
	public void testSelectRecursive() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		JSONArray prices = JSONUtil.query(BOOKS_JSON, "$..price");
		Assert.assertEquals(5, prices.size());
		Assert.assertEquals(8.95, ((Number) prices.get(0)).doubleValue(), 0.0001);
		Assert.assertEquals(22.99, ((Number) prices.get(3)).doubleValue(), 0.0001);
		Assert.assertEquals(19.95, ((Number) prices.get(4)).doubleValue(), 0.0001);
	}

	@Test
	public void testSelectRecursiveWildcard() {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		List<Object> all = JsonPath.select(obj, "$..*");
		Assert.assertFalse(all.isEmpty());
	}

	@Test
	public void testFilterNumericCompare() {
		JSONArray cheap = JSONUtil.query(BOOKS_JSON, "$.store.book[?(@.price < 10)]");
		Assert.assertEquals(2, cheap.size());
		Assert.assertEquals("Sayings of the Century", ((JSONObject) cheap.get(0)).get("title"));
		Assert.assertEquals("Moby Dick", ((JSONObject) cheap.get(1)).get("title"));
	}

	@Test
	public void testFilterStringEqual() {
		JSONArray fiction = JSONUtil.query(BOOKS_JSON, "$.store.book[?(@.category == 'fiction')]");
		Assert.assertEquals(3, fiction.size());
	}

	@Test
	public void testFilterNotEqualAndGte() {
		JSONArray notRef = JSONUtil.query(BOOKS_JSON, "$.store.book[?(@.category != 'reference')]");
		Assert.assertEquals(3, notRef.size());
		JSONArray expensive = JSONUtil.query(BOOKS_JSON, "$.store.book[?(@.price >= 20)]");
		Assert.assertEquals(1, expensive.size());
		Assert.assertEquals("The Lord of the Rings", ((JSONObject) expensive.get(0)).get("title"));
	}

	@Test
	public void testFilterLogicalAndOrNot() {
		JSONArray and = JSONUtil.query(BOOKS_JSON,
				"$.store.book[?(@.price < 15 && @.category == 'fiction')]");
		Assert.assertEquals(2, and.size());
		JSONArray or = JSONUtil.query(BOOKS_JSON,
				"$.store.book[?(@.price < 9 || @.category == 'reference')]");
		// price<9：8.95、8.99；reference 为 8.95（去重）=> 2 本
		Assert.assertEquals(2, or.size());
		JSONArray not = JSONUtil.query(BOOKS_JSON, "$.store.book[?(!(@.price < 10))]");
		Assert.assertEquals(2, not.size());
	}

	@Test
	public void testFilterExists() {
		JSONArray withIsbn = JSONUtil.query(BOOKS_JSON, "$.store.book[?(@.isbn)]");
		Assert.assertEquals(2, withIsbn.size());
	}

	@Test
	public void testNoMatch() {
		JSONArray none = JSONUtil.query(BOOKS_JSON, "$.store.nothing");
		Assert.assertTrue(none.isEmpty());
		Assert.assertNull(JsonPath.eval(JSONUtil.parseObj(BOOKS_JSON), "$.store.nothing"));
	}

	@Test(expected = JSONException.class)
	public void testInvalidPath() {
		JsonPath.select(JSONUtil.parseObj(BOOKS_JSON), "$.store.book[");
	}

	@Test
	public void testGetByPathConvenience() {
		Assert.assertEquals("red", JSONUtil.getByPath(BOOKS_JSON, "$.store.bicycle.color"));
		Assert.assertEquals("red", JSONUtil.getByPath(BOOKS_JSON, "$['store']['bicycle']['color']"));
	}

	@Test
	public void testQueryOnArrayRoot() {
		String json = "[\"a\", \"b\", \"c\"]";
		JSONArray all = JSONUtil.query(json, "$[*]");
		Assert.assertEquals(3, all.size());
		Assert.assertEquals("b", JsonPath.eval(JSONUtil.parseArray(json), "$[1]"));
	}

	// ============ 流式解析 ============

	@Test
	public void testStreamSimpleObject() throws Exception {
		List<String> events = new ArrayList<>();
		JSONUtil.parseStream(new ByteArrayInputStream(
				"{\"name\":\"sure\",\"age\":21,\"ok\":true,\"none\":null}".getBytes(StandardCharsets.UTF_8)),
				new JsonHandler() {
					@Override
					public void onStartObject() {
						events.add("SO");
					}

					@Override
					public void onEndObject() {
						events.add("EO");
					}

					@Override
					public void onKey(String key) {
						events.add("K:" + key);
					}

					@Override
					public void onString(String value) {
						events.add("S:" + value);
					}

					@Override
					public void onNumber(String raw) {
						events.add("N:" + raw);
					}

					@Override
					public void onBoolean(boolean value) {
						events.add("B:" + value);
					}

					@Override
					public void onNull() {
						events.add("NULL");
					}
				});
		Assert.assertEquals(List.of("SO", "K:name", "S:sure", "K:age", "N:21",
				"K:ok", "B:true", "K:none", "NULL", "EO"), events);
	}

	@Test
	public void testStreamNestedAndArray() throws Exception {
		List<String> events = new ArrayList<>();
		JSONUtil.parseStream(new ByteArrayInputStream(
				"{\"a\":[1,2,{\"b\":\"x\"}]}".getBytes(StandardCharsets.UTF_8)),
				new JsonHandler() {
					@Override
					public void onStartObject() {
						events.add("SO");
					}

					@Override
					public void onEndObject() {
						events.add("EO");
					}

					@Override
					public void onStartArray() {
						events.add("SA");
					}

					@Override
					public void onEndArray() {
						events.add("EA");
					}

					@Override
					public void onKey(String key) {
						events.add("K:" + key);
					}

					@Override
					public void onString(String value) {
						events.add("S:" + value);
					}

					@Override
					public void onNumber(String raw) {
						events.add("N:" + raw);
					}
				});
		Assert.assertEquals(List.of("SO", "K:a", "SA", "N:1", "N:2", "SO",
				"K:b", "S:x", "EO", "EA", "EO"), events);
	}

	@Test
	public void testStreamEscapesAndUnicode() throws Exception {
		List<Object> values = StreamJsonParser.parseToList(
				"\"line\\n\\t\\\"q\\\"\\\\ \\u4e2d\\u6587\\/\"");
		Assert.assertEquals(1, values.size());
		Assert.assertEquals("line\n\t\"q\"\\ 中文/", values.get(0));
	}

	@Test
	public void testStreamNumbers() throws Exception {
		List<Object> values = StreamJsonParser.parseToList("[-1.5e3, 0.25, 42, 1E+2]");
		Assert.assertEquals(4, values.size());
		Assert.assertEquals("-1.5e3", values.get(0));
		Assert.assertEquals("1E+2", values.get(3));
	}

	@Test
	public void testStreamWhitespaceTolerance() throws Exception {
		List<String> events = new ArrayList<>();
		JSONUtil.parseStream(new ByteArrayInputStream(
				" \n\t {\"k\": [ ]} \r\n ".getBytes(StandardCharsets.UTF_8)),
				new JsonHandler() {
					@Override
					public void onStartObject() {
						events.add("SO");
					}

					@Override
					public void onEndObject() {
						events.add("EO");
					}

					@Override
					public void onStartArray() {
						events.add("SA");
					}

					@Override
					public void onEndArray() {
						events.add("EA");
					}

					@Override
					public void onKey(String key) {
						events.add("K:" + key);
					}
				});
		Assert.assertEquals(List.of("SO", "K:k", "SA", "EA", "EO"), events);
	}

	@Test(expected = JSONException.class)
	public void testStreamMalformed() throws Exception {
		StreamJsonParser.parse(new StringReader("{\"a\":}"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamTrailingContent() throws Exception {
		StreamJsonParser.parse(new StringReader("{} extra"), new JsonHandler() {
		});
	}

	@Test(expected = JSONException.class)
	public void testStreamUnclosedString() throws Exception {
		StreamJsonParser.parse(new StringReader("\"abc"), new JsonHandler() {
		});
	}

	@Test
	public void testStreamDepthLimit() throws Exception {
		StringBuilder deep = new StringBuilder();
		for (int i = 0; i < 300; i++) {
			deep.append("{\"a\":");
		}
		try {
			StreamJsonParser.parse(new StringReader(deep.toString()), new JsonHandler() {
			});
			Assert.fail("应抛出深度异常");
		} catch (JSONException expected) {
			Assert.assertTrue("应报深度超限: " + expected.getMessage(), expected.getMessage().contains("深度"));
		}
	}

	@Test
	public void lengthAggregation() {
		Object obj = JSONUtil.parseObj("{\"items\":[{\"name\":\"a\"},{\"name\":\"b\"},{\"name\":\"c\"}],\"tags\":[1,2],\"text\":\"hello\"}");
		Assert.assertEquals(3, JsonPath.eval(obj, "$.items.length()"));
		Assert.assertEquals(3, JsonPath.eval(obj, "$.items.length()"));
		Assert.assertEquals(3, JsonPath.eval(JsonPath.eval(obj, "$.items"), "$.length()"));
		Assert.assertEquals(2, JsonPath.eval(obj, "$.tags.size()"));
		Assert.assertEquals(5, JsonPath.eval(obj, "$.text.length()"));
		Assert.assertEquals(3, JsonPath.eval(JSONUtil.parseObj("{\"x\":{\"a\":1,\"b\":2,\"c\":3}}"), "$.x.length()"));
	}

}