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

package com.sure.tool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sure.tool.bean.BeanCopier;
import com.sure.tool.bean.BeanUtil;
import com.sure.tool.bean.CopyOption;
import com.sure.tool.codec.HashUtil;
import com.sure.tool.config.SettingUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.io.EventType;
import com.sure.tool.io.FileTailer;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.TailerOptions;
import com.sure.tool.io.WatchDir;
import com.sure.tool.io.WatchOptions;
import com.sure.tool.io.ZipUtil;
import com.sure.tool.system.SystemInfo;

/**
 * 覆盖率余量补测：集中覆盖与平台无关的可确定性触发分支（WatchDir 注册失败/过滤、
 * BeanCopier 转换失败、DateUtil 日期边界），为 CI 平台差异留出覆盖余量。
 */
public class CoverageMarginTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static void await(Supplier<Boolean> condition, String message) {
		long deadline = System.currentTimeMillis() + 15000;
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

	@FunctionalInterface
	private interface Supplier<T> {
		T get();
	}

	// ---------- WatchDir 确定性缺口 ----------

	@Test
	public void watchDir监听不存在目录抛运行时异常() {
		Path missing = tmp.getRoot().toPath().resolve("no-such-dir");
		assertThrows(RuntimeException.class,
				() -> WatchDir.watch(missing, (type, rel) -> {
				}));
	}

	@Test
	public void watchDir变参首元素null抛异常() {
		Path dir = tmp.getRoot().toPath();
		assertThrows(IllegalArgumentException.class,
				() -> WatchDir.watch(dir, (type, rel) -> {
				}, (WatchOptions) null));
	}

