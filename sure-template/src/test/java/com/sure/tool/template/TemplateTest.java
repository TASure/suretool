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

package com.sure.tool.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Map;

import org.junit.Test;

/**
 * SimpleTemplate / TemplateUtil 测试：占位符、转义、空值、点号路径、Bean 渲染与边界。
 */
public class TemplateTest {

	@Test
	public void 基础占位符替换() {
		assertEquals("你好，sure！", TemplateUtil.render("你好，${name}！", Map.of("name", "sure")));
	}

	@Test
	public void 重复占位符全部替换() {
		final String out = TemplateUtil.render("${a}-${b}-${a}", Map.of("a", 1, "b", 2));
		assertEquals("1-2-1", out);
	}

	@Test
	public void 转义占位符输出字面() {
		final String out = TemplateUtil.render("\\${name} 与 ${name}", Map.of("name", "sure"));
		assertEquals("${name} 与 sure", out);
	}

	@Test
	public void 未知变量保留占位符() {
		final String out = TemplateUtil.render("${unknown}", Map.of());
		assertEquals("${unknown}", out);
	}

	@Test
	public void null值渲染为空串() {
		// Map.of 不允许 null 值，用 HashMap 验证 null 渲染为空串
		final java.util.HashMap<String, Object> values = new java.util.HashMap<>();
		values.put("x", null);
		final String out = TemplateUtil.render("a${x}b", values);
		assertEquals("ab", out);
	}

	@Test
	public void 点号路径Map嵌套() {
		final String out = TemplateUtil.render("${user.name}:${user.age}",
				Map.of("user", Map.of("name", "sure", "age", 18)));
		assertEquals("sure:18", out);
	}

	@Test
	public void 点号路径中间缺失保留原样() {
		final String out = TemplateUtil.render("${user.name}", Map.of("user", Map.of()));
		assertEquals("${user.name}", out);
	}

	@Test
	public void Bean渲染() {
		final User user = new User("sure", 18);
		final String out = TemplateUtil.render("${name}-${age}", user);
		assertEquals("sure-18", out);
	}

	@Test
	public void Bean嵌套渲染() {
		final User user = new User("sure", 18);
		final String out = TemplateUtil.render("${user.name}:${user.age}", Map.of("user", user));
		assertEquals("sure:18", out);
	}

	@Test
	public void Bean属性null渲染空串() {
		final User user = new User(null, 18);
		final String out = TemplateUtil.render("[${name}]", user);
		assertEquals("[]", out);
	}

	@Test
	public void Bean不存在属性保留占位符() {
		final User user = new User("sure", 18);
		final String out = TemplateUtil.render("${noSuchProp}", user);
		assertEquals("${noSuchProp}", out);
	}

	@Test
	public void 空模板与纯文本() {
		assertEquals("", TemplateUtil.render("", Map.of()));
		assertEquals("纯文本无占位符", TemplateUtil.render("纯文本无占位符", Map.of()));
	}

	@Test
	public void 空key与未闭合占位符() {
		assertEquals("ab", TemplateUtil.render("a${}b", Map.of()));
		// 未闭合 ${ 按普通文本
		assertEquals("a${b", TemplateUtil.render("a${b", Map.of()));
	}

	@Test
	public void key含特殊字符() {
		final String out = TemplateUtil.render("${用户_1}:${o2.name}", Map.of(
				"用户_1", "中文", "o2", Map.of("name", "ok")));
		assertEquals("中文:ok", out);
	}

	@Test
	public void 模板实例可复用() {
		final Template template = TemplateUtil.createTemplate("${a}|${b}");
		assertEquals("1|2", template.render(Map.of("a", 1, "b", 2)));
		assertEquals("3|4", template.render(Map.of("a", 3, "b", 4)));
	}

	@Test
	public void null模板抛异常() {
		assertThrows(IllegalArgumentException.class, () -> new SimpleTemplate(null));
	}

	@Test
	public void 特殊字符值原样输出() {
		final String out = TemplateUtil.render("${v}", Map.of("v", "a$b{c}"));
		assertEquals("a$b{c}", out);
	}

	/** 测试用用户 Bean */
	public static class User {
		private String name;
		private int age;

		public User(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}
	}
}
