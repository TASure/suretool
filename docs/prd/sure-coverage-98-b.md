# 批33 PRD：覆盖率 98% 攻坚 B（sure-log/sure-socket/sure-compress/sure-poi/sure-pdf/sure-process/sure-script）

- 状态：已批准
- 目标版本：1.13.1-SNAPSHOT（仅测试与门禁变更，无 API 变更）

## 背景

本片覆盖 7 个低覆盖或无门禁模块，其中 sure-log（70.49%）、sure-socket（72.02%）、sure-pdf（77.73%）、sure-process（76.92%）、sure-script（77.08%）为全库缺口最大的第二梯队。

## 交付内容

| 模块 | 基线 | 实测 | 门禁 | 补测文件 | 新增行数 |
|---|---|---|---|---|---|
| sure-log | 70.49% | **98.36%** | 0.98 | LogCoverageTest（SLF4J 委托 + Console 兜底 + AbstractLog 级别/占位符全分支） | 254 |
| sure-socket | 72.02% | **97.93%** | 0.97（例外） | SocketCoverageTest（本地端口 + 连接拒绝 + 反射注入 acceptLoop） | ~213 |
| sure-compress | 82.57% | **100.00%** | 0.98 | CompressCoverageTest（压缩解压往返 + 损坏流） | 189 |
| sure-poi | 87.03% | **98.33%** | 0.98 | PoiExtraTest（xlsx/docx 往返 + 公式/日期/错误类型 + 损坏输入） | 243 |
| sure-pdf | 77.73% | **98.10%** | 0.98 | PdfExtraTest（PDFBox 往返 + 抛错流/反射关闭内部流/非空目录删除） | 338 |
| sure-process | 76.92% | **98.90%** | 0.98 | ProcessUtilExtraTest（本地命令 + 超时/中断 + carrier 占满法） | 143 |
| sure-script | 77.08% | **100.00%** | 0.98 | ScriptUtilExtraTest + 测试引擎 SPI（JsTestEngine/BrokenTestEngine 等） | ~150 |

## sure-socket 例外说明（门禁 0.97）

实测 189/193 = 97.93%，为本环境物理上限。剩余 4 行均位于 SocketUtil.getLocalHost 的网卡枚举分支：L122（continue）需一张「非 loopback 且 down」的网卡；L129（site-local 返回）需 10.x/172.16-31.x/192.168.x 网段地址；L136-137（catch IOException）需 getNetworkInterfaces 抛 IO 异常——本 VM 三张网卡全部 up、无 site-local 网段、native 枚举无故障注入点，测试无法构造。其余 189 行全部覆盖（含 getRemoteAddress 经 Socket 子类注入 null 远端）。已用 javap 字节码级复核。

## 验收标准

- 7 模块 jacoco 行覆盖率：6 个 ≥0.98，sure-socket = 97.93%（门禁 0.97）
- checkstyle 0 违规，既有用例未删；sure-script 的 SPI 修改仅限 test scope
