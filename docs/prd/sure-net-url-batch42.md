# 批 42 · 网络工具 IP/URL 增强（PRD）

> 阶段 A（v1.19 → v2.0）第四批次，对标 Hutool NetUtil/URLUtil 高频方法 + 每方法示例文档化。
> 范围：`sure-core`。原则：JavaDoc 每方法含「示例：」代码片段 + 中文说明；jacoco 0.98 门禁不降。

## 一、背景

- `NetUtil` 9 方法（IPv4 判断/转换/本地 IP），缺 IPv6、CIDR 范围判断、多网卡地址、可用端口分配。
- `UrlUtil` 16 方法（编解码/参数/主机/协议），缺 HTTPS 判断、URL 格式校验、URL 对象转换、域名提取、无协议补全。

## 二、新增方法清单（10 个，全部含 JavaDoc 示例）

### 2.1 `NetUtil`（+5）

| 方法 | 语义 | 对标 |
|---|---|---|
| `isIpv6(String)` | 是否为合法 IPv6 地址 | hutool NetUtil.isIpv6 |
| `isIp(String)` | 是否为合法 IPv4 或 IPv6 | hutool NetUtil.isIp |
| `isInCidr(String, String)` | IPv4 是否在 CIDR 网段内（如 192.168.1.5 / 192.168.1.0/24） | hutool / 网络库 |
| `localIpv4s()` | 本机全部 IPv4 地址列表（含回环） | hutool NetUtil.localIpv4s |
| `getUsableLocalPort()` | 随机分配一个本机可用端口（0-65535 探测） | hutool NetUtil.getUsableLocalPort |

### 2.2 `UrlUtil`（+5）

| 方法 | 语义 | 对标 |
|---|---|---|
| `isHttps(String)` | 是否为 https 协议 | hutool URLUtil.isHttps |
| `isValidUrl(String)` | 是否为合法 http/https URL（格式校验） | hutool URLUtil |
| `toUrl(String)` | 转为 {@link java.net.URL}，非法 URL 抛运行时异常 | hutool URLUtil.url |
| `getDomain(String)` | 提取主机域名（去端口/路径/协议） | hutool URLUtil.getDomain |
| `toHttpUrl(String)` | 无协议时补全 {@code http://} | hutool URLUtil.toHttpUrl |

## 三、质量门禁

- 每个方法 ≥1 测试（合法/非法/null/边界），JavaDoc 示例与实现一致。
- `mvn -pl sure-core verify`：行覆盖率 ≥0.98、checkstyle / SpotBugs / license 全绿。
- CIDR 判断用已知网段向量验证（/8 /16 /24 /32 及越界）。

## 四、交付物

1. `NetUtil` / `UrlUtil` 增强（JavaDoc 示例文档化）。
2. 新增测试：`NetUtilBatch42Test` / `UrlUtilBatch42Test`。
3. `CHANGELOG.md` 追加 [Unreleased] 条目。
4. 提交并推送 main。
