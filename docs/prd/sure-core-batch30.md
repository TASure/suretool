# 批30 PRD：模糊测试 + Benchmark（v4.0 生态治理第二棒）

- 状态：已批准
- 目标版本：1.13.0（测试与基准增强，无 API 变更）

## 背景

v4.0 生态治理计划表批30：以生成式属性测试（jqwik 模糊测试）与 JMH 基准双线巩固可靠性证明。

## 交付内容

### 模糊测试（sure-core jqwik 属性测试，新增 8 例 → 累计 5 类）

| 测试类 | 属性 | 不变量 |
|---|---|---|
| CodecConvertPropertyTest（新，6 例） | base64RoundTrip / base64UrlSafeRoundTrip / base64StringRoundTrip | 任意 byte[]/String 编解码往返恒等 |
| 同上 | toIntNullSafety / toBooleanStable / convertStringRoundTrip | 任意输入不抛异常、默认值兜底、int↔String 往返稳定 |
| DateUtilPropertyTest（新，2 例） | formatParseRoundTrip | 格式化幂等：parse(format(d)) 再 format 与原串一致（4 种 pattern） |
| 同上 | formatDateFixedLength | 1970-2100 年 yyyy-MM-dd 恒 10 字符 |

踩坑记录：jqwik @Provide 须 public static 返回 Arbitrary；LongRange 在 net.jqwik.api.constraints；往返断言忌用毫秒容差（粒度敏感），改格式化幂等不变量。

### Benchmark（sure-benchmark 新增 ResultBenchmark，5 基准）

- resultOkGet / resultOkMap / optionalOfGet / optionalMap / exceptionFlow
- 验证「Result 显式错误门面零成本」：与 Optional 同量级（实测 ~0.57 vs ~0.56 ns/op）、远低于异常流
- BenchmarkRunner include 过滤已生效，本地跑通（jar 含 BenchmarkList 5 项）

## 验收标准

- 属性测试 8 例全绿（本地单独跑 BUILD SUCCESS）
- ResultBenchmark 本地可跑（No benchmarks to run 问题解决：-am install 全链路）
- 全量 `mvn -am verify` BUILD SUCCESS
- 零 API 变更、零新增依赖
