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

import com.sure.tool.collection.CsvUtil;
import com.sure.tool.collection.MapUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.IdUtil;
import com.sure.tool.util.Props;
import com.sure.tool.util.Singleton;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）第六批新增/增强单元测试：Singleton / Props / DateUtil / CsvUtil / IdUtil / MapUtil。
 */
public class P4Features6Test {

	// ---------------- Singleton ----------------

	@Test
	public void testSingleton() {
		Singleton.destroy();
		Assert.assertSame(Singleton.get(ArrayList.class), Singleton.get(ArrayList.class));
		Assert.assertTrue(Singleton.contains(ArrayList.class));

		TestSingletonTarget t1 = Singleton.get(TestSingletonTarget.class);
		Assert.assertSame(t1, Singleton.get(TestSingletonTarget.class));
		Assert.assertEquals("default", t1.name);

		TestSingletonTarget custom = new TestSingletonTarget("custom");
		Assert.assertSame(t1, Singleton.put(TestSingletonTarget.class, custom));
		Assert.assertSame(custom, Singleton.get(TestSingletonTarget.class));
		Assert.assertSame(custom, Singleton.remove(TestSingletonTarget.class));
		Assert.assertFalse(Singleton.contains(TestSingletonTarget.class));

		Singleton.destroy();
		Assert.assertFalse(Singleton.contains(ArrayList.class));
	}

	/** Singleton 测试目标类（默认构造器）。 */
	public static class TestSingletonTarget {
		public final String name;

		public TestSingletonTarget() {
			this.name = "default";
		}

		public TestSingletonTarget(String name) {
			this.name = name;
		}
	}

	// ---------------- Props ----------------

	@Test
	public void testProps() throws Exception {
		String content = "name=suretool\nport=8080\nrate=3.14\nenabled=true\n中文键=中文值";
		Props props = new Props(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

		Assert.assertEquals("suretool", props.getStr("name"));
		Assert.assertEquals("默认", props.getStr("missing", "默认"));
		Assert.assertEquals(8080, props.getInt("port"));
		Assert.assertEquals(99, props.getInt("missing", 99));
		Assert.assertEquals(3.14D, props.getDouble("rate"), 1e-9);
		Assert.assertTrue(props.getBool("enabled"));
		Assert.assertEquals("中文值", props.getStr("中文键"));
		Assert.assertTrue(props.containsKey("name"));
		Assert.assertFalse(props.containsKey("nope"));

		props.set("port", 9090);
		Assert.assertEquals(9090, props.getInt("port"));

		// 克隆保护：修改克隆不影响原对象
		Props p2 = new Props();
		p2.set("a", "1");
		p2.getProperties().setProperty("a", "2");
		Assert.assertEquals("1", p2.getStr("a"));
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance2() throws Exception {
		// 2026-09-16 是周三
		Date d = DateUtil.parse("2026-09-16 15:30:45", DateUtil.NORM_DATETIME_PATTERN);

		Date beginWeek = DateUtil.beginOfWeek(d);
		Calendar bc = Calendar.getInstance();
		bc.setTime(beginWeek);
		Assert.assertEquals(Calendar.MONDAY, bc.get(Calendar.DAY_OF_WEEK));
		Assert.assertEquals(0, bc.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(0, bc.get(Calendar.MILLISECOND));

		Date endWeek = DateUtil.endOfWeek(d);
		Calendar ec = Calendar.getInstance();
		ec.setTime(endWeek);
		Assert.assertEquals(Calendar.SUNDAY, ec.get(Calendar.DAY_OF_WEEK));
		Assert.assertEquals(23, ec.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(999, ec.get(Calendar.MILLISECOND));

		Assert.assertEquals("2026年9月16日 15:30:45", DateUtil.formatChineseDateTime(d));
		Assert.assertEquals(16, DateUtil.dayOfMonth(d));
		Assert.assertEquals(15, DateUtil.hour(d));
	}

	// ---------------- CsvUtil 增强 ----------------

	@Test
	public void testCsvEnhance() throws Exception {
		List<List<String>> rows = new ArrayList<>();
		rows.add(List.of("姓名", "年龄"));
		rows.add(List.of("张三", "18"));

		java.io.File tmp = java.io.File.createTempFile("sure-csv", ".csv");
		try {
			CsvUtil.write(tmp, rows, StandardCharsets.UTF_8);
			List<List<String>> read = CsvUtil.read(tmp, StandardCharsets.UTF_8);
			Assert.assertEquals(2, read.size());
			Assert.assertEquals("姓名", read.get(0).get(0));
			Assert.assertEquals("18", read.get(1).get(1));
		} finally {
			tmp.delete();
		}
	}

	// ---------------- IdUtil / MapUtil 增强 ----------------

	@Test
	public void testIdAndMapEnhance() {
		Assert.assertNotNull(IdUtil.createSnowflake().nextId());

		Map<String, Integer> map = new HashMap<>();
		map.put("a", 3);
		map.put("b", 1);
		map.put("c", 2);

		Map<String, Integer> asc = MapUtil.sortByValue(map, true);
		Assert.assertEquals(List.of("b", "c", "a"), new ArrayList<>(asc.keySet()));
		Map<String, Integer> desc = MapUtil.sortByValue(map, false);
		Assert.assertEquals(List.of("a", "c", "b"), new ArrayList<>(desc.keySet()));
		Assert.assertEquals(3, map.size());
		Assert.assertEquals(0, MapUtil.<String, Integer>sortByValue(new LinkedHashMap<>(), true).size());
	}
}
