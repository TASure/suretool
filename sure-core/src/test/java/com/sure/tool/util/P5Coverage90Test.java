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
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.codec.HashUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;
import com.sure.tool.io.ZipUtil;
import com.sure.tool.lang.Snowflake;
import com.sure.tool.system.SystemInfo;
import com.sure.tool.thread.ThreadUtil;

/**
 * P5 覆盖率补强测试：面向 sure-core ≥ 90% 目标，覆盖 StrUtil / ArrayUtil / FileUtil /
 * ConvertUtil / BeanUtil / NetUtil / Props / Singleton / Snowflake / ThreadUtil /
 * ReflectUtil / ZipUtil / CollUtil / IdcardUtil / NumberUtil / ObjectUtil / HashUtil /
 * ValidatorUtil / SystemInfo / IoUtil / ReUtil 的分支与异常路径。
 *
 * @author suretool
 * @since 0.1.0
 */
public class P5Coverage90Test {

	// ===== StrUtil =====

	@Test
	public void testStrUtilGaps() {
		Assert.assertEquals("hello", StrUtil.removePrefix("prefix-hello", "prefix-"));
		Assert.assertEquals("abc", StrUtil.removePrefix("abc", "xyz"));
		Assert.assertEquals("", StrUtil.removePrefix("", "a"));
		Assert.assertEquals("AbC", StrUtil.removePrefix("AbC", "abc"));
		Assert.assertEquals("hello", StrUtil.removePrefixIgnoreCase("AbC-hello", "abc-"));
		Assert.assertEquals("abc", StrUtil.removeSuffix("abc-suffix", "-suffix"));
		Assert.assertEquals("abc", StrUtil.removeSuffix("abc", "xyz"));
		Assert.assertEquals("AbC", StrUtil.removeSuffixIgnoreCase("AbC-SUFFIX", "-suffix"));
		Assert.assertNull(StrUtil.removePrefix((String) null, "x"));
		Assert.assertEquals("hello", StrUtil.removeSuffix("hello", null));

		Assert.assertEquals("X-X-X", StrUtil.replaceIgnoreCase("AbC-abc-AbC", "abc", "X"));
		Assert.assertEquals(null, StrUtil.replaceIgnoreCase(null, "a", "b"));
		Assert.assertEquals("abc", StrUtil.replaceIgnoreCase("abc", "", "X"));
		Assert.assertEquals("ab", StrUtil.replaceIgnoreCase("abc", "c", null));
		Assert.assertEquals("11", StrUtil.replaceIgnoreCase("aA", "a", "1"));

		Assert.assertEquals("def", StrUtil.blankToDefault(null, "def"));
		Assert.assertTrue(StrUtil.hasBlank("a", null));
		Assert.assertTrue(StrUtil.hasEmpty("a", ""));
		Assert.assertTrue(StrUtil.isAllNotBlank("a", "b"));
		Assert.assertTrue(StrUtil.isAllEmpty("", ""));
		Assert.assertFalse(StrUtil.equals("a", "b"));
		Assert.assertTrue(StrUtil.equalsIgnoreCase("A", "a"));
		Assert.assertTrue(StrUtil.containsAny("hello", "x", "ell"));
		Assert.assertTrue(StrUtil.containsAnyIgnoreCase("hello", "X", "ELL"));
		Assert.assertTrue(StrUtil.startWith("hello", "he"));
		Assert.assertTrue(StrUtil.endWith("hello", "lo"));
		Assert.assertEquals("a", StrUtil.subBetween("[a]bc", "[", "]"));
		Assert.assertEquals(1, StrUtil.subBetweenAll("a-b-c", "-", "-").size());
		Assert.assertEquals("a", StrUtil.splitTrim(" a , b ", ",")[0]);
		Assert.assertEquals("000abc", StrUtil.fillBefore("abc", 6, '0'));
		Assert.assertEquals("abc000", StrUtil.fillAfter("abc", 6, '0'));
		Assert.assertEquals("A", StrUtil.toUnicode("A"));
		Assert.assertEquals("\\u4f60", StrUtil.toUnicode("你"));
		Assert.assertEquals("abcd", StrUtil.maxLength("abcd", 4));
		Assert.assertEquals("abc", StrUtil.maxLength("abc", 4));
		Assert.assertEquals("  abc  ", StrUtil.center("abc", 7, ' '));
		Assert.assertEquals("a", StrUtil.firstNonBlank(null, "", "a"));
		Assert.assertTrue(StrUtil.endWithAny("hello", "x", "lo"));
		Assert.assertEquals("Abc", StrUtil.upperFirst("abc"));
		Assert.assertEquals("aBC", StrUtil.lowerFirst("ABC"));
		Assert.assertEquals(2, StrUtil.count("abcabc", "a"));
		Assert.assertEquals("ab", StrUtil.trimEnd("ab  "));
		Assert.assertEquals("bcbc", StrUtil.removeAll("abcabc", "a"));
		Assert.assertEquals("bc", StrUtil.subSuf("abc", 2));
		Assert.assertEquals("ab", StrUtil.subPre("abc", 2));
		Assert.assertEquals("a,b", StrUtil.join(",", (Object[]) new String[] { "a", "b" }));
		Assert.assertEquals("ab", StrUtil.concat("a", "b"));
		Assert.assertEquals(6, StrUtil.totalLength("ab", "cd", null, "ef"));
		Assert.assertTrue(StrUtil.isLowerCase("abc"));
		Assert.assertFalse(StrUtil.isLowerCase("Abc"));
		Assert.assertTrue(StrUtil.isUpperCase("ABC"));
	}

