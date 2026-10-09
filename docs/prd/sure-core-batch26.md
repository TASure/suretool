# 批26 PRD：Fluent API + Result/Option（显式错误与可选值门面）

- 状态：已批准
- 目标版本：1.12.0（@since 1.12.0）
- 模块：sure-core（新包 com.sure.tool.lang）

## 背景与定位

v3.0 差异化领先第二棒：易用性。现有错误传播靠异常流（try/catch 心智负担），可选值仅 JDK Optional
（缺 getOrElse/onEmpty 等高频便捷）。对标 Vavr（Result/Option/组合子）× Guava Optional × JDK Optional，
提供零第三方依赖的显式结果门面 + 链式 Fluent 组合。

## 迭代内容

### 1. Result<T>：显式错误传播（两态：成功值 / 失败信息）
- 构造：`ok(T)` / `fail(String)` / `fail(Throwable)` / `fail(String, Throwable)`
- 查询：`isSuccess()` / `isFailure()` / `get()` / `getOrNull()` / `getOrElse(T)` / `getOrElseGet(Supplier)`
- 链式：`map(Function)` / `flatMap(Function)` / `onSuccess(Consumer)` / `onFailure(Consumer)` / `recover(Function)`
- 终止：`throwIfFailed()` / `toOptional()` / `getErrorMessage()`

### 2. Option<T>：Optional 超集门面
- 构造：`of(T)` / `ofNullable(T)` / `empty()`
- 查询：`isPresent()` / `isEmpty()` / `get()` / `getOrElse(T)` / `getOrElseGet(Supplier)` / `orElse(Option)`
- 链式：`map(Function)` / `flatMap(Function)` / `filter(Predicate)` / `peek(Consumer)` / `onEmpty(Runnable)`
- 终止：`toOptional()` / `ifPresent(Consumer)`

### 3. Results：Fluent 组合子门面
- `allOf(Result<T>...)` → Result<List<T>>：全部成功聚合，任一失败携带首个错误
- `anyOf(Result<T>...)` → Result<T>：首个成功
- `sequence(List<Result<T>>)` → Result<List<T>>：列表聚合（allOf 别名）

## 验收标准

- ResultTest ≥12 例（ok/fail 两态、getOrElse 家族、map/flatMap 短路、onSuccess/onFailure 回调、recover、throwIfFailed、组合）
- OptionTest ≥10 例（ofNullable/empty、getOrElse 家族、map/flatMap、filter、onEmpty/peek、toOptional）
- ResultsTest ≥5 例（allOf 全成/含败、anyOf 首成/全败、sequence）
- sure-core 门禁全绿（checkstyle + SpotBugs + jacoco ≥0.90 + javadoc）
- 中文 Javadoc + @since 1.12.0、Apache-2.0 header、Tab 缩进、零新增第三方依赖
