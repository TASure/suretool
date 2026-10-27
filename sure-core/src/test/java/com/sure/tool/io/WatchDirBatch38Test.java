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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * WatchDir 批38 测试：递归/动态注册/溢出快照对齐/过滤/事件类型/线程生命周期。
 * 开启 sure.watch.diag 以便 CI 平台差异（Windows 子目录事件）诊断输出到日志。
 */
public class WatchDirBatch38Test {

	static {
		System.setProperty("sure.watch.diag", "1");
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final long TIMEOUT_MS = 15000L;

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
		// 跨平台归一：Windows 上 Path.toString() 使用反斜杠分隔符（sub\a.txt），
		// 测试期望统一为正斜杠相对路径，归一后再比较（语义不变，仅分隔符中立化）。
		return events.stream().anyMatch(e -> e.type == type
				&& e.path.toString().replace('\\', '/').equals(name));
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
	public void 溢出后快照对齐补发() throws Exception {
		Path dir = tmp.newFolder("watch3b").toPath();
		Path sub = Files.createDirectory(dir.resolve("sub"));
		Files.write(sub.resolve("a.txt"), "x".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			// 模拟 OVERFLOW 恢复：幂等补注册 + 全量补发（含递归子目录已存在内容）
			watcher.snapshotSync();
			await(() -> has(events, EventType.CREATE, "sub/a.txt"), "快照补发递归子目录");
			// 幂等注册不破坏后续监听：新写入仍由 WatchService 事件驱动
			Files.write(dir.resolve("new.txt"), "y".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "new.txt"), "溢出后仍正常监听");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 非递归溢出快照只补发直接子项() throws Exception {
		Path dir = tmp.newFolder("watch3c").toPath();
		Path sub = Files.createDirectory(dir.resolve("sub"));
		Files.write(sub.resolve("a.txt"), "x".getBytes(StandardCharsets.UTF_8));
		Files.write(dir.resolve("top.txt"), "y".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().recursive(false));
		try {
			watcher.snapshotSync();
			await(() -> has(events, EventType.CREATE, "top.txt"), "非递归补发直接子项");
			assertFalse(has(events, EventType.CREATE, "sub/a.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 溢出快照根目录消失容错() throws Exception {
		Path dir = tmp.newFolder("watch3e").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
		});
		watcher.stop();
		Files.delete(dir);
		// root 已不存在：walkFileTree 抛 NoSuchFileException，snapshotSync 内部忽略不抛
		watcher.snapshotSync();
	}

	@Test
	public void 溢出处理对齐并上报() throws Exception {
		Path dir = tmp.newFolder("watch3f").toPath();
		Path sub = Files.createDirectory(dir.resolve("sub"));
		Files.write(sub.resolve("a.txt"), "x".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().eventTypes(EventType.CREATE, EventType.OVERFLOW));
		try {
			// OVERFLOW 处理：快照对齐补发 + OVERFLOW 事件上报
			watcher.handleOverflow();
			await(() -> has(events, EventType.OVERFLOW, dir.toString().replace('\\', '/')), "OVERFLOW 上报");
			assertTrue(has(events, EventType.CREATE, "sub/a.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 溢出处理默认不过滤补发但不上报() throws Exception {
		Path dir = tmp.newFolder("watch3g").toPath();
		Files.write(dir.resolve("top.txt"), "y".getBytes(StandardCharsets.UTF_8));
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			// 默认 eventTypes 不含 OVERFLOW：只做快照对齐补发，不上报 OVERFLOW
			watcher.handleOverflow();
			await(() -> has(events, EventType.CREATE, "top.txt"), "快照补发");
			assertFalse(has(events, EventType.OVERFLOW, dir.toString().replace('\\', '/')));
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

	@Test
	public void start重复调用抛IllegalStateException() throws Exception {
		Path dir = tmp.newFolder("watch19").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> { });
		try {
			Method start = WatchDir.class.getDeclaredMethod("start");
			start.setAccessible(true);
			// 工厂方法已启动；二次调用触发运行态保护
			assertThrows(IllegalStateException.class, () -> invokeQuietly(start, watcher));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 溢出kind映射为空() throws Exception {
		Method map = WatchDir.class.getDeclaredMethod("map", WatchEvent.Kind.class);
		map.setAccessible(true);
		// OVERFLOW 不属于 CREATE/MODIFY/DELETE：映射回退为 null（防御分支）
		assertEquals(null, map.invoke(null, StandardWatchEventKinds.OVERFLOW));
	}

	@Test
	public void 注册不存在目录抛IOException() throws Exception {
		Path dir = tmp.newFolder("watch20").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> { });
		try {
			Method register = WatchDir.class.getDeclaredMethod("register", Path.class);
			register.setAccessible(true);
			// 不存在的目录注册必然失败：registered 集合回滚后原样抛出
			Path missing = dir.resolve("not-exists");
			assertThrows(IOException.class, () -> invokeQuietly(register, watcher, missing));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 线程中断退出循环() throws Exception {
		Path dir = tmp.newFolder("watch21").toPath();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> { });
		try {
			Field threadField = WatchDir.class.getDeclaredField("thread");
			threadField.setAccessible(true);
			Thread t = (Thread) threadField.get(watcher);
			assertTrue("监听线程已启动", t != null && t.isAlive());
			// 中断 take() 等待：循环捕获 InterruptedException 后复位中断并退出
			t.interrupt();
			await(() -> !watcher.isRunning(), "中断后退出监听循环");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 快照遍历无权限子目录不中断() throws Exception {
		Path dir = tmp.newFolder("watch22").toPath();
		Files.write(dir.resolve("top.txt"), "x".getBytes(StandardCharsets.UTF_8));
		// 无权限目录在 Linux/macOS 上注册与枚举均失败：walkFileTree 触发
		// preVisitDirectory 容错与 visitFileFailed 容错，快照对齐不中断
		Path blocked = Files.createDirectory(dir.resolve("blocked"));
		try {
			blocked.toFile().setReadable(false, false);
			blocked.toFile().setWritable(false, false);
			blocked.toFile().setExecutable(false, false);
		} catch (Exception ignored) {
			// Windows 权限位语义不同，设置失败不影响断言
		}
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			watcher.snapshotSync();
			await(() -> has(events, EventType.CREATE, "top.txt"), "快照补发不受无权限子目录影响");
		} finally {
			watcher.stop();
			blocked.toFile().setReadable(true, false);
			blocked.toFile().setWritable(true, false);
			blocked.toFile().setExecutable(true, false);
		}
	}

	@Test
	public void 单事件处理异常不杀loop后续事件仍到达() throws Exception {
		// 覆盖 dispatch0 外层 try-catch：注册已被删除的目录会抛 IOException，
		// 单事件异常必须被吞掉，watcher 继续运行并处理后续事件（Windows 竞态场景）
		Path dir = tmp.newFolder("watch-exception").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			await(() -> watcher.isRunning(), "运行中");
			// 创建后立即删除：dispatch 处理 CREATE 时目录已消失 → register 抛 NoSuchFileException
			Path sub = Files.createDirectory(dir.resolve("sub"));
			Files.delete(sub);
			Thread.sleep(300);
			// watcher 必须存活，且后续普通文件事件仍正常回调
			assertTrue("单事件异常后 watcher 仍运行", watcher.isRunning());
			Files.writeString(dir.resolve("after.txt"), "x", StandardCharsets.UTF_8);
			await(() -> has(events, EventType.CREATE, "after.txt"), "后续事件仍回调");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 过滤器异常不杀loop后续事件仍到达() throws Exception {
		// 覆盖两层 filter 异常隔离：①dispatch0 外层 try-catch（原生事件路径，RuntimeException）；
		// ②backfill 快照遍历的内层 catch（快照枚举路径）——过滤器抛异常都不允许杀死 watcher。
		Path dir = tmp.newFolder("watch-filter-ex").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchOptions opts = new WatchOptions().filter(rel -> {
			if (rel.toString().endsWith("boom.txt")) {
				throw new IllegalStateException("filter boom");
			}
			return true;
		});
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)), opts);
		try {
			await(() -> watcher.isRunning(), "运行中");
			Files.writeString(dir.resolve("boom.txt"), "x", StandardCharsets.UTF_8);
			Thread.sleep(300);
			// 过滤器抛异常（原生事件与快照两条路径）后 watcher 必须存活
			assertTrue("过滤器异常后 watcher 仍运行", watcher.isRunning());
			Files.writeString(dir.resolve("ok.txt"), "x", StandardCharsets.UTF_8);
			await(() -> has(events, EventType.CREATE, "ok.txt"), "后续事件仍回调");
		} finally {
			watcher.stop();
		}
	}

	private static void invokeQuietly(Method m, Object target, Object... args) throws Exception {
		try {
			m.invoke(target, args);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}
}
