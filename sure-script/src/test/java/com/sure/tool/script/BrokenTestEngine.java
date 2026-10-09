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

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * 测试替身脚本引擎：用于覆盖 {@code ScriptUtil} 中的异常分支。
 *
 * <p>约定：脚本文本为 {@value #THROW_COMPILE} 时，编译阶段直接抛 {@link ScriptException}，
 * 覆盖编译失败分支；其余脚本文本编译成功但求值阶段抛 {@link ScriptException}，
 * 覆盖求值失败分支。</p>
 */
public class BrokenTestEngine extends AbstractScriptEngine implements Compilable {

	/** 触发编译失败的标记脚本。 */
	public static final String THROW_COMPILE = "__throw_compile__";

	@Override
	public Object eval(String script, ScriptContext context) {
		return script == null ? 0 : script.length();
	}

	@Override
	public Object eval(java.io.Reader reader, ScriptContext context) throws ScriptException {
		final StringBuilder sb = new StringBuilder();
		try (var r = reader) {
			int ch;
			while ((ch = r.read()) >= 0) {
				sb.append((char) ch);
			}
		} catch (java.io.IOException e) {
			throw new ScriptException(e);
		}
		return eval(sb.toString(), context);
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return new BrokenTestEngineFactory();
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException {
		if (THROW_COMPILE.equals(script)) {
			throw new ScriptException("故意的编译失败");
		}
		return new CompiledScript() {
			@Override
			public Object eval(ScriptContext context) throws ScriptException {
				throw new ScriptException("故意的求值失败");
			}

			@Override
			public ScriptEngine getEngine() {
				return BrokenTestEngine.this;
			}
		};
	}

	@Override
	public CompiledScript compile(java.io.Reader reader) throws ScriptException {
		final StringBuilder sb = new StringBuilder();
		try (var r = reader) {
			int ch;
			while ((ch = r.read()) >= 0) {
				sb.append((char) ch);
			}
		} catch (java.io.IOException e) {
			throw new ScriptException(e);
		}
		return compile(sb.toString());
	}
}
