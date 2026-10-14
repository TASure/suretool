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

package com.sure.tool.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link FileTailer} 批38覆盖率补测：文件不存在等待重建、空文件定位、回调异常。
 */
public class FileTailerB38CoverageTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final long TIMEOUT_MS = 6000L;

	private static void await(Supplier<Boolean> condition, String message) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (Boolean.TRUE.equals(condition.get())) {
				return;
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("等待被中断: " + message);
			}
		}
		fail("等待超时: " + message);
	}

	@Test
	public void 文件不存在时start等待创建() throws Exception {
		Path file = tmp.getRoot().toPath().resolve("wait-create.log");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().pollInterval(Duration.ofMillis(50)));
		try {
			// start 时文件不存在 → 虚线程进入 waitRecreate
			assertTrue(tailer.isRunning());
			Thread.sleep(300);
			Files.write(file, "首行\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.contains("首行"), "文件创建后从头读取");
			Files.write(file, "次行\n".getBytes(StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.contains("次行"), "追加读取");
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 空文件readFromStart跳过N行不报错() throws Exception {
		Path file = tmp.newFile("empty-start.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().readFromStart(5));
		try {
			// 空文件 + readFromStart(N)：读到 EOF 直接返回，随后正常跟踪
			Files.write(file, "later\n".getBytes(StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 1, "空文件定位后收到追加行");
			assertEquals("later", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void onRotate回调抛异常不影响轮转() throws Exception {
		Path file = tmp.newFile("rot-boom.log").toPath();
		Files.write(file, "old\n".getBytes(StandardCharsets.UTF_8));
		List<String> lines = new CopyOnWriteArrayList<>();
		AtomicBoolean rotated = new AtomicBoolean();
		FileTailer tailer = FileTailer.start(file, lines::add, new TailerOptions()
				.onRotate(() -> {
					rotated.set(true);
					throw new RuntimeException("rotate boom");
				}));
		try {
			Path backup = tmp.getRoot().toPath().resolve("rot-boom.log.1");
			Files.move(file, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(rotated::get, "轮转回调触发");
			await(() -> lines.contains("new1"), "回调异常后仍读到新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void readFromStart大于行数定位到文件尾() throws Exception {
		Path file = tmp.newFile("over-start.log").toPath();
		Files.write(file, "a\nb\n".getBytes(StandardCharsets.UTF_8));
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().readFromStart(99));
		try {
			// N 超过行数 → 定位到文件尾，历史不输出，只跟踪新增
			Files.write(file, "new\n".getBytes(StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 1, "定位到文件尾后收到追加行");
			assertEquals("new", lines.get(0));
		} finally {
			tailer.stop();
		}
	}
}
