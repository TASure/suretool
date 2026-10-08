# PRD：sure-core 批13 · 核心工具类高频方法补齐（v1.8.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.8.0（sure-core 增量发布）
- 对标：Hutool（`cn.hutool.core.util.*` / `cn.hutool.core.collection.*` / `cn.hutool.core.bean.*`）
- 硬约束：仅 JDK25+；核心模块零第三方**运行期**依赖；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；每新增方法配套 JUnit 4.13.2 测试；门禁：checkstyle(UnusedImports) + SpotBugs(effort=Max) + jacoco line ≥ 0.70。

## 1. 背景与目标

v1.7.0 已具备 88 个核心类（Bean/Coll/Date/Id/Random/Reflect/Convert/Str 等），但对照 Hutool 高频 API，以下场景存在明显缺口：

- 反射：无按名取方法、无泛型类型参数解析、无字段表/字段存在性判断、无类名→Class 映射（含基础类型）
- 转换：无"键值数组→Map""Bean 集合→Map"（高频业务用法）
- 随机：无随机字母串、无随机汉字
- 集合：无旋转、无去 null/空元素、无"全部非空/全空"判断
- Map：无按 key/value 排序（业务报表高频）
- Bean：无列表复制 copyToList、toBean/mapToBean 缺 ignoreNullValue/ignoreError 重载、getProperty 不支持多级路径

目标：补齐以上高频方法，使 sure-core 在 P0 主赛道的 API 覆盖与 Hutool 对齐，且保持零运行期依赖与 JDK25 现代写法。

## 2. 变更范围

全部在 **sure-core** 模块内，不新增模块、不改既有方法签名（仅增强重载）。

| 类 | 新增方法 | 说明 |
|---|---|---|
| `ReflectUtil` | `getClassByName(String)`、`getMethodByName(Class,String)`、`getTypeArguments(Class)`、`getFieldMap(Class)`、`hasField(Class,String)` / `hasField(Class,String,boolean ignoreCase)`、`invoke(Object,String,Class[],Object...)`、`setAccessible(Field)` | 泛型/继承感知，对标 hutool ReflectUtil |
| `ConvertUtil` | `toMap(Object... keysAndValues)`、`toMap(Collection<?>, String keyFieldName)` | 键值数组与 Bean 集合转 Map |
| `RandomUtil` | `randomLetter(int)`、`randomChinese(int)`、`randomString(int, char[])` | 指定字符集随机串 |
| `CollUtil` | `rotate(List,int)`、`removeNull(Collection)`、`removeEmpty(Collection)`、`removeAny(Collection,Object...)`、`isAllEmpty(Collection...)`、`isAllNotNull(Collection...)` | 对集合的旋转与空值治理 |
| `MapUtil` | `sortByKey(Map)`、`sortByKey(Map,Comparator)`、`sortByValue(Map)`、`sortByValue(Map,Comparator)` | 返回 LinkedHashMap |
| `BeanUtil` | `copyToList(Collection,Class)`、`copyToList(Collection,Class,boolean)`、`toBean(Object,Class,boolean)`、`mapToBean(Map,Class,boolean)`、`getProperty` 支持多级路径 | 列表复制与容错转换 |

## 3. API 设计要点

- `ReflectUtil.getClassByName`：`int→int.class`、`Integer→Integer.class`、数组类名 `[I` 等按 `Class.forName` 语义映射；找不到抛 `ClassNotFoundException` 包装为 `IllegalArgumentException`。
- `getMethodByName` / `getFieldMap` / `hasField`：沿类继承链向上查找（含接口 default 方法不强制）；`hasField(ignoreCase)` 用于字段名大小写不敏感场景。
- `getTypeArguments(Class)`：解析泛型父类 `TypeVariable`→实际 `Type`，返回 `Map<String, Class<?>>`（对标 hutool `getTypeArguments`）。
- `ConvertUtil.toMap(Object...)`：奇数个元素抛 `IllegalArgumentException`（键值必须成对）；null 键跳过。`toMap(Collection, keyFieldName)`：取 Bean 属性作为 key，null key 抛异常。
- `CollUtil.rotate`：正数为右旋、负数为左旋；`removeEmpty` 移除 null 与空集合/空字符串；`isAllEmpty/isAllNotNull` 支持多参数。
- `MapUtil.sortByKey/sortByValue`：不修改原 Map，返回 `LinkedHashMap`。
- `BeanUtil.getProperty` 多级路径：`"a.b.c"` 逐级 get（集合/数组索引暂不支持，返回 null 或抛异常按 hutool 语义：null 即返回 null）。
- `copyToList`：深拷贝语义与 `copyProperties` 一致（忽略 null、可指定 ignore 属性）。

## 4. 测试要点（JUnit 4.13.2）

- ReflectUtil：继承链方法/字段、泛型解析、基础类型类名映射、显式参数类型 invoke、ignoreCase hasField
- ConvertUtil：键值对 Map、Bean 集合按字段转 Map、非法键值抛异常
- RandomUtil：随机字母长度与字符集、随机汉字为 CJK 范围、指定字符集
- CollUtil：旋转正负方向、removeNull/removeEmpty/removeAny、多参 isAllEmpty/isAllNotNull
- MapUtil：排序稳定性（等值保序）、不修改原 Map
- BeanUtil：copyToList 深度、ignoreNullValue 语义、getProperty 多级路径（含中间 null 安全返回 null）

## 5. 验收标准

1. `mvn -B verify`（sure-core）全绿：测试通过 + checkstyle + SpotBugs(Effort=Max) + jacoco ≥ 0.70
2. 全部新方法中文 Javadoc + `@since 1.8.0`
3. CHANGELOG 增加 Unreleased 条目；commit + push 至 main
