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
package com.sure.tool.extra.mail;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * P6（v1.7.0）：邮件增强测试（抄送 / 密送 / 编码 / 超时配置，本地假 SMTP）。
 */
public class P6MailEnhanceTest {

	@Test
	public void accountCharsetAndTimeout() {
		MailAccount account = new MailAccount("smtp.example.com", "a@example.com");
		Assert.assertEquals("UTF-8", account.getCharset());
		Assert.assertEquals(10000, account.getTimeout());
		account.setCharset("GBK");
		account.setTimeout(15000);
		Assert.assertEquals("GBK", account.getCharset());
		Assert.assertEquals(15000, account.getTimeout());
	}

	@Test
	public void sendWithCcAndBcc() throws Exception {
		try (FakeSmtpServer server = new FakeSmtpServer()) {
			server.start();
			MailAccount account = new MailAccount("127.0.0.1", "sender@example.com");
			account.setPort(server.port());
			MailUtil.send(account, List.of("to@example.com"), List.of("cc@example.com"),
					List.of("bcc@example.com"), "subject", "body", false, null);
			String data = server.awaitData();
			Assert.assertNotNull(data);
			Assert.assertTrue(data.contains("to@example.com"));
			Assert.assertTrue(data.contains("cc@example.com"));
			// Bcc 头在 SMTP 层被剥除，但 RCPT TO 阶段应出现密送地址
			Assert.assertTrue(server.rcptTo().contains("bcc@example.com"));
		}
	}

	@Test
	public void sendHtmlWithCharset() throws Exception {
		try (FakeSmtpServer server = new FakeSmtpServer()) {
			server.start();
			MailAccount account = new MailAccount("127.0.0.1", "sender@example.com");
			account.setPort(server.port());
			account.setCharset("GBK");
			MailUtil.sendHtml(account, List.of("to@example.com"), "主题", "<p>正文</p>");
			String data = server.awaitData();
			Assert.assertNotNull(data);
			Assert.assertTrue(data.contains("text/html"));
			Assert.assertTrue(data.contains("charset=GBK"));
		}
	}

	/**
	 * 最小 SMTP 假服务器（同 P5，验证协议会话）。
	 */
	private static final class FakeSmtpServer implements AutoCloseable {

		private ServerSocket server;
		private Thread thread;
		private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
		private final java.util.Set<String> recipients =
				java.util.concurrent.ConcurrentHashMap.newKeySet();

		void start() throws IOException {
			server = new ServerSocket(0);
			thread = new Thread(() -> {
				while (!server.isClosed()) {
					try {
						Socket socket = server.accept();
						Thread handler = new Thread(() -> handle(socket), "fake-smtp-conn");
						handler.setDaemon(true);
						handler.start();
					} catch (IOException ignored) {
						break;
					}
				}
			}, "fake-smtp");
			thread.setDaemon(true);
			thread.start();
		}

		private void handle(Socket socket) {
			try (socket) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream(), StandardCharsets.UTF_8));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						socket.getOutputStream(), StandardCharsets.UTF_8), true);
				out.println("220 localhost ESMTP");
				String line;
				StringBuilder message = new StringBuilder();
				boolean inData = false;
				while ((line = in.readLine()) != null) {
					String upper = line.toUpperCase();
					if (inData) {
						if (line.equals(".")) {
							messages.offer(message.toString());
							inData = false;
							out.println("250 OK: message queued");
						} else {
							message.append(line).append('\n');
						}
						continue;
					}
					if (upper.startsWith("EHLO") || upper.startsWith("HELO")) {
						out.println("250-localhost");
						out.println("250 OK");
					} else if (upper.startsWith("MAIL FROM")) {
						out.println("250 OK");
					} else if (upper.startsWith("RCPT TO")) {
						int lt = line.indexOf('<');
						int gt = line.indexOf('>');
						if (lt >= 0 && gt > lt) {
							recipients.add(line.substring(lt + 1, gt));
						}
						out.println("250 OK");
					} else if (upper.equals("DATA")) {
						out.println("354 End data with <CR><LF>.<CR><LF>");
						inData = true;
					} else if (upper.equals("QUIT")) {
						out.println("221 Bye");
						break;
					} else {
						out.println("250 OK");
					}
				}
			} catch (IOException ignored) {
				// 连接关闭
			}
		}

		int port() {
			return server.getLocalPort();
		}

		String awaitData() throws InterruptedException {
			return messages.poll(10, TimeUnit.SECONDS);
		}

		java.util.Set<String> rcptTo() {
			return java.util.Collections.unmodifiableSet(recipients);
		}

		@Override
		public void close() throws IOException {
			if (server != null) {
				server.close();
			}
		}
	}
}
