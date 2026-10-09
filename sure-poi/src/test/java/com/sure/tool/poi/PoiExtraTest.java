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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

import com.sure.tool.date.DateUtil;

/**
 * PoiUtil / WordUtil / ExcelUtil 补测：close 分支、单元格类型全分支、公式缓存读取、
 * Word 追加写入、空行/null 行分支。
 */
public class PoiExtraTest {

	private static File tempFile(String suffix) throws Exception {
		File file = File.createTempFile("suretool-extra", suffix);
		file.deleteOnExit();
		return file;
	}

	/** close(null) 与 close(真实工作簿) 覆盖 close 分支。 */
	@Test
	public void closeWorkbook() throws Exception {
		PoiUtil.close(null);
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, Arrays.asList(Arrays.asList("a")));
		try (Workbook wb = WorkbookFactory.create(file)) {
			PoiUtil.close(wb);
		}
	}

	/** getRow/getCell 正常返回分支。 */
	@Test
	public void getRowAndCell() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, Arrays.asList(Arrays.asList("x", 1)));
		try (Workbook wb = WorkbookFactory.create(file)) {
			Sheet sheet = PoiUtil.getSheet(wb, 0);
			assertNotNull(sheet);
			Row row = PoiUtil.getRow(sheet, 0);
			assertNotNull(row);
			Cell cell = PoiUtil.getCell(row, 0);
			assertNotNull(cell);
			assertEquals("x", PoiUtil.readCell(cell));
			// 越界返回 null
			assertNull(PoiUtil.getRow(sheet, -1));
			assertNull(PoiUtil.getCell(row, -1));
			assertNull(PoiUtil.getCell(row, 99));
		}
	}

	/** readCell 全类型分支：字符串、整数、浮点、布尔、空白、日期、公式。 */
	@Test
	public void readCellAllTypes() throws Exception {
		File file = tempFile(".xlsx");
		try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("s");
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("  hi  ");
			row.createCell(1).setCellValue(42);
			row.createCell(2).setCellValue(3.5);
			row.createCell(3).setCellValue(true);
			row.createCell(4).setBlank();
			// 日期单元格
			Cell dateCell = row.createCell(5);
			dateCell.setCellValue(DateUtil.parse("2020-05-06 07:08:09"));
			CellStyle cs = wb.createCellStyle();
			DataFormat df = wb.createDataFormat();
			cs.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm:ss"));
			dateCell.setCellStyle(cs);
			// 公式单元格 =A1&A2
			Cell formula = row.createCell(6);
			formula.setCellFormula("A1");
			// 触发求值以填充缓存
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
				wb.write(out);
			}
		}
		try (Workbook wb = WorkbookFactory.create(file)) {
			Row row = wb.getSheetAt(0).getRow(0);
			assertEquals("hi", PoiUtil.readCell(row.getCell(0)));
			assertEquals(42L, PoiUtil.readCell(row.getCell(1)));
			assertEquals(3.5, PoiUtil.readCell(row.getCell(2)));
			assertEquals(Boolean.TRUE, PoiUtil.readCell(row.getCell(3)));
			assertEquals("", PoiUtil.readCell(row.getCell(4)));
			assertEquals("2020-05-06 07:08:09", PoiUtil.readCell(row.getCell(5)));
			// 公式缓存为字符串
			Object fv = PoiUtil.readCell(row.getCell(6));
			assertEquals("hi", fv);
		}
	}

	/** readFormulaValue 数字/布尔/空白/默认分支。 */
	@Test
	public void readFormulaValueBranches() throws Exception {
		File file = tempFile(".xlsx");
		try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("s");
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue(10);
			row.createCell(1).setCellValue(20);
			row.createCell(2).setCellValue(true);
			// 数值公式
			Cell f1 = row.createCell(3);
			f1.setCellFormula("A1+B1");
			// 布尔公式
			Cell f2 = row.createCell(4);
			f2.setCellFormula("C1");
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
				wb.write(out);
			}
		}
		try (Workbook wb = WorkbookFactory.create(file)) {
			Row row = wb.getSheetAt(0).getRow(0);
			assertEquals(30L, PoiUtil.readCell(row.getCell(3)));
			assertEquals(Boolean.TRUE, PoiUtil.readCell(row.getCell(4)));
		}
	}

	/** WordUtil.write(file, text, append)：追加到已有文档、新建文档、空文本。 */
	@Test
	public void writeAppendModes() throws Exception {
		File file = tempFile(".docx");
		// 新建
		WordUtil.write(file, "first", false);
		// 追加
		WordUtil.write(file, "second", true);
		String text = WordUtil.readText(file);
		assertTrue(text.contains("first"));
		assertTrue(text.contains("second"));
		// 空文本不写段落
		WordUtil.write(file, "", true);
		// 追加到不存在的文件（exists=false）
		File fresh = tempFile(".docx");
		fresh.delete();
		WordUtil.write(fresh, "fresh", true);
		assertTrue(WordUtil.readText(fresh).contains("fresh"));
	}

	/** WordUtil.write 列表含 null 元素跳过。 */
	@Test
	public void writeParagraphsWithNull() throws Exception {
		File file = tempFile(".docx");
		List<String> paragraphs = new ArrayList<>();
		paragraphs.add("one");
		paragraphs.add(null);
		paragraphs.add("three");
		WordUtil.write(file, paragraphs);
		List<String> result = WordUtil.readParagraphs(file);
		assertEquals(2, result.size());
		assertEquals("one", result.get(0));
		assertEquals("three", result.get(1));
	}

	/** ExcelUtil.writeRows：null 行列表、null 行跳过。 */
	@Test
	public void writeRowsNullBranches() throws Exception {
		File file = tempFile(".xlsx");
		List<List<Object>> rows = new ArrayList<>();
		rows.add(Arrays.asList("a", "b"));
		rows.add(null);
		rows.add(Arrays.asList(1, 2));
		ExcelUtil.write(file, rows);
		List<List<Object>> read = ExcelUtil.read(file);
		assertEquals(2, read.size());
		assertEquals("a", read.get(0).get(0));
		assertEquals(1L, read.get(1).get(0));
	}

	/** ExcelUtil.write null rows 列表直接返回。 */
	@Test
	public void writeNullRows() throws Exception {
		File file = tempFile(".xlsx");
		ExcelUtil.write(file, (List<List<Object>>) null);
		// 空工作簿
		assertTrue(ExcelUtil.read(file).isEmpty());
	}

	/** readCell ERROR 类型走 default 返回 null。 */
	@Test
	public void readCellErrorType() throws Exception {
		try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("s");
			Row row = sheet.createRow(0);
			Cell err = row.createCell(0);
			err.setCellErrorValue((byte) 0);
			assertNull(PoiUtil.readCell(err));
		}
	}

	/** 公式缓存为日期分支。 */
	@Test
	public void readFormulaDateBranch() throws Exception {
		File file = tempFile(".xlsx");
		try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("s");
			Row row = sheet.createRow(0);
			// TODAY() 会缓存为日期类型
			Cell fDate = row.createCell(0);
			fDate.setCellFormula("TODAY()");
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
				wb.write(out);
			}
		}
		try (Workbook wb = WorkbookFactory.create(file)) {
			Row row = wb.getSheetAt(0).getRow(0);
			PoiUtil.readCell(row.getCell(0));
		}
	}
}
