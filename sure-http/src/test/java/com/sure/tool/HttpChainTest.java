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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * HttpRequest / HttpResponse 链式 API 测试（本地 HttpServer，不依赖外网）。
 */
public class HttpChainTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void setUp() throws Exception {
		server = HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/hello", exchange -> respond(exchange, "hello world"));
		server.createContext("/echoQuery", exchange -> respond(exchange, exchange.getRequestURI().getRawQuery()));
		server.createContext("/echoBody", exchange -> respond(exchange, readBody(exchange)));
		server.createContext("/echoContentType", exchange -> {
			String value = exchange.getRequestHeaders().getFirst("Content-Type");
			respond(exchange, value == null ? "" : value);
		});
		server.createContext("/bytes", exchange -> respondBytes(exchange, new byte[]{1, 2, 3, -1, 100}));
		server.createContext("/echoHeader", exchange -> {
			String value = exchange.getRequestHeaders().getFirst("X-Custom");
			respond(exchange, value == null ? "" : value);
		});
		server.createContext("/echoUserAgent", exchange -> {
			String value = exchange.getRequestHeaders().getFirst("User-Agent");
			respond(exchange, value == null ? "" : value);
		});
		server.createContext("/method", exchange -> respond(exchange, exchange.getRequestMethod()));
		server.createContext("/redirect", exchange -> {
			exchange.getResponseHeaders().add("Location", base + "/hello");
			exchange.sendResponseHeaders(302, -1);
			exchange.close();
		});
		server.createContext("/notfound", exchange -> {
			byte[] data = "no such page".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(404, data.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(data);
			}
		});
		server.createContext("/charset", exchange -> {
			byte[] data = "中文".getBytes(Charset.forName("GBK"));
			exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=GBK");
			exchange.sendResponseHeaders(200, data.length);
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
	public void testGetChain() {
		HttpResponse response = HttpRequest.get(base + "/hello").execute();
		assertEquals(200, response.getStatus());
		assertTrue(response.isOk());
		assertEquals("hello world", response.body());
		assertNotNull(response.getUrl());
	}

	@Test
	public void testQueryChain() {
		String result = HttpRequest.get(base + "/echoQuery")
				.query("name", "sure")
				.query("lang", "中文")
				.execute().body();
		assertTrue(result.contains("name=sure"));
		assertTrue(result.contains("lang=%E4%B8%AD%E6%96%87"));
	}

	@Test
	public void testQueryMap() {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("a", "1");
		params.put("b", "2");
		String result = HttpRequest.get(base + "/echoQuery").query(params).execute().body();
		assertEquals("a=1&b=2", result);
	}

	@Test
	public void testPostFormChain() {
		String result = HttpRequest.post(base + "/echoBody")
				.form("key", "value")
				.form("empty", null)
				.execute().body();
		assertEquals("key=value&empty=", result);
		String contentType = HttpRequest.post(base + "/echoContentType")
				.form("key", "value").execute().body();
		assertTrue(contentType.contains("application/x-www-form-urlencoded"));
	}

	@Test
	public void testPostJsonBody() {
		String result = HttpRequest.post(base + "/echoBody").body("{\"a\":1}").execute().body();
		assertEquals("{\"a\":1}", result);
		String contentType = HttpRequest.post(base + "/echoContentType").body("{\"a\":1}").execute().body();
		assertTrue(contentType.startsWith("application/json"));
	}

	@Test
	public void testBodyPriorityOverForm() {
		String result = HttpRequest.post(base + "/echoBody")
				.form("k", "v")
				.body("raw-body")
				.execute().body();
		assertEquals("raw-body", result);
	}

	@Test
	public void testByteBody() {
		byte[] payload = new byte[]{65, 66, 67};
		String result = HttpRequest.post(base + "/echoBody").body(payload).execute().body();
		assertEquals("ABC", result);
	}

	@Test
	public void testGetBytes() {
		byte[] data = HttpRequest.get(base + "/bytes").execute().bodyBytes();
		assertEquals(5, data.length);
		assertEquals((byte) 100, data[4]);
	}

	@Test
	public void testHeader() {
		String result = HttpRequest.get(base + "/echoHeader").header("X-Custom", "abc").execute().body();
		assertEquals("abc", result);
	}

	@Test
	public void testHeaderMapAndUserAgentOverride() {
		Map<String, Object> headers = new LinkedHashMap<>();
		headers.put("X-Custom", "mapped");
		headers.put("User-Agent", "my-agent/1.0");
		HttpResponse response = HttpRequest.get(base + "/echoHeader").header(headers).execute();
		assertEquals("mapped", response.body());
		assertEquals("my-agent/1.0",
				HttpRequest.get(base + "/echoUserAgent").header(headers).execute().body());
	}

	@Test
	public void testFollowRedirectDefault() {
		HttpResponse response = HttpRequest.get(base + "/redirect").execute();
		assertEquals(200, response.getStatus());
		assertEquals("hello world", response.body());
		assertTrue(response.getUrl().endsWith("/hello"));
	}

	@Test
	public void testFollowRedirectDisabled() {
		HttpResponse response = HttpRequest.get(base + "/redirect").followRedirects(false).execute();
		assertEquals(302, response.getStatus());
		assertFalse(response.isOk());
		assertNotNull(response.header("Location"));
	}

	@Test
	public void testMethodDeleteAndOf() {
		assertEquals("DELETE", HttpRequest.delete(base + "/method").execute().body());
		assertEquals("POST", HttpRequest.of(base + "/method", "post").execute().body());
		assertEquals("PUT", HttpRequest.put(base + "/method").execute().body());
	}

	@Test
	public void testPatchNotSupported() {
		try {
			HttpRequest.of(base + "/method", "PATCH").execute();
			fail("JDK HttpURLConnection 不支持 PATCH，应抛 HttpException");
		} catch (HttpException e) {
			// 预期：ProtocolException 包装
		}
	}

	@Test
	public void testErrorStatusNotThrow() {
		HttpResponse response = HttpRequest.get(base + "/notfound").execute();
		assertEquals(404, response.getStatus());
		assertFalse(response.isOk());
		assertEquals("no such page", response.body());
	}

	@Test
	public void testCharsetParsing() {
		HttpResponse response = HttpRequest.get(base + "/charset").execute();
		assertEquals(Charset.forName("GBK"), response.getCharset());
		assertEquals("中文", response.body());
		assertEquals("UTF-8", StandardCharsets.UTF_8.name());
	}

	@Test
	public void testCharsetFallbackUtf8() {
		HttpResponse response = HttpRequest.get(base + "/hello").execute();
		assertEquals(StandardCharsets.UTF_8, response.getCharset());
		assertEquals(null, response.header("Content-Type"));
	}

	@Test
	public void testNetworkError() {
		int freePort = findFreePort();
		try {
			HttpRequest.get("http://127.0.0.1:" + freePort + "/x").timeout(500).execute();
			fail("应抛出 HttpException");
		} catch (HttpException e) {
			assertEquals(-1, e.getStatusCode());
		}
	}

	@Test
	public void testInvalidProtocol() {
		try {
			HttpRequest.get("file:///etc/hosts").execute();
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	@Test
	public void testImmutableResponse() {
		HttpResponse response = HttpRequest.get(base + "/hello").execute();
		try {
			response.headers().put("X", Collections.singletonList("1"));
			fail("响应头应不可变");
		} catch (UnsupportedOperationException e) {
			// 预期
		}
		byte[] bytes = response.bodyBytes();
		bytes[0] = (byte) 0xFF;
		assertEquals("hello world", response.body());
	}

	@Test
	public void testConstructorValidation() {
		try {
			HttpRequest.get(null);
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
		try {
			HttpRequest.of(base + "/hello", " ");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
		try {
			HttpRequest.get(base + "/hello").header(" ", "v");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
		try {
			HttpRequest.get(base + "/hello").query(null, "v");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
		try {
			HttpRequest.get(base + "/hello").form(null, "v");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	@Test
	public void testUrlAndMethodSetters() {
		String result = HttpRequest.get("/ignored").url(base + "/method").method("delete").execute().body();
		assertEquals("DELETE", result);
	}

	private static int findFreePort() {
		try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
