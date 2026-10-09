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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Socket 模块补测：覆盖 {@link SocketClient} 各构造器与异常分支、
 * {@link SocketUtil} 校验/异常/unknown 分支、{@link SocketServer} 启动幂等、
 * 绑定失败、处理器异常与 {@code getLocalAddress} 分支、{@link SocketRuntimeException}
 * 单参构造器。全部仅使用 localhost 与短超时，服务端处理器主动写一行后立即关闭，
 * 资源用完即关，避免阻塞。
 */
public class SocketCoverageTest {

	/** 起一个后台线程：accept 后写入一行再关闭。 */
	private static void serveLine(ServerSocket ss, String line) {
		Thread.ofVirtual().start(() -> {
			try (Socket s = ss.accept()) {
				SocketUtil.writeString(s, line, StandardCharsets.UTF_8);
			} catch (Exception ignored) {
				// 测试清理
			}
		});
	}

	@Test
	public void socketClient两参构造与包装构造() throws Exception {
		try (ServerSocket ss = new ServerSocket(0)) {
			int port = ss.getLocalPort();
			serveLine(ss, "x\n");
			try (SocketClient client = new SocketClient("127.0.0.1", port)) {
				assertTrue(client.isConnected());
				assertNotNull(client.getSocket());
			}
		}
		try (ServerSocket ss = new ServerSocket(0)) {
			int port = ss.getLocalPort();
			serveLine(ss, "y\n");
			Socket raw = new Socket("127.0.0.1", port);
			try (SocketClient client = new SocketClient(raw)) {
				assertTrue(client.isConnected());
			}
		}
	}

	@Test
	public void socketClient异常分支() throws Exception {
		try (ServerSocket ss = new ServerSocket(0)) {
			int port = ss.getLocalPort();
			serveLine(ss, "hello\n");
			SocketClient client = new SocketClient("127.0.0.1", port, 3000);
			// 参数校验
			assertThrows(IllegalArgumentException.class, () -> client.send(null));
			assertThrows(IllegalArgumentException.class, () -> client.sendString(null, null));
			assertThrows(IllegalArgumentException.class, () -> client.receive(null));
			// receive 正常读满/EOF 路径
			byte[] buf = new byte[16];
			int n = client.receive(buf);
			assertTrue(n >= 0);
			// 关闭后再操作 -> IOException 包装为 SocketRuntimeException
			client.close();
			assertThrows(SocketRuntimeException.class, () -> client.send("x".getBytes(StandardCharsets.UTF_8)));
			assertThrows(SocketRuntimeException.class, () -> client.receive(new byte[4]));
			assertThrows(SocketRuntimeException.class, () -> client.receiveAll(null));
		}
	}

