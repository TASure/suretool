# sure-core 方法-用例映射审计报告（P5 · 二期 T6）

> 审计时间：2026-09-25 · 口径：`sure-core/src/main/java` 全部 **public/protected 方法名**，在 `sure-core/src/test/java` 中出现同名调用（`.` 调用、静态调用或 `new X<...>(` 构造）即视为命中；同名重载任一被调即命中。
> 审计脚本（保留在仓库，可复跑）：`.github/scripts/method_audit.py`

## 1. 结论

| 指标 | 数值 |
| --- | --- |
| public/protected 方法名总数 | **952** |
| 已命中 | **951（99.89%）** |
| 未命中 | 1（豁免，见下） |
| 本次补测 | `P6MethodAuditTest`（16 用例，全绿） |

**验收达成**：除 1 项声明豁免外，sure-core 每个 public/protected 方法至少被 1 个用例命中。

## 2. 补测覆盖（P6MethodAuditTest，16 例）

| 目标方法 | 补测用例 | 验证点 |
| --- | --- | --- |
| `BeanDesc#getBeanClass` | `beanDescGetBeanClass` | 类解析 + props 枚举 |
| `PropDesc` 构造器 | `propDescConstructor` | 反射字段/方法装配、getName/getType |
| `BiMap` 构造器 | `biMapConstructorAndInverse` | 双向映射 getKey |
| `BoundedPriorityQueue` 构造器/`addAllAndReturn` | `boundedPriorityQueueAddAllAndReturn` | 容量截断 + 链式返回 |
| `CaseInsensitiveMap` 构造器 | `caseInsensitiveMapConstructor` | 大小写不敏感键 |
| `MapUtil#builder`/`MapBuilder` | `mapUtilBuilder` | 链式构建 + putAll |
| `OrderedMap` 构造器 | `orderedMapConstructorAndOrder` | 插入序保持 |
| `TreeNode` 构造器/`setId`/`setParentId` | `treeNodeSetters` | 链式 setter 返回自身 |
| `DateUnit#getMillis` | `dateUnitGetMillis` | DAY/SECOND 毫秒换算 |
| `NanoIdUtil#getDefaultAlphabet` | `nanoIdDefaultAlphabet` | 字母表长度 + randomNanoId |
| `StopWatch#getTotalTimeSeconds` | `stopWatchGetTotalTimeSeconds` | start/stop 计时 |
| `WeightRandom` 构造器/`items` | `weightRandomItems` | 权重注册 + 命中抽取 |
| `Props#load` | `propsLoadFromStream` | 流加载 + getStr |
| `RuntimeUtil#exec` | `runtimeUtilExec` | java -version 跨平台进程执行 |
| `TimeInterval` 构造器 | `timeIntervalConstructor` | 计时/重启 |
| `UrlUtil#toUri` | `urlUtilToUri` | URI 解析 host/path |

## 3. 豁免项（1）

| 方法 | 原因 |
| --- | --- |
| `DateUtil.initialValue`（protected） | `ThreadLocal` 内部实现，仅由 `ThreadLocal.get()` 间接触发；其行为（SimpleDateFormat 缓存）已被 DateUtil 全量 format/parse 测试覆盖，直接调用无独立语义。 |

## 4. 复现

```bash
python3 .github/scripts/method_audit.py          # 审计
mvn -pl sure-core test -Dtest=P6MethodAuditTest  # 补测
mvn -pl sure-core verify                         # 全量回归（含覆盖率门禁）
```
