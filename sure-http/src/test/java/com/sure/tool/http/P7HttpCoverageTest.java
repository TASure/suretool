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
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P7：覆盖率补测，针对 HttpUtil/HttpRequest/HttpResponse/CookieStore/URLUtil 未命中分支，
 * 全部使用本地 localhost HttpServer 与连接拒绝端口，不访问外网。
 */
public class P7HttpCoverageTest {

	private static HttpServer server;
	private static String base;
	private static int refusedPort;

	@BeforeClass
	public static void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/ok", P7HttpCoverageTest::handleOk);
		server.createContext("/err404", P7HttpCoverageTest::handle404);
		server.createContext("/err500", P7HttpCoverageTest::handle500);
		server.createContext("/upload", P7HttpCoverageTest::handleUpload);
		server.createContext("/download", P7HttpCoverageTest::handleDownload);
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
		// 取一个立即关闭的端口用于触发连接拒绝
		try (ServerSocket ss = new ServerSocket(0)) {
			refusedPort = ss.getLocalPort();
		}
	}

	private static void respond(HttpExchange exchange, int code, byte[] body) throws IOException {
		exchange.sendResponseHeaders(code, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private static void handleOk(HttpExchange exchange) throws IOException {
		respond(exchange, 200, "hello".getBytes(StandardCharsets.UTF_8));
	}

	private static void handle404(HttpExchange exchange) throws IOException {
		respond(exchange, 404, "notfound".getBytes(StandardCharsets.UTF_8));
	}

	private static void handle500(HttpExchange exchange) throws IOException {
		respond(exchange, 500, "boom".getBytes(StandardCharsets.UTF_8));
	}

	private static void handleUpload(HttpExchange exchange) throws IOException {
		byte[] body = exchange.getRequestBody().readAllBytes();
		respond(exchange, 200, ("UP:" + body.length).getBytes(StandardCharsets.UTF_8));
	}

	private static void handleDownload(HttpExchange exchange) throws IOException {
		respond(exchange, 200, "FILEDATA".getBytes(StandardCharsets.UTF_8));
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	// ==================== HttpUtil 异常与边界 ====================

	@Test
	public void testGetBytesConnectionRefused() {
		try {
			HttpUtil.getBytes("http://127.0.0.1:" + refusedPort + "/x", 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			Assert.assertTrue(expected.getMessage().contains("请求失败"));
		}
	}

	@Test
	public void testExecuteGetConnectionRefused() {
		try {
			HttpUtil.get("http://127.0.0.1:" + refusedPort + "/x", null, 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 282/283 catch IOException
		}
	}

	@Test
	public void testPatchConnectionRefused() {
		try {
			HttpUtil.patchJson("http://127.0.0.1:" + refusedPort + "/x", "{}", 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 455/456 catch IOException
		}
	}

	@Test
	public void testHeadConnectionRefused() {
		try {
			HttpUtil.head("http://127.0.0.1:" + refusedPort + "/x", 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 491/492
		}
	}

	@Test
	public void testGetBytesOk() {
		byte[] data = HttpUtil.getBytes(base + "/ok");
		Assert.assertEquals("hello", new String(data, StandardCharsets.UTF_8));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDownloadNullDir() {
		HttpUtil.download(base + "/download", null);
	}

	@Test
	public void testDownloadNewDirAndOk() throws IOException {
		File dir = new File(Files.createTempDirectory("dl").toFile(), "sub");
		try {
			File f = HttpUtil.download(base + "/download", dir);
			Assert.assertTrue(f.exists());
			Assert.assertEquals("FILEDATA", new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
		} finally {
			deleteRecursively(dir.getParentFile());
		}
	}

	@Test(expected = HttpException.class)
	public void testDownloadEmptyFileName() {
		HttpUtil.download(base + "/", new File(System.getProperty("java.io.tmpdir")));
	}

	@Test(expected = HttpException.class)
	public void testDownloadErrorStatus() {
		HttpUtil.download(base + "/err404", new File(System.getProperty("java.io.tmpdir")), 200);
	}

	@Test
	public void testDownloadIOException() {
		try {
			HttpUtil.download("http://127.0.0.1:" + refusedPort + "/x",
					new File(System.getProperty("java.io.tmpdir")), 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 224/225
		}
	}

	@Test
	public void testDownloadBytesErrorStatus() {
		try {
			HttpUtil.downloadBytes(base + "/err404", 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			Assert.assertTrue(expected.getMessage().contains("HTTP 404"));
		} catch (IOException e) {
			Assert.fail();
		}
	}

	@Test
	public void testUploadSingleFileErrorAndMultiEdge() throws IOException {
		File tmp = File.createTempFile("uptest", ".txt");
		Files.write(tmp.toPath(), "data".getBytes(StandardCharsets.UTF_8));
		try {
			// 正常上传
			String ok = HttpUtil.upload(base + "/upload", null, "file", tmp, 200);
			Assert.assertTrue(ok.startsWith("UP:"));
			// 服务端 500 → 抛 HttpException（560）
			try {
				HttpUtil.upload(base + "/err500", null, "file", tmp, 200);
				Assert.fail("应抛 HttpException");
			} catch (HttpException expected) {
				// 560
			}
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testUploadIOException() throws IOException {
		File tmp = File.createTempFile("uptest", ".txt");
		Files.write(tmp.toPath(), "d".getBytes(StandardCharsets.UTF_8));
		try {
			HttpUtil.upload("http://127.0.0.1:" + refusedPort + "/x", null, "file", tmp, 200);
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 563/564
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testUploadMultiFileArrayNullAndMissingFile() {
		Map<String, File> files = new LinkedHashMap<>();
		files.put("ghost", new File("/nonexistent-" + System.nanoTime() + ".bin"));
		// upload(Map<File>) → uploadMulti：文件不存在走 652 continue
		String resp = HttpUtil.upload(base + "/upload", null, files, 200);
		Assert.assertTrue(resp.startsWith("UP:"));
		// File[] 数组含 null → 648 continue
		String resp2 = HttpUtil.upload(base + "/upload", null, "f", (File[]) null, 200);
		Assert.assertTrue(resp2.startsWith("UP:"));
	}

	@Test
	public void testUploadMultiErrorAndIOException() throws IOException {
		File tmp = File.createTempFile("uptest", ".txt");
		Files.write(tmp.toPath(), "d".getBytes(StandardCharsets.UTF_8));
		Map<String, File> files = new LinkedHashMap<>();
		files.put("f", tmp);
		try {
			// 681 code>=400
			try {
				HttpUtil.upload(base + "/err500", null, files, 200);
				Assert.fail("应抛 HttpException");
			} catch (HttpException expected) {
				// 681
			}
			// 684/685 IOException
			try {
				HttpUtil.upload("http://127.0.0.1:" + refusedPort + "/x", null, files, 200);
				Assert.fail("应抛 HttpException");
			} catch (HttpException expected) {
				// 684/685
			}
		} finally {
			tmp.delete();
		}
	}

	// ==================== HttpRequest 校验与引擎分支 ====================

	@Test(expected = IllegalArgumentException.class)
	public void testUrlEmpty() {
		HttpRequest.get(base).url("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMethodEmpty() {
		HttpRequest.get(base).method("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFormFilesVarargsNameEmpty() {
		HttpRequest.get(base).form("   ", (File[]) null);
	}

	@Test
	public void testSetters() {
		HttpRequest req = HttpRequest.get(base)
				.contentType("text/plain")
				.connectTimeout(1234)
				.readTimeout(5678)
				.userAgent("ua-test")
				.followRedirects(false)
				.timeout(1000);
		Assert.assertNotNull(req);
	}

	@Test
	public void testClientEngineHeadersAndError() {
		java.net.http.HttpClient client = HttpClientBuilder.builder().build();
		// 544/545 请求头循环
		HttpResponse ok = HttpRequest.get(base + "/ok").client(client).header("X-Test", "v").execute();
		Assert.assertEquals(200, ok.getStatus());
		// 568/569 client 引擎连接失败
		try {
			HttpRequest.get("http://127.0.0.1:" + refusedPort + "/x").client(client).execute();
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 568/569
		}
	}

	@Test
	public void testInterruptedExceptionPaths() {
		java.net.http.HttpClient client = HttpClientBuilder.builder().build();
		// 中断当前线程，HttpClient.send / patchJson 的 send 会抛 InterruptedException
		boolean restored = false;
		try {
			Thread.currentThread().interrupt();
			try {
				HttpRequest.get(base + "/ok").client(client).execute();
				Assert.fail("应抛 HttpException(Interrupted)");
			} catch (HttpException expected) {
				// 565/566/567
				Assert.assertTrue(expected.getCause() instanceof InterruptedException);
			}
			try {
				HttpUtil.patchJson(base + "/ok", "{}", 5000);
				Assert.fail("应抛 HttpException(Interrupted)");
			} catch (HttpException expected) {
				// 457/458/459
				Assert.assertTrue(expected.getCause() instanceof InterruptedException);
			}
		} finally {
			Thread.interrupted();
			restored = true;
		}
		Assert.assertTrue(restored);
	}

	@Test
	public void testMultipartBuildIoError() {
		// 文件不存在 → buildMultipart 内 FileInputStream 失败 → 694/695
		try {
			HttpRequest.post(base + "/upload").form("f", new File("/nope-" + System.nanoTime() + ".bin")).execute();
			Assert.fail("应抛 HttpException");
		} catch (HttpException expected) {
			// 694/695
		}
	}

	// ==================== HttpResponse 边界（直接构造） ====================

	@Test
	public void testHeaderValuesNull() {
		HttpResponse resp = new HttpResponse(200, base, new LinkedHashMap<>(), new byte[0]);
		Assert.assertTrue(resp.headerValues(null).isEmpty());
		Assert.assertNull(resp.header("X"));
	}

	@Test
	public void testGetCharsetVariants() {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		// 150 charset 后带分号参数
		headers.put("Content-Type", List.of("text/html; charset=GBK; x=y"));
		HttpResponse r1 = new HttpResponse(200, base, headers, new byte[0]);
		Assert.assertEquals("GBK", r1.getCharset().name());
		// 153 带引号
		headers.put("Content-Type", List.of("text/html; charset=\"UTF-8\""));
		HttpResponse r2 = new HttpResponse(200, base, headers, new byte[0]);
		Assert.assertEquals(StandardCharsets.UTF_8, r2.getCharset());
		// 158 非法 charset → 回退 UTF-8
		headers.put("Content-Type", List.of("text/html; charset=BOGUS"));
		HttpResponse r3 = new HttpResponse(200, base, headers, new byte[0]);
		Assert.assertEquals(StandardCharsets.UTF_8, r3.getCharset());
	}

	@Test
	public void testCookiesEdgeCases() {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		headers.put("Set-Cookie", Arrays.asList("a=1; Path=/", null, "noequals", "  =bad", "b=2; Domain=x"));
		HttpResponse resp = new HttpResponse(200, base, headers, new byte[0]);
		Map<String, String> cookies = resp.cookies();
		Assert.assertEquals("1", cookies.get("a"));
		Assert.assertEquals("2", cookies.get("b"));
		Assert.assertNull(resp.getCookie(null));
		Assert.assertNull(resp.getCookie("missing"));
	}

	@Test
	public void testCookiesEmpty() {
		HttpResponse resp = new HttpResponse(200, base, new LinkedHashMap<>(), new byte[0]);
		Assert.assertTrue(resp.cookies().isEmpty());
	}

	// ==================== HttpClientBuilder 代理 ====================

	@Test
	public void testBuilderProxyHostPortAndBuild() {
		HttpClientBuilder builder = HttpClientBuilder.builder().proxy("127.0.0.1", 8080).followRedirects(false);
		java.net.http.HttpClient c = builder.build();
		Assert.assertNotNull(c);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderProxyNonHttp() {
		HttpClientBuilder.builder().proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080)));
	}

	@Test
	public void testBuilderProxyValidObject() {
		java.net.http.HttpClient c = HttpClientBuilder.builder()
				.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
				.build();
		Assert.assertNotNull(c);
	}

	// ==================== URLUtil 边界 ====================

	@Test
	public void testGetParamNullName() {
		Assert.assertNull(URLUtil.getParam(base + "/ok?a=1", null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToUriEmpty() {
		URLUtil.getPath("  ");
	}

	// ==================== CookieStore 边界 ====================

	@Test
	public void testCookieStoreEdges() {
		CookieStore store = new CookieStore();
		// 48 null/空头
		store.add(null, "a=1");
		store.add("h", (String) null);
		store.add("h", "");
		// 69/71 直接添加 HttpCookie
		store.add("h", new HttpCookie("k", "v"));
		Assert.assertFalse(store.isEmpty());
		// 82 header(null)
		Assert.assertNull(store.header(null));
		Assert.assertEquals("k=v", store.header("h"));
		// 104 collect(null,...)
		store.collect(null, new LinkedHashMap<>());
		Assert.assertNotNull(store.collect("http://h/x", new LinkedHashMap<>()));
		store.clear();
		Assert.assertTrue(store.isEmpty());
	}

	// ==================== HttpException 单参构造 ====================

	@Test
	public void testHttpExceptionSingleArg() {
		HttpException e = new HttpException("boom");
		Assert.assertEquals("boom", e.getMessage());
		Assert.assertEquals(-1, e.getStatusCode());
	}

	private static void deleteRecursively(File f) {
		if (f == null || !f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) {
				for (File k : kids) {
					deleteRecursively(k);
				}
			}
		}
		f.delete();
	}
}
