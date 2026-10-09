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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

/**
 * FTP 覆盖率二轮补强：底层 client() 访问器与带路径 listFiles 重载（本地假服务器，不触外网）。
 */
public class FtpCoverTest {

	/** client() 访问器直接返回内部 FTPClient。 */
	@Test
	public void clientAccessor() throws Exception {
		try (FtpUtil ftp = new FtpUtil()) {
			assertNotNull(ftp.client());
		}
	}

	/** 连接本地假服务器后按路径列出文件。 */
	@Test
	public void listFiles_withPath() throws Exception {
		try (MiniFtpServer server = new MiniFtpServer()) {
			server.start();
			try (FtpUtil ftp = new FtpUtil()) {
				ftp.connect("127.0.0.1", server.port(), "u", "p");
				assertTrue(ftp.isConnected());
				ftp.passive();
				List<FTPFile> files = ftp.listFiles("/data");
				assertEquals(2, files.size());
				assertEquals("a.txt", files.get(0).getName());
			}
		}
	}

	/** 最小 FTP 假服务器（PASV + LIST）。 */
	static final class MiniFtpServer implements AutoCloseable {

		private ServerSocket control;

		private static void reply(PrintWriter out, String line) {
			out.print(line + "\r\n");
			out.flush();
		}

		void start() throws IOException {
			control = new ServerSocket(0);
			Thread t = new Thread(() -> {
				try (Socket socket = control.accept()) {
					BufferedReader in = new BufferedReader(new InputStreamReader(
							socket.getInputStream(), StandardCharsets.UTF_8));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream(), StandardCharsets.UTF_8), true);
					reply(out, "220 Mini FTP ready");
					String line;
					while ((line = in.readLine()) != null) {
						String cmd = line.toUpperCase();
						if (cmd.startsWith("USER")) {
							reply(out, "331 Password required");
						} else if (cmd.startsWith("PASS")) {
							reply(out, "230 User logged in");
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
								if (dataCmd != null && dataCmd.toUpperCase().startsWith("LIST")) {
									reply(out, "150 Opening data connection");
									dout.print("-rw-r--r-- 1 u g 100 Jan 01 00:00 a.txt\r\n");
									dout.print("-rw-r--r-- 1 u g 50  Jan 01 00:00 b.log\r\n");
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
					// 关闭
				}
			}, "mini-ftp");
			t.setDaemon(true);
			t.start();
		}

		int port() {
			return control.getLocalPort();
		}

		@Override
		public void close() throws IOException {
			control.close();
		}
	}
}
