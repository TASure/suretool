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
package com.sure.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sure.tool.config.Props;
import com.sure.tool.util.ExpressionUtil;

/**
 * 批14：Props / ExpressionUtil 测试。
 */
public class Batch14CoreTest {

	// ---------- Props ----------

	@Test
	public void testPropsLoadAndTypedGet() {
		Props props = new Props("batch14-app.properties");
		assertEquals("8080", props.getStr("server.port"));
		assertEquals(8080, props.getInt("server.port", -1));
		assertEquals(30L, props.getLong("server.timeout", -1L));
		assertEquals(0.85, props.getDouble("rate", -1), 1e-9);
		assertTrue(props.getBool("enabled", false));
		assertEquals("SureTool", props.getStr("name", "def"));
		assertEquals(0, new BigDecimal("150").compareTo(props.getBigDecimal("ratio", BigDecimal.ZERO)));
		// 缺失/解析失败回退默认值
		assertEquals(-1, props.getInt("no.such.key", -1));
		assertEquals(-1, props.getInt("name", -1));
		assertEquals("def", props.getStr("no.such.key", "def"));
		assertFalse(props.getBool("no.such.key", false));
	}

	@Test
	public void testPropsToMapAndToBean() {
		Props props = new Props("batch14-app.properties");
		Map<String, String> map = props.toMap();
		assertEquals("8080", map.get("server.port"));
		assertEquals(8, map.size());
		// toBean：属性名与键直匹配（server.port 等带点键不参与），类型自动转换
		ServerConfig bean = props.toBean(ServerConfig.class);
		assertNotNull(bean);
		assertEquals(8080, bean.port);
		assertEquals(30, bean.timeout);
		assertTrue(bean.enabled);
		assertEquals("SureTool", bean.name);
	}

	@Test
	public void testPropsLoadErrors() {
		try {
			new Props("no/such/resource.properties");
			fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	/** toBean 目标类。 */
	public static class ServerConfig {
		public int port;
		public long timeout;
		public boolean enabled;
		public String name;
	}

	// ---------- ExpressionUtil ----------

	@Test
	public void testEvalBasic() {
		assertEquals(6.0, ExpressionUtil.eval("1 + 2 + 3"), 1e-12);
		assertEquals(9.0, ExpressionUtil.eval("(1 + 2) * 3"), 1e-12);
		assertEquals(4.0, ExpressionUtil.eval("10 - 2 * 3"), 1e-12);
		assertEquals(2.5, ExpressionUtil.eval("5 / 2"), 1e-12);
		assertEquals(1.0, ExpressionUtil.eval("7 % 3"), 1e-12);
		assertEquals(-3.0, ExpressionUtil.eval("-1 - 2"), 1e-12);
		assertEquals(3.0, ExpressionUtil.eval("--3"), 1e-12);
		assertEquals(1.5, ExpressionUtil.eval("3/2"), 1e-12);
	}

	@Test
	public void testEvalNumbersAndWhitespace() {
		assertEquals(1.5, ExpressionUtil.eval(".5 + 1"), 1e-12);
		assertEquals(150.0, ExpressionUtil.eval("1.5e2"), 1e-9);
		assertEquals(7.0, ExpressionUtil.eval("  1  +   2  * 3 "), 1e-12);
	}

	@Test
	public void testEvalVariables() {
		Map<String, Object> vars = new HashMap<>();
		vars.put("x", 3);
		vars.put("y", 4);
		vars.put("price", new BigDecimal("19.9"));
		assertEquals(14.0, ExpressionUtil.eval("x * y + 2", vars), 1e-12);
		assertEquals(19.9, ExpressionUtil.eval("price * 1", vars), 1e-12);
		assertEquals(7.0, ExpressionUtil.eval("_v + 7", Map.of("_v", 0)), 1e-12);
	}

	@Test
	public void testEvalNumberPrecision() {
		BigDecimal result = ExpressionUtil.evalNumber("1 / 3", null);
		assertEquals(new BigDecimal("0.3333333333333333"), result);
	}

	@Test
	public void testEvalErrors() {
		expectError("1 +");
		expectError("(1 + 2");
		expectError("1 / 0");
		expectError("1 % 0");
		expectError("abc"); // 变量缺失
		expectError("");
		expectError("1 + 2 )");
		expectError(null);
	}

	@Test
	public void testCheck() {
		assertTrue(ExpressionUtil.check("1 + 2 * 3"));
		assertTrue(ExpressionUtil.check("  -5.5e2 "));
		assertFalse(ExpressionUtil.check("1 +"));
		assertFalse(ExpressionUtil.check("1 / 0"));
		assertFalse(ExpressionUtil.check(null));
	}

	private static void expectError(String expression) {
		try {
			ExpressionUtil.eval(expression);
			fail("应当抛异常: " + expression);
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}
}
