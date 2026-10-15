# 批 41 · 集合深度对标 Guava（PRD）

> 阶段 A（v1.19 → v2.0）第三批次，对标 Guava 高频集合 API（Lists / Maps / Sets / Collections2 / Iterables）。
> 范围：`sure-core`。原则：与现有 API（chunk/split/partitionBy/zip/inverse/powerSet 等）不重复；每方法中文 JavaDoc + 测试；jacoco 0.98 门禁不降。

## 一、背景

- CollUtil 83 方法、ListUtil 29、MapUtil 50、SetUtil 20，已覆盖批 20/34/35/36 的基础集合操作。
- Guava 仍有高频缺口：**排列/组合/笛卡尔积、多键分组、Map 变换、唯一索引、asMap、集合组合**。

## 二、新增方法清单（13 个）

### 2.1 `ListUtil`（+5）

| 方法 | 语义 | 对标 |
|---|---|---|
| `cartesianProduct(List<List<T>>)` | 任意维度笛卡尔积（空输入列表→空结果） | Guava Lists.cartesianProduct |
| `permutations(List<T>)` | 全排列（返回 Set<List<T>>，含自身） | Guava Collections2.permutations |
| `combinations(List<T>, int)` | C(n,k) 组合（0≤k≤n，越界抛 IAE） | Guava Sets.combinations 列表版 |
| `concat(List<T>, List<T>)` | 拼接两个列表（视图不可变） | Guava Lists.concat |
| `concat(List<T>...)` | 拼接多个列表（null 跳过） | Guava Iterables.concat |

### 2.2 `CollUtil`（+3）

| 方法 | 语义 | 对标 |
|---|---|---|
| `groupBy(Collection<T>, Function<T,K1>, Function<T,K2>)` | 多键嵌套分组 Map<K1, Map<K2, List<T>>（保序 LinkedHashMap） | Guava Multimaps.index 多层 |
| `cartesianProduct(Collection<? extends Collection<T>>)` | 任意维度笛卡尔积（Collection 版） | Guava Sets.cartesianProduct |
| `groupBy(Collection<T>, Function<T,K>...)` | 多键链式分组（深 N 层 Map） | Guava Multimaps.index |

### 2.3 `MapUtil`（+4）

| 方法 | 语义 | 对标 |
|---|---|---|
| `transformValues(Map<K,V>, Function<V,R>)` | 值变换（保留键，null-safe） | Guava Maps.transformValues |
| `transformEntries(Map<K,V>, BiFunction<K,V,R>)` | 键值联合变换 | Guava Maps.transformEntries |
| `uniqueIndex(Iterable<V>, Function<V,K>)` | 唯一索引，重复键抛 IAE | Guava Maps.uniqueIndex |
| `asMap(Set<K>, Function<K,V>)` | 集合→映射视图 | Guava Maps.asMap |

### 2.4 `SetUtil`（+1）

| 方法 | 语义 | 对标 |
|---|---|---|
| `combinations(Set<T>, int)` | k 元素组合（返回 Set<Set<T>>，0≤k≤n） | Guava Sets.combinations |

## 三、质量门禁

- 每个方法 ≥1 测试（null/空/边界/异常路径）。
- `mvn -pl sure-core verify`：行覆盖率 ≥0.98、checkstyle / SpotBugs / license 全绿。
- 排列组合结果用数学断言（n! / C(n,k)）验证。

## 四、交付物

1. ListUtil / CollUtil / MapUtil / SetUtil 增强。
2. 新增测试类：`ListUtilBatch41Test` / `CollUtilBatch41Test` / `MapUtilBatch41Test` / `SetUtilBatch41Test`。
3. `CHANGELOG.md` 追加 [Unreleased] 条目。
4. 提交并推送 main。
