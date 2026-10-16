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
package com.sure.tool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.codec.HashUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;
import com.sure.tool.io.WatchDir;
import com.sure.tool.io.ZipUtil;

/**
 * null 防护与异常分支覆盖补测（CI 平台无关，确定性触发）。
 */
public class NullGuardTest {

	/** 临时目录。 */
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void searchUtilNull防护() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.upperBound(null, 5));
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.lowerBound(null, 5));
		assertThrows(IllegalArgumentException.class,
				() -> SearchUtil.upperBound(Arrays.asList(1), 5, null));
		assertThrows(IllegalArgumentException.class,
				() -> SearchUtil.lowerBound(Arrays.asList(1), 5, null));
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.interpolationSearch(null, 1));
	}

	@Test
	public void reflectUtil字段不存在() {
		assertThrows(IllegalArgumentException.class, () -> ReflectUtil.getFieldValue(null, "x"));
		assertThrows(IllegalArgumentException.class,
				() -> ReflectUtil.getFieldValue(new Object(), "notExist"));
	}

	@Test
	public void fileUtil路径规范化Null() {
		assertNull(FileUtil.normalize(null));
		assertNull(FileUtil.getCanonicalPath((java.io.File) null));
	}

	@Test
	public void dateUtil取整Null() {
		assertNull(DateUtil.round(null, Calendar.SECOND));
		assertNull(DateUtil.ceiling(null, Calendar.SECOND));
	}

	@Test
	public void hashUtil不支持的摘要算法() {
		assertThrows(IllegalArgumentException.class,
				() -> HashUtil.digest("NOSUCH-ALGO", new byte[] { 1 }));
	}

	@Test
	public void beanUtil映射Null() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("name", "n");
		assertNull(BeanUtil.mapToBean(null, String.class));
		assertNull(BeanUtil.mapToBean(map, null));
	}

	@Test
	public void watchDir参数Null() {
		Path dir = java.nio.file.Paths.get(".");
		assertThrows(IllegalArgumentException.class,
				() -> WatchDir.watch(null, (type, rel) -> {
				}));
		assertThrows(IllegalArgumentException.class, () -> WatchDir.watch(dir, null));
	}

	@Test
	public void classUtil边界分支() throws Exception {
		assertFalse(ClassUtil.isBasicType(null));
		assertEquals("", ClassUtil.getPackageName(null));
		assertEquals("", ClassUtil.getPackageName(int[].class));
		assertThrows(ClassNotFoundException.class, () -> ClassUtil.loadClass("no.such.Clazz"));
		assertNull(ClassUtil.newInstance((Class<?>) null));
		assertThrows(RuntimeException.class, () -> ClassUtil.newInstance(java.util.List.class));
	}

	@Test
	public void zipUtilNull与空包() throws Exception {
		Path dest = Files.createDirectories(tmp.getRoot().toPath().resolve("z2"));
		assertThrows(IllegalArgumentException.class,
				() -> ZipUtil.unzip(null, dest.toFile()));
		Path zip = tmp.getRoot().toPath().resolve("empty.zip");
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
			// 空 zip：无条目
		}
		ZipUtil.unzip(zip.toFile(), dest.toFile());
	}

	@Test
	public void lineIterator读完与越界() throws Exception {
		java.io.StringReader sr = new java.io.StringReader("a\nb\n");
		try (IoUtil.LineIterator it = IoUtil.lineIterator(sr)) {
			assertTrue(it.hasNext());
			assertEquals("a", it.next());
			assertEquals("b", it.next());
			assertFalse(it.hasNext());
			assertThrows(java.util.NoSuchElementException.class, it::next);
		}
	}

	@Test
	public void reflectUtil调用私有方法() throws Exception {
		class Inner {
			@SuppressWarnings("unused")
			private int twice(int x) {
				return x * 2;
			}
		}
		assertEquals(6, ReflectUtil.invoke(new Inner(), "twice", 3));
	}

	@Test
	public void beanUtil空列表拷贝() {
		assertTrue(BeanUtil.copyToList(java.util.Collections.emptyList(), String.class).isEmpty());
	}

	@Test
	public void classUtil类名实例化与边界() {
		assertThrows(IllegalArgumentException.class, () -> ClassUtil.newInstance("no.such.Clazz"));
		assertFalse(ClassUtil.isAssignable(null, String.class));
		assertEquals('\0', ClassUtil.getDefaultValue(char.class));
	}

	@Test
	public void dateUtil非法T格式() {
		assertThrows(java.time.format.DateTimeParseException.class,
				() -> DateUtil.parseLocalDateTime("2026-10-16T25:00"));
	}

	@Test
	public void strUtil环绕与截断边界() {
		assertEquals("abc", StrUtil.removeSurrounding("abc", (CharSequence) null));
		assertEquals("abc", StrUtil.removeSurrounding("abc", ""));
		assertNull(StrUtil.abbreviate(null, 10, 4));
	}

	@Test
	public void ioUtil关闭与跳过与行迭代异常() throws Exception {
		IoUtil.closeQuietly((java.io.Closeable) () -> {
			throw new java.io.IOException("boom");
		});
		java.io.InputStream tiny = new java.io.ByteArrayInputStream(new byte[] { 1, 2 });
		assertThrows(java.io.IOException.class, () -> IoUtil.skipFully(tiny, 10));
		java.io.Reader bad = new java.io.Reader() {
			@Override
			public int read(char[] cbuf, int off, int len) throws java.io.IOException {
				throw new java.io.IOException("boom");
			}

			@Override
			public void close() {
				// no-op
			}
		};
		try (IoUtil.LineIterator it = IoUtil.lineIterator(bad)) {
			assertThrows(java.util.NoSuchElementException.class, it::next);
		}
	}

	@Test
	public void reflectUtil方法不存在() {
		assertThrows(IllegalArgumentException.class, () -> ReflectUtil.invoke(new Object(), "notExist"));
	}
}
