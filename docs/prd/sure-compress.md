# PRD：sure-compress 压缩模块（7z / Brotli）

| 项 | 内容 |
|---|---|
| 版本 | v1.4.0（批 7） |
| 模块 | sure-compress |
| 包 | com.sure.tool.compress |
| 依赖 | sure-core + commons-compress 1.27.1 + xz 1.10 + brotli dec/enc 0.1.2 |
| 许可 | Apache-2.0（commons-compress）/ 公域（xz）/ MIT（brotli）——均兼容 |

## 背景与目标

补齐高压缩率格式门面：7z（LZMA2）与 Brotli。Zip/Gzip 已在 sure-core（ZipUtil/GzipUtil）覆盖，本模块仅做 7z 与 Brotli。对标 Hutool-core 的 SevenZUtil/BrotliUtil。

## API 设计（私有构造器 + @since 1.4.0）

- `SevenZUtil`：
  - `compressDir(Path dir, Path out7z)`：目录整体压缩（LZMA2 默认压缩级别）
  - `decompress(Path in7z, Path outDir)`：解压到目标目录（自动建目录，防路径穿越：拒绝 `../` 条目）
  - `decompress(Path in7z)`：解压到当前目录
  - `isSevenZ(Path)`：魔数校验（`37 7A BC AF 27 1C`）
- `BrotliUtil`（设计调整：仅解码+检测，原因见下）：
  - `compress(byte[])` / `decompress(byte[])`（默认 quality 5）
  - `compress(Path src, Path dest)` / `decompress(Path src, Path dest)`
  - `decompress(InputStream, OutputStream)`

## 验收清单

- A1 目录 7z 压缩→解压→内容一致（含子目录与二进制文件）
- A2 空目录可压缩可解压
- A3 `isSevenZ` 正确识别 7z 与非 7z 文件
- A4 解压含 `../` 路径条目被拒绝（防路径穿越）
- A5 Brotli 合法流解码正确、空数组解压返回空（编码侧因生态约束裁减，见"设计约束"）
- A6 Brotli 文件解码→内容一致；魔数检测可识别 Brotli 且排除 gzip/zip/PNG
- A7 非法输入校验（null/不存在文件抛 IllegalArgumentException）
- A8 覆盖率 ≥ 70%（模块门禁）

## 架构评审

- 7z 用 Apache commons-compress（Apache-2.0，Hutool 同源方案），依赖显式声明 xz（commons-compress 的 optional 依赖）。
- Brotli 解码用官方 org.brotli:dec（纯 Java、Apache-2.0/MIT）。
- 【设计约束】Brotli 编码侧不提供：官方 Java 仅发布解码器（org.brotli:dec 无 encoder）；brotli4j 等编码方案强制依赖各平台 native 库，与 suretool"零原生依赖"原则冲突。故 `BrotliUtil` 定位为 Brotli 流解码 + 魔数检测（HTTP `Content-Encoding: br` 响应解码、.br 资源解包）。
- 防路径穿越是安全硬要求：解压时校验条目路径规范化后仍在目标目录内。
- SpotBugs 惯例：`@SuppressFBWarnings` + `requires static com.github.spotbugs.annotations`。
