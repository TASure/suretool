# 批32 PRD：Awesome Java + 教程（v4.0 生态治理收官）

- 状态：已批准
- 目标版本：1.13.0（影响力内容，无 API 变更）

## 背景

v4.0 计划表最后一棒：影响力收尾。批9b 的 awesome-java PR 已随 vinta/awesome-java 仓库消失（404），重新提交至活跃维护版 **akullpp/awesome-java**；批26/27 新 API（Result/Option/NullUtil）缺示例与教程。

## 交付内容

| 项 | 动作 |
|---|---|
| awesome-java PR | **akullpp/awesome-java PR #1350**：fork→feature/suretool 分支→Utility 分类插入条目（spring-chain 后、Underscore 前，17→18 projects）+ CONTRIBUTING 合规说明（独特差异：JDK25-first/零依赖/28 模块/虚拟线程一等公民/benchmark 对齐） |
| LangDemo | sure-examples 新增 Result/Option/NullUtil/Results.allOf 综合示例（编译通过） |
| lang-api-guide.md | 新教程：无异常显式错误门面（30 秒上手 + 对比表） |
| docs/index.md | 教程区注册新条目；docs-links 校验 63 个文件通过 |

## 验收标准

- PR #1350 已创建（TASure fork 分支 feature/suretool）
- LangDemo 编译通过、示例链接 docs 校验通过
