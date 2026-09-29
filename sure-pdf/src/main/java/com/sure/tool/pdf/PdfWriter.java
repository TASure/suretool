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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.sure.tool.lang.Assert;

/**
 * PDF 文档写入器（基于 Apache PDFBox 3）。
 *
 * <p>流式链式 API，覆盖文本 / 标题 / 表格 / 图片，自动分页。
 * 默认使用标准 14 字体（Helvetica，英文数字开箱即用）；
 * 中文字体需通过 {@link #setFont(PDType0Font)} 显式加载 TTF。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * try (FileOutputStream out = new FileOutputStream("report.pdf")) {
 *     new PdfWriter(out)
 *         .setTitle("月度报表")
 *         .addHeading("收入明细")
 *         .addText("Q3 收入 520 万元")
 *         .addTable(List.of(List.of("月份", "收入"), List.of("7月", "160")))
 *         .save();
 * }
 * }</pre>
 *
 * <p>线程安全：单文档单线程写入，非线程安全。</p>
 *
 * @since 1.3.0
 */
public final class PdfWriter {

	/** A4 纵向页面尺寸 */
	private static final PDRectangle PAGE = PDRectangle.A4;
	/** 页边距（pt） */
	private static final float MARGIN = 56F;
	/** 正文行距 */
	private static final float LINE_SPACING = 16F;
	/** 标题行距 */
	private static final float HEADING_SPACING = 22F;
	/** 表格行高 */
	private static final float TABLE_ROW_HEIGHT = 20F;
	/** 正文字体（标准 14，英文数字开箱即用） */
	private static final PDType1Font FONT_TEXT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	/** 标题字体 */
	private static final PDType1Font FONT_HEADING = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

	private final PDDocument document;
	/** 输出流为外部可变对象，按 Hutool/项目惯例抑制 SpotBugs EI_EXPOSE_REP2 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
	private final OutputStream out;
	private PDPageContentStream stream;
	private float y;
	/** 自定义字体（如中文 TTF），设置后 addText 使用之；字体对象为外部可变对象，惯例抑制 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
	private PDFont customFont;

	/**
	 * 构造写入器。
	 *
	 * @param out 输出流（save 时写入，不关闭该流）
	 * @throws IllegalArgumentException 输出流为 null 时抛出
	 */
	public PdfWriter(OutputStream out) {
		Assert.notNull(out, "输出流不能为 null");
		this.out = out;
		this.document = new PDDocument();
		this.newPage();
	}

	/**
	 * 设置文档标题（元数据）。
	 *
	 * @param title 标题
	 * @return this
	 */
	public PdfWriter setTitle(String title) {
		document.getDocumentInformation().setTitle(title);
		return this;
	}

	/**
	 * 追加标题行（加粗，字号略大）。
	 *
	 * @param text 标题文本
	 * @return this
	 */
	public PdfWriter addHeading(String text) {
		Assert.notNull(text, "标题文本不能为 null");
		ensureSpace(HEADING_SPACING);
		writeText(text, FONT_HEADING, 14F);
		y -= HEADING_SPACING;
		return this;
	}

	/**
	 * 追加文本行（支持换行与超宽自动折行）。
	 *
	 * @param text 文本
	 * @return this
	 */
	public PdfWriter addText(String text) {
		Assert.notNull(text, "文本不能为 null");
		final PDFont font = customFont != null ? customFont : FONT_TEXT;
		for (final String line : text.split("\\R", -1)) {
			writeWrapped(line, font, 12F);
		}
		return this;
	}

	/**
	 * 追加简单表格（首行为表头加粗，行内超宽截断，自动分页重画表头）。
	 *
	 * @param rows 表格行（List&lt;List&lt;String&gt;&gt;），列数以首行为准；null/空行安全返回
	 * @return this
	 */
	public PdfWriter addTable(List<List<String>> rows) {
		Assert.notNull(rows, "表格行不能为 null");
		if (rows.isEmpty() || rows.get(0).isEmpty()) {
			return this;
		}
		final int columns = rows.get(0).size();
		final float contentWidth = PAGE.getWidth() - 2 * MARGIN;
		final float colWidth = contentWidth / columns;
		int rowIndex = 0;
		for (final List<String> row : rows) {
			if (y - TABLE_ROW_HEIGHT < MARGIN) {
				newPage();
				// 分页后重画表头
				if (rowIndex > 0) {
					drawTableRow(rows.get(0), colWidth, true);
					y -= TABLE_ROW_HEIGHT;
				}
			}
			drawTableRow(row, colWidth, rowIndex == 0);
			y -= TABLE_ROW_HEIGHT;
			rowIndex++;
		}
		return this;
	}

