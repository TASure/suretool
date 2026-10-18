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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 批 44：StrSubstitutor 模板变量深度测试（自定义定界符 / 默认值 / 转义 / 缺失保留）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class StrSubstitutorBatch44Test {

	// ---------------- 基本替换 ----------------

	@Test
	public void testBasicSubstitute() {
		Assert.assertEquals("hello world",
				StrSubstitutor.substitute("hello ${name}", Map.of("name", "world")));
		Assert.assertEquals("订单 10086 已支付",
				StrSubstitutor.substitute("订单 ${id} 已支付", Map.of("id", 10086)));
	}

	@Test
	public void testMultipleVars() {
		Assert.assertEquals("a=1, b=2, c=3",
				StrSubstitutor.substitute("a=${a}, b=${b}, c=${c}",
						Map.of("a", 1, "b", 2, "c", 3)));
	}

	@Test
	public void testAdjacentPlaceholders() {
		Assert.assertEquals("12",
				StrSubstitutor.substitute("${a}${b}", Map.of("a", 1, "b", 2)));
		Assert.assertEquals("1122",
				StrSubstitutor.substitute("${a}${a}${b}${b}", Map.of("a", 1, "b", 2)));
	}

	@Test
	public void testPlaceholderAtEdges() {
		Assert.assertEquals("head", StrSubstitutor.substitute("${head}", Map.of("head", "head")));
		Assert.assertEquals("tail", StrSubstitutor.substitute("${tail}", Map.of("tail", "tail")));
		Assert.assertEquals("", StrSubstitutor.substitute("${x}", Map.of("x", "")));
	}

	@Test
	public void testNoPlaceholder() {
		Assert.assertEquals("plain text",
				StrSubstitutor.substitute("plain text", Map.of("a", 1)));
		Assert.assertEquals("", StrSubstitutor.substitute("", Map.of("a", 1)));
	}

	// ---------------- 缺失与空值 ----------------

	@Test
	public void testMissingVarKeepsPlaceholder() {
		Assert.assertEquals("${missing}",
				StrSubstitutor.substitute("${missing}", Map.of("a", 1)));
		Assert.assertEquals("x${nope}y",
				StrSubstitutor.substitute("x${nope}y", Map.of("a", 1)));
	}

	@Test
	public void testNullValueRendersEmpty() {
		final Map<String, Object> vars = new HashMap<>();
		vars.put("k", null);
		Assert.assertEquals("", StrSubstitutor.substitute("${k}", vars));
		Assert.assertEquals("a  b", StrSubstitutor.substitute("a ${k} b", vars));
	}

	@Test
	public void testEmptyKeyRendersEmpty() {
		Assert.assertEquals("", StrSubstitutor.substitute("${}", Map.of("a", 1)));
		Assert.assertEquals("x  y", StrSubstitutor.substitute("x ${} y", Map.of("a", 1)));
	}

	@Test
	public void testUnclosedPlaceholderKept() {
		Assert.assertEquals("${unclosed", StrSubstitutor.substitute("${unclosed", Map.of("a", 1)));
		Assert.assertEquals("a ${b", StrSubstitutor.substitute("a ${b", Map.of("b", 2)));
	}

	// ---------------- 默认值语法 ----------------

	@Test
	public void testDefaultValue() {
		Assert.assertEquals("prod", StrSubstitutor.substitute("${env:-prod}", Collections.emptyMap()));
		Assert.assertEquals("N/A", StrSubstitutor.substitute("${missing:-N/A}", Map.of("a", 1)));
	}

	@Test
	public void testDefaultValueEmpty() {
		Assert.assertEquals("", StrSubstitutor.substitute("${missing:-}", Map.of("a", 1)));
	}

	@Test
	public void testDefaultIgnoredWhenVarExists() {
		Assert.assertEquals("dev", StrSubstitutor.substitute("${env:-prod}", Map.of("env", "dev")));
		Assert.assertEquals("", StrSubstitutor.substitute("${env:-prod}", Map.of("env", "")));
	}

	@Test
	public void testNullValueUsesDefault() {
		final Map<String, Object> vars = new HashMap<>();
		vars.put("env", null);
		Assert.assertEquals("prod", StrSubstitutor.substitute("${env:-prod}", vars));
	}

	@Test
	public void testDefaultWithColons() {
		Assert.assertEquals("a:b", StrSubstitutor.substitute("${x:-a:b}", Collections.emptyMap()));
		Assert.assertEquals("v", StrSubstitutor.substitute("${x:-a:b}", Map.of("x", "v")));
	}

	@Test
	public void testDefaultWithEmptyName() {
		Assert.assertEquals("d", StrSubstitutor.substitute("${:-d}", Map.of("a", 1)));
	}

	@Test
	public void testNoDefaultDelimiterPreserved() {
		// 变量存在且值含 :-，不做默认值解析
		Assert.assertEquals("1-2", StrSubstitutor.substitute("${x}", Map.of("x", "1-2")));
	}

	// ---------------- 转义 ----------------

	@Test
	public void testEscapeDefault() {
		Assert.assertEquals("${name}", StrSubstitutor.substitute("$${name}", Map.of("name", "x")));
		Assert.assertEquals("a ${b} c", StrSubstitutor.substitute("a $${b} c", Map.of("b", "B")));
	}

	@Test
	public void testEscapeThenSubstitute() {
		Assert.assertEquals("${a} 1", StrSubstitutor.substitute("$${a} ${a}", Map.of("a", 1)));
	}

	@Test
	public void testCustomEscapeChar() {
		Assert.assertEquals("${name}", new StrSubstitutor("${", "}", '#')
				.replace("#${name}", Map.of("name", "x")));
		// 自定义转义符后，默认 $ 不再转义：$${name} = $ + 占位符
		Assert.assertEquals("$x", new StrSubstitutor("${", "}", '#')
				.replace("$${name}", Map.of("name", "x")));
	}

	@Test
	public void testEscapeDisabled() {
		final StrSubstitutor sub = new StrSubstitutor("${", "}", (char) 0);
		Assert.assertEquals("$x", sub.replace("$${name}", Map.of("name", "x")));
	}

	@Test
	public void testMultipleEscapes() {
		Assert.assertEquals("${a}${b}", StrSubstitutor.substitute("$${a}$${b}", Map.of("a", 1, "b", 2)));
	}

	// ---------------- 自定义定界符 ----------------

	@Test
	public void testCustomDelimiter() {
		Assert.assertEquals("sure 你好",
				StrSubstitutor.replace("<%name%> 你好", Map.of("name", "sure"), "<%", "%>"));
		Assert.assertEquals("sure 你好",
				StrSubstitutor.replace("[[name]] 你好", Map.of("name", "sure"), "[[", "]]"));
	}

	@Test
	public void testCustomDelimiterRegexChars() {
		Assert.assertEquals("v", StrSubstitutor.replace("$name$", Map.of("name", "v"), "$", "$"));
		Assert.assertEquals("v", StrSubstitutor.replace("(name)", Map.of("name", "v"), "(", ")"));
	}

	@Test
	public void testCustomDelimiterWithEscape() {
		final StrSubstitutor sub = new StrSubstitutor("<%", "%>");
		Assert.assertEquals("<%name%>", sub.replace("$<%name%>", Map.of("name", "v")));
	}

	@Test
	public void testPrefixEqualsSuffix() {
		Assert.assertEquals("v", StrSubstitutor.replace("$name$", Map.of("name", "v"), "$", "$"));
		Assert.assertEquals("$name$", StrSubstitutor.replace("$name$", Map.of("x", "v"), "$", "$"));
	}

	@Test
	public void testCustomDelimiterDefaultValue() {
		Assert.assertEquals("d", StrSubstitutor.replace("<%k:-d%>", Map.of("a", 1), "<%", "%>"));
	}

	@Test
	public void testCustomDelimiterMissingKept() {
		Assert.assertEquals("<%miss%>", StrSubstitutor.replace("<%miss%>", Map.of("a", 1), "<%", "%>"));
	}

	@Test
	public void testDelimiterConstructorInvalid() {
		try {
			new StrSubstitutor(null, "}");
			Assert.fail("应抛 IAE");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			new StrSubstitutor("", "}");
			Assert.fail("应抛 IAE");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			new StrSubstitutor("${", null);
			Assert.fail("应抛 IAE");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			new StrSubstitutor("${", "");
			Assert.fail("应抛 IAE");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	// ---------------- null / 边界 ----------------

	@Test
	public void testNullTemplate() {
		Assert.assertNull(StrSubstitutor.substitute(null, Map.of("a", 1)));
		Assert.assertNull(StrSubstitutor.replace(null, Map.of("a", 1), "<%", "%>"));
		Assert.assertNull(new StrSubstitutor().replace(null, Map.of("a", 1)));
	}

	@Test
	public void testNullVars() {
		Assert.assertEquals("hello ${name}", StrSubstitutor.substitute("hello ${name}", null));
		Assert.assertEquals("hello ${name}",
				new StrSubstitutor().replace("hello ${name}", (Map<String, ?>) null));
	}

	@Test
	public void testVarValueConversion() {
		Assert.assertEquals("3", StrSubstitutor.substitute("${n}", Map.of("n", 3L)));
		Assert.assertEquals("true", StrSubstitutor.substitute("${b}", Map.of("b", true)));
	}

	@Test
	public void testNestedNotResolved() {
		// 嵌套变量不解析（对齐 Commons 默认），key 含未闭合 { 视为缺失保留
		Assert.assertEquals("${a${b}}", StrSubstitutor.substitute("${a${b}}", Map.of("a", "1", "b", "2")));
	}

	@Test
	public void testLongTextPerformance() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			sb.append("${k").append(i % 3).append("}-");
		}
		final String result = StrSubstitutor.substitute(sb.toString(),
				Map.of("k0", "0", "k1", "1", "k2", "2"));
		Assert.assertEquals(200, result.length());
		Assert.assertTrue(result.startsWith("0-1-2-0-1-2-"));
	}

	// ---------------- StrUtil 委托 ----------------

	@Test
	public void testStrUtilSubstitute() {
		Assert.assertEquals("hi sure, env=dev",
				StrUtil.substitute("hi ${name}, env=${env:-dev}", Map.of("name", "sure")));
		Assert.assertNull(StrUtil.substitute(null, Map.of("a", 1)));
	}

	@Test
	public void testStrUtilSubstituteCustomDelimiter() {
		Assert.assertEquals("sure 你好",
				StrUtil.substitute("<%name%> 你好", Map.of("name", "sure"), "<%", "%>"));
		Assert.assertEquals("v", StrUtil.substitute("$name$", Map.of("name", "v"), "$", "$"));
	}

	// ---------------- 构造器默认值 ----------------

	@Test
	public void testDefaultConstructor() {
		Assert.assertEquals("sure", new StrSubstitutor().replace("${name}", Map.of("name", "sure")));
	}

	@Test
	public void testTwoArgConstructor() {
		Assert.assertEquals("v", new StrSubstitutor("[[", "]]").replace("[[k]]", Map.of("k", "v")));
	}
}
