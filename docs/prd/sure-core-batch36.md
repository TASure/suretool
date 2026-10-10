# 批36：SetUtil 新建（对标 Guava Sets 高频）

- 状态：已批准（2026-10-10）
- 目标版本：1.17.0
- 对标：Google Guava `Sets` + Apache Commons `SetUtils` 高频子集
- 原则：中文 JavaDoc + null-safe + 不可变/副本默认 + 保序（LinkedHashSet）

## 一、背景

批34/35 已补齐 CollUtil（集合深度）与 ListUtil（21 方法）+ MapUtil（4 方法），集合三件套中 **Set 工具类空白**：CollUtil 仅有构造方法（newHashSet/newLinkedHashSet/newTreeSet/newConcurrentHashSet），缺 Guava `Sets` 高频运算。新建 `com.sure.tool.collection.SetUtil`。

## 二、新增方法（18 个）

| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `newHashSet(T...)` / `newLinkedHashSet(T...)` / `newTreeSet(T...)` | 构造便捷（@SafeVarargs） | 空参 / 含 null |
| `of(T...)` | 不可变 Set（LinkedHashSet 拷贝后 unmodifiable） | 空参 / 重复去重 |
| `union(Set, Set)` | 并集（LinkedHashSet 保序） | 有交集 / null |
| `intersection(Set, Set)` | 交集（保序） | 有交集 / 无交集 / null |
| `subtract(Set, Set)` | 差集（保序，对齐 CollUtil.subtract） | 有差 / null |
| `symmetricDifference(Set, Set)` | 对称差（左右互斥元素，保序） | 互斥 / 全等 / null |
| `filter(Set, Predicate)` | 过滤新 Set（保序） | 全过 / 全滤 / null |
| `map(Set, Function)` | 映射新 Set（保序，结果去重） | 映射冲突合并 / null |
| `cartesianProduct(Set, Set)` | 笛卡尔积（左元素序 × 右元素序，不可变对） | 空任一 → 空 / null |
| `powerSet(Set)` | 幂集（含空集，元素序保持） | 空集 → [空集] / 3 元素 8 子集 / null |
| `isSubset(Set, Set)` | 前者是否后者子集 | 相等 / 真子集 / 非子集 / null |
| `isSuperset(Set, Set)` | 前者是否后者超集 | 相等 / null |
| `disjoint(Set, Set)` | 两集合是否无交集 | 无交集 true / 有交集 false / null |
| `containsAny(Set, Set)` | 是否含任一交集元素 | 有 / 无 / null |
| `join(Set, CharSequence)` | 元素拼接 | 正常 / 空 / null |
| `emptyIfNull(Set)` | null → 空 Set | null / 非 null |
| `reverse(Set)` | 反序新 Set（LinkedHashSet 逆序拷贝） | 正常 / null |
| `size(Set)` | 安全取大小（null → 0） | null / 非 null |

## 三、验收标准

1. 新增实现行 100% 测试覆盖，sure-core 整体保持 ≥ 98%
2. checkstyle 0 违规、SpotBugs 0 bugs、license header 完整
3. 全量 `-am verify` 23/23 模块门禁绿
4. 全部中文 JavaDoc + `@since 1.17.0`
5. 新增测试：`P9SetUtilBatch36Test`

## 四、风险与兼容

- 纯新增类，无破坏性变更；japicmp 不触发
- `of` 返回不可变 Set；`powerSet` 元素数上限由调用方把控（2^n）
