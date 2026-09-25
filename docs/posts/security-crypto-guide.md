# 加解密正确姿势：sure-crypto 默认安全指南

> 目标：用最少的配置写出"默认安全"的加解密代码。sure-crypto 的设计原则——**不安全的默认值是 bug**。

## 1. 哈希（不可逆）

```java
import com.sure.tool.crypto.SecureUtil;

// 摘要（十六进制）
String md5 = SecureUtil.md5("hello");          // 兼容旧系统，新系统优先 SHA-256
String sha256 = SecureUtil.sha256("hello");    // 推荐：碰撞风险低
String sha512 = SecureUtil.sha512("hello");

// 带密钥的哈希（防篡改 / 接口签名）
String mac = SecureUtil.hmacSha256(data, secretKey);  // HMAC-SHA256
```

> 存用户密码请配合加盐（salt）使用；HMAC 适合接口签名与防篡改校验。

## 2. 对称加密（AES，默认 GCM）

```java
import com.sure.tool.crypto.AesUtil;

// 生成密钥（Base64，每次调用随机）
String key = AesUtil.generateKey();

// 加密 → 输出 Base64；解密还原
String cipherBase64 = AesUtil.encryptBase64("机密内容", key);
String plain = AesUtil.decryptBase64(cipherBase64, key);

// 也可输出十六进制
String cipherHex = AesUtil.encryptHex("机密内容", key);
String plainHex = AesUtil.decryptHex(cipherHex, key);
```

> 默认使用 AES-GCM（带认证加密，防篡改），密钥 256 位随机生成。**不要**手工拼 `AES/ECB/PKCS5Padding`。

## 3. 非对称加密（RSA，最小 2048 位）

```java
import com.sure.tool.crypto.RsaUtil;
import java.security.KeyPair;
import java.security.Key;
import java.util.Base64;

KeyPair pair = RsaUtil.generateKeyPair();       // 默认 2048 位
Key pub = pair.getPublic();
Key pri = pair.getPrivate();

byte[] enc = RsaUtil.encrypt("小段数据".getBytes(), pub);   // 公钥加密
byte[] dec = RsaUtil.decrypt(enc, pri);                    // 私钥解密
System.out.println(new String(dec));                       // 小段数据
```

> RSA 适合小数据（密钥/对称密钥交换、签名验签）；大数据加密请用 AES。

## 4. 国密算法（SM2/SM3/SM4）

```java
import com.sure.tool.crypto.SmUtil;
import com.sure.tool.crypto.Sm3Util;
import com.sure.tool.crypto.Sm4Util;
import java.security.KeyPair;

// SM3 摘要（国密哈希，十六进制输出）
String sm3 = Sm3Util.sm3Hex("hello");

// SM4 对称加密（国密 AES 对标）：密钥为 byte[]，encryptHex(key, text) / decryptStr(key, hex)
byte[] sm4Key = Sm4Util.generateKey();
String cipher = Sm4Util.encryptHex(sm4Key, "机密");
String plain = Sm4Util.decryptStr(sm4Key, cipher);
```

> 合规场景（金融/政务）使用国密套件；`SmUtil` 提供 SM2 密钥对生成与加解密。

## 5. 常见错误清单

| ❌ 错误做法 | ✅ 正确做法 |
| --- | --- |
| MD5 存用户密码 | 加盐 + SHA-256/BCrypt |
| AES/ECB 模式 | AES-GCM（默认） |
| 硬编码密钥 | `AesUtil.generateKey()` + 密钥管理服务 |
| RSA 1024 位 | 2048 位起步（`MIN_KEY_SIZE = 2048` 已锁死） |
| 私钥放代码仓库 | 环境变量 / 密钥管理系统 |

## 6. 安全工程护栏

- sure-crypto 模块有独立的 CodeQL 与 OSV-Scanner 扫描（CI 自动执行）
- 覆盖率门禁 ≥90%，加解密路径有属性测试兜底
- 全部 API 默认采用认证加密模式（GCM），拒绝不安全默认值

> 需要 SM2 签名验签或更多场景，见 [sure-examples/CryptoDemo](../../sure-examples/src/main/java/com/sure/tool/example/CryptoDemo.java)。
