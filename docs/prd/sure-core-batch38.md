# 批38 PRD：Bean 拷贝 + 文件监听 + 查找算法（v3.0 差异化领先）

- 状态：已实现并全量验收（2026-10-14，4 个新类 + 102 例测试）
- 目标版本：1.18.0（`@since 1.18.0`）
- 模块：sure-core
- 原则：零第三方依赖、静态工具类 + 私有构造器、null-safe、中文 JavaDoc、Tab 缩进

## 1. 背景与对标缺口（Guava / Hutool / Apache Commons）

| 方向 | Guava | Hutool | Apache Commons | sure 差异化 |
|---|---|---|---|---|
| Bean 属性拷贝 | 无 | `BeanUtil.copyProperties`：无类型转换、无深浅拷贝选项、无链式 | `BeanUtils.copyProperties`：反射逐字段慢、无转换、无深拷贝 | 字段描述缓存（复用 BeanDesc/PropDesc）+ 类型转换 + 深浅拷贝 + 忽略/包含 + 链式 + null 策略 + Bean↔Map |
| 文件尾部跟踪（tail -f） | 无 | 无 | `Tailer`：轮询线程阻塞回调、无初始行、无编码探测 | 虚拟线程 + 回调 + 支持从头读 N 行 + UTF-8/GBK 自动探测 + 轮询间隔可配 + 日志 rotate 自动重开 |
| 目录变更监听 | 内部 watcher 未公开 | 无 | `FileAlterationObserver`：轮询慢 | JDK WatchService 封装 + 递归 + CREATE/MODIFY/DELETE 回调 + 后缀/前缀/正则过滤 + 虚拟线程 |
| 有序查找算法 | 无（`binarySearch` 仅 Collections 原生） | 无 | `CollectionUtils.binarySearch` 弱（无上下界） | `lowerBound` / `upperBound` / 范围查询 / 插值查找 / 指数查找，泛型 + Comparator + null-safe |

差异化叙事：主流框架要么缺失、要么实现粗糙（无类型转换、无上下界、轮询阻塞）。sure 用**零依赖 + 虚拟线程 + 缓存 + 类型转换**做出明显更好用的版本。

## 2. 用户场景

- **BeanCopier**：DTO/VO 转换（String id → long id）、表单参数 → 实体（类型转换）、深拷贝独立对象、Bean ↔ Map 互转
- **FileTailer**：日志 tail -f 监控、追加文件实时消费、日志文件轮转后自动重开
- **WatchDir**：热部署监听、目录同步、配置目录热更新、上传目录新增文件处理
- **SearchUtil**：有序列表范围查询（事件时间线定位）、分页边界计算、成绩/评分区间定位

## 3. API 设计（架构师评审稿）

### 3.1 `com.sure.tool.bean.BeanCopier`（新类）

```java
public final class BeanCopier {
    private BeanCopier() { }

    // 基础拷贝：新建目标（自动类型转换）
    public static <T> T copy(Object source, Class<T> targetClass)
    public static <T> T copy(Object source, Class<T> targetClass, CopyOption... options)
    // 拷贝到已有目标（自动类型转换，跳过不可写属性）
    public static void copy(Object source, Object target)
    public static void copy(Object source, Object target, CopyOption... options)
    // 深拷贝：集合/数组/嵌套 Bean 递归拷贝
    public static <T> T deepCopy(Object source, Class<T> targetClass)
    // Bean ↔ Map
    public static <T> T toBean(Map<String, ?> map, Class<T> beanClass)
    public static Map<String, Object> toMap(Object bean)
}
```

`CopyOption`（枚举，按位/可变参数组合）：
- `IGNORE_NULL`：源值为 null 时跳过目标属性
- `SKIP_MISSING`：源 Bean 缺失的属性跳过（默认缺失赋 null/默认值）
- `IGNORE_READONLY`：跳过只读目标属性
- `INCLUDE("fieldA","fieldB")` / `EXCLUDE("fieldA")`：字段白名单/黑名单（不可同时传，冲突抛 IAE）

类型转换规则（复用 `ConvertUtil`）：String↔Number↔Boolean↔char、枚举↔String、`Collection`↔数组、同类型直拷；转换失败抛 `ConvertException`（带源/目标属性名）。

### 3.2 `com.sure.tool.io.FileTailer`（新类）

