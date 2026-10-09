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

import javax.script.ScriptEngineFactory;

/**
 * 测试替身脚本引擎：以默认名 {@code js} 注册，行为与 {@link TestEngine} 一致
 * （求值返回脚本长度、支持编译），使 {@code ScriptUtil} 的默认委托重载可正常返回并被覆盖。
 */
public class JsTestEngine extends TestEngine {

	@Override
	public ScriptEngineFactory getFactory() {
		return new JsTestEngineFactory();
	}
}
