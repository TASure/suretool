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
package com.sure.tool.pdf;

import static org.junit.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Test;

/**
 * PdfWriter 两处 IOException 防御分支补测：
 * newPage 关闭旧流失败（L219-220）、charWidth 字体宽度计算失败（L282-283）。
 * 通过反射注入抛异常的子类对象触发，不改 src/main。
 */
public class PdfGapTest {

	/** 替换内部 stream 的底层输出流为 close 抛 IOException，newPage 命中关闭旧流失败。 */
	@Test
	public void newPage关闭旧流失败() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out).addText("setup");
		java.lang.reflect.Field streamF = PdfWriter.class.getDeclaredField("stream");
		streamF.setAccessible(true);
		PDPageContentStream cs = (PDPageContentStream) streamF.get(w);
		java.lang.reflect.Field of = cs.getClass().getSuperclass().getDeclaredField("outputStream");
		of.setAccessible(true);
		of.set(cs, new java.io.OutputStream() {
			@Override
			public void write(int b) {
				// 只关闭时抛，写入保持静默
			}

			@Override
			public void close() throws IOException {
				throw new IOException("close boom");
			}
		});
		assertThrows(PdfRuntimeException.class, () -> w.newPage());
	}

	/** 替换 customFont 为 getStringWidth 抛 IOException 的子类，折行计算命中字体宽度失败。 */
	@Test
	public void 字体宽度计算失败() throws Exception {
		try (java.io.InputStream is = PdfGapTest.class.getResourceAsStream("/fonts/DejaVuSans.ttf");
				PDDocument holder = new PDDocument()) {
			PDType0Font base = PDType0Font.load(holder, is);
			PDType0Font evil = new PDType0Font(base.getCOSObject()) {
				@Override
				public float getStringWidth(String text) throws IOException {
					throw new IOException("width boom");
				}
			};
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PdfWriter w = new PdfWriter(out);
			w.setFont(evil);
			// 非空文本即触发 writeWrapped → charWidth → getStringWidth 抛异常
			assertThrows(PdfRuntimeException.class, () -> w.addText("hello"));
		}
	}
}
