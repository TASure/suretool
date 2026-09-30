# 数学 · 进程 · 压缩 三合一速查（sure-math / sure-process / sure-compress）

> 适用版本：suretool ≥ 1.4.0 ｜ JDK 25+ ｜ Maven 坐标 `io.github.tasure:sure-math`（其余模块同理）

## 1. sure-math：大数、素数、随机与精确小数

### 1.1 素数判定与生成

```java
import com.sure.tool.math.MathUtil;

boolean p = MathUtil.isPrime(104729);          // true
long next = MathUtil.nextPrime(104729);        // 104759
java.util.List<Integer> primes = MathUtil.primes(1000); // 168 个素数（埃氏筛）
```

### 1.2 公约数与组合数学

```java
long g = MathUtil.gcd(1071, 462);   // 21
long l = MathUtil.lcm(12, 18);      // 36
long f = MathUtil.fibonacci(20);    // 6765
long c = MathUtil.comb(10, 3);      // 120
long p = MathUtil.perm(10, 3);      // 720
```

### 1.3 精确小数（默认 10 位 HALF_UP，杜绝浮点脏数据）

```java
import com.sure.tool.math.BigDecimalUtil;
import java.math.BigDecimal;

BigDecimal a = BigDecimalUtil.add(new BigDecimal("0.1"), new BigDecimal("0.2")); // 0.3
BigDecimal r = BigDecimalUtil.roundHalfUp(new BigDecimal("1234.56789"), 2);      // 1234.57
```

### 1.4 随机工具（ThreadLocalRandom / SecureRandom）

```java
import com.sure.tool.math.RandomUtil;

String s = RandomUtil.randomString(16);      // 字母数字混合
String n = RandomUtil.randomNumbers(6);     // 纯数字
byte[] b = RandomUtil.randomBytes(32);      // 密码学安全（SecureRandom）
```

## 2. sure-process：进程门面（超时 + 防管道死锁）

```java
import com.sure.tool.process.ProcessUtil;
import com.sure.tool.process.ProcessResult;
import java.util.List;
import java.util.concurrent.TimeUnit;

ProcessResult r = ProcessUtil.exec("ping -c 1 127.0.0.1");
System.out.println(r.getExitCode());   // 0
System.out.println(r.getStdout());     // 完整标准输出
System.out.println(r.isTimedOut());    // false

// 带超时：2 秒杀不掉直接 destroyForcibly，杜绝挂死
ProcessResult r2 = ProcessUtil.execWithTimeout(List.of("sleep", "100"), 2, TimeUnit.SECONDS);
System.out.println(r2.isTimedOut());   // true
```

> 底层用虚拟线程并发读 stdout/stderr，不会因管道缓冲区写满而互相阻塞。

## 3. sure-compress：7z 与 Brotli

```java
import com.sure.tool.compress.SevenZUtil;
import java.nio.file.Path;

// 目录递归压缩
SevenZUtil.compressDir(Path.of("data/"), Path.of("data.7z"));
// 解压（内置路径穿越校验，防止 zip-slip）
SevenZUtil.decompress(Path.of("data.7z"), Path.of("out/"));
```

```java
import com.sure.tool.compress.BrotliUtil;

byte[] raw = "hello suretool".getBytes(java.nio.charset.StandardCharsets.UTF_8);
byte[] dec = BrotliUtil.decompress(raw);   // 纯 Java 解码
```

## 4. 典型场景组合

- **订单金额汇总**：`BigDecimalUtil` 累加，最后 `roundHalfUp` 两位——金额永不出现 0.30000000000000004。
- **密码学随机数**：`RandomUtil.randomBytes(32)` 用于盐值/令牌，不要用 `Math.random()`。
- **定时任务快照**：`ProcessUtil.execWithTimeout` 跑系统命令，避免进程泄漏。
- **日志包上传**：`SevenZUtil.compress` 打包后 Brotli 二次压缩，体积最优。

> 完整 API 见各模块 Javadoc；示例见 `sure-examples` 的 `MathDemo` / `ProcessDemo` / `CompressDemo`。
