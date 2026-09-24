package com.sure.tool;

import com.sure.tool.collection.MapUtil;
import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * P0 核心高频 API 增强测试（1.0.1 新增方法）。
 */
public class CoreEnhance01Test {

	// ---- StrUtil ----

	@Test
	public void subBefore_works() {
		assertEquals("hello", StrUtil.subBefore("hello@world", "@", false));
		assertEquals("hello", StrUtil.subBefore("hello@world@again", "@", false));
		assertEquals("hello@world", StrUtil.subBefore("hello@world@again", "@", true));
		assertEquals("hello", StrUtil.subBefore("hello", "@", false));
		assertEquals("", StrUtil.subBefore("@world", "@", false));
		assertNull(StrUtil.subBefore(null, "@", false));
		assertEquals("hello@world", StrUtil.subBefore("hello@world", null, false));
	}

	@Test
	public void subAfter_works() {
		assertEquals("world", StrUtil.subAfter("hello@world", "@", false));
		assertEquals("again", StrUtil.subAfter("hello@world@again", "@", true));
		assertEquals("world@again", StrUtil.subAfter("hello@world@again", "@", false));
		assertEquals("hello", StrUtil.subAfter("hello", "@", false));
		assertEquals("", StrUtil.subAfter("hello@", "@", false));
		assertNull(StrUtil.subAfter(null, "@", false));
	}

	@Test
	public void trimStartEnd_works() {
		assertEquals("abc", StrUtil.trimStart("  \tabc"));
		assertEquals("abc  ", StrUtil.trimStart("abc  "));
		assertEquals("abc", StrUtil.trimEnd("abc  \t"));
		assertEquals("  abc", StrUtil.trimEnd("  abc"));
		assertEquals("abc", StrUtil.trimStart(StrUtil.trimEnd("  abc  ")));
		assertNull(StrUtil.trimStart(null));
		assertEquals("", StrUtil.trimStart(""));
	}

	@Test
	public void surround_works() {
		assertEquals("\"abc\"", StrUtil.surround("abc", "\""));
		assertEquals("(abc(", StrUtil.surround("abc", "("));
		assertNull(StrUtil.surround(null, "\""));
		assertEquals("abc", StrUtil.surround("abc", null));
	}

	// ---- MapUtil ----

	@Test
	public void values_works() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		assertEquals(2, MapUtil.values(map).size());
		assertTrue(MapUtil.values(map).contains(1));
		assertTrue(MapUtil.values(map).contains(2));
		assertNotNull(MapUtil.values(null));
		assertTrue(MapUtil.values(null).isEmpty());
	}

	@Test
	public void getBigInteger_works() {
		Map<String, Object> map = new HashMap<>();
		map.put("n", "12345678901234567890");
		assertEquals(new BigInteger("12345678901234567890"), MapUtil.getBigInteger(map, "n"));
		map.put("m", 42L);
		assertEquals(new BigInteger("42"), MapUtil.getBigInteger(map, "m"));
		assertNull(MapUtil.getBigInteger(map, "missing"));
		assertNull(MapUtil.getBigInteger(map, "bad"));
	}

	@Test
	public void getEnum_works() {
		Map<String, Object> map = new HashMap<>();
		map.put("level", "HIGH");
		assertEquals(DemoLevel.HIGH, MapUtil.getEnum(map, "level", DemoLevel.class));
		map.put("level2", DemoLevel.LOW);
		assertEquals(DemoLevel.LOW, MapUtil.getEnum(map, "level2", DemoLevel.class));
		assertNull(MapUtil.getEnum(map, "missing", DemoLevel.class));
		assertNull(MapUtil.getEnum(map, "bad", DemoLevel.class));
	}

	@Test
	public void getLocalDate_works() {
		Map<String, Object> map = new HashMap<>();
		map.put("d", "2026-09-24");
		assertEquals(LocalDate.of(2026, 9, 24), MapUtil.getLocalDate(map, "d"));
		assertNull(MapUtil.getLocalDate(map, "missing"));
	}

	@Test
	public void getLocalDateTime_works() {
		Map<String, Object> map = new HashMap<>();
		map.put("t", "2026-09-24 10:30:00");
		assertEquals(LocalDateTime.of(2026, 9, 24, 10, 30, 0), MapUtil.getLocalDateTime(map, "t"));
		assertNull(MapUtil.getLocalDateTime(map, "missing"));
	}

	// ---- ConvertUtil ----

	@Test
	public void toSqlDate_works() {
		assertEquals("2026-09-24", String.valueOf(ConvertUtil.toSqlDate("2026-09-24")));
		assertEquals("2026-09-24", String.valueOf(ConvertUtil.toSqlDate("2026-09-24 10:30:00")));
		assertNotNull(ConvertUtil.toSqlDate(1787011200000L));
		assertNull(ConvertUtil.toSqlDate(null));
		assertNull(ConvertUtil.toSqlDate("not-a-date"));
	}

	@Test
	public void toTimestamp_works() {
		assertEquals("2026-09-24 10:30:00.0", String.valueOf(ConvertUtil.toTimestamp("2026-09-24 10:30:00")));
		assertNotNull(ConvertUtil.toTimestamp(1787011200000L));
		assertNull(ConvertUtil.toTimestamp(null));
	}

	enum DemoLevel {
		LOW, HIGH
	}
}
