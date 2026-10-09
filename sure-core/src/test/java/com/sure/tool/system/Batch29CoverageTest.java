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
package com.sure.tool.system;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.collection.Multiset;
import com.sure.tool.config.Props;
import com.sure.tool.date.DateUtil;
import com.sure.tool.graph.DiGraph;
import com.sure.tool.io.FileUtil;

/**
 * 批29 覆盖率补测：method_audit 未命中方法全覆盖。
 */
public class Batch29CoverageTest {

	@Test
	public void testMultisetElementSet() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 2).add("b", 1);
		Set<String> es = ms.elementSet();
		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b")), es);
		// 元素集合随计数结构联动
		ms.add("c");
		Assert.assertTrue(ms.elementSet().contains("c"));
	}

	@Test
	public void testMultisetIteratorHasNextBranch() {
		// hasNext 的 `remaining > 0` 分支：同元素多次计数时跨条目迭代
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 3).add("b", 2);
		List<String> seen = new ArrayList<>();
		for (Iterator<String> it = ms.iterator(); it.hasNext(); ) {
			seen.add(it.next());
		}
		Assert.assertEquals(Arrays.asList("a", "a", "a", "b", "b"), seen);
		// 越界抛 NoSuchElementException
		Multiset<String> empty = new Multiset<>();
		try {
			empty.iterator().next();
			Assert.fail("应抛 NoSuchElementException");
		} catch (NoSuchElementException expected) {
			// 预期
		}
	}

	@Test
	public void testPropsGetObj() {
		Props props = new Props();
		props.setProperty("name", "suretool");
		Assert.assertEquals("suretool", props.getObj("name"));
		Assert.assertNull(props.getObj("missing"));
	}

	@Test
	public void testDateUtilFormatterCacheInitialValue() {
		// 触发 ThreadLocal initialValue（日期格式化缓存初始化）
		String s = DateUtil.formatDate(new java.util.Date());
		Assert.assertNotNull(s);
		Assert.assertEquals(10, s.length()); // yyyy-MM-dd
	}

	@Test
	public void testDiGraphAddVertexIfAbsent() {
		DiGraph<String> g = new DiGraph<>();
		g.addVertexIfAbsent("A").addVertexIfAbsent("B");
		// 已存在忽略（幂等）
		g.addVertexIfAbsent("A");
		Assert.assertEquals(new HashSet<>(Arrays.asList("A", "B")), g.vertices());
	}

	@Test
	public void testFileUtilWalkFilesVisitFile() throws Exception {
		Path root = Files.createTempDirectory("batch29-");
		Files.writeString(root.resolve("f1.txt"), "hello");
		Files.createDirectories(root.resolve("sub"));
		Files.writeString(root.resolve("sub/f2.txt"), "world");
		List<Path> files = FileUtil.walkFiles(root);
		Assert.assertEquals(2, files.size());
		Assert.assertTrue(files.contains(root.resolve("f1.txt")));
		Assert.assertTrue(files.contains(root.resolve("sub/f2.txt")));
	}

	@Test
	public void testProcessInfoOptionals() {
		ProcessInfo pi = new ProcessInfo.Builder()
				.pid(123)
				.command("java")
				.args("-Xmx1g")
				.user("sure")
				.startTime(Instant.now())
				.cpuDurationNanos(1000L)
				.status("RUNNING")
				.parentPid(1L)
				.build();
		Assert.assertTrue(pi.getArgs().isPresent());
		Assert.assertEquals("-Xmx1g", pi.getArgs().get());
		Assert.assertEquals("sure", pi.getUser().get());
		Assert.assertEquals(Long.valueOf(1L), pi.getParentPid().get());
		Assert.assertEquals(123L, pi.getPid());
		Assert.assertEquals("RUNNING", pi.getStatus());
		Assert.assertTrue(pi.getStartTime().isPresent());
	}

	@Test
	public void testProcessInfoParentPidAbsent() {
		// parentPid = -1（无父进程）→ Optional.empty
		ProcessInfo pi = new ProcessInfo.Builder().pid(9).build();
		Assert.assertTrue(pi.getParentPid().isEmpty());
		Assert.assertTrue(pi.getArgs().isEmpty());
		Assert.assertTrue(pi.getUser().isEmpty());
	}
}
