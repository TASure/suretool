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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Test;

/**
 * WordUtil 测试：段落读写、表格读取。
 */
public class WordTest {

	private static File tempFile() throws Exception {
		File file = File.createTempFile("suretool-word", ".docx");
		file.deleteOnExit();
		return file;
	}

	@Test
	public void testWriteReadText() throws Exception {
		File file = tempFile();
		WordUtil.write(file, "hello suretool");
		assertEquals("hello suretool\n", WordUtil.readText(file));
	}

	@Test
	public void testWriteReadParagraphs() throws Exception {
		File file = tempFile();
		WordUtil.write(file, Arrays.asList("first", "second", "third"));
		assertEquals(Arrays.asList("first", "second", "third"), WordUtil.readParagraphs(file));
		assertTrue(WordUtil.readText(file).contains("first\nsecond\nthird\n"));
	}

	@Test
	public void testReadTables() throws Exception {
		File file = tempFile();
		// 用 POI 直接构造带表格的 docx
		try (XWPFDocument doc = new XWPFDocument()) {
			doc.createParagraph().createRun().setText("before table");
			XWPFTable table = doc.createTable(2, 2);
			table.getRow(0).getCell(0).setText("h1");
			table.getRow(0).getCell(1).setText("h2");
			table.getRow(1).getCell(0).setText("v1");
			table.getRow(1).getCell(1).setText("v2");
			try (FileOutputStream out = new FileOutputStream(file)) {
				doc.write(out);
			}
		}

		String text = WordUtil.readText(file);
		assertTrue(text.contains("before table"));
		assertTrue(text.contains("h1\th2"));

		List<List<List<String>>> tables = WordUtil.readTables(file);
		assertEquals(1, tables.size());
		assertEquals(Arrays.asList("h1", "h2"), tables.get(0).get(0));
		assertEquals(Arrays.asList("v1", "v2"), tables.get(0).get(1));
	}

	@Test
	public void testWriteEmpty() throws Exception {
		File file = tempFile();
		WordUtil.write(file, new ArrayList<String>());
		assertEquals("", WordUtil.readText(file));
	}
}