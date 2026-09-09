package com.sure.tool.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * JSONUtil / JSONObject / JSONArray 测试。
 */
public class JsonTest {

	public static class User {
		private String name;
		private int age;
		private boolean active;
		private List<String> tags;

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

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}

	@Test
	public void testToJsonStrBasic() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", "x");
		map.put("c", true);
		map.put("d", null);
		assertEquals("{\"a\":1,\"b\":\"x\",\"c\":true,\"d\":null}", JSONUtil.toJsonStr(map));
	}

	@Test
	public void testToJsonStrList() {
		List<Object> list = new ArrayList<>();
		list.add(1);
		list.add("x");
		list.add(true);
		assertEquals("[1,\"x\",true]", JSONUtil.toJsonStr(list));
	}

	@Test
	public void testToJsonStrEscape() {
		assertEquals("\"a\\\"b\\\\c\\nd\"", JSONUtil.toJsonStr("a\"b\\c\nd"));
		assertEquals("\"\\u001f\"", JSONUtil.toJsonStr("\u001f"));
	}

	@Test
	public void testToJsonStrBean() {
		User user = new User();
		user.setName("sure");
		user.setAge(18);
		user.setActive(true);
		String json = JSONUtil.toJsonStr(user);
		assertTrue(json.contains("\"name\":\"sure\""));
		assertTrue(json.contains("\"age\":18"));
	}

	@Test
	public void testToJsonStrDate() {
		String json = JSONUtil.toJsonStr(new Date());
		assertTrue(json.matches("\"\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\""));
	}

	@Test
	public void testParseObject() {
		JSONObject obj = JSONUtil.parseObj("{\"a\":1,\"b\":\"x\",\"c\":true,\"d\":null,\"e\":[1,2],\"f\":{\"g\":\"h\"}}");
		assertEquals(Integer.valueOf(1), obj.getInt("a"));
		assertEquals("x", obj.getStr("b"));
		assertEquals(Boolean.TRUE, obj.getBool("c"));
		assertNull(obj.get("d"));
		assertEquals(Integer.valueOf(2), obj.getJSONArray("e").getInt(1));
		assertEquals("h", obj.getJSONObject("f").getStr("g"));
	}

	@Test
	public void testParseArray() {
		JSONArray array = JSONUtil.parseArray("[1,\"x\",true,null]");
		assertEquals(Integer.valueOf(1), array.getInt(0));
		assertEquals("x", array.getStr(1));
		assertEquals(Boolean.TRUE, array.getBool(2));
		assertNull(array.get(3));
		assertNull(array.getInt(9));
	}

	@Test
	public void testParseNumberPrecision() {
		JSONObject obj = JSONUtil.parseObj("{\"n\":0.12345678901234567890123}");
		Object value = obj.get("n");
		assertTrue(value instanceof BigDecimal);
		assertEquals("0.12345678901234567890123", value.toString());
	}

	@Test
	public void testParseEscapes() {
		JSONObject obj = JSONUtil.parseObj("{\"s\":\"a\\\"b\\\\c\\/d\\ne\\tf\\u4e2d\"}");
		assertEquals("a\"b\\c/d\ne\tf中", obj.getStr("s"));
	}

	@Test
	public void testRoundTrip() {
		String json = "{\"a\":1,\"b\":[1,2,3],\"c\":{\"d\":\"e\"},\"f\":false}";
		assertEquals(json, JSONUtil.toJsonStr(JSONUtil.parse(json)));
	}

	@Test
	public void testParseWhitespace() {
		JSONObject obj = JSONUtil.parseObj("  { \"a\" : 1 , \"b\" : 2 }  ");
		assertEquals(Integer.valueOf(1), obj.getInt("a"));
		assertEquals(Integer.valueOf(2), obj.getInt("b"));
	}

	@Test
	public void testParseError() {
		try {
			JSONUtil.parseObj("{");
			fail("应抛出 JSONException");
		} catch (JSONException e) {
			// 预期
		}
		try {
			JSONUtil.parseObj("{a:1}");
			fail("应抛出 JSONException");
		} catch (JSONException e) {
			// 预期
		}
		try {
			JSONUtil.parse("1 2");
			fail("应抛出 JSONException");
		} catch (JSONException e) {
			// 预期
		}
	}

	@Test
	public void testToBean() {
		User user = JSONUtil.toBean("{\"name\":\"sure\",\"age\":\"18\",\"active\":true,\"tags\":[\"a\",\"b\"]}",
				User.class);
		assertNotNull(user);
		assertEquals("sure", user.getName());
		assertEquals(18, user.getAge());
		assertTrue(user.isActive());
	}

	@Test
	public void testJSONObjectChain() {
		JSONObject obj = new JSONObject().set("name", "sure").set("age", 20);
		assertEquals("sure", obj.getStr("name"));
		assertEquals(Integer.valueOf(20), obj.getInt("age"));
		assertNull(obj.getStr("notExist"));
		assertNull(obj.getInt("notExist"));
		assertEquals("default", obj.getStr("notExist", "default"));
	}

	@Test
	public void testJSONObjectFromBean() {
		User user = new User();
		user.setName("sure");
		user.setAge(18);
		JSONObject obj = new JSONObject(user);
		assertEquals("sure", obj.getStr("name"));
		assertEquals(Integer.valueOf(18), obj.getInt("age"));
	}

	@Test
	public void testJSONArrayApi() {
		JSONArray array = new JSONArray(1, "x", true);
		assertEquals(3, array.size());
		assertEquals("x", array.getStr(1));
		assertEquals(Boolean.TRUE, array.getBool(2));
		assertEquals("[1,\"x\",true]", array.toJsonString());
	}

	@Test
	public void testIsJson() {
		assertTrue(JSONUtil.isJson("{}"));
		assertTrue(JSONUtil.isJson(" [1] "));
		assertFalse(JSONUtil.isJson("abc"));
		assertFalse(JSONUtil.isJson(""));
		assertFalse(JSONUtil.isJson(null));
	}

	@Test
	public void testNestedBean() {
		JSONObject obj = JSONUtil.parseObj("{\"user\":{\"name\":\"sure\",\"age\":18}}");
		User user = obj.getBean("user", User.class);
		assertNotNull(user);
		assertEquals("sure", user.getName());
		assertEquals(18, user.getAge());
	}
}
