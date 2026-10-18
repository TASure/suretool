# 批46：默认安全基线强化（阶段 B「好用」第二主轴，v1.19.0 候选）

## 一、背景与定位

超越路线图阶段 B「好用」= 差异化领先。批45 落地独占能力 Actor（结构化并发库首项）；批46 转向战略点名项 **「默认安全基线强化」**：全部加密 / 哈希 / 令牌 API 默认安全值，安全指南文档化。

对标盘点（四框架密码哈希与安全承诺）：

| 能力 | Hutool | Guava | Apache Commons | suretool（现状） |
|---|---|---|---|---|
| BCrypt 密码哈希 | ✅ `cn.hutool.crypto.digest.BCrypt` | ❌ | ❌ | ❌ **缺口** |
| 默认安全承诺文档 | 无系统性承诺 | ❌ | ❌ | ❌ **缺口** |
| 安全默认值回归审计测试 | 无 | ❌ | ❌ | ❌ **缺口** |
| 密码哈希零第三方依赖 | ❌（依赖 hutool 内部） | — | — | 可做到 ✅ |

本批三项交付：
1. **`BCryptUtil`**（新建 sure-crypto，零第三方依赖）：密码哈希标准能力——自动随机盐、cost 可配、恒定时间比较、`$2a`/`$2b`/`$2y` 前缀兼容、**超过 72 字节密码显式拒绝**（对齐 Spring Security 现代实践，不做 Hutool 式静默截断，JavaDoc 注明差异）。
2. **`docs/security-guide.md`**：默认安全基线承诺矩阵（全部加密 / 哈希 / 令牌 API 默认参数 + 危险用法边界 + 密码存储最佳实践 + 对标差异）。
3. **`SecurityBaselineTest`**（sure-crypto 测试）：现有 API 默认安全参数防回归审计（随机 IV / GCM 认证 / OAEP / 最小密钥 / 盐唯一性）。

## 二、BCryptUtil API 设计（新建 `com.sure.tool.crypto.BCryptUtil`）

| 方法 | 说明 | 边界 |
|---|---|---|
| `static String hash(String password)` | 密码哈希：自动随机盐（SecureRandom），默认 cost=10，输出 `$2a$10$` 前缀 60 字符 | password null 抛 NPE；>72 字节（UTF-8）抛 IAE |
| `static String hash(String password, int cost)` | 指定 cost 哈希 | cost 越界（<4 或 >30）抛 IAE |
| `static boolean check(String password, String hash)` | 恒定时间比较验证，兼容 `$2a`/`$2b`/`$2y` 前缀 | hash 格式非法抛 IAE；password null 抛 NPE |
| `static String gensalt()` | 生成 `$2a$` 前缀随机盐（22 字符 BCrypt base64） | — |
| `static String gensalt(int cost)` | 指定 cost 生成盐 | cost 越界抛 IAE |
| `static boolean isValid(String hash)` | 格式校验（前缀 / 长度 / cost 范围 / 字符集 / 分隔符） | null 返回 false |

### 语义约定

1. **算法**：EksBlowfish（Blowfish 变体密钥扩展，2^cost 轮），输出 24 字节经 BCrypt 自定义 base64 编码；实现零第三方依赖。
2. **随机盐**：16 字节 SecureRandom 熵，哈希中内嵌盐，同密码两次哈希结果不同。
3. **恒定时间比较**：`check` 对哈希串做固定长度异或累积比较，不短路，防时序侧信道。
4. **72 字节上限**：密码 UTF-8 字节数 >72 抛 `IllegalArgumentException`（BCrypt 算法只使用前 72 字节；静默截断会让不同密码共享哈希，属安全反模式，显式拒绝并对齐 Spring Security 5+ 行为）。
5. **前缀兼容**：`$2a`（生成格式，Hutool 互操作）/ `$2b`（Python bcrypt、现代 OpenBSD 格式）/ `$2y`（C 实现修正格式）验证统一走同一算法路径（差异仅涉及 0xFF 字节极端边界，普通密码三者等价）。
6. **线程安全**：无状态静态方法，内部不共享可变状态。

