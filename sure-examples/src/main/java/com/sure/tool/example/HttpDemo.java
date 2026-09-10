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

import com.sure.tool.http.HttpRequest;
import com.sure.tool.http.HttpResponse;
import com.sure.tool.http.HttpUtil;
import com.sure.tool.http.URLUtil;

/**
 * HTTP 工具示例（HttpUtil / HttpRequest / URLUtil）。
 *
 * <p>仅演示 API 用法；实际发起网络请求由调用方自行决定。
 */
public class HttpDemo {

	/**
	 * 运行示例（不发起真实请求）。
	 */
	public static void run() {
		System.out.println("=== HttpDemo ===");
		System.out.println("isHttpUrl = " + URLUtil.isHttpUrl("https://example.com/a"));
		System.out.println("getHost = " + URLUtil.getHost("https://example.com/a?x=1"));
		System.out.println("getQuery = " + URLUtil.getQuery("https://example.com/a?x=1&y=2"));
		System.out.println("buildUrl = "
				+ URLUtil.buildUrl("https://example.com/api", Map.of("page", "1", "size", "10")));
		HttpRequest request = HttpRequest.get("https://example.com");
		request.timeout(5000);
		System.out.println("request = " + request);
	}

	/**
	 * 演示真正发起请求（默认不执行，避免示例运行时依赖外网）。
	 */
	public static void fetchExample() {
		HttpResponse response = HttpRequest.get("https://example.com").timeout(5000).execute();
		System.out.println("status = " + response.getStatus());
		System.out.println("body = " + HttpUtil.get("https://example.com"));
	}
}
