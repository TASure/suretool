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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.sure.tool.lang.Assert;

/**
 * PDF 工具门面（基于 Apache PDFBox 3）。
 *
 * <p>提供文本提取、页数统计、文档合并与魔数校验。无状态，线程安全。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String text = PdfUtil.extractText(new File("report.pdf"));
 * int pages = PdfUtil.pageCount(new FileInputStream("report.pdf"));
 * }</pre>
 *
 * @since 1.3.0
 */
public final class PdfUtil {

	/** PDF 魔数 */
	private static final byte[] PDF_MAGIC = { '%', 'P', 'D', 'F', '-' };

	private PdfUtil() {
	}

	/**
	 * 提取 PDF 全部文本。
	 *
	 * @param in PDF 输入流（调用后关闭）
	 * @return 提取文本
	 * @throws IllegalArgumentException 输入流为 null 或非 PDF 时抛出
	 */
	public static String extractText(InputStream in) {
		Assert.notNull(in, "输入流不能为 null");
		final byte[] bytes = readAll(in);
		checkPdf(new ByteArrayInputStream(bytes));
		try (PDDocument doc = Loader.loadPDF(bytes)) {
			return new PDFTextStripper().getText(doc);
		} catch (IOException e) {
			throw new PdfRuntimeException("PDF 文本提取失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(in);
		}
	}

	/**
	 * 提取 PDF 全部文本（文件重载）。
	 *
	 * @param file PDF 文件
	 * @return 提取文本
	 * @throws IllegalArgumentException 文件为 null 或非 PDF 时抛出
	 */
	public static String extractText(File file) {
		Assert.notNull(file, "文件不能为 null");
		try {
			return extractText(new FileInputStream(file));
		} catch (IOException e) {
			throw new PdfRuntimeException("文件打开失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 页数统计。
	 *
	 * @param in PDF 输入流（调用后关闭）
	 * @return 页数
	 * @throws IllegalArgumentException 输入流为 null 或非 PDF 时抛出
	 */
	public static int pageCount(InputStream in) {
		Assert.notNull(in, "输入流不能为 null");
		final byte[] bytes = readAll(in);
		checkPdf(new ByteArrayInputStream(bytes));
		try (PDDocument doc = Loader.loadPDF(bytes)) {
			return doc.getNumberOfPages();
		} catch (IOException e) {
			throw new PdfRuntimeException("PDF 页数统计失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(in);
		}
	}

	/**
	 * 合并两个 PDF（按序追加页）。
	 *
	 * @param first  第一个 PDF 输入流（调用后关闭）
	 * @param second 第二个 PDF 输入流（调用后关闭）
	 * @param out    合并结果输出流（不关闭）
	 * @throws IllegalArgumentException 任一参数为 null 时抛出
	 */
	public static void merge(InputStream first, InputStream second, OutputStream out) {
		Assert.notNull(first, "第一个输入流不能为 null");
		Assert.notNull(second, "第二个输入流不能为 null");
		Assert.notNull(out, "输出流不能为 null");
		File tmp1 = null;
		File tmp2 = null;
		try {
			tmp1 = toTempFile(first, "sure-merge-1");
			tmp2 = toTempFile(second, "sure-merge-2");
			final PDFMergerUtility merger = new PDFMergerUtility();
			merger.addSource(tmp1);
			merger.addSource(tmp2);
			merger.setDestinationStream(out);
			merger.mergeDocuments(null);
		} catch (IOException e) {
			throw new PdfRuntimeException("PDF 合并失败: " + e.getMessage(), e);
		} finally {
			closeQuietly(first);
			closeQuietly(second);
			deleteQuietly(tmp1);
			deleteQuietly(tmp2);
		}
	}

	/**
	 * 校验输入是否为 PDF（按 {@code %PDF-} 魔数）。
	 *
	 * @param in 输入流（不关闭，读取后复位）
	 * @return true 表示是 PDF
	 * @throws IllegalArgumentException 输入流为 null 时抛出
	 */
	public static boolean isPdf(InputStream in) {
		Assert.notNull(in, "输入流不能为 null");
		if (!in.markSupported()) {
			in = new ByteArrayInputStream(readAll(in));
		}
		in.mark(8);
		final byte[] head = new byte[5];
		try {
			final int read = in.read(head);
			in.reset();
			if (read < 5) {
				return false;
			}
			for (int i = 0; i < 5; i++) {
				if (head[i] != PDF_MAGIC[i]) {
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			throw new PdfRuntimeException("PDF 校验失败: " + e.getMessage(), e);
		}
	}

	private static void checkPdf(InputStream in) {
		if (!isPdf(in)) {
			throw new IllegalArgumentException("输入流不是 PDF（缺少 %PDF- 魔数）");
		}
	}

	/** 输入流转临时文件（PDFBox3 合并需 File/RandomAccessRead 源） */
	private static File toTempFile(InputStream in, String prefix) throws IOException {
		final File tmp = File.createTempFile(prefix, ".pdf");
		try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp)) {
			in.transferTo(fos);
		}
		return tmp;
	}

	private static void deleteQuietly(File file) {
		if (file != null && !file.delete()) {
			file.deleteOnExit();
		}
	}

	private static byte[] readAll(InputStream in) {
		try {
			return in.readAllBytes();
		} catch (IOException e) {
			throw new PdfRuntimeException("输入流读取失败: " + e.getMessage(), e);
		}
	}

	private static void closeQuietly(InputStream in) {
		try {
			in.close();
		} catch (IOException ignored) {
			// 关闭失败忽略
		}
	}
}
