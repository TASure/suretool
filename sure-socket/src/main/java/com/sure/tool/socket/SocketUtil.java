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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Socket 静态工具。
 *
 * <p>提供带超时连接、可达性探测、端口可用性、本机 IPv4 获取、
 * 内网 IP 判断与安全关闭等能力。</p>
 *
 * @since 1.4.0
 */
public final class SocketUtil {

	private SocketUtil() {
	}

	/**
	 * 创建 TCP Socket 并带超时连接目标。
	 *
	 * @param host          主机名或 IP
	 * @param port          端口（1-65535）
	 * @param timeoutMillis 连接超时毫秒数（&gt;=0，0 表示无限）
	 * @return 已连接的 Socket
	 * @throws SocketRuntimeException 连接失败
	 */
	public static Socket connect(String host, int port, int timeoutMillis) {
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("host must not be blank");
		}
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("port must be in [1, 65535], got " + port);
		}
		if (timeoutMillis < 0) {
			throw new IllegalArgumentException("timeoutMillis must be >= 0");
		}
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), timeoutMillis);
			return socket;
		} catch (IOException e) {
			throw new SocketRuntimeException("连接失败: " + host + ":" + port, e);
		}
	}

	/**
	 * TCP 可达性探测：目标端口开放返回 true（探测后立即关闭连接）。
	 *
	 * @param host          主机名或 IP
	 * @param port          端口
	 * @param timeoutMillis 探测超时毫秒数
	 * @return true 表示端口可达
	 */
	public static boolean isReachable(String host, int port, int timeoutMillis) {
		if (host == null || host.isBlank() || port < 1 || port > 65535 || timeoutMillis < 0) {
			return false;
		}
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeoutMillis);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 判断本机端口是否可绑定（尝试绑定后立即释放）。
	 *
	 * @param port 端口（0 表示随机端口，恒返回 true 无意义，请传具体端口）
	 * @return true 表示端口可用
	 */
	public static boolean isPortAvailable(int port) {
		if (port < 0 || port > 65535) {
			return false;
		}
		try (ServerSocket server = new ServerSocket()) {
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress(port));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 获取本机 IPv4 地址（跳过 loopback 与虚拟网卡，优先 site-local）。
	 *
	 * @return 本机 IPv4 字符串；不可得时返回 "127.0.0.1"
	 */
	public static String getLocalHost() {
		try {
			InetAddress fallback = InetAddress.getByName("127.0.0.1");
			InetAddress siteLocal = fallback;
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces != null && interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				if (ni.isLoopback() || !ni.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address) {
						if (addr.isSiteLocalAddress()) {
							return addr.getHostAddress();
						}
						siteLocal = addr;
					}
				}
			}
			return siteLocal.getHostAddress();
		} catch (IOException e) {
			return "127.0.0.1";
		}
	}

	/**
	 * 判断 IP 是否为内网/保留地址。
	 *
	 * <p>覆盖 10.x、172.16-31.x、192.168.x、127.x、0.x、169.254.x。</p>
	 *
	 * @param ip IP 地址字符串
	 * @return true 表示内网/保留地址
	 */
	public static boolean isInnerIP(String ip) {
		if (ip == null || ip.isBlank()) {
			return false;
		}
		String[] parts = ip.trim().split("\\.");
		if (parts.length != 4) {
			return false;
		}
		try {
			int first = Integer.parseInt(parts[0]);
			int second = Integer.parseInt(parts[1]);
			return first == 10
					|| (first == 172 && second >= 16 && second <= 31)
					|| (first == 192 && second == 168)
					|| first == 127
					|| first == 0
					|| (first == 169 && second == 254);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 安全关闭：null 安全、忽略异常（便于 finally 中使用）。
	 *
	 * @param closeables 可关闭对象（Socket/ServerSocket/流等）
	 */
	public static void safeClose(AutoCloseable... closeables) {
		if (closeables == null) {
			return;
		}
		for (AutoCloseable closeable : closeables) {
			if (closeable == null) {
				continue;
			}
			try {
				closeable.close();
			} catch (Exception ignored) {
				// 关闭失败无意义，忽略
			}
		}
	}

	/**
	 * 获取 Socket 远程地址描述（host:port）。
	 *
	 * @param socket Socket
	 * @return 远程地址描述；Socket 为 null 或未连接时返回 "unknown"
	 */
	public static String getRemoteAddress(Socket socket) {
		if (socket == null || !socket.isConnected()) {
			return "unknown";
		}
		InetSocketAddress remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		if (remote == null) {
			return "unknown";
		}
		return remote.getAddress().getHostAddress() + ":" + remote.getPort();
	}

	/**
	 * 从 Socket 读取一行（以 {@code \n} 结尾，含换行；读到 EOF 返回已读内容）。
	 *
	 * @param socket  Socket
	 * @param charset 字符集，默认 UTF-8
	 * @return 一行文本（不含换行符）
	 * @throws SocketRuntimeException 读取失败
	 */
	public static String readLine(Socket socket, Charset charset) {
		if (socket == null) {
			throw new IllegalArgumentException("socket must not be null");
		}
		Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
		try {
			var in = socket.getInputStream();
			java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
			int b;
			while ((b = in.read()) != -1) {
				if (b == '\n') {
					break;
				}
				buf.write(b);
			}
			return buf.toString(cs);
		} catch (IOException e) {
			throw new SocketRuntimeException("读取 Socket 失败", e);
		}
	}

	/**
	 * 向 Socket 写出字符串并 flush。
	 *
	 * @param socket  Socket
	 * @param text    文本
	 * @param charset 字符集，默认 UTF-8
	 * @throws SocketRuntimeException 写出失败
	 */
	public static void writeString(Socket socket, String text, Charset charset) {
		if (socket == null) {
			throw new IllegalArgumentException("socket must not be null");
		}
		Charset cs = charset == null ? StandardCharsets.UTF_8 : charset;
		try {
			var out = socket.getOutputStream();
			out.write(text.getBytes(cs));
			out.flush();
		} catch (IOException e) {
			throw new SocketRuntimeException("写入 Socket 失败", e);
		}
	}
}
