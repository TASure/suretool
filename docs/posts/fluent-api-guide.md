# suretool Fluent API 实战指南

> 适用版本：v1.19.0+ · 模块：sure-core · 包：`com.sure.tool.fluent`
> 一句话：**链式不断流 + 不可变终端**，与 `XxxUtil` 静态方法双形态并存，零第三方依赖。

## 1. 为什么需要 Fluent API

Hutool / Guava / Apache Commons 的工具类都是「静态方法调用」形态：

```java
// Hutool 风格：每步一个静态调用，中间结果要反复赋值
String s = StrUtil.trim(input);
s = StrUtil.upper(s);
s = StrUtil.replace(s, "x", "y");
```

suretool 的 `Fluent` 管道把「构建 → 变换 → 校验 → 终端」串成一条链，**中间操作不断流**，
每一步都返回新的管道实例，原数据不被修改，终端返回**不可变**结果：

```java
// suretool Fluent 风格：一条链完成，每步可读
String s = Fluent.string(input).trim().upperCase().replace("x", "y").get();
```

两种形态并存：习惯静态方法就用 `StrUtil`，习惯链式就用 `Fluent`，同一能力、同一语义。

## 2. 统一入口 Fluent

| 方法 | 返回 | 说明 |
| --- | --- | --- |
| `Fluent.string(s)` / `Fluent.of(s)` | `FluentString` | 字符串管道（null → NPE） |
| `Fluent.list()` / `Fluent.listOf(e...)` | `FluentList` | 列表管道（元素 null → IAE） |
| `Fluent.of(collection)` | `FluentList` | 从集合创建（防御拷贝） |
| `Fluent.map()` / `Fluent.mapOf(k, v)` | `FluentMap` | Map 管道（键/值 null → IAE） |

> 命名设计：用 `listOf` / `mapOf` 而不是重载 `of(E...)` / `of(K,V)`，
> 规避与 `of(String)` 的泛型重载歧义——调用时类型意图一目了然。

## 3. FluentString：链式文本管道

### 3.1 清洗 + 校验（实战最常用）

```java
import com.sure.tool.fluent.Fluent;

// 输入可能带前后空白、大小写混杂，需要规范化并拒绝空值
String userId = Fluent.string(rawInput)
        .trim()
        .lowerCase()
        .notBlank()            // 链中校验点：空/纯空白抛 IllegalArgumentException
        .get();
```

`notBlank()` 是 suretool 独有的**链中校验**：JDK Stream 无法在管道中途做这类带语义的校验，
必须落到终端再 if 判断。放在链中间，校验失败立即抛错、语义就地可读。

### 3.2 替换与截取

```java
// 字面替换（非正则）：a.b.c → a-b-c
String dashed = Fluent.string("a.b.c").replace(".", "-").get();

// 正则替换：a1b2 → a#b#
String masked = Fluent.string("a1b2").replaceAll("\\d", "#").get();

// 截取：suretool → tool
String name = Fluent.string("suretool").substring(4).get();
```

### 3.3 对齐与补位

```java
// 序号补零：7 → 007
String seq = Fluent.string("7").padLeft(3, '0').get();
// 左对齐补位：report → report----
String aligned = Fluent.string("report").padRight(10, '-').get();
```

### 3.4 前缀后缀剥离

```java
// 去掉 Maven 坐标前缀：io.github.tasure → github.tasure
String shortCoords = Fluent.string("io.github.tasure").stripPrefix("io.").get();
// 去掉文件扩展名：report.md → report
String base = Fluent.string("report.md").stripSuffix(".md").get();
```

### 3.5 终端方法速查

| 终端 | 语义 |
| --- | --- |
| `get()` | 取最终字符串 |
| `length()` / `isEmpty()` / `isBlank()` | 长度 / 空 / 纯空白 |
| `startsWith(p)` / `endsWith(p)` / `contains(s)` | 前缀 / 后缀 / 包含（null → NPE） |
| `equalsIgnoreCase(o)` | 忽略大小写比较（null → false） |

## 4. FluentList：链式集合管道

### 4.1 去重 + 排序 + 截断（排行榜场景）

```java
import com.sure.tool.fluent.Fluent;

// 原始数据可能重复、乱序，取 Top N
List<String> top = Fluent.listOf("c", "a", "b", "a", "d")
        .distinct()
        .sorted()
        .limit(3)
        .toList();          // ["a", "b", "c"]，不可变
```

### 4.2 映射 + 过滤

```java
// 元素类型可变换：字符串 → 长度
List<Integer> lens = Fluent.listOf("sure", "tool")
        .map(String::length)
        .toList();          // [4, 4]

// 过滤后反转
List<String> kept = Fluent.listOf("a", "bb", "ccc")
        .filter(s -> s.length() > 1)
        .reverse()
        .toList();          // ["ccc", "bb"]
```

### 4.3 防御拷贝：原集合不受影响

