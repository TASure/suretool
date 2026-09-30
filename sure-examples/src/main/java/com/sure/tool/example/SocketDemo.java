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
package com.sure.tool.example;

import java.nio.charset.StandardCharsets;

import com.sure.tool.socket.SocketClient;
import com.sure.tool.socket.SocketServer;
import com.sure.tool.socket.SocketUtil;

/**
 * Socket 门面示例（sure-socket）：echo 服务 + 客户端。
 */
public class SocketDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws Exception {
		System.out.println("=== SocketDemo ===");
		SocketServer server = new SocketServer(0, socket -> {
			String line = SocketUtil.readLine(socket, StandardCharsets.UTF_8);
			SocketUtil.writeString(socket, "echo: " + line + "\n", StandardCharsets.UTF_8);
		});
		server.start();
		int port = server.getPort();
		try (SocketClient client = new SocketClient("127.0.0.1", port)) {
			client.sendString("hi suretool\n", StandardCharsets.UTF_8);
			System.out.println("server echo = " + client.receiveLine(StandardCharsets.UTF_8));
		}
		server.stop();
	}
}
