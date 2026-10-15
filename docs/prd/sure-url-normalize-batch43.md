# 批 43 · URL 规范化深度 + 示例文档化收口（PRD）

> 阶段 A（v1.19 → v2.0）第五批次，对标 Hutool URLUtil / RFC 3986 路径规范化。
> 范围：`sure-core`。目标：①URL 深度处理能力补齐；②UrlUtil 全部方法 JavaDoc 示例文档化（收口）。

## 一、背景

- UrlUtil 现有 21 方法，`normalize` 仅做"补协议 + 去尾斜杠"，缺路径级规范化（. / .. 解析）、协议剥离、同源判断、查询串/锚点提取。
- 存量 15 个方法 JavaDoc 无「示例：」片段，违背"每方法示例文档化"铁律，需收口。

## 二、新增方法清单（5 个，全部含 JavaDoc 示例）

| 方法 | 语义 | 对标 |
|---|---|---|
| `removeProtocol(String)` | 剥离协议前缀（http:// / https:// 等） | hutool URLUtil.removeProtocol |
| `normalizePath(String)` | 路径规范化：解析 `.` 与 `..`、合并连续斜杠（RFC 3986 §5.2.4） | RFC 3986 / hutool |
| `isSameOrigin(String, String)` | 同源判断：协议 + 主机 + 端口一致 | Web 安全语义 |
| `getQuery(String)` | 提取查询串（不含 `?`，无则空串） | hutool URLUtil |
| `getFragment(String)` | 提取锚点（不含 `#`，无则空串） | hutool URLUtil |

## 三、示例文档化收口

- 为存量 15 个方法（encode×2 / encodePath / decode×2 / toUri / getHost / getPort / getScheme / getPath / isHttp / getParams / getParam / appendParams / normalize）逐个补齐「示例：」JavaDoc 片段，示例与实现行为一致。
- 常量 `DEFAULT_CHARSET` 属字段，不要求示例。

## 四、质量门禁

- 每个新增方法 ≥1 测试（合法/边界/null），路径规范化用 RFC 向量验证（`a/./b` → `a/b`、`a/../b` → `b`、越界 `../a` 保持）。
- `mvn -pl sure-core verify`：行覆盖率 ≥0.98、checkstyle / SpotBugs / license 全绿。

## 五、交付物

1. UrlUtil 增强（5 新方法 + 15 存量方法补示例）。
2. 新增测试：`UrlUtilBatch43Test`。
3. CHANGELOG [Unreleased] 条目。
4. 提交并推送 main。
