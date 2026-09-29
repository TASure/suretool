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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;

import org.junit.Test;

/**
 * {@link SocketUtil} 单元测试。
 */
public class SocketUtilTest {

	@Test
	public void connectToOpenPort() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			int port = server.getLocalPort();
			try (var socket = SocketUtil.connect("127.0.0.1", port, 3000)) {
				assertTrue("连接应建立", socket.isConnected());
			}
		}
	}

	@Test
	public void connectToClosedPortFails() {
		assertThrows(SocketRuntimeException.class, () -> SocketUtil.connect("127.0.0.1", 1, 500));
	}

	@Test
	public void connectInvalidInput() {
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.connect(null, 80, 1000));
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.connect("localhost", 0, 1000));
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.connect("localhost", 70000, 1000));
		assertThrows(IllegalArgumentException.class, () -> SocketUtil.connect("localhost", 80, -1));
	}

	@Test
	public void reachableProbe() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			int port = server.getLocalPort();
			assertTrue("开放端口应可达", SocketUtil.isReachable("127.0.0.1", port, 2000));
		}
		assertFalse("未开放端口不可达", SocketUtil.isReachable("127.0.0.1", 1, 500));
		assertFalse("非法输入 false", SocketUtil.isReachable(null, 80, 500));
	}

	@Test
	public void portAvailableCheck() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			int port = server.getLocalPort();
			assertFalse("占用端口不可用", SocketUtil.isPortAvailable(port));
		}
		int free = 0;
		try (ServerSocket s = new ServerSocket(0)) {
			free = s.getLocalPort();
		}
		assertTrue("释放端口可用", SocketUtil.isPortAvailable(free));
	}

	@Test
	public void localHostReturnsValidIp() {
		String ip = SocketUtil.getLocalHost();
		assertNotNull("本机 IP 非 null", ip);
		String[] parts = ip.split("\\.");
		assertTrue("IPv4 格式", parts.length == 4);
	}

	@Test
	public void innerIpJudgement() {
		assertTrue(SocketUtil.isInnerIP("10.0.0.1"));
		assertTrue(SocketUtil.isInnerIP("172.16.0.1"));
		assertTrue(SocketUtil.isInnerIP("172.31.255.255"));
		assertTrue(SocketUtil.isInnerIP("192.168.1.1"));
		assertTrue(SocketUtil.isInnerIP("127.0.0.1"));
		assertTrue(SocketUtil.isInnerIP("169.254.1.1"));
		assertFalse(SocketUtil.isInnerIP("8.8.8.8"));
		assertFalse(SocketUtil.isInnerIP("114.114.114.114"));
		assertFalse(SocketUtil.isInnerIP("not-an-ip"));
		assertFalse(SocketUtil.isInnerIP(null));
	}

	@Test
	public void safeCloseNullAndTwice() {
		SocketUtil.safeClose((AutoCloseable[]) null);
		SocketUtil.safeClose(null, null);
	}

	@Test
	public void readWriteRoundTrip() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			int port = server.getLocalPort();
			Thread thread = Thread.ofVirtual().start(() -> {
				try (var s = server.accept()) {
					String line = SocketUtil.readLine(s, null);
					SocketUtil.writeString(s, "echo:" + line, null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			try (var client = SocketUtil.connect("127.0.0.1", port, 3000)) {
				SocketUtil.writeString(client, "hello\n", null);
				assertTrue("回显一致", SocketUtil.readLine(client, null).equals("echo:hello"));
			}
			thread.join(3000);
		}
	}
}
