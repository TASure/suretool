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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * SimpleTemplate 边界补测：点号路径中间节点为 null、Bean 取值抛异常、toString。
 */
public class SimpleTemplateExtraTest {

	@Test
	public void 点号路径中间节点为null保留占位符() {
		final Map<String, Object> data = new HashMap<>();
		data.put("a", null);
		assertEquals("[${a.b}]", new SimpleTemplate("[${a.b}]").render(data));
	}

	@Test
	public void Bean取值抛异常时保留占位符() {
		final BoomBean bean = new BoomBean();
		assertEquals("[${boom}]", new SimpleTemplate("[${boom}]").render(bean));
	}

	@Test
	public void toString输出片段数() {
		final SimpleTemplate template = new SimpleTemplate("${a}-${b}");
		assertEquals("SimpleTemplate(3 parts)", template.toString());
	}

	/** getter 抛异常的 Bean，验证异常被收敛为「保留占位符」。 */
	public static class BoomBean {

		/** @return 永不返回，始终抛出异常 */
		public String getBoom() {
			throw new IllegalStateException("boom");
		}
	}
}
