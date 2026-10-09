# 批33 PRD：覆盖率 98% 攻坚 A（sure-core/sure-json/sure-http/sure-crypto/sure-db）

- 状态：已批准
- 目标版本：1.13.1-SNAPSHOT（仅测试与门禁变更，无 API 变更）

## 背景

全库业务模块 jacoco 行覆盖率门禁整体提升至 ≥0.98。本片覆盖五大缺口模块：sure-core 701 行未覆盖（全库最大）、sure-json 188 行、sure-http 82 行、sure-crypto 83 行、sure-db 102 行（基线最低之一）。

## 交付内容

| 模块 | 基线 | 实测 | 门禁 | 补测文件（新增） | 新增行数 |
|---|---|---|---|---|---|
| sure-core | 90.51% | **98.01%** | 0.98 | 50 个 *GapTest 类（StrUtil/ReflectUtil/CollUtil/BeanUtil/Props/ThreadUtil/NetUtil/AsyncUtil/FileUtil/DateUtil/SettingUtil/SeqUtil 等全覆盖） | ~4213 |
| sure-json | 83.95% | **98.12%** | 0.98 | P6CoverageGapTest | 504 |
| sure-http | 87.82% | **98.81%** | 0.98 | P7HttpCoverageTest（本地 HttpServer + 未监听端口 + 中断注入） | 476 |
| sure-crypto | 87.39% | **98.02%** | 0.98 | CoverageBoostTest（JCA 边界 + removeProvider 算法不可用） | 439 |
| sure-db | 73.30% | **98.43%** | 0.98 | DbCoverageTest（HSQLDB 内存库 + 抛错 Connection 代理） | 565 |

攻坚要点：IO 异常分支用抛错流/只读目录；加密用非法密钥/填充模式/算法不可用（Security.removeProvider 后 finally 还原）；网络用 localhost 临时服务与未监听端口；并发用虚拟线程 + 预中断 + StructuredTaskScope 取消；sure-core 增补 jqwik 属性测试兼容 JUnit4 双框架。

## 验收标准

- 5 模块 jacoco 行覆盖率实测 ≥0.98，checkstyle 0 违规，既有用例未删
- sure-core 剩余 147 行未覆盖均为环境依赖（网卡枚举）/不可达防御分支，已逐类记录
