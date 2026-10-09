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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.script.SimpleBindings;

import org.junit.Test;

/**
 * ScriptUtil 补测：默认引擎委托重载、编译失败与求值失败的异常分支。
 */
public class ScriptUtilExtraTest {

	/** 默认委托重载均可进入并正常返回，逐行覆盖默认入口。 */
	@Test
	public void 默认委托重载正常返回() {
		assertNotNull(ScriptUtil.getScriptEngine());
		assertNotNull(ScriptUtil.compile("1 + 1"));
		assertEquals(5, ScriptUtil.eval("1 + 1"));
		assertNull(ScriptUtil.eval("$x", new SimpleBindings()));
		assertEquals(1, ScriptUtil.eval("$x", Map.of("x", 1)));
	}

	/** 最直接的委托调用：进入方法体即命中行探针。 */
	@Test
	public void 直接调用默认委托方法() {
		assertNotNull(ScriptUtil.getScriptEngine());
		assertEquals(3, ScriptUtil.eval("1+1"));
	}

	/** 编译阶段抛 ScriptException 时包装为 ScriptRuntimeException。 */
	@Test
	public void 编译失败分支() {
		final ScriptRuntimeException ex = assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.compile(BrokenTestEngine.THROW_COMPILE, "sure-broken"));
		assertTrue(ex.getMessage().contains("编译失败"));
	}

	/** 求值阶段抛 ScriptException 时包装为 ScriptRuntimeException。 */
	@Test
	public void 求值失败分支() {
		final ScriptRuntimeException ex = assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.eval("ok", "sure-broken"));
		assertTrue(ex.getMessage().contains("执行失败"));
	}

	/** 带变量绑定求值失败时同样包装。 */
	@Test
	public void 带绑定求值失败分支() {
		final ScriptRuntimeException ex = assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.eval("ok", "sure-broken", new SimpleBindings()));
		assertTrue(ex.getMessage().contains("执行失败"));
	}

	/** 按扩展名/探测到破损引擎并触发编译失败。 */
	@Test
	public void 扩展名路由到破损引擎() {
		assertTrue(ScriptUtil.getEngineByExtension("brk") instanceof BrokenTestEngine);
		assertThrows(ScriptRuntimeException.class,
				() -> ScriptUtil.compile(BrokenTestEngine.THROW_COMPILE, "sure-broken"));
	}
}
