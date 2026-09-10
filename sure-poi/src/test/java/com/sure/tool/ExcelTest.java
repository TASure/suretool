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
package com.sure.tool.poi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sure.tool.date.DateUtil;

/**
 * ExcelUtil / PoiUtil 测试：读写往返、类型感知、表头读取、Bean 写入。
 */
public class ExcelTest {

	private static File tempFile(String suffix) throws Exception {
		File file = File.createTempFile("suretool-office", suffix);
		file.deleteOnExit();
		return file;
	}

	@Test
	public void testWriteReadRoundTrip() throws Exception {
		File file = tempFile(".xlsx");
		List<List<Object>> rows = new ArrayList<>();
		rows.add(Arrays.asList("name", "age", "score", "active", "birth"));
		rows.add(Arrays.asList("sure", 18, 88.5, true, DateUtil.parse("2000-01-02 03:04:05")));

		ExcelUtil.write(file, rows);
		List<List<Object>> read = ExcelUtil.read(file);

		assertEquals(2, read.size());
		assertEquals("name", read.get(0).get(0));
		assertEquals("sure", read.get(1).get(0));
		assertEquals(18L, read.get(1).get(1));
		assertEquals(88.5, read.get(1).get(2));
		assertEquals(Boolean.TRUE, read.get(1).get(3));
		assertEquals("2000-01-02 03:04:05", read.get(1).get(4));
	}

	@Test
	public void testReadSheetNames() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, "data", Arrays.asList(Arrays.asList("a")));
		assertEquals(Arrays.asList("data"), ExcelUtil.readSheetNames(file));
	}

	@Test
	public void testReadWithHeader() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, Arrays.asList(
				Arrays.asList("id", "name"),
				Arrays.asList(1, "sure"),
				Arrays.asList(2, "tool")));

		List<Map<String, Object>> rows = ExcelUtil.readWithHeader(file);
		assertEquals(2, rows.size());
		assertEquals(1L, rows.get(0).get("id"));
		assertEquals("sure", rows.get(0).get("name"));
		assertEquals("tool", rows.get(1).get("name"));
	}

	@Test
	public void testWriteBeans() throws Exception {
		File file = tempFile(".xlsx");
		Person person = new Person();
		person.setName("sure");
		person.setAge(18);
		ExcelUtil.writeBeans(file, Arrays.asList(person));

		List<Map<String, Object>> rows = ExcelUtil.readWithHeader(file);
		assertEquals(1, rows.size());
		assertEquals("sure", rows.get(0).get("name"));
		assertEquals(18L, rows.get(0).get("age"));
	}

	@Test
	public void testReadEmptyOrMissingSheet() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, new ArrayList<>());
		assertTrue(ExcelUtil.read(file).isEmpty());
		assertTrue(ExcelUtil.read(file, 5).isEmpty());
		assertTrue(ExcelUtil.readWithHeader(file).isEmpty());
	}

	@Test
	public void testPoiUtilCellAccess() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, Arrays.asList(
				Arrays.asList("txt", 42, 3.14, true, null),
				Arrays.asList("", "space  ", 0, false, 99)));

		List<List<Object>> rows = ExcelUtil.read(file);
		assertEquals("txt", rows.get(0).get(0));
		assertEquals(42L, rows.get(0).get(1));
		assertEquals(3.14, rows.get(0).get(2));
		assertEquals(Boolean.TRUE, rows.get(0).get(3));
		// 未写入的单元格（null）读为空串
		assertEquals("", rows.get(0).get(4));
		assertEquals("space", rows.get(1).get(1));
		assertEquals(0L, rows.get(1).get(2));
	}

	@Test
	public void testPoiUtilHelpers() throws Exception {
		assertNull(PoiUtil.getSheet(null, 0));
		assertNull(PoiUtil.getRow(null, 0));
		assertNull(PoiUtil.getCell(null, 0));
		assertNull(PoiUtil.readCell(null));
		assertTrue(PoiUtil.isXlsx("a.XLSX"));
		assertFalse(PoiUtil.isXlsx("a.xls"));
		assertFalse(PoiUtil.isXlsx(null));
	}

	/**
	 * 测试 Bean。
	 */
	public static class Person {

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
}