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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 目录监听（JDK WatchService 封装）：虚拟线程轮询事件，支持递归注册、路径过滤与事件类型选择。
 *
 * <p>特性：
 * <ul>
 *   <li>递归监听子目录，新目录创建后动态注册；</li>
 *   <li>路径过滤器（后缀/前缀/正则等）与事件类型选择；</li>
 *   <li>OVERFLOW（事件丢失）单独上报；</li>
 *   <li>回调在虚拟线程上执行，单次回调异常不影响后续事件。</li>
 * </ul>
 */
public final class WatchDir {

	private final Path root;
	private final DirListener listener;
	private final WatchOptions options;
	private final WatchService watchService;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile Thread thread;
	private final java.util.Queue<Path> pendingRegisters = new java.util.concurrent.ConcurrentLinkedQueue<>();
	/** 已注册目录（幂等注册集合）：重复注册在同一 WatchService 上会抛异常或产生重复事件，按集合去重。 */
	private final java.util.Set<Path> registered = java.util.concurrent.ConcurrentHashMap.newKeySet();
	/** 已补发 CREATE 的路径（绝对路径）：快照对齐/backfill 只补发首次出现，避免周期快照重复上报。 */
	private final java.util.Set<Path> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();

	/**
	 * 递归监听目录。
	 *
	 * @param dir      待监听目录
	 * @param listener 事件回调（虚拟线程执行）
	 * @return WatchDir
	 */
	public static WatchDir watch(Path dir, DirListener listener) {
		return watch(dir, listener, new WatchOptions());
	}

	/**
	 * 按选项监听目录。
	 *
	 * @param dir      待监听目录
	 * @param listener 事件回调（虚拟线程执行）
	 * @param options  监听选项
	 * @return WatchDir
	 * @throws RuntimeException 目录不存在或注册失败
	 */
	public static WatchDir watch(Path dir, DirListener listener, WatchOptions... options) {
		if (dir == null) {
			throw new IllegalArgumentException("dir must not be null");
		}
		if (listener == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		if (options == null || options.length == 0) {
			options = new WatchOptions[] { new WatchOptions() };
		}
		if (options[0] == null) {
			throw new IllegalArgumentException("options must not be null");
		}
		WatchDir watcher = new WatchDir(dir, listener, options[0]);
		watcher.start();
		return watcher;
	}

	private WatchDir(Path dir, DirListener listener, WatchOptions options) {
		this.root = dir;
		this.listener = listener;
		this.options = options;
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			throw new RuntimeException("创建 WatchService 失败", e);
		}
	}

	/**
	 * 停止监听（幂等）。
	 */
	public void stop() {
		running.set(false);
		closeQuietly(watchService);
		Thread t = thread;
		if (t != null) {
			t.interrupt();
		}
	}

	/**
	 * 是否正在监听。
	 *
	 * @return 是否运行
	 */
	public boolean isRunning() {
		return running.get();
	}

