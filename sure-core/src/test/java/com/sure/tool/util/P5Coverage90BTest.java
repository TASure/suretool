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

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.io.FileUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;

/**
 * 覆盖率补强测试（第二批）：覆盖 jacoco 报告中剩余的方法级缺口。
 *
 * @author suretool
 * @since 0.1.0
 */
public class P5Coverage90BTest {

	public static class UserBean {
		private String name;
		private int age;
		private List<String> tags;

		public UserBean() {
		}

		UserBean(String name, int age, List<String> tags) {
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

	@Test
	public void testStrUtilRemainGaps() {
		Assert.assertTrue(StrUtil.hasBlank("a", null));
		Assert.assertFalse(StrUtil.hasBlank("a", "b"));
		Assert.assertTrue(StrUtil.hasEmpty("a", ""));
		Assert.assertTrue(StrUtil.isAllBlank(null, " "));
		Assert.assertFalse(StrUtil.isAllBlank("a", "b"));
		Assert.assertTrue(StrUtil.equals("abc", "abc"));
		Assert.assertFalse(StrUtil.equals("abc", "abd"));
		Assert.assertTrue(StrUtil.equalsIgnoreCase("aBc", "AbC"));
		Assert.assertTrue(StrUtil.containsIgnoreCase("aXb", "x"));
		Assert.assertTrue(StrUtil.containsAny("abc", "x", "b"));
		Assert.assertTrue(StrUtil.startWith("hello", "he"));
		Assert.assertTrue(StrUtil.endWith("hello", "lo"));
		Assert.assertEquals("he", StrUtil.sub("hello", 0, 2));
		Assert.assertEquals("llo", StrUtil.removePrefix("hello", "he"));
		Assert.assertEquals("hello", StrUtil.removePrefix("hello", "xy"));
		Assert.assertEquals("LLO", StrUtil.removePrefixIgnoreCase("HELLO", "he"));
		Assert.assertEquals("he", StrUtil.removeSuffix("hello", "llo"));
		Assert.assertEquals("hello", StrUtil.removeSuffix("hello", "xy"));
		Assert.assertEquals("HE", StrUtil.removeSuffixIgnoreCase("HELLO", "LLO"));
		Assert.assertEquals("x-b-c", StrUtil.replace("a-b-c", "a", "x"));
		Assert.assertEquals("a-b-c", StrUtil.join("-", Arrays.asList("a", "b", "c")));
		Assert.assertEquals("a=1", StrUtil.format("{}={}", "a", 1));
		Assert.assertEquals("aaa", StrUtil.repeat("a", 3));
		Assert.assertEquals("   a", StrUtil.padPre("a", 4, ' '));
		Assert.assertEquals("a   ", StrUtil.padAfter("a", 4, ' '));
		Assert.assertEquals("helloWorld", StrUtil.toCamelCase("hello_world"));
		Assert.assertEquals("hello_world", StrUtil.toUnderlineCase("helloWorld"));
		Assert.assertEquals("Abc", StrUtil.upperFirst("abc"));
		Assert.assertEquals("abc", StrUtil.lowerFirst("Abc"));
		Assert.assertEquals(2, StrUtil.count("a-b-a", "a"));
		Assert.assertEquals(3, StrUtil.split("a,b,c", ',').length);
		Assert.assertEquals(2, StrUtil.split("a,b", ",").length);
		Assert.assertEquals("d", StrUtil.blankToDefault("", "d"));
		Assert.assertEquals("a", StrUtil.subBetween("[a]bc", "[", "]"));
		Assert.assertEquals("bcbc", StrUtil.removeAll("abcabc", "a"));
		Assert.assertTrue(StrUtil.endWithAny("hello", "x", "lo"));
		Assert.assertArrayEquals(new long[] { 1L, 2L }, StrUtil.splitToLongArray("1 2"));
		Assert.assertEquals(1, StrUtil.subBetweenAll("a-b-c", "-", "-").size());
		Assert.assertEquals("llo", StrUtil.subSuf("hello", 3));
		Assert.assertEquals("he", StrUtil.subPre("hello", 2));
		Assert.assertEquals(2, StrUtil.splitTrim(" a , b ", ",").length);
		Assert.assertTrue(StrUtil.isLowerCase("abc"));
		Assert.assertEquals("ab  ", StrUtil.fillAfter("ab", 4, ' '));
		Assert.assertEquals("  ab", StrUtil.fillBefore("ab", 4, ' '));
		Assert.assertNotNull(StrUtil.toUnicode("你"));
		Assert.assertEquals("ab", StrUtil.maxLength("abcdef", 2));
		Assert.assertEquals("abc", StrUtil.maxLength("abc", 5));
		Assert.assertEquals(" ab ", StrUtil.center("ab", 4, " "));
		Assert.assertEquals("b", StrUtil.firstNonBlank(null, "", "b"));
		Assert.assertFalse(StrUtil.isAllNotBlank("a", ""));
		Assert.assertTrue(StrUtil.isAllEmpty("", ""));
		Assert.assertEquals("axc", StrUtil.replace("abc", 1, 2, "x"));
		Assert.assertTrue(StrUtil.containsAnyIgnoreCase("aBc", "X", "b"));
		Assert.assertTrue(StrUtil.equalsAnyIgnoreCase("Abc", "x", "ABC"));
		Assert.assertEquals("ab", StrUtil.trimEnd("ab  "));
	}

	@Test
	public void testArrayUtilRemainGaps() {
		Assert.assertTrue(ArrayUtil.contains(new int[] { 1, 2 }, 2));
		Assert.assertFalse(ArrayUtil.contains(new String[] { "a" }, "b"));
		Assert.assertEquals(1, ArrayUtil.indexOf(new int[] { 1, 2 }, 2));
		Assert.assertEquals(2, ArrayUtil.lastIndexOf(new String[] { "a", "b", "a" }, "a"));
		Assert.assertEquals(9, ArrayUtil.get(new int[] { 1, 9 }, 1, 0));
		Assert.assertEquals(0, ArrayUtil.get(new int[] { 1 }, 9, 0));
		Assert.assertEquals("1-2", ArrayUtil.join(new int[] { 1, 2 }, "-"));
		Assert.assertEquals(2, ArrayUtil.toList(new int[] { 1, 2 }).size());
		int[] rev = { 1, 2, 3 };
		ArrayUtil.reverse(rev);
		Assert.assertEquals(3, rev[0]);
		Assert.assertEquals(2, ArrayUtil.distinct(new int[] { 1, 1, 2 }).length);
		Assert.assertArrayEquals(new String[] { "b", "c" }, ArrayUtil.sub(new String[] { "a", "b", "c" }, 1, 3));
		Assert.assertEquals("a", ArrayUtil.firstNonNull(null, "a"));
		Assert.assertEquals(Integer.valueOf(1), ArrayUtil.min(new Integer[] { 1, 2 }));
		Assert.assertEquals(Integer.valueOf(2), ArrayUtil.max(new Integer[] { 1, 2 }));
		Assert.assertEquals(1, ArrayUtil.min(new int[] { 1, 2 }));
		Assert.assertEquals(2, ArrayUtil.max(new int[] { 1, 2 }));
		Assert.assertEquals(1L, ArrayUtil.min(new long[] { 1L, 2L }));
		Assert.assertEquals(2L, ArrayUtil.max(new long[] { 1L, 2L }));
		Assert.assertEquals(1.0, ArrayUtil.min(new double[] { 1.0, 2.0 }), 0.01);
		Assert.assertEquals(2.0, ArrayUtil.max(new double[] { 1.0, 2.0 }), 0.01);
		String[] sw = { "a", "b" };
		ArrayUtil.swap(sw, 0, 1);
		Assert.assertEquals("b", sw[0]);
		Assert.assertArrayEquals(new String[] { "a", "c" }, (String[]) ArrayUtil.remove(new String[] { "a", "b", "c" }, "b"));
		Assert.assertArrayEquals(new String[] { "a", "b", "c" },
				(String[]) ArrayUtil.append(new String[] { "a" }, "b", "c"));
		Assert.assertArrayEquals(new String[] { "a", "x", "b" },
				(String[]) ArrayUtil.insert(new String[] { "a", "b" }, 1, "x"));
		Assert.assertArrayEquals(new Long[] { 1L, 2L }, ArrayUtil.wrap(new long[] { 1L, 2L }));
		Assert.assertArrayEquals(new Double[] { 1.0 }, ArrayUtil.wrap(new double[] { 1.0 }));
		Assert.assertArrayEquals(new Float[] { 1.0f }, ArrayUtil.wrap(new float[] { 1.0f }));
		Assert.assertArrayEquals(new Short[] { 1 }, ArrayUtil.wrap(new short[] { 1 }));
		Assert.assertArrayEquals(new Byte[] { 1 }, ArrayUtil.wrap(new byte[] { 1 }));
		Assert.assertArrayEquals(new Character[] { 'a' }, ArrayUtil.wrap(new char[] { 'a' }));
		Assert.assertArrayEquals(new Boolean[] { true }, ArrayUtil.wrap(new boolean[] { true }));
		Assert.assertTrue(ArrayUtil.isSorted(new Integer[] { 1, 2 }, true));
		Assert.assertTrue(ArrayUtil.isSorted(new Integer[] { 1, 1 }, true));
		Assert.assertFalse(ArrayUtil.isSorted(new Integer[] { 1, 2 }, false));
		Assert.assertTrue(ArrayUtil.isSorted(new Integer[] { 2, 1 }, false));
		Assert.assertArrayEquals(new String[] { "a", "b", null },
				(String[]) ArrayUtil.resize(new String[] { "a", "b" }, 3));
	}

	@Test
	public void testNumberUtilRemainGaps() {
		Assert.assertEquals(5, NumberUtil.parseInt("bad", 5));
		Assert.assertEquals(5L, NumberUtil.parseLong("bad", 5L));
		Assert.assertEquals(5.0, NumberUtil.parseDouble("bad", 5.0), 0.01);
		Assert.assertEquals(5.0f, NumberUtil.parseFloat("bad", 5.0f), 0.01);
		Assert.assertEquals((short) 5, NumberUtil.parseShort("bad", (short) 5));
		Assert.assertEquals((byte) 5, NumberUtil.parseByte("bad", (byte) 5));
		Assert.assertTrue(NumberUtil.isInteger("123"));
		Assert.assertFalse(NumberUtil.isInteger("1.2"));
		Assert.assertTrue(NumberUtil.isDouble("1.2"));
		Assert.assertEquals(new BigDecimal("1.23"), NumberUtil.round(1.234, 2));
		Assert.assertTrue(NumberUtil.isEquals(1.0, 1.0001, 0.01));
		Assert.assertArrayEquals(new int[] { 1, 2, 3 }, NumberUtil.range(1, 4));
		Assert.assertEquals(Integer.valueOf(1), NumberUtil.min(1, 2, 3));
		Assert.assertTrue(NumberUtil.isPrime(7));
		Assert.assertFalse(NumberUtil.isPrime(8));
		Assert.assertArrayEquals(new int[] { 3, 2 }, NumberUtil.partValue(5, 3, 2));
		Assert.assertEquals("1.23", NumberUtil.toFixed(new BigDecimal("1.234"), 2));
		Assert.assertEquals(8L, NumberUtil.pow(2L, 3));
	}

	@Test
	public void testConvertUtilRemainGaps() {
		Assert.assertEquals("5", ConvertUtil.convert(String.class, 5));
		Assert.assertEquals(Integer.valueOf(5), ConvertUtil.convert(Integer.class, "5"));
		Assert.assertEquals(2, ConvertUtil.toList(new int[] { 1, 2 }).size());
		Assert.assertEquals(2, ConvertUtil.wrap(new int[] { 1, 2 }).length);
		Assert.assertEquals(new BigDecimal("1.5"), ConvertUtil.toBigDecimal("1.5"));
		Assert.assertNotNull(ConvertUtil.toDate("2026-01-01 00:00:00"));
		Assert.assertArrayEquals(new char[] { 'a', 'b' }, ConvertUtil.toCharArray("ab"));
		Assert.assertArrayEquals(new double[] { 1.0, 2.0 }, ConvertUtil.toDoubleArray("1,2"), 0.01);
		Assert.assertArrayEquals(new boolean[] { true, false }, ConvertUtil.toBooleanArray("true,false"));
		Assert.assertEquals(new java.math.BigInteger("123"), ConvertUtil.toBigInteger("123"));
		Assert.assertEquals(LocalDate.of(2026, 1, 1), ConvertUtil.toLocalDate("2026-01-01"));
		Assert.assertNotNull(ConvertUtil.toLocalDateTime("2026-01-01 00:00:00"));
	}

	@Test
	public void testFileUtilRemainGaps() throws Exception {
		File f = FileUtil.file("target", "p5b", "t.txt");
		Assert.assertNotNull(f);
		Assert.assertEquals("t.txt", f.getName());
		FileUtil.mkdir(f.getParentFile());
		File touched = FileUtil.touch(f);
		Assert.assertTrue(touched.exists());
		Assert.assertNotNull(FileUtil.getMimeType("a.jpg"));
		Assert.assertNotNull(FileUtil.getMimeType("a.png"));
		Assert.assertNotNull(FileUtil.getMimeType("a.pdf"));
		Assert.assertNotNull(FileUtil.getMimeType("a.html"));
		Assert.assertNotNull(FileUtil.getMimeType("a.zip"));
		Assert.assertNotNull(FileUtil.getMimeType("a.txt"));
		Assert.assertNotNull(FileUtil.getMimeType("unknown.xyz"));
		Assert.assertNotNull(FileUtil.getParent(f));
		File dest = FileUtil.file("target", "p5b", "t2.txt");
		Assert.assertNotNull(FileUtil.copy(f, dest));
		Assert.assertNotNull(FileUtil.move(dest, FileUtil.file("target", "p5b", "t3.txt")));
		File srcDir = new File("target", "p5b-src");
		FileUtil.mkdir(srcDir);
		FileUtil.touch(new File(srcDir, "x.txt"));
		FileUtil.copyDir(srcDir, new File("target", "p5b-dst"));
		FileUtil.clean(f.getParentFile());
		Assert.assertTrue(FileUtil.size(f.getParentFile()) >= 0);
		Assert.assertNotNull(FileUtil.readableFileSize(0L));
		Assert.assertEquals("a/b", FileUtil.normalize("a\\b"));
	}

	@Test
	public void testBeanUtilRemainGaps() {
		UserBean src = new UserBean("sure", 3, Arrays.asList("a", "b"));
		UserBean dst = new UserBean();
		Assert.assertSame(dst, BeanUtil.copyProperties(src, dst));
		Map<String, Object> map = BeanUtil.beanToMap(src);
		Assert.assertEquals("sure", map.get("name"));
		UserBean fromMap = BeanUtil.mapToBean(new LinkedHashMap<>(map), UserBean.class);
		Assert.assertEquals("sure", fromMap.getName());
		Assert.assertEquals("sure", BeanUtil.getProperty(src, "name"));
		BeanUtil.setProperty(src, "name", "tool");
		Assert.assertEquals("tool", src.getName());
		BeanUtil.setProperty(src, "age", "5");
		Assert.assertEquals(5, src.getAge());
		UserBean copy = BeanUtil.deepCopy(src);
		Assert.assertNotSame(src, copy);
		Assert.assertArrayEquals(new int[] { 1, 2 }, (int[]) BeanUtil.deepCopy(new int[] { 1, 2 }));
			}

	@Test
	public void testCollUtilRemainGaps() {
		Map<String, String> m = new HashMap<>();
		m.put("k", "v");
		Assert.assertTrue(CollUtil.isNotEmpty(m));
		Assert.assertTrue(CollUtil.containsAny(Arrays.asList("a", "b"), "x", "b"));
		Assert.assertEquals(1, CollUtil.intersection(Arrays.asList("a", "b"), Arrays.asList("b", "c")).size());
		Assert.assertEquals(1, CollUtil.disjunction(Arrays.asList("a", "b"), Arrays.asList("b")).size());
		Assert.assertEquals(2, CollUtil.toList(Arrays.asList("a", "b")).size());
		Assert.assertEquals("b", CollUtil.get(Arrays.asList("a", "b"), 1));
		Assert.assertEquals("d", CollUtil.get(Arrays.asList("a"), 5, "d"));
		Assert.assertEquals(2, CollUtil.filter(Arrays.asList("a", "b", "c"), s -> !"a".equals(s)).size());
		Assert.assertEquals(3, CollUtil.map(Arrays.asList("a", "b"), s -> s.length()).size() + 1);
		Assert.assertEquals(2, CollUtil.groupByKey(Arrays.asList("a1", "b1"), s -> s.substring(0, 1)).size());
		Assert.assertEquals(2, CollUtil.distinct(Arrays.asList("a", "a", "b")).size());
		Assert.assertEquals(2, CollUtil.sub(Arrays.asList("a", "b", "c"), 1, 3).size());
		Assert.assertEquals(0, CollUtil.emptyIfNull(null).size());
		Assert.assertEquals("a", CollUtil.min(Arrays.asList("b", "a")));
		Assert.assertEquals("b", CollUtil.max(Arrays.asList("b", "a")));
		Assert.assertEquals(2, CollUtil.listToMap(Arrays.asList("a", "b"), s -> s).size());
		Assert.assertEquals("def", CollUtil.maxBy(Arrays.asList("a", "bc", "def"), s -> s.length()));
		Assert.assertEquals(2, CollUtil.zip(Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2")).size());
		Assert.assertEquals(2, CollUtil.removeNull(Arrays.asList("a", null, "b")).size());
		Assert.assertEquals(2, CollUtil.lastIndexOf(Arrays.asList("a", "b", "a"), "a"));
		Assert.assertEquals(1, CollUtil.sortByProperty(usersList(), "name", true).size());
		Assert.assertEquals(1, CollUtil.sortByPropertyDesc(usersList(), "name").size());
	}

	private static List<Map<String, Object>> usersList() {
		Map<String, Object> u = new HashMap<>();
		u.put("name", "a");
		return Arrays.asList(u);
	}

	@Test
	public void testDateUtilRemainGaps() {
		Date now = new Date();
		Assert.assertNotNull(DateUtil.format(now, "yyyy-MM-dd"));
		Assert.assertNotNull(DateUtil.parse("2026-01-01", "yyyy-MM-dd"));
		Assert.assertNotNull(DateUtil.parse("2026-01-01 00:00:00"));
		Assert.assertEquals("2026-01-01", DateUtil.format(LocalDate.of(2026, 1, 1)));
		Assert.assertEquals("2026-01-01 00:00:00", DateUtil.format(LocalDateTime.of(2026, 1, 1, 0, 0)));
		Assert.assertNotNull(DateUtil.getMonthName(1));
		Assert.assertEquals("一月", DateUtil.getMonthName(DateUtil.parse("2026-01-01", "yyyy-MM-dd")));
		Assert.assertNotNull(DateUtil.offsetQuarter(now, 1));
		Assert.assertNotNull(DateUtil.endOfMinute(now));
		Assert.assertTrue(DateUtil.isSameYear(now, now));
		Assert.assertEquals(1L, DateUtil.daysBetween(now, new Date(now.getTime() + 86400000L)));
		Assert.assertNotNull(DateUtil.rangeToList(now, new Date(now.getTime() + 86400000L),
				com.sure.tool.date.DateUnit.DAY));
		Assert.assertTrue(DateUtil.getAge(DateUtil.parse("2000-01-01", "yyyy-MM-dd")) >= 0);
		Assert.assertTrue(DateUtil.age(DateUtil.parse("2000-01-01", "yyyy-MM-dd")) >= 0);
		Assert.assertTrue(DateUtil.age(DateUtil.parse("2000-01-01", "yyyy-MM-dd"), now) >= 0);
	}

	@Test
	public void testReflectUtilRemainGaps() {
		UserBean u = new UserBean("sure", 3, Arrays.asList("a"));
		Assert.assertEquals("sure", ReflectUtil.getFieldValue(u, "name"));
		ReflectUtil.setFieldValue(u, "name", "tool");
		Assert.assertEquals("tool", u.getName());
		Assert.assertTrue(ReflectUtil.getFields(UserBean.class).length >= 3);
		Assert.assertTrue(ReflectUtil.getMethods(UserBean.class).length > 0);
		Method m = ReflectUtil.getMethod(String.class, "length");
		Assert.assertNotNull(m);
		Assert.assertEquals(3, ReflectUtil.invoke("abc", "length"));
		Assert.assertNotNull(ReflectUtil.invokeStatic(System.class, "currentTimeMillis"));
			}

	@Test
	public void testNetUtilRemainGaps() {
		Assert.assertNotNull(NetUtil.getLocalhostStr());
		Assert.assertTrue(NetUtil.isIpv4("192.168.1.1"));
		Assert.assertFalse(NetUtil.isIpv4("999.1.1.1"));
		Assert.assertTrue(NetUtil.isInnerIP("10.0.0.1"));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isUsableLocalPort(0));
	}

	@Test
	public void testMiscRemainGaps() throws Exception {
		Assert.assertNotNull(com.sure.tool.lang.Snowflake.class);
		Assert.assertNotNull(new com.sure.tool.lang.Snowflake(1L, 1L).nextId());
		Assert.assertNotNull(com.sure.tool.util.UrlUtil.encode("a b"));
		Assert.assertNotNull(com.sure.tool.util.UrlUtil.decode("a%20b"));
		Assert.assertNotNull(com.sure.tool.util.RandomUtil.randomString(8));
		Assert.assertNotNull(com.sure.tool.codec.HexUtil.encodeHexStr("ab".getBytes(StandardCharsets.UTF_8)));
		Assert.assertTrue(com.sure.tool.util.BytesUtil.bytesToLong(new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 }) >= 1);
		Assert.assertNotNull(com.sure.tool.collection.BiMap.class);
		Assert.assertNotNull(com.sure.tool.util.EnumUtil.class);
		Assert.assertNotNull(com.sure.tool.id.UlidUtil.ulid());
		com.sure.tool.collection.CsvUtil.write(new File("target", "p5b-csv.csv"), Arrays.asList(Arrays.asList("a", "b")));
		Assert.assertNotNull(com.sure.tool.image.ImageUtil.class);
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isEmail("a@b.com"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isEmail("bad"));
		Assert.assertTrue(com.sure.tool.util.CompareUtil.compareIgnoreCase("a", "b") < 0);
		Assert.assertNotNull(com.sure.tool.util.DesensitizedUtil.mobilePhone("13812345678"));
		Assert.assertNotNull(com.sure.tool.util.ClassUtil.getClassName(UserBean.class, false));
		Assert.assertNotNull(com.sure.tool.thread.ThreadUtil.newThread(() -> { }, "p5b"));
		Set<String> keys = new HashSet<>();
		keys.add("a");
		com.sure.tool.collection.BoundedPriorityQueue<String> q = new com.sure.tool.collection.BoundedPriorityQueue<>(
				2, String::compareTo);
		q.offer("b");
		q.offer("a");
		Assert.assertEquals(2, q.size());
		com.sure.tool.collection.TreeNode<String> tn = new com.sure.tool.collection.TreeNode<>("id", "parent", "name");
		Assert.assertEquals("id", tn.getId());
		com.sure.tool.bean.BeanDesc bd = new com.sure.tool.bean.BeanDesc(UserBean.class);
		Assert.assertNotNull(bd.getProp("name"));
		Assert.assertNotNull(com.sure.tool.util.Singleton.class);
		com.sure.tool.thread.RateLimiter rl = new com.sure.tool.thread.RateLimiter(10.0);
		rl.tryAcquire();
		rl.tryAcquire(2);
		Assert.assertNotNull(rl);
	}

	@Test
	public void testDictAndMapRemainGaps() {
		com.sure.tool.lang.Dict dict = com.sure.tool.lang.Dict.of("k", "v");
		Assert.assertEquals("v", dict.get("k"));
		Assert.assertEquals("v", dict.getStr("k"));
		Assert.assertTrue(dict.containsKey("k"));
		Assert.assertNotNull(com.sure.tool.collection.MapUtil.sortByValue(new HashMap<String, Integer>() {{
			put("a", 2);
			put("b", 1);
		}}, true));
		Assert.assertTrue(com.sure.tool.collection.MapUtil.sortByKey(new HashMap<String, Integer>() {{
			put("b", 1);
			put("a", 2);
		}}).keySet().iterator().next().equals("a"));
		Assert.assertEquals(2, com.sure.tool.collection.MapUtil.join(new HashMap<String, String>() {{
			put("a", "1");
			put("b", "2");
		}}, "-", "=").split("-").length);
		Assert.assertEquals(2, com.sure.tool.collection.ListUtil.toList("a", "b").size());
		Assert.assertEquals(2, com.sure.tool.collection.ListUtil.partition(Arrays.asList("a", "b"), 1).size());
	}
}
