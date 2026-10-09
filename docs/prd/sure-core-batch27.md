# 批27 PRD：AI 可读 JavaDoc + null-safe（NullUtil 门面 + 文档规范）

- 状态：已批准
- 目标版本：1.12.0（@since 1.12.0）
- 模块：sure-core（com.sure.tool.lang）

## 背景与定位

v3.0 差异化领先第三棒：AI 时代易用性。两个缺口：
① null 处理散落（ArrayUtil.firstNonNull / ObjectUtil.defaultIfNull 等）且缺"安全调用"能力；
② JavaDoc 面向人读，AI 编程助手（Copilot/Cursor）理解 API 依赖示例与 null 语义标注。

## 迭代内容

### 1. NullUtil（新增，统一 null 门面）
- 判断：`isNull` / `isNonNull` / `isAnyNull(Object...)` / `isAllNull(Object...)`
- 取值：`firstNonNull(T...)`（委托 ArrayUtil）/ `lastNonNull(T...)` / `coalesce(T...)` / `defaultIfNull(T,T)`（委托 ObjectUtil）/ `defaultIfNull(T,Supplier)`
- 安全调用（新，与批26 Option 联动）：`applyIfNotNull(T, Function)` → Option<U> / `consumeIfNotNull(T, Consumer)`
- 展示：`nullSafeToString(Object)`（null → ""）

### 2. AI 可读 JavaDoc 规范落地
- 5 个新类（NullUtil/Result/Option/Results/AsyncUtil）类级 JavaDoc 统一补：
  `<pre>{@code ...}</pre>` 使用示例 + 「null 语义」说明段（示范标准）
- CONTRIBUTING.md 新增「AI 可读 JavaDoc 规范」小节：类级示例 + null 语义 + @since 硬性要求

## 验收标准

- NullUtilTest ≥12 例（判断/取值/安全调用/展示全覆盖，含 null 边界）
- 5 个类 JavaDoc 含示例块与 null 语义段（javadoc 构建通过）
- sure-core 门禁全绿（checkstyle + SpotBugs + jacoco ≥0.90 + javadoc）
- 零新增第三方依赖
