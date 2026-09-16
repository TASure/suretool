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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * P4（v0.2.0）第十二批：JSONUtil 美化输出测试。
 */
public class P4JsonPrettyTest {

	@Test
	public void testToJsonPrettyStr() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", "suretool");
		map.put("count", 3);
		map.put("active", true);
		map.put("tags", java.util.List.of("a", "b"));

		String pretty = JSONUtil.toJsonPrettyStr(map);
		Assert.assertTrue(pretty.startsWith("{\n"));
		Assert.assertTrue(pretty.contains("\"name\": \"suretool\""));
		Assert.assertTrue(pretty.contains("\"count\": 3"));
		Assert.assertTrue(pretty.contains("[\n"));
		Assert.assertTrue(pretty.endsWith("}"));

		// 空容器
		Assert.assertEquals("{}", JSONUtil.toJsonPrettyStr(new JSONObject()));
		Assert.assertEquals("[]", JSONUtil.toJsonPrettyStr(new JSONArray()));
		// null / 标量
		Assert.assertEquals("null", JSONUtil.toJsonPrettyStr(null));
		Assert.assertEquals("\"x\"", JSONUtil.toJsonPrettyStr("x"));
		Assert.assertEquals("1", JSONUtil.toJsonPrettyStr(1));
		// 转义
		Assert.assertTrue(JSONUtil.toJsonPrettyStr("a\"b").contains("\"a\\\"b\""));
	}
}
