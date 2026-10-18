# 批47：Fluent API 全面化（阶段 B「好用」第三主轴，v1.19.0 候选）

## 一、背景与定位

超越路线图阶段 B「好用」= 差异化领先。批45 落地独占能力 Actor，批46 落地默认安全基线；本批转向战略点名项 **「Fluent API 全面化」**：链式 Builder 覆盖核心域，与 `XxxUtil` 双形态并存（原路线图批45 条目）。

对标盘点（链式 / Fluent 体验）：

| 能力 | Hutool | Guava | Apache Commons | suretool（现状） |
|---|---|---|---|---|
| 链式字符串管道 | 部分（`StrUtil` 非链式） | ❌（无 String Fluent） | ❌ | ❌ **缺口** |
| 链式集合构建+变换 | 部分（`CollUtil` 非链式） | ✅ `ImmutableList.Builder` | ❌ | ❌（仅 JDK Stream）**缺口** |
| 链式 Map 构建 | ❌ | ✅ `ImmutableMap.Builder` | ❌ | ❌ **缺口** |
| 统一 Fluent 工厂入口 | ❌ | 分散 Builder | ❌ | ❌ **缺口** |
| 终端返回不可变集合 | ❌（可变） | ✅（Immutable 系列） | ❌ | 可做到 ✅ |
| 零第三方依赖 | ❌ | 依赖 Guava | 依赖 Commons | 可做到 ✅ |

**差异化结论**：不做 Guava 的 `Immutable*` 复制，而是以「链式中间操作不断流 + 终端返回 JDK 不可变集合（`List.copyOf`/`Map.copyOf`）+ null 防御 + 中文 JavaDoc 每方法示例 + `Fluent` 统一入口」建立更易用的体验。JDK Stream 是函数式管道、终端一次性，无法中途校验与逐步构建；`XxxUtil` 是静态方法调用，无法链式组合——Fluent 补上第三形态，三者并存。

## 二、API 设计（新建 `com.sure.tool.fluent` 包，sure-core 模块）

### 2.1 统一入口 `Fluent`（静态工厂门面）

| 方法 | 说明 | 边界 |
|---|---|---|
| `static FluentString string(String)` / `of(String)` | 创建字符串管道 | null 抛 NPE |
| `static FluentList<E> list()` / `listOf(E... items)` | 创建空/带初始元素列表管道（`listOf` 命名规避与 `of(String)` / `of(Object,Object)` 的泛型重载歧义） | 数组含 null 抛 IAE |
| `static <E> FluentList<E> of(Collection<E> c)` | 从集合创建（防御拷贝） | c null 抛 NPE |
| `static <K,V> FluentMap<K,V> map()` / `mapOf(K k, V v)` | 创建空/单键值 Map 管道（`mapOf` 命名同理规避歧义） | 键或值 null 抛 IAE |

### 2.2 `FluentString`（链式文本管道，约 15 个方法）

中间操作（全部返回 `FluentString`，不断流）：
- `trim()` / `upperCase()` / `lowerCase()` / `reverse()`
- `replace(String oldChar, String newChar)`（字面替换，null→NPE）
- `replaceAll(String regex, String replacement)`（正则替换，null→NPE）
- `substring(int begin)` / `substring(int begin, int end)`（越界抛 StringIndexOutOfBounds）
- `padLeft(int length, char padChar)` / `padRight(int length, char padChar)`（length 小于原长返回原串）
- `repeat(int count)`（count<0 抛 IAE，count=0 返回空）
- `stripPrefix(String prefix)` / `stripSuffix(String suffix)`（不匹配返回原串；null→NPE）
- `notBlank()`（当前值为空/空白抛 IAE，作为链中校验点）

终端操作：
- `get()`（返回当前 String，别名 `toString()` 等价）
- `length()` / `isEmpty()` / `isBlank()`
- `startsWith(String)` / `endsWith(String)` / `contains(String)`（null→NPE）
- `equalsIgnoreCase(String)`（null→false）

### 2.3 `FluentList<E>`（链式集合构建与变换，约 13 个方法）

中间操作（返回新 `FluentList`，原集合不变——防御拷贝）：
- `add(E item)` / `addAll(Collection<? extends E> c)`（null 元素抛 IAE）
- `map(Function<? super E, ? extends E2> mapper)` → `FluentList<E2>`（结果含 null 抛 IAE）
- `filter(Predicate<? super E> p)` / `distinct()` / `reverse()`
- `sorted()` / `sorted(Comparator<? super E> c)`（null 比较器抛 NPE）
- `limit(int maxSize)`（maxSize<0 抛 IAE；超过原长返回全量）
- `skip(int n)`（n<0 抛 IAE；超过原长返回空）

