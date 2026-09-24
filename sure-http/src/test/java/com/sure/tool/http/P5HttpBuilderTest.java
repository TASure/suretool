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
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * P5（v1.1.0）：HttpClientBuilder 构建器全方法、HttpRequest 引擎切换分支、
 * HttpUtil 代理重载与 multipart Content-Type 识别测试。
 */
public class P5HttpBuilderTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/echo", P5HttpBuilderTest::handleEcho);
		server.createContext("/multipart", P5HttpBuilderTest::handleMultipart);
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	private static void handleEcho(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		byte[] resp = body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, resp.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(resp);
		}
	}

	private static void handleMultipart(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String contentType = "none";
		if (body.contains("Content-Type: ")) {
			int idx = body.indexOf("Content-Type: ") + "Content-Type: ".length();
			contentType = body.substring(idx, body.indexOf('\r', idx));
		}
		byte[] resp = contentType.getBytes(StandardCharsets.UTF_8);
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
	public void testBuilderAllMethods() throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			HttpClient client = HttpClientBuilder.builder()
					.connectTimeout(Duration.ofSeconds(5))
					.executor(executor)
					.followRedirects(false)
					.version(HttpClient.Version.HTTP_2)
					.build();
			HttpResponse resp = HttpRequest.get(base + "/echo").client(client).execute();
			Assert.assertTrue(resp.isOk());
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void testClientEngineFormAndBodyBranches() throws IOException {
		HttpClient client = HttpClientBuilder.builder().build();
		// client 引擎 + 表单（无文件）：ofString 分支
		HttpResponse formResp = HttpRequest.post(base + "/echo")
				.client(client)
				.form("a", "1")
				.form("b", "2")
				.execute();
		Assert.assertEquals("a=1&b=2", formResp.body());
		// client 引擎 + body 字节：ofByteArray 分支
		HttpResponse bodyResp = HttpRequest.post(base + "/echo")
				.client(client)
				.body("raw")
				.execute();
		Assert.assertEquals("raw", bodyResp.body());
	}

	@Test
	public void testPoolConvenience() throws IOException {
		HttpResponse resp = HttpRequest.get(base + "/echo")
				.pool(HttpClientBuilder.builder().virtualThreads())
				.execute();
		Assert.assertTrue(resp.isOk());
	}

	@Test
	public void testProxyHostPortOverloadAndHttpUtilOverloads() throws IOException {
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
						while ((line = readLine(in)) != null && !line.isEmpty()) {
							// 丢弃请求头
						}
						String body = "PROXY-OK";
						String response = "HTTP/1.1 200 OK\r\n"
								+ "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
								+ "Connection: close\r\n\r\n" + body;
						OutputStream out = client.getOutputStream();
						out.write(response.getBytes(StandardCharsets.UTF_8));
						out.flush();
						out.close();
					} catch (IOException ignore) {
						// 代理随测试结束关闭
					}
				}
			}, "test-proxy-port");
			proxyThread.setDaemon(true);
			proxyThread.start();

			// HttpRequest.proxy(host, port) 重载
			HttpResponse viaRequest = HttpRequest.get(base + "/echo").proxy("127.0.0.1", proxyPort).execute();
			Assert.assertEquals("PROXY-OK", viaRequest.body());
			Assert.assertTrue(requestLines.get(0).startsWith("GET http://127.0.0.1:"));

			// HttpUtil 代理重载：postJson + upload
			String jsonResp = HttpUtil.postJson(base + "/echo", "{\"a\":1}",
					new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxyPort)));
			Assert.assertEquals("PROXY-OK", jsonResp);

			java.io.File tmp = java.io.File.createTempFile("upload", ".txt");
			try {
				java.nio.file.Files.write(tmp.toPath(), "data".getBytes(StandardCharsets.UTF_8));
				Map<String, Object> form = new java.util.LinkedHashMap<>();
				form.put("note", "x");
				String upResp = HttpUtil.upload(base + "/echo", form, "file", tmp,
						new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxyPort)));
				Assert.assertEquals("PROXY-OK", upResp);
			} finally {
				tmp.delete();
			}
		}
	}

	@Test
	public void testMultipartContentTypeDetection() throws IOException {
		HttpClient client = HttpClientBuilder.builder().build();
		checkContentType(client, "doc.xml", "application/xml");
		checkContentType(client, "pic.png", "image/png");
		checkContentType(client, "photo.jpeg", "image/jpeg");
		checkContentType(client, "anim.gif", "image/gif");
		checkContentType(client, "book.pdf", "application/pdf");
		checkContentType(client, "pkg.zip", "application/zip");
		checkContentType(client, "unknown.bin", "application/octet-stream");
		checkContentType(client, "noext", "application/octet-stream");
	}

	private static void checkContentType(HttpClient client, String name, String expected) throws IOException {
		java.io.File tmp = java.io.File.createTempFile("prefix", name);
		try {
			java.nio.file.Files.write(tmp.toPath(), new byte[]{1, 2, 3});
			HttpResponse resp = HttpRequest.post(base + "/multipart")
					.client(client)
					.form("file", tmp)
					.execute();
			Assert.assertEquals(name + " -> " + expected, expected, resp.body());
		} finally {
			tmp.delete();
		}
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
}