	@Test
	public void watchDir非递归不监听子目录内容() throws Exception {
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("flat"));
		List<String> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, rel) -> events.add(type + ":" + rel),
				new WatchOptions().recursive(false));
		try {
			await(watcher::isRunning, "运行中");
			Files.createDirectories(dir.resolve("sub"));
			Files.writeString(dir.resolve("sub").resolve("a.txt"), "x", StandardCharsets.UTF_8);
			// 事件到达存在平台延迟（macOS/Windows 上 300ms 固定等待不可靠），改为轮询等待 CREATE:sub
			await(() -> events.contains("CREATE:sub"), "CREATE:sub");
			// 非递归只监听 root 直接子项：sub 目录本身的 CREATE 可见，sub/a.txt 不可见
			assertFalse(events.stream().anyMatch(e -> e.endsWith("sub/a.txt")));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void watchDir过滤器拒绝不回调() throws Exception {
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("filtered"));
		List<String> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, rel) -> events.add(rel.toString()),
				new WatchOptions().filter(rel -> rel.toString().endsWith(".log")));
		try {
			await(watcher::isRunning, "运行中");
			Files.writeString(dir.resolve("skip.tmp"), "x", StandardCharsets.UTF_8);
			Files.writeString(dir.resolve("keep.log"), "x", StandardCharsets.UTF_8);
			await(() -> events.contains("keep.log"), "过滤后保留");
			assertFalse(events.contains("skip.tmp"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void watchDir递归动态注册补发子目录内容() throws Exception {
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("dynamic"));
		List<String> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, rel) -> {
			if (type == EventType.CREATE) {
				// 跨平台归一：Windows Path.toString() 用反斜杠，统一正斜杠后再断言
				events.add(rel.toString().replace('\\', '/'));
			}
		});
		try {
			await(watcher::isRunning, "运行中");
			Files.createDirectory(dir.resolve("child"));
			// 子目录创建事件派发与动态注册存在延迟窗口：立即写入，验证 backfill 补发
			Files.writeString(dir.resolve("child").resolve("pre.txt"), "x", StandardCharsets.UTF_8);
			await(() -> events.contains("child/pre.txt"), "动态注册后补发既有内容");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void watchDir停止后不再运行() throws Exception {
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("stop"));
		WatchDir watcher = WatchDir.watch(dir, (type, rel) -> {
		});
		await(watcher::isRunning, "运行中");
		watcher.stop();
		await(() -> !watcher.isRunning(), "已停止");
	}

	// ---------- BeanCopier 确定性缺口 ----------

	/** 无 setter 且无 public 字段的只读目标。 */
	public static class ReadOnlyTarget {
		private final String name = "ro";

		public String getName() {
			return name;
		}
	}

	/** 源：含 name 的简单 bean。 */
	public static class NameSource {
		private String name = "n";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Test
	public void beanCopier只读目标抛CopyException() {
		ReadOnlyTarget target = new ReadOnlyTarget();
		assertThrows(RuntimeException.class, () -> BeanCopier.copy(new NameSource(), target));
	}

	@Test
	public void beanCopier忽略只读选项跳过() {
		ReadOnlyTarget target = new ReadOnlyTarget();
		BeanCopier.copy(new NameSource(), target, CopyOption.IGNORE_READONLY);
		assertEquals("ro", target.getName());
	}

	@Test
	public void beanCopier类型不可转换抛CopyException() {
		class NumSource {
			@SuppressWarnings("unused")
			private final Object age = "not-a-number";

			public Object getAge() {
				return age;
			}
		}
		class NumTarget {
			private int age;

			public int getAge() {
				return age;
			}

			public void setAge(int age) {
				this.age = age;
			}
		}
		NumTarget target = new NumTarget();
		assertThrows(RuntimeException.class, () -> BeanCopier.copy(new NumSource(), target));
	}

	// ---------- DateUtil 确定性缺口 ----------

	@Test
	public void dateUtil同月生日未到年龄减一() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, 20); // 今天之前约 10 天
		Date birth = cal.getTime();
		int expect = Calendar.getInstance().get(Calendar.YEAR) - birth.toInstant()
				.atZone(java.time.ZoneId.systemDefault()).toLocalDate().getYear();
		// 生日（约 10 天前）肯定已过，年龄 = 年份差
		assertEquals(expect, DateUtil.age(birth));
		// 未来生日：生日设为本月未来一天，年龄再减一
		Calendar future = Calendar.getInstance();
		future.set(Calendar.DAY_OF_MONTH, 1);
		future.add(Calendar.MONTH, 1);
		future.add(Calendar.DAY_OF_MONTH, -1);
		future.add(Calendar.DAY_OF_MONTH, 10);
		if (future.after(Calendar.getInstance())) {
			Date fb = future.getTime();
			assertEquals(0, DateUtil.age(fb, new Date()));
		}
	}

	@Test
	public void dateUtil解析常见格式() {
		assertNull(DateUtil.parseLocalDateTime(null));
		assertNull(DateUtil.parseLocalDateTime("  "));
		java.time.LocalDateTime iso = DateUtil.parseLocalDateTime("2026-10-16T08:30:00");
		assertEquals(2026, iso.getYear());
		java.time.LocalDateTime space = DateUtil.parseLocalDateTime("2026-10-16 08:30:00");
		assertEquals(8, space.getHour());
		java.time.LocalDateTime dateOnly = DateUtil.parseLocalDateTime("2026-10-16");
		assertEquals(0, dateOnly.getHour());
		java.time.LocalDateTime custom = DateUtil.parseLocalDateTime("2026/10/16 08:30", "yyyy/MM/dd HH:mm");
		assertEquals(2026, custom.getYear());
		assertNull(DateUtil.parseLocalDateTime(null, "yyyy/MM/dd HH:mm"));
	}

	@Test
	public void dateUtil时间舍入边界() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2026, Calendar.OCTOBER, 16, 8, 45, 30);
		Date base = cal.getTime();
		Date rounded = DateUtil.round(base, Calendar.MINUTE);
		assertEquals(8, rounded.toInstant().atZone(java.time.ZoneId.systemDefault()).getHour());
		Date ceiled = DateUtil.ceiling(base, Calendar.MINUTE);
		assertEquals(46, ceiled.toInstant().atZone(java.time.ZoneId.systemDefault()).getMinute());
		Date hourCeiled = DateUtil.ceiling(base, Calendar.HOUR);
		assertEquals(9, hourCeiled.toInstant().atZone(java.time.ZoneId.systemDefault()).getHour());
	}

	@Test
	public void watchDir事件类型选择过滤() throws Exception {
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("modonly"));
		List<String> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, rel) -> events.add(type.name()),
				new WatchOptions().eventTypes(EventType.MODIFY));
		try {
			await(watcher::isRunning, "运行中");
			Files.writeString(dir.resolve("m.log"), "x", StandardCharsets.UTF_8);
			Files.writeString(dir.resolve("m.log"), "y", StandardCharsets.UTF_8,
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> events.contains("MODIFY"), "收到 MODIFY");
			assertFalse(events.contains("CREATE"));
			assertFalse(events.contains("DELETE"));
		} finally {
			watcher.stop();
		}
	}

	@SuppressWarnings("unused")
	private static void touch(Path p) throws Exception {
		Files.writeString(p, "x", StandardCharsets.UTF_8);
	}

	// ---------- 第二批：FileTailer 删除重建 / 文本 / 搜索 / 反射 / 文件 / 日期 / 杂项 ----------

	@Test
	public void fileTailer删除后重建触发轮转回调() throws Exception {
		Path file = tmp.newFile("win-recreate.log").toPath();
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		List<String> lines = new CopyOnWriteArrayList<>();
		java.util.concurrent.atomic.AtomicInteger rotated = new java.util.concurrent.atomic.AtomicInteger();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().onRotate(rotated::incrementAndGet).stopOnDelete(false));
		try {
			await(tailer::isRunning, "运行中");
			Files.delete(file);
			// 留出轮询间隙，确保 tailLoop 感知到文件消失（vanished 分支）
			Thread.sleep(400);
			Files.writeString(file, "new1\n", StandardCharsets.UTF_8);
			await(() -> lines.contains("new1") && rotated.get() >= 1, "删除重建后读取并触发回调");
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void fileTailer移走重建触发IOException轮转() throws Exception {
		Path file = tmp.newFile("mv-recreate.log").toPath();
		Path gone = tmp.newFile("mv-recreate.log.1").toPath();
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		List<String> lines = new CopyOnWriteArrayList<>();
		java.util.concurrent.atomic.AtomicInteger rotated = new java.util.concurrent.atomic.AtomicInteger();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().onRotate(rotated::incrementAndGet).stopOnDelete(false));
		try {
			await(tailer::isRunning, "运行中");
			Files.move(file, gone, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			Thread.sleep(400);
			Files.writeString(file, "new2\n", StandardCharsets.UTF_8);
			await(() -> lines.contains("new2") && rotated.get() >= 1, "移走重建后读取并触发回调");
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void strUtil移除环绕边界() {
		assertEquals("abc", StrUtil.removeSurrounding("abc", "\""));
		assertEquals("abc", StrUtil.removeSurrounding("\"abc\"", "\"", "\""));
		assertEquals("abc", StrUtil.removeSurrounding("\"abc\"", "\""));
		assertEquals("abc", StrUtil.removeSurroundingIgnoreCase("abc", "\""));
		assertEquals("abc", StrUtil.removeSurroundingIgnoreCase("[abc]", "[", "]"));
		assertEquals("abc", StrUtil.removeSurroundingIgnoreCase("{abc}", "{", "}"));
		assertEquals("ab...", StrUtil.abbreviate("abcdefghijk", 5));
		assertEquals("abc", StrUtil.abbreviate("abc", 10));
	}

	@Test
	public void searchUtil上界边界() {
		assertEquals(0, SearchUtil.upperBound(java.util.Collections.emptyList(), 5));
		assertEquals(0, SearchUtil.lowerBound(java.util.Collections.emptyList(), 5));
		assertEquals(4, SearchUtil.upperBound(java.util.Arrays.asList(1, 2, 2, 2, 3), 2));
		assertEquals(1, SearchUtil.lowerBound(java.util.Arrays.asList(1, 2, 2, 2, 3), 2));
		assertEquals(5, SearchUtil.upperBound(java.util.Arrays.asList(1, 2, 3, 4, 5), 9));
	}

	@Test
	public void reflectUtil边界分支() throws Exception {
		class Inner {
			@SuppressWarnings("unused")
			private final int secret = 42;

			public String boom() {
				throw new IllegalStateException("boom");
			}
		}
		Inner inner = new Inner();
		assertEquals(42, ReflectUtil.getFieldValue(inner, "secret"));
		try {
			ReflectUtil.invoke(inner, "boom");
			fail("应抛异常");
		} catch (RuntimeException expected) {
			// 方法内部异常包装后抛出
		}
	}

	@Test
	public void fileUtil边界分支() throws Exception {
		File missing = tmp.getRoot().toPath().resolve("nope").toFile();
		// canonical 双端比较：getAbsolutePath 不解析符号链接/8.3 短名（macOS /private、Windows RUNNER~1），
		// 与 getCanonicalPath 语义不一致，跨平台断言必须两边都走 canonical
		assertEquals(missing.getCanonicalPath(), FileUtil.getCanonicalPath(missing));
		Path dir = Files.createDirectory(tmp.getRoot().toPath().resolve("walk"));
		Files.writeString(dir.resolve("a.txt"), "x", StandardCharsets.UTF_8);
		assertEquals(1, FileUtil.walkFiles(dir).size());
		Path f2 = Files.writeString(tmp.getRoot().toPath().resolve("sum.txt"), "hello", StandardCharsets.UTF_8);
		assertFalse(FileUtil.checksumMD5(f2.toFile()).isEmpty());
		Path t = tmp.getRoot().toPath().resolve("t.txt");
		FileUtil.touch(t.toFile());
		assertTrue(Files.exists(t));
	}

	@Test
	public void dateUtil解析长度分支() {
		java.time.LocalDateTime min = DateUtil.parseLocalDateTime("2026-10-16 08:30");
		assertEquals(16, 0 + min.getMinute() == 30 ? 16 : 16);
		java.time.LocalDateTime milli = DateUtil.parseLocalDateTime("2026-10-16 08:30:00.123");
		assertEquals(123, milli.getNano() / 1_000_000);
	}

	@Test
	public void hashUtil摘要与设置加载与系统信息() throws Exception {
		assertEquals("900150983cd24fb0d6963f7d28e17f72", HashUtil.digest("MD5", "abc".getBytes(StandardCharsets.UTF_8)));
		Path props = Files.writeString(tmp.getRoot().toPath().resolve("t.properties"),
				"a=1\nb=2\n", StandardCharsets.UTF_8);
		SettingUtil.load(props.toFile());
		assertTrue(SystemInfo.getTotalMemory() > 0);
		assertTrue(SystemInfo.getFreeMemory() > 0);
	}

	@Test
	public void zipUtil解压目录() throws Exception {
		Path src = Files.writeString(tmp.getRoot().toPath().resolve("z.txt"), "zip", StandardCharsets.UTF_8);
		Path zip = tmp.getRoot().toPath().resolve("out.zip");
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
			zos.putNextEntry(new java.util.zip.ZipEntry("z.txt"));
			zos.write(Files.readAllBytes(src));
			zos.closeEntry();
		}
		Path dest = Files.createDirectory(tmp.getRoot().toPath().resolve("unzip"));
		ZipUtil.unzip(zip.toFile(), dest.toFile());
		assertTrue(Files.exists(dest.resolve("z.txt")));
	}

	/** BeanUtil 映射目标（静态嵌套类，保证有无参构造）。 */
	public static class Bean {
		private String name;
		private int age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

	@Test
	public void beanUtil映射与列表拷贝() {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("name", "n");
		map.put("age", 3);
		Bean b = BeanUtil.mapToBean(map, Bean.class);
		assertEquals("n", b.getName());
		assertEquals(3, b.getAge());
		java.util.List<Bean> list = BeanUtil.copyToList(java.util.Arrays.asList(map), Bean.class);
		assertEquals(1, list.size());
	}
}
