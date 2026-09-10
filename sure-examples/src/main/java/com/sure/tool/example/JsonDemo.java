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

import java.util.Map;

import com.sure.tool.json.JSONObject;
import com.sure.tool.json.JSONUtil;

/**
 * JSON 工具示例（JSONUtil / JSONObject）。
 */
public class JsonDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== JsonDemo ===");
		Map<String, Object> map = Map.of("name", "Alice", "age", 30);
		String json = JSONUtil.toJsonStr(map);
		System.out.println("toJsonStr = " + json);
		JSONObject obj = JSONUtil.parseObj(json);
		System.out.println("parseObj.get(\"name\") = " + obj.get("name"));
		System.out.println("isJson(\"{}\") = " + JSONUtil.isJson("{}"));
		User user = JSONUtil.toBean("{\"name\":\"Bob\",\"age\":25}", User.class);
		System.out.println("toBean = " + user.name + " / " + user.age);
	}

	/** 示例 Bean。 */
	public static class User {

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
