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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * FileTailer Windows/NTFS 分支模拟测试：本地强制 fileKey 恒为 null（与 Windows 一致），
 * 覆盖轮转重开、删除重建、stopOnDelete 等仅 Windows 可达的路径，防止跨平台回归。
 */
public class FileTailerWindowsBranchTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Before
	public void forceWindowsKey() {
		FileTailer.forceNullFileKey = true;
	}

	@After
	public void restoreKey() {
		FileTailer.forceNullFileKey = false;
	}

	private static void await(Supplier<Boolean> condition, String message) {
		long deadline = System.currentTimeMillis() + 8000;
		while (System.currentTimeMillis() < deadline) {
			if (condition.get()) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError("interrupted");
			}
		}
		throw new AssertionError("等待超时: " + message);
	}

	private static void append(Path file, String s) throws Exception {
		Files.writeString(file, s, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
	}

	@Test
	public void 追加多行全部回调() throws Exception {
		Path file = tmp.newFile("win-append.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			append(file, "a\nb\n");
			await(() -> lines.size() == 2, "收到两行");
			assertEquals("a", lines.get(0));
			assertEquals("b", lines.get(1));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 轮转重开新文件更大() throws Exception {
		Path file = tmp.newFile("win-rot-big.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-big.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			// 新文件比旧文件更大：仅 size 回退检测无法发现，必须靠创建时间
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 轮转重开新文件更小() throws Exception {
		Path file = tmp.newFile("win-rot-small.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-small.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "x\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "新文件行");
			assertEquals("x", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void onRotate回调触发() throws Exception {
		Path file = tmp.newFile("win-rot-cb.log").toPath();
		append(file, "old\n");
		AtomicBoolean rotated = new AtomicBoolean(false);
		FileTailer tailer = FileTailer.start(file, line -> {
		}, new TailerOptions().onRotate(() -> rotated.set(true)));
		try {
			Path backup = tmp.getRoot().toPath().resolve("win-rot-cb.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "x\n".getBytes(StandardCharsets.UTF_8));
			await(rotated::get, "轮转回调");
			assertTrue(rotated.get());
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void onRotate回调抛异常不影响轮转() throws Exception {
		Path file = tmp.newFile("win-rot-ex.log").toPath();
		append(file, "old\n");
		AtomicInteger rotateCount = new AtomicInteger();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().onRotate(() -> {
					rotateCount.incrementAndGet();
					throw new IllegalStateException("回调故意抛异常");
				}));
		try {
			Path backup = tmp.getRoot().toPath().resolve("win-rot-ex.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "y\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.contains("y"), "轮转后读取");
			assertEquals(1, rotateCount.get());
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void stopOnDelete文件删除后停止() throws Exception {
		Path file = tmp.newFile("win-del-stop.log").toPath();
		append(file, "old\n");
		FileTailer tailer = FileTailer.start(file, line -> {
		}, new TailerOptions().stopOnDelete(true));
		try {
			await(tailer::isRunning, "运行中");
			Files.delete(file);
			await(() -> !tailer.isRunning(), "删除后停止");
			assertFalse(tailer.isRunning());
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 默认删除后等待重建继续读() throws Exception {
		Path file = tmp.newFile("win-del-wait.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Files.delete(file);
			Files.write(file, "重建行\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.contains("重建行"), "重建后继续读");
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void start后立即轮转也能读到新文件() throws Exception {
		// start 返回后立即轮转：tailLoop 首轮可能已晚于 move+write，需靠 channel.size() 校验强制重开
		Path file = tmp.newFile("win-rot-immediate.log").toPath();
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			Path backup = tmp.getRoot().toPath().resolve("win-rot-immediate.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 从尾部开始不读历史行() throws Exception {
		Path file = tmp.newFile("win-tail.log").toPath();
		append(file, "old1\nold2\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			append(file, "new\n");
			await(() -> lines.size() == 1, "仅新行");
			assertEquals("new", lines.get(0));
		} finally {
			tailer.stop();
		}
	}
}
