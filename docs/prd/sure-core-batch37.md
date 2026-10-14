# 批37：TypeUtil + ClassScanner + CollStreamUtil（反射/类型/集合流式补齐）

- 状态：已批准（2026-10-14）
- 目标版本：1.18.0 候选（v1.17.0 已发布，当前 pom 1.17.1-SNAPSHOT）
- 对标：Hutool `TypeUtil` / `ClassScanner` / `CollStreamUtil` + Guava `TypeToken` 高频子集
- 原则：中文 JavaDoc + null-safe + 保序（LinkedHashMap/List）+ 防御性只读不要求（集合运算默认返回新结构）

## 一、背景

sure-core 已覆盖集合三件套（CollUtil/ListUtil/MapUtil/SetUtil）、反射（ReflectUtil）、转换（ConvertUtil），但缺三类高频能力：
1. **泛型类型解析**（TypeUtil + TypeReference）：框架/ORM/JSON 反序列化场景取 `List<String>` 的实参 `String`、解析任意 `Type` 到 `Class`——Hutool 中高频且 suretool 完全空白；
2. **包扫描**（ClassScanner）：按包扫描 classpath（目录 + jar），支持注解/父类/谓词过滤——Spring 生态组件发现的基础能力；
3. **集合流式**（CollStreamUtil）：对集合做 groupBy / toMap / summaryStatistics / filter / distinct / join / map——Hutool `CollStreamUtil` 对标，避免用户手写重复 Stream 样板。

## 二、新增类型与方法

### 2.1 `com.sure.tool.lang.TypeReference<T>`
| 成员 | 语义 |
| --- | --- |
| `TypeReference()` | 构造器解析泛型实参（`new TypeReference<List<String>>() {}` 模式），取 `getGenericSuperclass().getActualTypeArguments()[0]`，非法（无泛型/多参）抛 IllegalArgumentException |
| `getType()` | 返回解析出的 Type |
| `getClass()` 覆盖 | 由 getType 经 TypeUtil.getClass 推导 Class |

### 2.2 `com.sure.tool.util.TypeUtil`
| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `getClass(Type)` | Type→Class：ParameterizedType 取 raw；GenericArrayType 组件数组；WildcardType 取上界；TypeVariable 取首个边界；未知类型抛 IAE | 六种 Type 全分支 |
| `getTypeArgument(Type, int)` | 参数化类型第 i 个实参的 Class；非参数化或越界 → Object.class | 正常/越界/非参数化 |
| `getTypeArguments(Type)` | 全部实参 Class 列表（不可变） | 参数化/非参数化 |
| `isUnknown(Type)` | TypeVariable 且无边界 → true | true/false |
| `getType(TypeReference)` | 取引用类型（null 抛 IAE） | 正常/null |

### 2.3 `com.sure.tool.util.ClassScanner`
| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `scanPackage(String)` | 扫描包下全部类（classpath 目录 + jar，递归子包，不含内部类/匿名类） | 目录/jar/空包/不存在包→空集 |
| `scanPackage(String, Predicate<Class>)` | 谓词过滤 | 过滤命中/全滤 |
| `scanPackageByAnnotation(String, Class<A>)` | 含指定注解 | 命中/未命中 |
| `scanPackageBySuper(String, Class)` | 是父类/接口的实现（isAssignableFrom 且非同类） | 实现类/接口自身被排除 |

实现：ContextClassLoader#getResources(包路径) → 目录递归 `.class`（读字节码轻量解析类名，不加载）+ jar 内 `JarFile` 遍历；类加载统一 try/catch（LinkageError/ClassNotFound 跳过）。懒加载保证：默认只加载匹配类。

### 2.4 `com.sure.tool.collection.CollStreamUtil`
| 方法 | 语义 | 分支要求 |
| --- | --- | --- |
| `groupBy(Collection, Function)` | 分组 → `LinkedHashMap<K,List<V>>` 保序；data/keyMapper null → 空 Map（keyMapper null 抛 IAE） | 多元素/同键聚合/空/null |
| `groupBy(Collection, Function, Supplier<Map>)` | 自定义结果容器 | 容器生效 |
| `groupByKeyValue(Collection, keyMapper, valueMapper)` | 按键聚合值（Map<K,V>，后者覆盖前者） | 值映射/空 |
| `toMap(Collection, keyMapper, valueMapper)` | 唯一键语义，重复键抛 IllegalArgumentException（Guava uniqueIndex 语义） | 正常/重复键/空 |
| `toMap(Collection, keyMapper, valueMapper, BinaryOperator)` | 冲突合并 | 合并函数生效 |
| `summaryStatistics(Collection, Function<T,Long>)` | LongSummaryStatistics（流式一次汇总 count/sum/min/max/avg） | 多元素/空 |
| `filter(Collection, Predicate)` | 过滤新 List 保序 | 全过/全滤/null |
| `distinct(Collection, Function keyExtractor)` | 按键去重保序（首现保留） | 重复键/全唯一/null |
| `join(Collection, Function, String sep)` | 元素 toString 拼接（null 元素跳过） | 含 null/空/单元素 |
| `map(Collection, Function)` | 映射新 List（null 结果跳过） | null 映射/正常/空 |

## 三、验收标准

1. 四类型全部中文 JavaDoc、Apache-2.0 header、Tab 缩进、`@since 1.18.0`；
2. JUnit4 测试：TypeUtil ≥ 12 例、TypeReference ≥ 4 例、ClassScanner ≥ 6 例、CollStreamUtil ≥ 14 例，覆盖全部异常/边界分支；
3. sure-core 全量 `mvn -B -pl sure-core verify` 通过（jacoco 门禁 0.98 + checkstyle + SpotBugs + license header）；
4. 不新增第三方依赖（ClassScanner 仅用 JDK API）；
5. CHANGELOG [Unreleased] 记录 + PRD 落盘。
