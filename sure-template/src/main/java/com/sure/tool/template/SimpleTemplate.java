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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.lang.Assert;

/**
 * 内置简单模板（零依赖）。
 *
 * <p>语法：</p>
 * <ul>
 *   <li>占位符：{@code ${name}}，name 支持点号层级（{@code ${user.name}}）</li>
 *   <li>转义：{@code \${name}} 渲染为字面 {@code ${name}}</li>
 *   <li>null 值渲染为空串；未知变量保留占位符原样（便于发现拼写错误）</li>
 * </ul>
 *
 * <p>线程安全：模板解析结果不可变，{@code render} 不修改内部状态，实例可多线程共享。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * SimpleTemplate template = new SimpleTemplate("Hello ${name}, age=${user.age}");
 * template.render(Map.of("name", "sure", "user", Map.of("age", 18)));
 * }</pre>
 *
 * @since 1.3.0
 */
public final class SimpleTemplate implements Template {

	/** 模板片段：文本或占位符 key */
	private static sealed interface Part permits TextPart, KeyPart {
	}

	/** 文本片段 */
	private record TextPart(String text) implements Part {
	}

	/** 占位符片段 */
	private record KeyPart(String key) implements Part {
	}

	/** 未知变量哨兵：区分「值为 null」（渲染空串）与「变量不存在」（保留占位符） */
	private static final Object MISSING = new Object();

	private final List<Part> parts;

	/**
	 * 构造模板并解析。
	 *
	 * @param templateText 模板文本
	 * @throws IllegalArgumentException 模板文本为 null 时抛出
	 */
	public SimpleTemplate(String templateText) {
		Assert.notNull(templateText, "模板文本不能为 null");
		this.parts = parse(templateText);
	}

	/**
	 * 单遍解析模板：识别 {@code \${} 转义} 与 {@code ${key}} 占位符。
	 */
	private static List<Part> parse(String text) {
		final List<Part> result = new ArrayList<>();
		final StringBuilder plain = new StringBuilder();
		int i = 0;
		final int len = text.length();
		while (i < len) {
			final char c = text.charAt(i);
			if (c == '\\' && i + 1 < len && text.charAt(i + 1) == '$'
					&& i + 2 < len && text.charAt(i + 2) == '{') {
				// 转义 \${ -> 字面 ${
				plain.append("${");
				i += 3;
				continue;
			}
			if (c == '$' && i + 1 < len && text.charAt(i + 1) == '{') {
				final int close = text.indexOf('}', i + 2);
				if (close < 0) {
					// 未闭合：按普通文本处理
					plain.append(c);
					i++;
					continue;
				}
				final String key = text.substring(i + 2, close);
				if (plain.length() > 0) {
					result.add(new TextPart(plain.toString()));
					plain.setLength(0);
				}
				result.add(new KeyPart(key));
				i = close + 1;
				continue;
			}
			plain.append(c);
			i++;
		}
		if (plain.length() > 0) {
			result.add(new TextPart(plain.toString()));
		}
		return List.copyOf(result);
	}

	@Override
	public String render(Map<String, ?> data) {
		return renderInternal(data);
	}

	@Override
	public String render(Object bean) {
		return renderInternal(bean);
	}

	private String renderInternal(Object root) {
		final StringBuilder sb = new StringBuilder();
		for (Part part : parts) {
			if (part instanceof TextPart text) {
				sb.append(text.text());
			} else if (part instanceof KeyPart key) {
				if (key.key().isEmpty()) {
					// 空占位符 ${}：无 key 无值，渲染为空
					continue;
				}
				final Object value = resolve(root, key.key());
				if (value == MISSING) {
					// 未知变量：保留占位符原样
					sb.append("${").append(key.key()).append('}');
				} else {
					sb.append(value == null ? "" : value.toString());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 解析点号路径取值：Map 直接取值，非 Map 按 Bean 属性反射取值。
	 */
	private static Object resolve(Object root, String key) {
		Object current = root;
		for (String part : key.split("\\.")) {
			if (current == null) {
				return MISSING;
			}
			if (current instanceof Map<?, ?> map) {
				if (!map.containsKey(part)) {
					return MISSING;
				}
				current = map.get(part);
			} else {
				try {
					// 属性存在性检查：区分「属性值为 null」（渲染空串）与「属性不存在」（保留占位符）
					final com.sure.tool.bean.BeanDesc desc = BeanUtil.getBeanDesc(current.getClass());
					if (!desc.containsProp(part)) {
						return MISSING;
					}
					current = BeanUtil.getProperty(current, part);
				} catch (RuntimeException e) {
					return MISSING;
				}
			}
		}
		return current;
	}

	@Override
	public String toString() {
		return "SimpleTemplate(" + parts.size() + " parts)";
	}
}
