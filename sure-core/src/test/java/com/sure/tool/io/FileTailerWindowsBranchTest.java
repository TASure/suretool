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
		System.setProperty("sure.tailer.debug", "true");
	}

	@After
	public void restoreKey() {
		FileTailer.forceNullFileKey = false;
		FileTailer.forceSameCreation = false;
		System.clearProperty("sure.tailer.debug");
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
	public void 创建时间判据失效时靠EOF探测轮转() throws Exception {
		// 模拟部分平台 creationTime 语义差异：轮转后新文件 creationTime 与旧文件相同，
		// size 回退检测失效（新文件更大），必须靠 EOF 探测重开读到新文件。
		FileTailer.forceSameCreation = true;
		Path file = tmp.newFile("win-rot-eof.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-eof.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "新文件行");
			assertEquals("new1", lines.get(0));
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

	@Test
	public void 同大小轮转仍能读到新文件() throws Exception {
		// 批51 场景：old\n 与 new\n 同 2B，仅 size 判据无法检出轮转；
		// 覆盖 EOF 探测的创建时间交叉判据（creationRotated），保证同大小替换后仍读到新行
		Path file = tmp.newFile("win-rot-equal.log").toPath();
		append(file, "a\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-equal.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			// 跨秒等待：秒级时间戳文件系统（tmpfs/macOS）上同秒写入 lastModified 相同，
			// 真实日志轮转与上次写入必相隔更久，跨秒后 lastModified 判据可检出
			Thread.sleep(1100);
			Files.write(file, "b\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "同大小新文件行");
			assertEquals("b", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 同大小同创建时间轮转靠最后修改时间检出() throws Exception {
		// macOS/Windows 创建时间秒级精度：同秒内同大小替换时 creation/size 判据全失效，
		// 覆盖 EOF 探测的最后修改时间兜底判据（modRotated）：写入必前进、追加写暂停不变
		FileTailer.forceSameCreation = true;
		Path file = tmp.newFile("win-rot-mod.log").toPath();
		append(file, "a\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-mod.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			// 跨秒等待：秒级时间戳文件系统上同秒写入 lastModified 相同，跨秒后兜底判据可检出
			Thread.sleep(1100);
			Files.write(file, "b\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "同大小同创建时间新文件行");
			assertEquals("b", lines.get(0));
		} finally {
			tailer.stop();
			FileTailer.forceSameCreation = false;
		}
	}


	@Test
	public void 无间隙不同大小替换触发size判据() throws Exception {
		// 覆盖 macOS/Windows 秒级时间戳下可触发的 size 判据分支（216/217）：
		// 替换间隙无 sleep（无 vanished 分支优先），attrs 直接读新文件，size 变化（4B→5B）
		// 触发 size<maxSize / sizeRotated，不依赖创建时间精度
		Path file = tmp.newFile("win-rot-sz.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-sz.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "size 判据新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void stopOnDelete真时删除文件即停止() throws Exception {
		// stopOnDelete=true 语义：路径文件消失且按删除处理时 tailer 停止
		//（覆盖 loop catch 的 stillExists=false && stopOnDelete=true → 不 waitRecreate 分支）。
		Path file = tmp.newFile("win-stop-del.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add, new TailerOptions().stopOnDelete(true));
		try {
			await(tailer::isRunning, "运行中");
			Files.delete(file);
			await(() -> !tailer.isRunning(), "删除后停止");
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 轮转重开目标为目录时等待后恢复() throws Exception {
		// 覆盖 rotated 分支 reopen() 的 try-catch 重试：attrs 判据（创建时间变化）触发轮转，
		// 但目标被替换为目录（FileChannel.open 跨平台抛 IOException）→ 捕获后等待下一轮重试，
		// 不停止 tailer；恢复文件后继续读到新行。
		Path file = tmp.newFile("win-rot-dir.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-dir.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.createDirectory(file);
			Thread.sleep(600);
			assertTrue("reopen 失败重试中 tailer 仍运行", tailer.isRunning());
			Files.delete(file);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "恢复后新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void EOF轮转重开失败后等待重建继续读() throws Exception {
		// 覆盖批51 两处防御分支：①EOF 探测触发 reopen 但打开失败（目标被替换为目录，
		// FileChannel.open 跨平台抛 IOException）时不允许停止 tailer；
		// ②loop catch IOException 先探测 stillExists——路径文件仍在（替换场景）
		// 无论 stopOnDelete 都进入 waitRecreate，恢复文件后继续读到新行。
		FileTailer.forceSameCreation = true;
		Path file = tmp.newFile("win-rot-eof-fail.log").toPath();
		append(file, "old\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(tailer::isRunning, "运行中");
			Path backup = tmp.getRoot().toPath().resolve("win-rot-eof-fail.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			// 同名目录替换：EOF 探测判定轮转后 reopen 打开目录必抛 IOException（Linux EISDIR / Windows AccessDenied）
			Files.createDirectory(file);
			Thread.sleep(600);
			// 打开失败不允许停止 tailer：路径仍在 → waitRecreate 等待重建
			assertTrue("reopen 失败后 tailer 仍运行", tailer.isRunning());
			Files.delete(file);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.size() == 1, "恢复后新文件行");
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
		}
	}
}
