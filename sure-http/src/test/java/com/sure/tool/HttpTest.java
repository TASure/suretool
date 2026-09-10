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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * HttpUtil / URLUtil 测试（使用 JDK 内置 HttpServer 本地服务，不依赖外网）。
 */
public class HttpTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void setUp() throws Exception {
		server = HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/hello", exchange -> respond(exchange, "hello world"));
		server.createContext("/echo", exchange -> respond(exchange, exchange.getRequestURI().getRawQuery()));
		server.createContext("/echoBody", exchange -> respond(exchange, readBody(exchange)));
		server.createContext("/bytes", exchange -> respondBytes(exchange, new byte[]{1, 2, 3, -1, 100}));
		server.createContext("/download.txt", exchange -> respond(exchange, "download content"));
		server.createContext("/notfound", exchange -> {
			byte[] data = "no such page".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(404, data.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(data);
			}
		});
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	private static void respond(HttpExchange exchange, String body) throws IOException {
		byte[] data = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, data.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(data);
		}
	}

	private static void respondBytes(HttpExchange exchange, byte[] data) throws IOException {
		exchange.sendResponseHeaders(200, data.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(data);
		}
	}

	private static String readBody(HttpExchange exchange) throws IOException {
		return com.sure.tool.io.IoUtil.readUtf8(exchange.getRequestBody());
	}

	@Test
	public void testGet() {
		assertEquals("hello world", HttpUtil.get(base + "/hello"));
	}

	@Test
	public void testGetWithParams() {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("name", "sure");
		params.put("lang", "中文");
		String result = HttpUtil.get(base + "/echo", params);
		assertTrue(result.contains("name=sure"));
		assertTrue(result.contains("lang=%E4%B8%AD%E6%96%87"));
	}

	@Test
	public void testPostForm() {
		Map<String, Object> form = new LinkedHashMap<>();
		form.put("key", "value");
		form.put("empty", null);
		String result = HttpUtil.post(base + "/echoBody", form);
		assertEquals("key=value&empty=", result);
	}

	@Test
	public void testPostJson() {
		String result = HttpUtil.postJson(base + "/echoBody", "{\"a\":1}");
		assertEquals("{\"a\":1}", result);
	}

	@Test
	public void testPostRaw() {
		String result = HttpUtil.post(base + "/echoBody", "raw-body", "text/plain; charset=UTF-8");
		assertEquals("raw-body", result);
	}

	@Test
	public void testGetBytes() {
		byte[] data = HttpUtil.getBytes(base + "/bytes");
		assertEquals(5, data.length);
		assertEquals((byte) 100, data[4]);
	}

	@Test
	public void testDownload() throws Exception {
		File dir = java.nio.file.Files.createTempDirectory("suretool-http").toFile();
		try {
			File file = HttpUtil.download(base + "/download.txt", dir);
			assertEquals("download.txt", file.getName());
			assertEquals("download content",
					com.sure.tool.io.FileUtil.readString(file, StandardCharsets.UTF_8));
		} finally {
			deleteRecursively(dir);
		}
	}

	@Test
	public void testErrorStatus() {
		try {
			HttpUtil.get(base + "/notfound");
			fail("应抛出 HttpException");
		} catch (HttpException e) {
			assertEquals(404, e.getStatusCode());
		}
	}

	@Test
	public void testInvalidProtocol() {
		try {
			HttpUtil.get("file:///etc/hosts");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	@Test
	public void testGetHostAndDomain() {
		assertEquals("127.0.0.1:" + server.getAddress().getPort(), URLUtil.getHost(base + "/hello?a=1"));
		assertEquals("127.0.0.1", URLUtil.getDomain(base + "/hello"));
		assertEquals("example.com", URLUtil.getHost("https://example.com:443/a"));
		assertEquals("example.com:8080", URLUtil.getHost("http://example.com:8080/a"));
	}

	@Test
	public void testGetPathAndQuery() {
		assertEquals("/hello", URLUtil.getPath(base + "/hello"));
		assertEquals("a=1&b=2", URLUtil.getQuery(base + "/hello?a=1&b=2"));
		assertEquals(null, URLUtil.getQuery(base + "/hello"));
	}

	@Test
	public void testGetParams() {
		Map<String, String> params = URLUtil.getParams("http://example.com/p?a=1&b=%E4%B8%AD&c");
		assertEquals("1", params.get("a"));
		assertEquals("中", params.get("b"));
		assertEquals("", params.get("c"));
		assertTrue(URLUtil.getParams("http://example.com/p").isEmpty());
	}

	@Test
	public void testGetParam() {
		assertEquals("1", URLUtil.getParam("http://example.com/p?a=1&b=2", "a"));
		assertEquals(null, URLUtil.getParam("http://example.com/p?a=1", "x"));
	}

	@Test
	public void testBuildUrl() {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("a", "1");
		params.put("b", "中文");
		assertEquals("http://example.com/p?a=1&b=%E4%B8%AD%E6%96%87", URLUtil.buildUrl("http://example.com/p", params));

		Map<String, Object> params2 = new LinkedHashMap<>();
		params2.put("a", "1");
		assertEquals("http://example.com/p?x=0&a=1",
				URLUtil.buildUrl("http://example.com/p?x=0", params2));
		assertEquals("http://example.com/p", URLUtil.buildUrl("http://example.com/p", null));
	}

	@Test
	public void testIsHttpUrl() {
		assertTrue(URLUtil.isHttpUrl("http://a.com"));
		assertTrue(URLUtil.isHttpUrl("https://a.com"));
		assertFalse(URLUtil.isHttpUrl("ftp://a.com"));
		assertFalse(URLUtil.isHttpUrl(null));
	}

	@Test
	public void testInvalidUrl() {
		try {
			URLUtil.getDomain("not a url");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	private static void deleteRecursively(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteRecursively(child);
			}
		}
		file.delete();
	}

	@Test
	public void testNotNullBase() {
		assertNotNull(base);
	}
}