```java
List<String> source = new ArrayList<>(List.of("x", "y"));
FluentList<String> pipe = Fluent.of(source);
source.add("z");            // 外部修改源集合
pipe.sorted().distinct();   // 中间操作不碰源集合
// pipe 内部仍是 ["x", "y"] 的不可变快照
```

### 4.4 终端方法速查

| 终端 | 语义 |
| --- | --- |
| `toList()` | 不可变 List（修改抛 `UnsupportedOperationException`） |
| `toArray()` | `Object[]` 独立副本（对齐 JDK `Stream.toArray()`；强类型数组用 `toList().toArray(new String[0])`） |
| `count()` / `isEmpty()` / `contains(o)` | 数量 / 空 / 包含（null → false） |
| `get(i)` | 按索引取（越界抛 `IndexOutOfBoundsException`） |
| `join(delim)` | 拼接（空列表返回空串） |

## 5. FluentMap：链式 Map 管道

### 5.1 条件过滤（保插入顺序）

```java
import com.sure.tool.fluent.Fluent;

// 筛选出值大于阈值的条目
Map<String, Integer> hot = Fluent.<String, Integer>map()
        .put("a", 1)
        .put("b", 2)
        .put("c", 3)
        .filterValues(v -> v > 1)
        .toMap();           // {b=2, c=3}，保插入顺序、不可变
```

### 5.2 值映射（键不变）

```java
Map<String, String> labels = Fluent.<String, Integer>map()
        .put("a", 1)
        .mapValues(v -> "v" + v)
        .toMap();           // {a=v1}
```

### 5.3 安全插入与删除

```java
// putIfAbsent：键已存在不覆盖
Map<String, Integer> m = Fluent.mapOf("a", 1)
        .putIfAbsent("a", 99)   // 不生效，a 仍是 1
        .putIfAbsent("b", 99)   // 生效
        .remove("zz")           // 键不存在原样返回
        .toMap();

// 空管道起步，逐步装配
Map<String, Integer> cfg = Fluent.<String, Integer>map()
        .put("timeout", 3000)
        .toMap();
```

### 5.4 终端方法速查

| 终端 | 语义 |
| --- | --- |
| `toMap()` | 不可变 Map（保插入顺序） |
| `get(k)` / `size()` / `isEmpty()` / `containsKey(k)` | 取值（null → null）/ 数量 / 空 / 含键 |
| `keys()` | 不可变键集合（保序） |
| `values()` | 不可变值列表（保序） |

## 6. 语义基线（三条铁律）

1. **不可变默认**：所有终端返回不可变集合（`List.copyOf` / `Map.copyOf` 级），修改抛 `UnsupportedOperationException`，杜绝共享可变状态。
2. **null 防御**：集合元素 / Map 键值 null → `IllegalArgumentException`；非法参数（null 方法参 / 负 limit）→ `NullPointerException` / `IAE`，链中校验点 `notBlank()` 拒绝空与纯空白。
3. **零第三方依赖**：`com.sure.tool.fluent` 仅用 JDK 集合与 `StringJoiner`，不引入 Guava Immutable 或任何外部库。

## 7. 与 XxxUtil 双形态对照

| 场景 | 静态形态（XxxUtil） | 链式形态（Fluent） |
| --- | --- | --- |
| 单步清洗 | `StrUtil.trim(s)` | `Fluent.string(s).trim().get()` |
| 多步管道 | 每步赋值 | 一条链 |
| 中途校验 | 拆开写 if | `notBlank()` 内建 |
| 终端不可变 | 自己 `List.copyOf` | 默认不可变 |

**取舍建议**：一步操作用 `XxxUtil`（少打字）；三步以上管道或需要中途校验/不可变结果时用 `Fluent`（可读性 + 安全默认）。

## 8. 完整示例（可直接运行）

```java
import com.sure.tool.fluent.Fluent;
import com.sure.tool.fluent.FluentList;
import com.sure.tool.fluent.FluentMap;
import java.util.List;
import java.util.Map;

public class FluentDemo {

	public static void main(String[] args) {
		// 1. 用户输入清洗 + 校验
		String name = Fluent.string("  SureTool  ").trim().notBlank().get();

		// 2. 原始日志行解析出 Top 词
		List<String> words = Fluent.listOf("INFO", "ERROR", "INFO", "WARN", "ERROR")
				.map(String::toLowerCase)
				.distinct()
				.sorted()
				.toList();

		// 3. 统计词频并保留高频词
		Map<String, Integer> freq = Fluent.<String, Integer>map()
				.put("info", 2).put("error", 2).put("warn", 1)
				.filterValues(v -> v >= 2)
				.toMap();

		System.out.println(name);   // SureTool
		System.out.println(words);  // [error, info, warn]
		System.out.println(freq);   // {info=2, error=2}
	}
}
```

## 9. 参考

- API 详情：`com.sure.tool.fluent.Fluent` / `FluentString` / `FluentList` / `FluentMap` 中文 JavaDoc（每个方法带示例）
- 规格文档：`docs/prd/sure-fluent-api-batch47.md`
- 快速开始：`docs/posts/quickstart.md`
