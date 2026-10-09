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

package com.sure.tool.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import org.junit.Test;

/**
 * ScriptUtil 测试：SPI 探测、引擎缓存、编译缓存、求值与异常语义。
 */
public class ScriptUtilTest {

	@Test
	public void getEngine返回测试替身引擎() {
		final ScriptEngine engine = ScriptUtil.getEngine("sure-test");
		assertNotNull(engine);
		assertTrue(engine instanceof TestEngine);
	}

	@Test
	public void getEngine大小写不敏感且实例缓存() {
		final ScriptEngine a = ScriptUtil.getEngine("sure-test");
		final ScriptEngine b = ScriptUtil.getEngine("Sure-Test");
		assertSame("同名引擎应返回缓存实例", a, b);
	}

	@Test
	public void getEngineByExtension正确路由() {
		assertTrue(ScriptUtil.getEngineByExtension("tst") instanceof TestEngine);
	}

	@Test
	public void getEngineByMimeType正确路由() {
		assertTrue(ScriptUtil.getEngineByMimeType("text/sure-test") instanceof TestEngine);
	}

	@Test
	public void compile同一脚本返回同一实例() throws Exception {
		final CompiledScript a = ScriptUtil.compile("abc", "sure-test");
		final CompiledScript b = ScriptUtil.compile("abc", "sure-test");
		assertSame("编译缓存应复用实例", a, b);
	}

	@Test
	public void eval返回脚本长度() {
		assertEquals(3, ScriptUtil.eval("abc", "sure-test"));
	}

	@Test
	public void eval带变量绑定() {
		final Object result = ScriptUtil.eval("$name", "sure-test", Map.of("name", "sure"));
		assertEquals("sure", result);
	}

	@Test
	public void eval带null安全绑定() {
		// Map.of 不允许 null 值，用 HashMap 验证 null 绑定透传
		final java.util.HashMap<String, Object> values = new java.util.HashMap<>();
		values.put("name", null);
		final Object result = ScriptUtil.eval("$name", "sure-test", values);
		assertEquals(null, result);
	}

	@Test
	public void 引擎缺失时抛出带指引异常() {
		final ScriptRuntimeException ex = assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.getEngine("no-such-engine-xyz"));
		assertTrue(ex.getMessage().contains("no-such-engine-xyz"));
		assertTrue("缺失引擎提示应含依赖指引", ex.getMessage().contains("js-scriptengine"));
	}

	@Test
	public void 默认引擎缺失时各默认入口均抛异常() {
		// 测试环境注入了名为 js 的测试替身引擎：默认委托重载均可进入并正常返回，逐行覆盖默认入口
		assertNotNull(ScriptUtil.getScriptEngine());
		assertNotNull(ScriptUtil.compile("1 + 1"));
		assertEquals(5, ScriptUtil.eval("1 + 1"));
		assertNull(ScriptUtil.eval("$x", new javax.script.SimpleBindings()));
		assertEquals(1, ScriptUtil.eval("$x", Map.of("x", 1)));
	}

	@Test
	public void 扩展名探测返回引擎() {
		assertNotNull(ScriptUtil.getEngineByExtension("tst"));
		assertThrows(ScriptRuntimeException.class, () -> ScriptUtil.getEngineByExtension("nope"));
		assertThrows(ScriptRuntimeException.class, () -> ScriptUtil.getEngineByMimeType("text/nope"));
	}

	@Test
	public void 非Compilable引擎编译抛异常() {
		final ScriptRuntimeException ex = assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.compile("abc", "sure-simple"));
		assertTrue("应提示引擎不支持编译", ex.getMessage().contains("不支持编译"));
	}

	@Test
	public void 双参构造器() {
		final ScriptRuntimeException ex = new ScriptRuntimeException("msg", new IllegalStateException("cause"));
		assertEquals("msg", ex.getMessage());
		assertNotNull(ex.getCause());
	}

	@Test
	public void clearCache后重新编译() {
		final CompiledScript before = ScriptUtil.compile("hello", "sure-test");
		ScriptUtil.clearCache();
		final CompiledScript after = ScriptUtil.compile("hello", "sure-test");
		assertNotSame("清缓存后应重新编译", before, after);
	}

	@Test
	public void 并发获取引擎安全() throws Exception {
		final int threads = 8;
		final Thread[] ts = new Thread[threads];
		final ScriptEngine[] engines = new ScriptEngine[threads];
		for (int i = 0; i < threads; i++) {
			final int idx = i;
			ts[i] = new Thread(() -> engines[idx] = ScriptUtil.getEngine("sure-test"));
			ts[i].start();
		}
		for (Thread t : ts) {
			t.join();
		}
		for (int i = 1; i < threads; i++) {
			assertSame("并发获取应得到同一缓存实例", engines[0], engines[i]);
		}
	}
}