	private void start() {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("WatchDir 已在运行");
		}
		try {
			if (!Files.isDirectory(root)) {
				throw new IOException("不是目录: " + root);
			}
			if (options.recursive()) {
				registerTree(root);
			} else {
				register(root);
			}
			thread = Thread.ofPlatform().name("sure-watch-" + root.getFileName()).start(this::loop);
		} catch (IOException e) {
			closeQuietly(watchService);
			running.set(false);
			throw new RuntimeException("注册监控目录失败: " + root, e);
		}
	}

	private void registerTree(Path dir) throws IOException {
		Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<Path>() {
			@Override
			public java.nio.file.FileVisitResult preVisitDirectory(Path dir2, java.nio.file.attribute.BasicFileAttributes attrs)
					throws IOException {
				try {
					register(dir2);
				} catch (IOException e) {
					// 单个目录注册失败不中断整体注册：目录可用时由动态注册/快照补发兜底
				}
				return java.nio.file.FileVisitResult.CONTINUE;
			}

			@Override
			public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) {
				// 单个目录访问失败（Windows 权限/瞬时占用）不中断整体注册
				return java.nio.file.FileVisitResult.CONTINUE;
			}
		});
	}

	private void register(Path dir) throws IOException {
		// 幂等：已注册目录直接返回，避免重复注册异常/重复事件
		if (registered.add(dir)) {
			try {
				dir.register(watchService, options.kinds());
			} catch (IOException e) {
				registered.remove(dir);
				throw e;
			}
		}
	}

	private void loop() {
		long lastSync = 0;
		try {
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] loop start root=" + root + " recursive=" + options.recursive());
			}
			while (running.get()) {
				// 先处理动态注册失败的待重试目录（目录未就绪时注册会失败，稍后重试）
				Path toRegister;
				while ((toRegister = pendingRegisters.poll()) != null) {
					if (Files.isDirectory(toRegister)) {
						try {
							register(toRegister);
							// 补发注册前已存在的内容（动态注册窗口内产生的事件已错过）
							backfill(toRegister);
						} catch (IOException e) {
							// 仍不可用：退回队列，短暂退避后下一轮再试
							pendingRegisters.offer(toRegister);
							Thread.sleep(100);
							break;
						}
					}
				}
				WatchKey key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
				// 周期快照对齐兜底不依赖 poll 超时：事件密集平台（Windows）poll 可能持续命中，
				// 若仅在超时分支快照会被饿死，子目录/动态注册事件（原生不报告）将永久等待。
				// 每 1s 无条件执行一次，保证丢失事件以状态快照形式恢复。
				if (System.currentTimeMillis() - lastSync >= 1000) {
					snapshotSync();
					lastSync = System.currentTimeMillis();
					if (System.getProperty("sure.watch.diag") != null) {
						System.err.println("[watch-diag] sync tick root=" + root + " t=" + System.currentTimeMillis());
					}
				}
				if (key == null) {
					continue;
				}
				try {
					for (WatchEvent<?> event : key.pollEvents()) {
						dispatch(key, event);
					}
				} finally {
					key.reset();
				}
			}
		} catch (java.nio.file.ClosedWatchServiceException e) {
			// stop() 关闭服务，正常退出
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Throwable t) {
			// 兜底：未预期异常不允许静默退出（Windows 平台差异定位）
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] loop fatal root=" + root + " t=" + t);
			}
		} finally {
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] loop end root=" + root + " running=" + running.get());
			}
			running.set(false);
		}
	}

	private void dispatch(WatchKey key, WatchEvent<?> event) {
		if (System.getProperty("sure.watch.diag") != null) {
			System.err.println("[watch-diag] evt root=" + root + " kind=" + event.kind() + " ctx=" + event.context());
		}
		try {
			dispatch0(key, event);
		} catch (RuntimeException | IOException e) {
			// 单事件处理异常不允许杀死 loop：Windows 上目录属性/注册瞬时失败常见，
			// 未捕获会逃逸至 loop 顶层 catch Throwable 导致 watcher 整体停止（事件永久丢失）。
			// 已入待注册队列的目录由下一轮周期快照兜底，无需重放当前事件。
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] evt fail root=" + root + " kind=" + event.kind() + " e=" + e);
			}
		}
	}

	private void dispatch0(WatchKey key, WatchEvent<?> event) throws IOException {
		if (event.kind() == java.nio.file.StandardWatchEventKinds.OVERFLOW) {
			handleOverflow();
			return;
		}
		Path dir = (Path) key.watchable();
		Path name = (Path) event.context();
		Path full = dir.resolve(name);
		Path relative = root.relativize(full);
		EventType type = map(event.kind());
		if (type == null || !options.acceptEvent(type)) {
			return;
		}
		// 目录注册与补发不受 filter 限制：filter 只过滤最终事件，否则子目录会被过滤而漏监听
		if (type == EventType.CREATE && options.recursive()) {
			// Files.isDirectory 不抛受检异常（失败返回 false），无需额外容错
			boolean isDir = Files.isDirectory(full);
			if (isDir) {
				try {
					register(full);
					// 补发注册前已存在的内容（子目录内文件可能在动态注册完成前已被写入）
					backfill(full);
				} catch (IOException e) {
					// 目录可能尚未完全就绪：入队延迟重试，不丢动态注册
					pendingRegisters.offer(full);
				}
			} else {
				// Windows 上报 CREATE 时目录可能尚未可见：延迟确认并注册
				pendingRegisters.offer(full);
			}
		}
		if (options.filter() != null && !options.filter().accept(relative)) {
			return;
		}
		// 事件驱动到达的 CREATE 也记录到已见集合，防止周期快照重复补发同一路径
		seen.add(full);
		safeListener(type, relative);
	}

	/**
	 * 补发：枚举目录内已存在内容并补发 CREATE 事件（递归模式下含子目录）。
	 * 动态注册窗口内产生的事件 WatchService 无法感知，注册成功后以枚举补发兜底。
	 *
	 * @param dir 刚注册成功的目录
	 */
	private void backfill(Path dir) {
		try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
			for (Path child : ds) {
				try {
					Path childRel = root.relativize(child);
					if (options.filter() != null && !options.filter().accept(childRel)) {
						continue;
					}
					// 补发语义尊重事件类型过滤：仅接受 CREATE 时才补发，避免越过 eventTypes 误报
					// 已见路径不重复补发（周期快照下保证 CREATE 只上报一次）
					if (options.acceptEvent(EventType.CREATE) && seen.add(child)) {
						safeListener(EventType.CREATE, childRel);
						if (System.getProperty("sure.watch.diag") != null) {
							System.err.println("[watch-diag] backfill evt root=" + root + " path=" + childRel);
						}
					}
					if (Files.isDirectory(child) && options.recursive()) {
						try {
							register(child);
							backfill(child);
						} catch (IOException e) {
							pendingRegisters.offer(child);
						}
					}
				} catch (RuntimeException e) {
					// 单条目瞬时失败（Windows 目录项句柄短暂占用等，DirectoryStream 迭代可能抛
					// 未检查异常）不中断整体枚举：下一轮周期快照会再次尝试，
					// 避免单点异常导致整个目录补发丢失。
					if (System.getProperty("sure.watch.diag") != null) {
						System.err.println("[watch-diag] backfill item fail dir=" + dir + " child=" + child + " e=" + e);
					}
				}
			}
		} catch (IOException e) {
			// 目录被删除等瞬时异常：忽略，下一轮由 WatchKey 事件驱动
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] backfill fail dir=" + dir + " e=" + e);
			}
		}
	}

	/**
	 * OVERFLOW 事件处理：事件队列溢出后 WatchService 已丢失部分事件，
	 * 以当前目录树快照对齐补发，保证 CREATE/删除等状态变化不因溢出而永久丢失
	 * （Windows 高负载下常见）；随后按选项上报 OVERFLOW。
	 *
	 * <p>包私有：测试可直接调用以模拟溢出处理，验证快照对齐与上报语义。
	 */
	void handleOverflow() {
		snapshotSync();
		if (options.acceptEvent(EventType.OVERFLOW)) {
			safeListener(EventType.OVERFLOW, root);
		}
	}

	/**
	 * OVERFLOW 快照对齐：事件队列溢出后以当前目录树为准补注册并补发已存在内容，
	 * 使丢失的事件以状态快照形式恢复（CREATE 不丢、动态注册不漏）。
	 *
	 * <p>包私有：测试可直接调用以模拟溢出恢复场景，验证幂等注册与补发语义。
	 */
	void snapshotSync() {
		try {
			java.util.Set<Path> current = new java.util.HashSet<>();
			if (options.recursive()) {
				Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<Path>() {
					@Override
					public java.nio.file.FileVisitResult preVisitDirectory(Path dir2,
							java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
						try {
							register(dir2);
						} catch (IOException e) {
							// 单个目录注册失败不中断快照遍历
						}
						current.add(dir2);
						return java.nio.file.FileVisitResult.CONTINUE;
					}

					@Override
					public java.nio.file.FileVisitResult visitFile(Path file,
							java.nio.file.attribute.BasicFileAttributes attrs) {
						current.add(file);
						return java.nio.file.FileVisitResult.CONTINUE;
					}

					@Override
					public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) {
						// 单个目录访问失败不中断快照对齐
						return java.nio.file.FileVisitResult.CONTINUE;
					}
				});
			} else {
				register(root);
				current.add(root);
			}
			// 清理已消失项：删除/轮转后重建的文件需能重新补发 CREATE
			seen.removeIf(p -> !current.contains(p));
			backfill(root);
		} catch (IOException e) {
			// 快照对齐失败（目录消失等）：下一轮由 WatchKey 事件驱动继续
			if (System.getProperty("sure.watch.diag") != null) {
				System.err.println("[watch-diag] snapshotSync fail e=" + e);
			}
		}
	}

	private void safeListener(EventType type, Path relative) {
		try {
			listener.onEvent(type, relative);
		} catch (RuntimeException e) {
			// 单次回调异常不影响后续事件
		}
	}

	private static EventType map(WatchEvent.Kind<?> kind) {
		if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE) {
			return EventType.CREATE;
		}
		if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY) {
			return EventType.MODIFY;
		}
		if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_DELETE) {
			return EventType.DELETE;
		}
		return null;
	}

	private static void closeQuietly(WatchService service) {
		try {
			service.close();
		} catch (IOException e) {
			// 忽略关闭失败
		}
	}
}
