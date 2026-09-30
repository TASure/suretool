# PRD：影响力内容（教程 + awesome-java 提交 + 示例覆盖）

- 状态：Accepted（2026-09-30）｜ 版本：1.5.0

## A. 目标
让新增模块「可被搜索、可被学会、可被引用」：
1. 教程：docs/posts 新增批 7/批 8 实战教程（延续 README 教程区风格）
2. awesome-java：产出可直接提交的条目内容与提交说明
3. 示例：sure-examples 补齐批 7/批 8 Demo 并挂入 ExamplesRunner

## B. 交付物
### B1. docs/posts/math-process-compress-guide.md
sure-math / sure-process / sure-compress 速查教程（各工具 2-3 个可运行代码块 + 典型场景）
### B2. docs/posts/socket-event-guide.md
sure-socket（echo 服务 + 客户端）+ sure-event（事件驱动示例）完整教程
### B3. docs/awesome-java-submission.md
awesome-java 提交条目（Libraries→Utilities 分类）：项目名/一句话描述/主页/许可证，+ PR 提交说明与 checklist
### B4. sure-examples 新增 5 个 Demo 并注册 ExamplesRunner
MathDemo / ProcessDemo / CompressDemo / SocketDemo / EventBusDemo
### B5. README 教程区更新（新增 2 篇链接）、示例数 14 → 19

## C. 验收
- A1 教程每篇 ≥3 个可直接运行代码块，命令可复现
- A2 awesome-java 条目含 Maven 坐标、仓库、许可证、分类，可直接粘贴提交
- A3 ExamplesRunner 能列出全部 19 个示例且新 Demo 编译通过
- A4 README 链接均可达（docs/posts/*.md 存在）

## D. 非目标
- 不提交真实 PR 到 awesome-java（提供内容与流程，由用户确认后提交）
- 不建独立文档站点（沿用 GitHub Pages 现有 docs 结构）
