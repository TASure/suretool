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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.sure.tool.lang.Assert;

/**
 * 脚本工具门面（JSR-223）。
 *
 * <p>基于 JDK 标准 {@code javax.script}，提供引擎探测、编译缓存与快速求值。
 * 本工具不绑定任何具体脚本引擎：JDK15+ 已移除内置 Nashorn，
 * 调用方需自行引入对应 JSR-223 引擎依赖（如 {@code org.graalvm.js:js-scriptengine}、
 * {@code org.codehaus.groovy:groovy} 等），引擎缺失时抛出带指引的异常。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * // 引入 GraalVM JS 后：
 * Object result = ScriptUtil.eval("1 + 2 * 3");        // 7
 * ScriptUtil.eval("greet(name)", Map.of("name", "sure")); // 变量绑定
 * }</pre>
 *
 * <p>线程安全：引擎缓存与编译缓存均基于并发容器，可安全并发访问。</p>
 *
 * @since 1.3.0
 */
public final class ScriptUtil {

	/** 脚本引擎管理器（单例共享，SPI 探测一次） */
	private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
	/** 引擎缓存：key 为小写引擎名 */
	private static final Map<String, ScriptEngine> ENGINE_CACHE = new ConcurrentHashMap<>();
	/** 编译缓存：key 为脚本文本 */
	private static final Map<String, CompiledScript> COMPILED_CACHE = new ConcurrentHashMap<>();

	private ScriptUtil() {
	}

	/**
	 * 获取默认脚本引擎。
	 *
	 * <p>依次按 {@code js}、{@code JavaScript}、{@code javascript} 名称探测，
	 * 找不到时抛出异常。</p>
	 *
	 * @return 脚本引擎
	 * @throws ScriptRuntimeException 引擎缺失时抛出
	 */
	public static ScriptEngine getScriptEngine() {
		return getEngine("js");
	}

	/**
	 * 按名称获取脚本引擎（忽略大小写，实例缓存）。
	 *
	 * @param name 引擎名称，如 {@code js}、{@code groovy}、{@code python}
	 * @return 脚本引擎
	 * @throws ScriptRuntimeException 引擎缺失时抛出
	 */
	public static ScriptEngine getEngine(String name) {
		Assert.notNull(name, "引擎名称不能为 null");
		final String key = name.toLowerCase(java.util.Locale.ROOT);
		return ENGINE_CACHE.computeIfAbsent(key, k -> {
			final ScriptEngine engine = MANAGER.getEngineByName(name);
			if (engine == null) {
				throw new ScriptRuntimeException(
						"找不到脚本引擎 [" + name + "]，请添加对应 JSR-223 引擎依赖，"
								+ "如 org.graalvm.js:js-scriptengine 或 org.codehaus.groovy:groovy");
			}
			return engine;
		});
	}

	/**
	 * 按脚本文件扩展名获取脚本引擎。
	 *
	 * @param extension 扩展名（不含点号），如 {@code js}、{@code groovy}
	 * @return 脚本引擎
	 * @throws ScriptRuntimeException 引擎缺失时抛出
	 */
	public static ScriptEngine getEngineByExtension(String extension) {
		final ScriptEngine engine = MANAGER.getEngineByExtension(extension);
		if (engine == null) {
			throw new ScriptRuntimeException(
					"找不到扩展名 [" + extension + "] 对应的脚本引擎，请添加对应 JSR-223 引擎依赖");
		}
		return engine;
	}

	/**
	 * 按 MIME 类型获取脚本引擎。
	 *
	 * @param mimeType MIME 类型，如 {@code application/javascript}
	 * @return 脚本引擎
	 * @throws ScriptRuntimeException 引擎缺失时抛出
	 */
	public static ScriptEngine getEngineByMimeType(String mimeType) {
		final ScriptEngine engine = MANAGER.getEngineByMimeType(mimeType);
		if (engine == null) {
			throw new ScriptRuntimeException(
					"找不到 MIME 类型 [" + mimeType + "] 对应的脚本引擎，请添加对应 JSR-223 引擎依赖");
		}
		return engine;
	}

	/**
	 * 编译脚本并缓存（按脚本文本缓存 {@link CompiledScript}，使用默认引擎）。
	 *
	 * @param script 脚本源码
	 * @return 编译后的脚本
	 * @throws ScriptRuntimeException 编译失败或引擎缺失时抛出
	 */
	public static CompiledScript compile(String script) {
		return compile(script, "js");
	}

	/**
	 * 编译脚本并缓存（指定引擎）。
	 *
	 * @param script     脚本源码
	 * @param engineName 引擎名称
	 * @return 编译后的脚本
	 * @throws ScriptRuntimeException 编译失败或引擎缺失时抛出
	 */
	public static CompiledScript compile(String script, String engineName) {
		Assert.notNull(script, "脚本内容不能为 null");
		return COMPILED_CACHE.computeIfAbsent(script, s -> {
			final ScriptEngine engine = getEngine(engineName);
			if (!(engine instanceof Compilable compilable)) {
				throw new ScriptRuntimeException(
						"脚本引擎 [" + engine.getFactory().getEngineName() + "] 不支持编译（未实现 Compilable）");
			}
			try {
				return compilable.compile(s);
			} catch (ScriptException e) {
				throw new ScriptRuntimeException("脚本编译失败: " + e.getMessage(), e);
			}
		});
	}

	/**
	 * 快速求值脚本（使用默认引擎）。
	 *
	 * @param script 脚本源码
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script) {
		return eval(script, "js");
	}

	/**
	 * 快速求值脚本（指定引擎）。
	 *
	 * @param script     脚本源码
	 * @param engineName 引擎名称
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script, String engineName) {
		try {
			return compile(script, engineName).eval();
		} catch (ScriptException e) {
			throw new ScriptRuntimeException("脚本执行失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 快速求值脚本（变量绑定）。
	 *
	 * @param script   脚本源码
	 * @param bindings 变量绑定
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script, Bindings bindings) {
		return eval(script, "js", bindings);
	}

	/**
	 * 快速求值脚本（指定引擎 + 变量绑定）。
	 *
	 * @param script     脚本源码
	 * @param engineName 引擎名称
	 * @param bindings   变量绑定
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script, String engineName, Bindings bindings) {
		try {
			return compile(script, engineName).eval(bindings);
		} catch (ScriptException e) {
			throw new ScriptRuntimeException("脚本执行失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 快速求值脚本（Map 变量）。
	 *
	 * @param script 脚本源码
	 * @param values 变量映射（转 {@link SimpleBindings}）
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script, Map<String, Object> values) {
		return eval(script, "js", values);
	}

	/**
	 * 快速求值脚本（指定引擎 + Map 变量）。
	 *
	 * @param script     脚本源码
	 * @param engineName 引擎名称
	 * @param values     变量映射（转 {@link SimpleBindings}）
	 * @return 执行结果
	 * @throws ScriptRuntimeException 执行失败或引擎缺失时抛出
	 */
	public static Object eval(String script, String engineName, Map<String, Object> values) {
		final SimpleBindings bindings = new SimpleBindings();
		bindings.putAll(values);
		return eval(script, engineName, bindings);
	}

	/**
	 * 清空引擎缓存与编译缓存。
	 */
	public static void clearCache() {
		ENGINE_CACHE.clear();
		COMPILED_CACHE.clear();
	}
}
