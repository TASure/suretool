# PRD：sure-pdf 模块（批 6 / v1.3.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.3.0（与 sure-aop 同批发布）
- 对标：Hutool Poi PDF 支持面（`cn.hutool.poi`，基于 OpenPDF/PDFBox）
- 硬约束：仅 JDK21+；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG。
- 依赖许可：PDFBox 为 Apache-2.0（与项目一致，区别于 OpenPDF 的 LGPL/MPL）

## 1. 背景与目标

PDF 生成与文本提取是报表、合同、电子凭证等场景的高频刚需。纯 JDK 无法生成 PDF，需引入成熟引擎。

目标：基于 **Apache PDFBox 3.x**（Apache-2.0 许可）提供轻量 PDF 门面：`PdfWriter`（文本/标题/表格/图片/换行排版）+ `PdfUtil`（文本提取、文档校验），API 保持极简，覆盖工具类场景。

## 2. 模块信息

- 模块：`sure-pdf`，坐标 `io.github.tasure:sure-pdf`，包 `com.sure.tool.pdf`
- 运行期依赖：`org.apache.pdfbox:pdfbox:3.0.4`（Apache-2.0）；`sure-core`
- 测试依赖：无额外第三方
- module-info：`module sure.pdf { requires transitive sure.core; requires org.apache.pdfbox; requires org.apache.fontbox; exports com.sure.tool.pdf; }`

## 3. API 设计

### 3.1 PdfWriter（文档写入器，对标 hutool PdfWriter）

- `PdfWriter(OutputStream out)`：构造（校验非空）
- `PdfWriter setTitle(String title)`：文档标题（元数据 + 首行大字号）
- `PdfWriter addText(String text)`：追加文本行（自动换行，默认字号 12）
- `PdfWriter addHeading(String text)`：小标题（加粗/略大字号）
- `PdfWriter addTable(List<List<String>> rows)`：简单表格（首行为表头，边框细线，行内不换行截断）
- `PdfWriter addImage(InputStream in)`：插入图片（等比缩放至页面可用宽度）
- `PdfWriter newPage()`：强制换页
- `void save()`：写入并关闭文档（输出流不关闭）
- 字体：默认 Helvetica（标准 14 字体，英文/数字开箱即用）；`setFont(PDType0Font)` 扩展点支持自定义 TTF（含中文字体）
- 页面尺寸：A4 纵向，页边距 56pt，自动分页（内容超页自动 newPage）

### 3.2 PdfUtil（静态门面）

- `static String extractText(InputStream in)`：提取 PDF 全部文本（PDFBox 3 的 PDFTextStripper）
- `static String extractText(File file)`：文件重载
- `static int pageCount(InputStream in)`：页数
- `static void merge(InputStream first, InputStream second, OutputStream out)`：合并两个 PDF（追加页）
- `static boolean isPdf(InputStream in)`：按 `%PDF-` 魔数校验

### 3.3 线程安全

- PdfWriter 非线程安全（单文档单线程写入）；PdfUtil 无状态，线程安全。

## 4. 验收标准

- A1 生成 PDF：save 后输出流以 `%PDF-` 开头，文件可被 PDFBox 打开
- A2 文本回读：addText/addHeading 写入的中英文（标准字体）文本经 extractText 可提取（英文为准，中文需 setFont TTF）
- A3 自动分页：长文本跨页，pageCount ≥ 2
- A4 表格：addTable 后页数 ≥ 1 且 PDF 有效；空 rows 安全（不写表格）
- A5 图片：addImage 支持 JPEG/PNG，插入后文档有效
- A6 newPage：强制分页生效
- A7 merge：两文档合并后页数 = 两页数之和
- A8 extractText 对非 PDF 输入抛 IllegalArgumentException（含魔数提示）
- A9 全量 verify 门禁：Checkstyle 0、SpotBugs 0、javadoc 0 error、sure-core 覆盖率不降
- A10 模块注册同步：root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG

## 5. 架构评审结论

- **依赖与许可**：PDFBox 3.0.4 为 Apache-2.0，与项目许可一致，规避 OpenPDF（LGPL/MPL）兼容风险——通过。
- **极简面**：Writer 流式 API（链式）覆盖文本/表格/图片；Util 覆盖提取/合并/校验——通过。
- **字体策略**：标准 14 字体零配置起步；中文场景通过 setFont 显式加载 TTF（文档示例用 Noto Sans CJK）——通过。
- **模块化**：module-info 显式 requires PDFBox 自动模块（org.apache.pdfbox / org.apache.fontbox）——通过。
- **命名与包**：`com.sure.tool.pdf`；PdfUtil 私有构造器 + @since 1.3.0。
