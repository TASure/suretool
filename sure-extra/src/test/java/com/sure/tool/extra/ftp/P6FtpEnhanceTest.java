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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * P6（v1.7.0）：FTP 增强测试（编码连接 / 超时 / 重命名 / 存在判断，本地假 FTP）。
 */
public class P6FtpEnhanceTest {

	@Test
	public void connectWithEncodingAndTimeout() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "sure", "secret", 5000, 10000, "GBK");
				Assert.assertTrue(ftp.isConnected());
			}
		}
	}

	@Test
	public void renameAndExist() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "sure", "secret");
				ftp.passive();
				Assert.assertTrue(ftp.rename("/old.txt", "/new.txt"));
				Assert.assertTrue(ftp.exist("/a.txt"));
				Assert.assertFalse(ftp.exist("/nonexistent.txt"));
			}
		}
	}

	@Test
	public void listFilesViaEncodingConnect() throws Exception {
		try (FakeFtpServer server = new FakeFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "sure", "secret", "UTF-8");
				ftp.passive();
				List<FTPFile> files = ftp.listFiles();
				Assert.assertEquals(1, files.size());
				Assert.assertEquals("a.txt", files.get(0).getName());
			}
		}
	}

	/**
	 * 最小 FTP 假服务器（支持 RNFR/RNTO、单文件 LIST）。
	 */
	private static final class FakeFtpServer implements AutoCloseable {

		private ServerSocket control;

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
							reply(out, "331 Password required");
						} else if (cmd.startsWith("PASS")) {
							loggedIn = true;
							reply(out, "230 User logged in");
						} else if (cmd.startsWith("PWD")) {
							reply(out, "257 \"" + currentDir + "\"");
						} else if (cmd.startsWith("CWD")) {
							String target = line.substring(4).trim();
							currentDir = target.equals("..") ? "/" : target;
							reply(out, "250 CWD successful");
						} else if (cmd.startsWith("RNFR")) {
							reply(out, "350 Ready for RNTO");
						} else if (cmd.startsWith("RNTO")) {
							reply(out, "250 Rename successful");
						} else if (cmd.startsWith("SYST")) {
							reply(out, "215 UNIX Type: L8");
						} else if (cmd.startsWith("TYPE")) {
							reply(out, "200 Type set");
						} else if (cmd.startsWith("PASV")) {
							try (ServerSocket data = new ServerSocket(0)) {
								int p = data.getLocalPort();
								reply(out, "227 Entering Passive Mode (127,0,0,1,"
										+ (p / 256) + "," + (p % 256) + ")");
								Socket ds = data.accept();
								PrintWriter dout = new PrintWriter(new OutputStreamWriter(
										ds.getOutputStream(), StandardCharsets.UTF_8), true);
								String dataCmd = in.readLine();
								String upper = dataCmd == null ? "" : dataCmd.toUpperCase();
								if (upper.startsWith("LIST")) {
									// 无参数：当前目录列表；带路径：命中 a.txt 视为存在，其余视为不存在
									String listCmd = dataCmd == null ? "" : dataCmd.trim();
									boolean exists = listCmd.contains("a.txt");
									reply(out, "150 Opening data connection");
									if (listCmd.equalsIgnoreCase("LIST") || exists) {
										dout.print("-rw-r--r-- 1 user group 100 Jan 01 00:00 a.txt\r\n");
									}
									dout.flush();
									ds.shutdownOutput();
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

		@Override
		public void close() throws IOException {
			if (control != null) {
				control.close();
			}
		}
	}
}
