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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 轻量 TCP 服务端（虚拟线程模型）。
 *
 * <p>每接受一个连接，就为该连接启动一个虚拟线程执行 {@link SocketHandler}，
 * 连接处理异常只记录不影响 accept 循环。{@code stop()} 关闭 ServerSocket 并
 * 中断 accept，幂等安全。</p>
 *
 * @since 1.4.0
 */
public class SocketServer {

	private final int port;
	private final SocketHandler handler;
	private final AtomicBoolean running = new AtomicBoolean(false);

	private volatile ServerSocket serverSocket;

	/**
	 * 构造服务端。
	 *
	 * @param port    监听端口（0 表示自动分配，用 {@link #getPort()} 获取实际端口）
	 * @param handler 连接处理器
	 */
	public SocketServer(int port, SocketHandler handler) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("port must be in [0, 65535], got " + port);
		}
		if (handler == null) {
			throw new IllegalArgumentException("handler must not be null");
		}
		this.port = port;
		this.handler = handler;
	}

	/**
	 * 启动服务端（阻塞在 accept 循环的独立线程中运行，本方法立即返回）。
	 *
	 * @throws SocketRuntimeException 绑定端口失败
	 */
	public synchronized void start() {
		if (running.get()) {
			return;
		}
		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new java.net.InetSocketAddress(port));
		} catch (IOException e) {
			throw new SocketRuntimeException("端口绑定失败: " + port, e);
		}
		running.set(true);
		Thread.ofVirtual().name("sure-socket-accept-" + port).start(this::acceptLoop);
	}

	private void acceptLoop() {
		ServerSocket server = serverSocket;
		while (running.get() && server != null && !server.isClosed()) {
			try {
				Socket socket = server.accept();
				Thread.ofVirtual().name("sure-socket-conn-" + SocketUtil.getRemoteAddress(socket)).start(() -> handleSafely(socket));
			} catch (SocketException e) {
				// stop() 关闭 ServerSocket 触发，正常退出
				break;
			} catch (IOException e) {
				if (running.get()) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
				break;
			}
		}
	}

	private void handleSafely(Socket socket) {
		try (Socket s = socket) {
			handler.handle(s);
		} catch (Exception e) {
			Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
		}
	}

	/**
	 * 停止服务端（幂等）。
	 */
	public synchronized void stop() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		SocketUtil.safeClose(serverSocket);
		serverSocket = null;
	}

	/**
	 * 是否运行中。
	 *
	 * @return true 表示运行中
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * 获取监听端口（port=0 自动分配后返回实际端口）。
	 *
	 * @return 监听端口
	 * @throws IllegalStateException 未启动
	 */
	public int getPort() {
		ServerSocket server = serverSocket;
		if (server == null) {
			throw new IllegalStateException("server not started");
		}
		return server.getLocalPort();
	}

	/**
	 * 获取本机监听地址。
	 *
	 * @return 监听地址
	 * @throws IllegalStateException 未启动
	 */
	public InetAddress getLocalAddress() {
		ServerSocket server = serverSocket;
		if (server == null) {
			throw new IllegalStateException("server not started");
		}
		return server.getInetAddress();
	}
}
