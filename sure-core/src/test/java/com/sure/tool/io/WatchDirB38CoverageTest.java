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
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link WatchDir} / {@link WatchOptions} 批38覆盖率补测：变参、事件类型选择边界。
 */
public class WatchDirB38CoverageTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final long TIMEOUT_MS = 5000L;

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
	public void watch变参null走默认选项() throws Exception {
		Path dir = tmp.newFolder("vnull").toPath();
		List<String> created = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
			if (type == EventType.CREATE) {
				created.add(path.toString());
			}
		}, (WatchOptions[]) null);
		try {
			Files.write(dir.resolve("a.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> created.contains("a.txt"), "默认选项下 CREATE");
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void eventTypesGetter返回数组() throws Exception {
		WatchOptions options = new WatchOptions().eventTypes(EventType.CREATE, EventType.MODIFY);
		assertEquals(2, options.eventTypes().length);
		assertEquals(EventType.CREATE, options.eventTypes()[0]);
	}

	@Test
	public void eventTypesNull视为全部接受() throws Exception {
		WatchOptions options = new WatchOptions().eventTypes((EventType[]) null);
		assertTrue(options.acceptEvent(EventType.CREATE));
		assertTrue(options.acceptEvent(EventType.MODIFY));
		assertTrue(options.acceptEvent(EventType.DELETE));
		assertTrue(options.acceptEvent(EventType.OVERFLOW));
	}

	@Test
	public void eventTypes空数组兜底注册Create() throws Exception {
		WatchOptions options = new WatchOptions().eventTypes(new EventType[0]);
		java.nio.file.WatchEvent.Kind<?>[] kinds = options.kinds();
		assertEquals(1, kinds.length);
		assertEquals(StandardWatchEventKinds.ENTRY_CREATE, kinds[0]);
	}

	@Test
	public void 仅监听Modify不触发Create() throws Exception {
		Path dir = tmp.newFolder("only-mod").toPath();
		Files.write(dir.resolve("m.txt"), "1".getBytes(StandardCharsets.UTF_8));
		List<String> modifies = new CopyOnWriteArrayList<>();
		List<String> creates = new CopyOnWriteArrayList<>();
		WatchDir watcher = WatchDir.watch(dir, (type, path) -> {
			if (type == EventType.MODIFY) {
				modifies.add(path.toString());
			}
			if (type == EventType.CREATE) {
				creates.add(path.toString());
			}
		}, new WatchOptions().eventTypes(EventType.MODIFY));
		try {
			Files.write(dir.resolve("m.txt"), "2".getBytes(StandardCharsets.UTF_8));
			Files.write(dir.resolve("n.txt"), "x".getBytes(StandardCharsets.UTF_8));
			await(() -> modifies.contains("m.txt"), "MODIFY 触发");
			Thread.sleep(300);
			assertFalse(creates.contains("n.txt"));
		} finally {
			watcher.stop();
		}
	}

	@Test
	public void recursiveGetter默认true() throws Exception {
		WatchOptions options = new WatchOptions();
		assertTrue(options.recursive());
		assertFalse(new WatchOptions().recursive(false).recursive());
	}

	@Test
	public void filterGetter返回设置值() throws Exception {
		PathFilter f = p -> true;
		WatchOptions options = new WatchOptions().filter(f);
		assertEquals(f, options.filter());
	}
}
