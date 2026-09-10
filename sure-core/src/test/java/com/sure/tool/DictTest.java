package com.sure.tool;

import com.sure.tool.lang.Dict;
import org.junit.Assert;
import org.junit.Test;

/**
 * Dict 单元测试。
 */
public class DictTest {

	@Test
	public void testOfAndSet() {
		Dict dict = Dict.of("name", "sure", "age", 18);
		Assert.assertEquals(2, dict.size());
		Assert.assertEquals("sure", dict.get("name"));
		dict.set("active", true);
		Assert.assertEquals(Boolean.TRUE, dict.get("active"));
	}

	@Test
	public void testTypedGet() {
		Dict dict = Dict.of("name", "sure", "age", 18, "score", 99.5, "ok", true, "str", "42");
		Assert.assertEquals("sure", dict.getStr("name"));
		Assert.assertEquals(Integer.valueOf(18), dict.getInt("age"));
		Assert.assertEquals(Integer.valueOf(42), dict.getInt("str"));
		Assert.assertNull(dict.getInt("missing"));
		Assert.assertEquals(Long.valueOf(18L), dict.getLong("age"));
		Assert.assertEquals(Double.valueOf(99.5), dict.getDouble("score"));
		Assert.assertEquals(Boolean.TRUE, dict.getBool("ok"));
		Assert.assertNull(dict.getBool("missing"));
		Assert.assertEquals("default", dict.getStr("missing", "default"));
	}

	@Test
	public void testConvertFromString() {
		Dict dict = Dict.of("int", "123", "double", "1.5", "bool", "true", "bad", "abc");
		Assert.assertEquals(Integer.valueOf(123), dict.getInt("int"));
		Assert.assertEquals(Double.valueOf(1.5), dict.getDouble("double"));
		Assert.assertEquals(Boolean.TRUE, dict.getBool("bool"));
		Assert.assertNull(dict.getInt("bad"));
		Assert.assertNull(dict.getDouble("bad"));
		Assert.assertNull(dict.getBool("bad"));
	}

	@Test
	public void testBoolAliases() {
		Dict dict = Dict.of("a", "1", "b", "yes", "c", "是", "d", "0", "e", "no", "f", "否");
		Assert.assertEquals(Boolean.TRUE, dict.getBool("a"));
		Assert.assertEquals(Boolean.TRUE, dict.getBool("b"));
		Assert.assertEquals(Boolean.TRUE, dict.getBool("c"));
		Assert.assertEquals(Boolean.FALSE, dict.getBool("d"));
		Assert.assertEquals(Boolean.FALSE, dict.getBool("e"));
		Assert.assertEquals(Boolean.FALSE, dict.getBool("f"));
	}
}
