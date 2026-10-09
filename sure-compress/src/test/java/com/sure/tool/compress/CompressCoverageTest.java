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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link SevenZUtil} / {@link BrotliUtil} / {@link CompressRuntimeException} 覆盖率补测。
 *
 * <p>聚焦异常分支、参数校验、单参重载与越界拒绝路径。</p>
 */
public class CompressCoverageTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void compressDir参数校验与失败分支() {
		// null 校验
		assertThrows(IllegalArgumentException.class,
				() -> SevenZUtil.compressDir(null, tmp.getRoot().toPath().resolve("o.7z")));
		assertThrows(IllegalArgumentException.class,
				() -> SevenZUtil.compressDir(tmp.getRoot().toPath(), null));
		// dir 非目录
		Path f = tmp.getRoot().toPath().resolve("plain.txt");
		assertThrows(IllegalArgumentException.class,
				() -> SevenZUtil.compressDir(f, tmp.getRoot().toPath().resolve("o.7z")));
	}

	@Test
	public void compressDir写失败抛运行时异常() throws Exception {
		Path srcDir = tmp.getRoot().toPath().resolve("src");
		Files.createDirectories(srcDir);
		Files.writeString(srcDir.resolve("a.txt"), "data");
		// out7z 落在不存在的父目录 -> new SevenZOutputFile 抛 IOException
		Path badOut = tmp.getRoot().toPath().resolve("no_such_dir/o.7z");
		assertThrows(CompressRuntimeException.class, () -> SevenZUtil.compressDir(srcDir, badOut));
	}

	@Test
	public void decompress参数校验() throws Exception {
		Path in7z = tmp.getRoot().toPath().resolve("x.7z");
		Files.createFile(in7z);
		assertThrows(IllegalArgumentException.class, () -> SevenZUtil.decompress(null, tmp.getRoot().toPath()));
		assertThrows(IllegalArgumentException.class, () -> SevenZUtil.decompress(in7z, null));
		// in7z 不是常规文件
		assertThrows(IllegalArgumentException.class,
				() -> SevenZUtil.decompress(tmp.getRoot().toPath(), tmp.getRoot().toPath()));
	}

	@Test
	public void decompress损坏文件抛运行时异常() throws Exception {
		Path bad = tmp.getRoot().toPath().resolve("bad.7z");
		Files.write(bad, new byte[] {0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C, 0, 1, 2});
		assertThrows(CompressRuntimeException.class,
				() -> SevenZUtil.decompress(bad, tmp.getRoot().toPath().resolve("out")));
	}

	@Test
	public void decompress单参重载() throws Exception {
		Path srcDir = tmp.getRoot().toPath().resolve("src");
		Files.createDirectories(srcDir);
		Files.writeString(srcDir.resolve("a.txt"), "single-arg");
		Path out7z = tmp.getRoot().toPath().resolve("s.7z");
		SevenZUtil.compressDir(srcDir, out7z);
		// 单参重载解压到 .（工作目录）——不验证落盘，仅覆盖调用行
		SevenZUtil.decompress(out7z);
	}

	@Test
	public void pathTraversal条目被拒绝() throws Exception {
		// 手动构造含 ../ 条目的 7z
		Path evil = tmp.getRoot().toPath().resolve("evil.7z");
		try (SevenZOutputFile out = new SevenZOutputFile(evil.toFile())) {
			SevenZArchiveEntry entry = out.createArchiveEntry(
					tmp.getRoot().toPath().resolve("x.txt").toFile(), "../evil.txt");
			out.putArchiveEntry(entry);
			out.write("pwned".getBytes());
			out.closeArchiveEntry();
		}
		Path outDir = tmp.getRoot().toPath().resolve("safe");
		assertThrows(CompressRuntimeException.class, () -> SevenZUtil.decompress(evil, outDir));
	}

	@Test
	public void isSevenZ魔数失败与null() throws Exception {
		assertFalse(SevenZUtil.isSevenZ(null));
		// 空文件 -> head.length < MAGIC.length
		Path empty = tmp.getRoot().toPath().resolve("empty");
		Files.createFile(empty);
		assertFalse(SevenZUtil.isSevenZ(empty));
		// 魔数不匹配
		Path mismatch = tmp.getRoot().toPath().resolve("mismatch");
		Files.write(mismatch, new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
		assertFalse(SevenZUtil.isSevenZ(mismatch));
		// 不可读文件 -> IOException catch
		Path locked = tmp.getRoot().toPath().resolve("locked.7z");
		Files.write(locked, new byte[] {0x37, 0x7A});
		locked.toFile().setReadable(false);
		assertThrows(CompressRuntimeException.class, () -> SevenZUtil.isSevenZ(locked));
		locked.toFile().setReadable(true);
	}

	@Test
	public void brotliDecompress文件失败与参数校验() throws Exception {
		// null 校验
		assertThrows(IllegalArgumentException.class,
				() -> BrotliUtil.decompress((Path) null, tmp.getRoot().toPath()));
		assertThrows(IllegalArgumentException.class,
				() -> BrotliUtil.decompress(tmp.getRoot().toPath(), null));
		// src 非文件
		assertThrows(IllegalArgumentException.class,
				() -> BrotliUtil.decompress(tmp.getRoot().toPath(), tmp.getRoot().toPath().resolve("o")));
		// 损坏 brotli 文件
		Path bad = tmp.getRoot().toPath().resolve("bad.br");
		Files.write(bad, new byte[] {0, 1, 2, 3, 4, 5});
		assertThrows(CompressRuntimeException.class,
				() -> BrotliUtil.decompress(bad, tmp.getRoot().toPath().resolve("out.br")));
	}

	@Test
	public void brotli字节数组参数与空输入() {
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.decompress((byte[]) null));
		assertArrayEquals(new byte[0], BrotliUtil.decompress(new byte[0]));
		// 损坏字节 -> IOException 包装
		assertThrows(CompressRuntimeException.class,
				() -> BrotliUtil.decompress(new byte[] {0, 1, 2, 3}));
	}

	@Test
	public void brotli魔数检测() {
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.isBrotli((byte[]) null));
		assertFalse(BrotliUtil.isBrotli(new byte[0]));
		assertFalse(BrotliUtil.isBrotli(new byte[] {0x1F}));
		// 合法首字节：WBITS=16 -> (16-12)<<3|3 = 0x23
		assertTrue(BrotliUtil.isBrotli(new byte[] {0x23, 0, 0}));
	}

	@Test
	public void brotli文件魔数() throws Exception {
		assertFalse(BrotliUtil.isBrotli((Path) null));
		Path notExist = tmp.getRoot().toPath().resolve("no.br");
		assertFalse(BrotliUtil.isBrotli(notExist));
		// 合法首字节文件
		Path ok = tmp.getRoot().toPath().resolve("ok.br");
		Files.write(ok, new byte[] {0x23, 0, 0});
		assertTrue(BrotliUtil.isBrotli(ok));
		// 不可读文件 -> IOException catch
		Path locked = tmp.getRoot().toPath().resolve("locked.br");
		Files.write(locked, new byte[] {0x23});
		locked.toFile().setReadable(false);
		assertThrows(CompressRuntimeException.class, () -> BrotliUtil.isBrotli(locked));
		locked.toFile().setReadable(true);
	}

	@Test
	public void 异常构造器() {
		CompressRuntimeException e1 = new CompressRuntimeException("msg");
		assertEquals("msg", e1.getMessage());
		CompressRuntimeException e2 = new CompressRuntimeException("msg2", new Throwable("cause"));
		assertTrue(e2.getMessage().contains("msg2"));
	}
}
