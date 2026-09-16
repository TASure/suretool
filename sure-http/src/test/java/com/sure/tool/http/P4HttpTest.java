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

	@Test
	public void testPatchJson() throws Exception {
		com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
				new java.net.InetSocketAddress(0), 0);
		String[] method = new String[1];
		String[] body = new String[1];
		server.createContext("/patch", exchange -> {
			method[0] = exchange.getRequestMethod();
			body[0] = new String(exchange.getRequestBody().readAllBytes(),
					java.nio.charset.StandardCharsets.UTF_8);
			byte[] resp = "{\"ok\":true}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, resp.length);
			exchange.getResponseBody().write(resp);
			exchange.close();
		});
		server.start();
		try {
			String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/patch";
			String result = com.sure.tool.http.HttpUtil.patchJson(url, "{\"a\":1}");
			Assert.assertEquals("{\"ok\":true}", result);
			Assert.assertEquals("PATCH", method[0]);
			Assert.assertEquals("{\"a\":1}", body[0]);
		} finally {
			server.stop(0);
		}
	}



	@Test
	public void testHead() throws Exception {
		com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
				new java.net.InetSocketAddress(0), 0);
		String[] method = new String[1];
		server.createContext("/head", exchange -> {
			method[0] = exchange.getRequestMethod();
			exchange.getResponseHeaders().add("X-Custom", "yes");
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		server.start();
		try {
			String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/head";
			java.util.Map<String, java.util.List<String>> headers = com.sure.tool.http.HttpUtil.head(url);
			Assert.assertEquals("HEAD", method[0]);
			Assert.assertTrue("headers=" + headers, headers.containsKey("X-custom"));
			Assert.assertEquals("yes", headers.get("X-custom").get(0));
		} finally {
			server.stop(0);
		}
	}


}
