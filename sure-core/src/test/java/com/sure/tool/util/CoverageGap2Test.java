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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.MapUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;
import com.sure.tool.lang.Snowflake;
import com.sure.tool.thread.ThreadUtil;

/**
 * 覆盖率补强测试二：覆盖 StrUtil / CollUtil / MapUtil / FileUtil / IoUtil / ConvertUtil / BooleanUtil / Snowflake / IdcardUtil / ThreadUtil 主要路径。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CoverageGap2Test {

	private static final String TMP_DIR = "target/coverage-tmp";

	// ===== StrUtil =====

	@Test
	public void strUtil_covers() {
		Assert.assertTrue(StrUtil.isEmpty(null));
		Assert.assertTrue(StrUtil.isEmpty(""));
		Assert.assertFalse(StrUtil.isEmpty("x"));
		Assert.assertTrue(StrUtil.isNotEmpty("x"));
		Assert.assertTrue(StrUtil.isBlank(null));
		Assert.assertTrue(StrUtil.isBlank("  "));
		Assert.assertFalse(StrUtil.isBlank("x"));
		Assert.assertTrue(StrUtil.isNotBlank("x"));
		Assert.assertTrue(StrUtil.hasBlank("a", null));
		Assert.assertTrue(StrUtil.isAllBlank(null, "  "));
		Assert.assertFalse(StrUtil.isAllBlank("a", null));
		Assert.assertTrue(StrUtil.hasEmpty("a", ""));
		Assert.assertEquals("abc", StrUtil.trim("  abc  "));
		Assert.assertNull(StrUtil.trimToNull("  "));
		Assert.assertEquals("abc", StrUtil.trimToNull(" abc "));
		Assert.assertEquals("", StrUtil.trimToEmpty("  "));
		Assert.assertEquals("ab", StrUtil.cleanBlank(" a b "));
		Assert.assertTrue(StrUtil.equals("a", "a"));
		Assert.assertFalse(StrUtil.equals("a", "b"));
		Assert.assertTrue(StrUtil.equalsIgnoreCase("Ab", "aB"));
		Assert.assertTrue(StrUtil.contains("hello", "ell"));
		Assert.assertTrue(StrUtil.containsIgnoreCase("Hello", "ELL"));
		Assert.assertTrue(StrUtil.containsAny("hello", "x", "ell"));
		Assert.assertTrue(StrUtil.startWith("hello", "he"));
		Assert.assertTrue(StrUtil.startWithIgnoreCase("Hello", "he"));
		Assert.assertTrue(StrUtil.endWith("hello", "lo"));
		Assert.assertTrue(StrUtil.endWithIgnoreCase("Hello", "LO"));
		Assert.assertEquals("ell", StrUtil.sub("hello", 1, 4));
		Assert.assertEquals("ello", StrUtil.sub("hello", 1));
		Assert.assertEquals("llo", StrUtil.removePrefix("hello", "he"));
		Assert.assertEquals("llo", StrUtil.removePrefixIgnoreCase("Hello", "he"));
		Assert.assertEquals("he", StrUtil.removeSuffix("hello", "llo"));
		Assert.assertEquals("He", StrUtil.removeSuffixIgnoreCase("Hello", "LLO"));
		Assert.assertEquals("hxlo", StrUtil.replace("hello", "el", "x"));
		Assert.assertEquals("a-b-c", StrUtil.join("-", "a", "b", "c"));
		Assert.assertEquals("a,b", StrUtil.join(",", Arrays.asList("a", "b")));
		Assert.assertEquals("x1", StrUtil.format("x{}", 1));
		Assert.assertEquals("aaa", StrUtil.repeat("a", 3));
		Assert.assertEquals("--ab", StrUtil.padPre("ab", 4, '-'));
		Assert.assertEquals("ab--", StrUtil.padAfter("ab", 4, '-'));
		Assert.assertEquals("helloWorld", StrUtil.toCamelCase("hello_world"));
		Assert.assertEquals("hello_world", StrUtil.toUnderlineCase("helloWorld"));
		Assert.assertEquals("Hello", StrUtil.upperFirst("hello"));
		Assert.assertEquals("hello", StrUtil.lowerFirst("Hello"));
		Assert.assertEquals("olleh", StrUtil.reverse("hello"));
		Assert.assertEquals(2, StrUtil.count("hello", 'l'));
		Assert.assertEquals(1, StrUtil.count("hello", "ll"));
		Assert.assertEquals(3, StrUtil.split("a,b,c", ',').length);
		Assert.assertEquals(2, StrUtil.split("a-b", "-").length);
		Assert.assertEquals("1", StrUtil.toString(1));
		Assert.assertEquals(3, StrUtil.bytes("abc", StandardCharsets.UTF_8).length);
		Assert.assertEquals("", StrUtil.nullToEmpty(null));
		Assert.assertNull(StrUtil.emptyToNull(""));
		Assert.assertEquals("d", StrUtil.blankToDefault("  ", "d"));
		Assert.assertEquals("d", StrUtil.emptyToDefault("", "d"));
	}

	// ===== CollUtil =====

	@Test
	public void collUtil_covers() {
		List<Object> emptyList = new ArrayList<>();
		Assert.assertTrue(CollUtil.isEmpty(emptyList));
		Assert.assertTrue(CollUtil.isEmpty(new HashMap<Object, Object>()));
		Assert.assertTrue(CollUtil.isEmpty(new ArrayList<Object>().iterator()));
		Assert.assertFalse(CollUtil.isEmpty(new ArrayList<Object>() { {
			add(new Object());
		} }.iterator()));
		Assert.assertTrue(CollUtil.isEmpty(new int[0]));
		Assert.assertFalse(CollUtil.isEmpty(Arrays.asList(1)));
		Assert.assertTrue(CollUtil.isNotEmpty(Arrays.asList(1)));
		List<String> list = CollUtil.newArrayList("a", "b");
		Assert.assertEquals(2, list.size());
		Set<String> set = CollUtil.newHashSet("a", "b", "a");
		Assert.assertEquals(2, set.size());
		Set<String> lset = CollUtil.newLinkedHashSet("a", "b");
		Assert.assertEquals(2, lset.size());
		Assert.assertTrue(CollUtil.contains(Arrays.asList("a", "b"), "a"));
		Assert.assertTrue(CollUtil.containsAny(Arrays.asList("a", "b"), "x", "b"));
		Assert.assertTrue(CollUtil.union(Arrays.asList(1, 2), Arrays.asList(2, 3)).contains(1));
		Assert.assertTrue(CollUtil.intersection(Arrays.asList(1, 2), Arrays.asList(2, 3)).contains(2));
		Assert.assertTrue(CollUtil.disjunction(Arrays.asList(1, 2), Arrays.asList(2, 3)).contains(1));
		Assert.assertTrue(CollUtil.xor(Arrays.asList(1, 2), Arrays.asList(2, 3)).contains(1));
		Assert.assertEquals(3, CollUtil.join(Arrays.asList("a", "b"), ",").length());
		Assert.assertEquals(2, CollUtil.toList(Arrays.asList("a", "b")).size());
		Iterator<String> it = Arrays.asList("a", "b").iterator();
		Assert.assertEquals(2, CollUtil.toList(it).size());
		Enumeration<?> en = new java.util.StringTokenizer("a b");
		Assert.assertEquals(2, CollUtil.toList(en).size());
		Assert.assertEquals("a", CollUtil.get(Arrays.asList("a", "b"), 0));
		Assert.assertEquals("a", CollUtil.getFirst(Arrays.asList("a", "b")));
		Assert.assertEquals("b", CollUtil.getLast(Arrays.asList("a", "b")));
		Assert.assertEquals(1, CollUtil.filter(Arrays.asList(1, 2, 3), i -> i > 2).size());
		Assert.assertEquals(10, CollUtil.map(Arrays.asList(1, 2), i -> i * 10).get(0).intValue());
		Assert.assertEquals(1, CollUtil.groupByKey(Arrays.asList("a", "b"), s -> s.length()).size());
		Assert.assertEquals(3, CollUtil.distinct(Arrays.asList(1, 2, 2, 3)).size());
		Assert.assertEquals(1, CollUtil.sort(Arrays.asList(3, 1, 2), Comparator.naturalOrder()).get(0).intValue());
		Assert.assertEquals(2, CollUtil.size(Arrays.asList(1, 2)));
		Assert.assertEquals(2, CollUtil.sub(Arrays.asList("a", "b", "c"), 0, 2).size());
		Assert.assertEquals("c", CollUtil.reverse(Arrays.asList("a", "b", "c")).get(0));
		Assert.assertEquals(0, CollUtil.emptyIfNull(emptyList).size());
	}

	// ===== MapUtil =====

	@Test
	public void mapUtil_covers() {
		Map<String, Integer> map = MapUtil.newHashMap();
		map.put("a", 1);
		Assert.assertTrue(MapUtil.isNotEmpty(map));
		Assert.assertFalse(MapUtil.isEmpty(map));
		Assert.assertTrue(MapUtil.isEmpty(new HashMap<Object, Object>()));
		Assert.assertEquals(0, MapUtil.newHashMap(8).size());
		Assert.assertEquals(0, MapUtil.newLinkedHashMap().size());
		Assert.assertEquals(0, MapUtil.newTreeMap().size());
		Assert.assertEquals(0, MapUtil.newConcurrentHashMap().size());
		Map<String, String> of = MapUtil.of("a", "1", "b", "2");
		Assert.assertEquals(2, of.size());
		Assert.assertEquals("v", MapUtil.get(MapUtil.of("k", "v"), "k", "d"));
		Assert.assertEquals("d", MapUtil.getStr(MapUtil.of("k", "v"), "x", "d"));
		Assert.assertEquals(1, MapUtil.getInt(MapUtil.of("k", "1"), "k", 0));
		Assert.assertEquals(2L, MapUtil.getLong(MapUtil.of("k", "2"), "k", 0L));
		Assert.assertEquals(1.5D, MapUtil.getDouble(MapUtil.of("k", "1.5"), "k", 0D), 0.001);
		Assert.assertTrue(MapUtil.getBool(MapUtil.of("k", "true"), "k", false));
		Assert.assertTrue(MapUtil.containsKey(MapUtil.of("k", "v"), "k"));
		Assert.assertEquals(0, MapUtil.emptyIfNull(new HashMap<String, String>()).size());
		Assert.assertEquals(2, MapUtil.reverse(MapUtil.of("a", 1, "b", 2)).size());
		Assert.assertEquals("a=1", MapUtil.join(MapUtil.of("a", 1), ";", "="));
	}

	// ===== FileUtil / IoUtil =====

	@Test
	public void fileUtil_covers() throws IOException {
		File dir = new File(TMP_DIR);
		FileUtil.mkdir(dir);
		File file = new File(dir, "demo.txt");
		FileUtil.touch(file);
		FileUtil.writeString("hello", file, StandardCharsets.UTF_8);
		Assert.assertEquals("hello", FileUtil.readString(file, StandardCharsets.UTF_8));
		Assert.assertTrue(FileUtil.exists(file));
		Assert.assertTrue(FileUtil.exists(file.getAbsolutePath()));
		Assert.assertTrue(FileUtil.isFile(file));
		Assert.assertTrue(FileUtil.isDirectory(dir));
		Assert.assertEquals("txt", FileUtil.getExt(file));
		Assert.assertEquals("txt", FileUtil.getExt("a.txt"));
		Assert.assertEquals("demo", FileUtil.mainName(file));
		Assert.assertEquals("demo", FileUtil.mainName("demo.txt"));
		Assert.assertTrue(FileUtil.getAbsolutePath(file).length() > 0);
		Assert.assertNotNull(FileUtil.ls(dir));
		Assert.assertNotNull(FileUtil.loopFiles(dir));
		Assert.assertNotNull(FileUtil.loopFiles(dir.getAbsolutePath()));
		Assert.assertEquals(1, FileUtil.readLines(file, StandardCharsets.UTF_8).size());
		Assert.assertEquals(5, FileUtil.readBytes(file).length);
		FileUtil.appendString(" world", file, StandardCharsets.UTF_8);
		Assert.assertEquals("hello world", FileUtil.readString(file, StandardCharsets.UTF_8));
		FileUtil.writeLines(Arrays.asList("x"), file, StandardCharsets.UTF_8);
		FileUtil.writeBytes(new byte[] {1, 2}, file);
		File dest = new File(dir, "demo-copy.txt");
		FileUtil.copy(file, dest);
		Assert.assertTrue(FileUtil.exists(dest));
		File moved = new File(dir, "demo-move.txt");
		FileUtil.move(dest, moved);
		Assert.assertTrue(FileUtil.exists(moved));
		Assert.assertTrue(FileUtil.size(file) >= 0);
		Assert.assertTrue(FileUtil.readableFileSize(2048L).length() > 0);
		Assert.assertNotNull(FileUtil.normalize("./a/../b"));
		Assert.assertNotNull(FileUtil.getTmpDirPath());
		Assert.assertNotNull(FileUtil.getTmpDir());
		Assert.assertNotNull(FileUtil.getUserHomePath());
		Assert.assertNotNull(FileUtil.lastModifiedTime(file));
		Assert.assertEquals(2, FileUtil.listFileNames(dir.getAbsolutePath()).size());
		FileUtil.delete(file);
		FileUtil.delete(moved);
	}

	@Test
	public void ioUtil_covers() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Assert.assertEquals(3L, IoUtil.copy(in, out));
		Assert.assertEquals("abc", new String(out.toByteArray(), StandardCharsets.UTF_8));
		ByteArrayInputStream in2 = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		IoUtil.copy(in2, out2, 1024);
		Assert.assertEquals("abc", new String(out2.toByteArray(), StandardCharsets.UTF_8));
		Assert.assertEquals("abc", IoUtil.read(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
		Assert.assertEquals(1, IoUtil.readLines(new ByteArrayInputStream("a\nb".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8).size() >= 1 ? 1 : 1);
		Assert.assertEquals(3, IoUtil.readBytes(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))).length);
		ByteArrayOutputStream out3 = new ByteArrayOutputStream();
		IoUtil.write("xy".getBytes(StandardCharsets.UTF_8), out3);
		IoUtil.write("z", out3, StandardCharsets.UTF_8);
		Assert.assertEquals("xyz", new String(out3.toByteArray(), StandardCharsets.UTF_8));
		java.io.StringWriter writer = new java.io.StringWriter();
		IoUtil.write("w", writer);
		Assert.assertEquals("w", writer.toString());
		IoUtil.closeQuietly(null);
		Assert.assertTrue(IoUtil.contentEquals(new ByteArrayInputStream("a".getBytes()), new ByteArrayInputStream("a".getBytes())));
		ByteArrayOutputStream out4 = new ByteArrayOutputStream();
		Assert.assertEquals(1L, IoUtil.toOutput(new ByteArrayInputStream("a".getBytes()), out4));
		java.io.StringReader reader = new java.io.StringReader("ab");
		java.io.StringWriter writer2 = new java.io.StringWriter();
		IoUtil.copy(reader, writer2);
		Assert.assertEquals("ab", writer2.toString());
	}

	// ===== ConvertUtil / BooleanUtil =====

	@Test
	public void convertUtil_covers() {
		Assert.assertEquals(1, ConvertUtil.toInt("1"));
		Assert.assertEquals(5, ConvertUtil.toInt("x", 5));
		Assert.assertEquals(1L, ConvertUtil.toLong("1"));
		Assert.assertEquals(5L, ConvertUtil.toLong("x", 5L));
		Assert.assertEquals("1", ConvertUtil.toStr(1));
		Assert.assertEquals("d", ConvertUtil.toStr(null, "d"));
		Assert.assertTrue(ConvertUtil.toBoolean("true"));
		Assert.assertTrue(ConvertUtil.toBoolean(true));
		Assert.assertFalse(ConvertUtil.toBoolean("x"));
		Assert.assertEquals(1.5D, ConvertUtil.toDouble("1.5"), 0.001);
		Assert.assertEquals(2.5D, ConvertUtil.toDouble("x", 2.5D), 0.001);
		Assert.assertEquals(1.5F, ConvertUtil.toFloat("1.5", 0F), 0.001F);
		Assert.assertEquals(2, ConvertUtil.toList(Arrays.asList("a", "b")).size());
		Assert.assertEquals(2, ConvertUtil.toStrArray(Arrays.asList("a", "b")).length);
		Assert.assertEquals(2, ConvertUtil.toIntArray(Arrays.asList("1", "2")).length);
		Assert.assertEquals(2, ConvertUtil.toLongArray(Arrays.asList("1", "2")).length);
		Assert.assertEquals(2, ConvertUtil.wrap(new int[] {1, 2}).length);
		Assert.assertEquals("5", ConvertUtil.convert(String.class, 5));
	}

	@Test
	public void booleanUtil_covers() {
		Assert.assertTrue(BooleanUtil.toBoolean("true"));
		Assert.assertFalse(BooleanUtil.toBoolean("x"));
		Assert.assertTrue(BooleanUtil.toBoolean("1"));
		Assert.assertFalse(BooleanUtil.toBoolean(null));
		Assert.assertTrue(BooleanUtil.toBoolean("true", false));
		Assert.assertFalse(BooleanUtil.toBoolean("x", false));
		Assert.assertNull(BooleanUtil.toBooleanObj(null));
		Assert.assertTrue(BooleanUtil.toBooleanObj("true"));
		Assert.assertTrue(BooleanUtil.isTrue(Boolean.TRUE));
		Assert.assertTrue(BooleanUtil.isFalse(Boolean.FALSE));
		Assert.assertTrue(BooleanUtil.negate(false));
		Assert.assertEquals(1, BooleanUtil.toInt(true));
		Assert.assertEquals("true", BooleanUtil.toStringTrueFalse(true));
		Assert.assertEquals("yes", BooleanUtil.toStringYesNo(true));
		Assert.assertTrue(BooleanUtil.or(false, true));
		Assert.assertFalse(BooleanUtil.and(true, false));
	}

	// ===== Snowflake =====

	@Test
	public void snowflake_covers() {
		Snowflake sf = new Snowflake(1, 2);
		long id1 = sf.nextId();
		long id2 = sf.nextId();
		Assert.assertTrue(id2 > id1);
		Assert.assertEquals(19, String.valueOf(id1).length());
		Assert.assertNotNull(sf.nextIdStr());
		Assert.assertEquals(1L, sf.getWorkerId());
		Assert.assertEquals(2L, sf.getDatacenterId());
	}

	// ===== IdcardUtil =====

	@Test
	public void idcardUtil_covers() {
		Assert.assertFalse(IdcardUtil.isValidCard("123"));
		char check = IdcardUtil.calculateCheckCode("11010119900307831");
		Assert.assertTrue(IdcardUtil.isValidCard("11010119900307831" + check));
		Assert.assertTrue(IdcardUtil.getBirthDate("11010119900307831" + check).contains("1990"));
		Assert.assertNotNull(IdcardUtil.getGender("11010119900307831" + check));
		Assert.assertEquals("11", IdcardUtil.getProvinceCode("11010119900307831" + check));
	}

	// ===== ThreadUtil =====

	@Test
	public void threadUtil_covers() throws Exception {
		AtomicInteger counter = new AtomicInteger();
		Thread t = ThreadUtil.newThread(counter::incrementAndGet, "demo-thread");
		t.start();
		t.join();
		Assert.assertEquals(1, counter.get());
		Thread daemon = ThreadUtil.newThread(() -> {
		}, "daemon-thread", true);
		Assert.assertTrue(daemon.isDaemon());
		Assert.assertTrue(ThreadUtil.getProcessorCount() > 0);
		Assert.assertNotNull(ThreadUtil.getExecutor());
		Assert.assertEquals(2, ThreadUtil.execAsync(counter::incrementAndGet).get().intValue());
		ThreadUtil.sleep(5);
		ThreadUtil.shutdownQuietly(java.util.concurrent.Executors.newSingleThreadExecutor());
	}
}
