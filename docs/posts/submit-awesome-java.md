# awesome-java 提交材料

> [awesome-java](https://github.com/akullpp/awesome-java) 是 Java 生态最权威的资源清单。
> 本文件整理 suretool 的提交条目，发布稳定版本（Maven Central 可搜）后提交。

## 提交条目（提交到相应分类）

### Tools / Utilities 分类

```markdown
* [suretool](https://github.com/TASure/suretool) - A small but complete Java utility library for JDK 21+, secure-by-default crypto, zero-dependency core, with BOM and Spring Boot starter.
```

### 可选补充（JSON / Security 分类）

```markdown
* [suretool-json](https://github.com/TASure/suretool) - Lightweight JSON parser and serializer (JSONObject/JSONArray/JSONUtil) for JDK 21+.
* [suretool-crypto](https://github.com/TASure/suretool) - Secure-by-default crypto utilities: AES-GCM, RSA-OAEP, HMAC, deprecated DES for legacy data.
```

## 仓库级元数据（供提交时填写）

| 字段 | 值 |
| --- | --- |
| 名称 | suretool |
| 描述 | 小而全的 Java 工具库（JDK 21+、默认安全、零依赖核心、BOM + Starter） |
| 主页 | https://github.com/TASure/suretool |
| 许可证 | Apache-2.0 |
| 标签 | java, utility, toolkit, jdk21, security, json, cache, jwt, captcha |

## 提交前检查清单

- [ ] 发布首个稳定版本到 Maven Central（`io.github.tasure:sure-core` 等可搜索）
- [ ] README 徽章：Maven Central 版本徽章、CI、覆盖率、CodeQL
- [ ] 仓库有明确贡献指南（CONTRIBUTING.md）与行为准则（Code of Conduct）
- [ ] GitHub Releases 含版本标签与 CHANGELOG 摘要
- [ ] 中文/英文双语文档（README 至少含英文或双语）

## 提交方式

1. Fork [akullpp/awesome-java](https://github.com/akullpp/awesome-java)；
2. 按分类在 README 对应位置插入条目（保持字母序）；
3. 提交 PR，附上"项目已发布 Maven Central + CI 全绿 + 文档完善"的证据；
4. 若被要求补充信息，按维护者意见修改。
