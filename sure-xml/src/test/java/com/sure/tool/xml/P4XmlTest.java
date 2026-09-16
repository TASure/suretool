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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * P4（v0.2.0）第十二批：XmlUtil 文件读取/格式化测试。
 */
public class P4XmlTest {

	@Test
	public void testReadXmlAndFormat() throws IOException {
		String xml = "<root><name>sure</name><age>21</age></root>";

		String formatted = XmlUtil.format(xml, 2);
		Assert.assertTrue(formatted.contains("<root>"));
		Assert.assertTrue(formatted.contains("<name>sure</name>"));
		Assert.assertTrue(formatted.contains("\n"));

		// 写临时文件再读
		Path tmp = Files.createTempFile("sure-xml-", ".xml");
		try {
			Files.writeString(tmp, xml);
			Map<String, Object> map = XmlUtil.readXml(tmp.toString());
			Assert.assertTrue(map.containsKey("root"));
		} finally {
			Files.deleteIfExists(tmp);
		}
	}

	@Test
	public void testGetByXPath() {
		String xml = "<root><user id=\"1\"><name>Zhang</name><age>30</age></user></root>";
		Assert.assertEquals("Zhang", com.sure.tool.xml.XmlUtil.getByXPath(xml, "/root/user/name"));
		Assert.assertEquals("30", com.sure.tool.xml.XmlUtil.getByXPath(xml, "/root/user/age"));
		Assert.assertNull(com.sure.tool.xml.XmlUtil.getByXPath(xml, "/root/missing"));
		Assert.assertNull(com.sure.tool.xml.XmlUtil.getByXPath(null, "/root"));
	}


}
