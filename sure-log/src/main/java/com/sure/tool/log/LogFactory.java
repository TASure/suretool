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
package com.sure.tool.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sure.tool.util.StrUtil;

/**
 * 日志工厂：按名称获取 {@link Log} 实例，classpath 存在 SLF4J 时自动委托，
 * 否则回退 {@link ConsoleLog}。
 *
 * <p>实例按名称缓存，同一名称返回同一实例（线程安全）。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public final class LogFactory {

	private static final Map<String, Log> CACHE = new ConcurrentHashMap<>();

	/**
	 * SLF4J 探测结果缓存（惰性探测一次）。
	 */
	private static volatile Boolean slf4jAvailable;

	private LogFactory() {
	}

	/**
	 * 获取日志实例（名称=类全名）。
	 *
	 * @param clazz 类
	 * @return 日志实例
	 */
	public static Log get(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz 不能为 null");
		}
		return get(clazz.getName());
	}

	/**
	 * 获取日志实例。
	 *
	 * @param name 日志名称
	 * @return 日志实例
	 */
	public static Log get(String name) {
		if (StrUtil.isBlank(name)) {
			throw new IllegalArgumentException("name 不能为空");
		}
		return CACHE.computeIfAbsent(name, LogFactory::create);
	}

	/**
	 * 创建日志实例（探测 SLF4J 优先，结果缓存）。
	 *
	 * @param name 日志名称
	 * @return 日志实例
	 */
	private static Log create(String name) {
		if (slf4j()) {
			return new Slf4jLog(name);
		}
		return new ConsoleLog(name);
	}

	/**
	 * SLF4J 探测（双检锁缓存结果）。
	 *
	 * @return classpath 存在 SLF4J 返回 {@code true}
	 */
	private static boolean slf4j() {
		Boolean available = slf4jAvailable;
		if (available == null) {
			synchronized (LogFactory.class) {
				available = slf4jAvailable;
				if (available == null) {
					available = Slf4jLog.slf4jAvailable();
					slf4jAvailable = available;
				}
			}
		}
		return available;
	}

	/**
	 * 清理全部缓存实例与探测结果（主要用于测试）。
	 */
	static void clearCache() {
		CACHE.clear();
		slf4jAvailable = null;
	}
}
