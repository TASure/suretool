# 批33 PRD：覆盖率 98% 攻坚 C（sure-extra/sure-math/sure-jwt/sure-xml/sure-cron/sure-cache/sure-dfa/sure-captcha/sure-template/sure-event/sure-aop）+ 门禁收官

- 状态：已批准
- 目标版本：1.13.1-SNAPSHOT（仅测试与门禁变更，无 API 变更）

## 背景

本片覆盖 11 个中小缺口模块（90%~94% 基线为主），并完成全库 23 个业务模块 jacoco 门禁统一收官。

## 交付内容

| 模块 | 基线 | 实测 | 门禁 | 补测文件 | 新增行数 |
|---|---|---|---|---|---|
| sure-extra | 90.49% | **98.37%** | 0.98 | Mail/Qr/Img/Ftp 四个 CoverTest（配置校验 + 内存图 + 本地假 FTP） | 456 |
| sure-math | 90.46% | **98.46%** | 0.98 | MathCoverTest（除零/舍入/溢出/空集合） | 261 |
| sure-jwt | 90.51% | **98.73%** | 0.98 | JwtCoverTest（RSA 往返/篡改/过期/缺私钥） | 170 |
| sure-xml | 90.37% | **99.26%** | 0.98 | XmlCoverTest（DOM 往返/非法 XML/命名空间） | 138 |
| sure-cron | 94.20% | **99.11%** | 0.98 | CronExtraTest（非法表达式/7 段年边界/幂等调度） | 90 |
| sure-cache | 92.08% | **99.51%** | 0.98 | CacheExtraTest（TTL/容量/GC/并发） | 71 |
| sure-dfa | 93.17% | **100.00%** | 0.98 | DfaExtraTest（词树/重叠命中/边界） | 67 |
| sure-captcha | 94.12% | **98.04%** | 0.98 | CaptchaExtraTest（字符集/干扰线/校验） | 37 |
| sure-template | 94.44% | **100.00%** | 0.98 | SimpleTemplateExtraTest（占位符/转义/缺失） | 58 |
| sure-event | 87.50% | **98.08%** | 0.98 | EventBusExtraTest（DeadEvent/异常传播/快照关闭） | 128 |
| sure-aop | 91.89% | **100.00%** | 0.98 | AspectExtraTest（代理回调/异常/默认实现） | 90 |

## 门禁收官

- 13 个既有门禁模块：jacoco.line.min 0.70~0.93 → **0.98**
- 10 个新增门禁模块（aop/compress/db/event/math/pdf/process/script/socket/template）：默认 0.70 → **0.98**（sure-socket → 0.97，理由见 sure-coverage-98-b.md）
- 全量 `mvn -am verify` BUILD SUCCESS（checkstyle FileLength 2600 / SpotBugs Max-Medium / license 全绿）

## 验收标准

- 11 模块 jacoco 行覆盖率实测 ≥0.98；23 个业务模块门禁统一（0.98，socket 0.97）
- 未发布版本、未触碰 release.yml / CI 发布通道
