# 批28 PRD：JPMS 治理收官（module-info 完整性 + 自动模块名 + 一致性审计）

- 状态：已批准
- 目标版本：1.12.0（模块化声明变更）
- 范围：21 个已有 module-info 的模块 + 4 个缺声明的模块 + 审计脚本

## 背景

21 模块已有 module-info（模块名 `sure.<artifactId>` 统一）。治理缺口：
① sure-core 漏导出 `com.sure.tool.config`（既有包）与 `com.sure.tool.graph`（批23 新增），
   且全库 0 opens —— BeanUtil/ConvertUtil 反射在 module path 下受限（Hutool 等主流库均 opens 反射包）；
② sure-extra / sure-benchmark / sure-examples / sure-spring-boot-starter 有源码但无模块化声明
   （sure-extra 因第三方模块名不确定，采用 Automatic-Module-Name 兜底）。

## 迭代内容

1. **sure-core module-info**：exports 补 config/graph；新增 opens 全部 15 包（反射可用，
   对齐 Hutool opens 实践；保证 module path 下 BeanUtil/JSON 互转可用）
2. **4 模块 Automatic-Module-Name**：sure-extra→sure.extra、sure-benchmark→sure.benchmark、
   sure-examples→sure.example、sure-spring-boot-starter→sure.boot（maven-jar-plugin 配置）
3. **一致性审计脚本** `.github/scripts/check_moduleinfo.py`：
   - 有 module-info：exports 覆盖实际包（可含 opens 检查）
   - 有源码无 module-info：必须声明 Automatic-Module-Name
   - 模块名前缀 `sure.` 一致性
4. **docs/modules.md**：新增 JPMS 状态列（模块化 / 自动模块名 / 未模块化）

## 验收标准

- 审计脚本本地跑全绿（含修复后 sure-core）
- sure-core module-info 编译通过；全量 `mvn -am verify` 各模块 BUILD SUCCESS
- 不破坏既有模块名（sure.core 等保持不变，避免 API 破坏）
- 零新增第三方依赖
