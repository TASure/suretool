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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sure.tool.bean.BeanUtil;

/**
 * XML 工具类（最小实现）：Map/Bean 与 XML 互转、转义，基于 JDK DOM，参考 Hutool 的 {@code XmlUtil} 设计。
 * <p>
 * 仅支持元素层级与文本内容（忽略属性）；同名兄弟元素解析为 List。
 *
 * @author suretool
 * @since 0.1.0
 */
public class XmlUtil {

	private XmlUtil() {
	}

	/**
	 * Map 转 XML 字符串。
	 *
	 * @param map      Map（值支持基本类型、Map、List/Collection）
	 * @param rootName 根元素名
	 * @return XML 字符串
	 */
	public static String toXml(Map<String, Object> map, String rootName) {
		try {
			Document doc = newDocument();
			Element root = doc.createElement(rootName);
			doc.appendChild(root);
			appendMap(doc, root, map);
			return toString(doc);
		} catch (Exception e) {
			throw new XmlException("Map 转 XML 失败", e);
		}
	}

	/**
	 * Bean 转 XML 字符串。
	 *
	 * @param bean     Bean 对象
	 * @param rootName 根元素名
	 * @return XML 字符串
	 */
	public static String toXml(Object bean, String rootName) {
		return toXml(BeanUtil.beanToMap(bean, false), rootName);
	}

	/**
	 * 解析 XML 为 Map（根元素名作为键）。
	 *
	 * @param xml XML 字符串
	 * @return Map
	 */
	public static Map<String, Object> parseXml(String xml) {
		try {
			DocumentBuilder builder = newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			Element root = doc.getDocumentElement();
			Map<String, Object> map = new LinkedHashMap<>();
			map.put(root.getNodeName(), parseElement(root));
			return map;
		} catch (Exception e) {
			throw new XmlException("XML 解析失败", e);
		}
	}

	/**
	 * 解析 XML 为 Bean。
	 *
	 * @param xml       XML 字符串
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 实例
	 */
	public static <T> T parseXmlToBean(String xml, Class<T> beanClass) {
		Map<String, Object> map = parseXml(xml);
		Object rootValue = map.values().iterator().hasNext() ? map.values().iterator().next() : null;
		if (rootValue instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> props = (Map<String, Object>) rootValue;
			return BeanUtil.mapToBean(props, beanClass);
		}
		return null;
	}

	/**
	 * XML 转义（5 个预定义实体）。
	 *
	 * @param text 原文
	 * @return 转义结果
	 */
	public static String escape(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&apos;");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * XML 反转义。
	 *
	 * @param text 转义文本
	 * @return 原文
	 */
	public static String unescape(String text) {
		if (text == null) {
			return null;
		}
		return text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
				.replace("&apos;", "'").replace("&amp;", "&");
	}

	private static void appendMap(Document doc, Element parent, Map<String, Object> map) {
		if (map == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			appendValue(doc, parent, entry.getKey(), entry.getValue());
		}
	}

	private static void appendValue(Document doc, Element parent, String name, Object value) {
		Element element = doc.createElement(name);
		parent.appendChild(element);
		if (value == null) {
			return;
		}
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> child = (Map<String, Object>) value;
			appendMap(doc, element, child);
		} else if (value instanceof Iterable) {
			for (Object item : (Iterable<?>) value) {
				appendValue(doc, parent, name, item);
			}
			parent.removeChild(element);
		} else {
			element.setTextContent(String.valueOf(value));
		}
	}

	private static Map<String, Object> parseElement(Element element) {
		Map<String, Object> result = new LinkedHashMap<>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element child = (Element) node;
			String name = child.getNodeName();
			Object value;
			if (hasElementChildren(child)) {
				value = parseElement(child);
			} else {
				value = child.getTextContent();
			}
			Object existing = result.get(name);
			if (existing == null) {
				result.put(name, value);
			} else if (existing instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) existing;
				list.add(value);
			} else {
				List<Object> list = new ArrayList<>();
				list.add(existing);
				list.add(value);
				result.put(name, list);
			}
		}
		return result;
	}

	private static boolean hasElementChildren(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return true;
			}
		}
		return false;
	}

	private static Document newDocument() throws Exception {
		return newDocumentBuilder().newDocument();
	}

	private static DocumentBuilder newDocumentBuilder() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder();
	}

	private static String toString(Document doc) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}
}