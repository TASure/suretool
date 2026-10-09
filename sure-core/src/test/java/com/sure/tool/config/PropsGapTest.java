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
package com.sure.tool.config;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Props 覆盖率补测：文件加载、类型解析失败默认值、null 守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class PropsGapTest {

	/** 供 toBean 使用的 Bean。 */
	public static class Sample {
		private String name;
		private int port;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}

	private Path tempDir;
	private Path propsFile;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("props-gap");
		propsFile = tempDir.resolve("app.properties");
		String content = "name=hello\nport=8080\nratio=1.5\nflag=true\nbig=10.5\n"
				+ "badInt=abc\nbadLong=xyz\nbadDouble=qq\nbadBigDecimal=!!\n";
		Files.write(propsFile, content.getBytes(StandardCharsets.UTF_8));
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(propsFile);
		Files.deleteIfExists(tempDir);
	}

	@Test
	public void testLoadFromFile() {
		Props p = new Props(propsFile.toFile());
		Assert.assertEquals("hello", p.getStr("name"));
		Assert.assertEquals(8080, p.getInt("port", 0));
		Assert.assertEquals(1.5d, p.getDouble("ratio", 0d), 0.0001d);
		Assert.assertTrue(p.getBool("flag", false));
		Assert.assertEquals(new BigDecimal("10.5"), p.getBigDecimal("big", BigDecimal.ZERO));
	}

	@Test
	public void testLoadErrors() {
		try {
			new Props((String) null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			new Props((java.io.File) null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			new Props(tempDir.resolve("no-such.properties").toFile());
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testGetDefaultsAndParseFail() {
		Props p = new Props(propsFile.toFile());
		Assert.assertEquals("def", p.getStr("missing", "def"));
		Assert.assertEquals(5, p.getInt("missing", 5));
		Assert.assertEquals(5, p.getInt("badInt", 5));
		Assert.assertEquals(9L, p.getLong("missing", 9L));
		Assert.assertEquals(9L, p.getLong("badLong", 9L));
		Assert.assertEquals(2.5d, p.getDouble("missing", 2.5d), 0.0001d);
		Assert.assertEquals(2.5d, p.getDouble("badDouble", 2.5d), 0.0001d);
		Assert.assertEquals(BigDecimal.ONE, p.getBigDecimal("missing", BigDecimal.ONE));
		Assert.assertEquals(BigDecimal.ONE, p.getBigDecimal("badBigDecimal", BigDecimal.ONE));
		Assert.assertNull(p.getStr("missing"));
		Assert.assertNull(p.getObj("missing"));
	}

	@Test
	public void testToBean() {
		Props p = new Props(propsFile.toFile());
		Assert.assertNull(p.toBean(null));
		Sample s = p.toBean(Sample.class);
		Assert.assertEquals("hello", s.getName());
		Assert.assertEquals(8080, s.getPort());
		Assert.assertTrue(p.toMap().containsKey("name"));
	}

	@Test
	public void testIoExceptionBranches() throws Exception {
		Props p = new Props();
		java.io.InputStream bad = new java.io.FilterInputStream(new java.io.ByteArrayInputStream(new byte[0])) {
			@Override
			public int read() throws java.io.IOException {
				throw new java.io.IOException("boom");
			}

			@Override
			public int read(byte[] b, int off, int len) throws java.io.IOException {
				throw new java.io.IOException("boom");
			}
		};
		try {
			p.load(bad);
			Assert.fail("应抛异常");
		} catch (java.io.IOException e) {
			// expected
		}
		java.io.File dir = new java.io.File("/tmp");
		try {
			p.load(dir);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