```java
public final class FileTailer {
    // 从文件尾部开始跟踪（等同行 tail -f），回调在虚拟线程执行
    public static FileTailer start(Path file, Consumer<String> lineListener)
    public static FileTailer start(Path file, Consumer<String> lineListener, TailerOptions options)

    public void stop()                 // 幂等
    public boolean isRunning()
}
```

`TailerOptions`（builder）：`pollInterval(Duration)`（默认 1s）、`charset(Charset)`（默认自动探测 UTF-8/GBK）、`readFromStart(int lines)`（从文件头读最近 N 行后再跟踪，N=0 从尾部）、`stopOnDelete(boolean)`（文件被删/轮转后停止或自动重开，默认自动重开）、`onRotate(Runnable)`（轮转回调）。

实现：`FileChannel` 按字节偏移增量读 + 行缓冲按换行切分；文件被替换（`Files.isSameFile` 失效）→ 重开新通道；虚拟线程循环，`stop()` 中断唤醒。

### 3.3 `com.sure.tool.io.WatchDir`（新类）

```java
public final class WatchDir {
    public static WatchDir watch(Path dir, DirListener listener)
    public static WatchDir watch(Path dir, DirListener listener, WatchOptions... options)

    public void stop()
    public boolean isRunning()
}

@FunctionalInterface
public interface DirListener {
    void onEvent(EventType type, Path relativePath);   // CREATE / MODIFY / DELETE / OVERFLOW
}
```

`WatchOptions`（builder）：`recursive(boolean)`（默认 true）、`filter(PathFilter)`（后缀/前缀/正则，默认不过滤）、`eventTypes(EventType...)`（默认 CREATE+MODIFY+DELETE）。

实现：JDK `WatchService` 注册 `StandardWatchEventKinds`；递归目录注册 + 新目录动态注册；虚拟线程轮询事件；OVERFLOW 单独上报。

### 3.4 `com.sure.tool.util.SearchUtil`（新类）

```java
public final class SearchUtil {
    private SearchUtil() { }

    // 上下界（Guava/Hutool 均缺；Commons 弱实现）：返回插入点下标
    public static <T extends Comparable<? super T>> int lowerBound(List<? extends T> list, T key)
    public static <T extends Comparable<? super T>> int upperBound(List<? extends T> list, T key)
    public static <T> int lowerBound(List<? extends T> list, T key, Comparator<? super T> cmp)
    public static <T> int upperBound(List<? extends T> list, T key, Comparator<? super T> cmp)
    // 范围查询：返回 [lower, upper) 下标视图（不可变子列表）
    public static <T extends Comparable<? super T>> List<T> range(List<? extends T> list, T from, T to)
    // 任意命中：与 Collections.binarySearch 语义一致，但 null-safe 且支持逆序比较器
    public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> cmp)
    // 插值 / 指数查找（原始 long[] 有序数组，均含越界/空数组安全）
    public static int interpolationSearch(long[] sorted, long key)
    public static int exponentialSearch(long[] sorted, long key)
}
```

## 4. 验收标准

1. 4 个新类全部实现，JUnit 4 测试覆盖正常/边界/异常路径（每类 ≥ 20 例）；
2. 新增类行覆盖率 ≥ 98%；sure-core 实测覆盖率不降（当前 98.725%）；
3. `mvn -B verify` 全绿（checkstyle 0 违规 / SpotBugs 0 bug / license header 全齐）；
4. 无第三方运行期依赖新增；API 全部 null-safe + 中文 JavaDoc + `@since 1.18.0`；
5. 与既有类不重名冲突（BeanUtil/FileUtil/IoUtil 已有方法不破坏）。

## 5. 不做的边界

- 不做 Bean 深拷贝的循环引用保护（有环时报 `StackOverflowError` 提示改用浅拷贝；本批不引入引用追踪）
- 不做 NIO.2 异步 IO（虚拟线程同步封装已足够，`AsynchronousFileChannel` 留给后续批次）
- 不做目录监听的事件批处理/丢失合并（OVERFLOW 直接上报由调用方处理）
- `SearchUtil` 不接收乱序输入（未排序列表结果未定义，文档注明）

## 6. 版本计划

批 38 与批 37 共同构成 **v1.18.0**（批 37 已推 main 为候选）；发布时机由研发小组长另行决策。
