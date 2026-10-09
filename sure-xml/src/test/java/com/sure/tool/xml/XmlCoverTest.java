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

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/**
 * XML 工具覆盖率二轮补强：readXml/format/getByXPath 及异常分支、XmlException 构造。
 */
public class XmlCoverTest {

	/** 读取合法 XML 文件并解析成功。 */
	@Test
	public void readXml_validFile() throws Exception {
		Path file = Files.createTempFile("sure-xml-", ".xml");
		try {
			Files.writeString(file, "<root><name>Tom</name><age>20</age></root>");
			Object root = XmlUtil.readXml(file.toString()).get("root");
			assertTrue(root instanceof java.util.Map);
			assertEquals("Tom", ((java.util.Map<?, ?>) root).get("name"));
		} finally {
			Files.deleteIfExists(file);
		}
	}

	/** 读取不存在文件触发 XmlException。 */
	@Test
	public void readXml_missingFile() {
		try {
			XmlUtil.readXml("/suretool/not/exist/path/config.xml");
			fail("应抛出 XmlException");
		} catch (XmlException expected) {
			assertTrue(expected.getMessage().contains("读取 XML 文件失败"));
		}
	}

	/** 格式化合法 XML 成功。 */
	@Test
	public void format_valid() {
		String out = XmlUtil.format("<root><a>1</a><b>2</b></root>", 2);
		assertNotNull(out);
		assertTrue(out.contains("<root>"));
	}

	/** 格式化非法 XML 触发 XmlException。 */
	@Test
	public void format_invalid() {
		try {
			XmlUtil.format("<root><a>1</root>", 2);
			fail("应抛出 XmlException");
		} catch (XmlException expected) {
			assertTrue(expected.getMessage().contains("格式化 XML 失败"));
		}
	}

	/** getByXPath 空参数分支。 */
	@Test
	public void getByXPath_nullArgs() {
		assertNull(XmlUtil.getByXPath(null, "/root/name"));
		assertNull(XmlUtil.getByXPath("<root/>", null));
	}

	/** getByXPath 命中与未命中。 */
	@Test
	public void getByXPath_hitAndMiss() {
		String xml = "<root><name>Alice</name></root>";
		assertEquals("Alice", XmlUtil.getByXPath(xml, "/root/name"));
		assertNull(XmlUtil.getByXPath(xml, "/root/nope"));
	}

	/** getByXPath 非法 XML 与非法表达式均返回 null。 */
	@Test
	public void getByXPath_errorPaths() {
		assertNull(XmlUtil.getByXPath("not xml at all", "/root/x"));
		assertNull(XmlUtil.getByXPath("<root/>", "///[[[bad"));
	}

	/** parseXmlToBean 根值为纯文本（解析为空 Map）时走 mapToBean 分支。 */
	@Test
	public void parseXmlToBean_rootIsText() {
		assertNotNull(XmlUtil.parseXmlToBean("<root>just text</root>", Object.class));
	}

	/** toXml 中值为 null 的条目走空值分支。 */
	@Test
	public void toXml_nullValue() {
		java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
		map.put("keep", "v");
		map.put("nil", null);
		String xml = XmlUtil.toXml(map, "root");
		assertTrue(xml.contains("<keep>v</keep>"));
	}

	/** toXml 非法根元素名触发内部异常并包装为 XmlException。 */
	@Test
	public void toXml_illegalRootName() {
		java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
		map.put("a", "b");
		try {
			XmlUtil.toXml(map, "1 illegal name!");
			fail("应抛出 XmlException");
		} catch (XmlException expected) {
			assertTrue(expected.getMessage().contains("Map 转 XML 失败"));
		}
	}

	/** XmlException 两个构造器均可达。 */
	@Test
	public void xmlException_constructors() {
		assertEquals("msg", new XmlException("msg").getMessage());
		Throwable cause = new IllegalStateException("boom");
		XmlException ex = new XmlException("msg2", cause);
		assertEquals("msg2", ex.getMessage());
		assertEquals(cause, ex.getCause());
	}
}
