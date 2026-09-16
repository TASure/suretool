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
package com.sure.tool.collection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.sure.tool.io.FileUtil;

/**
 * CSV 读写工具类（最小实现），支持引号包裹、引号内逗号与双引号转义，参考 Hutool 的 {@code CsvUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CsvUtil {

	private CsvUtil() {
	}

	/**
	 * 解析 CSV 字符串。
	 *
	 * @param csv CSV 内容
	 * @return 行列表（每行为字段列表）
	 */
	public static List<List<String>> read(String csv) {
		List<List<String>> rows = new ArrayList<>();
		if (csv == null || csv.isEmpty()) {
			return rows;
		}
		List<String> row = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < csv.length(); i++) {
			char c = csv.charAt(i);
			if (inQuotes) {
				if (c == '"') {
					if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
						field.append('"');
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					field.append(c);
				}
			} else {
				if (c == '"') {
					inQuotes = true;
				} else if (c == ',') {
					row.add(field.toString());
					field.setLength(0);
				} else if (c == '\n' || c == '\r') {
					if (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
						i++;
					}
					row.add(field.toString());
					field.setLength(0);
					rows.add(row);
					row = new ArrayList<>();
				} else {
					field.append(c);
				}
			}
		}
		row.add(field.toString());
		rows.add(row);
		return rows;
	}

	/**
	 * 读取 CSV 文件（UTF-8）。
	 *
	 * @param file 文件
	 * @return 行列表
	 * @throws IOException 读取失败
	 */
	public static List<List<String>> read(File file) throws IOException {
		return read(FileUtil.readString(file, StandardCharsets.UTF_8));
	}

	/**
	 * 序列化为 CSV 字符串。
	 *
	 * @param rows 行列表（每行为字段列表）
	 * @return CSV 内容
	 */
	public static String toCsv(List<List<String>> rows) {
		if (rows == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean firstRow = true;
		for (List<String> row : rows) {
			if (!firstRow) {
				sb.append("\r\n");
			}
			firstRow = false;
			boolean firstField = true;
			if (row != null) {
				for (String field : row) {
					if (!firstField) {
						sb.append(',');
					}
					firstField = false;
					sb.append(quoteIfNeeded(field));
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 写入 CSV 文件（UTF-8）。
	 *
	 * @param file 文件
	 * @param rows 行列表
	 * @throws IOException 写入失败
	 */
	public static void write(File file, List<List<String>> rows) throws IOException {
		FileUtil.writeString(toCsv(rows), file, StandardCharsets.UTF_8);
	}

	/**
	 * 需要时加引号（字段含逗号/引号/换行）。
	 */
	private static String quoteIfNeeded(String field) {
		if (field == null) {
			return "";
		}
		boolean needQuote = field.indexOf(',') >= 0 || field.indexOf('"') >= 0
				|| field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0;
		if (!needQuote) {
			return field;
		}
		return '"' + field.replace("\"", "\"\"") + '"';
	}

	/**
	 * 读取 CSV 文件（指定字符集）。
	 *
	 * @param file    文件
	 * @param charset 字符集
	 * @return 行数据
	 * @throws IOException IO 异常
	 */
	public static List<List<String>> read(File file, Charset charset) throws IOException {
		return read(FileUtil.readString(file, charset));
	}

	/**
	 * 写入 CSV 文件（指定字符集）。
	 *
	 * @param file    文件
	 * @param rows    行数据
	 * @param charset 字符集
	 * @throws IOException IO 异常
	 */
	public static void write(File file, List<List<String>> rows, Charset charset) throws IOException {
		FileUtil.writeString(toCsv(rows), file, charset);
	}



	/**
	 * 读取 CSV 输入流。
	 *
	 * @param in      输入流
	 * @param charset 字符集
	 * @return 行列表
	 * @throws java.io.IOException IO 异常
	 */
	public static List<List<String>> read(java.io.InputStream in, Charset charset) throws java.io.IOException {
		if (in == null) {
			return new ArrayList<>();
		}
		byte[] bytes;
		try (java.io.InputStream input = in) {
			bytes = input.readAllBytes();
		}
		return read(new String(bytes, charset));
	}

	/**
	 * 写出 CSV 到 Writer。
	 *
	 * @param writer Writer
	 * @param rows   行列表
	 * @throws java.io.IOException IO 异常
	 */
	public static void write(java.io.Writer writer, List<List<String>> rows) throws java.io.IOException {
		writer.write(toCsv(rows));
		writer.flush();
	}


}