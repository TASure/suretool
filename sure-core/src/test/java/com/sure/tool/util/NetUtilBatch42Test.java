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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 42：NetUtil IPv6 / CIDR / 本地地址 / 可用端口测试。
 *
 * @author suretool
 * @since 1.19.0
 */
public class NetUtilBatch42Test {

	@Test
	public void testIsIpv6() {
		Assert.assertTrue(NetUtil.isIpv6("::1"));
		Assert.assertTrue(NetUtil.isIpv6("2001:db8::1"));
		Assert.assertTrue(NetUtil.isIpv6("fe80::1"));
		Assert.assertTrue(NetUtil.isIpv6("::"));
		Assert.assertFalse(NetUtil.isIpv6("127.0.0.1"));
		Assert.assertFalse(NetUtil.isIpv6(""));
		Assert.assertFalse(NetUtil.isIpv6(null));
		Assert.assertFalse(NetUtil.isIpv6("not-an-ip"));
	}

	@Test
	public void testIsIp() {
		Assert.assertTrue(NetUtil.isIp("192.168.1.1"));
		Assert.assertTrue(NetUtil.isIp("::1"));
		Assert.assertFalse(NetUtil.isIp("256.1.1.1"));
		Assert.assertFalse(NetUtil.isIp(""));
		Assert.assertFalse(NetUtil.isIp(null));
	}

	@Test
	public void testIsInCidr() {
		Assert.assertTrue(NetUtil.isInCidr("192.168.1.5", "192.168.1.0/24"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.2.5", "192.168.1.0/24"));
		Assert.assertTrue(NetUtil.isInCidr("10.1.2.3", "10.0.0.0/8"));
		Assert.assertTrue(NetUtil.isInCidr("8.8.8.8", "0.0.0.0/0"));
		Assert.assertTrue(NetUtil.isInCidr("192.168.1.5", "192.168.1.5/32"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.1.6", "192.168.1.5/32"));
		// 边界与非法
		Assert.assertTrue(NetUtil.isInCidr("192.168.1.255", "192.168.1.0/24"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.1.5", "192.168.1.0/33"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.1.5", "192.168.1.0/-1"));
		Assert.assertFalse(NetUtil.isInCidr("not-ip", "192.168.1.0/24"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.1.5", "not-cidr"));
		Assert.assertFalse(NetUtil.isInCidr("192.168.1.5", null));
		Assert.assertFalse(NetUtil.isInCidr(null, "192.168.1.0/24"));
	}

	@Test
	public void testLocalIpv4s() {
		List<String> addrs = NetUtil.localIpv4s();
		Assert.assertFalse(addrs.isEmpty());
		Assert.assertTrue(addrs.contains("127.0.0.1"));
		for (String addr : addrs) {
			Assert.assertTrue(NetUtil.isIpv4(addr));
		}
	}

	@Test
	public void testGetUsableLocalPort() {
		int port = NetUtil.getUsableLocalPort();
		Assert.assertTrue(port >= 1 && port <= 65535);
	}
}
