# 批22 PRD：IO / 流 / 文本补齐（对标 commons-io / commons-text）

- 状态：已批准
- 目标版本：1.11.0（@since 1.11.0）
- 模块：sure-core（com.sure.tool.io / com.sure.tool.util）

## 背景与缺口

sure-core 已有：FileUtil（读写/复制/移动/删除/遍历 30+ 方法）、IoUtil（copy/read/write/closeQuietly/contentEquals）、StrUtil（trim/contains/sub/replace/join/format 50+ 方法）、StrSimilarity（levenshtein/similarity/jaccard/cosine）。

对标 commons-io / commons-text 的剩余高频缺口：
1. **FileUtil**：缺大小格式化（sizeFormat）、路径便捷重载（copy(String,String)/size(String)/readLines(String)/touch(String)）、walkFiles（FileVisitor）、lastModified/isNewer/isOlder
2. **IoUtil**：缺流-文件互通（copy(InputStream,File)/copy(File,OutputStream)）、writeLines、追加式 readLines
3. **StrUtil**：缺 abbreviate / abbreviateMiddle / substringBetween / difference / indexOfIgnoreCase / swapCase / normalizeSpace / isNumeric / getCommonPrefix / rotate（commons-text/lang 高频）

## 迭代内容

### 1. FileUtil +8
- `sizeFormat(long)`：B/KB/MB/GB/TB 二进制单位格式化（保留 1 位小数，<1024 显示 B）
- `size(String path)`、`copy(String src, String dest)`、`readLines(String path)`、`touch(String)`：路径便捷重载
- `walkFiles(Path root)`：FileVisitor 深度遍历返回文件列表（目录过滤、循环安全）
- `lastModified(File)`：最后修改时间（毫秒）
- `isNewer(File, File)` / `isOlder(File, File)`：与参考文件比较

### 2. IoUtil +4
- `copy(InputStream, File)`：流写入文件
- `copy(File, OutputStream)`：文件写入流
- `writeLines(Collection<?>, OutputStream, Charset)`：逐行写出（行分隔符换行）
- `readLines(InputStream, Charset, Collection<String>)`：追加式读取（大文件免 OOM）

### 3. StrUtil +11（对标 commons-text / commons-lang StringUtils）
- `abbreviate(CharSequence, int maxWidth)`：超长尾部省略号（最小 3）
- `abbreviateMiddle(CharSequence, String middle, int length)`：中间省略
- `substringBetween(CharSequence, CharSequence)` / `substringBetween(CharSequence, CharSequence, CharSequence)`：取标签间文本
- `difference(CharSequence, CharSequence)`：返回首个差异起点起的子串
- `indexOfIgnoreCase(CharSequence, CharSequence, int from)`：忽略大小写定位
- `swapCase(CharSequence)`：大小写互换
- `normalizeSpace(CharSequence)`：首尾去空白 + 内部连续空白压缩为单空格
- `isNumeric(CharSequence)`：非空且全为数字
- `getCommonPrefix(CharSequence...)`：多个字符串公共前缀
- `rotate(CharSequence, int)`：循环移位（正右移/负左移，参考 commons-lang）

## 验收标准

- FileUtilBatch22Test ≥8 例：sizeFormat 各档位/路径重载/walkFiles/lastModified/isNewer
- IoUtilBatch22Test ≥4 例：流文件互通/writeLines/追加 readLines
- StrUtilBatch22Test ≥11 例：abbreviate/abbreviateMiddle/substringBetween/difference/indexOfIgnoreCase/swapCase/normalizeSpace/isNumeric/getCommonPrefix/rotate
- sure-core 全量门禁：checkstyle + SpotBugs(effort=Max) + jacoco ≥0.90 + 全测试绿
- 中文 Javadoc + @since 1.11.0、Apache-2.0 header、Tab 缩进、零新增第三方运行期依赖
