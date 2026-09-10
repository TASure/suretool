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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 覆盖率补强测试：覆盖 CharsetUtil / Assert / ClassUtil / ObjectUtil / ArrayUtil / RandomUtil / ReflectUtil / ReUtil / NetUtil / NumberUtil 主要路径。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CoverageGapTest {

	// ===== CharsetUtil =====

	@Test
	public void charsetUtil_covers() {
		Assert.assertEquals("UTF-8", CharsetUtil.charset("UTF-8").name());
		Assert.assertEquals(Charset.defaultCharset(), CharsetUtil.charset(null));
		Assert.assertEquals(CharsetUtil.UTF_8, CharsetUtil.UTF_8);
		Assert.assertEquals(CharsetUtil.GBK, CharsetUtil.GBK);
		Assert.assertEquals(CharsetUtil.ISO_8859_1, CharsetUtil.ISO_8859_1);
		Assert.assertEquals(CharsetUtil.US_ASCII, CharsetUtil.US_ASCII);
		Assert.assertEquals(CharsetUtil.UTF_16, CharsetUtil.UTF_16);
		Assert.assertEquals(CharsetUtil.UTF_16BE, CharsetUtil.UTF_16BE);
		Assert.assertEquals(CharsetUtil.UTF_16LE, CharsetUtil.UTF_16LE);
		Assert.assertEquals("中文", CharsetUtil.convert("中文", CharsetUtil.UTF_8, CharsetUtil.UTF_8));
		Assert.assertEquals("中文", CharsetUtil.convert("中文", "UTF-8", "UTF-8"));
		Assert.assertNull(CharsetUtil.convert(null, CharsetUtil.UTF_8, CharsetUtil.UTF_8));
		Assert.assertTrue(CharsetUtil.isSupported("GBK"));
		Assert.assertFalse(CharsetUtil.isSupported("NO-SUCH-CHARSET-123"));
		Assert.assertNotNull(CharsetUtil.defaultCharset());
	}

	@Test(expected = java.nio.charset.UnsupportedCharsetException.class)
	public void charsetUtil_unknownCharsetThrows() {
		CharsetUtil.charset("NO-SUCH-CHARSET-123");
	}

	// ===== Assert =====

	@Test
	public void assert_passPaths() {
		com.sure.tool.lang.Assert.isTrue(true);
		com.sure.tool.lang.Assert.isTrue(1 == 1, "must be true {}", 1);
		com.sure.tool.lang.Assert.isFalse(false, "msg");
		com.sure.tool.lang.Assert.isNull(null, "msg");
		Assert.assertEquals("x", com.sure.tool.lang.Assert.isNotNull("x", "msg"));
		Assert.assertEquals("x", com.sure.tool.lang.Assert.notNull("x", "msg"));
		Assert.assertEquals("abc", com.sure.tool.lang.Assert.notEmpty("abc", "msg"));
		Assert.assertEquals(" abc ", com.sure.tool.lang.Assert.notBlank(" abc ", "msg"));
		String[] arr = {"a"};
		Assert.assertSame(arr, com.sure.tool.lang.Assert.notEmpty(arr, "msg"));
		Collection<String> col = Arrays.asList("a");
		Assert.assertSame(col, com.sure.tool.lang.Assert.notEmpty(col, "msg"));
		Map<String, String> map = new HashMap<>();
		map.put("k", "v");
		Assert.assertSame(map, com.sure.tool.lang.Assert.notEmpty(map, "msg"));
		com.sure.tool.lang.Assert.isInstanceOf(String.class, "x", "msg");
		com.sure.tool.lang.Assert.match("\\d+", "123", "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isTrueFails() {
		com.sure.tool.lang.Assert.isTrue(false, "msg {}", 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isTrueNoMsgFails() {
		com.sure.tool.lang.Assert.isTrue(false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isFalseFails() {
		com.sure.tool.lang.Assert.isFalse(true, "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isNullFails() {
		com.sure.tool.lang.Assert.isNull(new Object(), "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isNotNullFails() {
		com.sure.tool.lang.Assert.isNotNull(null, "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_notEmptyTextFails() {
		com.sure.tool.lang.Assert.notEmpty("", "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_notBlankTextFails() {
		com.sure.tool.lang.Assert.notBlank("   ", "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_notEmptyArrayFails() {
		com.sure.tool.lang.Assert.notEmpty(new String[0], "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_notEmptyCollectionFails() {
		com.sure.tool.lang.Assert.notEmpty(new ArrayList<String>(), "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_notEmptyMapFails() {
		com.sure.tool.lang.Assert.notEmpty(new HashMap<String, String>(), "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_isInstanceOfFails() {
		com.sure.tool.lang.Assert.isInstanceOf(Integer.class, "x", "msg");
	}

	@Test(expected = IllegalArgumentException.class)
	public void assert_matchFails() {
		com.sure.tool.lang.Assert.match("\\d+", "abc", "msg");
	}

	// ===== ClassUtil =====

	@Test
	public void classUtil_covers() throws Exception {
		Assert.assertEquals(String.class, ClassUtil.getClass("x"));
		Assert.assertEquals("String", ClassUtil.getClassName("x", true));
		Assert.assertEquals("java.lang.String", ClassUtil.getClassName("x", false));
		Assert.assertTrue(ClassUtil.isBasicType(int.class));
		Assert.assertTrue(ClassUtil.isBasicType(String.class));
		Assert.assertFalse(ClassUtil.isBasicType(Object.class));
		Assert.assertTrue(ClassUtil.isPrimitiveWrapper(Integer.class));
		Assert.assertFalse(ClassUtil.isPrimitiveWrapper(int.class));
		Assert.assertEquals(int.class, ClassUtil.getPrimitive(Integer.class));
		Assert.assertEquals("java.lang", ClassUtil.getPackageName(String.class));
		Assert.assertEquals("java/lang", ClassUtil.getPackagePath(String.class));
		Assert.assertEquals(String.class, ClassUtil.loadClass("java.lang.String"));
		Assert.assertEquals(String.class, ClassUtil.loadClass("java.lang.String", false));
		Assert.assertEquals("", ClassUtil.newInstance(String.class));
		Assert.assertEquals("hi", ClassUtil.newInstance(String.class, "hi"));
		Assert.assertEquals(5, ClassUtil.<Integer>newInstance("java.lang.Integer", 5).intValue());
		Assert.assertTrue(ClassUtil.isAssignable(CharSequence.class, String.class));
		Assert.assertFalse(ClassUtil.isAssignable(String.class, CharSequence.class));
		Assert.assertTrue(ClassUtil.isNormalClass(String.class));
		Assert.assertTrue(ClassUtil.isEnum(TestEnum.A.getClass()));
		Assert.assertTrue(ClassUtil.isInterface(Runnable.class));
		Assert.assertFalse(ClassUtil.isAnnotation(String.class));
		Assert.assertEquals(0, ClassUtil.getDefaultValue(int.class));
		Assert.assertNull(ClassUtil.getDefaultValue(String.class));
		Assert.assertEquals("String", ClassUtil.getShortClassName(String.class));
	}

	@Test(expected = ClassNotFoundException.class)
	public void classUtil_loadMissingThrows() throws Exception {
		ClassUtil.loadClass("no.such.Class", true);
	}

	// ===== ObjectUtil =====

	@Test
	public void objectUtil_covers() {
		Assert.assertTrue(ObjectUtil.isNull(null));
		Assert.assertFalse(ObjectUtil.isNull("x"));
		Assert.assertTrue(ObjectUtil.isNotNull("x"));
		Assert.assertTrue(ObjectUtil.isEmpty(null));
		Assert.assertTrue(ObjectUtil.isEmpty(""));
		Assert.assertTrue(ObjectUtil.isEmpty(new Object[0]));
		Assert.assertTrue(ObjectUtil.isEmpty(new ArrayList<Object>()));
		Assert.assertTrue(ObjectUtil.isEmpty(new HashMap<Object, Object>()));
		Assert.assertFalse(ObjectUtil.isEmpty("x"));
		Assert.assertTrue(ObjectUtil.isNotEmpty("x"));
		Assert.assertTrue(ObjectUtil.equals("a", "a"));
		Assert.assertTrue(ObjectUtil.equals(null, null));
		Assert.assertFalse(ObjectUtil.equals("a", "b"));
		Assert.assertTrue(ObjectUtil.notEqual("a", "b"));
		Assert.assertEquals("a".hashCode(), ObjectUtil.hashCode("a"));
		Assert.assertEquals(0, ObjectUtil.hashCode(null));
		Assert.assertNotNull(ObjectUtil.identityToString("x"));
		Assert.assertEquals(String.class, ObjectUtil.getClass("x"));
		Assert.assertEquals("d", ObjectUtil.defaultIfNull(null, "d"));
		Assert.assertEquals("x", ObjectUtil.defaultIfNull("x", "d"));
		Assert.assertEquals("d", ObjectUtil.defaultIfEmpty(null, "d"));
		Assert.assertEquals("x", ObjectUtil.defaultIfEmpty("x", "d"));
		Assert.assertEquals("d", ObjectUtil.defaultIfEmpty("", "d"));
		Assert.assertTrue(ObjectUtil.isEmpty(java.util.Optional.empty()));
		Assert.assertFalse(ObjectUtil.isEmpty(java.util.Optional.of("x")));
	}

	@Test
	public void objectUtil_cloneAndSerialize() throws Exception {
		List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
		List<String> clone = ObjectUtil.clone(list);
		Assert.assertNotSame(list, clone);
		Assert.assertEquals(list, clone);
		List<String> streamClone = ObjectUtil.cloneByStream(list);
		Assert.assertNotSame(list, streamClone);
		Assert.assertEquals(list, streamClone);
		byte[] bytes = ObjectUtil.serialize("hello");
		Assert.assertEquals("hello", ObjectUtil.deserialize(bytes));
	}

	// ===== ArrayUtil =====

	@Test
	public void arrayUtil_covers() {
		Assert.assertTrue(ArrayUtil.isArray(new int[0]));
		Assert.assertFalse(ArrayUtil.isArray("x"));
		Assert.assertTrue(ArrayUtil.isEmpty(null));
		Assert.assertTrue(ArrayUtil.isEmpty(new int[0]));
		Assert.assertFalse(ArrayUtil.isEmpty(new int[] {1}));
		Assert.assertTrue(ArrayUtil.isNotEmpty(new int[] {1}));
		Assert.assertEquals(3, ArrayUtil.length(new int[] {1, 2, 3}));
		Assert.assertEquals(0, ArrayUtil.length("x"));
		Assert.assertTrue(ArrayUtil.contains(new int[] {1, 2}, 2));
		Assert.assertFalse(ArrayUtil.contains(new int[] {1, 2}, 3));
		Assert.assertEquals(1, ArrayUtil.indexOf(new int[] {1, 2}, 2));
		Assert.assertEquals(2, ArrayUtil.get(new int[] {1, 2, 3}, 1));
		Assert.assertEquals(9, ArrayUtil.get(new int[] {1}, 5, 9));
		Assert.assertEquals("1,2,3", ArrayUtil.join(new int[] {1, 2, 3}, ","));
		Assert.assertEquals(2, ArrayUtil.toList(new int[] {1, 2}).size());
		Assert.assertEquals(2, ArrayUtil.toArray(Arrays.asList("a", "b"), String.class).length);
		int[] rev = {1, 2, 3};
		ArrayUtil.reverse(rev);
		Assert.assertArrayEquals(new int[] {3, 2, 1}, rev);
		Assert.assertEquals(2, ArrayUtil.distinct(new int[] {1, 2, 2}).length);
		Assert.assertEquals(2, ArrayUtil.sub(new int[] {1, 2, 3}, 1, 3).length);
		Assert.assertEquals("a", ArrayUtil.firstNonNull(null, "a"));
		Assert.assertEquals(1, ArrayUtil.min(new Integer[] {3, 1, 2}).intValue());
		Assert.assertEquals(3, ArrayUtil.max(new Integer[] {3, 1, 2}).intValue());
		Assert.assertEquals(1, ArrayUtil.min(new int[] {3, 1, 2}));
		Assert.assertEquals(3, ArrayUtil.max(new int[] {3, 1, 2}));
		Assert.assertEquals(1L, ArrayUtil.min(new long[] {3, 1, 2}));
		Assert.assertEquals(3L, ArrayUtil.max(new long[] {3, 1, 2}));
		Assert.assertEquals(1.5D, ArrayUtil.min(new double[] {3.5, 1.5}), 0.001);
		Assert.assertEquals(3.5D, ArrayUtil.max(new double[] {3.5, 1.5}), 0.001);
		Assert.assertEquals("[1, 2]", ArrayUtil.toString(new int[] {1, 2}));
		Assert.assertEquals("[]", ArrayUtil.toString(null));
	}

	// ===== RandomUtil =====

	@Test
	public void randomUtil_covers() {
		RandomUtil.randomInt();
		int ir = RandomUtil.randomInt(5, 10);
		Assert.assertTrue(ir >= 5 && ir < 10);
		int il = RandomUtil.randomInt(10);
		Assert.assertTrue(il >= 0 && il < 10);
		RandomUtil.randomLong();
		long lr = RandomUtil.randomLong(5, 10);
		Assert.assertTrue(lr >= 5 && lr < 10);
		RandomUtil.randomDouble();
		double dr = RandomUtil.randomDouble(1.0, 2.0);
		Assert.assertTrue(dr >= 1.0 && dr < 2.0);
		double ds = RandomUtil.randomDouble(1.0, 2.0, 2);
		Assert.assertTrue(ds >= 1.0 && ds < 2.0);
		RandomUtil.randomBoolean();
		Assert.assertEquals(10, RandomUtil.randomString(10).length());
		Assert.assertEquals(8, RandomUtil.randomString("AB", 8).length());
		Assert.assertEquals(6, RandomUtil.randomNumbers(6).length());
		Assert.assertEquals(16, RandomUtil.randomBytes(16).length);
		Assert.assertEquals(36, RandomUtil.randomUUID().length());
		Assert.assertTrue(RandomUtil.simpleUUID().length() <= 36);
		Assert.assertNotNull(RandomUtil.BASE_NUMBER);
		Assert.assertNotNull(RandomUtil.BASE_CHAR);
		Assert.assertNotNull(RandomUtil.BASE_CHAR_NUMBER);
	}

	// ===== ReflectUtil =====

	@Test
	public void reflectUtil_covers() throws Exception {
		ReflectBean bean = new ReflectBean();
		Field field = ReflectUtil.getField(ReflectBean.class, "name");
		Assert.assertNotNull(field);
		ReflectUtil.setFieldValue(bean, "name", "tom");
		Assert.assertEquals("tom", ReflectUtil.getFieldValue(bean, "name"));
		Assert.assertNull(ReflectUtil.getField(ReflectBean.class, "noSuchField"));
		Assert.assertEquals(2, ReflectUtil.getFields(ReflectBean.class).length);
		Method[] methods = ReflectUtil.getMethods(ReflectBean.class);
		Assert.assertTrue(methods.length > 0);
		Assert.assertNotNull(ReflectUtil.getMethod(ReflectBean.class, "getName"));
		Assert.assertEquals("tom", ReflectUtil.invoke(bean, "getName"));
		Assert.assertEquals(3, ReflectUtil.invokeStatic(ReflectBean.class, "staticAdd", 1, 2));
		ReflectBean created = ReflectUtil.invokeConstructor(ReflectBean.class);
		Assert.assertNotNull(created);
		Assert.assertEquals("hi", ReflectUtil.invokeConstructor(String.class, "hi"));
	}

	// ===== ReUtil =====

	@Test
	public void reUtil_covers() {
		Assert.assertTrue(ReUtil.isMatch("\\d+", "123"));
		Assert.assertFalse(ReUtil.isMatch("\\d+", "abc"));
		Assert.assertEquals("123", ReUtil.get("(\\d+)", "abc123", 1));
		Assert.assertEquals("x", ReUtil.get("(?<letter>[a-z])", "xa", "letter"));
		Assert.assertEquals(3, ReUtil.getAllGroups("(a)(b)", "ab").size());
		Assert.assertEquals(5, ReUtil.getAllGroupMap("(?<x>a)(?<y>b)", "ab").size());
		Assert.assertEquals(3, ReUtil.findAll("\\d", "1a2b3").size());
		Assert.assertEquals(3, ReUtil.count("\\d", "1a2b3"));
		Assert.assertEquals("a-1-b", ReUtil.replaceAll("a1b", "\\d", "-$0-"));
		Assert.assertEquals("ab2", ReUtil.delFirst("\\d", "a1b2"));
		Assert.assertEquals("ab", ReUtil.delAll("\\d", "a1b2"));
		Assert.assertEquals("x-123", ReUtil.extractMulti("(\\d+)", "a123", "x-{1}"));
		Assert.assertEquals("\\Q1+2\\E", ReUtil.quote("1+2"));
	}

	// ===== NetUtil =====

	@Test
	public void netUtil_covers() {
		Assert.assertTrue(NetUtil.isInnerIP("10.0.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("172.16.0.1"));
		Assert.assertTrue(NetUtil.isInnerIP("192.168.1.1"));
		Assert.assertFalse(NetUtil.isInnerIP("8.8.8.8"));
		Assert.assertFalse(NetUtil.isValidPort(0));
		Assert.assertTrue(NetUtil.isValidPort(65535));
		Assert.assertFalse(NetUtil.isValidPort(-1));
		Assert.assertFalse(NetUtil.isValidPort(65536));
		String localhost = NetUtil.getLocalhostStr();
		Assert.assertTrue(localhost == null || !localhost.isEmpty());
		// 无外网 IPv4 环境可能返回 null，断言不抛异常即可
		NetUtil.getLocalIpv4();
	}

	// ===== NumberUtil =====

	@Test
	public void numberUtil_covers() {
		Assert.assertEquals(5, NumberUtil.parseInt("5"));
		Assert.assertEquals(5, NumberUtil.parseInt("x", 5));
		Assert.assertEquals(5L, NumberUtil.parseLong("5"));
		Assert.assertEquals(5L, NumberUtil.parseLong("x", 5L));
		Assert.assertEquals(5.5D, NumberUtil.parseDouble("5.5"), 0.001);
		Assert.assertEquals(5.5D, NumberUtil.parseDouble("x", 5.5D), 0.001);
		Assert.assertEquals(5.5F, NumberUtil.parseFloat("5.5", 0F), 0.001F);
		Assert.assertEquals((short) 5, NumberUtil.parseShort("5", (short) 0));
		Assert.assertEquals((byte) 5, NumberUtil.parseByte("5", (byte) 0));
		Assert.assertTrue(NumberUtil.isNumber("123.45"));
		Assert.assertFalse(NumberUtil.isNumber("abc"));
		Assert.assertTrue(NumberUtil.isInteger("123"));
		Assert.assertFalse(NumberUtil.isInteger("12.3"));
		Assert.assertTrue(NumberUtil.isDouble("12.3"));
		Assert.assertEquals(new BigDecimal("1.24"), NumberUtil.round(1.235D, 2));
		Assert.assertEquals(new BigDecimal("1.24"), NumberUtil.round("1.235", 2));
		Assert.assertEquals(3.0D, NumberUtil.add(1.0D, 2.0D), 0.001);
		Assert.assertEquals(1.0D, NumberUtil.sub(3.0D, 2.0D), 0.001);
		Assert.assertEquals(6.0D, NumberUtil.mul(2.0D, 3.0D), 0.001);
		Assert.assertEquals(1.5D, NumberUtil.div(3.0D, 2.0D, 2), 0.001);
		Assert.assertEquals(3L, NumberUtil.add(1L, 2L));
		Assert.assertTrue(NumberUtil.isEquals(1.0D, 1.0D, 0.01D));
		Assert.assertTrue(NumberUtil.isEven(4L));
		Assert.assertTrue(NumberUtil.isOdd(3L));
		Assert.assertEquals("12.30", NumberUtil.decimalFormat(12.3D, "00.00"));
		Assert.assertEquals("1010", NumberUtil.toBinaryStr(10));
		Assert.assertEquals("a", NumberUtil.toHexStr(10));
	}

	// ===== 辅助 Bean / 枚举 =====

	/**
	 * 反射测试用 Bean。
	 */
	public static class ReflectBean {

		private String name;
		private int age;

		/**
		 * 无参构造。
		 */
		public ReflectBean() {
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

		/**
		 * 静态方法。
		 *
		 * @param a 加数
		 * @param b 加数
		 * @return 和
		 */
		public static int staticAdd(int a, int b) {
			return a + b;
		}
	}

	/**
	 * 枚举测试用。
	 */
	public enum TestEnum {
		/** A */
		A,
		/** B */
		B
	}
}
