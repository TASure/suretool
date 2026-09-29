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
package com.sure.tool.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * {@link SocketClient} 单元测试。
 */
public class SocketClientTest {

	@Test
	public void sendAndReceiveAll() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
			SocketUtil.writeString(socket, "welcome \u4e2d\u6587\n", null);
		});
		server.start();
		try (SocketClient client = new SocketClient("127.0.0.1", server.getPort(), 3000)) {
			client.sendString("x\n", null);
			String all = client.receiveAll(null);
			assertEquals("welcome \u4e2d\u6587\n", all);
			assertTrue("已连接", client.isConnected());
		}
		server.stop();
	}

	@Test
	public void receiveLineWithCharset() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
			SocketUtil.writeString(socket, "line1\nline2\n", StandardCharsets.UTF_8);
		});
		server.start();
		try (SocketClient client = new SocketClient("127.0.0.1", server.getPort(), 3000)) {
			assertEquals("line1", client.receiveLine(StandardCharsets.UTF_8));
			assertEquals("line2", client.receiveLine(StandardCharsets.UTF_8));
		}
		server.stop();
	}

	@Test
	public void closeIsIdempotentAndNullGuard() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
		});
		server.start();
		SocketClient client = new SocketClient("127.0.0.1", server.getPort(), 3000);
		client.close();
		client.close();
		assertFalse("关闭后未连接", client.isConnected());
		server.stop();
	}

	@Test
	public void invalidInput() {
		assertThrows(IllegalArgumentException.class, () -> new SocketClient((java.net.Socket) null));
		assertThrows(SocketRuntimeException.class, () -> new SocketClient("127.0.0.1", 1, 300));
		assertThrows(IllegalArgumentException.class, () -> new SocketClient("127.0.0.1", 70000, 300));
	}
}
