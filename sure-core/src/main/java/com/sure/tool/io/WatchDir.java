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
				register(dir2);
				return java.nio.file.FileVisitResult.CONTINUE;
			}
		});
	}

	private void register(Path dir) throws IOException {
		dir.register(watchService, options.kinds());
	}

	private void loop() {
		try {
			while (running.get()) {
				// 先处理动态注册失败的待重试目录（目录未就绪时注册会失败，稍后重试）
				Path toRegister;
				while ((toRegister = pendingRegisters.poll()) != null) {
					if (Files.isDirectory(toRegister)) {
						try {
							register(toRegister);
						} catch (IOException e) {
							// 仍不可用：退回队列，短暂退避后下一轮再试
							pendingRegisters.offer(toRegister);
							Thread.sleep(100);
							break;
						}
					}
				}
				WatchKey key = watchService.take();
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
		} finally {
			running.set(false);
		}
	}

	private void dispatch(WatchKey key, WatchEvent<?> event) {
		if (event.kind() == java.nio.file.StandardWatchEventKinds.OVERFLOW) {
			if (options.acceptEvent(EventType.OVERFLOW)) {
				safeListener(EventType.OVERFLOW, root);
			}
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
		if (options.filter() != null && !options.filter().accept(relative)) {
			return;
		}
		if (type == EventType.CREATE && options.recursive()) {
			if (Files.isDirectory(full)) {
				try {
					register(full);
				} catch (IOException e) {
					// 目录可能尚未完全就绪：入队延迟重试，不丢动态注册
					pendingRegisters.offer(full);
				}
			} else {
				// Windows 上报 CREATE 时目录可能尚未可见：延迟确认并注册
				pendingRegisters.offer(full);
			}
		}
		safeListener(type, relative);
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
