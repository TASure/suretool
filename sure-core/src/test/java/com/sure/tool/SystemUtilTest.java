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
package com.sure.tool;

import com.sure.tool.lang.Console;
import com.sure.tool.lang.StopWatch;
import com.sure.tool.util.NetUtil;
import com.sure.tool.util.RuntimeUtil;
import com.sure.tool.util.StrUtil;
import com.sure.tool.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * SystemUtil / NetUtil / RuntimeUtil / Console / StopWatch 单元测试。
 */
public class SystemUtilTest {

	@Test
	public void testSystemInfo() {
		Assert.assertFalse(StrUtil.isEmpty(SystemUtil.getOsName()));
		Assert.assertNotNull(SystemUtil.getOsArch());
		Assert.assertNotNull(SystemUtil.getJavaVersion());
		Assert.assertTrue(SystemUtil.getUserDir().length() > 0);
		Assert.assertNotNull(SystemUtil.getUserName());
		Assert.assertNotNull(SystemUtil.getUserHome());
		Assert.assertNotNull(SystemUtil.getLineSeparator());
		Assert.assertNotNull(SystemUtil.getFileSeparator());
		Assert.assertTrue(SystemUtil.getTotalMemory() > 0);
		Assert.assertTrue(SystemUtil.getFreeMemory() >= 0);
		Assert.assertTrue(SystemUtil.getMaxMemory() > 0);
	}

	@Test
	public void testNetUtil() {
		Assert.assertTrue(NetUtil.isInnerIP("192.168.1.1"));
		Assert.assertTrue(NetUtil.isInnerIP("10.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("172.16.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("172.31.255.255"));
		Assert.assertTrue(NetUtil.isInnerIP("127.0.0.1"));
		Assert.assertFalse(NetUtil.isInnerIP("172.32.0.1"));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isInnerIP("114.114.114.114"));
		Assert.assertFalse(NetUtil.isInnerIP(null));

		Assert.assertTrue(NetUtil.isValidPort(80));
		Assert.assertFalse(NetUtil.isValidPort(0));
		Assert.assertFalse(NetUtil.isValidPort(65536));

		String ip = NetUtil.getLocalIpv4();
		Assert.assertTrue(ip == null || ip.matches("\\d{1,3}(\\.\\d{1,3}){3}"));
	}

	@Test
	public void testRuntimeUtil() throws Exception {
		boolean windows = SystemUtil.getOsName().toLowerCase().contains("windows");
		String[] cmd = windows
				? new String[] { "cmd", "/c", "echo", "sure-tool-test" }
				: new String[] { "sh", "-c", "echo sure-tool-test" };
		Assert.assertEquals("sure-tool-test", RuntimeUtil.execForStr(cmd));
		Assert.assertTrue(RuntimeUtil.execForLines(cmd).contains("sure-tool-test"));
	}

	@Test
	public void testConsole() {
		// 仅验证不抛异常、不阻塞
		Console.log("suretool {} v{}", "console", 1);
		Console.log("plain");
		Console.print("no-newline");
		Console.error("err {}", "x");
		Console.error(new RuntimeException("demo"));
	}

	@Test
	public void testStopWatch() throws InterruptedException {
		StopWatch stopWatch = new StopWatch("demo");
		Assert.assertFalse(stopWatch.isRunning());
		stopWatch.start();
		Assert.assertTrue(stopWatch.isRunning());
		Thread.sleep(30);
		stopWatch.stop();
		Assert.assertFalse(stopWatch.isRunning());
		Assert.assertTrue(stopWatch.getTotalTimeNanos() > 0);
		Assert.assertTrue(stopWatch.getTotalTimeMillis() >= 0);
		Assert.assertTrue(stopWatch.getTime() >= 0);
		Assert.assertTrue(stopWatch.prettyPrint().contains("demo"));
		Assert.assertTrue(stopWatch.toString().contains("demo"));

		stopWatch.reset();
		Assert.assertEquals(0, stopWatch.getTotalTimeMillis());

		StopWatch anonymous = new StopWatch();
		anonymous.start();
		anonymous.stop();
		Assert.assertTrue(anonymous.getTotalTimeNanos() >= 0);
	}
}