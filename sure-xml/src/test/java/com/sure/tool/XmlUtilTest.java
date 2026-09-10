package com.sure.tool.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * XmlUtil 测试：转义、Map/Bean 往返、同名元素转 List。
 */
public class XmlUtilTest {

	@Test
	public void testXmlEscape() {
		assertEquals("a&lt;b&amp;c&quot;d", XmlUtil.escape("a<b&c\"d"));
		assertEquals("a<b&c\"d", XmlUtil.unescape("a&lt;b&amp;c&quot;d"));
		assertNull(XmlUtil.escape(null));
	}

	@Test
	public void testXmlMapRoundTrip() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", "sure");
		map.put("age", 18);
		map.put("active", true);
		String xml = XmlUtil.toXml(map, "user");
		assertTrue(xml.contains("<name>sure</name>"));
		assertTrue(xml.contains("<age>18</age>"));

		Map<String, Object> parsed = XmlUtil.parseXml(xml);
		assertTrue(parsed.containsKey("user"));
		Object root = parsed.get("user");
		assertTrue(root instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, Object> user = (Map<String, Object>) root;
		assertEquals("sure", user.get("name"));
		assertEquals("18", user.get("age"));
		assertEquals("true", user.get("active"));
	}

	@Test
	public void testXmlList() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("item", Arrays.asList("a", "b", "c"));
		String xml = XmlUtil.toXml(map, "root");
		assertEquals(3, countOccurrences(xml, "<item>"));

		Map<String, Object> parsed = XmlUtil.parseXml(xml);
		@SuppressWarnings("unchecked")
		Map<String, Object> root = (Map<String, Object>) parsed.get("root");
		assertTrue(root.get("item") instanceof List);
		assertEquals(3, ((List<?>) root.get("item")).size());
	}

	@Test
	public void testXmlBeanRoundTrip() {
		Person person = new Person();
		person.setName("sure");
		person.setAge(18);
		String xml = XmlUtil.toXml(person, "person");
		Person parsed = XmlUtil.parseXmlToBean(xml, Person.class);
		assertEquals("sure", parsed.getName());
		assertEquals(18, parsed.getAge());
	}

	private static int countOccurrences(String text, String token) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(token, index)) >= 0) {
			count++;
			index += token.length();
		}
		return count;
	}

	/**
	 * 测试 Bean。
	 */
	public static class Person {

		private String name;
		private int age;

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
	}
}