	@Test
	public void socketUtil校验与异常分支() throws Exception {
		// readLine/writeString 空 socket 校验
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.readLine(null, null));
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.writeString(null, "x", null));
		// getRemoteAddress null 与未连接
		assertEquals("unknown", SocketUtil.getRemoteAddress(null));
		try (Socket unconnected = new Socket()) {
			assertEquals("unknown", SocketUtil.getRemoteAddress(unconnected));
		}
		// isInnerIP 四段但非数字
		assertFalse(SocketUtil.isInnerIP("a.b.c.d"));
		assertFalse(SocketUtil.isInnerIP("1.2.3.x"));
		// safeClose：关闭抛异常的资源被忽略
		SocketUtil.safeClose(() -> {
			throw new RuntimeException("ignore");
		});
		assertNotNull(SocketUtil.getLocalHost());

		// 正常读写后关闭，再触发 IOException 包装
		try (ServerSocket ss = new ServerSocket(0)) {
			int port = ss.getLocalPort();
			serveLine(ss, "hi\n");
			try (Socket s = SocketUtil.connect("127.0.0.1", port, 3000)) {
				SocketUtil.readLine(s, null);
				s.close();
				assertThrows(SocketRuntimeException.class, () -> SocketUtil.writeString(s, "x", null));
				assertThrows(SocketRuntimeException.class, () -> SocketUtil.readLine(s, null));
			}
		}
	}

	@Test
	public void socketServer启动幂等与绑定失败() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
		});
		server.start();
		server.start(); // 幂等直接 return
		assertTrue(server.isRunning());
		assertNotNull(server.getLocalAddress());
		server.stop();

		// 占用端口后再绑定 -> SocketRuntimeException
		try (ServerSocket occupier = new ServerSocket(0)) {
			int port = occupier.getLocalPort();
			SocketServer bad = new SocketServer(port, socket -> {
			});
			assertThrows(SocketRuntimeException.class, bad::start);
		}
	}

	@Test
	public void socketServer处理器异常被吞() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
			throw new IllegalStateException("handler boom");
		});
		server.start();
		int port = server.getPort();
		// 客户端连上即断，触发 handleSafely 的 catch 分支
		try (SocketClient client = new SocketClient("127.0.0.1", port, 3000)) {
			client.sendString("x\n", null);
		} catch (SocketRuntimeException expected) {
			// 客户端侧可能读到断连，忽略
		}
		Thread.sleep(200);
		server.stop();
		assertFalse(server.isRunning());
	}

	@Test
	public void socketServer启动前访问抛异常() {
		SocketServer server = new SocketServer(0, socket -> {
		});
		assertThrows(IllegalStateException.class, server::getLocalAddress);
		assertThrows(IllegalStateException.class, server::getPort);
	}

	@Test
	public void getRemoteAddressConnectedButRemoteNull() {
		// 子类覆写 isConnected()=true 但 getRemoteSocketAddress()=null，命中 L204 unknown 分支
		Socket fake = new Socket() {
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public java.net.SocketAddress getRemoteSocketAddress() {
				return null;
			}
		};
		assertEquals("unknown", SocketUtil.getRemoteAddress(fake));
	}

	@Test
	public void socketRuntimeException构造器() {
		SocketRuntimeException e = new SocketRuntimeException("only message");
		assertEquals("only message", e.getMessage());
		SocketRuntimeException w = new SocketRuntimeException("msg", new Throwable("cause"));
		assertNotNull(w.getCause());
	}

	@Test
	public void isPortAvailable非法端口返回false() {
		assertFalse(SocketUtil.isPortAvailable(-1));
		assertFalse(SocketUtil.isPortAvailable(70000));
	}

	/**
	 * 反射给运行中的 {@link SocketServer} 的 serverSocket 设 soTimeout，
	 * 使 accept() 抛 SocketTimeoutException（IOException 子类、非 SocketException），
	 * 命中 acceptLoop 的通用 IOException 分支与 uncaughtException 处理。
	 */
	@Test
	public void acceptLoop通用IOException分支() throws Exception {
		SocketServer server = new SocketServer(0, socket -> {
		});
		// 反射构造自带 soTimeout 的 ServerSocket，直接喂给 acceptLoop，消除竞态
		ServerSocket ss = new ServerSocket();
		ss.bind(new java.net.InetSocketAddress("127.0.0.1", 0));
		ss.setSoTimeout(200);
		java.lang.reflect.Field ssField = SocketServer.class.getDeclaredField("serverSocket");
		ssField.setAccessible(true);
		ssField.set(server, ss);
		java.lang.reflect.Field runningField = SocketServer.class.getDeclaredField("running");
		runningField.setAccessible(true);
		java.util.concurrent.atomic.AtomicBoolean running =
				(java.util.concurrent.atomic.AtomicBoolean) runningField.get(server);
		running.set(true);
		// 直接调用 acceptLoop：accept() 200ms 后抛 SocketTimeoutException（IOException 子类）
		java.lang.reflect.Method m = SocketServer.class.getDeclaredMethod("acceptLoop");
		m.setAccessible(true);
		m.invoke(server);
		// 此时 acceptLoop 应已退出；清理
		running.set(false);
		ss.close();
	}
}
