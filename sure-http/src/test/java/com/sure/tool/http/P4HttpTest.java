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
package com.sure.tool.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * P4（v0.2.0）第十四批：HttpUtil PUT/DELETE 方法测试（本地 HttpServer）。
 */
public class P4HttpTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/echo", P4HttpTest::handle);
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort() + "/echo";
	}

	private static void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		byte[] resp = (method + "|" + body).getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, resp.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(resp);
		}
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void testPutAndDelete() {
		String putResp = HttpUtil.putJson(base, "{\"a\":1}");
		Assert.assertEquals("PUT|{\"a\":1}", putResp);

		String delResp = HttpUtil.deleteJson(base, "{\"id\":9}");
		Assert.assertEquals("DELETE|{\"id\":9}", delResp);

		String plainDel = HttpUtil.delete(base);
		Assert.assertEquals("DELETE|", plainDel);

		Map<String, Object> form = new HashMap<>();
		form.put("k", "v");
		String putForm = HttpUtil.put(base, form);
		Assert.assertTrue(putForm.startsWith("PUT|"));
		Assert.assertTrue(putForm.contains("k=v"));
	}
}
