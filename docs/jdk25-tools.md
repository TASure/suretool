# JDK 25 差异化能力（sure-concurrent）

## 设计（10 行以内）

1. **虚拟线程**：`VirtualThreads.start(name, task)` / `newThreadPerTaskExecutor(prefix)`，API 形态为工厂 + 执行器，JDK 21 起正式，放本模块作为并发基线。
2. **结构化并发**：`StructuredTaskScope` 在 JDK 25 仍是**预览 API**（JEP 505），封装会迫使使用方加 `--enable-preview`，故不产出 jar 代码，仅在本文件给出推荐用法。
3. **Scoped Values**：`ScopedValueUtil` —— `newKey` / `where`（Runnable 与 Supplier 两个重载）/ `orElse` / `isBound`，JDK 25 起正式。
4. **Class-File API**：`ClassFileUtil` —— `parse` / `parseFile` / `methodNames` / `methodSignatures` / `generate`，JDK 24 起正式。
5. **放置模块**：新建 `sure-concurrent`，理由：Class-File API 属字节码域，塞进 `sure-core.thread` 语义不符；独立模块零依赖、可单独演进，不扩大 `sure-core` 的 API 面。
6. **@since**：两套 API 均为 `1.20.0`（下一个功能版本）。

> 结论：JDK 17/21 的 `javac` 无法编译本模块（`ScopedValue` 未正式、`java.lang.classfile` 不存在）；
> 结构化并发在 JDK 25 上不加 `--enable-preview` 同样无法编译，故未封装。

## ScopedValueUtil

```java
ScopedValue<String> REQUEST_ID = ScopedValueUtil.newKey();

ScopedValueUtil.where(REQUEST_ID, "req-1", () -> {
    String id = REQUEST_ID.get();                 // req-1
    String same = ScopedValueUtil.orElse(REQUEST_ID, "none");
});

String outside = ScopedValueUtil.orElse(REQUEST_ID, "none"); // none
```

| 方法 | 说明 |
| --- | --- |
| `newKey()` | 创建未绑定键 |
| `where(key, value, Runnable)` | 绑定执行，退出自动恢复 |
| `where(key, value, Supplier<R>)` | 绑定执行并返回结果 |
| `orElse(key, default)` | 未绑定返回缺省值；null 是合法绑定值，不回退 |
| `isBound(key)` | 当前线程是否绑定 |

绑定是线程本地的：`where` 内启动的新线程看不到绑定（需要跨线程可用 JDK 预览的结构化并发）。

## ClassFileUtil

```java
byte[] bytes = ClassFileUtil.generate("com.sure.tool.demo.Generated");
ClassFileUtil.ClassInfo info = ClassFileUtil.parse(bytes);
info.className();     // com.sure.tool.demo.Generated
info.superName();     // java.lang.Object
info.majorVersion();  // 69（JDK 25）
info.interfaces();    // []
```

| 方法 | 说明 |
| --- | --- |
| `parse(byte[])` / `parseFile(Path)` | 解析类名 / 父类 / 版本 / 接口 |
| `methodNames(byte[])` | 方法名（保留重载重复项） |
| `methodSignatures(byte[])` | 方法名 + 描述符，如 `run()V` |
| `generate(binaryName)` | 生成仅含 public 无参构造器的空类 |

非法输入：`null` → `NullPointerException`；空字节 / 格式非法 / 非法二进制类名 → `IllegalArgumentException`。

## 结构化并发（仅文档，不封装）

JDK 25 的 `StructuredTaskScope` 仍是预览 API，发布到 Central 的 jar 若引用它会要求使用方
统一加 `--enable-preview`，破坏“零额外开关”的使用体验，因此本项目不提供封装。推荐直接使用 JDK 原生写法：

```java
// 需 javac --release 25 --enable-preview
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var user = scope.fork(() -> loadUser(id));
    var orders = scope.fork(() -> loadOrders(id));
    scope.join().throwIfFailed();
    return new Profile(user.get(), orders.get());
}
```

## 编译边界验证

```bash
# JDK 25：通过
mvn -B -pl sure-concurrent verify

# JDK 21：ScopedValue / java.lang.classfile 不可用（项目本身也要求 release=25）
javac --release 21 -d /tmp/out sure-concurrent/src/main/java/com/sure/tool/concurrent/*.java
```

模块声明：`module sure.concurrent { exports com.sure.tool.concurrent; }`，零 `requires`。
