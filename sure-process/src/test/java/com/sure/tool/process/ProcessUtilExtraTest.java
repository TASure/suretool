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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

/**
 * {@link ProcessUtil} / {@link ProcessResult} / {@link ProcessRuntimeException} 补测：
 * 参数校验、启动失败、中断分支与 toString。
 */
public class ProcessUtilExtraTest {

	@Test
	public void 超时单位为空抛异常() {
		assertThrows(IllegalArgumentException.class,
				() -> ProcessUtil.execWithTimeout(List.of("java", "-version"), 1, null));
	}

	@Test
	public void 空命令启动抛异常() {
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.start(List.of()));
	}

	@Test
	public void 空构建器启动抛异常() {
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.start((ProcessBuilder) null));
	}

	@Test
	public void 启动不存在命令抛异常() {
		assertThrows(ProcessRuntimeException.class,
				() -> ProcessUtil.start(List.of("suretool-definitely-not-exists-xyz")));
	}

	@Test
	public void 空引号命令拆分抛异常() {
		assertThrows(IllegalArgumentException.class, () -> ProcessUtil.exec("\"\""));
	}

	/** 主线程在 waitFor 上被中断，覆盖等待中断分支。 */
	@Test
	public void 等待进程被中断() {
		final Thread main = Thread.currentThread();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				main.interrupt();
			}
		}, 150L);
		try {
			ProcessUtil.exec("sleep", "60");
			org.junit.Assert.fail("应抛出 ProcessRuntimeException");
		} catch (ProcessRuntimeException e) {
			assertTrue(e.getMessage().contains("中断"));
		} finally {
			timer.cancel();
		}
	}

	/**
	 * 确定性覆盖读取输出被中断分支：传入永不返回的阻塞 InputStream，
	 * 使 readStream 内 reader 虚拟线程必然阻塞、主线程 join 必然阻塞；
	 * Timer 中断主线程后确定性抛"读取进程输出被中断"。
	 * 不依赖 CPU 数与虚拟线程调度时序（此前用 carrier 抢占方案在 Windows runner 上偶发失败）。
	 */
	@Test
	public void 读取输出被中断() throws Exception {
		InputStream blocking = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(60_000L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return -1;
			}
		};
		final Thread main = Thread.currentThread();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				main.interrupt();
			}
		}, 200L);
		try {
			Method m = ProcessUtil.class.getDeclaredMethod("readStream", InputStream.class);
			m.setAccessible(true);
			try {
				m.invoke(null, blocking);
				org.junit.Assert.fail("应抛出 ProcessRuntimeException");
			} catch (java.lang.reflect.InvocationTargetException e) {
				Throwable cause = e.getCause();
				assertTrue("实际异常: " + cause, cause instanceof ProcessRuntimeException);
				assertTrue(cause.getMessage().contains("读取进程输出被中断"));
			}
		} finally {
			timer.cancel();
			Thread.interrupted();
		}
	}

	@Test
	public void toString覆盖长短输出() {
		ProcessResult shortR = new ProcessResult(0, "ok", "err", false);
		assertTrue(shortR.toString().contains("exitCode=0"));
		String longOut = "x".repeat(100);
		ProcessResult longR = new ProcessResult(1, longOut, longOut, true);
		assertTrue(longR.toString().contains("..."));
	}

	@Test
	public void 单参异常构造器() {
		ProcessRuntimeException ex = new ProcessRuntimeException("only message");
		assertEquals("only message", ex.getMessage());
	}

	@Test
	public void 空输入流返回空串() throws Exception {
		// readStream(null) 为防御分支：正常 API 路径拿不到 null 流，用反射覆盖
		Method m = ProcessUtil.class.getDeclaredMethod("readStream", InputStream.class);
		m.setAccessible(true);
		assertEquals("", m.invoke(null, (Object) null));
	}

	@Test
	public void 读取流抛IOException返回空串() throws Exception {
		// 虚拟线程中 readAllBytes 抛 IOException → 内部捕获抛运行时异常（线程内吞掉）
		// → holder 保持 null → 返回空串。确定性覆盖 catch 分支与 null 返回分支
		Method m = ProcessUtil.class.getDeclaredMethod("readStream", InputStream.class);
		m.setAccessible(true);
		InputStream bad = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("boom");
			}
		};
		assertEquals("", m.invoke(null, bad));
	}

	@Test
	public void 正常输入流返回内容() throws Exception {
		Method m = ProcessUtil.class.getDeclaredMethod("readStream", InputStream.class);
		m.setAccessible(true);
		byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
		assertEquals("hello", m.invoke(null, new ByteArrayInputStream(data)));
	}
}
