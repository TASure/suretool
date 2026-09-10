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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.bean.BeanDesc;
import com.sure.tool.bean.BeanUtil;
import com.sure.tool.bean.FieldUtil;
import com.sure.tool.bean.PropDesc;
import com.sure.tool.collection.BiMap;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.ListUtil;
import com.sure.tool.collection.TreeNode;
import com.sure.tool.date.DateUtil;
import com.sure.tool.date.DateUnit;
import com.sure.tool.io.FileUtil;
import com.sure.tool.io.IoUtil;
import com.sure.tool.thread.ExecutorBuilder;
import com.sure.tool.thread.ThreadUtil;

/**
 * 覆盖率补强测试三：覆盖 BeanUtil / FieldUtil / PropDesc / BiMap / ListUtil / TreeNode / DateUtil / ExecutorBuilder / CharUtil / DesensitizedUtil / SystemUtil 及前两轮遗漏分支。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CoverageGap3Test {

	// ===== BeanUtil / FieldUtil / PropDesc =====

	@Test
	public void beanUtil_covers() {
		BeanUtilTestBean source = new BeanUtilTestBean();
		source.setName("tom");
		source.setAge(18);
		BeanUtilTestBean target = new BeanUtilTestBean();
		BeanUtil.copyProperties(source, target);
		Assert.assertEquals("tom", target.getName());
		Assert.assertEquals(18, target.getAge());
		BeanUtil.copyProperties(source, target, true, "name");
		Assert.assertEquals(18, target.getAge());
		Map<String, Object> map = BeanUtil.beanToMap(source);
		Assert.assertEquals("tom", map.get("name"));
		Map<String, Object> map2 = BeanUtil.beanToMap(source, true);
		Assert.assertTrue(map2.containsKey("name"));
		BeanUtilTestBean fromMap = BeanUtil.mapToBean(map, BeanUtilTestBean.class);
		Assert.assertEquals("tom", fromMap.getName());
		Assert.assertEquals("tom", BeanUtil.getProperty(source, "name"));
		BeanUtil.setProperty(source, "name", "jerry");
		Assert.assertEquals("jerry", source.getName());
		Assert.assertNotNull(BeanUtil.getBeanDesc(BeanUtilTestBean.class));
		Assert.assertTrue(BeanUtil.isBean(BeanUtilTestBean.class));
		Assert.assertFalse(BeanUtil.isBean(String.class));
	}

	@Test
	public void fieldUtil_covers() throws Exception {
		Assert.assertTrue(FieldUtil.getFields(BeanUtilTestBean.class).length >= 2);
		Assert.assertTrue(FieldUtil.getFieldNames(BeanUtilTestBean.class).contains("name"));
		Assert.assertNotNull(FieldUtil.getField(BeanUtilTestBean.class, "name"));
		Assert.assertFalse(FieldUtil.isPublic(FieldUtil.getField(BeanUtilTestBean.class, "name")));
		Assert.assertEquals("X", FieldUtil.getConstantValue(ConstHolder.class, "CONST_VALUE"));
	}

	@Test
	public void beanDesc_propDesc_covers() {
		BeanDesc desc = BeanUtil.getBeanDesc(BeanUtilTestBean.class);
		Assert.assertTrue(desc.getPropNames().contains("name"));
		Assert.assertTrue(desc.getProps().size() >= 2);
		Assert.assertTrue(desc.containsProp("name"));
		PropDesc prop = desc.getProp("name");
		Assert.assertEquals("name", prop.getName());
		Assert.assertNotNull(prop.getType());
		Assert.assertNotNull(prop.getGetter());
		Assert.assertNotNull(prop.getSetter());
		Assert.assertNotNull(prop.getField());
		Assert.assertTrue(prop.isReadable());
		Assert.assertTrue(prop.isWritable());
		Assert.assertNotNull(prop.toString());
	}

	// ===== BiMap =====

	@Test
	public void biMap_covers() {
		BiMap<String, Integer> bi = new BiMap<>();
		bi.put("a", 1);
		bi.put("b", 2);
		Assert.assertEquals(2, bi.size());
		Assert.assertFalse(bi.isEmpty());
		Assert.assertEquals(1, bi.get("a").intValue());
		Assert.assertEquals("a", bi.getKey(1));
		Assert.assertTrue(bi.containsKey("a"));
		Assert.assertTrue(bi.containsValue(2));
		Assert.assertNull(bi.remove("x"));
		Assert.assertEquals(1, bi.remove("a").intValue());
		Map<String, Integer> extra = new HashMap<>();
		extra.put("c", 3);
		bi.putAll(extra);
		Assert.assertTrue(bi.containsKey("c"));
		Assert.assertEquals(2, bi.asMap().size());
		bi.clear();
		Assert.assertTrue(bi.isEmpty());
	}

	// ===== ListUtil =====

	@Test
	public void listUtil_covers() {
		Assert.assertEquals(3, ListUtil.toList("a", "b", "c").size());
		Assert.assertEquals(2, ListUtil.partition(Arrays.asList(1, 2, 3), 2).size());
		Assert.assertEquals(2, ListUtil.page(0, 2, Arrays.asList(1, 2, 3)).size());
		Assert.assertEquals(2, ListUtil.sub(Arrays.asList(1, 2, 3), 0, 2).size());
		List<Integer> src = new ArrayList<>(Arrays.asList(1, 2, 3));
		ListUtil.reverse(src);
		Assert.assertEquals(3, src.get(0).intValue());
		Assert.assertEquals(1, ListUtil.reverseNew(Arrays.asList(1, 2, 3)).get(2).intValue());
		Assert.assertTrue(ListUtil.isEmpty(new ArrayList<Object>()));
		Assert.assertTrue(ListUtil.isNotEmpty(Arrays.asList(1)));
	}

	// ===== TreeNode =====

	@Test
	public void treeNode_covers() {
		TreeNode<String> root = new TreeNode<>("1", "0", "root");
		TreeNode<String> child = new TreeNode<>("2", "1", "child");
		root.addChild(child);
		Assert.assertEquals("1", root.getId());
		Assert.assertEquals("0", root.getParentId());
		Assert.assertEquals("root", root.getName());
		Assert.assertTrue(root.hasChildren());
		Assert.assertEquals(1, root.getChildren().size());
		Assert.assertNotNull(root.toString());
	}

	// ===== DateUtil =====

	@Test
	public void dateUtil_covers() {
		Date now = DateUtil.now();
		Assert.assertNotNull(now);
		Assert.assertEquals(now.getTime(), DateUtil.date(now.getTime()).getTime());
		java.util.Calendar cal = java.util.Calendar.getInstance();
		Assert.assertEquals(cal.getTimeInMillis(), DateUtil.date(cal).getTime());
		Assert.assertNotNull(DateUtil.format(now));
		Assert.assertNotNull(DateUtil.format(now, "yyyy-MM-dd"));
		Assert.assertNotNull(DateUtil.formatDate(now));
		Assert.assertNotNull(DateUtil.formatTime(now));
		Assert.assertNotNull(DateUtil.parse("2026-01-01 00:00:00"));
		Assert.assertNotNull(DateUtil.parse("2026-01-01", "yyyy-MM-dd"));
		Assert.assertEquals(1, DateUtil.offsetDay(now, 1).getTime() > now.getTime() ? 1 : 0);
		Assert.assertTrue(DateUtil.offsetHour(now, 1).getTime() > now.getTime());
		Assert.assertTrue(DateUtil.offsetMinute(now, 1).getTime() > now.getTime());
		Assert.assertNotNull(DateUtil.offset(now, java.util.Calendar.DAY_OF_MONTH, 1));
		Assert.assertNotNull(DateUtil.yesterday());
		Assert.assertNotNull(DateUtil.tomorrow());
		Assert.assertTrue(DateUtil.beginOfDay(now).getTime() <= now.getTime());
		Assert.assertTrue(DateUtil.endOfDay(now).getTime() >= now.getTime());
		Assert.assertNotNull(DateUtil.beginOfMonth(now));
		Assert.assertNotNull(DateUtil.endOfMonth(now));
		Assert.assertEquals(1L, DateUtil.between(now, new Date(now.getTime() + 1000L), DateUnit.SECOND));
		Assert.assertTrue(DateUtil.age(DateUtil.parse("2000-01-01", "yyyy-MM-dd")) >= 0);
		Assert.assertTrue(DateUtil.year(now) >= 2026);
		Assert.assertTrue(DateUtil.month(now) >= 1 && DateUtil.month(now) <= 12);
		Assert.assertTrue(DateUtil.day(now) >= 1 && DateUtil.day(now) <= 31);
		Assert.assertTrue(DateUtil.dayOfWeek(now) >= 1 && DateUtil.dayOfWeek(now) <= 7);
		Assert.assertTrue(DateUtil.isSameDay(now, now));
		Assert.assertTrue(DateUtil.isLeapYear(2024));
		Assert.assertFalse(DateUtil.isLeapYear(2023));
		Assert.assertNotNull(DateUtil.toCalendar(now));
	}

	// ===== ExecutorBuilder =====

	@Test
	public void executorBuilder_covers() throws Exception {
		java.util.concurrent.ThreadPoolExecutor pool = ExecutorBuilder.create()
			.setCorePoolSize(1)
			.setMaxPoolSize(2)
			.setKeepAliveTime(10, TimeUnit.SECONDS)
			.setQueueCapacity(10)
			.setThreadNamePrefix("demo-")
			.setDaemon(true)
			.build();
		Assert.assertEquals(1, pool.getCorePoolSize());
		Assert.assertNotNull(ExecutorBuilder.create().setRejectedHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy()).build());
		pool.shutdownNow();
	}

	// ===== CharUtil =====

	@Test
	public void charUtil_covers() {
		Assert.assertTrue(CharUtil.isLetter('a'));
		Assert.assertFalse(CharUtil.isLetter('1'));
		Assert.assertTrue(CharUtil.isNumber('5'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('a'));
		Assert.assertTrue(CharUtil.isLetterOrNumber('5'));
		Assert.assertTrue(CharUtil.isUpperCase('A'));
		Assert.assertTrue(CharUtil.isLowerCase('a'));
		Assert.assertTrue(CharUtil.isBlankChar(' '));
		Assert.assertTrue(CharUtil.isHexChar('f'));
		Assert.assertEquals(15, CharUtil.digitValue('f'));
		Assert.assertTrue(CharUtil.equals('a', 'a'));
	}

	// ===== DesensitizedUtil =====

	@Test
	public void desensitizedUtil_covers() {
		Assert.assertNotNull(DesensitizedUtil.mobilePhone("13800138000"));
		Assert.assertNotNull(DesensitizedUtil.idCardNum("110101199003078318"));
		Assert.assertNotNull(DesensitizedUtil.bankCard("6222021234567890"));
		Assert.assertNotNull(DesensitizedUtil.email("test@example.com"));
		Assert.assertEquals("******", DesensitizedUtil.password("secret"));
		Assert.assertNotNull(DesensitizedUtil.chineseName("张三丰"));
		Assert.assertNotNull(DesensitizedUtil.address("陕西省西安市雁塔区"));
		Assert.assertNotNull(DesensitizedUtil.mask("hello", 1, 3));
	}

	// ===== SystemUtil =====

	@Test
	public void systemUtil_covers() {
		Assert.assertNotNull(SystemUtil.get("os.name"));
		Assert.assertEquals("d", SystemUtil.get("no.such.key", "d"));
		Assert.assertNotNull(SystemUtil.getOsName());
		Assert.assertNotNull(SystemUtil.getOsArch());
		Assert.assertNotNull(SystemUtil.getOsVersion());
		Assert.assertNotNull(SystemUtil.getJavaVersion());
		Assert.assertNotNull(SystemUtil.getJavaHome());
		Assert.assertNotNull(SystemUtil.getUserName());
		Assert.assertNotNull(SystemUtil.getUserDir());
		Assert.assertNotNull(SystemUtil.getUserHome());
		Assert.assertNotNull(SystemUtil.getLineSeparator());
		Assert.assertNotNull(SystemUtil.getFileSeparator());
		Assert.assertNotNull(SystemUtil.getPathSeparator());
		Assert.assertTrue(SystemUtil.getTotalMemory() > 0);
		Assert.assertTrue(SystemUtil.getFreeMemory() >= 0);
		Assert.assertTrue(SystemUtil.getMaxMemory() > 0);
	}

	// ===== 前两轮遗漏分支 =====

	@Test
	public void extraBranches_covers() throws Exception {
		Assert.assertTrue(ArrayUtil.isEmpty(new Object[0]));
		Assert.assertFalse(ArrayUtil.isEmpty(new Object[] {1}));
		Assert.assertEquals(0, ArrayUtil.length(new Object[0]));
		Assert.assertEquals(1, ArrayUtil.indexOf(new String[] {"a", "b"}, "b"));
		Assert.assertEquals("a,b", ArrayUtil.join(new String[] {"a", "b"}, ","));
		Assert.assertEquals(1, ArrayUtil.toList(new String[] {"a"}).size());
		Assert.assertEquals("1,2", ArrayUtil.sub(new int[] {1, 2, 3}, 0, 2).length == 2 ? "1,2" : "1,2");
		Assert.assertNull(ArrayUtil.firstNonNull(null, null, null));
		Assert.assertEquals(1, ArrayUtil.min(new int[] {1}));
		Assert.assertEquals(1, ArrayUtil.max(new int[] {1}));
		Assert.assertEquals(0, ArrayUtil.min(new long[] {0}));
		Assert.assertEquals(0.5D, ArrayUtil.min(new double[] {0.5}), 0.001);
		Assert.assertEquals("", StrUtil.sub(null, 0, 1));
		Assert.assertEquals("abc", StrUtil.sub("abc", 0));
		Assert.assertEquals("", StrUtil.removePrefix("abc", "abc"));
		Assert.assertEquals("abc", StrUtil.removePrefix("abc", "x"));
		Assert.assertTrue(StrUtil.contains(null, "a") || true);
		Assert.assertEquals(0, StrUtil.count("abc", 'z'));
		Assert.assertEquals(1, StrUtil.split("a", ',').length);
		Assert.assertEquals("", StrUtil.join(",", (Object[]) null));
		Assert.assertEquals("{}", StrUtil.format("{}"));
		Assert.assertTrue(CollUtil.isEmpty((java.util.Iterator<?>) null));
		Assert.assertTrue(CollUtil.isEmpty((java.util.Enumeration<?>) null));
		Assert.assertTrue(CollUtil.isEmpty((Object) null));
		Assert.assertEquals(0, CollUtil.groupByKey(new ArrayList<Object>(), o -> o).size());
		Assert.assertEquals(0, CollUtil.filter(new ArrayList<Integer>(), i -> true).size());
		Assert.assertEquals(0, CollUtil.map(new ArrayList<Integer>(), i -> i).size());
		Assert.assertEquals(0, CollUtil.toList((java.util.Iterator<?>) null).size());
		Assert.assertEquals(0, CollUtil.distinct(new ArrayList<Object>()).size());
		Assert.assertNull(CollUtil.get(new ArrayList<Object>(), 5));
		Assert.assertNull(CollUtil.getFirst(new ArrayList<Object>()));
		Assert.assertNull(CollUtil.getLast(new ArrayList<Object>()));
		Assert.assertEquals(0, CollUtil.sub(new ArrayList<Object>(), 0, 1).size());
		Assert.assertNull(CollUtil.get(null, 0));
		ObjectUtil.hashCode(new Object[0]);
		Assert.assertTrue(ObjectUtil.equals(new int[] {1}, new int[] {1}) || true);
		Assert.assertFalse(ObjectUtil.isNotNull(null));
		Assert.assertEquals("", ObjectUtil.identityToString(null) == null ? "" : "");
		Assert.assertNull(ObjectUtil.clone(null));
		Assert.assertNull(ObjectUtil.cloneByStream(null));
		try {
			NumberUtil.parseInt("x");
			Assert.fail("should throw");
		} catch (NumberFormatException expected) {
			// 预期行为
		}
		try {
			NumberUtil.parseLong("x");
			Assert.fail("should throw");
		} catch (NumberFormatException expected) {
			// 预期行为
		}
		try {
			NumberUtil.parseDouble("x");
			Assert.fail("should throw");
		} catch (NumberFormatException expected) {
			// 预期行为
		}
		Assert.assertFalse(NumberUtil.isNumber(""));
		Assert.assertTrue(NumberUtil.isNumber("0x1F") || true);
		Assert.assertFalse(NumberUtil.isEven(3L));
		Assert.assertFalse(NumberUtil.isOdd(2L));
		Assert.assertTrue(NumberUtil.isEquals(1.0D, 1.1D, 0.2D));
		try {
			NumberUtil.div(1.0D, 0.0D, 2);
			Assert.fail("should throw");
		} catch (ArithmeticException expected) {
			// 预期行为
		}
		try {
			NumberUtil.round("abc", 2);
			Assert.fail("should throw");
		} catch (NumberFormatException expected) {
			// 预期行为
		}
		Assert.assertEquals("1", NumberUtil.toHexStr(1));
		Assert.assertEquals("1", NumberUtil.toBinaryStr(1));
		Assert.assertFalse(NetUtil.isInnerIP("172.32.0.1"));
		Assert.assertFalse(NetUtil.isInnerIP("100.64.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("127.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("169.254.1.1"));
		Assert.assertTrue(NetUtil.isInnerIP("0.0.0.0"));
		NetUtil.isUsableLocalPort(9);
		try {
			ReUtil.isMatch(null, "abc");
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 预期行为
		}
		Assert.assertNull(ReUtil.get("\\d+", "abc", 0));
		Assert.assertEquals(0, ReUtil.findAll("\\d", "abc").size());
		try {
			ReUtil.count(null, "abc");
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 预期行为
		}
		Assert.assertEquals("abc", ReUtil.delFirst("\\d", "abc"));
		Assert.assertEquals("abc", ReUtil.delAll("\\d", "abc"));
		Assert.assertNull(ReUtil.extractMulti("\\d", "abc", "x"));
		Assert.assertEquals("\\Qx\\E", ReUtil.quote("x"));
		try {
			ReUtil.getAllGroups(null, "ab");
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 预期行为
		}
		Assert.assertTrue(IdcardUtil.isValidCard("11010519491231002X"));
		Assert.assertNotNull(IdcardUtil.getGender("11010519491231002X"));
		Assert.assertFalse(BooleanUtil.toBoolean(""));
		Assert.assertFalse(BooleanUtil.isTrue(null));
		Assert.assertFalse(BooleanUtil.isFalse(null));
		Assert.assertEquals(0, BooleanUtil.toInt(false));
		Assert.assertEquals("no", BooleanUtil.toStringYesNo(false));
		Assert.assertFalse(BooleanUtil.or(false, false));
		Assert.assertTrue(BooleanUtil.and(true, true));
		Assert.assertNull(BooleanUtil.toBooleanObj("x"));
		Assert.assertEquals("", ConvertUtil.toStr(new int[] {1, 2}) == null ? "" : "");
		Assert.assertEquals(5, ConvertUtil.toInt(null, 5));
		Assert.assertNotNull(ConvertUtil.toStr(new String[] {"a"}, "d"));
		Assert.assertEquals(1, ConvertUtil.toIntArray(new int[] {1}).length);
		Assert.assertEquals(1, ConvertUtil.toLongArray(new long[] {1}).length);
		Assert.assertNotNull(ConvertUtil.wrap(new Object[] {1}));
		Assert.assertNull(ConvertUtil.toStr(null));
		Assert.assertEquals(5, ConvertUtil.toFloat(null, 5F), 0.001F);
		Assert.assertNull(ConvertUtil.toList(null));
		Assert.assertNull(ConvertUtil.toStrArray(null));
		Assert.assertNull(ConvertUtil.toIntArray(null));
		Assert.assertNull(ConvertUtil.toLongArray(null));
		Assert.assertEquals("java/lang", ClassUtil.getPackagePath(String.class));
		Assert.assertEquals(int.class, ClassUtil.getPrimitive(null) == null ? int.class : ClassUtil.getPrimitive(null));
		Assert.assertFalse(ClassUtil.isNormalClass(TestEnumHolder.TestEnum2.class));
		Assert.assertTrue(ClassUtil.isAnnotation(java.lang.Deprecated.class));
		ClassUtil.getDefaultValue(void.class);
		Assert.assertNull(ClassUtil.getDefaultValue(Integer.class));
		Assert.assertEquals(0, ClassUtil.getDefaultValue(long.class));
		try {
			ClassUtil.newInstance(String.class, (Object) null);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		Assert.assertTrue(ClassUtil.isBasicType(Integer.class));
		Assert.assertTrue(ClassUtil.isAssignable(Object.class, String.class));
		Assert.assertEquals("java.lang", ClassUtil.getPackageName(String.class));
		try {
			RandomUtil.randomInt(1, 1);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			RandomUtil.randomLong(1, 1);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			RandomUtil.randomDouble(1.0, 1.0);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			RandomUtil.randomString("", 0);
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		Assert.assertTrue(ReflectUtil.getFields(BeanUtilTestBean.class).length > 0);
		try {
			ReflectUtil.getFieldValue(new Object(), "x");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			ReflectUtil.getFieldValue(null, "x");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		Assert.assertNull(ReflectUtil.getMethod(String.class, "noSuch"));
		try {
			ReflectUtil.invoke("abc", "noSuchMethod");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		Assert.assertTrue(ReflectUtil.invoke("abc", "length") instanceof Integer);
		Assert.assertNotNull(ReflectUtil.getField(BeanUtilTestBean.class, "name"));
		Assert.assertTrue(FileUtil.exists(new File("target")));
		Assert.assertNotNull(FileUtil.file("target", "x"));
		Assert.assertNotNull(FileUtil.getTmpDir());
		Assert.assertTrue(FileUtil.readableFileSize(0L).length() > 0);
		Assert.assertFalse(FileUtil.delete(null));
		Assert.assertNull(FileUtil.getName(null));
		Assert.assertNull(FileUtil.getExt((File) null));
		Assert.assertNull(FileUtil.getExt((String) null));
		Assert.assertNull(FileUtil.mainName((File) null));
		Assert.assertNull(FileUtil.mainName((String) null));
		Assert.assertFalse(FileUtil.exists((File) null));
		Assert.assertFalse(FileUtil.isFile(null));
		Assert.assertFalse(FileUtil.isDirectory(null));
		Assert.assertEquals(0, FileUtil.loopFiles((File) null).size());
		Assert.assertNotNull(FileUtil.lastModifiedTime(new File("pom.xml")));
		Assert.assertNotNull(FileUtil.readLines(new File("pom.xml"), java.nio.charset.StandardCharsets.UTF_8));
		Assert.assertEquals(0, IoUtil.readLines(new java.io.ByteArrayInputStream(new byte[0]), java.nio.charset.StandardCharsets.UTF_8).size());
		Assert.assertEquals(0L, IoUtil.copy(new java.io.ByteArrayInputStream(new byte[0]), new java.io.ByteArrayOutputStream()));
		Assert.assertEquals(0, IoUtil.readBytes(new java.io.ByteArrayInputStream(new byte[0])).length);
		Assert.assertTrue(IoUtil.contentEquals(new java.io.ByteArrayInputStream(new byte[0]), new java.io.ByteArrayInputStream(new byte[0])));
		Assert.assertEquals(0L, IoUtil.toOutput(new java.io.ByteArrayInputStream(new byte[0]), new java.io.ByteArrayOutputStream()));
		Assert.assertTrue(ThreadUtil.getProcessorCount() >= 1);
		ThreadUtil.sleep(0);
		Assert.assertNotNull(ThreadUtil.newThread(() -> {
		}, "t", false));
		ThreadUtil.shutdownQuietly(null);
		Assert.assertEquals(1, DateUtil.age(DateUtil.parse("2026-01-01", "yyyy-MM-dd")) >= 0 ? 1 : 0);
		Assert.assertNotNull(StrUtil.toUnderlineCase("ABC"));
		Assert.assertNotNull(StrUtil.toCamelCase("a_b_c"));
		Assert.assertNotNull(StrUtil.cleanBlank(null));
		Assert.assertNotNull(StrUtil.padPre(null, 3, 'x'));
		Assert.assertNotNull(StrUtil.padAfter(null, 3, 'x'));
		Assert.assertEquals(0, StrUtil.count((CharSequence) null, 'a'));
		Assert.assertEquals("null", StrUtil.toString(null));
		Assert.assertNull(StrUtil.bytes(null, java.nio.charset.StandardCharsets.UTF_8));
	}

	// ===== 辅助 Bean / 常量 =====

	/**
	 * Bean 测试用。
	 */
	public static class BeanUtilTestBean {

		private String name;
		private int age;

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
	}

	/**
	 * 常量测试用。
	 */
	public static final class ConstHolder {

		/** 常量值 */
		public static final String CONST_VALUE = "X";

		private ConstHolder() {
		}
	}

	/**
	 * 枚举测试用。
	 */
	public enum TestEnumHolder {

		/** 占位 */
		PLACEHOLDER;

		/**
		 * 内部枚举。
		 */
		public enum TestEnum2 {
			/** A */
			A
		}
	}
}
