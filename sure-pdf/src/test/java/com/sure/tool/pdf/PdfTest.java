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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 * PdfWriter / PdfUtil 测试：生成、文本回读、分页、表格、图片、合并与校验。
 */
public class PdfTest {

	@Test
	public void 生成PDF以魔数开头且可打开() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out).save();
		final byte[] bytes = out.toByteArray();
		assertEquals('%', bytes[0]);
		assertEquals('P', bytes[1]);
		assertEquals('D', bytes[2]);
		assertEquals('F', bytes[3]);
		assertEquals('-', bytes[4]);
		assertTrue("空文档页数应 ≥ 1", PdfUtil.pageCount(new ByteArrayInputStream(bytes)) >= 1);
	}

	@Test
	public void 文本回读() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out)
				.addHeading("Report Title")
				.addText("Hello suretool pdf")
				.save();
		final String text = PdfUtil.extractText(new ByteArrayInputStream(out.toByteArray()));
		assertTrue("文本应可提取", text.contains("Hello suretool pdf"));
	}

	@Test
	public void 长文本自动分页() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PdfWriter w = new PdfWriter(out);
		for (int i = 0; i < 120; i++) {
			w.addText("line number " + i);
		}
		w.save();
		assertTrue("120 行应跨多页", PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())) >= 2);
	}

	@Test
	public void 表格生成() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out)
				.addTable(List.of(
						List.of("Month", "Revenue"),
						List.of("Jul", "160"),
						List.of("Aug", "190")))
				.save();
		final byte[] bytes = out.toByteArray();
		assertTrue(PdfUtil.isPdf(new ByteArrayInputStream(bytes)));
		final String text = PdfUtil.extractText(new ByteArrayInputStream(bytes));
		assertTrue(text.contains("Month"));
		assertTrue(text.contains("190"));
	}

	@Test
	public void 空表格安全() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out).addTable(List.of()).save();
		assertTrue(PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())) >= 1);
	}

	@Test
	public void 图片插入() throws IOException {
		final BufferedImage img = new BufferedImage(60, 30, BufferedImage.TYPE_INT_RGB);
		final ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
		ImageIO.write(img, "png", imgOut);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out)
				.addImage(new ByteArrayInputStream(imgOut.toByteArray()))
				.save();
		assertTrue(PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())) >= 1);
	}

	@Test
	public void newPage强制分页() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out).addText("page one").newPage().addText("page two").save();
		assertEquals(2, PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	public void 合并两文档页数相加() {
		final ByteArrayOutputStream a = new ByteArrayOutputStream();
		new PdfWriter(a).addText("a1").newPage().addText("a2").save();
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		new PdfWriter(b).addText("b1").save();
		final ByteArrayOutputStream merged = new ByteArrayOutputStream();
		PdfUtil.merge(new ByteArrayInputStream(a.toByteArray()),
				new ByteArrayInputStream(b.toByteArray()), merged);
		assertEquals(3, PdfUtil.pageCount(new ByteArrayInputStream(merged.toByteArray())));
	}

	@Test
	public void 非PDF输入抛异常() {
		final ByteArrayInputStream notPdf = new ByteArrayInputStream("plain text".getBytes(StandardCharsets.UTF_8));
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> PdfUtil.extractText(notPdf));
		assertTrue(ex.getMessage().contains("%PDF-"));
		assertFalse(PdfUtil.isPdf(new ByteArrayInputStream("x".getBytes())));
	}

	@Test
	public void 文件重载() throws IOException {
		final File tmp = File.createTempFile("sure-pdf-test", ".pdf");
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			new PdfWriter(out).addText("file based").save();
			try (FileOutputStream fos = new FileOutputStream(tmp)) {
				fos.write(out.toByteArray());
			}
			assertTrue(PdfUtil.extractText(tmp).contains("file based"));
			assertEquals(1, PdfUtil.pageCount(new java.io.FileInputStream(tmp)));
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void null参数校验() {
		assertThrows(IllegalArgumentException.class, () -> new PdfWriter(null));
		assertThrows(IllegalArgumentException.class, () -> PdfUtil.extractText((File) null));
		assertThrows(IllegalArgumentException.class, () -> PdfUtil.pageCount(null));
		assertThrows(IllegalArgumentException.class, () -> PdfUtil.isPdf(null));
		assertThrows(IllegalArgumentException.class, () -> PdfUtil.merge(null, null, null));
	}
}
