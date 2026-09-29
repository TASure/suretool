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
 * 模板工具门面。
 *
 * <p>提供模板创建与一行式渲染的便捷方法，默认实现为 {@link SimpleTemplate}。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String text = TemplateUtil.render("订单 ${orderId} 已支付", Map.of("orderId", 10086));
 * }</pre>
 *
 * @since 1.3.0
 */
public final class TemplateUtil {

	private TemplateUtil() {
	}

	/**
	 * 创建模板（默认 {@link SimpleTemplate}）。
	 *
	 * @param templateText 模板文本
	 * @return 模板实例
	 */
	public static Template createTemplate(String templateText) {
		return new SimpleTemplate(templateText);
	}

	/**
	 * 一行式渲染（Map 数据）。
	 *
	 * @param templateText 模板文本
	 * @param data         变量映射
	 * @return 渲染结果
	 */
	public static String render(String templateText, Map<String, ?> data) {
		return createTemplate(templateText).render(data);
	}

	/**
	 * 一行式渲染（Bean 数据）。
	 *
	 * @param templateText 模板文本
	 * @param bean         任意对象
	 * @return 渲染结果
	 */
	public static String render(String templateText, Object bean) {
		return createTemplate(templateText).render(bean);
	}
}
