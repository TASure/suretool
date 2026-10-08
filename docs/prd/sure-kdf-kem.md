# PRD：JDK25 密钥派生与密钥封装工具（KdfUtil / MlKemUtil / EcdhUtil）

> 批次：批 10　|　负责人：软件研发小组　|　状态：已评审，进入实现
> 版本目标：v1.7.0

## 1. 目标与范围

在 `sure-crypto` 提供基于 **JDK 25 正式特性**的现代密码学工具，兑现「JDK 25+ 独占差异化」：

- `KdfUtil`：基于 **JEP 510（KDF API，JDK25 转正）** 的 HKDF 密钥派生；
- `MlKemUtil`：基于 **JEP 496（ML-KEM，JDK25 实现）** 的后量子密钥对生成与编解码；
- `EcdhUtil`：基于 `KeyAgreement` 的 X25519 / EC 共享密钥协商（经典 KEM 语义，与 ML-KEM 互为补充）。

**范围内**：上述三个类的生成、派生、协商、编解码 API 与测试。
**范围外（不做）**：

- 不做 KEM 封装/解封装高级 API：JDK 25 **不含** `javax.crypto.KeyEncapsulationMechanism`（JEP 452 未进入 JDK 25，实测 `Security.getAlgorithms("KeyEncapsulationMechanism")` 为空）；未来 JDK 提供时再扩展 `MlKemUtil.encapsulate/decapsulate`；
- 不引入任何第三方加密库（核心零依赖原则）；
- 不做非对称加密整体框架（`RsaUtil` 已承担）。

## 2. 用户场景

1. **口令/密钥派生**：HKDF 从高熵源派生会话密钥、子密钥（TLS 1.3、WireGuard 同款构造）；
2. **后量子准备**：生成 ML-KEM（Kyber）密钥对，X.509/PKCS8 编解码，为 PQ 迁移储备；
3. **端到端协商**：双方 X25519 协商共享密钥，配合 AES-GCM 加密。

## 3. 架构评审结论（第 2 步）

| 项 | 结论 |
| --- | --- |
| 模块归属 | `sure-crypto` → `com.sure.tool.crypto`（module-info 已 exports，无需改动） |
| 类名 | `KdfUtil`、`MlKemUtil`、`EcdhUtil`（`XxxUtil` 命名规范） |
| 依赖边界 | 仅 JDK `java.base`，零第三方运行期依赖 |
| API 形状 | `public final class` + 私有构造器 + 静态方法 + `@since 1.7.0` + 中文 Javadoc + Tab 缩进 |

### 3.1 API 签名（评审通过）

```java
public final class KdfUtil {
	/** HKDF extract-then-expand：从 IKM 派生 length 字节密钥材料 */
	public static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length);
	public static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length, String algorithm); // "HKDF-SHA256|384|512"
	/** HKDF expand-only：基于已提取的伪随机密钥扩展 */
	public static byte[] hkdfExpand(SecretKey prk, byte[] info, int length, String algorithm);
}

public final class MlKemUtil {
	public static KeyPair generateKeyPair(String algorithm);              // "ML-KEM-512|768|1024"
	public static String publicKeyToBase64(PublicKey key);
	public static PublicKey publicKeyFromBase64(String base64, String algorithm);
	public static String privateKeyToBase64(PrivateKey key);
	public static PrivateKey privateKeyFromBase64(String base64, String algorithm);
}

public final class EcdhUtil {
	public static KeyPair generateKeyPair(String algorithm);              // "X25519" | "EC"
	public static byte[] computeSharedSecret(PrivateKey privateKey, PublicKey publicKey, String algorithm);
}
```

### 3.2 JDK25 实测结论（第 2 步证据）

| 能力 | JDK 25.0.4.1 状态 | 实测 |
| --- | --- | --- |
| KDF API（JEP 510） | ✅ 转正 | HKDF-SHA256/384/512，`deriveData`/`deriveKey` 正常 |
| ML-KEM（JEP 496） | ✅ 密钥对生成 | ML-KEM-512/768/1024，X.509/PKCS8 可编解码 |
| KEM API（JEP 452） | ❌ 未进 JDK25 | `KeyEncapsulationMechanism` 类不存在，算法列表为空 |
| X25519 协商 | ✅ | KeyAgreement 双方一致 |

## 4. 验收标准（门禁）

1. HKDF：已知向量长度正确、同一输入幂等、不同 info 输出不同；
2. ML-KEM：三种参数生成成功，公钥 base64 编解码往返一致；
3. ECDH：双方 computeSharedSecret 一致（X25519）；
4. 全量 `mvn verify` EXIT=0（checkstyle / license / SpotBugs / jacoco ≥0.70）。
