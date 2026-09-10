package com.sure.tool.poi;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * POI 通用门面：工作簿/行/单元格的容错访问与类型感知取值，参考 Hutool 的 {@code PoiUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class PoiUtil {

	private PoiUtil() {
	}

	/**
	 * 安静关闭工作簿。
	 *
	 * @param workbook 工作簿
	 */
	public static void close(Workbook workbook) {
		if (workbook != null) {
			try {
				workbook.close();
			} catch (IOException e) {
				// 忽略关闭异常
			}
		}
	}

	/**
	 * 按索引取 Sheet，越界或空返回 {@code null}。
	 *
	 * @param workbook 工作簿
	 * @param index    Sheet 索引
	 * @return Sheet 或 {@code null}
	 */
	public static Sheet getSheet(Workbook workbook, int index) {
		if (workbook == null || index < 0 || index >= workbook.getNumberOfSheets()) {
			return null;
		}
		return workbook.getSheetAt(index);
	}

	/**
	 * 按索引取行，越界或空返回 {@code null}。
	 *
	 * @param sheet Sheet
	 * @param index 行索引
	 * @return Row 或 {@code null}
	 */
	public static Row getRow(Sheet sheet, int index) {
		if (sheet == null || index < 0) {
			return null;
		}
		return sheet.getRow(index);
	}

	/**
	 * 按索引取单元格，越界或空返回 {@code null}。
	 *
	 * @param row   Row
	 * @param index 列索引
	 * @return Cell 或 {@code null}
	 */
	public static Cell getCell(Row row, int index) {
		if (row == null || index < 0) {
			return null;
		}
		return row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	}

	/**
	 * 类型感知读取单元格：字符串去空白、日期格式化为字符串、整数去小数点、公式取缓存值、空白返回空串。
	 *
	 * @param cell 单元格
	 * @return 值（null/字符串/数字/布尔）
	 */
	public static Object readCell(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return com.sure.tool.date.DateUtil.format(cell.getDateCellValue());
				}
				return normalizeNumber(cell.getNumericCellValue());
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case FORMULA:
				return readFormulaValue(cell);
			case BLANK:
				return "";
			default:
				return null;
		}
	}

	/**
	 * 读取公式单元格的缓存结果值。
	 */
	private static Object readFormulaValue(Cell cell) {
		switch (cell.getCachedFormulaResultType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return com.sure.tool.date.DateUtil.format(cell.getDateCellValue());
				}
				return normalizeNumber(cell.getNumericCellValue());
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case BLANK:
				return "";
			default:
				return null;
		}
	}

	/**
	 * 整数值返回 Long，否则返回 Double。
	 */
	private static Object normalizeNumber(double value) {
		if (value == Math.floor(value) && !Double.isInfinite(value)) {
			return (long) value;
		}
		return value;
	}

	/**
	 * 是否 xlsx 文件（按扩展名判断）。
	 *
	 * @param fileName 文件名
	 * @return 是否 xlsx
	 */
	public static boolean isXlsx(String fileName) {
		return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
	}
}
