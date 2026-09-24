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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P5（v1.1.0）：HttpUtil 增强测试——Cookie 解析与发送、代理转发、multipart 文件上传、连接池（JDK HttpClient）复用。
 */
public class P5HttpEnhanceTest {

	private static HttpServer server;
	private static String base;
	private static final Map<String, String> receivedHeaders = new ConcurrentHashMap<>();
	private static final List<Integer> clientPorts = Collections.synchronizedList(new ArrayList<>());

	@BeforeClass
	public static void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/echo", P5HttpEnhanceTest::handleEcho);
		server.createContext("/cookie", P5HttpEnhanceTest::handleCookie);
		server.createContext("/multipart", P5HttpEnhanceTest::handleMultipart);
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	private static void handleEcho(HttpExchange exchange) throws IOException {
		clientPorts.add(exchange.getRemoteAddress().getPort());
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		byte[] resp = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
		exchange.sendResponseHeaders(200, resp.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(resp);
		}
	}

	private static void handleCookie(HttpExchange exchange) throws IOException {
		receivedHeaders.put("Cookie", exchange.getRequestHeaders().getFirst("Cookie"));
		exchange.getResponseHeaders().add("Set-Cookie", "sid=abc123; Path=/; HttpOnly");
		exchange.getResponseHeaders().add("Set-Cookie", "theme=dark; Path=/");
		byte[] resp = "ok".getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, resp.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(resp);
		}
	}

	private static void handleMultipart(HttpExchange exchange) throws IOException {
		String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String boundary = extractBoundary(contentType);
		String reply = "boundary=" + boundary + "|hasName=" + body.contains("name=\"username\"")
				+ "|hasFile=" + (body.contains("filename=\"") && (body.contains("Content-Type: text/plain")
				|| body.contains("Content-Type: application/json")))
				+ "|fileContent=" + body.contains("HELLO-SURETOOL")
				+ "|fieldValue=" + body.contains("tester");
		byte[] resp = reply.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, resp.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(resp);
		}
	}

	private static String extractBoundary(String contentType) {
		if (contentType == null) {
			return "";
		}
		int idx = contentType.indexOf("boundary=");
		if (idx < 0) {
			return "";
		}
		return contentType.substring(idx + "boundary=".length()).trim();
	}

	private static String readLine(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		int b;
		while ((b = in.read()) != -1) {
			if (b == '\n') {
				break;
			}
			if (b != '\r') {
				sb.append((char) b);
			}
		}
		return sb.length() == 0 ? (b == -1 ? null : "") : sb.toString();
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void testCookieSendAndParse() {
		HttpResponse resp = HttpRequest.get(base + "/cookie")
				.cookie("pref", "cn")
				.execute();
		Assert.assertTrue(resp.isOk());
		Assert.assertEquals("abc123", resp.getCookie("sid"));
		Assert.assertEquals("dark", resp.getCookie("theme"));
		Map<String, String> cookies = resp.cookies();
		Assert.assertEquals(2, cookies.size());
		Assert.assertEquals("abc123", cookies.get("sid"));
		Assert.assertEquals("pref=cn", receivedHeaders.get("Cookie"));
	}

	@Test
	public void testProxyForwarding() throws IOException {
		// 假代理：只验证请求确实经代理发出，且携带绝对 URI 请求行（HTTP 代理协议特征），直接回显固定响应
		try (ServerSocket proxySocket = new ServerSocket(0)) {
			int proxyPort = proxySocket.getLocalPort();
			final List<String> requestLines = Collections.synchronizedList(new ArrayList<>());
			Thread proxyThread = new Thread(() -> {
				while (!proxySocket.isClosed()) {
					try (Socket client = proxySocket.accept()) {
						InputStream in = client.getInputStream();
						String firstLine = readLine(in);
						requestLines.add(firstLine);
						String line;
						int contentLength = 0;
						while ((line = readLine(in)) != null && !line.isEmpty()) {
							if (line.toLowerCase().startsWith("content-length:")) {
								contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
							}
						}
						if (contentLength > 0) {
							byte[] reqBody = in.readNBytes(contentLength);
							requestLines.add(new String(reqBody, StandardCharsets.UTF_8));
						}
						String body = "PROXY-OK";
						String response = "HTTP/1.1 200 OK\r\n"
								+ "Content-Type: text/plain; charset=UTF-8\r\n"
								+ "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
								+ "Connection: close\r\n\r\n" + body;
						OutputStream out = client.getOutputStream();
						out.write(response.getBytes(StandardCharsets.UTF_8));
						out.flush();
						out.close();
					} catch (IOException e) {
						if (!proxySocket.isClosed()) {
							System.err.println("[proxy] " + e);
						}
					}
				}
			}, "test-http-proxy");
			proxyThread.setDaemon(true);
			proxyThread.start();

			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxyPort));
			String result = HttpUtil.get(base + "/echo", proxy);
			Assert.assertEquals("PROXY-OK", result);
			Assert.assertFalse(requestLines.isEmpty());
			Assert.assertTrue("请求行应为绝对 URI 形式: " + requestLines.get(0),
					requestLines.get(0).startsWith("GET http://127.0.0.1:"));

			Map<String, Object> form = new java.util.LinkedHashMap<>();
			form.put("k", "v1");
			String postResult = HttpUtil.post(base + "/echo", form, proxy);
			Assert.assertEquals("PROXY-OK", postResult);
		}
	}

	@Test
	public void testMultipartUpload() throws IOException {
		java.io.File tmp = java.io.File.createTempFile("hello", ".txt");
		try {
			java.nio.file.Files.write(tmp.toPath(), "HELLO-SURETOOL".getBytes(StandardCharsets.UTF_8));
			HttpResponse resp = HttpRequest.post(base + "/multipart")
					.form("username", "tester")
					.form("file", tmp)
					.execute();
			Assert.assertTrue(resp.isOk());
			String reply = resp.body();
			Assert.assertTrue(reply.contains("hasName=true"));
			Assert.assertTrue(reply.contains("hasFile=true"));
			Assert.assertTrue(reply.contains("fileContent=true"));
			Assert.assertTrue(reply.contains("fieldValue=true"));
			Assert.assertTrue(reply.contains("boundary=suretool-"));
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testConnectionPoolReuse() throws IOException {
		// JDK HttpClient 引擎 + 连接池：连续请求同一目标，观察客户端源端口数远小于请求数（keep-alive 复用）
		clientPorts.clear();
		java.net.http.HttpClient client = HttpClientBuilder.builder().virtualThreads().build();
		int requests = 5;
		for (int i = 0; i < requests; i++) {
			HttpResponse resp = HttpRequest.post(base + "/echo")
					.client(client)
					.form("n", i)
					.execute();
			Assert.assertTrue(resp.isOk());
		}
		Assert.assertFalse(clientPorts.isEmpty());
		int distinctPorts = (int) clientPorts.stream().distinct().count();
		Assert.assertTrue("期望连接复用（端口数 " + distinctPorts + " < 请求数 " + requests + "）",
				distinctPorts < requests);
	}

	@Test
	public void testClientEngineCookieAndMultipart() throws IOException {
		// HttpClient 引擎（连接池）下的 cookie 发送与 multipart 文件上传分支
		java.net.http.HttpClient client = HttpClientBuilder.builder().build();

		HttpResponse cookieResp = HttpRequest.get(base + "/cookie")
				.client(client)
				.cookie("token", "t-9")
				.execute();
		Assert.assertTrue(cookieResp.isOk());
		Assert.assertEquals("abc123", cookieResp.getCookie("sid"));
		Assert.assertEquals("token=t-9", receivedHeaders.get("Cookie"));

		java.io.File tmp = java.io.File.createTempFile("data", ".json");
		try {
			java.nio.file.Files.write(tmp.toPath(), "{\"a\":1}".getBytes(StandardCharsets.UTF_8));
			HttpResponse resp = HttpRequest.post(base + "/multipart")
					.client(client)
					.form("username", "tester")
					.form("file", tmp)
					.execute();
			Assert.assertTrue(resp.isOk());
			String reply = resp.body();
			Assert.assertTrue(reply.contains("hasName=true"));
			Assert.assertTrue(reply.contains("hasFile=true"));
			Assert.assertTrue(reply.contains("fieldValue=true"));
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testProxyNullSafe() {
		// proxy(null) 不影响正常请求
		String result = HttpUtil.get(base + "/echo", (Proxy) null);
		Assert.assertEquals("", result);
	}
}
