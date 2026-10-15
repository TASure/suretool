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
	 * 获取本机 MAC 地址（格式 {@code AA:BB:CC:DD:EE:FF}，优先非回环网卡）。
	 *
	 * @return MAC 地址，获取失败返回 {@code null}
	 */
	public static String getLocalMacAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				if (ni.isLoopback() || !ni.isUp()) {
					continue;
				}
				byte[] mac = ni.getHardwareAddress();
				if (mac != null && mac.length > 0) {
					StringBuilder sb = new StringBuilder(17);
					for (int i = 0; i < mac.length; i++) {
						if (i > 0) {
							sb.append(':');
						}
						sb.append(String.format("%02X", mac[i]));
					}
					return sb.toString();
				}
			}
			byte[] fallback = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			if (fallback != null && fallback.length > 0) {
				StringBuilder sb = new StringBuilder(17);
				for (int i = 0; i < fallback.length; i++) {
					if (i > 0) {
						sb.append(':');
					}
					sb.append(String.format("%02X", fallback[i]));
				}
				return sb.toString();
			}
			return null;
		} catch (java.net.SocketException | java.net.UnknownHostException e) {
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

	/**
	 * IPv4 地址转 long（大端）。
	 *
	 * @param ip IPv4 地址
	 * @return long 表示
	 * @throws IllegalArgumentException 非法地址
	 */
	public static long ipv4ToLong(String ip) {
		if (!isIpv4(ip)) {
			throw new IllegalArgumentException("非法 IPv4 地址: " + ip);
		}
		String[] parts = ip.split("\\.");
		return (Long.parseLong(parts[0]) << 24) | (Long.parseLong(parts[1]) << 16)
				| (Long.parseLong(parts[2]) << 8) | Long.parseLong(parts[3]);
	}

	/**
	 * long 转 IPv4 地址。
	 *
	 * @param value long 表示
	 * @return IPv4 地址
	 */
	public static String longToIpv4(long value) {
		return (value >>> 24 & 0xFF) + "." + (value >>> 16 & 0xFF) + "." + (value >>> 8 & 0xFF) + "." + (value & 0xFF);
	}

	/**
	 * 校验是否为合法 IPv4 地址。
	 *
	 * @param ip 地址
	 * @return 是否合法
	 */
	public static boolean isIpv4(String ip) {
		if (ip == null) {
			return false;
		}
		String[] parts = ip.split("\\.", -1);
		if (parts.length != 4) {
			return false;
		}
		for (String p : parts) {
			if (p.isEmpty() || p.length() > 3) {
				return false;
			}
			for (int i = 0; i < p.length(); i++) {
				if (!Character.isDigit(p.charAt(i))) {
					return false;
				}
			}
			if (p.length() > 1 && p.charAt(0) == '0') {
				return false; // 拒绝前导零
			}
			if (Integer.parseInt(p) > 255) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验是否为合法 IPv6 地址。
	 *
	 * <p>示例：{@code NetUtil.isIpv6("2001:db8::1") -> true}；
	 * {@code NetUtil.isIpv6("fe80::1%eth0") -> true}（含区域索引）。</p>
	 *
	 * @param ip 地址，可为 {@code null}
	 * @return 是否合法
	 * @since 1.19.0
	 */
	public static boolean isIpv6(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}
		try {
			return InetAddress.getByName(ip) instanceof java.net.Inet6Address;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 校验是否为合法 IP 地址（IPv4 或 IPv6）。
	 *
	 * <p>示例：{@code NetUtil.isIp("192.168.1.1") -> true}；
	 * {@code NetUtil.isIp("::1") -> true}。</p>
	 *
	 * @param ip 地址，可为 {@code null}
	 * @return 是否合法
	 * @since 1.19.0
	 */
	public static boolean isIp(String ip) {
		return isIpv4(ip) || isIpv6(ip);
	}

	/**
	 * 判断 IPv4 是否位于 CIDR 网段内（对齐标准 CIDR 语义）。
	 *
	 * <p>示例：{@code NetUtil.isInCidr("192.168.1.5", "192.168.1.0/24") -> true}；
	 * {@code NetUtil.isInCidr("10.1.2.3", "192.168.0.0/16") -> false}。</p>
	 *
	 * @param ip    IPv4 地址，可为 {@code null}
	 * @param cidr  CIDR 网段（如 {@code 192.168.1.0/24}），可为 {@code null}
	 * @return 是否在网段内；任一参数非法返回 {@code false}
	 * @since 1.19.0
	 */
	public static boolean isInCidr(String ip, String cidr) {
		if (ip == null || cidr == null || !isIpv4(ip)) {
			return false;
		}
		int slash = cidr.indexOf('/');
		if (slash < 0) {
			return false;
		}
		String net = cidr.substring(0, slash);
		if (!isIpv4(net)) {
			return false;
		}
		int prefix;
		try {
			prefix = Integer.parseInt(cidr.substring(slash + 1));
		} catch (NumberFormatException e) {
			return false;
		}
		if (prefix < 0 || prefix > 32) {
			return false;
		}
		long ipVal = ipv4ToLong(ip);
		long netVal = ipv4ToLong(net);
		long mask = prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
		return (ipVal & mask) == (netVal & mask);
	}

	/**
	 * 获取本机全部 IPv4 地址（含回环，按枚举顺序）。
	 *
	 * <p>示例：{@code NetUtil.localIpv4s()} 返回形如 {@code [127.0.0.1, 192.168.x.x]}。</p>
	 *
	 * @return IPv4 地址列表（至少含回环地址）
	 * @since 1.19.0
	 */
	public static java.util.List<String> localIpv4s() {
		java.util.List<String> result = new java.util.ArrayList<>();
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis != null && nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address) {
						result.add(addr.getHostAddress());
					}
				}
			}
		} catch (IOException ignored) {
			// 网络接口枚举失败时至少返回回环地址
		}
		if (result.isEmpty()) {
			result.add("127.0.0.1");
		}
		return result;
	}

	/**
	 * 随机分配一个本机当前可用端口（0-65535）。
	 *
	 * <p>示例：{@code NetUtil.getUsableLocalPort()} 返回一个未被占用的端口号。</p>
	 *
	 * @return 可用端口（1-65535）；分配失败返回 0
	 * @since 1.19.0
	 */
	public static int getUsableLocalPort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			return 0;
		}
	}

}