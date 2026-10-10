# 批35：ListUtil 深度补齐 + MapUtil 增强

- 状态：已批准（2026-10-10）
- 目标版本：1.16.0
- 对标：Google Guava `Lists` / `Maps` + Apache Commons `CollectionUtils` 高频子集
- 原则：中文 JavaDoc + null-safe + 保序 + 不可变/副本默认；只补高频 80%，不追求全量

## 一、背景

批34 已补齐 CollUtil（partition/groupBy2/原地交集差集等，v1.15.0 发布）。但 **ListUtil 仅 8 个方法**（toList/partition/page/sub/reverse/reverseNew/isEmpty/isNotEmpty），是 sure-core 集合族最薄弱一环；MapUtil 46 方法中仍缺 Guava Maps 高频的 toMap（带冲突策略）、difference、mergeAll。

## 二、ListUtil 新增 21 方法

| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `get(List, int index)` | 越界安全取值；负数从尾部定位（-1=末尾），越界返回 null | null 列表 / 正越界 / 负越界 / 正常 |
| `subListSafe(List, int from, int to)` | 越界安全截取（钳制范围，from>to 返回空） | 越界钳制 / 正常 / null |
| `distinct(List)` | 去重保序（LinkedHashSet） | 重复 / null |
| `map(List, Function)` | 映射新列表 | 正常 / null / 映射结果 null 保留 |
| `filter(List, Predicate)` | 过滤新列表 | 全过 / 全滤 / 混合 / null |
| `flatMap(List, Function)` | 扁平映射，内层 null 跳过 | 内层 null / 空内层 / null |
| `zip(List, List)` | 按最短长度配对（List.of 不可变对） | 等长 / 长短不一 / 空 / null |
| `shuffle(List)` | 原地洗牌（Random 可注入） | 1 元素 / 多元素 / null |
| `shuffleCopy(List)` | 洗牌副本（原列表不变） | 正常 / null |
| `sample(List, int count)` | 随机取 count 个不重复；count>=size 返回全量洗牌 | count=0 / count>size / count<size / null |
| `chunk(List, int size)` | 均分块（size<=0 抛 IAE） | size<=0 / 整除 / 余数 / null |
| `union(List, List)` | 并集保序去重 | 有交集 / 无交集 / null |
| `intersection(List, List)` | 交集保序去重 | 有交集 / 无交集 / null |
| `subtract(List, List)` | 差集保序去重（对齐 CollUtil.subtract） | 有差 / 无差 / null |
| `min(List)` / `max(List)` | 自然序极值，空抛 NoSuchElementException | 正常 / 空 |
| `min(List, Comparator)` / `max(List, Comparator)` | 带比较器极值 | 正常 / 空 |
| `sum(List)` / `average(List)` | 数值求和 / 均值（null 元素跳过，空返回 0 / 0.0） | null 元素 / 空 / 正常 |
| `move(List, int from, int to)` | 元素移动到新下标（其余顺移） | 前移 / 后移 / 边界 / 同下标 |

## 三、MapUtil 新增 4 方法

| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `toMap(Iterable, keyMapper, valueMapper)` | 列表转 Map，重复键抛 IllegalArgumentException（Guava uniqueIndex 语义） | 正常 / 重复键 / null |
| `toMap(Iterable, keyMapper, valueMapper, merge)` | 带冲突合并策略 | 冲突合并 / 无冲突 / null |
| `difference(Map, Map)` | 键差：只在左 / 只在右 / 共有但值不同（record Difference） | 各类差 / 全等 / null |
| `mergeAll(Map target, Map source, merge)` | 合并带冲突策略（Map.merge 循环） | 冲突 / 无冲突 / null |

## 四、验收标准

1. 新增实现行 100% 测试覆盖，sure-core 整体保持 ≥ 98%
2. checkstyle 0 违规、SpotBugs 0 bugs、license header 完整
3. 全量 `-am verify` 23/23 模块门禁绿
4. 全部中文 JavaDoc + `@since 1.16.0`
5. 新增测试文件：`P8ListUtilBatch35Test` + `P8MapUtilBatch35Test`

## 五、风险与兼容

- 纯新增 API，无破坏性变更；japicmp 不触发
- `sample`/`shuffle` 使用可注入 `java.util.Random`（JDK25 默认 Random 即可，不强依赖 LXM）
