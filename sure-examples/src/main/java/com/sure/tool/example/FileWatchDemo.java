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
package com.sure.tool.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.sure.tool.io.FileTailer;
import com.sure.tool.io.TailerOptions;
import com.sure.tool.io.WatchDir;
import com.sure.tool.io.WatchOptions;

/**
 * 文件监控示例：{@link FileTailer} 跟踪日志追加与 rename 轮转，{@link WatchDir} 监听目录事件。
 *
 * <p>运行方式：
 * <pre>{@code
 * mvn -pl sure-examples compile exec:java -Dexec.mainClass=com.sure.tool.example.FileWatchDemo
 * }</pre>
 * 全程约 2 秒，运行结束自动清理临时目录。
 */
public final class FileWatchDemo {

	private FileWatchDemo() {
	}

	/**
	 * 独立运行入口。
	 *
	 * @param args 命令行参数（忽略）
	 * @throws Exception 文件操作或线程等待失败
	 */
	public static void main(String[] args) throws Exception {
		run();
	}

	/**
	 * 运行示例。
	 *
	 * @throws Exception 文件操作或线程等待失败
	 */
	public static void run() throws Exception {
		Path dir = Files.createTempDirectory("sure-tailer-");
		try {
			tail(dir);
			watch(dir);
		} finally {
			Files.deleteIfExists(dir.resolve("app.log.1"));
			Files.deleteIfExists(dir.resolve("app.log"));
			Files.deleteIfExists(dir.resolve("report.txt"));
			Files.deleteIfExists(dir);
		}
	}

	/**
	 * FileTailer 演示：先追加一行，再 rename 轮转并写入同名新文件。
	 *
	 * @param dir 临时目录
	 * @throws Exception 文件操作或线程等待失败
	 */
	private static void tail(Path dir) throws Exception {
		System.out.println("=== FileWatchDemo / FileTailer ===");
		Path log = dir.resolve("app.log");
		Files.writeString(log, "boot line 1\nboot line 2\n");

		AtomicInteger rotations = new AtomicInteger();
		AtomicInteger lines = new AtomicInteger();
		FileTailer tailer = FileTailer.start(log, line -> {
			lines.incrementAndGet();
			System.out.println("[tail] " + line);
		}, new TailerOptions()
				.pollInterval(Duration.ofMillis(100))
				.onRotate(() -> {
					rotations.incrementAndGet();
					System.out.println("[rotate] 检测到轮转，重新打开文件");
				}));

		Files.writeString(log, "append line 3\n", StandardOpenOption.APPEND);
		Thread.sleep(300);

		// 模拟 logrotate：把当前文件改名，再创建同名新文件
		Files.move(log, dir.resolve("app.log.1"), StandardCopyOption.REPLACE_EXISTING);
		Files.writeString(log, "new file line 4\n");
		Thread.sleep(500);

		tailer.stop();
		System.out.println("[done] rotations=" + rotations.get() + " lines=" + lines.get());
	}

	/**
	 * WatchDir 演示：监听目录内文件的创建、修改与删除。
	 *
	 * @param dir 临时目录
	 * @throws Exception 文件操作或线程等待失败
	 */
	private static void watch(Path dir) throws Exception {
		System.out.println("=== FileWatchDemo / WatchDir ===");
		AtomicInteger events = new AtomicInteger();
		WatchDir watcher = WatchDir.watch(dir, (type, relative) -> {
			events.incrementAndGet();
			System.out.println("[watch] " + type + " " + relative);
		}, new WatchOptions().recursive(false));

		Path report = dir.resolve("report.txt");
		Files.writeString(report, "v1\n");
		Thread.sleep(300);
		Files.writeString(report, "v2\n", StandardOpenOption.APPEND);
		Thread.sleep(300);
		Files.deleteIfExists(report);
		Thread.sleep(500);

		watcher.stop();
		System.out.println("[done] watch events=" + events.get());
	}
}
