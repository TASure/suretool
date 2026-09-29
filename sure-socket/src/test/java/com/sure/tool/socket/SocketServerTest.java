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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * {@link SocketServer} 单元测试。
 */
public class SocketServerTest {

	@Test
	public void startOnAutoPortAndEcho() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
			String line = SocketUtil.readLine(socket, null);
			SocketUtil.writeString(socket, "echo:" + line, null);
		});
		server.start();
		int port = server.getPort();
		assertTrue("运行中", server.isRunning());

		try (SocketClient client = new SocketClient("127.0.0.1", port, 3000)) {
			client.sendString("hello\n", null);
			assertEquals("回显一致", "echo:hello", client.receiveLine(null));
		}

		server.stop();
		assertFalse("已停止", server.isRunning());
		server.stop(); // 幂等
	}

	@Test
	public void concurrentClientsAllGetEcho() throws Exception {
		int clients = 5;
		CountDownLatch latch = new CountDownLatch(clients);
		List<String> results = new CopyOnWriteArrayList<>();
		SocketServer server = new SocketServer(0, socket -> {
			String line = SocketUtil.readLine(socket, null);
			SocketUtil.writeString(socket, "echo:" + line, null);
		});
		server.start();
		int port = server.getPort();

		List<Thread> threads = new java.util.ArrayList<>();
		for (int i = 0; i < clients; i++) {
			int idx = i;
			threads.add(Thread.ofVirtual().start(() -> {
				try (SocketClient client = new SocketClient("127.0.0.1", port, 3000)) {
					client.sendString("m" + idx + "\n", null);
					results.add(client.receiveLine(null));
				} catch (Exception e) {
					results.add("ERR:" + e.getMessage());
				} finally {
					latch.countDown();
				}
			}));
		}
		assertTrue("并发完成", latch.await(5, TimeUnit.SECONDS));
		for (Thread t : threads) {
			t.join(1000);
		}
		assertEquals("全部回显", clients, results.size());
		for (int i = 0; i < clients; i++) {
			assertTrue("回显正确", results.contains("echo:m" + i));
		}
		server.stop();
	}

	@Test
	public void stopBeforeStartIsSafe() {
		SocketServer server = new SocketServer(0, socket -> {
		});
		server.stop(); // 未启动 stop 不抛
		assertFalse(server.isRunning());
	}

	@Test
	public void invalidConstructorInput() {
		assertThrows(IllegalArgumentException.class, () -> new SocketServer(70000, socket -> {
		}));
		assertThrows(IllegalArgumentException.class, () -> new SocketServer(0, null));
	}

	@Test
	public void getPortBeforeStartThrows() {
		SocketServer server = new SocketServer(0, socket -> {
		});
		assertThrows(IllegalStateException.class, server::getPort);
	}
}
