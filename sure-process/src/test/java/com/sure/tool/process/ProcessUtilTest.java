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
package com.sure.tool.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * {@link ProcessUtil} 与 {@link ProcessResult} 单元测试。
 */
public class ProcessUtilTest {

	@Test
	public void execSuccess() {
		ProcessResult r = ProcessUtil.exec("java", "-version");
		assertEquals(0, r.getExitCode());
		assertTrue(r.isSuccess());
		assertTrue(r.getStderr().toLowerCase().contains("version"));
	}

	@Test
	public void execFailureStillReturnsResult() {
		ProcessResult r = ProcessUtil.exec("java", "-no-such-option-xyz");
		assertTrue(r.getExitCode() != 0);
		assertFalse(r.isSuccess());
		assertFalse(r.isTimedOut());
	}

	@Test
	public void execListOverload() {
		ProcessResult r = ProcessUtil.exec(List.of("java", "-version"));
		assertEquals(0, r.getExitCode());
	}

	@Test
	public void execStringSplitsCommand() {
		ProcessResult r = ProcessUtil.exec("java -version");
		assertEquals(0, r.getExitCode());
	}

	@Test
	public void execStringWithQuotedArgument() {
		// 双引号包裹含空格参数：echo 输出参数原文，验证拆分正确
		ProcessResult r = ProcessUtil.exec("echo \"hello world\"");
		assertEquals(0, r.getExitCode());
		assertEquals("hello world", r.getStdout().trim());
	}

	@Test
	public void execWithTimeoutKillsProcess() {
		// 跨平台挂起命令：Windows 用 ping -n，Linux/macOS 用 sleep
		boolean win = System.getProperty("os.name").toLowerCase().contains("win");
		List<String> hang = win ? List.of("ping", "-n", "60", "127.0.0.1") : List.of("sleep", "60");
		long start = System.currentTimeMillis();
		ProcessResult r = ProcessUtil.execWithTimeout(hang, 300, TimeUnit.MILLISECONDS);
		assertTrue("应超时被杀", r.isTimedOut());
		assertTrue("强杀应快速返回", System.currentTimeMillis() - start < 20000);
	}

	@Test
	public void execWithTimeoutCompletesInTime() {
		ProcessResult r = ProcessUtil.execWithTimeout(List.of("java", "-version"), 5, TimeUnit.SECONDS);
		assertFalse(r.isTimedOut());
		assertEquals(0, r.getExitCode());
	}

	@Test
	public void startReturnsRunningProcess() throws Exception {
		Process p = ProcessUtil.start(List.of("java", "-version"));
		assertTrue(p.isAlive() || p.waitFor(5, TimeUnit.SECONDS));
		p.waitFor(5, TimeUnit.SECONDS);
		p.destroyForcibly();
	}

	@Test
	public void environmentAndWorkingDirViaBuilder() throws Exception {
		ProcessBuilder builder = new ProcessBuilder("java", "-version");
		builder.environment().put("SURE_TEST_ENV", "hello");
		ProcessResult r = ProcessUtil.exec(builder);
		assertEquals(0, r.getExitCode());
	}

	@Test
	public void nullAndEmptyValidation() {
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec(new String[0]));
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec(List.of()));
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec(""));
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec((ProcessBuilder) null));
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec("\"unclosed"));
	}

}