## 三、正确性验证方案（交叉验证，双源）

1. **Python 参考向量**：`python3 -c "import bcrypt"`（v5.0.0，OpenBSD 参考实现）生成 **10 组确定性哈希**（空串 / 普通 / 短语 / 特殊字符 / 中文 / 72 字节 / 多种 cost 4/8/10/12/14），Java `check` 必须全部通过；错误密码必须 False。
2. **自洽往返**：Java `hash` → `check` 全量通过；不同盐同密码哈希不同。
3. **格式互操作**：Java 生成 `$2a$` 哈希 → Python `bcrypt.checkpw` 验证通过（双向互验）。

## 四、测试清单

### BCryptUtilTest（17 例）
| 用例 | 断言 |
|---|---|
| 10 组 Python 参考向量 check 通过 | 每组 check(password, hash) == true |
| 参考向量错误密码 | check("wrong", hash) == false |
| hash 自洽往返（cost 4/10/12） | hash 后 check true |
| 随机盐唯一性 | 同密码两次 hash 不同；内嵌盐不同 |
| 恒定时间比较行为 | 不同长度/内容 hash 均正常返回 false，不抛异常 |
| $2a/$2b/$2y 前缀兼容 | 三种前缀合法 hash check 均 true |
| gensalt 格式 | 29 字符 `$2a$cc$` + 22 盐；可解出 cost |
| isValid 合法/非法 | 坏前缀 / 坏长度 / 坏字符 / 坏 cost → false |
| cost 越界 | hash/gensalt cost=3、31 抛 IAE |
| >72 字节密码 | 73 字节 UTF-8 抛 IAE；72 字节正常 |
| null 防御 | hash(null) NPE；check(null, h) NPE；check(p, null) IAE；isValid(null) false |
| 输出格式精确 | 60 字符、`$2a$` 前缀、5 段结构 |

### SecurityBaselineTest（8 例，现有 API 防回归）
| 用例 | 断言 |
|---|---|
| AesUtil 随机 IV | 同明文两次加密密文不同 |
| AesUtil GCM 认证 | 密文篡改 1 字节解密失败 |
| AesUtil IV 长度 | 密文头 12 字节 IV（解密自洽即证） |
| RsaUtil 最小密钥 | keySize=1024 抛 IAE；2048 正常 |
| RsaUtil OAEP 签名 | sign/verify 往返；篡改验签失败 |
| SecureRandom 熵 | 16 字节盐两两不同（循环 32 组） |
| SecureUtil 委托 | aesEncrypt/decrypt 委托 GCM 默认（自洽） |
| 哈希基线 | sha256/sha512 长度正确（64/128 hex） |

## 五、安全指南文档（docs/security-guide.md）

- 默认安全基线承诺矩阵（AesUtil / RsaUtil / HmacUtil / Sm2-4 / KdfUtil / MlKemUtil / EcdhUtil / BCryptUtil 默认参数与承诺）
- 危险用法边界（DesUtil 仅存量、md5/sha1 仅完整性、hmacSha1 不建议、密钥管理）
- 密码存储最佳实践（BCrypt cost 建议 10-12、随机盐、72 字节上限原因、恒定时间比较）
- 对标差异（Hutool AES 默认 ECB/CBC 无认证 vs suretool 默认 GCM；三家无密码哈希 vs suretool BCrypt）
- 与 SECURITY.md 响应 SOP 的衔接

## 六、验收标准

1. `BCryptUtil` 行覆盖 ≥98%（目标 100%），sure-crypto 全量行覆盖 ≥98% 门禁。
2. Python 10 组参考向量全过 + 双向互验（Java→Python、Python→Java）。
3. checkstyle / SpotBugs / license / japicmp 全绿；全量测试绿。
4. 中文 JavaDoc，每方法带可运行示例。
5. `docs/security-guide.md` 交付。
6. CHANGELOG [Unreleased] 新增批46 条目；推送 main 后回读验证 SHA 一致。
