# 批 40 · sure-date 增强 + 编解码 Base32/Ascii85（PRD）

> 阶段 A（v1.19 → v2.0）第二批次，对标 Hutool / Apache Commons 高频方法，主攻「易用 + 现代体验」。
> 范围：`sure-core` 模块。原则：每个新方法带中文 JavaDoc + 示例 + 测试覆盖（jacoco 门禁 0.98 不降）。

## 一、背景与目标

- `DateUtil` 现有 87 方法覆盖 Date/Calendar 全场景，但 **java.time 互转与解析缺失**（JDK 25 现代体验核心缺口）、缺 `isSameWeek`、缺 Commons `DateUtils` 的通用截断 API（truncate/round/ceiling）。
- `codec` 包现有 Base64Util/EncodeUtil/HashUtil/HexUtil，**缺 Base32 与 Ascii85**（commons-codec 均有，Hutool 无 Ascii85）。

## 二、新增方法清单

### 2.1 `DateUtil`（+11）

| 方法 | 语义 | 对标 |
|---|---|---|
| `toLocalDate(Date)` | Date → LocalDate | hutool DateUtil.toLocalDateTime 族 |
| `toLocalDateTime(Date)` | Date → LocalDateTime（系统时区） | 同上 |
| `toDate(LocalDate)` | LocalDate → Date（系统时区 00:00） | 同上 |
| `toDate(LocalDateTime)` | LocalDateTime → Date | 同上 |
| `parseLocalDate(String)` | 解析 ISO 日期（yyyy-MM-dd） | hutool DateUtil.parseLocalDate |
| `parseLocalDateTime(String)` | 解析常见日期时间格式（自动探测） | hutool DateUtil.parseLocalDateTime |
| `parseLocalDateTime(String, String)` | 按指定格式解析 | 同上 |
| `isSameWeek(Date, Date)` | 是否同一 ISO 周（周一为一周首日） | commons DateUtils.isSameWeek |
| `truncate(Date, int)` | 截断到指定 Calendar 字段（忽略更小字段） | commons DateUtils.truncate |
| `round(Date, int)` | 四舍五入到指定字段 | commons DateUtils.round |
| `ceiling(Date, int)` | 进位到指定字段（保留字段本身） | commons DateUtils.ceiling |

### 2.2 `Base32Util` 新建（com.sure.tool.codec，+6）

| 方法 | 语义 | 对标 |
|---|---|---|
| `encode(byte[])` | 标准 Base32 编码（A-Z2-7，带 = padding） | commons-codec Base32 |
| `encode(byte[], boolean)` | 可选 padding（false 时去掉尾部 =） | commons-codec |
| `encode(String, Charset)` | 字符串便捷编码 | hutool Base32 |
| `decode(String)` | 解码为 byte[]（容忍缺失 padding） | commons-codec |
| `decodeToString(String, Charset)` | 解码为字符串 | hutool |
| `isBase32(String)` | 格式校验（字母 A-Z2-7 与 = 填充） | commons-codec isInAlphabet |

### 2.3 `Ascii85Util` 新建（com.sure.tool.codec，+5）

| 方法 | 语义 | 对标 |
|---|---|---|
| `encode(byte[])` | Ascii85 编码（4 字节 → 5 字符，零字节压缩） | commons-codec Ascii85 |
| `encode(String, Charset)` | 字符串便捷编码 | 同上 |
| `decode(String)` | 解码（容忍 `<~`/`~>` 包裹，z 解压） | commons-codec Ascii85 |
| `decodeToString(String, Charset)` | 解码为字符串 | 同上 |
| `isAscii85(String)` | 格式校验 | 同上 |

## 三、质量门禁

- 每个方法至少 1 个测试（含 null / 空 / 非法输入 / 边界）。
- `mvn -pl sure-core verify`：jacoco 行覆盖率 ≥0.98、checkstyle / SpotBugs / license 全绿。
- 新测试文件带 Apache-2.0 header、Tab 缩进、中文方法名。

## 四、交付物

1. `DateUtil.java` 增强 + `Base32Util.java` / `Ascii85Util.java` 新建。
2. 新增测试：`DateUtilBatch40Test`、`Base32UtilTest`、`Ascii85UtilTest`。
3. `CHANGELOG.md` 追加 [Unreleased] 条目。
4. 提交并推送 main。
