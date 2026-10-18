# 批 44 PRD：StrSubstitutor 模板变量深度（v1.19.0 候选，阶段 A「易用」）

## 背景

路线图（docs/strategy-beyond-ecosystem.html）阶段 A「追平高频 90%」明确点名：
**文本处理（模板变量深度）**。当前现状：

- `StrUtil.format(CharSequence, Object...)`：`{}` 顺序占位，已具备；
- `sure-template` 模块 `SimpleTemplate`：`${key}` 固定定界符，值缺失保留、`\${` 转义；
- **缺口**：Apache Commons Text `StrSubstitutor` 的三项深度能力 sure-core 均无——
  ① 自定义定界符（prefix/suffix 可配）；② 默认值语法 `${key:-default}`；③ 转义符可配（默认 `$`，可禁用）。

## 目标

对标 Commons Text `StrSubstitutor` 高频子集，在 **sure-core**（零第三方依赖）新增：

1. `StrSubstitutor` 类（util 包）：可配置定界符的模板变量替换引擎；
2. `StrUtil` 增便捷静态方法委托（易用优先）。

## API 设计

### `StrSubstitutor`（新类，`com.sure.tool.util`）

| 方法 | 说明 |
|---|---|
| `StrSubstitutor()` | 默认定界符 `${` / `}`、默认转义符 `$` |
| `StrSubstitutor(String prefix, String suffix)` | 自定义定界符；null/空 prefix 抛 IAE |
| `StrSubstitutor(String prefix, String suffix, char escapeChar)` | 自定义定界符与转义符；escapeChar 为 `0` 表示禁用转义 |
| `String replace(String template, Map<String, ?> vars)` | 实例替换；null 模板返回 null |
| `static String replace(String template, Map<String, ?> vars, String prefix, String suffix)` | 一行式便捷（默认转义符 `$`） |

**替换语义**：

- `${key}` → 变量值；值为 null 渲染空串（区分「缺失」与「值为 null」）；
- 缺失变量 → **保留占位符原样**（对齐 Commons 默认与 SimpleTemplate）；
- `${key:-default}` → 变量缺失时渲染 `default`（Commons 兼容默认值语法，`:-` 后可为空）；
- `$${` → 字面 `${`（默认转义符 `$`）；`##{`（escapeChar=#）同理；escapeChar=0 不转义；
- 未闭合占位符（无 `}`）→ 保留原样；
- 空 key `${}` → 渲染为空；
- 嵌套变量**不解析**（对齐 Commons `enableSubstitutionInVariables=false` 默认），`${a${b}}` 视为 key=`a${b` 缺失→保留。

### `StrUtil` 新增（2 个静态方法）

| 方法 | 说明 |
|---|---|
| `substitute(String template, Map<String, ?> vars)` | `${}` 替换（默认转义 `$`，默认值语法） |
| `substitute(String template, Map<String, ?> vars, String prefix, String suffix)` | 自定义定界符替换 |

> `StrUtil.format` 已存在 `{}` 顺序占位，不重复实现。

## 边界与防御

- 实现用 `indexOf(char)` 逐字符扫描 + 字符串搜索，**不使用正则**（定界符可含正则特殊字符，无转义陷阱）；
- 大量拼接用 `StringBuilder`；一次扫描解析，复杂度 O(n·占位符数) 有界；
- 覆盖目标：`StrSubstitutor` 全部分支行覆盖 100%（缺失/空 key/默认值/转义/未闭合/空 map/null 模板/自定义定界符），sure-core 整体 ≥98% 门禁。

## 测试

新类 `StrSubstitutorBatch44Test`，测试方法 ≥20：

- 基本：单变量 / 多变量 / 相邻 / 连续 / 首尾 / 无占位符；
- 默认值：有默认 / 空默认 / `:-` 无默认 / 变量存在时忽略默认 / 值为 null 走默认；
- 转义：`$${` / 转义符可配 / 禁用转义 / 多个转义连续；
- 定界符：自定义 `<% %>` / `[[ ]]` / 含正则特殊字符定界符 / prefix==suffix；
- 边界：未闭合 / 空 key / 空模板 / null 模板 / null map / 空 map / null 值；
- StrUtil 委托两方法各 ≥2 例。

## 验收

1. `mvn -q -pl sure-core -am verify -Dmaven.javadoc.skip=true` BUILD SUCCESS；
2. 清 jacoco.exec 后行覆盖率 ≥98%（sure-core）；
3. 全部方法 JavaDoc 带「示例：」可运行片段；
4. CHANGELOG [Unreleased] 增「批44」条目；PRD 落 docs/prd/sure-str-substitutor-batch44.md。
