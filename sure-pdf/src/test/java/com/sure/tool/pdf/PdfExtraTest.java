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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

/**
 * PdfWriter / PdfUtil / PdfRuntimeException 补测：setTitle/setFont、空文本折行、
 * 表格分页重画表头、图片换页、异常分支与魔数校验。
 */
public class PdfExtraTest {

	@Test
	public void 异常构造器() {
		assertNotNull(new PdfRuntimeException("msg"));
		assertNotNull(new PdfRuntimeException("msg", new IOException("cause")));
	}

	@Test
	public void setTitleAndSetFont() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out).setTitle("我的标题");
		// 覆盖 setFont 方法体（字体对象仅赋值，不实际渲染）；字体随测试资源打包，三平台一致
		try (InputStream fontIs = PdfExtraTest.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
			try (PDDocument holder = new PDDocument()) {
				org.apache.pdfbox.pdmodel.font.PDType0Font font =
						org.apache.pdfbox.pdmodel.font.PDType0Font.load(holder, fontIs);
				w.setFont(font);
			}
		}
		w.addText("plain text").save();
		assertTrue(out.size() > 0);
	}

	@Test
	public void 空文本折行分支() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out)
				.addText("")
				.addText("line1\n\nline3")
				.save();
		assertTrue(out.size() > 0);
	}

	@Test
	public void 表格跨页重画表头() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out);
		var rows = new java.util.ArrayList<List<String>>();
		rows.add(List.of("h1", "h2"));
		for (int i = 0; i < 60; i++) {
			rows.add(List.of("r" + i, "v" + i));
		}
		w.addTable(rows).save();
		assertTrue(PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())) >= 2);
	}

	@Test
	public void 图片不足时换页() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out);
		// 先写很多文本把 y 推到底部
		for (int i = 0; i < 40; i++) {
			w.addText("filler line " + i);
		}
		// 生成一张图片
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
				400, 300, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", imgOut);
		w.addImage(new ByteArrayInputStream(imgOut.toByteArray()));
		w.save();
		assertTrue(PdfUtil.pageCount(new ByteArrayInputStream(out.toByteArray())) >= 2);
	}

	@Test
	public void 损坏图片抛异常() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out);
		assertThrows(RuntimeException.class,
				() -> w.addImage(new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8))));
	}

	@Test
	public void isPdf非markSupported流() {
		InputStream raw = new InputStream() {
			private final byte[] data = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8);
			private int idx;

			@Override
			public int read() {
				return idx < data.length ? data[idx++] & 0xFF : -1;
			}
		};
		assertTrue(PdfUtil.isPdf(raw));
	}

	@Test
	public void 不存在文件抛异常() {
		File f = new File("/suretool-no-such-file-xyz.pdf");
		assertThrows(PdfRuntimeException.class, () -> PdfUtil.extractText(f));
	}

	@Test
	public void 损坏PDF加载失败抛异常() {
		// 合法魔数但内容损坏
		byte[] bad = "%PDF-1.4 broken content".getBytes(StandardCharsets.UTF_8);
		assertThrows(PdfRuntimeException.class,
				() -> PdfUtil.extractText(new ByteArrayInputStream(bad)));
	}

	@Test
	public void 合并损坏PDF抛异常() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] bad = "%PDF-1.4 broken".getBytes(StandardCharsets.UTF_8);
		assertThrows(PdfRuntimeException.class, () -> PdfUtil.merge(
				new ByteArrayInputStream(bad),
				new ByteArrayInputStream(bad),
				out));
	}

	@Test
	public void 短于5字节非PDF() {
		assertFalse(PdfUtil.isPdf(new ByteArrayInputStream("%PD".getBytes(StandardCharsets.UTF_8))));
	}

	@Test
	public void 魔数不匹配() {
		assertFalse(PdfUtil.isPdf(new ByteArrayInputStream("XXXX-1.4".getBytes(StandardCharsets.UTF_8))));
	}

	/** readAll/closeQuietly 抛 IOException 的输入流。 */
	@Test
	public void 读取失败与关闭失败() {
		InputStream boom = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("read boom");
			}

			@Override
			public void close() throws IOException {
				throw new IOException("close boom");
			}
		};
		assertThrows(PdfRuntimeException.class, () -> PdfUtil.extractText(boom));
	}

	/** pageCount 损坏 PDF 抛异常。 */
	@Test
	public void pageCount损坏PDF() {
		byte[] bad = "%PDF-1.4 broken".getBytes(StandardCharsets.UTF_8);
		assertThrows(PdfRuntimeException.class,
				() -> PdfUtil.pageCount(new ByteArrayInputStream(bad)));
	}

	/** isPdf 读取抛 IOException（markSupported 流）。 */
	@Test
	public void isPdf读取失败() {
		InputStream boom = new java.io.BufferedInputStream(new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("isPdf boom");
			}
		});
		assertThrows(PdfRuntimeException.class, () -> PdfUtil.isPdf(boom));
	}

	/** extractText 读取成功但关闭失败（closeQuietly 分支）。 */
	@Test
	public void 关闭失败忽略() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out).addText("data").save();
		InputStream closeBoom = new ByteArrayInputStream(out.toByteArray()) {
			@Override
			public void close() throws IOException {
				throw new IOException("close boom");
			}
		};
		String text = PdfUtil.extractText(closeBoom);
		assertTrue(text.contains("data"));
	}

	/** save 到写入即抛异常的输出流触发 IOException。 */
	@Test
	public void save到损坏流() {
		java.io.OutputStream boom = new java.io.OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new IOException("write boom");
			}
		};
		PdfWriter w = new PdfWriter(boom).addText("hi");
		assertThrows(PdfRuntimeException.class, () -> w.save());
	}

	/** save 后继续写入触发 newPage/文本 IOException。 */
	@Test
	public void save后继续写入抛异常() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out).addText("first");
		w.save();
		assertThrows(RuntimeException.class, () -> w.addText("after save"));
	}
	@Test
	public void 图片流关闭失败() throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
				20, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", imgOut);
		InputStream closingBoom = new ByteArrayInputStream(imgOut.toByteArray()) {
			@Override
			public void close() throws IOException {
				throw new IOException("close boom");
			}
		};
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PdfWriter(out).addImage(closingBoom).save();
		assertTrue(out.size() > 0);
	}

	/** 反射关闭内部 document 后 newPage 关闭已关流，触发 IOException 分支。 */
	@Test
	public void 关闭文档后newPage抛异常() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out).addText("x");
		java.lang.reflect.Field f = PdfWriter.class.getDeclaredField("document");
		f.setAccessible(true);
		org.apache.pdfbox.pdmodel.PDDocument doc =
				(org.apache.pdfbox.pdmodel.PDDocument) f.get(w);
		doc.close();
		// newPage 关闭已关流 → IOException
		assertThrows(PdfRuntimeException.class, () -> w.newPage());
	}

	/** addImage 读取流抛 IOException，命中 catch。 */
	@Test
	public void addImage读取失败抛异常() {
		InputStream boom = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("image read boom");
			}
		};
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out);
		assertThrows(PdfRuntimeException.class, () -> w.addImage(boom));
	}

	/** deleteQuietly 删除非空目录失败，命中 deleteOnExit 分支。 */
	@Test
	public void deleteQuietly非空目录() throws Exception {
		File dir = File.createTempFile("sure-pdf-dir", "");
		dir.delete();
		dir.mkdir();
		File child = new File(dir, "inner.txt");
		child.createNewFile();
		java.lang.reflect.Method m = com.sure.tool.pdf.PdfUtil.class
				.getDeclaredMethod("deleteQuietly", File.class);
		m.setAccessible(true);
		m.invoke(null, dir);
		assertTrue(dir.exists());
		dir.delete();
		child.delete();
	}

	/** 反射替换底层 outputStream 为写时抛异常，触发 writeText IOException。 */
	@Test
	public void writeText流异常() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out).addText("setup");
		java.lang.reflect.Field sf = PdfWriter.class.getDeclaredField("stream");
		sf.setAccessible(true);
		org.apache.pdfbox.pdmodel.PDPageContentStream cs =
				(org.apache.pdfbox.pdmodel.PDPageContentStream) sf.get(w);
		java.lang.reflect.Field of = cs.getClass().getSuperclass()
				.getDeclaredField("outputStream");
		of.setAccessible(true);
		of.set(cs, new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new IOException("write boom");
			}
		});
		assertThrows(PdfRuntimeException.class, () -> w.addText("after"));
	}

	/** 反射替换底层 outputStream 为写时抛异常，触发 drawTableRow IOException。 */
	@Test
	public void drawTableRow流异常() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter w = new PdfWriter(out);
		java.lang.reflect.Field sf = PdfWriter.class.getDeclaredField("stream");
		sf.setAccessible(true);
		org.apache.pdfbox.pdmodel.PDPageContentStream cs =
				(org.apache.pdfbox.pdmodel.PDPageContentStream) sf.get(w);
		java.lang.reflect.Field of = cs.getClass().getSuperclass()
				.getDeclaredField("outputStream");
		of.setAccessible(true);
		of.set(cs, new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new IOException("table boom");
			}
		});
		assertThrows(PdfRuntimeException.class,
				() -> w.addTable(java.util.List.of(java.util.List.of("a", "b"))));
	}
}
