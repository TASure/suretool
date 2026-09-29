# PRD：sure-math 数学工具模块

| 项 | 内容 |
|---|---|
| 版本 | v1.4.0（批 7） |
| 模块 | sure-math |
| 包 | com.sure.tool.math |
| 依赖 | 仅 sure-core（零第三方运行期依赖） |
| 许可 | Apache-2.0 |

## 背景与目标

提供精确计算与常用数学门面：素数、公约数/公倍数、阶乘/组合、进制转换、位运算、BigDecimal 精确运算、随机数、数字解析。对标 Hutool 的 MathUtil/BigDecimalUtil/RandomUtil/NumberUtil。

## API 设计（私有构造器 + @since 1.4.0）

- `MathUtil`：`isPrime`、`nextPrime`、`primes(n)`（埃氏筛）、`gcd`/`lcm`（int 与 long 变参）、`factorial`、`fibonacci`、`comb`/`perm`、`toBinary`/`toHex`/`toOctal`/`toBase`、`isPowerOfTwo`/`nextPowerOfTwo`
- `BigDecimalUtil`：`add`/`sub`/`mul`/`div`（默认 10 位 HALF_UP，可指定 scale）、`round`/`roundHalfUp`、`equals`（stripTrailingZeros 语义）、`isNumber`、`toBigDecimal`（null/空→null）
- `RandomUtil`：`randomInt`/`randomLong`/`randomDouble`（区间）、`randomString`/`randomNumbers`/`randomBytes`、`randomElement`、`shuffle`、SecureRandom 变体（`secureRandomInt` 等）
- `NumberUtil`：`parseInt`/`parseLong`/`parseDouble`（异常回退默认值）、`isInteger`/`isLong`/`isDouble`/`isNumber`、`toInt`/`toLong`（null→默认）、`min`/`max`（变参）

## 验收清单

- A1 素数判断与生成正确（含边界 2、合数、大数）
- A2 gcd/lcm/factorial/comb/perm 数值正确
- A3 进制转换含 0、负数、大基数
- A4 BigDecimal 运算精度可控、equals 忽略尾零
- A5 随机数在闭区间内、字符串长度正确、shuffle 保留元素集合
- A6 NumberUtil 非法输入回退默认值不抛异常
- A7 覆盖率 ≥ 70%（模块门禁）
- A8 中文 Javadoc + 私有构造器

## 架构评审

- 全部静态门面、无状态、线程安全。
- 随机数默认 `ThreadLocalRandom`（性能），安全场景提供 SecureRandom 变体。
