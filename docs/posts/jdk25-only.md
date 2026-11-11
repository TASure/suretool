# 只用 JDK 25 能换来什么：Scoped Values 与 Class-File API 的一次务实评估

suretool 从 1.20.0 起把编译基线定在 JDK 25，并新增了 `sure-concurrent` 模块（[模块说明](../modules.md)）。
这不是追新，而是两个已经**转正**的 API 让两类长期依赖第三方库的问题可以直接用 JDK 解决：

- **Scoped Values**（[JEP 506](https://openjdk.org/jeps/506)，JDK 25 正式）
- **Class-File API**（[JEP 484](https://openjdk.org/jeps/484)，JDK 24 正式）

下面每个结论都配可运行代码，并说明它在 JDK 17/21 上为什么做不到。

## 1. Scoped Values：替代 ThreadLocal 的请求上下文

### 问题

ThreadLocal 的经典坑不是「线程不安全」，而是**生命周期**：线程池复用线程时，
值会跟着线程活到下一次任务，忘记 `remove()` 就串数据；虚拟线程数量大时，
每个 ThreadLocal 还要占用一份线程本地存储。

```java
// 传统写法：必须在 finally 里 remove，漏一次就是事故
REQUEST_ID.set(id);
try {
    handle();
} finally {
    REQUEST_ID.remove();
}
```

### JDK 25 写法

```java
import com.sure.tool.concurrent.ScopedValueUtil;

static final ScopedValue<String> TENANT = ScopedValueUtil.newKey();

String inside = ScopedValueUtil.where(TENANT, "acme", () -> "tenant=" + TENANT.get());
String outside = ScopedValueUtil.orElse(TENANT, "none");
```

作用域退出后绑定自动恢复，不存在「忘记清理」。绑定只对**执行 `where` 的那个线程**可见，
新起的普通线程不会继承（这正是避免隐式共享的关键）。

### 为什么 17/21 不行

| JDK | Scoped Values 状态 | 能否直接编译 `ScopedValue` |
| --- | --- | --- |
| 17 | 不存在 | 否 |
| 21 | 预览（JEP 446） | 仅 `--enable-preview`，且 API 形态与 25 不同 |
| 22 / 23 / 24 | 继续预览（JEP 480 / 487 / 497） | 仅 `--enable-preview`，预览期仍有调整 |
| 25 | **正式**（JEP 506） | 是，无需 `--enable-preview` |

也就是说，用 25 编译的字节码在 21 上既不能运行（class 文件主版本 69 > 65），
源码也不能直接回退编译：预览期 `Carrier.call` 接收的是 `Callable`，
正式版（JEP 506）改成接收 `Supplier`，异常语义也不同。
写一层「21 可用、25 可用」的 shim，实际上等于同时维护两套语义，收益极低。

## 2. Class-File API：不引 ASM 也能读写字节码

### 问题

想在启动期扫描 class、或在构建工具里生成类，过去基本绕不开 ASM：

- 多一个第三方依赖，还要跟着 JDK 版本升级（每次新增 class 文件特性，ASM 都要发新版）；
- 核心模块「零第三方运行期依赖」的约束会被打破。

### JDK 25 写法

```java
import com.sure.tool.concurrent.ClassFileUtil;

byte[] bytes = ClassFileUtil.generate("demo.Generated");
var info = ClassFileUtil.parse(bytes);
// info.className() = "demo.Generated"
// info.superName() = "java.lang.Object"
// info.majorVersion() = 69  ← JDK 25
List<String> signatures = ClassFileUtil.methodSignatures(bytes); // ["<init>()V"]
```

读一个已有类同样简单：

```java
var self = ClassFileUtil.parseFile(Path.of("target/classes/demo/App.class"));
System.out.println(self.className() + " major=" + self.majorVersion());
```

### 为什么 17/21 不行

- JDK 17：`java.lang.classfile` 包不存在。
- JDK 21：同样不存在；Class-File API 到 JDK 22 才进入预览（JEP 457），24 转正（JEP 484）。
- 在 17/21 上想做同样的事，只能继续用 ASM 或自己解析常量池。

## 3. 一次真实运行

环境：Windows 11 + OpenJDK 25.0.4，`sure-concurrent-1.20.0.jar`。

```bash
javac -cp sure-concurrent-1.20.0.jar -d out Jdk25Demo.java
java  -cp "out;sure-concurrent-1.20.0.jar" Jdk25Demo
```

输出（原样粘贴）：

```text
tenant=acme
outside=none
generated.class=demo.Generated super=java.lang.Object major=69
generated.methods=[<init>()V]
self.class=Jdk25Demo
self.methods=[<init>, main, lambda$main$1, lambda$main$0, <clinit>]
virtualThread.tenant=beta
```

三件事被同时验证：作用域外读不到绑定、生成的 class 主版本号为 69（JDK 25）、
虚拟线程内再次绑定互不影响。

完整示例（含虚拟线程片段）：

```java
import com.sure.tool.concurrent.ClassFileUtil;
import com.sure.tool.concurrent.ScopedValueUtil;

import java.util.concurrent.Executors;

public class Jdk25Demo {

    static final ScopedValue<String> TENANT = ScopedValueUtil.newKey();

    public static void main(String[] args) throws Exception {
        System.out.println(ScopedValueUtil.where(TENANT, "acme", () -> "tenant=" + TENANT.get()));
        System.out.println("outside=" + ScopedValueUtil.orElse(TENANT, "none"));

        byte[] generated = ClassFileUtil.generate("demo.Generated");
        var info = ClassFileUtil.parse(generated);
        System.out.println("major=" + info.majorVersion()
                + " methods=" + ClassFileUtil.methodSignatures(generated));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var future = executor.submit(() -> ScopedValueUtil.where(TENANT, "beta", TENANT::get));
            System.out.println("virtualThread.tenant=" + future.get());
        }
    }
}
```

## 4. 什么时候值得升到 25

**值得**

- 新建服务/工具，没有历史包袱，且需要请求级上下文（租户、追踪 ID、鉴权主体）——Scoped Values 比 ThreadLocal 更难写错。
- 编写构建插件、启动期扫描、代码生成器，希望去掉 ASM 依赖。
- 已经用虚拟线程，线程数量大、ThreadLocal 数量多，内存与串数据风险都在上升。

**不值得（或需要等）**

- 你的库被大量 JDK 17/21 用户引用。suretool 只服务 25，是因为它定位为「工具库 + 应用侧依赖」，
  且明确放弃了 17/21 兼容；面向 17 的库不要照搬这个决定。
- 依赖链里有尚未验证 25 的框架/插件。升级前请用真实工程跑一遍 `mvn -B verify`，不要只看 release notes。
- 只是想要「新语法」。Scoped Values 和 Class-File API 的收益都来自**去掉一类依赖或一类 bug**，不是语法糖。

## 5. 代价要说清楚

1. **运行时只有 25**：class 文件主版本 69，17/21 的 JVM 直接拒绝加载。
2. **无法回退编译**：`maven.compiler.release=25` 需要 JDK 25 的 `javac`。
3. **API 面很窄**：`sure-concurrent` 只封装 `ScopedValue` 与 `Class-File API`，
   没有封装仍处于预览状态的结构化并发（`StructuredTaskScope`），避免把预览 API 带进公开契约。
4. **收益是可量化的「少依赖」**：核心路径少一个 ASM 级别的第三方库，少一类 ThreadLocal 泄漏，
   但没有性能上的承诺——我们没有针对这两个 API 做基准测试，也不打算给出未经验证的吞吐数字。

## 6. 复现

- 模块源码：[sure-concurrent](../../sure-concurrent)
- API 索引：[docs/modules.md](../modules.md)
- 构建要求：JDK 25；`mvn -B -pl sure-concurrent verify`

如果你在 21 上评估过同类能力，欢迎在 issue 里贴出你的场景与迁移成本，我们会把真实案例补进这篇文章。