	/**
	 * 插入图片（JPEG/PNG，等比缩放至内容宽度，居中）。
	 *
	 * @param in 图片输入流（调用后关闭）
	 * @return this
	 */
	public PdfWriter addImage(InputStream in) {
		Assert.notNull(in, "图片输入流不能为 null");
		try {
			final byte[] bytes = in.readAllBytes();
			final PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, "image");
			final float maxWidth = PAGE.getWidth() - 2 * MARGIN;
			final float scale = Math.min(1F, maxWidth / image.getWidth());
			final float width = image.getWidth() * scale;
			final float height = image.getHeight() * scale;
			if (y - height < MARGIN) {
				newPage();
			}
			stream.drawImage(image, MARGIN + (maxWidth - width) / 2, y - height, width, height);
			y -= height + 8F;
			return this;
		} catch (IOException e) {
			throw new PdfRuntimeException("图片写入失败: " + e.getMessage(), e);
		} finally {
			try {
				in.close();
			} catch (IOException ignored) {
				// 输入流关闭失败忽略
			}
		}
	}

	/**
	 * 强制换页。
	 *
	 * @return this
	 */
	public PdfWriter newPage() {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				throw new PdfRuntimeException("页面流关闭失败: " + e.getMessage(), e);
			}
		}
		final PDPage page = new PDPage(PAGE);
		document.addPage(page);
		try {
			stream = new PDPageContentStream(document, page);
		} catch (IOException e) {
			throw new PdfRuntimeException("页面创建失败: " + e.getMessage(), e);
		}
		y = PAGE.getHeight() - MARGIN;
		return this;
	}

	/**
	 * 写入并关闭文档（输出流不关闭）。
	 */
	public void save() {
		try {
			if (stream != null) {
				stream.close();
				stream = null;
			}
			document.save(out);
			document.close();
		} catch (IOException e) {
			throw new PdfRuntimeException("文档保存失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 设置自定义字体（如中文字体 TTF），后续 {@link #addText} 使用该字体。
	 *
	 * @param font PDFBox 字体对象（由调用方 {@code PDType0Font.load} 加载）
	 * @return this
	 */
	public PdfWriter setFont(PDType0Font font) {
		Assert.notNull(font, "字体不能为 null");
		this.customFont = font;
		return this;
	}

	private void ensureSpace(float needed) {
		if (y - needed < MARGIN) {
			newPage();
		}
	}

	/** 带折行的文本写入（按字符宽度近似，均分列宽内换行） */
	private void writeWrapped(String text, PDFont font, float fontSize) {
		if (text.isEmpty()) {
			ensureSpace(LINE_SPACING);
			y -= LINE_SPACING;
			return;
		}
		final float maxWidth = PAGE.getWidth() - 2 * MARGIN;
		final float charW = charWidth(font, fontSize);
		final int maxChars = Math.max(1, (int) (maxWidth / charW));
		for (int i = 0; i < text.length(); i += maxChars) {
			final String chunk = text.substring(i, Math.min(i + maxChars, text.length()));
			ensureSpace(LINE_SPACING);
			writeText(chunk, font, fontSize);
			y -= LINE_SPACING;
		}
	}

	/** 单字符宽度（pt），PDFBox 3 getStringWidth 抛 IOException 故收敛于此 */
	private static float charWidth(PDFont font, float fontSize) {
		try {
			return font.getStringWidth("W") / 1000F * fontSize;
		} catch (IOException e) {
			throw new PdfRuntimeException("字体宽度计算失败: " + e.getMessage(), e);
		}
	}

	private void writeText(String text, PDFont font, float fontSize) {
		try {
			stream.beginText();
			stream.setFont(font, fontSize);
			stream.newLineAtOffset(MARGIN, y);
			stream.showText(text);
			stream.endText();
		} catch (IOException e) {
			throw new PdfRuntimeException("文本写入失败: " + e.getMessage(), e);
		}
	}

	private void drawTableRow(List<String> row, float colWidth, boolean header) {
		final float top = y;
		final float bottom = y - TABLE_ROW_HEIGHT;
		try {
			// 画行边框
			stream.moveTo(MARGIN, top);
			stream.lineTo(MARGIN + colWidth * row.size(), top);
			stream.moveTo(MARGIN, bottom);
			stream.lineTo(MARGIN + colWidth * row.size(), bottom);
			stream.stroke();
			for (int i = 0; i < row.size(); i++) {
				final float x = MARGIN + i * colWidth;
				stream.moveTo(x, top);
				stream.lineTo(x, bottom);
				final String cell = row.get(i) == null ? "" : row.get(i);
				final String clipped = clip(cell, colWidth - 6F);
				stream.beginText();
				stream.setFont(header ? FONT_HEADING : FONT_TEXT, 10F);
				stream.newLineAtOffset(x + 3F, bottom + 6F);
				stream.showText(clipped);
				stream.endText();
			}
			stream.stroke();
		} catch (IOException e) {
			throw new PdfRuntimeException("表格写入失败: " + e.getMessage(), e);
		}
	}

	/** 按列宽截断单元格文本 */
	private String clip(String text, float maxWidth) {
		final float charW = charWidth(FONT_TEXT, 10F);
		final int maxChars = Math.max(1, (int) (maxWidth / charW));
		return text.length() <= maxChars ? text : text.substring(0, maxChars - 1) + "…";
	}
}
