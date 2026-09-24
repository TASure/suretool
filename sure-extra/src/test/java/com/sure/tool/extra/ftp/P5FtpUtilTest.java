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
package com.sure.tool.extra.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * P5（v1.1.0）：FTP 客户端测试（本地假 FTP 服务器，PASV 模式）。
 */
public class P5FtpUtilTest {

	@Test
	public void testConnectPwdListUpload() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "sure", "secret");
				Assert.assertTrue(ftp.isConnected());
				Assert.assertEquals("/", ftp.pwd());
				ftp.binary();
				ftp.passive();
				List<FTPFile> files = ftp.listFiles();
				Assert.assertEquals(2, files.size());
				Assert.assertEquals("a.txt", files.get(0).getName());
				Assert.assertEquals("b.log", files.get(1).getName());

				boolean ok = ftp.upload("/upload.bin",
						new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)));
				Assert.assertTrue(ok);
				Assert.assertEquals("payload", server.lastStored());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Assert.assertTrue(ftp.download("/a.txt", out));
				Assert.assertTrue(out.size() > 0);
			}
		}
	}

	@Test
	public void testDirOperations() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "sure", "secret");
				Assert.assertTrue(ftp.cd("/tmp"));
				Assert.assertEquals("/tmp", ftp.pwd());
				Assert.assertTrue(ftp.mkdir("/tmp/new"));
				Assert.assertTrue(ftp.delete("/tmp/del.txt"));
			}
		}
	}

	@Test(expected = IOException.class)
	public void testLoginFail() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			server.rejectLogin();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "bad", "bad");
			}
		}
	}

	/**
	 * 最小 FTP 假服务器（PASV 模式，固定目录内容）。
	 */
	private static final class FakeFtpServer implements AutoCloseable {

		private ServerSocket control;
		private volatile boolean rejectLogin;
		private volatile String stored;

		private static void reply(PrintWriter out, String line) {
			out.print(line + "\r\n");
			out.flush();
		}

		void start() throws IOException {
			control = new ServerSocket(0);
			Thread thread = new Thread(() -> {
				try (Socket socket = control.accept()) {
					BufferedReader in = new BufferedReader(new InputStreamReader(
							socket.getInputStream(), StandardCharsets.UTF_8));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream(), StandardCharsets.UTF_8), true);
					reply(out, "220 Fake FTP ready");
					boolean loggedIn = false;
					String currentDir = "/";
					String line;
					while ((line = in.readLine()) != null) {
						String cmd = line.toUpperCase();
						if (cmd.startsWith("USER")) {
							reply(out, rejectLogin ? "530 Not logged in" : "331 Password required");
						} else if (cmd.startsWith("PASS")) {
							if (rejectLogin) {
								reply(out, "530 Login incorrect");
								break;
							}
							loggedIn = true;
							reply(out, "230 User logged in");
						} else if (cmd.startsWith("PWD")) {
							reply(out, "257 \"" + currentDir + "\"");
						} else if (cmd.startsWith("CWD")) {
							String target = line.substring(4).trim();
							currentDir = target.equals("..") ? "/" : target;
							reply(out, "250 CWD successful");
						} else if (cmd.startsWith("MKD")) {
							reply(out, "257 \"" + cmd.substring(4).trim() + "\" created");
						} else if (cmd.startsWith("DELE")) {
							reply(out, "250 Deleted");
						} else if (cmd.startsWith("SYST")) {
							reply(out, "215 UNIX Type: L8");
						} else if (cmd.startsWith("TYPE")) {
							reply(out, "200 Type set");
						} else if (cmd.startsWith("PASV")) {
							try (ServerSocket data = new ServerSocket(0)) {
								int p = data.getLocalPort();
								reply(out, "227 Entering Passive Mode (127,0,0,1,"
										+ (p / 256) + "," + (p % 256) + ")");
								// 客户端建立数据连接后等待命令
								Socket ds = data.accept();
								BufferedReader din = new BufferedReader(new InputStreamReader(
										ds.getInputStream(), StandardCharsets.UTF_8));
								PrintWriter dout = new PrintWriter(new OutputStreamWriter(
										ds.getOutputStream(), StandardCharsets.UTF_8), true);
								// 数据命令在控制通道到达
								String dataCmd = in.readLine();
								String upper = dataCmd == null ? "" : dataCmd.toUpperCase();
								if (upper.startsWith("LIST")) {
									reply(out, "150 Opening data connection");
									dout.print("-rw-r--r-- 1 user group 100 Jan 01 00:00 a.txt\r\n");
									dout.print("-rw-r--r-- 1 user group 50  Jan 01 00:00 b.log\r\n");
									dout.flush();
									ds.shutdownOutput();
									reply(out, "226 Transfer complete");
								} else if (upper.startsWith("RETR")) {
									reply(out, "150 Opening data connection");
									dout.print("file content of a.txt\n");
									dout.flush();
									ds.shutdownOutput();
									reply(out, "226 Transfer complete");
								} else if (upper.startsWith("STOR")) {
									reply(out, "150 Opening data connection");
									java.io.InputStream bin = ds.getInputStream();
									ByteArrayOutputStream buf = new ByteArrayOutputStream();
									byte[] chunk = new byte[512];
									int n;
									while ((n = bin.read(chunk)) != -1) {
										buf.write(chunk, 0, n);
									}
									stored = buf.toString(StandardCharsets.UTF_8);
									reply(out, "226 Transfer complete");
								}
								ds.close();
							}
						} else if (cmd.startsWith("QUIT")) {
							reply(out, "221 Goodbye");
							break;
						} else {
							reply(out, "200 OK");
						}
					}
				} catch (IOException ignored) {
					// 服务器关闭
				}
			}, "fake-ftp");
			thread.setDaemon(true);
			thread.start();
		}

		int port() {
			return control.getLocalPort();
		}

		void rejectLogin() {
			rejectLogin = true;
		}

		String lastStored() {
			return stored;
		}

		@Override
		public void close() throws IOException {
			if (control != null) {
				control.close();
			}
		}
	}
}
