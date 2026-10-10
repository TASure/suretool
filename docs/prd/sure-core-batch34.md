# 批34 PRD：CollUtil 集合深度补齐（v3.0 差异化第一批）

- 状态：已批准
- 目标版本：1.15.0（纯 sure-core 增量，零依赖、零 API 破坏）
- 对标：Eclipse Collections（partitionBy）/ Guava（flatten、多键分组）/ Apache Commons（原地交集差集、去重合并）

## 背景

v2.0「能力追平」收官（批21-33），进入 v3.0「差异化领先」。`docs/comparison-ecosystem.md` 对 Eclipse Collections 的借鉴点明确点名：
**partition、zip、groupBy 多键、交集差集原地版**——CollUtil 现有 filter/map/groupByKey（单键）/intersection/subtract（均返回新列表），
缺「按谓词二分」「多键嵌套分组」「原地修改」「去重合并」四类高频能力。

## 交付内容（7 个新增方法 + 1 个 record）

| 方法 | 签名 | 语义 | 对标 |
|---|---|---|---|
| `partitionBy` | `<T> Partition<T> partitionBy(Collection<T> collection, Predicate<T> predicate)` | 按谓词分成 matched / unmatched 两列表 | Eclipse Collections PartitionIterable |
| `groupBy2` | `<T, K1, K2> Map<K1, Map<K2, List<T>>> groupBy2(Collection<T>, Function<T,K1>, Function<T,K2>)` | 两级嵌套分组（LinkedHashMap 保序） | Guava Multimap 变体 |
| `addAllDistinct` | `<T> boolean addAllDistinct(Collection<T> target, Iterable<T> source)` | 去重合并（原地追加未见元素，返回是否变更） | commons-collections CollectionUtils |
| `removeAll` | `<T> boolean removeAll(Collection<T> target, Iterable<T> source)` | 原地差集（移除命中元素，返回是否变更） | commons-collections |
| `retainAll` | `<T> boolean retainAll(Collection<T> target, Iterable<T> source)` | 原地交集（仅保留命中元素，返回是否变更） | commons-collections |
| `flatten` | `<T> List<T> flatten(Collection<? extends Collection<T>> nested)` | 两级扁平化 | Guava Iterables.concat |
| `pairwise` | `<T> List<List<T>> pairwise(Collection<T> collection)` | 相邻元素两两配对（size-1 对） | 自研差异化 |
| `takeWhile` | `<T> List<T> takeWhile(Collection<T> collection, Predicate<T> predicate)` | 从头取满足谓词前缀，遇不满足即停 | JDK9 Stream.takeWhile 集合版 |

`Partition<T>`：`public record Partition<T>(List<T> matched, List<T> unmatched)`（JDK25 record，源码内嵌于 CollUtil 或独立文件）。

## 设计约束

- **null-safe**：collection/source/predicate 为 null 时按既有 CollUtil 惯例处理（null 集合按空处理；predicate null → NPE 由调用方承担，与 filter 一致）
- **原地语义**：removeAll/retainAll/addAllDistinct 直接修改 target（委托 Collection 原生方法，返回是否发生变更）；新增元素用 `addAllDistinct` 的 HashSet 缓存去重加速（O(n+m)）
- **保序**：groupBy2 用 LinkedHashMap + ArrayList；partitionBy/pairwise/takeWhile 保输入顺序
- **泛型**：`<? extends Collection<T>>` 支持任意集合实现
- **中文 JavaDoc** + `@since 1.15.0` 标注

## 验收标准（门禁）

- sure-core `jacoco.line.min=0.98` 通过（新增实现行全测，missed 增幅为 0）
- checkstyle（Tab/import 顺序/FileLength 2600）/ SpotBugs / license header 全绿
- 全量 `-am verify` BUILD SUCCESS
- 不修改任何既有 API 签名与行为；sure-core 依赖清单不变

## 测试用例（P7CollectionGapTest）

- partitionBy：匹配/不匹配各半、全匹配、全不匹配、null 集合、predicate null（NPE 校验）、空集合
- groupBy2：两级分组正确性、保序、单元素组、空集合、null 输入
- addAllDistinct：全重复（无变更 false）、全新增（变更 true）、混合、null source、重复元素去重
- removeAll/retainAll：命中/未命中/空/全部移除 → 变更布尔值与元素集正确性
- flatten：嵌套两层、空外层、空内层、null 元素
- pairwise：1 个元素（0 对）、2 个、5 个、null 输入
- takeWhile：全满足、首个即不满足（空结果）、中途停止、空集合
