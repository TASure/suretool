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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SettingUtil 覆盖率补测：文件加载、无参 getter、布尔解析、坏 URL 异常路径。
 *
 * @author suretool
 * @since 1.13.1
 */
public class SettingUtilGapTest {

	private Path tempDir;
	private Path file;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("setting-gap");
		file = tempDir.resolve("app.setting");
		String content = "my.gap.int=42\nmy.gap.long=100\nmy.gap.double=3.5\n"
				+ "my.gap.bool=true\nmy.gap.bool2=no\nmy.gap.badbool=maybe\n"
				+ "\n# comment line\nnoequals\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(file);
		Files.deleteIfExists(tempDir);
		SettingUtil.clear();
	}

	@Test
	public void testLoadAndGetters() {
		SettingUtil.load(file.toString());
		Assert.assertEquals(Integer.valueOf(42), SettingUtil.getInt("my.gap.int"));
		Assert.assertEquals(42, SettingUtil.getInt("my.gap.int", 0));
		Assert.assertEquals(Long.valueOf(100), SettingUtil.getLong("my.gap.long"));
		Assert.assertEquals(100L, SettingUtil.getLong("my.gap.long", 0L));
		Assert.assertEquals(Double.valueOf(3.5d), SettingUtil.getDouble("my.gap.double"));
		Assert.assertEquals(3.5d, SettingUtil.getDouble("my.gap.double", 0d), 0.0001d);
		Assert.assertEquals(0d, SettingUtil.getDouble("my.gap.missing", 0d), 0.0001d);
		Assert.assertEquals(Boolean.TRUE, SettingUtil.getBoolean("my.gap.bool"));
		Assert.assertFalse(SettingUtil.getBoolean("my.gap.bool2", true));
		Assert.assertEquals("42", SettingUtil.get("my.gap.int"));
		Assert.assertEquals("42", SettingUtil.getString("my.gap.int"));
		Assert.assertEquals("def", SettingUtil.getString("my.gap.missing", "def"));
		Assert.assertTrue(SettingUtil.contains("my.gap.int"));
		Assert.assertFalse(SettingUtil.contains("my.gap.missing.xyz"));
	}

	@Test
	public void testBadBoolean() {
		SettingUtil.load(file.toString());
		try {
			SettingUtil.getBoolean("my.gap.badbool");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testBadUrl() throws Exception {
		try {
			SettingUtil.load(tempDir.resolve("no-such.setting").toUri().toURL());
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			// expected
		}
	}
}
