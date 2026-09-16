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

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.id.NanoIdUtil;
import com.sure.tool.id.UlidUtil;
import com.sure.tool.system.SystemInfo;
import com.sure.tool.util.ReUtil;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）新增功能单元测试：CollUtil 增强、NanoId/ULID、SystemInfo、ReUtil 增强、BeanUtil 深拷贝。
 */
public class P4FeaturesTest {

	// ---------------- CollUtil 增强 ----------------

	@Test
	public void testCollPage() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.page(list, 1, 2));
		Assert.assertEquals(Arrays.asList(5), CollUtil.page(list, 3, 2));
		Assert.assertEquals(Collections.emptyList(), CollUtil.page(list, 99, 2));
		Assert.assertEquals(Collections.emptyList(), CollUtil.page(null, 1, 2));
		Assert.assertEquals(Collections.emptyList(), CollUtil.page(list, 0, 2));
		Assert.assertEquals(Collections.emptyList(), CollUtil.page(list, 1, 0));
	}

	@Test
	public void testCollShuffle() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
		List<Integer> shuffled = CollUtil.shuffle(new ArrayList<>(list));
		Assert.assertEquals(8, shuffled.size());
		Assert.assertTrue(shuffled.containsAll(list));
		Assert.assertNull(CollUtil.shuffle(null));
	}

	@Test
	public void testCollCountMap() {
		Map<String, Integer> count = CollUtil.countMap(Arrays.asList("a", "b", "a", "c", "a"));
		Assert.assertEquals(Integer.valueOf(3), count.get("a"));
		Assert.assertEquals(Integer.valueOf(1), count.get("b"));
		Assert.assertEquals(Integer.valueOf(1), count.get("c"));
		Assert.assertTrue(CollUtil.countMap(null).isEmpty());
	}

	@Test
	public void testCollSubtract() {
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.subtract(Arrays.asList(1, 2, 3), Arrays.asList(3, 4)));
		Assert.assertEquals(Arrays.asList(1, 2, 3), CollUtil.subtract(Arrays.asList(1, 2, 3), null));
		Assert.assertTrue(CollUtil.subtract(null, Arrays.asList(1)).isEmpty());
	}

	@Test
	public void testCollToMap() {
		List<String> list = Arrays.asList("a", "bb", "ccc");
		Map<String, Integer> map = CollUtil.toMap(list, s -> s, String::length);
		Assert.assertEquals(Integer.valueOf(3), map.get("ccc"));
		Assert.assertEquals(3, map.size());
		Assert.assertTrue(CollUtil.toMap(null, s -> s, s -> s).isEmpty());
	}

	@Test
	public void testCollAddAll() {
		Collection<String> target = new ArrayList<>(Arrays.asList("a"));
		Assert.assertSame(target, CollUtil.addAll(target, Arrays.asList("b", "c")));
		Assert.assertEquals(3, target.size());
		CollUtil.addAll(target, null);
		Assert.assertEquals(3, target.size());
		try {
			CollUtil.addAll(null, Arrays.asList("x"));
			Assert.fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	@Test
	public void testCollIsEmptyIterable() {
		Assert.assertTrue(CollUtil.isEmpty((Iterable<String>) null));
		Assert.assertTrue(CollUtil.isEmpty(Collections.emptyList()));
		Assert.assertFalse(CollUtil.isEmpty(Arrays.asList(1)));
	}

	// ---------------- NanoId / ULID ----------------

	@Test
	public void testNanoId() {
		String id1 = NanoIdUtil.randomNanoId();
		String id2 = NanoIdUtil.randomNanoId();
		Assert.assertEquals(21, id1.length());
		Assert.assertNotEquals(id1, id2);
		Assert.assertEquals(16, NanoIdUtil.randomNanoId(16).length());
		String custom = NanoIdUtil.randomNanoId(10, "ABC123".toCharArray());
		Assert.assertEquals(10, custom.length());
		Assert.assertTrue(custom.matches("[ABC123]+"));
		Assert.assertEquals(10, NanoIdUtil.fastRandomNanoId(10).length());
		try {
			NanoIdUtil.randomNanoId(0);
			Assert.fail("size=0 应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			NanoIdUtil.randomNanoId(10, new char[0]);
			Assert.fail("空字母表应抛异常");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	@Test
	public void testUlid() {
		String id1 = UlidUtil.ulid();
		String id2 = UlidUtil.ulid();
		Assert.assertEquals(26, id1.length());
		Assert.assertTrue(UlidUtil.isValid(id1));
		Assert.assertTrue(UlidUtil.isValid(id2));
		Assert.assertNotEquals(id1, id2);

		String mono1 = UlidUtil.monotonicUlid();
		String mono2 = UlidUtil.monotonicUlid();
		String mono3 = UlidUtil.monotonicUlid();
		Assert.assertTrue(mono1.compareTo(mono2) <= 0);
		Assert.assertTrue(mono2.compareTo(mono3) <= 0);

		String ts1 = UlidUtil.ulid(Instant.ofEpochMilli(1700000000000L));
		String ts2 = UlidUtil.ulid(Instant.ofEpochMilli(1700000001000L));
		Assert.assertTrue(ts1.compareTo(ts2) < 0);

		Assert.assertFalse(UlidUtil.isValid(null));
		Assert.assertFalse(UlidUtil.isValid("abc"));
		Assert.assertTrue(UlidUtil.isValid("AAAAAAAAAAAAAAAAAAAAAAAAAA")); // 全 A 合法
		Assert.assertFalse(UlidUtil.isValid("!!!!!!!!!!!!!!!!!!!!!!!!!!"));
	}

	// ---------------- SystemInfo ----------------

	@Test
	public void testSystemInfo() {
		Assert.assertTrue(SystemInfo.getAvailableProcessors() >= 1);
		Assert.assertNotNull(SystemInfo.getOsName());
		Assert.assertNotNull(SystemInfo.getOsVersion());
		Assert.assertNotNull(SystemInfo.getOsArch());
		Assert.assertNotNull(SystemInfo.getJvmName());
		Assert.assertNotNull(SystemInfo.getJvmVendor());
		Assert.assertNotNull(SystemInfo.getJvmVersion());
		Assert.assertNotNull(SystemInfo.getJavaVersion());
		Assert.assertTrue(SystemInfo.getJvmTotalMemory() > 0);
		Assert.assertTrue(SystemInfo.getJvmFreeMemory() >= 0);
		Assert.assertTrue(SystemInfo.getJvmUsedMemory() >= 0);
		Assert.assertTrue(SystemInfo.getJvmMaxMemory() > 0);
		Assert.assertTrue(SystemInfo.getPid() > 0);
		Assert.assertNotNull(SystemInfo.getStartTime());
		Assert.assertNotNull(SystemInfo.getUptime());
		Assert.assertNotNull(SystemInfo.getCommandLine());
		Assert.assertNotNull(SystemInfo.getSystemLoadAverage());
		boolean anyOs = SystemInfo.isWindows() || SystemInfo.isLinux() || SystemInfo.isMac() || SystemInfo.isUnix();
		Assert.assertTrue(anyOs);
	}

	// ---------------- ReUtil 增强 ----------------

	@Test
	public void testReEscape() {
		Assert.assertEquals("a" + '\\' + ".b", ReUtil.escape("a.b"));
		Assert.assertEquals("1" + '\\' + "+1", ReUtil.escape("1+1"));
		Assert.assertEquals("plain", ReUtil.escape("plain"));
		Assert.assertNull(ReUtil.escape(null));
		Assert.assertEquals("a.b", ReUtil.unescape("a" + '\\' + ".b"));
		Assert.assertEquals("a" + '\\' + "b", ReUtil.unescape("a" + '\\' + '\\' + "b"));
		Assert.assertNull(ReUtil.unescape(null));
	}

	@Test
	public void testReFindAllGroup() {
		List<String> groups = ReUtil.findAll("(\\d+)-(\\d+)", "1-2 3-4", 2);
		Assert.assertEquals(Arrays.asList("2", "4"), groups);
		List<String> whole = ReUtil.findAll("(\\d+)-(\\d+)", "1-2 3-4", 0);
		Assert.assertEquals(Arrays.asList("1-2", "3-4"), whole);
	}

	// ---------------- BeanUtil 深拷贝 ----------------

	@Test
	public void testDeepCopy() {
		Assert.assertNull(BeanUtil.deepCopy(null));

		String s = "abc";
		Assert.assertSame(s, BeanUtil.deepCopy(s));

		List<Object> srcList = new ArrayList<>();
		srcList.add("x");
		srcList.add(new HashMap<>(Map.of("k", 1)));
		List<Object> listCopy = BeanUtil.deepCopy(srcList);
		Assert.assertNotSame(srcList, listCopy);
		Assert.assertNotSame(srcList.get(1), listCopy.get(1));
		Assert.assertEquals(srcList, listCopy);

		Map<String, Object> srcMap = new LinkedHashMap<>();
		srcMap.put("nested", new HashMap<>(Map.of("a", 1)));
		Map<String, Object> mapCopy = BeanUtil.deepCopy(srcMap);
		Assert.assertNotSame(srcMap, mapCopy);
		Assert.assertNotSame(srcMap.get("nested"), mapCopy.get("nested"));
		Assert.assertEquals(srcMap, mapCopy);

		String[] srcArr = {"a", "b"};
		String[] arrCopy = BeanUtil.deepCopy(srcArr);
		Assert.assertNotSame(srcArr, arrCopy);
		Assert.assertArrayEquals(srcArr, arrCopy);

		int[] intArr = {1, 2, 3};
		int[] intCopy = BeanUtil.deepCopy(intArr);
		Assert.assertNotSame(intArr, intCopy);
		Assert.assertArrayEquals(intArr, intCopy);
	}
}
