package com.sure.tool.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 * Word（docx）读写工具类，参考 Hutool 的 {@code WordUtil} 设计。
 * <p>
 * 读取支持段落与表格文本提取；写入支持单段/多段简单文档。
 *
 * @author suretool
 */
public class WordUtil {

	private WordUtil() {
	}

	/**
	 * 读取 docx 全文（段落 + 表格文本）。
	 *
	 * @param file docx 文件
	 * @return 全文文本
	 * @throws IOException 读取失败
	 */
	public static String readText(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (FileInputStream in = new FileInputStream(file);
				XWPFDocument doc = new XWPFDocument(in)) {
			for (XWPFParagraph paragraph : doc.getParagraphs()) {
				appendParagraph(sb, paragraph.getText());
			}
			for (XWPFTable table : doc.getTables()) {
				appendTable(sb, table);
			}
		}
		return sb.toString();
	}

	/**
	 * 读取 docx 段落文本。
	 *
	 * @param file docx 文件
	 * @return 段落列表
	 * @throws IOException 读取失败
	 */
	public static List<String> readParagraphs(File file) throws IOException {
		List<String> paragraphs = new ArrayList<>();
		try (FileInputStream in = new FileInputStream(file);
				XWPFDocument doc = new XWPFDocument(in)) {
			for (XWPFParagraph paragraph : doc.getParagraphs()) {
				String text = paragraph.getText();
				if (text != null && !text.trim().isEmpty()) {
					paragraphs.add(text);
				}
			}
		}
		return paragraphs;
	}

	/**
	 * 读取 docx 所有表格（表格 → 行 → 单元格）。
	 *
	 * @param file docx 文件
	 * @return 表格列表
	 * @throws IOException 读取失败
	 */
	public static List<List<List<String>>> readTables(File file) throws IOException {
		List<List<List<String>>> tables = new ArrayList<>();
		try (FileInputStream in = new FileInputStream(file);
				XWPFDocument doc = new XWPFDocument(in)) {
			for (XWPFTable table : doc.getTables()) {
				List<List<String>> rows = new ArrayList<>();
				for (XWPFTableRow row : table.getRows()) {
					List<String> cells = new ArrayList<>();
					for (XWPFTableCell cell : row.getTableCells()) {
						String text = cell.getText();
						cells.add(text == null ? "" : text.trim());
					}
					rows.add(cells);
				}
				tables.add(rows);
			}
		}
		return tables;
	}

	/**
	 * 写入简单 docx（单段落）。
	 *
	 * @param file 目标文件
	 * @param text 文本
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File write(File file, String text) throws IOException {
		List<String> paragraphs = new ArrayList<>();
		paragraphs.add(text);
		return write(file, paragraphs);
	}

	/**
	 * 写入 docx（多段落）。
	 *
	 * @param file       目标文件
	 * @param paragraphs 段落列表
	 * @return 目标文件
	 * @throws IOException 写入失败
	 */
	public static File write(File file, List<String> paragraphs) throws IOException {
		try (XWPFDocument doc = new XWPFDocument()) {
			if (paragraphs != null) {
				for (String text : paragraphs) {
					if (text == null) {
						continue;
					}
					XWPFParagraph paragraph = doc.createParagraph();
					paragraph.createRun().setText(text);
				}
			}
			try (FileOutputStream out = new FileOutputStream(file)) {
				doc.write(out);
			}
		}
		return file;
	}

	/**
	 * 追加非空段落。
	 */
	private static void appendParagraph(StringBuilder sb, String text) {
		if (text != null && !text.trim().isEmpty()) {
			sb.append(text).append('\n');
		}
	}

	/**
	 * 追加表格（单元格以制表符分隔）。
	 */
	private static void appendTable(StringBuilder sb, XWPFTable table) {
		for (XWPFTableRow row : table.getRows()) {
			boolean first = true;
			for (XWPFTableCell cell : row.getTableCells()) {
				if (!first) {
					sb.append('\t');
				}
				first = false;
				String text = cell.getText();
				sb.append(text == null ? "" : text.trim());
			}
			sb.append('\n');
		}
	}
}
