# PRD：sure-http multipart 多文件上传增强

> 批次：批 12　|　负责人：软件研发小组　|　状态：已评审，进入实现
> 版本目标：v1.7.0

## 1. 目标与范围

在 `sure-http` 补齐 **multipart/form-data 多文件上传**能力，对标 Hutool
`HttpRequest.form(String, File...)` / `HttpUtil.upload(...)` 多文件形态：

- `HttpRequest.form(String, File...)`：同一字段上传多个文件；
- `HttpRequest.formFiles(Map<String, File>)`：多字段文件批量添加；
- `HttpUtil.upload(url, form, fileField, File[])` 与 `HttpUtil.upload(url, form, Map<String, File>)`：
  多文件重载（含超时版本），内部统一走私有 `uploadMulti(url, form, Map<String, File[]>, timeout)`。

**范围内**：上述 API、`fileParams` 多文件模型改造、`buildMultipart` 双层写出、测试与文档。
**范围外（不做）**：

- 不做流式/分块上传（`setFixedLengthStreamingMode` 已覆盖常规场景）；
- 不引入 Apache HttpComponents / OkHttp（核心零依赖原则）；
- 不改动 `HttpUtil.upload` 单文件既有签名与行为。

## 2. 用户场景

1. **多图/多附件提交**：一次请求上传多个文件到同一字段（如多图投稿、批量附件）；
2. **多字段文件**：一张表单同时携带多个不同字段的文件（如头像 + 身份证件）；
3. **既有单文件兼容**：`upload(url, form, field, File)` 行为不变，回归由 P5/P6 测试守护。

## 3. 架构评审结论（第 2 步）

| 项 | 结论 |
| --- | --- |
| 数据模型 | `fileParams` 由 `Map<String, File>` 升级为 `Map<String, List<File>>`，文本字段 `formParams` 不变；`isEmpty()` 判断分支（multipart/表单二选一）无需改动 |
| 引擎 | 双引擎共用同一 `buildMultipart`/`buildForm`；HttpURLConnection 与 JDK HttpClient 均自动获得多文件能力 |
| 重载决议坑 | `form("x", null)` 字面量 null 会被 Java 选中 `form(String, File...)`（更具体）；null 分支按既有契约写入空值文本字段（输出 `name=`），由 `HttpChainTest.testPostFormChain` 回归守护 |
| 测试策略 | `com.sun.net.httpserver.HttpServer` 本地端点解析 multipart，断言文件数 / 字段名 / 文件名摘要；复用 P5 模式 |

## 4. 实现

1. `HttpRequest.fileParams` → `Map<String, List<File>>`；`form(String, Object)` File 分支按字段追加；
2. 新增 `form(String, File...)`（null → 空值文本字段）、`formFiles(Map<String, File>)`；
3. `buildMultipart()` 双层循环逐文件写出 part（name 重复、filename 各自）；
4. `HttpUtil` 新增 4 个多文件重载 + 私有 `uploadMulti` 统一实现；
5. 新增 `P6HttpMultipartTest` 5 用例。

## 5. 验收（第 4 步）

- [x] sure-http 64 测试全绿（含新增 P6 5 用例、既有 HttpChainTest 回归）
- [x] 全量 `mvn verify` BUILD SUCCESS（checkstyle / license / SpotBugs effort=Max / jacoco ≥ 0.70）
- [x] CHANGELOG / README 登记

## 6. 已知边界

- `form(String, File...)` 与 `form(String, Object)` 的同名重载存在 Java null 决议歧义（与 Hutool 一致）；
  空文件数组（`new File[0]`）为合法 no-op；字面量 `null` 语义为「空值文本字段」。
