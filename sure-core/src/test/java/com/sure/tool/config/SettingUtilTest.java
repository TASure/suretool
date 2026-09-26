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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link SettingUtil} 分层配置读取测试。
 */
public class SettingUtilTest {

	private Path file;

	@Before
	public void setUp() throws Exception {
		SettingUtil.clear();
		this.file = Files.createTempFile("sure-setting-", ".setting");
		Files.writeString(file, """
				# 注释行
				server.port=8080
				app.name=suretool

				home=file_value
				st.file.key=file_v
				flag=true
				rate=3.14
				""", StandardCharsets.UTF_8);
	}

	@After
	public void tearDown() throws Exception {
		SettingUtil.clear();
		System.clearProperty("st.sys.key");
		System.clearProperty("server.port");
		Files.deleteIfExists(file);
	}

	@Test
	public void 文件解析与注释空行() {
		SettingUtil.load(file.toFile());
		Assert.assertEquals("suretool", SettingUtil.getString("app.name"));
		Assert.assertEquals("8080", SettingUtil.getString("server.port"));
		Assert.assertNull("注释与空行不应产生键", SettingUtil.getString("# 注释行"));
	}

	@Test
	public void 系统属性优先于文件() {
		SettingUtil.load(file.toFile());
		System.setProperty("server.port", "9999");
		Assert.assertEquals("9999", SettingUtil.getString("server.port"));
		Assert.assertEquals(9999, SettingUtil.getInt("server.port", 0));
	}

	@Test
	public void 环境变量优先于文件() {
		SettingUtil.load(file.toFile());
		// key=home 映射环境变量 HOME（真实存在且非空）
		String home = System.getenv("HOME");
		Assert.assertNotNull("测试前提：HOME 环境变量存在", home);
		Assert.assertEquals(home, SettingUtil.getString("home"));
		Assert.assertNotEquals("file_value", SettingUtil.getString("home"));
	}

	@Test
	public void 文件值优先于默认值() {
		SettingUtil.load(file.toFile());
		Assert.assertEquals("file_v", SettingUtil.getString("st.file.key", "default_v"));
		Assert.assertEquals("default_v", SettingUtil.getString("no.such.key", "default_v"));
		Assert.assertNull(SettingUtil.getString("no.such.key"));
	}

	@Test
	public void 类型转换() {
		SettingUtil.load(file.toFile());
		Assert.assertEquals(8080, SettingUtil.getInt("server.port", 0));
		Assert.assertEquals(3.14d, SettingUtil.getDouble("rate", 0d), 0.0001);
		Assert.assertTrue(SettingUtil.getBoolean("flag", false));
		Assert.assertEquals(42L, SettingUtil.getLong("no.such", 42L));
	}

	@Test
	public void 非法值抛出清晰异常() {
		SettingUtil.load(file.toFile());
		Assert.assertEquals("suretool", SettingUtil.getString("app.name"));
		try {
			SettingUtil.getInt("app.name", 0);
			Assert.fail("应抛 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("app.name"));
		}
		try {
			SettingUtil.getBoolean("server.port", false);
			Assert.fail("应抛 IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("server.port"));
		}
	}

	@Test
	public void 重复加载覆盖与keys() throws Exception {
		SettingUtil.load(file.toFile());
		Assert.assertEquals("suretool", SettingUtil.getString("app.name"));
		Path other = Files.createTempFile("sure-setting-2-", ".setting");
		Files.writeString(other, "app.name=other\nextra.key=1\n", StandardCharsets.UTF_8);
		try {
			SettingUtil.load(other.toFile());
			Assert.assertEquals("other", SettingUtil.getString("app.name"));
			Assert.assertEquals("1", SettingUtil.getString("extra.key"));
			Assert.assertNull("旧键应被整体替换", SettingUtil.getString("server.port"));
			Assert.assertTrue(SettingUtil.keys().contains("app.name"));
			Assert.assertTrue(SettingUtil.keys().contains("extra.key"));
			Assert.assertFalse(SettingUtil.keys().contains("server.port"));
		} finally {
			Files.deleteIfExists(other);
		}
	}

	@Test
	public void contains与clear() {
		SettingUtil.load(file.toFile());
		Assert.assertTrue(SettingUtil.contains("app.name"));
		SettingUtil.clear();
		Assert.assertFalse(SettingUtil.contains("app.name"));
		Assert.assertTrue(SettingUtil.keys().isEmpty());
	}

	@Test
	public void 并发读写不抛错() throws Exception {
		SettingUtil.load(file.toFile());
		int threads = 10;
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		AtomicReference<Throwable> error = new AtomicReference<>();
		for (int i = 0; i < threads; i++) {
			Thread.startVirtualThread(() -> {
				try {
					start.await();
					for (int j = 0; j < 100; j++) {
						SettingUtil.getString("app.name");
						SettingUtil.getInt("server.port", 0);
						SettingUtil.keys();
					}
				} catch (Throwable e) {
					error.set(e);
				} finally {
					done.countDown();
				}
			});
		}
		start.countDown();
		done.await();
		Assert.assertNull("并发读写不应抛错：" + error.get(), error.get());
	}

	@Test
	public void 空键抛异常() {
		try {
			SettingUtil.getString(null);
			Assert.fail("应抛 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 符合预期
		}
	}
}
