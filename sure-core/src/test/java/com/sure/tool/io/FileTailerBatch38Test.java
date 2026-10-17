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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link FileTailer} 批38补测：追加监控、行切分、编码探测、轮转重开、删除策略。
 */
public class FileTailerBatch38Test {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final long TIMEOUT_MS = 15000L;

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

	private static void append(Path file, String text) throws IOException {
		Files.write(file, text.getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND,
				java.nio.file.StandardOpenOption.CREATE);
	}

	@Test
	public void 从尾部开始不读历史行() throws Exception {
		Path file = tmp.newFile("tail.log").toPath();
		append(file, "历史1\n历史2\n历史3\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			append(file, "新行\n");
			await(() -> lines.size() == 1, "收到追加行");
			assertEquals(1, lines.size());
			assertEquals("新行", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 追加多行全部回调() throws Exception {
		Path file = tmp.newFile("multi.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			append(file, "a\nb\nc\n");
			await(() -> lines.size() == 3, "收到 3 行");
			assertEquals(List.of("a", "b", "c"), lines);
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void crlf行切分() throws Exception {
		Path file = tmp.newFile("crlf.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			Files.write(file, "甲\r\n乙\r\n".getBytes(StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 2, "CRLF 行");
			assertEquals("甲", lines.get(0));
			assertEquals("乙", lines.get(1));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 跨行缓冲无换行不回调() throws Exception {
		Path file = tmp.newFile("partial.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			append(file, "未完");
			Thread.sleep(300);
			assertEquals(0, lines.size());
			append(file, "成的行\n");
			await(() -> lines.size() == 1, "补全后回调");
			assertEquals("未完成的行", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 空行回调() throws Exception {
		Path file = tmp.newFile("empty.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			append(file, "\n");
			await(() -> lines.size() == 1, "空行");
			assertEquals("", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 中文utf8行() throws Exception {
		Path file = tmp.newFile("cn.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			append(file, "你好世界\n");
			await(() -> lines.size() == 1, "中文行");
			assertEquals("你好世界", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 显式gbk编码() throws Exception {
		Path file = tmp.newFile("gbk.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().charset(Charset.forName("GBK")));
		try {
			Files.write(file, "中文GBK\n".getBytes(Charset.forName("GBK")),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 1, "GBK 行");
			assertEquals("中文GBK", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void gbk自动探测() throws Exception {
		Path file = tmp.newFile("gbk-auto.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		// 先写 GBK 历史样本（探测发生在 start 时，需文件已有内容）
		Files.write(file, "历史行\n".getBytes(Charset.forName("GBK")),
				java.nio.file.StandardOpenOption.CREATE);
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().readFromStart(1));
		try {
			// 追加 GBK 新行：UTF-8 严格解码必然失败 → 自动回退 GBK
			Files.write(file, "自动探测\n".getBytes(Charset.forName("GBK")),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 1, "GBK 自动探测行");
			assertEquals("自动探测", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void readFromStart0等价尾部模式() throws Exception {
		Path file = tmp.newFile("from0.log").toPath();
		append(file, "h1\nh2\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().readFromStart(0));
		try {
			// N=0 表示从尾部开始（同 tail -f），历史行不输出
			append(file, "new\n");
			await(() -> lines.size() == 1, "仅追加行");
			assertEquals(List.of("new"), lines);
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void readFromStart2跳过前两行() throws Exception {
		Path file = tmp.newFile("from2.log").toPath();
		append(file, "h1\nh2\nh3\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().readFromStart(2));
		try {
			await(() -> lines.size() == 1, "跳过 2 行后第 1 行");
			assertEquals(List.of("h3"), lines);
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 轮转自动重开新文件() throws Exception {
		Path file = tmp.newFile("rotate.log").toPath();
		append(file, "old1\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			await(() -> tailer.isRunning(), "运行中");
			// 模拟日志轮转：rename 旧文件 + 新建同名文件
			Path backup = tmp.getRoot().toPath().resolve("rotate.log.1");
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
		Path file = tmp.newFile("rot-cb.log").toPath();
		append(file, "old\n");
		AtomicBoolean rotated = new AtomicBoolean(false);
		FileTailer tailer = FileTailer.start(file, line -> {
		}, new TailerOptions().onRotate(() -> rotated.set(true)));
		try {
			Path backup = tmp.getRoot().toPath().resolve("rot-cb.log.1");
			Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "x\n".getBytes(StandardCharsets.UTF_8));
			await(rotated::get, "轮转回调");
			assertTrue(rotated.get());
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void stop后不再回调() throws Exception {
		Path file = tmp.newFile("stop.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		await(() -> tailer.isRunning(), "运行中");
		tailer.stop();
		await(() -> !tailer.isRunning(), "已停止");
		append(file, "after\n");
		Thread.sleep(300);
		assertEquals(0, lines.size());
	}

	@Test
	public void stop幂等() throws Exception {
		Path file = tmp.newFile("idem.log").toPath();
		FileTailer tailer = FileTailer.start(file, line -> {
		});
		tailer.stop();
		tailer.stop(); // 不抛异常
		assertFalse(tailer.isRunning());
	}

	@Test
	public void isRunning状态变化() throws Exception {
		Path file = tmp.newFile("state.log").toPath();
		FileTailer tailer = FileTailer.start(file, line -> {
		});
		await(tailer::isRunning, "运行中");
		tailer.stop();
		await(() -> !tailer.isRunning(), "停止");
	}

	@Test
	public void null参数抛IAE() throws Exception {
		Path file = tmp.newFile("null.log").toPath();
		assertThrows(IllegalArgumentException.class, () -> FileTailer.start(null, line -> {
		}));
		assertThrows(IllegalArgumentException.class, () -> FileTailer.start(file, null));
		assertThrows(IllegalArgumentException.class, () -> FileTailer.start(file, line -> {
		}, null));
	}

	@Test
	public void stopOnDelete文件删除后停止() throws Exception {
		Path file = tmp.newFile("del-stop.log").toPath();
		append(file, "a\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().stopOnDelete(true));
		await(() -> tailer.isRunning(), "运行中");
		Files.delete(file);
		await(() -> !tailer.isRunning(), "删除后停止");
		assertFalse(tailer.isRunning());
	}

	@Test
	public void 默认删除后等待重建继续读() throws Exception {
		Path file = tmp.newFile("recreate.log").toPath();
		append(file, "a\n");
		List<String> lines = new CopyOnWriteArrayList<>();
		AtomicInteger rotateCount = new AtomicInteger();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().onRotate(rotateCount::incrementAndGet));
		try {
			await(() -> tailer.isRunning(), "运行中");
			Files.delete(file);
			Thread.sleep(200);
			Files.write(file, "重建行\n".getBytes(StandardCharsets.UTF_8));
			await(() -> lines.contains("重建行"), "重建后继续读");
			assertEquals(1, rotateCount.get());
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 大量行快速追加不丢行() throws Exception {
		Path file = tmp.newFile("bulk.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().pollInterval(Duration.ofMillis(50)));
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 200; i++) {
				sb.append("line-").append(i).append('\n');
			}
			Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.APPEND);
			await(() -> lines.size() == 200, "200 行全部收到");
			assertEquals("line-199", lines.get(199));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 短轮询间隔生效() throws Exception {
		Path file = tmp.newFile("fast.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		long start = System.currentTimeMillis();
		FileTailer tailer = FileTailer.start(file, lines::add,
				new TailerOptions().pollInterval(Duration.ofMillis(30)));
		try {
			append(file, "x\n");
			await(() -> lines.size() == 1, "快速收到");
			long elapsed = System.currentTimeMillis() - start;
			assertTrue("响应过慢: " + elapsed + "ms", elapsed < 3000);
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void utf8Bom自动识别() throws Exception {
		Path file = tmp.newFile("bom.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
			Files.write(file, bom, java.nio.file.StandardOpenOption.APPEND);
			append(file, "带BOM\n");
			await(() -> lines.size() == 1, "BOM 行");
			assertEquals("带BOM", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 单行回调异常不影响后续行() throws Exception {
		Path file = tmp.newFile("cb-err.log").toPath();
		List<String> lines = new CopyOnWriteArrayList<>();
		AtomicInteger calls = new AtomicInteger();
		FileTailer tailer = FileTailer.start(file, line -> {
			int i = calls.incrementAndGet();
			if (i == 1) {
				throw new RuntimeException("处理失败");
			}
			lines.add(line);
		});
		try {
			append(file, "one\n");
			append(file, "two\n");
			await(() -> lines.size() == 1, "第二行仍回调");
			assertEquals("two", lines.get(0));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void start重复调用抛IllegalStateException() throws Exception {
		Path file = tmp.newFile("dup.log").toPath();
		FileTailer tailer = FileTailer.start(file, line -> { });
		try {
			Method start = FileTailer.class.getDeclaredMethod("start");
			start.setAccessible(true);
			// 工厂方法已启动；二次调用触发运行态保护
			assertThrows(IllegalStateException.class, () -> invokeQuietly(start, tailer));
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void utf8Bom启动即识别() throws Exception {
		Path file = tmp.newFile("bom-head.log").toPath();
		// 文件在 start 之前已带 BOM：probeCharset 首读即命中 UTF-8 BOM 分支
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		Files.write(file, bom, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
		append(file, "启始BOM\n");
		FileTailer tailer = FileTailer.start(file, line -> { });
		try {
			// start 时已同步完成字符集探测：BOM 命中 → UTF-8
			Field charsetField = FileTailer.class.getDeclaredField("charset");
			charsetField.setAccessible(true);
			Object detected = charsetField.get(tailer);
			assertEquals(StandardCharsets.UTF_8, detected);
		} finally {
			tailer.stop();
		}
	}

	@Test
	public void 读取不存在文件创建时间返回null() throws Exception {
		Method readCreation = FileTailer.class.getDeclaredMethod("readCreation", Path.class);
		readCreation.setAccessible(true);
		Path missing = tmp.getRoot().toPath().resolve("no-such-file.log");
		// 读取失败（文件不存在）返回 null，调用方按"基准缺失"重建
		assertEquals(null, invokeQuietly(readCreation, null, missing));
	}

	private static Object invokeQuietly(Method m, Object target, Object... args) throws Exception {
		try {
			return m.invoke(target, args);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}
}
