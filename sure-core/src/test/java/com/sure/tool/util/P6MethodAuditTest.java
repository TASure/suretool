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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.bean.BeanDesc;
import com.sure.tool.bean.PropDesc;
import com.sure.tool.collection.BiMap;
import com.sure.tool.collection.BoundedPriorityQueue;
import com.sure.tool.collection.CaseInsensitiveMap;
import com.sure.tool.collection.MapUtil;
import com.sure.tool.collection.OrderedMap;
import com.sure.tool.collection.TreeNode;
import com.sure.tool.date.DateUnit;
import com.sure.tool.id.NanoIdUtil;
import com.sure.tool.lang.StopWatch;
import com.sure.tool.lang.WeightRandom;

/**
 * 方法-用例映射审计补测（P6）：覆盖审计报告列出的零命中方法，
 * 保证 sure-core 每个 public/protected 方法名至少被一个用例命中。
 *
 * @author suretool
 * @since 1.1.0
 */
public class P6MethodAuditTest {

	/** 简单 Bean：供 BeanDesc/PropDesc 反射用例使用 */
	public static class SimpleBean {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Test
	public void beanDescGetBeanClass() {
		BeanDesc desc = new BeanDesc(SimpleBean.class);
		Assert.assertEquals(SimpleBean.class, desc.getBeanClass());
		Assert.assertTrue(desc.getProps().size() >= 1);
	}

	@Test
	public void propDescConstructor() throws Exception {
		Field field = SimpleBean.class.getDeclaredField("name");
		Method getter = SimpleBean.class.getMethod("getName");
		Method setter = SimpleBean.class.getMethod("setName", String.class);
		PropDesc prop = new PropDesc("name", String.class, getter, setter, field);
		Assert.assertEquals("name", prop.getName());
		Assert.assertEquals(String.class, prop.getType());
	}

	@Test
	public void biMapConstructorAndInverse() {
		BiMap<String, Integer> map = new BiMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Assert.assertEquals(1, (int) map.get("a"));
		Assert.assertEquals("a", map.getKey(1));
	}

	@Test
	public void boundedPriorityQueueAddAllAndReturn() {
		BoundedPriorityQueue<Integer> queue = new BoundedPriorityQueue<>(2);
		queue.offer(5);
		queue.offer(1);
		queue.offer(9);
		Assert.assertEquals(2, queue.size());
		BoundedPriorityQueue<Integer> returned = queue.addAllAndReturn(Arrays.asList(3, 7));
		Assert.assertSame(queue, returned);
		Assert.assertEquals(2, returned.size());
	}

	@Test
	public void caseInsensitiveMapConstructor() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>();
		map.put("Key", 1);
		Assert.assertEquals(1, (int) map.get("key"));
	}

	@Test
	public void mapUtilBuilder() {
		MapUtil.MapBuilder<String, Integer> builder = MapUtil.builder();
		Map<String, Integer> map = builder.put("a", 1).put("b", 2).build();
		Assert.assertEquals(2, map.size());
		MapUtil.MapBuilder<String, Integer> direct = new MapUtil.MapBuilder<>();
		direct.putAll(map);
		Assert.assertEquals(2, direct.build().size());
	}

	@Test
	public void orderedMapConstructorAndOrder() {
		OrderedMap<String, Integer> map = new OrderedMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Assert.assertEquals(Arrays.asList("a", "b"), map.keySet().stream().toList());
	}

	@Test
	public void treeNodeSetters() {
		TreeNode<Long> root = new TreeNode<>(1L, 0L, "root");
		root.setId(2L).setParentId(1L).setName("child");
		Assert.assertEquals(2L, (long) root.getId());
		Assert.assertEquals(1L, (long) root.getParentId());
		Assert.assertEquals("child", root.getName());
	}

	@Test
	public void dateUnitGetMillis() {
		Assert.assertEquals(86_400_000L, DateUnit.DAY.getMillis());
		Assert.assertEquals(1_000L, DateUnit.SECOND.getMillis());
	}

	@Test
	public void nanoIdDefaultAlphabet() {
		char[] alphabet = NanoIdUtil.getDefaultAlphabet();
		Assert.assertTrue(alphabet.length >= 60);
		Assert.assertNotNull(NanoIdUtil.randomNanoId(10));
	}

	@Test
	public void stopWatchGetTotalTimeSeconds() {
		StopWatch watch = new StopWatch();
		watch.start();
		watch.stop();
		Assert.assertTrue(watch.getTotalTimeSeconds() >= 0L);
		Assert.assertTrue(watch.getTotalTimeMillis() >= 0L);
	}

	@Test
	public void weightRandomItems() {
		WeightRandom<String> random = new WeightRandom<>();
		random.add("low", 1);
		random.add("high", 9);
		Assert.assertEquals(2, random.items().size());
		Assert.assertNotNull(random.next());
	}

	@Test
	public void propsLoadFromStream() throws Exception {
		Props props = new Props();
		props.load(new ByteArrayInputStream("key=value\n".getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals("value", props.getStr("key"));
	}

	@Test
	public void runtimeUtilExec() throws Exception {
		String javaBin = System.getProperty("java.home")
				+ (System.getProperty("os.name").toLowerCase().contains("win") ? "\\bin\\java.exe" : "/bin/java");
		java.lang.Process process = RuntimeUtil.exec(javaBin, "-version");
		Assert.assertNotNull(process);
		process.waitFor();
		process.destroy();
	}

	@Test
	public void timeIntervalConstructor() {
		TimeInterval interval = new TimeInterval();
		Assert.assertTrue(interval.intervalMs() >= 0L);
		interval.restart();
		Assert.assertTrue(interval.intervalMs() >= 0L);
	}

	@Test
	public void urlUtilToUri() {
		URI uri = UrlUtil.toUri("https://example.com/a?b=1");
		Assert.assertEquals("example.com", uri.getHost());
		Assert.assertEquals("/a", uri.getPath());
	}
}
