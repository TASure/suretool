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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sure.tool.bean.BeanUtil;

/**
 * Excel 读写工具类（xlsx/xls），参考 Hutool 的 {@code ExcelUtil} 设计。
 * <p>
 * 读取时单元格类型感知（数字/日期/布尔/字符串），写入时按 Java 类型自动落格；
 * Bean 写入以字段名作为表头。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ExcelUtil {

	private ExcelUtil() {
	}

	/**
	 * 读取 Excel 第一个工作表（每行为单元格值列表）。
	 *
	 * @param file Excel 文件
	 * @return 行列表
	 * @throws IOException 读取失败
	 */
	public static List<List<Object>> read(File file) throws IOException {
		return read(file, 0);
	}

	/**
	 * 读取指定工作表。
	 *
	 * @param file       Excel 文件
	 * @param sheetIndex 工作表索引
	 * @return 行列表
	 * @throws IOException 读取失败
	 */
	public static List<List<Object>> read(File file, int sheetIndex) throws IOException {
		List<List<Object>> rows = new ArrayList<>();
		try (Workbook workbook = WorkbookFactory.create(file)) {
			Sheet sheet = PoiUtil.getSheet(workbook, sheetIndex);
			if (sheet == null) {
				return rows;
			}
			for (Row row : sheet) {
				List<Object> values = new ArrayList<>();
				for (Cell cell : row) {
					values.add(PoiUtil.readCell(cell));
				}
				rows.add(values);
			}
		}
		return rows;
	}

	/**
	 * 读取第一个工作表，第一行作为表头，返回 Map 列表。
	 *
	 * @param file Excel 文件
	 * @return Map 列表
	 * @throws IOException 读取失败
	 */
	public static List<Map<String, Object>> readWithHeader(File file) throws IOException {
		return readWithHeader(file, 0);
	}

	/**
	 * 读取指定工作表，第一行作为表头，返回 Map 列表。
	 *
	 * @param file       Excel 文件
	 * @param sheetIndex 工作表索引
	 * @return Map 列表
	 * @throws IOException 读取失败
	 */
	public static List<Map<String, Object>> readWithHeader(File file, int sheetIndex) throws IOException {
		List<List<Object>> rows = read(file, sheetIndex);
		List<Map<String, Object>> result = new ArrayList<>();
		if (rows.isEmpty()) {
			return result;
		}
		List<Object> header = rows.get(0);
		for (int i = 1; i < rows.size(); i++) {
			List<Object> row = rows.get(i);
			Map<String, Object> map = new LinkedHashMap<>();
			for (int c = 0; c < header.size(); c++) {
				map.put(String.valueOf(header.get(c)), c < row.size() ? row.get(c) : null);
			}
			result.add(map);
		}
		return result;
	}

	/**
	 * 工作表名称列表。
	 *
	 * @param file Excel 文件
	 * @return 名称列表
	 * @throws IOException 读取失败
	 */
	public static List<String> readSheetNames(File file) throws IOException {
		List<String> names = new ArrayList<>();
		try (Workbook workbook = WorkbookFactory.create(file)) {
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				names.add(workbook.getSheetName(i));
			}
		}
		return names;
	}

	/**
	 * 写入行数据到默认工作表。
	 *
	 * @param file 目标文件
	 * @param rows 行列表（每行为值列表）
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File write(File file, List<List<Object>> rows) throws IOException {
		return write(file, "Sheet1", rows);
	}

	/**
	 * 写入行数据到指定工作表。
	 *
	 * @param file      目标文件
	 * @param sheetName 工作表名
	 * @param rows      行列表（每行为值列表）
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File write(File file, String sheetName, List<List<Object>> rows) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet(sheetName);
			writeRows(sheet, rows);
			try (FileOutputStream out = new FileOutputStream(file)) {
				workbook.write(out);
			}
		}
		return file;
	}

	/**
	 * Bean 列表写入 Excel（字段名作为表头）。
	 *
	 * @param file  目标文件
	 * @param beans Bean 列表
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File writeBeans(File file, List<?> beans) throws IOException {
		return writeBeans(file, "Sheet1", beans);
	}

	/**
	 * Bean 列表写入 Excel（字段名作为表头）。
	 *
	 * @param file      目标文件
	 * @param sheetName 工作表名
	 * @param beans     Bean 列表
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File writeBeans(File file, String sheetName, List<?> beans) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet(sheetName);
			if (beans != null && !beans.isEmpty()) {
				List<Map<String, Object>> rows = new ArrayList<>();
				for (Object bean : beans) {
					rows.add(BeanUtil.beanToMap(bean, false));
				}
				List<String> fieldNames = new ArrayList<>(rows.get(0).keySet());
				Row header = sheet.createRow(0);
				for (int c = 0; c < fieldNames.size(); c++) {
					header.createCell(c).setCellValue(fieldNames.get(c));
				}
				for (int r = 0; r < rows.size(); r++) {
					Row row = sheet.createRow(r + 1);
					Map<String, Object> map = rows.get(r);
					for (int c = 0; c < fieldNames.size(); c++) {
						setCellValue(row.createCell(c), map.get(fieldNames.get(c)));
					}
				}
			}
			try (FileOutputStream out = new FileOutputStream(file)) {
				workbook.write(out);
			}
		}
		return file;
	}

	/**
	 * 写入行数据。
	 */
	private static void writeRows(Sheet sheet, List<List<Object>> rows) {
		if (rows == null) {
			return;
		}
		for (int r = 0; r < rows.size(); r++) {
			List<Object> rowValues = rows.get(r);
			if (rowValues == null) {
				continue;
			}
			Row row = sheet.createRow(r);
			for (int c = 0; c < rowValues.size(); c++) {
				setCellValue(row.createCell(c), rowValues.get(c));
			}
		}
	}

	/**
	 * 按 Java 类型写入单元格。
	 */
	private static void setCellValue(Cell cell, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				cell.setCellValue(((Number) value).doubleValue());
			} else {
				cell.setCellValue(((Number) value).longValue());
			}
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Date) {
			cell.setCellValue(com.sure.tool.date.DateUtil.format((Date) value));
		} else {
			cell.setCellValue(String.valueOf(value));
		}
	}
}