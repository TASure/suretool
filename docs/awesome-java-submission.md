# awesome-java 提交内容（Libraries → Utilities）

> 目标仓库：https://github.com/akullpp/awesome-java
> 状态：**内容就绪，待用户确认后手动提交 PR**（不代提交）

## 1. 待提交条目

在 `README.md` 的 **Libraries → Utilities** 分类追加：

```markdown
- [SureTool](https://github.com/TASure/suretool) - 模块化 Java 工具库，面向 JDK 21+，提供集合、字符串、日期、JSON、加密、Office、进程、数学、压缩、Socket、事件总线等 15+ 工具域，零第三方运行时依赖（sure-core 及其子模块），Apache-2.0
```

- **分类**：Libraries → Utilities（如列表按字母序，插入 `S` 位置）
- **链接**：https://github.com/TASure/suretool
- **许可证**：Apache License 2.0（仓库 LICENSE 文件已存在）
- **主题标签**（GitHub topics 已配置）：`java` `java-library` `toolkit` `utilities` `jdk21` `maven`

## 2. 提交流程（用户操作）

1. Fork https://github.com/akullpp/awesome-java
2. 新建分支 `feat/add-suretool`
3. 编辑 `README.md` Utilities 段，粘贴上面条目
4. Commit：`Add SureTool to Utilities`
5. 发起 PR，标题：`Add SureTool (Java utility library)`

## 3. PR 提交说明（模板）

```
## What does this PR do?

Adds [SureTool](https://github.com/TASure/suretool), a modular Java utility
library for JDK 21+ (collection, string, date, JSON, crypto, office, process,
math, compression, socket, event-bus and more). Core modules have zero
third-party runtime dependencies; licensed under Apache-2.0.

## Checklist

- [x] The project is actively maintained (latest release v1.4.0, 2026-09)
- [x] The link points to the official repository
- [x] The project has documentation (docs/, Javadoc, GitHub Pages)
- [x] License file is present (Apache-2.0)
```

## 4. 维护性检查清单（提交前自查）

- [ ] README 首页含中文/英文双语简介与模块表
- [ ] GitHub Pages 文档站 https://tasure.github.io/suretool/ 可访问
- [ ] 最新 Release v1.4.0 已发布到 Maven Central（`io.github.tasure:*`）
- [ ] CI 全绿（build/benchmark/security 三类 workflow）
