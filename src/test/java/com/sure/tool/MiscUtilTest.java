package com.sure.tool;

import com.sure.tool.lang.Snowflake;
import com.sure.tool.util.ClassUtil;
import com.sure.tool.util.IdUtil;
import com.sure.tool.util.RandomUtil;
import com.sure.tool.util.ReUtil;
import com.sure.tool.util.ReflectUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * RandomUtil / ReUtil / IdUtil / ClassUtil / ReflectUtil / Assert / Snowflake 单元测试。
 */
public class MiscUtilTest {

	// ---------------- RandomUtil ----------------

	@Test
	public void testRandom() {
		for (int i = 0; i < 100; i++) {
			int value = RandomUtil.randomInt(10, 20);
			assertTrue(value >= 10 && value < 20);
		}
		String s = RandomUtil.randomString(16);
		assertEquals(16, s.length());
		String numbers = RandomUtil.randomNumbers(8);
		for (int i = 0; i < numbers.length(); i++) {
			assertTrue(Character.isDigit(numbers.charAt(i)));
		}
		assertEquals(32, RandomUtil.simpleUUID().length());
		assertTrue(RandomUtil.randomDouble(0, 1) >= 0);
		assertTrue(RandomUtil.randomDouble(1, 2, 2) < 2);
	}

	// ---------------- ReUtil ----------------

	@Test
	public void testRegex() {
		assertTrue(ReUtil.isMatch("\\d+", "abc123"));
		assertFalse(ReUtil.isMatch("^\\d+$", "abc123"));
		assertEquals("123", ReUtil.get("(\\d+)", "abc123def", 1));
		assertEquals("2026", ReUtil.get("(?<year>\\d{4})-(?<month>\\d{2})", "2026-09-09", "year"));
		List<String> groups = ReUtil.getAllGroups("(\\d{4})-(\\d{2})", "2026-09-09");
		assertEquals(3, groups.size());
		assertEquals("2026-09", groups.get(0));
		assertEquals("2026", groups.get(1));
		assertEquals(2, ReUtil.count("\\d", "a1b2"));
		assertEquals(list("abc", "def"), ReUtil.findAll("\\w+", "abc,def"));
		assertEquals("aXc", ReUtil.replaceAll("abc", "b", "X"));
		assertEquals("ac", ReUtil.delFirst("\\d+", "a123c"));
		assertEquals("abc", ReUtil.delAll("\\d", "a1b2c"));
		assertEquals("2026-09", ReUtil.extractMulti("(\\d{4})-(\\d{2})", "2026-09-09", "{1}-{2}"));
	}

	private static List<String> list(String... values) {
		List<String> list = new ArrayList<>();
		for (String v : values) {
			list.add(v);
		}
		return list;
	}

	// ---------------- IdUtil / Snowflake ----------------

	@Test
	public void testIdUtil() {
		assertEquals(32, IdUtil.simpleUUID().length());
		assertEquals(24, IdUtil.objectId().length());
		assertTrue(IdUtil.objectId().matches("^[0-9a-f]{24}$"));
	}

	@Test
	public void testSnowflake() {
		Snowflake snowflake = IdUtil.createSnowflake(1, 1);
		Set<Long> ids = new HashSet<>();
		for (int i = 0; i < 10000; i++) {
			long id = snowflake.nextId();
			assertTrue(id > 0);
			ids.add(id);
		}
		assertEquals(10000, ids.size());
		assertEquals(1L, snowflake.getWorkerId());
		assertEquals(1L, snowflake.getDatacenterId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSnowflakeInvalidWorker() {
		new Snowflake(32, 1);
	}

	// ---------------- ClassUtil ----------------

	@Test
	public void testClassUtil() {
		assertTrue(ClassUtil.isBasicType(Integer.class));
		assertTrue(ClassUtil.isBasicType(String.class));
		assertFalse(ClassUtil.isBasicType(MiscUtilTest.class));
		assertTrue(ClassUtil.isPrimitiveWrapper(Integer.class));
		assertEquals(int.class, ClassUtil.getPrimitive(Integer.class));
		assertEquals("com.sure.tool", ClassUtil.getPackageName(MiscUtilTest.class));
		assertEquals("com/sure/tool", ClassUtil.getPackagePath(MiscUtilTest.class));
		assertEquals("MiscUtilTest", ClassUtil.getShortClassName(MiscUtilTest.class));
		assertEquals(0, ClassUtil.getDefaultValue(int.class));
		assertEquals(Boolean.FALSE, ClassUtil.getDefaultValue(boolean.class));
		try {
			assertEquals(String.class, ClassUtil.loadClass("java.lang.String"));
		} catch (ClassNotFoundException e) {
			fail(e.getMessage());
		}
		assertTrue(ClassUtil.isAssignable(Number.class, Integer.class));
		assertTrue(ClassUtil.isAssignable(int.class, Integer.class));
	}

	@Test
	public void testNewInstance() {
		StringBuilder sb = ClassUtil.newInstance(StringBuilder.class);
		assertNotNull(sb);
	}

	// ---------------- ReflectUtil ----------------

	public static class Demo {
		private String name = "init";
		private int count = 1;

		public String getName() {
			return name;
		}

		public int add(int a, int b) {
			return a + b;
		}

		public static String staticHello(String who) {
			return "hello " + who;
		}
	}

	@Test
	public void testReflect() {
		Demo demo = new Demo();
		ReflectUtil.setFieldValue(demo, "name", "changed");
		assertEquals("changed", ReflectUtil.getFieldValue(demo, "name"));
		assertEquals(5, ReflectUtil.invoke(demo, "add", 2, 3));
		assertEquals("hello world", ReflectUtil.invokeStatic(Demo.class, "staticHello", "world"));
		assertEquals("init", ReflectUtil.getFieldValue(new Demo(), "name"));
		assertNotNull(ReflectUtil.getField(Demo.class, "count"));
		assertNotNull(ReflectUtil.getMethod(Demo.class, "add", int.class, int.class));
	}

	// ---------------- Assert ----------------

	@Test
	public void testAssertPass() {
		com.sure.tool.lang.Assert.isTrue(1 > 0);
		com.sure.tool.lang.Assert.notNull("x", "不能为空");
		com.sure.tool.lang.Assert.notBlank("abc", "不能空白");
		com.sure.tool.lang.Assert.notEmpty(java.util.Arrays.asList(1), "不能为空");
		com.sure.tool.lang.Assert.isInstanceOf(Number.class, 1, "类型不符");
		com.sure.tool.lang.Assert.match("\\d+", "123", "不匹配");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertFail() {
		com.sure.tool.lang.Assert.notNull(null, "对象不能为 null");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertBlankFail() {
		com.sure.tool.lang.Assert.notBlank("   ", "字符串不能为空白");
	}
}
