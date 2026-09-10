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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * JSON 解析/序列化 fuzz 与边界测试：畸形输入、深度嵌套、超大数值、特殊字符、类型边界。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JsonFuzzTest {

	// ===== 序列化：类型覆盖 =====

	@Test
	public void toJsonStr_coversAllTypes() {
		Assert.assertEquals("null", JSONUtil.toJsonStr(null));
		Assert.assertEquals("1", JSONUtil.toJsonStr(1));
		Assert.assertEquals("1.5", JSONUtil.toJsonStr(1.5D));
		Assert.assertEquals("9223372036854775807", JSONUtil.toJsonStr(Long.MAX_VALUE));
		Assert.assertEquals("true", JSONUtil.toJsonStr(true));
		Assert.assertEquals("false", JSONUtil.toJsonStr(false));
		Assert.assertEquals("\"abc\"", JSONUtil.toJsonStr("abc"));
		Assert.assertEquals("\"c\"", JSONUtil.toJsonStr('c'));
		Assert.assertEquals("1E+400", JSONUtil.toJsonStr(new BigDecimal("1E+400")));
		Assert.assertEquals("[1,2,3]", JSONUtil.toJsonStr(new int[] {1, 2, 3}));
		Assert.assertEquals("[\"a\",\"b\"]", JSONUtil.toJsonStr(new String[] {"a", "b"}));
		Assert.assertEquals("[1,2]", JSONUtil.toJsonStr(new Iterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return Arrays.asList(1, 2).iterator();
			}
		}));
		Assert.assertEquals("{\"k\":\"v\"}", JSONUtil.toJsonStr(new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("k", "v");
			}
		}));
	}

	@Test
	public void toJsonStr_escapesSpecialChars() {
		String json = JSONUtil.toJsonStr("a\"b\\c\nd\re\tf\bg\fh\u001f");
		Assert.assertEquals("\"a\\\"b\\\\c\\nd\\re\\tf\\bg\\fh\\u001f\"", json);
	}

	@Test
	public void toJsonStr_nestedAndDateAndBean() {
		Map<String, Object> nested = new LinkedHashMap<>();
		nested.put("list", Arrays.asList(1, "x", true));
		nested.put("inner", new LinkedHashMap<String, Object>());
		Assert.assertEquals("{\"list\":[1,\"x\",true],\"inner\":{}}", JSONUtil.toJsonStr(nested));

		String dateJson = JSONUtil.toJsonStr(new Date(0));
		Assert.assertTrue(dateJson.startsWith("\"") && dateJson.endsWith("\""));

		String beanJson = JSONUtil.toJsonStr(new UserBean("tom", 18, Arrays.asList("a", "b")));
		Assert.assertTrue(beanJson.contains("\"name\":\"tom\""));
		Assert.assertTrue(beanJson.contains("\"age\":18"));
		Assert.assertTrue(beanJson.contains("\"tags\":[\"a\",\"b\"]"));
	}

	// ===== 解析：合法输入 =====

	@Test
	public void parse_validScalars() {
		Assert.assertEquals(Long.valueOf(1L), JSONUtil.parse("1"));
		Assert.assertEquals(new BigDecimal("1.5"), JSONUtil.parse("1.5"));
		Assert.assertEquals(new BigInteger("123456789012345678901234567890"), JSONUtil.parse("123456789012345678901234567890"));
		Assert.assertEquals(Boolean.TRUE, JSONUtil.parse("true"));
		Assert.assertEquals(Boolean.FALSE, JSONUtil.parse("false"));
		Assert.assertNull(JSONUtil.parse("null"));
		Assert.assertEquals("hello", JSONUtil.parse("\"hello\""));
	}

	@Test
	public void parse_validWhitespace() {
		Assert.assertEquals("x", JSONUtil.parse("  \t\n\r \"x\" "));
	}

	@Test
	public void parse_validEscapes() {
		Assert.assertEquals("a\"b\\c/d\b\f\n\r\t", JSONUtil.parse("\"a\\\"b\\\\c\\/d\\b\\f\\n\\r\\t\""));
		Assert.assertEquals("中", JSONUtil.parse("\"\\u4e2d\""));
	}

	@Test
	public void parse_validObject() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":1,\"b\":\"x\",\"c\":true,\"d\":null,\"e\":[1,2],\"f\":{\"g\":2}}");
		Assert.assertEquals(6, obj.size());
		Assert.assertEquals(Long.valueOf(1L), obj.get("a"));
		Assert.assertEquals("x", obj.get("b"));
		Assert.assertEquals(Boolean.TRUE, obj.get("c"));
		Assert.assertNull(obj.get("d"));
		Assert.assertTrue(obj.get("e") instanceof JSONArray);
		Assert.assertTrue(obj.get("f") instanceof JSONObject);
	}

	@Test
	public void parse_validEmptyObjectAndArray() {
		Assert.assertEquals(0, JSONUtil.parseObj("{}").size());
		Assert.assertEquals(0, JSONUtil.parseArray("[]").size());
	}

	@Test
	public void parse_validArrayMixed() {
		JSONArray arr = JSONUtil.parseArray("[1,\"a\",true,null,[2],{\"k\":1}]");
		Assert.assertEquals(6, arr.size());
		Assert.assertTrue(arr.get(4) instanceof JSONArray);
		Assert.assertTrue(arr.get(5) instanceof JSONObject);
	}

	@Test
	public void parse_exponentNumbers() {
		Assert.assertEquals(new BigDecimal("1e10"), JSONUtil.parse("1e10"));
		Assert.assertEquals(new BigDecimal("1E+400"), JSONUtil.parse("1E+400"));
	}

	// ===== 解析：畸形输入 =====

	@Test(expected = JSONException.class)
	public void parse_nullInputThrows() {
		JSONUtil.parse(null);
	}

	@Test(expected = JSONException.class)
	public void parse_emptyInputThrows() {
		JSONUtil.parse("");
	}

	@Test(expected = JSONException.class)
	public void parse_blankInputThrows() {
		JSONUtil.parse("   ");
	}

	@Test(expected = JSONException.class)
	public void parse_unclosedObjectThrows() {
		JSONUtil.parse("{");
	}

	@Test(expected = JSONException.class)
	public void parse_unclosedArrayThrows() {
		JSONUtil.parse("[1,2,");
	}

	@Test(expected = JSONException.class)
	public void parse_missingValueThrows() {
		JSONUtil.parse("{\"a\":}");
	}

	@Test(expected = JSONException.class)
	public void parse_missingColonThrows() {
		JSONUtil.parse("{\"a\" 1}");
	}

	@Test(expected = JSONException.class)
	public void parse_missingCommaThrows() {
		JSONUtil.parse("{\"a\":1 \"b\":2}");
	}

	@Test(expected = JSONException.class)
	public void parse_wrongBracketThrows() {
		JSONUtil.parse("[}");
	}

	@Test(expected = JSONException.class)
	public void parse_trailingGarbageThrows() {
		JSONUtil.parse("{\"a\":1} extra");
	}

	@Test(expected = JSONException.class)
	public void parse_truncatedLiteralThrows() {
		JSONUtil.parse("tru");
	}

	@Test(expected = JSONException.class)
	public void parse_truncatedNullThrows() {
		JSONUtil.parse("nul");
	}

	@Test(expected = JSONException.class)
	public void parse_badNumberThrows() {
		JSONUtil.parse("1.2.3");
	}

	@Test(expected = JSONException.class)
	public void parse_loneDotThrows() {
		JSONUtil.parse(".");
	}

	@Test(expected = JSONException.class)
	public void parse_unclosedStringThrows() {
		JSONUtil.parse("\"abc");
	}

	@Test(expected = JSONException.class)
	public void parse_invalidEscapeThrows() {
		JSONUtil.parse("\"\\q\"");
	}

	@Test(expected = JSONException.class)
	public void parse_shortUnicodeThrows() {
		JSONUtil.parse("\"\\u12\"");
	}

	@Test(expected = JSONException.class)
	public void parse_badUnicodeThrows() {
		JSONUtil.parse("\"\\uZZZZ\"");
	}

	@Test(expected = JSONException.class)
	public void parse_bareKeyThrows() {
		JSONUtil.parse("{a:1}");
	}

	@Test(expected = JSONException.class)
	public void parseObj_onArrayThrows() {
		JSONUtil.parseObj("[1]");
	}

	@Test(expected = JSONException.class)
	public void parseArray_onObjectThrows() {
		JSONUtil.parseArray("{}");
	}

	// ===== 解析：fuzz 专项 =====

	@Test
	public void fuzz_deepNestingParses() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			sb.append('[');
		}
		sb.append('1');
		for (int i = 0; i < 1000; i++) {
			sb.append(']');
		}
		Object value = JSONUtil.parse(sb.toString());
		JSONArray cur = (JSONArray) value;
		for (int i = 0; i < 999; i++) {
			cur = cur.getJSONArray(0);
		}
		Assert.assertEquals(Long.valueOf(1L), cur.get(0));
	}

	@Test
	public void fuzz_roundTripLongString() {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 100000; i++) {
			content.append('x');
		}
		String json = JSONUtil.toJsonStr(content.toString());
		Assert.assertEquals(content.toString(), JSONUtil.parse(json));
	}

	@Test
	public void fuzz_roundTripAllUnicodeEscape() {
		String json = "\"\\u0000\\u0001\\u001f\\u007f\\u00ff\\u4e2d\"";
		String parsed = (String) JSONUtil.parse(json);
		Assert.assertEquals(6, parsed.length());
		Assert.assertEquals('\u0000', parsed.charAt(0));
		Assert.assertEquals('中', parsed.charAt(5));
	}

	@Test
	public void fuzz_negativeZeroAndBigIntRoundTrip() {
		Assert.assertEquals("0", JSONUtil.toJsonStr(new BigInteger("0")));
		Assert.assertEquals(new BigInteger("999999999999999999999999999999"), JSONUtil.parse("999999999999999999999999999999"));
	}

	@Test
	public void fuzz_serializeCircularReferenceFailsGracefully() {
		Map<String, Object> map = new HashMap<>();
		map.put("self", map);
		try {
			JSONUtil.toJsonStr(map);
			Assert.fail("循环引用应导致栈溢出/异常而非死循环");
		} catch (StackOverflowError | RuntimeException expected) {
			// 递归序列化对循环引用无保护，抛异常即达标（不挂死）
		}
	}

	// ===== JSONObject API =====

	@Test
	public void jsonObject_constructors() {
		Assert.assertEquals(0, new JSONObject().size());
		Assert.assertEquals(2, new JSONObject("{\"a\":1,\"b\":2}").size());
		Map<String, Object> map = new HashMap<>();
		map.put("k", 1);
		Assert.assertEquals(Integer.valueOf(1), new JSONObject(map).get("k"));
		UserBean bean = new UserBean("n", 1, null);
		Assert.assertEquals("n", new JSONObject(bean).get("name"));
		Assert.assertFalse(new JSONObject(bean, true).containsKey("tags"));
		Assert.assertTrue(new JSONObject(bean, false).containsKey("tags"));
		Assert.assertNull(new JSONObject((Map<?, ?>) null).get("x"));
	}

	@Test
	public void jsonObject_setAndGet() {
		JSONObject obj = new JSONObject();
		obj.set("s", "str").set("i", 42).set("l", 42L).set("d", 1.5D).set("b", true);
		obj.set("nested", new LinkedHashMap<String, Object>());
		obj.set("arr", Arrays.asList(1, 2));

		Assert.assertEquals("str", obj.getStr("s"));
		Assert.assertEquals("def", obj.getStr("missing", "def"));
		Assert.assertNull(obj.getStr("missing"));
		Assert.assertEquals(Integer.valueOf(42), obj.getInt("i"));
		Assert.assertEquals(Long.valueOf(42L), obj.getLong("l"));
		Assert.assertEquals(Double.valueOf(1.5D), obj.getDouble("d"));
		Assert.assertEquals(Boolean.TRUE, obj.getBool("b"));
		Assert.assertNull(obj.getInt("missing"));
		Assert.assertEquals(Integer.valueOf(7), obj.set("si", "7").getInt("si"));
		Assert.assertNull(obj.set("bad", "abc").getInt("bad"));
		Assert.assertEquals(Boolean.FALSE, obj.set("fb", "false").getBool("fb"));
		Assert.assertTrue(obj.getJSONObject("nested") instanceof JSONObject);
		Assert.assertNull(obj.getJSONObject("missing"));
		Assert.assertTrue(obj.getJSONArray("arr") instanceof JSONArray);
		Assert.assertNull(obj.getJSONArray("missing"));
		UserBean nestedBean = obj.set("nested", new JSONObject().set("x", 1)).getBean("nested", UserBean.class);
		Assert.assertNotNull(nestedBean);
		Assert.assertNull(obj.getBean("missing", UserBean.class));
		Assert.assertTrue(obj.toJsonString().startsWith("{"));
		Assert.assertEquals(obj.toString(), obj.toJsonString());
	}

	@Test
	public void jsonObject_beanRoundTrip() {
		JSONObject obj = new JSONObject(new UserBean("tom", 18, Arrays.asList("a")));
		UserBean bean = JSONUtil.toBean(obj, UserBean.class);
		Assert.assertEquals("tom", bean.getName());
		Assert.assertEquals(18, bean.getAge());
	}

	@Test
	public void jsonObject_parseStatic() {
		JSONObject obj = JSONObject.parse("{\"a\":1}");
		Assert.assertEquals(Long.valueOf(1L), obj.get("a"));
	}

	// ===== JSONArray API =====

	@Test
	public void jsonArray_constructors() {
		Assert.assertEquals(0, new JSONArray().size());
		Assert.assertEquals(2, new JSONArray("[1,2]").size());
		Assert.assertEquals(2, new JSONArray(Arrays.asList(1, 2)).size());
		Assert.assertEquals(3, new JSONArray(1, "a", true).size());
		Assert.assertEquals(0, new JSONArray((Collection<?>) null).size());
		Assert.assertEquals(0, new JSONArray((Object[]) null).size());
	}

	@Test
	public void jsonArray_typedGet() {
		JSONArray arr = new JSONArray("[\"s\",7,\"8\",1.5,true,{\"k\":1},[1,2]]");
		Assert.assertEquals("s", arr.getStr(0));
		Assert.assertEquals(Integer.valueOf(7), arr.getInt(1));
		Assert.assertEquals(Long.valueOf(8L), arr.getLong(2));
		Assert.assertEquals(Double.valueOf(1.5D), arr.getDouble(3));
		Assert.assertEquals(Boolean.TRUE, arr.getBool(4));
		Assert.assertEquals(Integer.valueOf(1), arr.getJSONObject(5).getInt("k"));
		Assert.assertEquals(Long.valueOf(1L), arr.getJSONArray(6).getLong(0));
		Assert.assertNull(arr.getStr(99));
		Assert.assertNull(arr.getInt(-1));
		Assert.assertNull(arr.getJSONObject(99));
		Assert.assertNull(arr.getJSONArray(99));
		Assert.assertNull(arr.getBean(99, UserBean.class));
	}

	@Test
	public void jsonArray_parseStaticAndToString() {
		JSONArray arr = JSONArray.parse("[1]");
		Assert.assertEquals(1, arr.size());
		Assert.assertTrue(arr.toJsonString().startsWith("["));
		Assert.assertEquals(arr.toString(), arr.toJsonString());
	}

	// ===== isJson =====

	@Test
	public void isJson_variants() {
		Assert.assertFalse(JSONUtil.isJson(null));
		Assert.assertFalse(JSONUtil.isJson(""));
		Assert.assertFalse(JSONUtil.isJson("   "));
		Assert.assertFalse(JSONUtil.isJson("1"));
		Assert.assertFalse(JSONUtil.isJson("\"x\""));
		Assert.assertTrue(JSONUtil.isJson("{}"));
		Assert.assertTrue(JSONUtil.isJson("[1]"));
		Assert.assertTrue(JSONUtil.isJson("  {a:1}"));
	}

	// ===== 测试 Bean =====

	/**
	 * 测试用 Bean。
	 */
	public static class UserBean {

		private String name;
		private int age;
		private List<String> tags;

		/**
		 * 无参构造。
		 */
		public UserBean() {
		}

		/**
		 * 全参构造。
		 *
		 * @param name 姓名
		 * @param age  年龄
		 * @param tags 标签
		 */
		public UserBean(String name, int age, List<String> tags) {
			this.name = name;
			this.age = age;
			this.tags = tags;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}
}
