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
/**
 * sure-pdf：PDF 轻量门面。
 *
 * <p>基于 Apache PDFBox 3（Apache-2.0 许可）提供 {@code PdfWriter}（文本/标题/表格/图片/自动分页）
 * 与 {@code PdfUtil}（文本提取/合并/校验）。</p>
 */
module sure.pdf {
	requires transitive sure.core;
	requires org.apache.pdfbox;
	requires org.apache.fontbox;
	requires static com.github.spotbugs.annotations;

	exports com.sure.tool.pdf;
}
