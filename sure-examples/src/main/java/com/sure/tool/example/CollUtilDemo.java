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

import java.util.Arrays;
import java.util.List;

import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.MapUtil;

/**
 * 集合与 Map 工具示例（CollUtil / MapUtil）。
 */
public class CollUtilDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== CollUtilDemo ===");
		List<String> list = Arrays.asList("a", "b", "c");
		System.out.println("join(list, \",\") = " + CollUtil.join(list, ","));
		System.out.println("map(list, s -> s + \"!\") = " + CollUtil.map(list, s -> s + "!"));
		System.out.println("MapUtil.of(\"name\", \"Alice\", \"age\", 30) = " + MapUtil.of("name", "Alice", "age", 30));
		System.out.println("getInt(map, \"age\", 0) = " + MapUtil.getInt(MapUtil.of("age", "30"), "age", 0));
	}
}
