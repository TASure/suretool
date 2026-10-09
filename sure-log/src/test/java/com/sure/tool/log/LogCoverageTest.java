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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 日志门面补测：逐分支覆盖 {@link AbstractLog} 级别开关与占位符/异常提取、
 * {@link Slf4jLog} 反射失败回退、{@link LogFactory} 回退 {@link ConsoleLog} 路径、
 * {@link LogUtil} 全静态方法。
 *
 * <p>说明：测试 classpath 含 slf4j-api + slf4j-simple，故
 * {@link Slf4jLog#slf4jAvailable()} 的 {@code ClassNotFoundException} 分支与
 * 构造器 {@code ReflectiveOperationException} 分支在该 classpath 下物理不可达，
 * 其余分支（含反射注入失败回退）均由本测试覆盖。</p>
 */
public class LogCoverageTest {

	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream errBuffer;

	@Before
	public void capture() {
		this.originalOut = System.out;
		this.originalErr = System.err;
		this.errBuffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
		LogFactory.clearCache();
	}

	@After
	public void restore() {
		System.setOut(originalOut);
		System.setErr(originalErr);
		LogFactory.clearCache();
	}

	/** 可记录、可按级别开关的 {@link AbstractLog} 测试子类。 */
	static final class RecordingLog extends AbstractLog {

		final Set<String> disabled = new HashSet<>();
		final List<String> events = new ArrayList<>();
		Throwable lastThrowable;

		RecordingLog(String name) {
			super(name);
		}

		@Override
		protected boolean isEnabled(String level) {
			return !disabled.contains(level);
		}

		@Override
		protected void handle(String level, String message, Throwable throwable) {
			this.lastThrowable = throwable;
			events.add(level + "|" + message);
		}
	}

	@Test
	public void abstractLog全级别输出与开关() {
		RecordingLog log = new RecordingLog("rec");
		Assert.assertTrue(log.isTraceEnabled());
		Assert.assertTrue(log.isDebugEnabled());
		Assert.assertTrue(log.isInfoEnabled());
		Assert.assertTrue(log.isWarnEnabled());
		Assert.assertTrue(log.isErrorEnabled());
		Assert.assertEquals("rec", log.getName());

		log.trace("trace {}", 1);
		log.debug("debug {}", 2);
		log.info("info {}", 3);
		log.warn("warn {}", 4);
		log.error("error {}", 5);
		Assert.assertEquals(5, log.events.size());

		// 关闭 debug 级别后，调用直接返回，不进入 handle
		log.disabled.add("debug");
		log.debug("不应输出 {}", "x");
		Assert.assertFalse(log.events.contains("debug|不应输出 x"));
	}

	@Test
	public void abstractLog占位符与异常提取分支() {
		RecordingLog log = new RecordingLog("rec");
		// 模板为 null：countPlaceholders 返回 0，StrUtil.format(null,..) 返回空串
		log.info((String) null);
		Assert.assertEquals("info|", log.events.get(0));

		// 末位 Throwable 无对应占位符 -> 提取为异常，不入消息
		RuntimeException ex = new RuntimeException("root");
		log.warn("警告", ex);
		Assert.assertEquals("warn|警告", log.events.get(1));
		Assert.assertSame(ex, log.lastThrowable);

		// 末位 Throwable 有对应占位符（占位符数 >= 参数数）-> 不提取
		log.info("值={}", ex);
		Assert.assertEquals("info|值=" + ex, log.events.get(2));
		Assert.assertNull(log.lastThrowable);

		// varargs 显式传 null -> args==null 分支
		log.error("plain", (Object[]) null);
		Assert.assertEquals("error|plain", log.events.get(3));

		// 模板为 null 且末位为 Throwable、参数非空 -> 触发 countPlaceholders(null)==0 分支
		log.warn((String) null, new RuntimeException("null-tpl"));
		Assert.assertEquals("warn|", log.events.get(4));
	}

	@Test
	public void logUtil全静态方法可达() {
		LogUtil.trace("trace {}", "a");
		LogUtil.debug("debug {}", "a");
		LogUtil.info("info {}", "a");
		LogUtil.warn("warn {}", "a");
		LogUtil.error("error {}", "a");
		Log byName = LogUtil.get("custom.name");
		Assert.assertEquals("custom.name", byName.getName());
		Log byClass = LogUtil.get(LogCoverageTest.class);
		Assert.assertEquals(LogCoverageTest.class.getName(), byClass.getName());
		Log byCaller = LogUtil.get();
		Assert.assertNotNull(byCaller.getName());

		// lazy debug：slf4j-simple 默认 DEBUG 关闭，supplier 不应被求值
		LogUtil.debug(() -> {
			throw new AssertionError("DEBUG 关闭时不应求值");
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void lazyDebug开启时求值() throws Exception {
		// 反射向工厂缓存注入一个 DEBUG 恒开的日志，覆盖 LogUtil.debug(Supplier) 的求值分支
		Field cacheField = LogFactory.class.getDeclaredField("CACHE");
		cacheField.setAccessible(true);
		Map<String, Log> cache = (Map<String, Log>) cacheField.get(null);
		RecordingLog recording = new RecordingLog("lazy");
		// callerClass() skip(2) 落在 LogUtil 自身方法帧，日志名恒为 LogUtil 全限定名
		String key = LogUtil.class.getName();
		cache.put(key, recording);
		try {
			LogUtil.debug(() -> "lazy-payload");
			Assert.assertTrue(recording.events.contains("debug|lazy-payload"));
		} finally {
			cache.remove(key);
		}
	}

	@Test
	public void logFactory回退ConsoleLog与空类() throws Exception {
		// clazz 为 null -> 抛 IllegalArgumentException
		Assert.assertThrows(IllegalArgumentException.class, () -> LogFactory.get((Class<?>) null));

		// 反射将探测结果置为 false，触发 create() 的 ConsoleLog 回退分支
		Field field = LogFactory.class.getDeclaredField("slf4jAvailable");
		field.setAccessible(true);
		field.set(null, Boolean.FALSE);
		Log fallback = LogFactory.get("should.be.console");
		Assert.assertTrue(fallback instanceof ConsoleLog);

		// 再置为 true，覆盖 slf4j() 非空直接返回与 Slf4jLog 创建
		field.set(null, Boolean.TRUE);
		Log slf = LogFactory.get("should.be.slf4j");
		Assert.assertTrue(slf instanceof Slf4jLog);
	}

	@Test
	public void slf4jLog反射失败回退() throws Exception {
		Slf4jLog log = new Slf4jLog("reflect.fail");
		// 注入一个没有任何级别方法的“假” loggerClass，触发 getMethod 抛
		Field lc = Slf4jLog.class.getDeclaredField("loggerClass");
		lc.setAccessible(true);
		lc.set(log, Void.class);

		// isEnabled 反射失败 -> 返回 true
		Assert.assertTrue(log.isDebugEnabled());
		// handle 反射失败 -> 回退 System.err 输出（含异常堆栈）
		log.error("失败消息", new IllegalStateException("ref-boom"));
		String text = errBuffer.toString(StandardCharsets.UTF_8);
		Assert.assertTrue(text.contains("失败消息"));
		Assert.assertTrue(text.contains("ref-boom"));
	}

	@Test
	public void slf4jLogCapitalize空分支() throws Exception {
		Method m = Slf4jLog.class.getDeclaredMethod("capitalize", String.class);
		m.setAccessible(true);
		Assert.assertNull(m.invoke(null, (String) null));
		Assert.assertEquals("", m.invoke(null, ""));
		Assert.assertEquals("Trace", m.invoke(null, "trace"));
	}

	@Test
	public void slf4jAvailable返回真() {
		Assert.assertTrue(Slf4jLog.slf4jAvailable());
	}

	/**
	 * 构造器 catch 分支：传入 null 名称，使
	 * {@code LoggerFactory.getLogger(null)} 抛 NPE，经 {@code Method.invoke}
	 * 包装为 {@code InvocationTargetException}（属 ReflectiveOperationException），
	 * 从而命中构造器异常分支并包成 {@link IllegalStateException}。
	 */
	@Test
	public void slf4jLog构造失败分支() {
		try {
			new Slf4jLog(null);
			Assert.fail("构造应抛 IllegalStateException");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().startsWith("SLF4J 不可用"));
		}
	}

	@Test
	public void consoleLog全级别与异常() {
		ConsoleLog log = new ConsoleLog("cov.console");
		log.trace("t");
		log.debug("d");
		log.info("i");
		log.warn("w");
		log.error("e", new RuntimeException("cc"));
		Assert.assertNotNull(log.getName());
	}
}