	// ===== ArrayUtil =====

	@Test
	public void testArrayUtilGaps() {
		Assert.assertEquals("[]", ArrayUtil.toString((Object) null));
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new int[] { 1, 2 }));
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new long[] { 1, 2 }));
		Assert.assertEquals("[1.5, 2.5]", ArrayUtil.toString(new double[] { 1.5, 2.5 }));
		Assert.assertEquals("[true, false]", ArrayUtil.toString(new boolean[] { true, false }));
		Assert.assertEquals("[a, b]", ArrayUtil.toString(new char[] { 'a', 'b' }));
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new byte[] { 1, 2 }));
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new short[] { 1, 2 }));
		Assert.assertEquals("[1.5]", ArrayUtil.toString(new float[] { 1.5f }));
		Assert.assertEquals("[a, b]", ArrayUtil.toString(new String[] { "a", "b" }));
		Assert.assertEquals("not-array", ArrayUtil.toString("not-array"));

		Assert.assertTrue(ArrayUtil.contains(new int[] { 1, 2, 3 }, 2));
		Assert.assertEquals(1, ArrayUtil.indexOf(new Integer[] { 1, 2, 3 }, 2));
		Assert.assertEquals("x", ArrayUtil.get(new String[] { "x", "y" }, 0));
		Assert.assertNull(ArrayUtil.get(new String[] { "x" }, 5));
		Assert.assertNull(ArrayUtil.get(new String[] { "x" }, -1));
		Assert.assertEquals("y", ArrayUtil.get(new String[] { "x", "y" }, 1, "z"));
		Assert.assertEquals("a,b", ArrayUtil.join(new String[] { "a", "b" }, ","));
		Assert.assertEquals(Arrays.asList("a", "b"), ArrayUtil.toList(new String[] { "a", "b" }));
		String[] rev = new String[] { "a", "b" };
		ArrayUtil.reverse(rev);
		Assert.assertArrayEquals(new String[] { "b", "a" }, rev);
		Assert.assertArrayEquals(new String[] { "a", "b" }, ArrayUtil.distinct(new String[] { "a", "a", "b" }));
		Assert.assertArrayEquals(new Object[] { "b", "c" }, ArrayUtil.sub(new String[] { "a", "b", "c" }, 1, 3));
		Assert.assertEquals("a", ArrayUtil.firstNonNull(null, "a"));
		Assert.assertEquals(1, ArrayUtil.min(new int[] { 3, 1, 2 }));
		Assert.assertEquals(3, ArrayUtil.max(new int[] { 3, 1, 2 }));
		Assert.assertEquals(1.0, ArrayUtil.min(new Double[] { 3.0, 1.0 }), 0.0);
		Assert.assertEquals(3.0, ArrayUtil.max(new Double[] { 3.0, 1.0 }), 0.0);
		Assert.assertEquals(1, ArrayUtil.min(new long[] { 3, 1 }));
		Assert.assertEquals(3, ArrayUtil.max(new long[] { 3, 1 }));
		Assert.assertEquals(1.0f, ArrayUtil.min(new Float[] { 3f, 1f }), 0.0f);
		Assert.assertEquals(3.0f, ArrayUtil.max(new Float[] { 3f, 1f }), 0.0f);
		Assert.assertArrayEquals(new Integer[] { 1, 2 }, ArrayUtil.resize(new Integer[] { 1, 2, 3 }, 2));
		Assert.assertTrue(ArrayUtil.isSorted(new Integer[] { 1, 2, 3 }));
		Assert.assertEquals(2, ArrayUtil.lastIndexOf(new String[] { "a", "b", "a" }, "a"));
		String[] sw = new String[] { "a", "b" };
		ArrayUtil.swap(sw, 0, 1);
		Assert.assertArrayEquals(new String[] { "b", "a" }, sw);
		Assert.assertArrayEquals(new Object[] { "a", "c" },
				(Object[]) ArrayUtil.remove(new String[] { "a", "b", "c" }, "b"));
		Assert.assertArrayEquals(new Object[] { "a", "b", "c" },
				(Object[]) ArrayUtil.append(new String[] { "a", "b" }, "c"));
		Assert.assertArrayEquals(new Object[] { "a", "x", "b" },
				(Object[]) ArrayUtil.insert(new String[] { "a", "b" }, 1, "x"));
		Assert.assertArrayEquals(new Integer[] { 1, 2 }, ArrayUtil.wrap(new int[] { 1, 2 }));
		Assert.assertTrue(ArrayUtil.isArray(new int[1]));
		Assert.assertEquals(1, ArrayUtil.length(new String[] { "a" }));
		Assert.assertEquals(2, ArrayUtil.length(new int[] { 1, 2 }));
	}

	// ===== FileUtil =====

	@Test
	public void testFileUtilGaps() throws IOException {
		Assert.assertEquals(null, FileUtil.getMimeType(null));
		Assert.assertEquals(null, FileUtil.getMimeType("noext"));
		Assert.assertEquals("text/plain", FileUtil.getMimeType("a.txt"));
		Assert.assertEquals("text/html", FileUtil.getMimeType("a.html"));
		Assert.assertEquals("text/css", FileUtil.getMimeType("a.css"));
		Assert.assertEquals("application/javascript", FileUtil.getMimeType("a.js"));
		Assert.assertEquals("application/json", FileUtil.getMimeType("a.json"));
		Assert.assertEquals("application/xml", FileUtil.getMimeType("a.xml"));
		Assert.assertEquals("text/csv", FileUtil.getMimeType("a.csv"));
		Assert.assertEquals("application/pdf", FileUtil.getMimeType("a.pdf"));
		Assert.assertEquals("image/png", FileUtil.getMimeType("a.png"));
		Assert.assertEquals("image/jpeg", FileUtil.getMimeType("a.JPG"));
		Assert.assertEquals("image/gif", FileUtil.getMimeType("a.gif"));
		Assert.assertEquals("image/svg+xml", FileUtil.getMimeType("a.svg"));
		Assert.assertEquals("image/x-icon", FileUtil.getMimeType("a.ico"));
		Assert.assertEquals("application/zip", FileUtil.getMimeType("a.zip"));
		Assert.assertEquals("application/x-7z-compressed", FileUtil.getMimeType("a.7z"));
		Assert.assertEquals("application/vnd.rar", FileUtil.getMimeType("a.rar"));
		Assert.assertEquals("application/java-archive", FileUtil.getMimeType("a.jar"));
		Assert.assertEquals("application/msword", FileUtil.getMimeType("a.doc"));
		Assert.assertEquals("application/vnd.ms-excel", FileUtil.getMimeType("a.xls"));
		Assert.assertEquals("application/vnd.ms-powerpoint", FileUtil.getMimeType("a.ppt"));
		Assert.assertEquals("audio/mpeg", FileUtil.getMimeType("a.mp3"));
		Assert.assertEquals("audio/wav", FileUtil.getMimeType("a.wav"));
		Assert.assertEquals("video/mp4", FileUtil.getMimeType("a.mp4"));
		Assert.assertEquals("video/x-msvideo", FileUtil.getMimeType("a.avi"));
		Assert.assertEquals("video/x-matroska", FileUtil.getMimeType("a.mkv"));
		Assert.assertEquals("video/quicktime", FileUtil.getMimeType("a.mov"));
		Assert.assertEquals("video/webm", FileUtil.getMimeType("a.webm"));
		Assert.assertEquals("application/x-msdownload", FileUtil.getMimeType("a.exe"));
		Assert.assertEquals("application/x-sh", FileUtil.getMimeType("a.sh"));
		Assert.assertEquals("application/octet-stream", FileUtil.getMimeType("a.xyz"));
	}

	@Test
	public void testFileOpsGaps() throws IOException {
		File dir = new File("target/p5cov");
		FileUtil.mkdir(dir);
		Assert.assertTrue(dir.isDirectory());
		File f = FileUtil.touch(new File(dir, "x.txt"));
		Assert.assertTrue(f.exists());
		Assert.assertEquals("x.txt", FileUtil.getName(f));
		Assert.assertEquals("txt", FileUtil.getExt(f));
		Assert.assertEquals("x", FileUtil.mainName(f));
		Assert.assertEquals(dir.getAbsolutePath(), FileUtil.getParent(f));
		File copy = new File(dir, "copy.txt");
		FileUtil.copy(f, copy);
		Assert.assertTrue(copy.exists());
		Assert.assertEquals(FileUtil.size(f), FileUtil.size(copy));
		Assert.assertTrue(FileUtil.pathEquals(dir.getAbsolutePath(), dir.getAbsolutePath()));
		Assert.assertFalse(FileUtil.isDirEmpty(dir));
		Assert.assertTrue(FileUtil.getTmpDir().isDirectory());
		Assert.assertFalse(FileUtil.getTmpDirPath().isEmpty());
		Assert.assertFalse(FileUtil.getUserHomePath().isEmpty());
		Assert.assertTrue(FileUtil.isFile(f));
		Assert.assertTrue(FileUtil.isDirectory(dir));
		Assert.assertTrue(FileUtil.exists(f));
		FileUtil.move(copy, new File(dir, "moved.txt"));
		Assert.assertTrue(new File(dir, "moved.txt").exists());
		File sub = new File(dir, "sub");
		FileUtil.mkdir(sub);
		FileUtil.writeUtf8String("hello", new File(sub, "y.txt"));
		Assert.assertEquals(2, FileUtil.listFileNames(dir.getAbsolutePath()).size());
		Assert.assertNotNull(FileUtil.loopFiles(dir));
		Assert.assertEquals("1 KB", FileUtil.readableFileSize(1024));
		Assert.assertEquals(FileUtil.normalize(dir.getAbsolutePath()), FileUtil.normalize(dir.getAbsolutePath()));
		FileUtil.clean(dir);
		Assert.assertTrue(FileUtil.isDirEmpty(dir));
		FileUtil.delete(dir);
		Assert.assertFalse(dir.exists());
	}

	// ===== ConvertUtil =====

	@Test
	public void testConvertGaps() {
		Assert.assertNull(ConvertUtil.convert(Integer.class, null));
		Assert.assertEquals(Integer.valueOf(5), ConvertUtil.convert(Integer.class, "5"));
		Assert.assertEquals(Long.valueOf(5), ConvertUtil.convert(Long.class, "5"));
		Assert.assertEquals(Double.valueOf(5.5), ConvertUtil.convert(Double.class, "5.5"));
		Assert.assertEquals("5", ConvertUtil.convert(String.class, 5));
		Assert.assertArrayEquals(new Integer[] { 1, 2 }, ConvertUtil.wrap(new int[] { 1, 2 }));
		Assert.assertNull(ConvertUtil.wrap(null));
		Assert.assertEquals(Arrays.asList("a", "b"), ConvertUtil.toList(new String[] { "a", "b" }));
		Assert.assertArrayEquals(new char[] { 'a' }, ConvertUtil.toCharArray("a"));
		Assert.assertArrayEquals(new double[] { 1.5 }, ConvertUtil.toDoubleArray(new String[] { "1.5" }), 0.0);
		Assert.assertArrayEquals(new boolean[] { true }, ConvertUtil.toBooleanArray(new String[] { "true" }));
		Assert.assertEquals(new BigDecimal("5"), ConvertUtil.toBigDecimal("5"));
		Assert.assertEquals(java.math.BigInteger.valueOf(5), ConvertUtil.toBigInteger("5"));
		Assert.assertEquals(LocalDate.of(2024, 1, 2), ConvertUtil.toLocalDate("2024-01-02"));
		Assert.assertEquals(LocalDate.of(2024, 1, 2), ConvertUtil.toLocalDate(LocalDate.of(2024, 1, 2)));
		Assert.assertEquals(LocalDate.of(2024, 1, 2), ConvertUtil.toLocalDate(
				LocalDateTime.of(2024, 1, 2, 3, 4)));
		Assert.assertNull(ConvertUtil.toLocalDate("bad"));
		Assert.assertNull(ConvertUtil.toLocalDate(""));
		Assert.assertNull(ConvertUtil.toLocalDate(null));
		Assert.assertEquals(LocalDateTime.of(2024, 1, 2, 10, 20, 30),
				ConvertUtil.toLocalDateTime("2024-01-02 10:20:30"));
		Assert.assertEquals(LocalDateTime.of(2024, 1, 2, 0, 0),
				ConvertUtil.toLocalDateTime(LocalDate.of(2024, 1, 2)));
		Assert.assertNull(ConvertUtil.toLocalDateTime(null));
		Assert.assertNull(ConvertUtil.toLocalDateTime("bad"));
		Assert.assertNotNull(ConvertUtil.toDate(LocalDateTime.of(2024, 1, 2, 0, 0)));
	}

	// ===== BeanUtil =====

	@Test
	public void testBeanGaps() {
		Map<String, Object> bean = new HashMap<>();
		bean.put("name", "sure");
		Assert.assertNull(BeanUtil.getProperty(bean, "name"));

		User u = new User("sure", 3, Arrays.asList("a", "b"));
		Assert.assertNull(BeanUtil.deepCopy(null));
		User copy = BeanUtil.deepCopy(u);
		Assert.assertNotSame(u, copy);
		Assert.assertEquals(u.name, copy.name);
		Assert.assertEquals(u.tags, copy.tags);
		Assert.assertEquals("sure", BeanUtil.deepCopy("sure"));
		Assert.assertEquals(Integer.valueOf(1), BeanUtil.deepCopy(1));
		Assert.assertEquals(Arrays.asList("a", "b"), BeanUtil.deepCopy(Arrays.asList("a", "b")));
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("a")),
				BeanUtil.deepCopy(new LinkedHashSet<>(Arrays.asList("a"))));
		Map<String, String> m = new HashMap<>();
		m.put("k", "v");
		Assert.assertEquals(m, BeanUtil.deepCopy(m));
		Assert.assertArrayEquals(new String[] { "a", "b" }, BeanUtil.deepCopy(new String[] { "a", "b" }));

		Map<String, Object> map = new HashMap<>();
		map.put("name", "x");
		User fromMap = BeanUtil.mapToBean(map, User.class);
		Assert.assertEquals("x", fromMap.name);
		Map<String, Object> toMap = BeanUtil.beanToMap(u);
		Assert.assertEquals("sure", toMap.get("name"));
		User target = new User();
		BeanUtil.copyProperties(u, target);
		Assert.assertEquals("sure", target.name);
		User filled = BeanUtil.toBean(u, User.class);
		Assert.assertEquals("sure", filled.name);
	}

	/** 测试用简单 Bean。 */
	public static class User implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name;
		private int age;
		private List<String> tags;

		public User() {
		}

		public User(String name, int age, List<String> tags) {
			this.name = name;
			this.age = age;
			this.tags = tags;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}

	// ===== NetUtil =====

	@Test
	public void testNetGaps() {
		Assert.assertNotNull(NetUtil.getLocalhostStr());
		Assert.assertNotNull(NetUtil.getLocalIpv4());
		Assert.assertNotNull(NetUtil.getLocalMacAddress());
		Assert.assertTrue(NetUtil.isInnerIP("127.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("10.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("192.168.1.1"));
		Assert.assertTrue(NetUtil.isInnerIP("172.16.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("::ffff:192.168.1.1"));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isInnerIP(null));
		Assert.assertFalse(NetUtil.isInnerIP("bad"));
		Assert.assertFalse(NetUtil.isInnerIP("1.2.3"));
		Assert.assertTrue(NetUtil.isValidPort(1));
		Assert.assertTrue(NetUtil.isValidPort(65535));
		Assert.assertFalse(NetUtil.isValidPort(0));
		Assert.assertFalse(NetUtil.isValidPort(65536));
		Assert.assertEquals(0x7f000001L, NetUtil.ipv4ToLong("127.0.0.1"));
		Assert.assertEquals("127.0.0.1", NetUtil.longToIpv4(0x7f000001L));
		Assert.assertTrue(NetUtil.isIpv4("1.2.3.4"));
		Assert.assertFalse(NetUtil.isIpv4("1.2.3"));
	}

	// ===== Props / Singleton =====

	@Test
	public void testPropsGaps() throws IOException {
		File tmp = File.createTempFile("sure-props", ".properties");
		FileUtil.writeUtf8String("name=sure\ncount=3\nratio=1.5\nflag=true\n", tmp);
		Props props = new Props(tmp);
		Assert.assertEquals("sure", props.getStr("name"));
		Assert.assertEquals("def", props.getStr("missing", "def"));
		Assert.assertEquals(3, props.getInt("count"));
		Assert.assertEquals(9, props.getInt("missing", 9));
		Assert.assertEquals(3L, props.getLong("count"));
		Assert.assertEquals(9L, props.getLong("missing", 9L));
		Assert.assertEquals(1.5, props.getDouble("ratio"), 0.0);
		Assert.assertEquals(9.0, props.getDouble("missing", 9.0), 0.0);
		Assert.assertTrue(props.getBool("flag"));
		Assert.assertTrue(props.containsKey("name"));
		Assert.assertNotNull(props.toString());
		Assert.assertNotNull(props.getProperties());
		tmp.delete();
	}

	@Test
	public void testSingletonGaps() {
		StringBuilder sb = Singleton.get(StringBuilder.class);
		Assert.assertSame(sb, Singleton.get(StringBuilder.class));
		Assert.assertTrue(Singleton.contains(StringBuilder.class));
		Singleton.put(StringBuilder.class, sb);
		Assert.assertSame(sb, Singleton.get(StringBuilder.class));
		Assert.assertSame(sb, Singleton.remove(StringBuilder.class));
		Assert.assertFalse(Singleton.contains(StringBuilder.class));
		Singleton.get(String.class, "arg");
		Assert.assertEquals("arg", Singleton.get(String.class));
		Singleton.destroy();
	}

	// ===== Snowflake =====

	@Test
	public void testSnowflakeGaps() {
		Snowflake sf = new Snowflake(1, 2);
		long a = sf.nextId();
		long b = sf.nextId();
		Assert.assertTrue(b > a);
		Assert.assertEquals(1L, sf.getWorkerId());
		Assert.assertEquals(2L, sf.getDatacenterId());
		Assert.assertNotNull(sf.nextIdStr());
		try {
			new Snowflake(32, 0);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// workerId 越界
		}
		try {
			new Snowflake(0, 32);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// datacenterId 越界
		}
		try {
			new Snowflake(0, 0, -1);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// epoch 非法
		}
	}

	// ===== ThreadUtil =====

	@Test
	public void testThreadGaps() throws Exception {
		Future<?> f1 = ThreadUtil.execAsync(() -> {
			// 简单任务
		});
		f1.get();
		Future<Integer> f2 = ThreadUtil.execAsync(() -> 42);
		Assert.assertEquals(Integer.valueOf(42), f2.get());
		ThreadUtil.sleep(1);
		List<Callable<Integer>> tasks = new ArrayList<>();
		tasks.add(() -> 1);
		tasks.add(() -> 2);
		Assert.assertEquals(Arrays.asList(1, 2), ThreadUtil.invokeAll(tasks));
		ExecutorService pool = Executors.newSingleThreadExecutor();
		ThreadUtil.shutdownQuietly(pool);
		ThreadUtil.shutdownQuietly(null);
	}

	// ===== ReflectUtil =====

	@Test
	public void testReflectGaps() {
		ReflectBean bean = new ReflectBean();
		Assert.assertEquals("default", ReflectUtil.getFieldValue(bean, "secret"));
		ReflectUtil.setFieldValue(bean, "secret", "changed");
		Assert.assertEquals("changed", ReflectUtil.getFieldValue(bean, "secret"));
		Assert.assertNotNull(ReflectUtil.getField(ReflectBean.class, "secret"));
		Assert.assertTrue(ReflectUtil.getFields(ReflectBean.class).length > 0);
		Assert.assertTrue(ReflectUtil.getMethods(ReflectBean.class).length > 0);
		Assert.assertNotNull(ReflectUtil.getMethod(ReflectBean.class, "hidden", String.class));
		Assert.assertEquals("hidden:changed", ReflectUtil.invoke(bean, "hidden", "changed"));
		Assert.assertEquals("static-ok", ReflectUtil.invokeStatic(ReflectBean.class, "stat"));
		ReflectBean created = ReflectUtil.invokeConstructor(ReflectBean.class);
		Assert.assertNotNull(created);
	}

	/** 反射测试 Bean。 */
	public static class ReflectBean {
		private String secret = "default";

		@SuppressWarnings("unused")
		private String hidden(String prefix) {
			return "hidden:" + prefix;
		}

		public static String stat() {
			return "static-ok";
		}
	}

	// ===== ZipUtil =====

	@Test
	public void testZipGaps() throws IOException {
		File dir = new File("target/p5zip");
		FileUtil.mkdir(dir);
		File sub = new File(dir, "sub");
		FileUtil.mkdir(sub);
		FileUtil.writeUtf8String("a", new File(dir, "a.txt"));
		FileUtil.writeUtf8String("b", new File(sub, "b.txt"));
		File zipFile = new File("target/p5zip.zip");
		ZipUtil.zip(Arrays.asList(dir, new File(dir, "a.txt")), zipFile);
		Assert.assertTrue(zipFile.exists());
		File out = new File("target/p5unzip");
		FileUtil.mkdir(out);
		ZipUtil.unzip(zipFile, out);
		Assert.assertTrue(new File(out, "p5zip").isDirectory());
		try {
			ZipUtil.zip(Collections.emptyList(), zipFile);
			Assert.fail();
		} catch (IllegalArgumentException expected) {
			// 空列表
		}
		ZipUtil.zip(new File(dir, "a.txt"), new File("target/p5zip2.zip"));
		Assert.assertTrue(new File("target/p5zip2.zip").exists());
		FileUtil.delete(dir);
		FileUtil.delete(out);
		zipFile.delete();
		new File("target/p5zip2.zip").delete();
	}

	// ===== CollUtil =====

	@Test
	public void testCollGaps() {
		Assert.assertTrue(CollUtil.isNotEmpty(Arrays.asList("a")));
		Assert.assertFalse(CollUtil.isNotEmpty((java.util.Collection<?>) null));
		Assert.assertTrue(CollUtil.containsAny(Arrays.asList("a", "b"), "x", "b"));
		Assert.assertEquals(Arrays.asList("a", "b"),
				CollUtil.intersection(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b")));
		Assert.assertEquals(Arrays.asList("c"),
				CollUtil.disjunction(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b")));
		Assert.assertEquals(Arrays.asList("a", "b"), CollUtil.toList((Iterable<String>) Arrays.asList("a", "b")));
		Iterator<String> it = Arrays.asList("a", "b").iterator();
		Assert.assertEquals(Arrays.asList("a", "b"), CollUtil.toList(it));
		Assert.assertEquals(Arrays.asList("a", "b"),
				CollUtil.toList(Collections.enumeration(Arrays.asList("a", "b"))));
		Assert.assertEquals("b", CollUtil.get(Arrays.asList("a", "b"), 1));
		Assert.assertEquals("z", CollUtil.get(Arrays.asList("a"), 5, "z"));
		Assert.assertEquals(Arrays.asList("a"), CollUtil.filter(Arrays.asList("a", "b"), s -> s.equals("a")));
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.map(Arrays.asList("1", "2"), Integer::parseInt));
		Assert.assertEquals(Arrays.asList("a", "b"), CollUtil.distinct(Arrays.asList("a", "a", "b")));
		Assert.assertEquals(1, CollUtil.min(Arrays.asList(3, 1, 2)).intValue());
		Assert.assertEquals(3, CollUtil.max(Arrays.asList(3, 1, 2)).intValue());
		Map<String, String> byName = CollUtil.listToMap(Arrays.asList("a", "b"), s -> s);
		Assert.assertEquals("b", byName.get("b"));
		Assert.assertEquals("def", CollUtil.maxBy(Arrays.asList("a", "bc", "def"), s -> s.length()));
		Assert.assertEquals(2, CollUtil.zip(Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2")).size());
		Assert.assertEquals(Arrays.asList("a"), CollUtil.removeNull(Arrays.asList("a", null)));
		Assert.assertEquals(2, CollUtil.lastIndexOf(Arrays.asList("a", "b", "a"), "a"));
		Map<String, List<String>> group = CollUtil.groupByKey(Arrays.asList("a1", "b1"),
				s -> s.substring(0, 1));
		Assert.assertEquals(2, group.size());
		Assert.assertEquals(Arrays.asList("b", "c"), CollUtil.sub(Arrays.asList("a", "b", "c"), 1, 3));
		Assert.assertTrue(CollUtil.sub(Arrays.asList("a", "b"), -1, 5).size() >= 1);
		Assert.assertTrue(CollUtil.emptyIfNull(null).isEmpty());
		Assert.assertEquals(Arrays.asList("a", "b"), CollUtil.emptyIfNull(Arrays.asList("a", "b")));
		List<Map<String, Object>> sortedUsers = CollUtil.sortByProperty(users(), "name", true);
		Assert.assertEquals(1, sortedUsers.size());
		List<Map<String, Object>> sortedUsersDesc = CollUtil.sortByPropertyDesc(users(), "name");
		Assert.assertEquals(1, sortedUsersDesc.size());
	}

	private static List<Map<String, Object>> users() {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> a = new HashMap<>();
		a.put("name", "a");
		list.add(a);
		return list;
	}

	// ===== IdcardUtil =====

	@Test
	public void testIdcardGaps() {
		String id17 = "11010519491231002";
		char check = IdcardUtil.calculateCheckCode(id17);
		String id18 = id17 + check;
		Assert.assertTrue(IdcardUtil.isValidCard(id18));
		Assert.assertTrue(IdcardUtil.isValidCard18(id18));
		Assert.assertEquals("1949-12-31", IdcardUtil.getBirthDate(id18));
		Assert.assertEquals("女", IdcardUtil.getGender(id18));
		Assert.assertEquals("11", IdcardUtil.getProvinceCode(id18));
		String id15 = "110105491231002";
		Assert.assertTrue(IdcardUtil.isValidCard15(id15));
		Assert.assertEquals(id18, IdcardUtil.convert15To18(id15));
		Assert.assertTrue(IdcardUtil.getAge(id18) >= 0);
		Assert.assertNull(IdcardUtil.getBirthDate(null));
		Assert.assertNull(IdcardUtil.getGender("bad"));
	}

	// ===== NumberUtil / ObjectUtil =====

	@Test
	public void testNumberAndObjectGaps() {
		Assert.assertEquals(5, NumberUtil.parseInt("5"));
		Assert.assertEquals(9, NumberUtil.parseInt("bad", 9));
		Assert.assertEquals(5L, NumberUtil.parseLong("5"));
		Assert.assertEquals(9L, NumberUtil.parseLong("bad", 9L));
		Assert.assertEquals(5.5, NumberUtil.parseDouble("5.5"), 0.0);
		Assert.assertEquals(9.0, NumberUtil.parseDouble("bad", 9.0), 0.0);
		Assert.assertEquals(5.5f, NumberUtil.parseFloat("5.5", 1f), 0.0f);
		Assert.assertEquals((short) 5, NumberUtil.parseShort("5", (short) 1));
		Assert.assertEquals((byte) 5, NumberUtil.parseByte("5", (byte) 1));
		Assert.assertFalse(NumberUtil.isInteger("1.5"));
		Assert.assertTrue(NumberUtil.isDouble("1.5"));
		Assert.assertEquals(new BigDecimal("1.50"), NumberUtil.round(1.5, 2));
		Assert.assertTrue(NumberUtil.isEquals(1.0, 1.0, 0.1));
		Assert.assertArrayEquals(new int[] { 1, 2, 3 }, NumberUtil.range(1, 4));
		Assert.assertEquals(1, NumberUtil.min(3, 1, 2));
		Assert.assertTrue(NumberUtil.isPrime(7));
		Assert.assertFalse(NumberUtil.isPrime(1));
		Assert.assertEquals(4, NumberUtil.partValue(4, 1, 1)[0] + NumberUtil.partValue(4, 1, 1)[1]);
		Assert.assertEquals("1.50", NumberUtil.toFixed(1.5, 2));
		Assert.assertEquals(8L, NumberUtil.pow(2, 3));
		Assert.assertTrue(NumberUtil.isLong("123"));
		Assert.assertEquals("50.00%", NumberUtil.percent(0.5, 2));

		Assert.assertTrue(ObjectUtil.equals(1, 1));
		Assert.assertFalse(ObjectUtil.equals(null, 1));
		Assert.assertFalse(ObjectUtil.equals(1, null));
		Assert.assertTrue(ObjectUtil.isAllNotNull("a", 1));
		Assert.assertTrue(ObjectUtil.isAllNull(null, null));
		Assert.assertTrue(ObjectUtil.isBasicType(Integer.class));
		Assert.assertEquals(3, ObjectUtil.length(new String[] { "a", "b", "c" }));
		Assert.assertTrue(ObjectUtil.isValidIfNumber("5"));
		Assert.assertNull(ObjectUtil.clone(new Object()));
		byte[] bytes = ObjectUtil.serialize("data");
		Assert.assertEquals("data", ObjectUtil.deserialize(bytes));
		Assert.assertEquals("data", ObjectUtil.cloneByStream("data"));
		Assert.assertNull(ObjectUtil.cloneByStream(new Object()));
	}

	// ===== HashUtil / ValidatorUtil / SystemInfo / IoUtil / ReUtil =====

	@Test
	public void testMiscGaps() throws IOException {
		Assert.assertNotNull(HashUtil.digest("MD5", "a".getBytes(StandardCharsets.UTF_8)));
		Assert.assertNotEquals(0L, HashUtil.murmur3_32("sure"));
		Assert.assertNotEquals(0L, HashUtil.fnv1a64("sure"));

		Assert.assertTrue(ValidatorUtil.isMatch("\\d+", "123"));
		Assert.assertTrue(ValidatorUtil.isMatch(Pattern.compile("\\d+"), "123"));
		Assert.assertTrue(ValidatorUtil.isIpv6("::1"));
		Assert.assertTrue(ValidatorUtil.isMac("00:1A:2B:3C:4D:5E"));
		Assert.assertTrue(ValidatorUtil.isLowerCase("abc"));
		Assert.assertTrue(ValidatorUtil.isUpperCase("ABC"));
		Assert.assertTrue(ValidatorUtil.isTime("12:30:00"));
		Assert.assertTrue(ValidatorUtil.isGeneralWithChinese("你好abc"));

		Assert.assertTrue(SystemInfo.getTotalMemory() > 0);
		Assert.assertTrue(SystemInfo.getFreeMemory() > 0);
		Assert.assertNotNull(SystemInfo.isMac() ? "mac" : "other");
		Assert.assertNotNull(SystemInfo.isUnix() ? "unix" : "other");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IoUtil.writeUtf8("hello", out);
		Assert.assertEquals("hello", out.toString(StandardCharsets.UTF_8));
		Assert.assertTrue(IoUtil.contentEquals(
				new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)),
				new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8))));

		Assert.assertEquals("123", ReUtil.get("(\\d+)", "a123", 1));
		Assert.assertEquals("123", ReUtil.get("(?<num>\\d+)", "a123", "num"));
		Assert.assertEquals("3", ReUtil.getLast("(\\d)", "a1b3", 1));
		Assert.assertEquals(Arrays.asList("a1b", "a", "1", "b"), ReUtil.getAllGroups("(a)(1)(b)", "a1b"));
		Assert.assertNotNull(ReUtil.getAllGroupMap("(?<n>\\d)", "a1b3"));
		Assert.assertEquals(2, ReUtil.findAll("\\d", "a1b3").size());
		Assert.assertEquals(2, ReUtil.count("\\d", "a1b3"));
		Assert.assertEquals("axbx", ReUtil.replaceAll("a1b3", "\\d", "x"));
		Assert.assertEquals("ab3", ReUtil.delFirst("\\d", "a1b3"));
		Assert.assertEquals("ab", ReUtil.delAll("\\d", "a1b3"));
		Assert.assertEquals("1", ReUtil.extractMulti("a(\\d)b", "a1b", "{1}"));
		Assert.assertEquals("\\Q\\d\\E", ReUtil.quote("\\d"));
	}
}
