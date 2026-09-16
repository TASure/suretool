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

import java.util.HashMap;
import java.util.Map;

/**
 * P4（v0.2.0）第十四批：JSONObject 链式批量设置测试。
 */
public class P4JsonSetAllTest {

	@Test
	public void testSetAll() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", "suretool");
		map.put("version", "0.2.0");

		JSONObject obj = new JSONObject();
		JSONObject result = obj.setAll(map);
		Assert.assertSame(obj, result);
		Assert.assertEquals("suretool", obj.getStr("name"));
		Assert.assertEquals("0.2.0", obj.getStr("version"));

		// null 安全
		obj.setAll(null);
		Assert.assertEquals("suretool", obj.getStr("name"));
	}
}
