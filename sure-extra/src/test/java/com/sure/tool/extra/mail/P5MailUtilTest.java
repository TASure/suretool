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
 * P5（v1.1.0）：邮件发送测试（本地假 SMTP 服务器，不发真实邮件）。
 */
public class P5MailUtilTest {

	@Test
	public void testSendText() throws Exception {
		try (FakeSmtpServer server = new FakeSmtpServer()) {
			server.start();
			MailAccount account = new MailAccount("127.0.0.1", "sender@example.com");
			account.setPort(server.port());
			MailUtil.sendText(account, List.of("to@example.com"), "sure subject",
					"body hello");
			String data = server.awaitData();
			Assert.assertNotNull("应收到 DATA 内容", data);
			Assert.assertTrue(data.contains("sender@example.com"));
			Assert.assertTrue(data.contains("to@example.com"));
			Assert.assertTrue(data.contains("Subject: sure subject"));
			Assert.assertTrue(data.contains("body hello"));
		}
	}

	@Test
	public void testSendHtmlAndAttachment() throws Exception {
		try (FakeSmtpServer server = new FakeSmtpServer()) {
			server.start();
			MailAccount account = new MailAccount("127.0.0.1", "sender@example.com");
			account.setPort(server.port());
			java.io.File tmp = java.io.File.createTempFile("sure-mail-", ".txt");
			try {
				java.nio.file.Files.writeString(tmp.toPath(), "attachment body",
						StandardCharsets.UTF_8);
				MailUtil.sendHtml(account, List.of("to@example.com"), "html mail",
						"<b>bold</b>");
				MailUtil.sendWithAttachments(account, List.of("to@example.com"), "with file",
						"content", List.of(tmp));
				String first = server.awaitData();
				String second = server.awaitData();
				Assert.assertNotNull(first);
				Assert.assertNotNull(second);
				Assert.assertTrue(first.contains("<b>bold</b>"));
				Assert.assertTrue(second.contains("content"));
				Assert.assertTrue(second.contains("attachment body"));
			} finally {
				tmp.delete();
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullHost() throws Exception {
		MailUtil.sendText(new MailAccount(), List.of("a@b.com"), "s", "c");
	}

	/**
	 * 最小 SMTP 假服务器：仅验证协议会话，不真正投递。
	 */
	private static final class FakeSmtpServer implements AutoCloseable {

		private ServerSocket server;
		private Thread thread;
		private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

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

		@Override
		public void close() throws IOException {
			if (server != null) {
				server.close();
			}
		}
	}
}
