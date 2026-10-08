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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * P6（v1.7.0）：multipart 多文件上传测试（同字段多文件 / 多字段文件 / 文本字段混发）。
 */
public class P6HttpMultipartTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/mp", P6HttpMultipartTest::handleMultipart);
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	/**
	 * 解析 multipart 请求体，返回 "name=filename,..." 摘要与 "files=N" 统计。
	 */
	private static void handleMultipart(HttpExchange exchange) throws IOException {
		String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
		byte[] raw = exchange.getRequestBody().readAllBytes();
		StringBuilder summary = new StringBuilder();
		int fileCount = 0;
		StringBuilder textFields = new StringBuilder();
		if (contentType != null && contentType.contains("boundary=")) {
			String boundary = "--" + contentType.substring(contentType.indexOf("boundary=") + 9).trim();
			String text = new String(raw, StandardCharsets.ISO_8859_1);
			for (String part : text.split(java.util.regex.Pattern.quote(boundary))) {
				if (part.contains("Content-Disposition")) {
					String name = extract(part, "name=\"", "\"");
					String filename = extract(part, "filename=\"", "\"");
					if (filename != null) {
						fileCount++;
						if (summary.length() > 0) {
							summary.append(",");
						}
						summary.append(name).append("=").append(filename);
					} else if (name != null) {
						if (textFields.length() > 0) {
							textFields.append(",");
						}
						textFields.append(name);
					}
				}
			}
		}
		String response = "files=" + fileCount + ";text=" + textFields + ";" + summary;
		byte[] data = response.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, data.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(data);
		}
	}

	private static String extract(String part, String start, String end) {
		int s = part.indexOf(start);
		if (s < 0) {
			return null;
		}
		int e = part.indexOf(end, s + start.length());
		return e < 0 ? null : part.substring(s + start.length(), e);
	}

	private static File temp(String name, String content) throws IOException {
		File file = File.createTempFile("sure-mp-", "-" + name);
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
		return file;
	}

	@Test
	public void sameFieldMultipleFiles() throws Exception {
		File a = temp("a.txt", "AAA");
		File b = temp("b.log", "BBB");
		try {
			String body = HttpRequest.post(base + "/mp")
					.form("note", "hello")
					.form("files", a, b)
					.execute().body();
			Assert.assertTrue(body.contains("files=2"));
			Assert.assertTrue(body.contains("files=" + a.getName()));
			Assert.assertTrue(body.contains("files=" + b.getName()));
			Assert.assertTrue(body.contains("text=note"));
		} finally {
			a.delete();
			b.delete();
		}
	}

	@Test
	public void multiFieldFiles() throws Exception {
		File img = temp("img.png", "PNGDATA");
		File doc = temp("doc.pdf", "PDFDATA");
		try {
			Map<String, File> files = new LinkedHashMap<>();
			files.put("image", img);
			files.put("doc", doc);
			String body = HttpRequest.post(base + "/mp").formFiles(files).execute().body();
			Assert.assertTrue(body.contains("files=2"));
			Assert.assertTrue(body.contains("image=" + img.getName()));
			Assert.assertTrue(body.contains("doc=" + doc.getName()));
		} finally {
			img.delete();
			doc.delete();
		}
	}

	@Test
	public void utilUploadFileArray() throws Exception {
		File a = temp("u1.txt", "U1");
		File b = temp("u2.txt", "U2");
		try {
			String body = HttpUtil.upload(base + "/mp", Map.of("note", "x"), "files", new File[] { a, b });
			Assert.assertTrue(body.contains("files=2"));
			Assert.assertTrue(body.contains("files=" + a.getName()));
			Assert.assertTrue(body.contains("files=" + b.getName()));
		} finally {
			a.delete();
			b.delete();
		}
	}

	@Test
	public void utilUploadFileMap() throws Exception {
		File a = temp("m1.txt", "M1");
		File b = temp("m2.txt", "M2");
		try {
			Map<String, File> files = new LinkedHashMap<>();
			files.put("f1", a);
			files.put("f2", b);
			String body = HttpUtil.upload(base + "/mp", null, files);
			Assert.assertTrue(body.contains("files=2"));
			Assert.assertTrue(body.contains("f1=" + a.getName()));
			Assert.assertTrue(body.contains("f2=" + b.getName()));
		} finally {
			a.delete();
			b.delete();
		}
	}

	@Test
	public void utilUploadSingleFileStillWorks() throws Exception {
		File a = temp("s.txt", "S1");
		try {
			String body = HttpUtil.upload(base + "/mp", null, "file", a);
			Assert.assertTrue(body.contains("files=1"));
			Assert.assertTrue(body.contains("file=" + a.getName()));
		} finally {
			a.delete();
		}
	}
}
