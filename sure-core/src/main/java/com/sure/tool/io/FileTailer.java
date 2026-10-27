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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 文件 tail -f 监控：虚拟线程增量读文件追加内容，按行回调，支持日志轮转自动重开。
 *
 * <p>特性：
 * <ul>
 *   <li>从文件尾部或文件头（跳过前 N 行）开始跟踪；</li>
 *   <li>行切分支持 LF / CRLF；</li>
 *   <li>编码自动探测 UTF-8 / GBK（也可显式指定）；</li>
 *   <li>文件被替换（rename + 新建）后自动重开新通道并触发 {@link TailerOptions#onRotate()}；</li>
 *   <li>文件被删除时按 {@link TailerOptions#stopOnDelete()} 决定停止或等待重建。</li>
 * </ul>
 *
 * <p>回调在虚拟线程上执行；单行回调抛异常不影响后续行。
 */
public final class FileTailer {

	private final Path file;
	private final Consumer<String> lineListener;
	private final TailerOptions options;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile Thread thread;
	private java.nio.file.attribute.FileTime lastCreation;
	private long maxSize;
	private long position;
	private ByteArrayOutputStream pending = new ByteArrayOutputStream();
	private FileChannel channel;
	private Object lastKey;
	private Charset charset;
	/** 路径文件曾消失（轮转间隙被轮询到），消失后重现视为轮转。 */
	private boolean vanished;
	/** 运行中重建（IOException 触发 waitRecreate）是否属于轮转语义，重建后需触发 onRotate。 */
	private boolean recreateIsRotation;

	/**
	 * 从文件尾部开始跟踪。
	 *
	 * @param file         待跟踪文件
	 * @param lineListener 行回调（虚拟线程执行）
	 * @return FileTailer
	 */
	public static FileTailer start(Path file, Consumer<String> lineListener) {
		return start(file, lineListener, new TailerOptions());
	}

	/**
	 * 按选项跟踪文件。
	 *
	 * @param file         待跟踪文件
	 * @param lineListener 行回调（虚拟线程执行）
	 * @param options      tail 选项
	 * @return FileTailer
	 */
	public static FileTailer start(Path file, Consumer<String> lineListener, TailerOptions options) {
		if (file == null) {
			throw new IllegalArgumentException("file must not be null");
		}
		if (lineListener == null) {
			throw new IllegalArgumentException("lineListener must not be null");
		}
		if (options == null) {
			throw new IllegalArgumentException("options must not be null");
		}
		FileTailer tailer = new FileTailer(file, lineListener, options);
		tailer.start();
		return tailer;
	}

	private FileTailer(Path file, Consumer<String> lineListener, TailerOptions options) {
		this.file = file;
		this.lineListener = lineListener;
		this.options = options;
	}

	/**
	 * 停止跟踪（幂等）。
	 */
	public void stop() {
		running.set(false);
		Thread t = thread;
		if (t != null) {
			t.interrupt();
		}
	}

	/**
	 * 是否正在跟踪。
	 *
	 * @return 是否运行
	 */
	public boolean isRunning() {
		return running.get();
	}

	private void start() {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("FileTailer 已在运行");
		}
		charset = detectCharset(file, options.charset());
		try {
			// 同步完成打开与初始定位，保证 start() 返回后追加内容可被读到
			channel = FileChannel.open(file, StandardOpenOption.READ);
			position = initialPosition(channel);
			lastKey = fileKey(file);
			// 同步取样创建时间基准：tailLoop 首轮即有可比基准（start 后立即追加/替换均能正确判定）
			lastCreation = readCreation(file);
			thread = Thread.ofVirtual().name("sure-tailer-" + file.getFileName()).start(this::loop);
		} catch (IOException e) {
			// 文件暂不存在：虚线程等待文件创建后从头跟踪
			thread = Thread.ofVirtual().name("sure-tailer-" + file.getFileName()).start(this::waitRecreate);
		}
	}

	private void loop() {
		try {
			tailLoop();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			// 读取中断（文件被替换/删除等）：先探测路径文件是否仍存在。
			// 存在 = 轮转/替换场景（Windows 上 reopen 或读取瞬时失败常见），
			// 无论 stopOnDelete 与否都应等待重建重试，否则替换场景会误停止 tailer；
			// 仅当文件确实消失且 stopOnDelete=true 才按删除语义停止。
			boolean stillExists = Files.exists(file);
			if (stillExists || !options.stopOnDelete()) {
				recreateIsRotation = true;
				waitRecreate();
			}
		} finally {
			closeQuietly(channel);
			running.set(false);
		}
	}

	private void tailLoop() throws IOException, InterruptedException {
		maxSize = position;
		while (running.get()) {
			Object key = fileKey(file);
			boolean rotated = false;
			if (key == null) {
				// Windows/NTFS 的 fileKey 恒为 null：以文件存在性 + 创建时间/大小联合检测轮转
				// exists 与 readAttributes 合并为一次原子读：exists 通过后文件被 move/替换
				// （Windows 轮转测试 rename+新建）会抛 NoSuchFileException，按 vanished 处理，
				// 避免 TOCTOU 竞态中断轮转检测路径。
				java.nio.file.attribute.BasicFileAttributes attrs;
				try {
					attrs = Files.readAttributes(file, java.nio.file.attribute.BasicFileAttributes.class);
				} catch (java.nio.file.NoSuchFileException e) {
					if (options.stopOnDelete()) {
						running.set(false);
						break;
					}
					// 记录消失：move/替换间隙被轮询到（轮转语义），重现后必触发轮转
					vanished = true;
					Thread.sleep(options.pollInterval().toMillis());
					continue;
				}
				long size = attrs.size();
				if (System.getProperty("sure.tailer.debug") != null) {
					System.err.println("[tail-debug] t=" + System.currentTimeMillis() + " size=" + size
							+ " creation=" + creationOf(attrs) + " lastCreation=" + lastCreation
							+ " chSize=" + channel.size() + " pos=" + position + " maxSize=" + maxSize);
				}
				// 消失后重现：无论创建时间/大小判据如何，一律视为轮转
				if (vanished) {
					rotated = true;
				} else if (lastCreation != null && !lastCreation.equals(creationOf(attrs))) {
					// 追加写不改创建时间；轮转（rename 后新建）会生成新的创建时间，即使新文件更大也能检出
					rotated = true;
				} else if (size < maxSize) {
					// 创建时间未变的兜底：size 回退 = 文件被替换成更小的新文件
					rotated = true;
				} else if (lastCreation == null) {
					// 基准缺失（初始读取失败等）：重开一次重建基准
					rotated = true;
				}
				maxSize = Math.max(maxSize, size);
				lastCreation = creationOf(attrs);
			} else if (lastKey != null && !lastKey.equals(key)) {
				rotated = true;
			}
			if (rotated) {
				// 文件被替换：轮转重开，并立即建立新文件的大小/创建时间基准。
				// Windows 上替换后新文件打开可能瞬时失败（占用/未就绪）：
				// 等待下一轮重试，不允许异常逃逸导致 tailer 进入重建等待或停止。
				try {
					reopen();
				} catch (IOException e) {
					if (System.getProperty("sure.tailer.debug") != null) {
						System.err.println("[tail-debug] reopen retry e=" + e);
					}
					Thread.sleep(options.pollInterval().toMillis());
				}
				continue;
			}
			channel.position(position);
			ByteBuffer buffer = ByteBuffer.allocate(8192);
			int n = channel.read(buffer);
			if (n < 0) {
				// EOF：探测路径当前文件是否与 channel 底层文件不一致（轮转/重建后新文件）。
				// 判据：路径 size 已变化且 channel 仍是旧文件（chSize==position==旧文件大小），
				// 追加写时 chSize 随同 inode 增长（==pathSize），不会误判。
				// 交叉判据：同大小轮转（新文件字节数相同）时 size 不变，靠创建时间变化检出。
				if (key == null) {
					try {
						BasicFileAttributes eofAttrs = Files.readAttributes(file,
								java.nio.file.attribute.BasicFileAttributes.class);
						long pathSize = eofAttrs.size();
						long chSize = channel.size();
						boolean sizeRotated = pathSize != chSize && chSize == position;
						boolean creationRotated = chSize == position && lastCreation != null
								&& !lastCreation.equals(creationOf(eofAttrs));
						if (sizeRotated || creationRotated) {
							if (System.getProperty("sure.tailer.debug") != null) {
								System.err.println("[tail-debug] EOF-rotate pathSize=" + pathSize
										+ " chSize=" + chSize + " pos=" + position
										+ " creationRotated=" + creationRotated);
							}
							reopen();
							continue;
						}
					} catch (IOException e) {
						// 探测失败（文件消失等）：常规等待下一轮
					}
				}
				Thread.sleep(options.pollInterval().toMillis());
				continue;
			}
			position += n;
			buffer.flip();
			while (buffer.hasRemaining()) {
				byte b = buffer.get();
				if (b == '\n') {
					emitLine(pending.toByteArray(), charset);
					pending.reset();
				} else if (b != '\r') {
					pending.write(b);
				}
			}
		}
	}

	/**
	 * 轮转重开：关闭旧 channel，打开路径当前文件并从头部读取，重建大小/创建时间基准。
	 *
	 * @throws IOException 打开或读取失败
	 */
	private void reopen() throws IOException {
		onRotate();
		vanished = false;
		channel.close();
		channel = FileChannel.open(file, StandardOpenOption.READ);
		position = 0;
		maxSize = channel.size();
		pending.reset();
		lastKey = fileKey(file);
		lastCreation = readCreation(file);
		if (System.getProperty("sure.tailer.debug") != null) {
			System.err.println("[tail-debug] reopen pos=" + position + " maxSize=" + maxSize
					+ " lastCreation=" + lastCreation);
		}
	}

	private void waitRecreate() {
		while (running.get()) {
			if (Files.exists(file)) {
				try {
					channel = FileChannel.open(file, StandardOpenOption.READ);
					position = 0;
					lastKey = fileKey(file);
					lastCreation = readCreation(file);
					// 运行中 IOException 触发的重建属轮转语义（文件被移走/替换），需通知 onRotate
					if (recreateIsRotation) {
						recreateIsRotation = false;
						onRotate();
					}
					tailLoop();
					return;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				} catch (IOException e) {
					// 重建瞬时失败，继续等待
				}
			}
			try {
				Thread.sleep(options.pollInterval().toMillis());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/**
	 * 读取文件创建时间；失败返回 null（由调用方按基准缺失兜底重开）。
	 *
	 * @param file 目标文件
	 * @return 创建时间，失败为 null
	 */
	private static java.nio.file.attribute.FileTime readCreation(Path file) {
		if (forceSameCreation) {
			return java.nio.file.attribute.FileTime.fromMillis(1000L);
		}
		try {
			return Files.readAttributes(file, java.nio.file.attribute.BasicFileAttributes.class).creationTime();
		} catch (IOException e) {
			return null;
		}
	}

	private static java.nio.file.attribute.FileTime creationOf(java.nio.file.attribute.BasicFileAttributes attrs) {
		if (forceSameCreation) {
			return java.nio.file.attribute.FileTime.fromMillis(1000L);
		}
		return attrs.creationTime();
	}

	private static void closeQuietly(FileChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				// 忽略关闭失败
			}
		}
	}

	private long initialPosition(FileChannel channel) throws IOException {
		int lines = options.readFromStart();
		if (lines <= 0) {
			return channel.size();
		}
		long position = 0;
		int found = 0;
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		while (found < lines) {
			channel.position(position);
			buffer.clear();
			int n = channel.read(buffer);
			if (n < 0) {
				break;
			}
			buffer.flip();
			while (buffer.hasRemaining() && found < lines) {
				if (buffer.get() == '\n') {
					found++;
				}
				position++;
			}
		}
		return position;
	}

	private void emitLine(byte[] bytes, Charset charset) {
		String line = new String(bytes, charset);
		if (line.startsWith("\uFEFF")) {
			// 去除 UTF-8 BOM 残留
			line = line.substring(1);
		}
		try {
			lineListener.accept(line);
		} catch (RuntimeException e) {
			// 单行回调异常不影响后续行
		}
	}

	private void onRotate() {
		Runnable callback = options.onRotate();
		if (callback != null) {
			try {
				callback.run();
			} catch (RuntimeException e) {
				// 回调异常不影响 tail 主循环
			}
		}
	}

	/**
	 * 包私有测试钩子：置 true 时 fileKey 恒返回 null，模拟 Windows/NTFS 文件系统
	 * （其 BasicFileAttributes.fileKey() 恒为 null），用于本地覆盖并验证 Windows 分支逻辑。
	 */
	static volatile boolean forceNullFileKey;

	/** 测试钩子：强制创建时间判据失效（模拟部分平台 creationTime 语义差异）。 */
	static volatile boolean forceSameCreation;

	private static Object fileKey(Path file) {
		if (forceNullFileKey) {
			return null;
		}
		try {
			return Files.readAttributes(file, BasicFileAttributes.class).fileKey();
		} catch (NoSuchFileException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	private static Charset detectCharset(Path file, Charset explicit) {
		if (explicit != null) {
			return explicit;
		}
		return probeCharset(file);
	}

	private static Charset probeCharset(Path file) {
		try (InputStream in = Files.newInputStream(file)) {
			byte[] head = new byte[4096];
			int n = in.read(head);
			if (n >= 3 && head[0] == (byte) 0xEF && head[1] == (byte) 0xBB && head[2] == (byte) 0xBF) {
				return StandardCharsets.UTF_8;
			}
			byte[] sample = n < 0 ? new byte[0] : Arrays.copyOf(head, n);
			try {
				StandardCharsets.UTF_8.newDecoder()
						.onMalformedInput(CodingErrorAction.REPORT)
						.onUnmappableCharacter(CodingErrorAction.REPORT)
						.decode(ByteBuffer.wrap(sample));
				return StandardCharsets.UTF_8;
			} catch (CharacterCodingException e) {
				return Charset.forName("GBK");
			}
		} catch (IOException e) {
			return StandardCharsets.UTF_8;
		}
	}
}
