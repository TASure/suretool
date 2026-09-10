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
package com.sure.tool.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * XML 工具覆盖率补强：toXml/parseXml/parseXmlToBean/escape/unescape 行为路径。
 */
public class XmlGapTest {

	@Test
	public void toXml_map() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", "Alice");
		map.put("age", 30);
		List<String> tags = new ArrayList<>();
		tags.add("a");
		tags.add("b");
		map.put("tags", tags);
		String xml = XmlUtil.toXml(map, "root");
		assertTrue(xml.contains("<name>Alice</name>"));
		assertTrue(xml.contains("<age>30</age>"));
		assertTrue(xml.contains("<tags>a</tags>"));
		assertTrue(xml.contains("<tags>b</tags>"));
	}

	@Test
	public void toXml_mapNested() {
		Map<String, Object> inner = new LinkedHashMap<>();
		inner.put("city", "Xi'an");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("address", inner);
		String xml = XmlUtil.toXml(map, "root");
		assertTrue(xml.contains("<address>"));
		assertTrue(xml.contains("<city>Xi'an</city>"));
	}

	@Test
	public void toXml_nullMap() {
		assertNotNull(XmlUtil.toXml((Map<String, Object>) null, "root"));
	}

	@Test
	public void toXml_bean() {
		XmlBean bean = new XmlBean();
		bean.setName("Bob");
		bean.setAge(25);
		String xml = XmlUtil.toXml(bean, "person");
		assertTrue(xml.contains("<name>Bob</name>"));
		assertTrue(xml.contains("<age>25</age>"));
	}

	@Test
	public void parseXml_simple() {
		Map<String, Object> map = XmlUtil.parseXml("<root><name>Alice</name><age>30</age></root>");
		assertTrue(map.containsKey("root"));
		Object value = map.get("root");
		assertTrue(value instanceof Map);
		Map<?, ?> props = (Map<?, ?>) value;
		assertEquals("Alice", props.get("name"));
		assertEquals("30", props.get("age"));
	}

	@Test
	public void parseXml_listAndNested() {
		Map<String, Object> map = XmlUtil.parseXml(
				"<root><tag>x</tag><tag>y</tag><addr><city>Xi'an</city></addr></root>");
		Map<?, ?> props = (Map<?, ?>) map.get("root");
		Object tags = props.get("tag");
		assertTrue(tags instanceof List);
		assertEquals(2, ((List<?>) tags).size());
		Object addr = props.get("addr");
		assertTrue(addr instanceof Map);
		assertEquals("Xi'an", ((Map<?, ?>) addr).get("city"));
	}

	@Test
	public void parseXmlToBean() {
		XmlBean bean = XmlUtil.parseXmlToBean("<person><name>Bob</name><age>25</age></person>", XmlBean.class);
		assertNotNull(bean);
		assertEquals("Bob", bean.getName());
		assertEquals(25, bean.getAge());
	}

	@Test
	public void escape_and_unescape() {
		String escaped = XmlUtil.escape("<a b=\"c\">&'d'</a>");
		assertEquals("&lt;a b=&quot;c&quot;&gt;&amp;&apos;d&apos;&lt;/a&gt;", escaped);
		assertEquals("<a b=\"c\">&'d'</a>", XmlUtil.unescape(escaped));
		assertNull(XmlUtil.escape(null));
		assertNull(XmlUtil.unescape(null));
		assertEquals("plain", XmlUtil.escape("plain"));
		assertEquals("plain", XmlUtil.unescape("plain"));
	}

	@Test
	public void parseXml_invalidInput() {
		try {
			XmlUtil.parseXml("this is not xml");
			fail("should throw");
		} catch (XmlException expected) {
			// 预期行为
		}
	}

	/** XML Bean 测试载体。 */
	public static class XmlBean {

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
