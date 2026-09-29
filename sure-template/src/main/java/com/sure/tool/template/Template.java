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

import java.util.Map;

/**
 * 模板接口。
 *
 * <p>与具体模板引擎解耦：suretool 内置 {@link SimpleTemplate}（零依赖），
 * 未来可通过本接口适配 Freemarker / Velocity 等引擎而不影响调用方。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * Template template = TemplateUtil.createTemplate("你好，${name}！");
 * String text = template.render(Map.of("name", "sure")); // 你好，sure！
 * }</pre>
 *
 * @since 1.3.0
 */
public interface Template {

	/**
	 * 按数据映射渲染模板。
	 *
	 * @param data 变量映射（key 对应占位符名，支持点号路径）
	 * @return 渲染结果
	 */
	String render(Map<String, ?> data);

	/**
	 * 按 Bean 属性渲染模板。
	 *
	 * @param bean 任意对象（属性名对应占位符名，支持嵌套）
	 * @return 渲染结果
	 */
	String render(Object bean);
}
