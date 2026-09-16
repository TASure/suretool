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

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第三十八批测试：异常解包装、断言失败、后台线程执行、枚举字段值。
 */
public class P4Features33Test {

	@Test
	public void testExceptionUnwrap() {
		IllegalStateException root = new IllegalStateException("root");
		RuntimeException mid = new RuntimeException("mid", root);
		RuntimeException outer = new RuntimeException("outer", mid);
		Assert.assertSame(root, com.sure.tool.util.ExceptionUtil.unwrap(outer));
		Assert.assertSame(root, com.sure.tool.util.ExceptionUtil.unwrap(root));
		Assert.assertNull(com.sure.tool.util.ExceptionUtil.unwrap(null));
	}

	@Test
	public void testAssertFail() {
		try {
			com.sure.tool.lang.Assert.fail("必须为 {} 类型", "数字");
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("必须为 数字 类型", e.getMessage());
		}
	}

	@Test
	public void testThreadExecute() {
		java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
		com.sure.tool.thread.ThreadUtil.execute(counter::incrementAndGet);
		com.sure.tool.util.RandomUtil.randomString(1); // 微小间隔
		int before = counter.get();
		long deadline = System.currentTimeMillis() + 2000;
		while (counter.get() == before && System.currentTimeMillis() < deadline) {
			com.sure.tool.thread.ThreadUtil.sleep(10);
		}
		Assert.assertEquals(1, counter.get());
	}

	enum Color {
		RED(1, "红色"), GREEN(2, "绿色"), BLUE(3, "蓝色");

		final int code;
		final String label;

		Color(int code, String label) {
			this.code = code;
			this.label = label;
		}
	}

	@Test
	public void testEnumFieldValues() {
		java.util.List<Object> codes = com.sure.tool.util.EnumUtil.getFieldValues(Color.class, "code");
		Assert.assertEquals(java.util.Arrays.asList(1, 2, 3), codes);
		java.util.List<Object> labels = com.sure.tool.util.EnumUtil.getFieldValues(Color.class, "label");
		Assert.assertEquals(java.util.Arrays.asList("红色", "绿色", "蓝色"), labels);
		java.util.List<Object> names = com.sure.tool.util.EnumUtil.getFieldValues(Color.class, "name");
		Assert.assertEquals(java.util.Arrays.asList("RED", "GREEN", "BLUE"), names);
		try {
			com.sure.tool.util.EnumUtil.getFieldValues(Color.class, "missing");
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}
}
