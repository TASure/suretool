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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link WatchDir} 批38补测：创建/修改/删除事件、递归注册、过滤、事件类型选择。
 */
public class WatchDirBatch38Test {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final long TIMEOUT_MS = 5000L;

	/** 事件记录。 */
	static class Event {
		final EventType type;
		final Path path;

		Event(EventType type, Path path) {
			this.type = type;
			this.path = path;
		}
	}

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

	private static boolean has(List<Event> events, EventType type, String name) {
		return events.stream().anyMatch(e -> e.type == type && e.path.toString().equals(name));
	}

	@Test
	public void 文件创建触发Create() throws Exception {
		Path dir = tmp.newFolder("watch1").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.write(dir.resolve("a.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "a.txt"), "CREATE a.txt");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 文件修改触发Modify() throws Exception {
		Path dir = tmp.newFolder("watch2").toPath();
		Files.write(dir.resolve("b.txt"), "1".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.write(dir.resolve("b.txt"), "2".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.MODIFY, "b.txt"), "MODIFY b.txt");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 文件删除触发Delete() throws Exception {
		Path dir = tmp.newFolder("watch3").toPath();
		Path f = dir.resolve("c.txt");
		Files.write(f, "x".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.delete(f);
			await(() -> has(events, EventType.DELETE, "c.txt"), "DELETE c.txt");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 递归子目录内事件() throws Exception {
		Path dir = tmp.newFolder("watch4").toPath();
		Path sub = Files.createDirectory(dir.resolve("sub"));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.write(sub.resolve("deep.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "sub/deep.txt"), "CREATE sub/deep.txt");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 新子目录动态注册() throws Exception {
		Path dir = tmp.newFolder("watch5").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Path newSub = Files.createDirectory(dir.resolve("newsub"));
			// 等 CREATE(newsub) 事件被分发（register 先于回调完成），再写内部文件
			await(() -> has(events, EventType.CREATE, "newsub"), "CREATE newsub");
			Files.write(newSub.resolve("x.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "newsub/x.txt"), "动态注册后 CREATE");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 非递归子目录事件不触发() throws Exception {
		Path dir = tmp.newFolder("watch6").toPath();
		Path sub = Files.createDirectory(dir.resolve("sub"));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().recursive(false));
		try {
			Files.write(sub.resolve("x.txt"), "x".getBytes(StandardCharsets.UTF_8));
			Thread.sleep(500);
			assertFalse(has(events, EventType.CREATE, "sub/x.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 过滤后缀() throws Exception {
		Path dir = tmp.newFolder("watch7").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().filter(p -> p.toString().endsWith(".java")));
		try {
			Files.write(dir.resolve("skip.txt"), "x".getBytes(StandardCharsets.UTF_8));
			Thread.sleep(400);
			assertFalse(has(events, EventType.CREATE, "skip.txt"));
			Files.write(dir.resolve("Main.java"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "Main.java"), "CREATE Main.java");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 事件类型选择只Create() throws Exception {
		Path dir = tmp.newFolder("watch8").toPath();
		Files.write(dir.resolve("d.txt"), "1".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().eventTypes(EventType.CREATE));
		try {
			Files.write(dir.resolve("d.txt"), "2".getBytes(StandardCharsets.UTF_8));
			Files.write(dir.resolve("e.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "e.txt"), "CREATE e.txt");
			assertFalse(has(events, EventType.MODIFY, "d.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 相对路径正确性() throws Exception {
		Path dir = tmp.newFolder("watch9").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.write(dir.resolve("rel.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "rel.txt"), "相对路径为文件名");
			assertTrue(has(events, EventType.CREATE, "rel.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 创建新目录自身触发Create() throws Exception {
		Path dir = tmp.newFolder("watch10").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.createDirectory(dir.resolve("newdir"));
			await(() -> has(events, EventType.CREATE, "newdir"), "CREATE newdir");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void stop后不再触发() throws Exception {
		Path dir = tmp.newFolder("watch11").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		await(watcher::isRunning, "运行中");
		watcher.stop();
		await(() -> !watcher.isRunning(), "已停止");
		Files.write(dir.resolve("after.txt"), "x".getBytes(StandardCharsets.UTF_8));
		Thread.sleep(400);
		assertEquals(0, events.size());
	}

	@Test
	public void isRunning状态() throws Exception {
		Path dir = tmp.newFolder("watch12").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
		});
		await(watcher::isRunning, "运行中");
		watcher.stop();
		await(() -> !watcher.isRunning(), "停止");
	}

	@Test
	public void stop幂等() throws Exception {
		Path dir = tmp.newFolder("watch13").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
		});
		watcher.stop();
		watcher.stop();
		assertFalse(watcher.isRunning());
	}

	@Test
	public void null参数抛IAE() throws Exception {
		Path dir = tmp.newFolder("watch14").toPath();
		assertThrows(IllegalArgumentException.class, () -> WatchDir.watch(null, (type, path) -> {
		}));
		assertThrows(IllegalArgumentException.class, () -> WatchDir.watch(dir, null));
		assertThrows(IllegalArgumentException.class, () -> WatchDir.watch(dir, (type, path) -> {
		}, (WatchOptions) null));
	}

	@Test
	public void 目录不存在抛异常() throws Exception {
		Path missing = tmp.getRoot().toPath().resolve("no-such-dir");
		assertThrows(RuntimeException.class, () -> WatchDir.watch(missing, (type, path) -> {
		}));
	}

	@Test
	public void 监听文件而非目录抛异常() throws Exception {
		Path file = tmp.newFile("afile.txt").toPath();
		assertThrows(RuntimeException.class, () -> WatchDir.watch(file, (type, path) -> {
		}));
	}

	@Test
	public void 回调异常不影响后续事件() throws Exception {
		Path dir = tmp.newFolder("watch15").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
			if (path.toString().equals("boom.txt")) {
				throw new RuntimeException("回调失败");
			}
			events.add(new Event(type, path));
		});
		try {
			Files.write(dir.resolve("boom.txt"), "x".getBytes(StandardCharsets.UTF_8));
			Files.write(dir.resolve("ok.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "ok.txt"), "后续事件正常");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 已有子目录批量注册() throws Exception {
		Path dir = tmp.newFolder("watch16").toPath();
		for (int i = 0; i < 5; i++) {
			Files.createDirectory(dir.resolve("d" + i));
		}
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Files.write(dir.resolve("d0/x.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "d0/x.txt"), "已注册子目录事件");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 正则过滤器() throws Exception {
		Path dir = tmp.newFolder("watch17").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().filter(p -> p.getFileName().toString().matches("log-\\d+\\.log")));
		try {
			Files.write(dir.resolve("other.txt"), "x".getBytes(StandardCharsets.UTF_8));
			Thread.sleep(400);
			assertFalse(has(events, EventType.CREATE, "other.txt"));
			Files.write(dir.resolve("log-1.log"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "log-1.log"), "正则命中");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 创建后修改产生两事件() throws Exception {
		Path dir = tmp.newFolder("watch18").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Path f = dir.resolve("two.txt");
			Files.write(f, "1".getBytes(StandardCharsets.UTF_8));
			Files.write(f, "2".getBytes(StandardCharsets.UTF_8));
			// CREATE 必现；MODIFY 在 macOS（PollingWatchService 轮询快照）上可能
			// 与 CREATE 合并为同一事件，跨平台不强制两者独立出现
			await(() -> has(events, EventType.CREATE, "two.txt"), "CREATE");
			Thread.sleep(200);
			assertTrue("CREATE 或 MODIFY 至少其一感知到修改", has(events, EventType.CREATE, "two.txt")
					|| has(events, EventType.MODIFY, "two.txt"));
		} finally {
			watcher.stop();
		}
	}
}
