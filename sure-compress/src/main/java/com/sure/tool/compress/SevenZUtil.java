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
package com.sure.tool.compress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

/**
 * 7z 压缩工具门面（LZMA2）。
 *
 * <p>基于 Apache commons-compress 实现目录整体压缩与解压，默认 LZMA2 压缩。
 * 解压时校验条目路径，拒绝 {@code ../} 等越界路径，防止路径穿越攻击。</p>
 *
 * @since 1.4.0
 */
public final class SevenZUtil {

	/** 7z 文件魔数。 */
	private static final byte[] MAGIC = {(byte) 0x37, (byte) 0x7A, (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C};

	private SevenZUtil() {
	}

	/**
	 * 将目录整体压缩为 7z 文件（LZMA2）。
	 *
	 * @param dir   源目录，必须存在且为目录
	 * @param out7z 输出 7z 文件路径
	 * @throws CompressRuntimeException 压缩失败
	 * @throws IllegalArgumentException  参数不合法
	 */
	public static void compressDir(Path dir, Path out7z) {
		if (dir == null || out7z == null) {
			throw new IllegalArgumentException("dir and out7z must not be null");
		}
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("dir must be an existing directory: " + dir);
		}
		try {
			try (SevenZOutputFile out = new SevenZOutputFile(out7z.toFile())) {
				addDirectory(out, dir, dir);
			}
		} catch (IOException e) {
			throw new CompressRuntimeException("7z 压缩失败: " + dir + " -> " + out7z, e);
		}
	}

	private static void addDirectory(SevenZOutputFile out, Path base, Path current) throws IOException {
		try (var stream = Files.list(current)) {
			for (Path child : stream.toList()) {
				String name = base.relativize(child).toString().replace('\\', '/');
				if (Files.isDirectory(child)) {
					SevenZArchiveEntry entry = out.createArchiveEntry(child.toFile(), name + "/");
					out.putArchiveEntry(entry);
					out.closeArchiveEntry();
					addDirectory(out, base, child);
				} else {
					SevenZArchiveEntry entry = out.createArchiveEntry(child.toFile(), name);
					out.putArchiveEntry(entry);
					byte[] buf = new byte[8192];
					try (var in = Files.newInputStream(child)) {
						int n;
						while ((n = in.read(buf)) != -1) {
							out.write(buf, 0, n);
						}
					}
					out.closeArchiveEntry();
				}
			}
		}
	}

	/**
	 * 解压 7z 文件到目标目录（自动创建目录）。
	 *
	 * @param in7z   源 7z 文件
	 * @param outDir 目标目录
	 * @throws CompressRuntimeException 解压失败（含路径越界条目）
	 * @throws IllegalArgumentException  参数不合法
	 */
	public static void decompress(Path in7z, Path outDir) {
		if (in7z == null || outDir == null) {
			throw new IllegalArgumentException("in7z and outDir must not be null");
		}
		if (!Files.isRegularFile(in7z)) {
			throw new IllegalArgumentException("in7z must be an existing file: " + in7z);
		}
		try {
			Files.createDirectories(outDir);
			try (SevenZFile sevenZFile = new SevenZFile(in7z.toFile())) {
				SevenZArchiveEntry entry;
				while ((entry = sevenZFile.getNextEntry()) != null) {
					String name = entry.getName();
					Path target = outDir.resolve(name).normalize();
					Path base = outDir.toAbsolutePath().normalize();
					if (!target.toAbsolutePath().normalize().startsWith(base)) {
						throw new CompressRuntimeException("7z 条目路径越界，已拒绝: " + name);
					}
					if (entry.isDirectory()) {
						Files.createDirectories(target);
					} else {
						Path parent = target.getParent();
						if (parent != null) {
							Files.createDirectories(parent);
						}
						byte[] buf = new byte[8192];
						try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
							int n;
							while ((n = sevenZFile.read(buf)) != -1) {
								out.write(buf, 0, n);
							}
						}
					}
				}
			}
		} catch (CompressRuntimeException e) {
			throw e;
		} catch (IOException e) {
			throw new CompressRuntimeException("7z 解压失败: " + in7z + " -> " + outDir, e);
		}
	}

	/**
	 * 解压 7z 文件到当前工作目录。
	 *
	 * @param in7z 源 7z 文件
	 * @throws CompressRuntimeException 解压失败
	 */
	public static void decompress(Path in7z) {
		decompress(in7z, Path.of("."));
	}

	/**
	 * 判断文件是否为 7z 格式（魔数校验）。
	 *
	 * @param file 待判断文件
	 * @return true 表示 7z 格式
	 * @throws CompressRuntimeException 读取失败
	 */
	public static boolean isSevenZ(Path file) {
		if (file == null || !Files.isRegularFile(file)) {
			return false;
		}
		try {
			byte[] head = Files.readAllBytes(file);
			if (head.length < MAGIC.length) {
				return false;
			}
			for (int i = 0; i < MAGIC.length; i++) {
				if (head[i] != MAGIC[i]) {
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			throw new CompressRuntimeException("读取文件失败: " + file, e);
		}
	}
}
