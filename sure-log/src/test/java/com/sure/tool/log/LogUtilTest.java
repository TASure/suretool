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
package com.sure.tool.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link LogUtil} / {@link LogFactory} / {@link ConsoleLog} / {@link Slf4jLog} 测试。
 *
 * <p>测试 classpath 含 slf4j-api + slf4j-simple（test scope），因此
 * {@link LogFactory} 探测命中 SLF4J，委托路径经 slf4j-simple 输出；
 * {@link ConsoleLog} 兜底路径直接实例化验证。SLF4J 不存在时的
 * 自动回退分支由 ConsoleLog 行为测试 + 代码审查覆盖。</p>
 */
public class LogUtilTest {

	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream outBuffer;
	private ByteArrayOutputStream errBuffer;

	@Before
	public void capture() {
		this.originalOut = System.out;
		this.originalErr = System.err;
		this.outBuffer = new ByteArrayOutputStream();
		this.errBuffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outBuffer, true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
		LogFactory.clearCache();
	}

	@After
	public void restore() {
		System.setOut(originalOut);
		System.setErr(originalErr);
		LogFactory.clearCache();
	}

	private String out() {
		return outBuffer.toString(StandardCharsets.UTF_8);
	}

	private String err() {
		return errBuffer.toString(StandardCharsets.UTF_8);
	}

	@Test
	public void slf4j委托输出包含占位符替换() {
		LogUtil.info("处理 {}", "abc");
		Assert.assertTrue("slf4j-simple 应输出到 stderr", err().contains("处理 abc"));
	}

	@Test
	public void 工厂按类名获取() {
		Log log = LogFactory.get(LogUtilTest.class);
		Assert.assertEquals(LogUtilTest.class.getName(), log.getName());
	}

	@Test
	public void 工厂同名返回同一实例() {
		Log a = LogFactory.get("same.name");
		Log b = LogFactory.get("same.name");
		Assert.assertSame(a, b);
	}

	@Test
	public void 空名称抛异常() {
		try {
			LogFactory.get("");
			Assert.fail("应抛 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 符合预期
		}
	}

	@Test
	public void consoleLog占位符与级别() {
		ConsoleLog log = new ConsoleLog("console.test");
		log.info("x={}", 1);
		log.warn("警告 {}", "w");
		Assert.assertTrue(out().contains("[INFO] console.test - x=1"));
		Assert.assertTrue(err().contains("[WARN] console.test - 警告 w"));
	}

	@Test
	public void consoleLog异常堆栈() {
		ConsoleLog log = new ConsoleLog("console.test");
		log.error("失败", new RuntimeException("boom"));
		Assert.assertTrue(out().isEmpty());
		Assert.assertTrue(err().contains("失败"));
		Assert.assertTrue(err().contains("boom"));
	}

	@Test
	public void consoleLog占位符对应时不提取异常() {
		ConsoleLog log = new ConsoleLog("console.test");
		RuntimeException ex = new RuntimeException("trap");
		log.info("值={}", ex);
		Assert.assertTrue(out().contains("值=java.lang.RuntimeException"));
		Assert.assertFalse("占位符已对应，不应输出堆栈", out().contains("at com.sure"));
	}

	@Test
	public void slf4j异常堆栈() {
		LogUtil.error("处理失败 {}", "x", new IllegalStateException("state-boom"));
		String text = err();
		Assert.assertTrue(text.contains("处理失败 x"));
		Assert.assertTrue(text.contains("state-boom"));
	}

	@Test
	public void 并发调用不抛错() throws Exception {
		int threads = 10;
		int rounds = 100;
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		AtomicReference<Throwable> error = new AtomicReference<>();
		Thread[] workers = new Thread[threads];
		for (int i = 0; i < threads; i++) {
			int idx = i;
			workers[i] = Thread.startVirtualThread(() -> {
				try {
					start.await();
					for (int j = 0; j < rounds; j++) {
						LogUtil.info("t{} r{}", idx, j);
					}
				} catch (Throwable e) {
					error.set(e);
				} finally {
					done.countDown();
				}
			});
		}
		start.countDown();
		done.await();
		for (Thread worker : workers) {
			worker.join();
		}
		Assert.assertNull("并发调用不应抛错：" + error.get(), error.get());
	}
}
