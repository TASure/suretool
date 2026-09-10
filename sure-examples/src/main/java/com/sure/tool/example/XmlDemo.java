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
package com.sure.tool.example;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sure.tool.xml.XmlUtil;

/**
 * XML 工具示例（XmlUtil）。
 */
public class XmlDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== XmlDemo ===");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", "Alice");
		map.put("age", 30);
		String xml = XmlUtil.toXml(map, "person");
		System.out.println("toXml = " + xml);
		System.out.println("parseXml = " + XmlUtil.parseXml(xml));
		System.out.println("escape = " + XmlUtil.escape("<a>&\"'</a>"));
		System.out.println("unescape = " + XmlUtil.unescape("&lt;a&gt;&amp;&quot;&apos;&lt;/a&gt;"));
	}
}
