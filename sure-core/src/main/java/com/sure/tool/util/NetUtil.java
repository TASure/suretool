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
package com.sure.tool.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;

/**
 * 网络工具类，参考 Hutool 的 {@code NetUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class NetUtil {

	private NetUtil() {
	}

	/**
	 * 获取本机主机名。
	 *
	 * @return 主机名，获取失败返回 {@code null}
	 */
	public static String getLocalhostStr() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取本机局域网 IPv4 地址（优先非回环网卡）。
	 *
	 * @return IPv4 地址，获取失败返回 {@code null}
	 */
	public static String getLocalIpv4() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				if (ni.isLoopback() || !ni.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address) {
						return address.getHostAddress();
					}
				}
			}
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 判断 IP 是否为内网地址（10/172.16-31/192.168/127/169.254/0 段）。
	 *
	 * @param ip IP 地址
	 * @return 是否为内网地址
	 */
	public static boolean isInnerIP(String ip) {
		if (ip == null) {
			return false;
		}
		if (ip.startsWith("::ffff:")) {
			ip = ip.substring(7);
		}
		String[] parts = ip.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		int first;
		int second;
		try {
			first = Integer.parseInt(parts[0]);
			second = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		return first == 10 || first == 127 || first == 0
				|| (first == 172 && second >= 16 && second <= 31)
				|| (first == 192 && second == 168)
				|| (first == 169 && second == 254);
	}

	/**
	 * 是否为合法端口号（1-65535）。
	 *
	 * @param port 端口号
	 * @return 是否合法
	 */
	public static boolean isValidPort(int port) {
		return port > 0 && port <= 0xFFFF;
	}

	/**
	 * 本机指定端口当前是否可用（可成功绑定）。
	 *
	 * @param port 端口号
	 * @return 是否可用
	 */
	public static boolean isUsableLocalPort(int port) {
		if (!isValidPort(port)) {
			return false;
		}
		try (ServerSocket socket = new ServerSocket(port)) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}