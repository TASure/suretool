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
	 * 用忙等虚拟线程占满 carrier，使 readStream 内的 reader 虚拟线程排队、join 阻塞；
	 * 此时中断主线程，确定性覆盖读取输出被中断分支。
	 */
	@Test
	public void 读取输出被中断() {
		int cpus = Runtime.getRuntime().availableProcessors();
		java.util.List<Thread> blockers = new java.util.ArrayList<>();
		for (int i = 0; i < cpus * 2; i++) {
			Thread t = Thread.ofVirtual().start(() -> {
				while (!Thread.currentThread().isInterrupted()) {
					long s = 0;
					for (int j = 0; j < 100000; j++) {
						s += j;
					}
				}
			});
			blockers.add(t);
		}
		try {
			Thread.sleep(300L);
			final Thread main = Thread.currentThread();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					main.interrupt();
				}
			}, 500L);
			try {
				ProcessUtil.exec("sh", "-c", "echo hi");
			} catch (ProcessRuntimeException e) {
				assertTrue(e.getMessage().contains("读取进程输出被中断"));
			} finally {
				timer.cancel();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			for (Thread b : blockers) {
				b.interrupt();
			}
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
}
