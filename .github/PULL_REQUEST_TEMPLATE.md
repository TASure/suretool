## 变更内容

<!-- 本次 PR 做了什么、为什么做，关联 issue 编号（如 Fixes #123） -->

## 变更类型

- [ ] feat: 新工具类 / 新方法
- [ ] fix: 缺陷修复
- [ ] docs: 文档
- [ ] test: 测试
- [ ] refactor: 重构
- [ ] build: 构建 / 依赖 / CI

## 影响范围

<!-- 受影响模块、是否改动公共 API、是否需要迁移（行为变更请写进 CHANGELOG Unreleased） -->

## 检查清单

- [ ] `mvn -B verify` 全绿（测试 + Checkstyle + SpotBugs + License 头 + JaCoCo 覆盖率）
- [ ] 新增/修改的 API 有 Javadoc，且带 `@since`（下一个未发布版本）
- [ ] 新增 public/protected 方法至少一个测试命中，含正常与边界路径
- [ ] 未删除、未禁用任何测试
- [ ] 核心模块无第三方运行期依赖；第三方库依赖标为 `optional`
- [ ] japicmp 二进制兼容检查通过（公共 API 变更需说明不兼容点）
- [ ] 模块有 `module-info` 时已补充 `exports`（如新增包）
- [ ] 涉及用户可见行为 / 模块清单时已同步更新 README（中英）与 docs

## 验证证据

<!-- 本地或 CI 关键输出：测试数、覆盖率、命中的命令；示例类改动请贴运行输出 -->
