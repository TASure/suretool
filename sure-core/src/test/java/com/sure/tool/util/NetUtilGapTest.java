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

import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Test;

/**
 * NetUtil 覆盖率补测：内网 IP 判断、端口可用性、IPv4 校验各失败分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class NetUtilGapTest {

	@Test
	public void testLocalInfo() {
		// 尽力调用：环境相关，不断言具体值
		NetUtil.getLocalhostStr();
		NetUtil.getLocalIpv4();
		NetUtil.getLocalMacAddress();
	}

	@Test
	public void testIsInnerIP() {
		Assert.assertFalse(NetUtil.isInnerIP(null));
		Assert.assertTrue(NetUtil.isInnerIP("::ffff:127.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("10.1.2.3"));
		Assert.assertTrue(NetUtil.isInnerIP("172.16.5.5"));
		Assert.assertTrue(NetUtil.isInnerIP("192.168.1.1"));
		Assert.assertTrue(NetUtil.isInnerIP("169.254.1.1"));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isInnerIP("1.2.3"));
		Assert.assertFalse(NetUtil.isInnerIP("abc.def.1.1"));
	}

	@Test
	public void testPort() {
		Assert.assertFalse(NetUtil.isValidPort(0));
		Assert.assertTrue(NetUtil.isValidPort(8080));
		Assert.assertFalse(NetUtil.isValidPort(70000));
		Assert.assertFalse(NetUtil.isUsableLocalPort(0));
		// 取一个空闲端口
		int freePort;
		try (ServerSocket ss = new ServerSocket(0)) {
			freePort = ss.getLocalPort();
		} catch (Exception e) {
			freePort = -1;
		}
		if (freePort > 0) {
			Assert.assertTrue(NetUtil.isUsableLocalPort(freePort));
		}
	}

	@Test
	public void testIpv4Convert() {
		long v = NetUtil.ipv4ToLong("192.168.1.1");
		Assert.assertEquals("192.168.1.1", NetUtil.longToIpv4(v));
		try {
			NetUtil.ipv4ToLong("bad");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testIsIpv4() {
		Assert.assertFalse(NetUtil.isIpv4(null));
		Assert.assertTrue(NetUtil.isIpv4("192.168.1.1"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.3"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.3.4.5"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.a.4"));
		Assert.assertFalse(NetUtil.isIpv4("1.2222.3.4"));
		Assert.assertFalse(NetUtil.isIpv4("01.2.3.4"));
		Assert.assertFalse(NetUtil.isIpv4("256.2.3.4"));
		Assert.assertFalse(NetUtil.isIpv4(".2.3.4"));
	}
}
