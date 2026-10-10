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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * JsonPath 缓冲补测：通过反射构造 CompareNode 驱动 readPath 的 [...] 索引分支
 * （Map 引号键 / List 数字索引 / 标量元素）、CompareNode 未知操作符防御分支。
 * 目标是把 sure-json 本地覆盖率从擦线 0.9812 提升，留出 CI 波动裕量。
 */
public class P6JsonGapTest {

	private static final String BOOKS_JSON = "{"
			+ "\"store\": {"
			+ "\"book\": ["
			+ "{\"category\": \"reference\", \"author\": \"Nigel Rees\", \"title\": \"Sayings of the Century\", \"price\": 8.95},"
			+ "{\"category\": \"fiction\", \"author\": \"Evelyn Waugh\", \"title\": \"Sword of Honour\", \"price\": 12.99}"
			+ "],"
			+ "\"bicycle\": {\"color\": \"red\", \"price\": 19.95}"
			+ "}"
			+ "}";

	/** 反射构造 CompareNode，path 用 ['key'] 括号键 → readPath Map 分支。 */
	@Test
	public void testReadPathBracketMapKey() throws Exception {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		Object book0 = JsonPath.eval(obj, "$.store.book[0]");
		Object node = compareNode("==", "['price']", 8.95);
		Assert.assertTrue((Boolean) eval(node, book0));
	}

	/** path 用 [0] 数字索引 → readPath List 分支。 */
	@Test
	public void testReadPathBracketListIndex() throws Exception {
		Object node = compareNode("==", "[0]", "a");
		Assert.assertTrue((Boolean) eval(node, Arrays.asList("a", "b")));
	}

	/** current 为标量（非 Map/List）→ readPath else 分支（node 置 null）。 */
	@Test
	public void testReadPathOnScalar() throws Exception {
		Object node = compareNode("==", "[0]", "x");
		Assert.assertFalse((Boolean) eval(node, "hello"));
	}

	/** CompareNode 未知操作符 → default 返回 false（防御分支）。 */
	@Test
	public void testCompareNodeUnknownOp() throws Exception {
		JSONObject obj = JSONUtil.parseObj(BOOKS_JSON);
		Object node = compareNode("???", "price", 1);
		Assert.assertFalse((Boolean) eval(node, obj));
	}

	private static Object compareNode(String op, String path, Object literal) throws Exception {
		Class<?> cn = Class.forName("com.sure.tool.json.JsonPath$CompareNode");
		java.lang.reflect.Constructor<?> ctor =
				cn.getDeclaredConstructor(String.class, String.class, Object.class);
		ctor.setAccessible(true);
		return ctor.newInstance(op, path, literal);
	}

	private static Object eval(Object node, Object current) throws Exception {
		java.lang.reflect.Method eval =
				node.getClass().getDeclaredMethod("eval", Object.class);
		eval.setAccessible(true);
		return eval.invoke(node, current);
	}
}
