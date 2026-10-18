<!--
Copyright (c) 2026 suretool contributors
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
-->

# suretool 默认安全基线指南

> 本文档是 suretool 对「默认安全」的公开承诺：**所有加密、哈希、令牌相关 API 的默认参数即为安全参数，绝不静默降级**。
> 适用版本：v1.19.0+（`sure-crypto` 模块）。审计测试见 `SecurityBaselineTest`，任何回归都会导致 CI 失败。

## 1. 安全承诺矩阵

| API | 默认算法 | 默认安全参数 | 弱参数处理 | 说明 |
| --- | --- | --- | --- | --- |
| `AesUtil` | AES-GCM（`AES/GCM/NoPadding`） | 随机 12 字节 IV（SecureRandom）、GCM 认证标签 128 位、密钥 SHA-256 派生 | 无降级路径；密文格式 `IV \|\| ciphertext+tag` | 认证加密，防篡改，消除 padding oracle |
| `RsaUtil` | RSA-OAEP（`OAEPWithSHA-256AndMGF1Padding`） | 密钥 ≥ 2048 位、SHA-256 签名 | `generateKeyPair(<2048)` 抛 `IllegalArgumentException` | OAEP 随机填充，CCA 安全 |
| `BCryptUtil` | OpenBSD bcrypt（EksBlowfish） | cost 默认 10（2^10 轮）、16 字节随机盐、`$2a$` 前缀 | cost∉[4,30] 抛异常；密码 >72 字节抛异常（**不静默截断**） | 恒定时间比较，兼容 `$2a/$2b/$2y` |
| `HmacUtil` | HMAC-SHA256 | 密钥最小长度约束 | 空密钥拒绝 | 消息认证 |
| `KdfUtil` / `MlKemUtil` | 现代 KDF / ML-KEM（PQC） | 默认迭代次数满足 OWASP 建议 | 弱参数拒绝 | 后量子加密 |

## 2. BCrypt 密码哈希（v1.19.0 新增）

### 2.1 快速上手

```java
// 注册/改密时：生成哈希（默认 cost=10）
String hash = BCryptUtil.hash("P@ssw0rd!2026");
// => $2a$10$<22位盐><31位哈希>（共 60 字符）

// 登录校验
boolean ok = BCryptUtil.check("P@ssw0rd!2026", hash);
```

### 2.2 安全语义（与竞品的差异，务必知悉）

| 语义 | suretool `BCryptUtil` | Hutool `BCrypt`（jBCrypt 移植） | Spring Security 5+ |
| --- | --- | --- | --- |
| 密码 >72 字节 | **抛 `IllegalArgumentException`** | 静默截断（危险） | 抛异常 |
| 默认 cost | 10 | 10 | 10 |
| 前缀 | `$2a$` | `$2a$` | `$2a$` |
| 校验恒定时间 | 是，逐字节异或不短路 | 是 | 是 |
| 兼容 `$2b$/$2y$` | 是，互操作已验证（Python bcrypt / OpenBSD） | 仅 `$2a$` | 是 |

> **为什么不静默截断**：Hutool 对超过 72 字节的密码静默截断，用户以为设置了完整密码、实际只校验前 72 字节，
> 会带来「两段不同密码可互相登录」的安全事故。suretool 选择显式报错，倒逼调用方在接入层限制密码长度
> （如登录表单 maxlength 与后端校验），符合 OWASP Password Storage Cheat Sheet 建议。

### 2.3 建议

- **cost 选择**：交互式登录 10（约 60–100ms/次）；高安全场景（管理后台）12；注册时可临时提高至 12 然后回落。
- **密码长度策略**：建议限制 UTF-8 字节 ≤ 72（约 24 个汉字 / 72 个 ASCII 字符），并在接入层与存储层同时校验。
- **不要自造盐**：盐由 `BCryptUtil.gensalt()` 生成（16 字节 SecureRandom），无需也不应自行拼接。
- **跨库迁移**：存量 `$2b$/$2y$` 哈希可直接用 `BCryptUtil.check` 校验，无需批量重算。

## 3. 现有 API 默认安全回顾

- `AesUtil`：`encrypt` 每次生成随机 12 字节 IV，输出 `IV || body`；解密自动解析。**同明文两次加密密文不同**（语义安全）。
- `RsaUtil`：默认 2048 位密钥 + OAEP-SHA256；`encrypt` 数据长度上限受 OAEP 填充限制（2048 位约 190 字节），超限抛 `CryptoException`。
- `DesUtil`（遗留）：仅用于兼容旧系统，新代码请使用 `AesUtil`。

## 4. 回归防护

`SecurityBaselineTest`（8 例）审计：
1. AES 默认认证加密 + IV 头；
2. AES 随机 IV（两次加密不同）；
3. RSA OAEP 往返；
4. RSA 最小 2048 位强制；
5. BCrypt 默认 cost=10；
6. BCrypt 拒绝 >72 字节（无静默截断）；
7. 安全指南文档存在且承诺完整；
8. 弱参数全路径拒绝（无静默降级）。

任何默认参数被削弱、或新增"便捷但弱安全"的重载，必须同步更新本指南与审计测试，否则 CI 失败。

## 5. 免责与报告

- 本指南覆盖 `sure-crypto` 模块当前公开 API；新增 API 须遵循「默认即安全」原则并入审计。
- 发现安全问题请通过 GitHub Issues（`TASure/suretool`）提交，标注 `security` 标签。
