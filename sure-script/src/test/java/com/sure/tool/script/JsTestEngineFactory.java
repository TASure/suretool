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

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * 测试替身脚本引擎工厂（name=js）：让默认引擎可被探测到，覆盖默认委托重载。
 */
public class JsTestEngineFactory implements ScriptEngineFactory {

	@Override
	public String getEngineName() {
		return "sure-js-test";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return List.of();
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList("application/javascript");
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList("js", "JavaScript", "javascript");
	}

	@Override
	public String getLanguageName() {
		return "js-test-lang";
	}

	@Override
	public String getLanguageVersion() {
		return "1.0";
	}

	@Override
	public Object getParameter(String key) {
		return switch (key) {
			case ScriptEngine.ENGINE -> getEngineName();
			case ScriptEngine.ENGINE_VERSION -> getEngineVersion();
			case ScriptEngine.LANGUAGE -> getLanguageName();
			case ScriptEngine.LANGUAGE_VERSION -> getLanguageVersion();
			case ScriptEngine.NAME -> getNames().get(0);
			default -> null;
		};
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return obj + "." + m + "()";
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(" + toDisplay + ")";
	}

	@Override
	public String getProgram(String... statements) {
		return String.join(";", statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new JsTestEngine();
	}
}