终端操作：
- `toList()`（**不可变**：`List.copyOf`，副本不受后续外部修改影响）
- `toArray()`（返回 `Object[]` 独立副本，对齐 JDK `Stream.toArray()`；强类型数组用 `toList().toArray(new E[0])`）
- `count()` / `isEmpty()` / `contains(Object o)`
- `get(int index)`（越界抛 IndexOutOfBounds）
- `join(String delimiter)`（null 分隔符→NPE；空列表返回空串）

### 2.4 `FluentMap<K,V>`（链式 Map 构建与变换，约 12 个方法）

中间操作（返回新 `FluentMap`，原 Map 不变）：
- `put(K key, V value)`（键或值 null 抛 IAE——与 `Map.copyOf` 的 NPE 语义对齐为显式 IAE）
- `putIfAbsent(K key, V value)` / `remove(K key)`
- `mapValues(Function<? super V, ? extends V2> mapper)` → `FluentMap<K,V2>`（结果含 null 抛 IAE）
- `filterKeys(Predicate<? super K> p)` / `filterValues(Predicate<? super V> p)`

终端操作：
- `toMap()`（**不可变**：`Map.copyOf`）
- `get(K key)`（返回 V 或 null）/ `size()` / `isEmpty()` / `containsKey(K)`
- `keys()`（不可变 Set）/ `values()`（不可变 List）

## 三、核心语义（可靠底线）

1. **不可变默认**：`toList()/toArray()/toMap()/keys()/values()` 全部返回不可变视图或副本，杜绝外部修改（对标 Guava Immutable 理念，零依赖实现）。
2. **原集合不可变**：中间操作基于内部防御拷贝，绝不修改传入的集合/Map 参数。
3. **null 防御**：输入 null → NPE（创建/参数）；集合元素 null → IAE；`contains(null)` 按 JDK 语义返回 false。
4. **校验内建**：`notBlank()` 作为链中校验点，让「校验+变换+终端」一条链完成（易用卖点）。
5. **双形态并存**：不替代 `StrUtil/CollUtil/MapUtil`；Fluent 只做链式形态，终端结果可直接喂给既有 Util。
6. **零第三方依赖**：仅 JDK 标准库（`java.util`、`java.util.function`）。
7. **中文 JavaDoc + 每方法可运行示例**（沿用全库规范，Tab 缩进、Apache-2.0 头）。

## 四、测试清单（`FluentTest`，sure-core 测试）

| 组 | 用例 |
|---|---|
| FluentString 基本 | trim/大小写/reverse 链式组合；字面与正则替换；substring 边界与越界；pad 补足/超长/相等；repeat 0/1/n 与负数拒绝 |
| FluentString 校验 | notBlank 通过/拒绝；null 创建 NPE；replaceAll 非法正则；stripPrefix 命中/未命中 |
| FluentString 终端 | length/isEmpty/isBlank/startsWith/endsWith/contains/equalsIgnoreCase |
| FluentList 构建 | of 空/单/多；from 防御拷贝（改原集合不影响管道）；null 元素拒绝 |
| FluentList 变换 | map 类型变换与 null 结果拒绝；filter/distinct/sorted/limit/skip/reverse 组合 |
| FluentList 终端 | toList 不可变（add 抛 UnsupportedOperationException）；toArray 独立副本；join 空/分隔符；get 越界 |
| FluentMap 构建 | of 单键值；from 防御拷贝；put null 拒绝 |
| FluentMap 变换 | putIfAbsent/remove/mapValues/filterKeys/filterValues |
| FluentMap 终端 | toMap 不可变；get/size/containsKey/keys/values 不可变 |
| 统一入口 | Fluent.of 各重载分发正确；类型推断（`Fluent.<String>of()` 泛型用法） |

预估 45–55 个断言方法，目标 sure-core 全模块行覆盖 ≥98%（既有铁律）。

## 五、验收标准

1. `sure-core` 测试全绿（含既有 1328+ 例），新增 `FluentTest` 全覆盖 2.2–2.4 全部公开方法；
2. 门禁：jacoco 行覆盖 ≥98%、checkstyle、SpotBugs 全绿；
3. `CHANGELOG.md` [Unreleased] 顶部追加批47 条目（API 清单、不可变语义、门禁数据）；
4. 推送 main 后回读验证（git diff origin/main 空差异）。
