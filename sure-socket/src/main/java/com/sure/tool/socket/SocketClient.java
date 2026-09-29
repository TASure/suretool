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
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TCP 客户端门面。
 *
 * <p>构造即连接；提供发送/读取（读满字节、读一行、读到 EOF）与幂等关闭。</p>
 *
 * @since 1.4.0
 */
public class SocketClient implements AutoCloseable {

	private final Socket socket;

	/**
	 * 构造并连接。
	 *
	 * @param host 主机
	 * @param port 端口
	 * @throws SocketRuntimeException 连接失败
	 */
	public SocketClient(String host, int port) {
		this(host, port, 0);
	}

	/**
	 * 构造并带超时连接。
	 *
	 * @param host          主机
	 * @param port          端口
	 * @param timeoutMillis 连接超时毫秒数（0 表示无限）
	 * @throws SocketRuntimeException 连接失败
	 */
	public SocketClient(String host, int port, int timeoutMillis) {
		this.socket = SocketUtil.connect(host, port, timeoutMillis);
	}

	/**
	 * 包装已有 Socket。
	 *
	 * @param socket 已连接 Socket
	 */
	public SocketClient(Socket socket) {
		if (socket == null) {
			throw new IllegalArgumentException("socket must not be null");
		}
		this.socket = socket;
	}

	/**
	 * 发送字节。
	 *
	 * @param data 字节数据
	 * @throws SocketRuntimeException 发送失败
	 */
	public void send(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		try {
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			throw new SocketRuntimeException("发送失败", e);
		}
	}

	/**
	 * 发送字符串。
	 *
	 * @param text    文本
	 * @param charset 字符集，默认 UTF-8
	 * @throws SocketRuntimeException 发送失败
	 */
	public void sendString(String text, Charset charset) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
		send(text.getBytes(cs));
	}

	/**
	 * 读满指定字节数组（阻塞直到读满或流关闭）。
	 *
	 * @param buffer 缓冲区
	 * @return 实际读到的字节数
	 * @throws SocketRuntimeException 读取失败
	 */
	public int receive(byte[] buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("buffer must not be null");
		}
		try {
			int total = 0;
			while (total < buffer.length) {
				int n = socket.getInputStream().read(buffer, total, buffer.length - total);
				if (n == -1) {
					break;
				}
				total += n;
			}
			return total;
		} catch (IOException e) {
			throw new SocketRuntimeException("读取失败", e);
		}
	}

	/**
	 * 读取一行（以 {@code \n} 结尾，不含换行符；读到 EOF 返回已读内容）。
	 *
	 * @param charset 字符集，默认 UTF-8
	 * @return 一行文本
	 * @throws SocketRuntimeException 读取失败
	 */
	public String receiveLine(Charset charset) {
		Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
		return SocketUtil.readLine(socket, cs);
	}

	/**
	 * 读取到流关闭（EOF），返回全部内容。
	 *
	 * @param charset 字符集，默认 UTF-8
	 * @return 全部文本
	 * @throws SocketRuntimeException 读取失败
	 */
	public String receiveAll(Charset charset) {
		Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
		try {
			return new String(socket.getInputStream().readAllBytes(), cs);
		} catch (IOException e) {
			throw new SocketRuntimeException("读取失败", e);
		}
	}

	/**
	 * 是否已连接。
	 *
	 * @return true 表示已连接且未关闭
	 */
	public boolean isConnected() {
		return socket.isConnected() && !socket.isClosed();
	}

	/**
	 * 获取底层 Socket。
	 *
	 * @return Socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * 关闭连接（幂等）。
	 */
	@Override
	public void close() {
		SocketUtil.safeClose(socket);
	}
}
