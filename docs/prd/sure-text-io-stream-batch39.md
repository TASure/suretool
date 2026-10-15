# 批 39 · sure-text / io / stream 高频补齐（PRD）

> 阶段 A（v1.19 → v2.0）首个批次，对标 Hutool / Apache Commons 高频方法，主攻「易用」。
> 范围：`sure-core` 模块。原则：只补高频 80%，每个新方法带中文 JavaDoc + 一个示例 + 测试覆盖（jacoco 门禁 0.98 不降）。

## 一、背景与目标

sure-core 现有 `StrUtil`（108 方法）、`IoUtil`（20）、`FileUtil`（55）、`text` 包仅 `StrSimilarity`。
与 commons-lang3 `StringUtils` / commons-io `IOUtils`+`FileUtils` / hutool `StrUtil`+`IoUtil`+`FileUtil`+`StreamUtil`
高频子集对照，存在四类缺口：

1. **字符串判定与处理**：字母/数字/空白类判定、任意索引、只替换一次、剥首尾、精确截断、保留空 token 分割、strip 字符集合版。
2. **流工具**：缓冲包装、精确读取/跳过、字符流按行、懒加载行迭代器、跨流复制、Reader 全读。
3. **文件工具**：文件校验和（MD5/SHA-1/CRC32）、符号链接判定、规范化路径、打开流、静默删除。
4. **Stream 工具（新建 `StreamUtil`）**：数组/集合/Optional 转流、多流合并、空安全。

## 二、新增方法清单

### 2.1 `StrUtil`（+15）

| 方法 | 语义 | 对标 |
|---|---|---|
| `isAlpha(CharSequence)` | 全字母 | StringUtils.isAlpha |
| `isAlphanumeric(CharSequence)` | 字母或数字 | StringUtils.isAlphanumeric |
| `isNumericSpace(CharSequence)` | 数字或空格 | StringUtils.isNumericSpace |
| `isAsciiPrintable(CharSequence)` | 全为可打印 ASCII（32–126） | StringUtils.isAsciiPrintable |
| `containsWhitespace(CharSequence)` | 含任意空白字符 | StringUtils.containsWhitespace |
| `removeWhitespace(CharSequence)` | 移除全部空白 | StringUtils.deleteWhitespace |
| `indexOfAny(CharSequence, CharSequence...)` | 任一子串首次出现位置 | StringUtils.indexOfAny |
| `lastIndexOfAny(CharSequence, CharSequence...)` | 任一子串最后出现位置 | StringUtils.lastIndexOfAny |
| `replaceOnce(CharSequence, CharSequence, CharSequence)` | 仅替换首次出现 | StringUtils.replaceOnce |
| `removeSurrounding(CharSequence, CharSequence)` | 同时剥去相同首尾 | hutool StrUtil.removeSurrounding |
| `removeSurroundingIgnoreCase(CharSequence, CharSequence)` | 忽略大小写版 | hutool StrUtil |
| `truncate(CharSequence, int)` | 精确截断（maxWidth-1 结尾省略） | StringUtils.truncate |
| `splitPreserveAllTokens(CharSequence, char)` | 分割保留空 token（含尾空） | StringUtils.splitPreserveAllTokens |
| `stripStart(String, String)` | 剥离开头字符集合（null=空白） | StringUtils.stripStart |
| `stripEnd(String, String)` | 剥离结尾字符集合（null=空白） | StringUtils.stripEnd |

### 2.2 `IoUtil`（+10）

| 方法 | 语义 | 对标 |
|---|---|---|
| `toBufferedInputStream(InputStream)` | 缓冲包装 | IOUtils.toBufferedInputStream |
| `toBufferedReader(Reader)` | BufferedReader 包装 | IOUtils.toBufferedReader |
| `readFully(InputStream, byte[], int, int)` | 精确读满指定长度（不足抛 IOException） | IOUtils.readFully |
| `skipFully(InputStream, long)` | 精确跳过（不足抛 EOF） | IOUtils.skipFully |
| `toString(Reader)` | 字符流全读 | IOUtils.toString(Reader) |
| `lineIterator(Reader)` | 懒加载行迭代器 | IOUtils.lineIterator |
| `lineIterator(InputStream, Charset)` | 懒加载行迭代器（字节流版） | IOUtils.lineIterator |
| `contentEqualsIgnoreEOL(Reader, Reader)` | 忽略行结束符比较 | IOUtils.contentEqualsIgnoreEOL |
| `copy(Reader, OutputStream, Charset)` | 字符→字节复制 | IOUtils.copy |
| `copy(InputStream, Writer, Charset)` | 字节→字符复制 | IOUtils.copy |

### 2.3 `FileUtil`（+8）

| 方法 | 语义 | 对标 |
|---|---|---|
| `checksumMD5(File)` | 文件 MD5（hex 小写） | FileUtils.checksum / hutool |
| `checksumSHA1(File)` | 文件 SHA-1（hex 小写） | 同上 |
| `checksumCRC32(File)` | 文件 CRC32（long） | FileUtils.checksumCRC32 |
| `isSymlink(File)` / `isSymlink(String)` | 符号链接判定 | hutool FileUtil.isSymlink |
| `getCanonicalPath(File)` / `getCanonicalPath(String)` | 规范化绝对路径 | FileUtils.getCanonicalPath |
| `openInputStream(File)` | 打开输入流（IOException 包装） | FileUtils.openInputStream |
| `openOutputStream(File)` | 打开输出流（覆盖写） | FileUtils.openOutputStream |
| `deleteQuietly(File)` | 静默删除（不抛异常） | FileUtils.deleteQuietly |

### 2.4 `StreamUtil` 新建（com.sure.tool.util，+7）

| 方法 | 语义 | 对标 |
|---|---|---|
| `of(T...)` | 数组/变参转流（null 安全） | hutool StreamUtil.of |
| `of(Collection)` | 集合转流（null → 空流） | hutool StreamUtil.of |
| `of(Iterable)` | 迭代器转流 | hutool StreamUtil.of |
| `of(Optional)` | Optional 转流（0/1 元素） | Java 9 Stream.ofNullable 语义 |
| `concat(Stream<T>...)` | 多流合并（null 流跳过） | hutool StreamUtil.concat |
| `nullToEmpty(Stream<T>)` | null 流 → 空流 | hutool StreamUtil.nullToEmpty |
| `empty()` | 空流 | 语义同 Stream.empty |

## 三、质量门禁

- 每个方法至少 1 个测试用例（含边界：null / 空串 / 混合字符 / IO 异常路径）。
- `mvn -pl sure-core verify`：jacoco 行覆盖率 ≥0.98（保持当前 98.15% 不降）、checkstyle / SpotBugs / license 全绿。
- 新测试文件带 Apache-2.0 header、Tab 缩进、中文方法名（沿用现有测试风格）。

## 四、交付物

1. `StrUtil.java` / `IoUtil.java` / `FileUtil.java` 增强 + `StreamUtil.java` 新建。
2. 新增测试：`StrUtilBatch39Test`、`IoUtilBatch39Test`、`FileUtilBatch39Test`、`StreamUtilTest`（或并入现有测试类，按现有约定）。
3. `CHANGELOG.md` 追加 [Unreleased] 条目。
4. 提交并推送 main。
