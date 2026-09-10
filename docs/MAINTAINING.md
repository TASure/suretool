# 维护者手册（MAINTAINING）

面向 suretool 维护者与核心贡献者的日常工作手册。

## 1. 角色与职责

- **维护者**：拥有 merge / release / issue 管理权限；对变更安全性与质量负最终责任。
- **贡献者**：通过 PR 提交变更；需通过全部质量门禁。

## 2. 分支与提交规范

- 主干分支：`main`（始终可构建、可发布）。
- 功能分支：`feat/<简述>`；修复分支：`fix/<简述>`。
- 提交信息遵循 Conventional Commits：

```
feat(core): 新增 StrUtil 的 xxx 方法
fix(json): 修复嵌套对象反序列化溢出
docs(readme): 更新模块列表
build(ci): 升级 setup-java 到 v5
```

- 合并方式：Squash and Merge（保持 main 历史线性）。

## 3. 质量门禁（合入前必须全绿）

| 门禁 | 命令/检查 | 失败处理 |
| --- | --- | --- |
| 编译 | `mvn -q clean verify` | 修复后重跑 |
| Checkstyle | 0 违规（含测试源码） | 按违规项修正 |
| SpotBugs | Max / Medium，无新增告警 | 确认为误报需在 exclude-filter.xml 注明理由 |
| JaCoCo | 各模块行覆盖率 ≥ 85%（业务模块） | 补充测试，禁止无理由降门槛 |
| License | 所有 `*.java` / `pom.xml` 带 Apache-2.0 头 | `mvn license:format` 或手动补齐 |
| CodeQL | 无新增告警（PR 检查） | 修复或给出安全论证 |
| Dependabot | 定期合入依赖升级 PR | 关注 breaking change |

## 4. 评审清单（Review Checklist）

- [ ] API 是否向后兼容（新增方法不破坏既有签名）
- [ ] 是否有空值/边界/异常路径处理
- [ ] 是否引入新依赖（默认零依赖原则，需在 PR 说明理由）
- [ ] 并发安全性：共享状态是否有同步/不可变设计
- [ ] 安全默认值：加密类不使用弱算法；工具方法不做危险默认
- [ ] 是否添加对应单元测试并达覆盖率要求
- [ ] JavaDoc 是否完整（public API 必须有 @param/@return）

## 5. Issue 处理流程

1. 新 Issue 打标签：`bug` / `feature` / `question` / `security`；
2. `security` 标签的 Issue **立即处理**（见 SECURITY.md 响应承诺），不在公开区泄露细节；
3. Bug 复现后先写失败测试再修复（TDD）；
4. 24h 内回应的目标：所有新 Issue 至少获得一次维护者回复。

## 6. 发布流程

见 [RELEASING.md](RELEASING.md)：

1. `CHANGELOG.md` 收尾；
2. `-Prelease clean deploy` 上传 OSSRH；
3. 关闭并发布暂存仓库；
4. 打 Git 标签 + GitHub Release。

## 7. 社区运营

- 每周合并依赖升级与低风险 PR；
- 每月审视 CodeQL / Dependabot / 覆盖率趋势；
- 在 GitHub Discussions 收集需求，重大问题进入 ROADMAP；
- 保持 README 徽章与实际状态一致。
