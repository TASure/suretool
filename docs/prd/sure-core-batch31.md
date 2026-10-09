# 批31 PRD：OSS 治理（v4.0 生态治理第三棒）

- 状态：已批准
- 目标版本：1.13.0（仓库治理，无 API 变更）

## 背景

v4.0 计划表批31：让仓库具备优秀开源项目的社区治理面——模板齐备、安全响应闭环、元数据可发现。

## 交付内容

| 项 | 动作 |
|---|---|
| SECURITY.md | 修正过时支持矩阵（0.1.x → 1.x 当前线）；安全修复策略明确 |
| ISSUE_TEMPLATE/config.yml | 禁止 blank issue，引导 Security Advisory / Discussions |
| stale.yml 工作流 | 90 天无活动标记 + 14 天关闭；security/bug/依赖标签豁免 |
| CONTRIBUTING.md | 新增 7.5 节「版本与 @since 规范」（未发布窗口标注法） |
| 仓库元数据 | description + 10 个 topics（java/jdk25/utilities/toolkit/对比项/零依赖） |
| docs-links 校验 | 61 个 Markdown 文件链接全通过（含锚点） |

既有治理资产（已核验存在）：Issue/PR 模板、CODE_OF_CONDUCT（贡献者公约）、Dependabot、CodeQL、osv-scanner、CI/Pages/Release/Benchmark 工作流、README 徽章全套。

## 验收标准

- 治理文件全部落盘并推送
- GitHub 仓库 description + topics 生效（API 已确认返回 10 项）
- docs-links 本地校验通过
