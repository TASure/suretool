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
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * 测试替身：不实现 {@code Compilable} 的引擎，用于验证编译失败分支。
 */
public class TestSimpleEngine extends AbstractScriptEngine {

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
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
		return new TestSimpleEngineFactory();
	}
}
