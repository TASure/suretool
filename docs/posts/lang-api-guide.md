# Result / Option / NullUtil 指南：无异常的显式错误门面

> 适用版本：sure-core 1.12.0+（批26/27 新增 `com.sure.tool.lang` 包）
> 示例源码：`sure-examples/src/main/java/com/sure/tool/example/LangDemo.java`

## 为什么需要 Result

- **零异常开销**：错误路径不抛异常（Benchmark 实测 Result 与 Optional 同量级，~0.57 ns/op）
- **显式短路**：失败后 `map`/`onSuccess` 自动跳过，无需 `if` 分支
- **可聚合**：`Results.allOf/anyOf/sequence` 一次执行多个操作并聚合错误

## 30 秒上手

```java
import com.sure.tool.lang.Result;
import com.sure.tool.lang.Results;

// 成功/失败
Result<Integer> ok = Result.ok(42);
Result<Integer> bad = Result.fail("服务不可用");

// 链式短路：失败后不再执行
Result<String> mapped = bad.map(v -> "值=" + v)
        .onSuccess(v -> System.out.println("不会执行"));
// 兜底 + 取值
int v = bad.recover(err -> -1).get();
// 批量执行：任一失败聚合
Result<java.util.List<Integer>> all = Results.allOf(
        Result.ok(1), Result.fail("失败"), Result.ok(3));
String err = all.isFailure() ? all.getErrorMessage() : "";
```

## Option：Optional 超集

```java
import com.sure.tool.lang.Option;
import com.sure.tool.lang.NullUtil; // 注意：NullUtil 在 com.sure.tool.util

Option<String> opt = NullUtil.applyIfNotNull(name, s -> s.trim()); // null 安全
if (opt.isEmpty()) { /* 兜底逻辑 */ }
```

## 与异常流对比

| 维度 | try/catch | Result |
|---|---|---|
| 错误传播 | 隐式（调用栈） | 显式（返回值） |
| 性能 | 异常栈填充昂贵 | 与 Optional 同量级 |
| 组合 | 需手写分支 | map/flatMap/onSuccess 链式短路 |
| 语义 | 异常即控制流 | 错误是数据，可聚合可恢复 |

## 相关

- [快速上手](quickstart.md)
- [集合黄金标准（批20）](../modules.md)
