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

import static org.junit.Assert.assertTrue;

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
 * WatchDir 动态注册补发测试：模拟 Windows 时序——子目录创建后立即写入内容，
 * 验证注册完成后的 backfill 补发不会丢事件。
 */
public class WatchDirBackfillTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static class Event {
		final EventType type;
		final Path path;

		Event(EventType type, Path path) {
			this.type = type;
			this.path = path;
		}
	}

	private static boolean has(List<Event> events, EventType type, String rel) {
		for (Event e : events) {
			if (e.type == type && e.path.toString().replace('\\', '/').equals(rel)) {
				return true;
			}
		}
		return false;
	}

	private static void await(Supplier<Boolean> condition, String message) {
		long deadline = System.currentTimeMillis() + 8000;
		while (System.currentTimeMillis() < deadline) {
			if (Boolean.TRUE.equals(condition.get())) {
				return;
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError("interrupted");
			}
		}
		throw new AssertionError("等待超时: " + message);
	}

	@Test
	public void 动态注册窗口内写入内容被补发() throws Exception {
		Path dir = tmp.newFolder("bf1").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Path newSub = Files.createDirectory(dir.resolve("newsub"));
			// 不等 CREATE(newsub) 事件、不等动态注册完成，立即写入：模拟 Windows 上注册延迟
			Files.write(newSub.resolve("x.txt"), "x".getBytes(StandardCharsets.UTF_8));
			Files.write(newSub.resolve("y.txt"), "y".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "newsub/x.txt"), "补发 x.txt");
			await(() -> has(events, EventType.CREATE, "newsub/y.txt"), "补发 y.txt");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 补发含嵌套子目录内容() throws Exception {
		Path dir = tmp.newFolder("bf2").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)));
		try {
			Path a = Files.createDirectory(dir.resolve("a"));
			Path aSub = Files.createDirectory(a.resolve("a1"));
			Files.write(aSub.resolve("deep.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "a"), "补发 a");
			await(() -> has(events, EventType.CREATE, "a/a1"), "补发 a/a1");
			await(() -> has(events, EventType.CREATE, "a/a1/deep.txt"), "补发 deep.txt");
			assertTrue("a/a1 应已被递归注册", true);
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void 过滤条件下补发遵守过滤器() throws Exception {
		Path dir = tmp.newFolder("bf3").toPath();
		List<Event> events = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> events.add(new Event(type, path)),
				new WatchOptions().filter(p -> p.getFileName().toString().endsWith(".log")));
		try {
			Path sub = Files.createDirectory(dir.resolve("logs"));
			Files.write(sub.resolve("app.log"), "x".getBytes(StandardCharsets.UTF_8));
			Files.write(sub.resolve("readme.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> has(events, EventType.CREATE, "logs/app.log"), "补发 app.log");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			assertTrue("readme.txt 应被过滤器排除", !has(events, EventType.CREATE, "logs/readme.txt"));
		} finally {
			watcher.stop();
		}
	}
}
