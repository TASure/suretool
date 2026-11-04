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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.Test;

public class FileTailerRotateSoloTest {

	@Test
	public void 轮转重开新文件更大() throws Exception {
		Path dir = Files.createTempDirectory("rot-solo");
		Path file = dir.resolve("rot.log");
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		List<String> lines = new CopyOnWriteArrayList<>();
		FileTailer.forceNullFileKey = true;
		FileTailer tailer = FileTailer.start(file, lines::add);
		try {
			Thread.sleep(150);
			Files.move(file, dir.resolve("rot.log.1"), StandardCopyOption.REPLACE_EXISTING);
			Files.write(file, "new1\n".getBytes(StandardCharsets.UTF_8));
			long deadline = System.currentTimeMillis() + 6000;
			while (System.currentTimeMillis() < deadline && lines.size() < 1) {
				Thread.sleep(20);
			}
			System.out.println("[solo] lines=" + lines);
			assertEquals(1, lines.size());
			assertEquals("new1", lines.get(0));
		} finally {
			tailer.stop();
			FileTailer.forceNullFileKey = false;
		}
	}

	/**
	 * 轮转去重边界：两次独立轮转的新文件都始终无内容时，仍应各通知一次。
	 *
	 * <p>1.19.1 的 rotateNotified 去重只在读到新文件内容后复位，若新文件一直为空，
	 * 第二次独立轮转会被误判为同一次物理轮转而丢弃通知。修复后按文件身份去重：
	 * 同一物理轮转（重开重试 / IOException 重建）仍只通知一次，身份变化则视为新轮转。
	 *
	 * <p>平台说明：Unix/macOS 用 fileKey（inode）区分独立轮转；Windows/NTFS 的 fileKey
	 * 恒为 null，且同名文件在 15 秒内重建会因文件隧道（tunneling）继承旧创建时间，
	 * 因此身份改用「创建时间 + 最后修改时间」组合，后者不参与隧道。本用例两次轮转之间
	 * 必然跨过一次轮询（已收到第一次回调才触发第二次），NTFS 最后修改时间粒度 100ns，
	 * 可稳定区分，Windows 上不会偶发；仅当文件系统最后修改时间粒度粗于两次轮转间隔
	 * （例如 FAT 的 2 秒）时才可能合并为一次通知。
	 */
	@Test
	public void 两次独立轮转均无内容时各通知一次() throws Exception {
		Path dir = Files.createTempDirectory("rot-empty");
		Path file = dir.resolve("rot.log");
		Files.writeString(file, "old\n", StandardCharsets.UTF_8);
		AtomicInteger rotateCount = new AtomicInteger();
		FileTailer tailer = FileTailer.start(file, line -> { },
				new TailerOptions().pollInterval(Duration.ofMillis(20))
						.onRotate(rotateCount::incrementAndGet));
		try {
			await(() -> tailer.isRunning(), 3000);
			// 第一次轮转：新文件为空
			Files.move(file, dir.resolve("rot.log.1"), StandardCopyOption.REPLACE_EXISTING);
			Files.createFile(file);
			await(() -> rotateCount.get() == 1, 6000);
			// 第二次独立轮转：新文件同样为空，仍应再次通知
			Files.move(file, dir.resolve("rot.log.2"), StandardCopyOption.REPLACE_EXISTING);
			Files.createFile(file);
			await(() -> rotateCount.get() >= 2, 6000);
			assertEquals(2, rotateCount.get());
		} finally {
			tailer.stop();
		}
	}

	private static void await(BooleanSupplier condition, long timeoutMillis) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		while (System.currentTimeMillis() < deadline) {
			if (condition.getAsBoolean()) {
				return;
			}
			Thread.sleep(10);
		}
		throw new AssertionError("等待条件超时");
	}
}
