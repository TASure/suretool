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
package com.sure.tool.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sure.tool.poi.ExcelUtil;
import com.sure.tool.poi.WordUtil;

/**
 * Office 文档示例（ExcelUtil / WordUtil）。
 */
public class PoiDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws IOException {
		System.out.println("=== PoiDemo ===");
		List<List<Object>> rows = new ArrayList<>();
		rows.add(Arrays.asList("姓名", "年龄", "城市"));
		rows.add(Arrays.asList("Alice", 30, "西安"));
		rows.add(Arrays.asList("Bob", 25, "北京"));
		File xlsx = ExcelUtil.write(new File("target/demo.xlsx"), "人员", rows);
		System.out.println("excel written = " + xlsx.exists());
		System.out.println("excel read = " + ExcelUtil.read(xlsx, 0));
		System.out.println("sheets = " + ExcelUtil.readSheetNames(xlsx));

		File docx = WordUtil.write(new File("target/demo.docx"), Arrays.asList("第一段", "第二段"));
		System.out.println("word written = " + docx.exists());
		System.out.println("word paragraphs = " + WordUtil.readParagraphs(docx));
	}
}
