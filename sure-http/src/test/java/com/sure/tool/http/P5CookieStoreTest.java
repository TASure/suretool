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

import com.sun.net.httpserver.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CookieStore 会话保持测试：本地 HTTP 服务器验证 Set-Cookie 收集与自动携带。
 */
public class P5CookieStoreTest {

	@Test
	public void sessionCookiesRoundTrip() throws Exception {
		AtomicInteger seen = new AtomicInteger(0);
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/login", exchange -> {
			byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Set-Cookie", "sid=abc123; Path=/; HttpOnly");
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.createContext("/echo", exchange -> {
			String cookie = exchange.getRequestHeaders().getFirst("Cookie");
			if (cookie != null && cookie.contains("sid=abc123")) {
				seen.incrementAndGet();
			}
			byte[] body = (cookie == null ? "no-cookie" : cookie).getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();
		try {
			String base = "http://localhost:" + server.getAddress().getPort();
			CookieStore store = new CookieStore();

			HttpResponse login = HttpRequest.get(base + "/login").cookieStore(store).execute();
			Assert.assertTrue(login.isOk());

			// 已自动收集 Set-Cookie
			Assert.assertFalse(store.isEmpty());
			Assert.assertNotNull(store.header("localhost"));

			// 第二次请求自动携带（HttpClient 引擎）
			HttpResponse echo = HttpRequest.get(base + "/echo")
					.cookieStore(store).client(java.net.http.HttpClient.newHttpClient()).execute();
			Assert.assertTrue(echo.isOk());
			Assert.assertEquals(1, seen.get());

			// HttpURLConnection 引擎同样生效
			HttpResponse echo2 = HttpRequest.get(base + "/echo")
					.cookieStore(store).execute();
			Assert.assertTrue(echo2.isOk());
			Assert.assertEquals(2, seen.get());

			store.clear();
			Assert.assertTrue(store.isEmpty());
			Assert.assertNull(store.header("localhost"));
		} finally {
			server.stop(0);
		}
	}

	@Test
	public void storeBasicOperations() {
		CookieStore store = new CookieStore();
		Assert.assertTrue(store.isEmpty());
		store.add("example.com", "a=1");
		Assert.assertFalse(store.isEmpty());
		Assert.assertEquals("a=1", store.header("example.com"));
		store.add("example.com", "b=2; Path=/");
		Assert.assertEquals("a=1; b=2", store.header("example.com"));
		Assert.assertNull(store.header("other.com"));
		store.clear();
		Assert.assertTrue(store.isEmpty());
		// 非法 header 静默忽略
		store.add("example.com", "not a valid cookie");
		Assert.assertTrue(store.isEmpty());
		Assert.assertEquals("localhost", CookieStore.hostOf("http://localhost:8080/x"));
		Assert.assertNull(CookieStore.hostOf("not a url"));
	}
}
