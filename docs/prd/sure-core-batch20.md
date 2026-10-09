# PRD：sure-core 批20 · 集合黄金标准（v1.10.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.10.0（sure-core 集合域增量）
- 对标：Guava `Multimap / Multiset / RangeSet` 与 commons-collections4 `MultiValuedMap / Bag`；CollUtil 高频缺口对齐 Guava `Lists/Sets` 与 commons `CollectionUtils`
- 硬约束：仅 JDK25+；核心模块零第三方运行期依赖；中文 Javadoc + `@since 1.10.0`；Tab 缩进；每新增类/方法配套 JUnit 4.13.2 测试；门禁：checkstyle + SpotBugs(effort=Max) + jacoco sure-core line ≥ 0.90

## 1. 背景与目标

v1.9.0 的 collection 包已有 BiMap / BloomFilterUtil / BoundedPriorityQueue / CaseInsensitiveMap / OrderedMap / TreeUtil / CollUtil(70+) / MapUtil(40+)，但**缺少 Guava 三大招牌数据结构**与 commons 对应物：

1. **多值映射**：业务中"一个 key 对应多个值"（标签、分组、一对多关联）高频出现，JDK 无原生结构，Guava `Multimap` / commons `MultiValuedMap` 均为第三方。
2. **计数集合**：统计词频/元素频次需要 `Map<T,Integer>` 手工累加，Guava `Multiset` / commons `Bag` 提供语义化 API。
3. **区间集合**：日期段/数值段合并、判交判含是排期、配额、监控告警的常见需求，Guava `RangeSet` 是唯一成熟实现。
4. **CollUtil 高频缺口**：tree/concurrent 便捷构造、展平、交换、数组化、众数提取、集合版 containsAny。

目标：三大结构 + CollUtil 增量全部落地，集合域追平 Guava/commons 招牌能力，v1.10.0 发布。

## 2. 变更范围

全部在 **sure-core** 内（保持"核心零依赖"卖点）。

| 类 | 新增能力 | 对标 |
|---|---|---|
| `collection.MultiMap<K, V>`（新类） | `put(K, V)`、`putAll(K, Collection<V>)`、`get(K)`、`removeAll(K)`、`remove(K, V)`、`containsKey/containsValue`、`keyCount()`（key 数）、`size()`（值总数）、`values()/keys()/entries()`、`asMap()`、`clear/isEmpty` | Guava `ArrayListMultimap`、commons `MultiValuedMap` |
| `collection.Multiset<T>`（新类） | `add(T)`、`add(T, int)`、`remove(T)`、`remove(T, int)`、`count(T)`、`elementSet()`、`entrySet()`（含 count）、`setCount(T, int)`、`size()`（含重复计数）、`uniqueSize()`、`iterator()`（按计数展开）、`clear` | Guava `Multiset`、commons `Bag` |
| `collection.RangeSet<T extends Comparable<T>>`（新类） | `add(T lower, T upper)`（自动合并重叠/相邻）、`remove(T lower, T upper)`、`contains(T)`、`ranges()`（合并后区间）、`span()`（覆盖全集区间）、`isEmpty/clear` | Guava `RangeSet` |
| `collection.CollUtil`（增强） | `newTreeSet(Comparator, T...)`、`newConcurrentHashSet(T...)`、`flatMap(Collection<Collection<? extends T>>)`、`swap(List, int, int)`、`toArray(Iterable, Class<T>)`、`maxCount(Collection)`（众数）、`containsAny(Collection, Collection)` | Guava `Sets/Lists`、commons `CollectionUtils` |

## 3. API 设计要点

### 3.1 MultiMap<K, V>
- 内部 `LinkedHashMap<K, List<V>>`：**保持插入顺序**，get 缺失 key 返回空不可变列表（不抛 null）。
- `put` 返回 `boolean`（值新增成功）；`remove(K, V)` 返回是否移除；`size()` 为值总数，`keyCount()` 为 key 数。
- **线程不安全**，Javadoc 明示；并发场景用外部同步或后续 sure-concurrent 包装。

### 3.2 Multiset<T>
- 内部 `HashMap<T, Integer>`；`add(null)` 允许（与 Guava 一致用 null 计数）。
- `iterator()` 按 count 展开元素；`entrySet()` 返回 `Set<MultisetEntry<T>>`（`getElement()/getCount()`）。
- `size()` 为含重复总数，`uniqueSize()` 为去重元素数。

### 3.3 RangeSet<T>
- 内部 `TreeMap<T, T>`（lower → upper，闭区间 [lower, upper]）。
- `add` 时自动合并重叠与相邻区间（`lower <= maxUpper+1` 语义由 Comparator 定义，默认自然序）。
- `ranges()` 返回只读 `List<Range<T>>`（新内部类 Range 含 getLower/getUpper/contains）。

### 3.4 CollUtil 增量
- `newTreeSet(Comparator, T...)` → TreeSet；`newConcurrentHashSet(T...)` → ConcurrentHashMap.newKeySet。
- `flatMap` 展平一层；`swap` 交换下标；`toArray(Iterable, Class)` 反射建数组；`maxCount` 返回出现次数最多的元素（并列取先出现）；`containsAny(Collection, Collection)` 集合版。

## 4. 测试与验收

| 验收项 | 标准 |
|---|---|
| 单元测试 | MultiMap 12 例、Multiset 10 例、RangeSet 10 例、CollUtil 增量 8 例（含边界：空/null/重复/合并） |
| 门禁 | `mvn -pl sure-core verify`：checkstyle + SpotBugs + jacoco ≥ 0.90 全绿 |
| 回归 | sure-core 全量既有测试不回退 |
| 兼容 | japicmp 对比 1.9.0 无破坏性变更 |

## 5. 里程碑

1. PRD 评审 → 2. 实现（三大结构 + CollUtil）→ 3. 测试 → 4. 门禁全绿 → 5. commit+push main（v1.10.0 候选）。
