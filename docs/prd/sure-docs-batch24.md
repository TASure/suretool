# 批24 PRD：双语文档门户（模块-API 门户 + 双语同步 + 链接治理）

- 状态：已批准
- 目标版本：1.11.0（文档随行）
- 涉及：docs/、README.md、README.en.md、.github/workflows/ci.yml

## 背景

README 已有快速开始、28 模块表、7 篇教程（docs/posts/）；Pages 站点（Jekyll + 聚合 javadoc /api）已上线。
**缺口**：① 无 javadoc.io / Maven Central / 源码三链接合一的"模块-API 门户"；② 文档断链无自动校验；③ README.en 未同步 1.11.0 新能力（批23）。

## 迭代内容

### 1. docs/modules.md：模块-API 门户（双语结构）
- 28 模块全表：坐标 / 主包 / 职责 / 对标对象 / 关键类
- 每模块 javadoc.io 直链（javadoc.io/doc/io.github.tasure/<module>/latest/）+ Maven Central + 源码链接
- 中文为主 + 英文模块名/关键词双语（降低国际开发者门槛）

### 2. 导航整合
- docs/index.md：新增"模块与 API 门户"入口
- README.md 文档区：新增 modules.md 链接

### 3. README.en.md 双语同步
- 补批23 新能力：DiGraph/GraphUtil/StatUtil/EventBus.DeadEvent
- 同步新增模块-API 门户链接

### 4. 文档链接 CI 校验（治理闭环）
- `.github/scripts/check_docs_links.py`：校验 README.md/README.en.md/docs/**/*.md 相对链接存在性 + 内部锚点
- 挂入 ci.yml（文档变更即校验，防断链）

## 验收标准

- docs/modules.md 覆盖全部 28 模块，链接格式正确
- 脚本校验通过（0 断链/坏锚点），本批文档全部可解析
- README.en 无过时内容（批23 能力已同步）
- 本地跑 check_docs_links.py 全绿；mvn 门禁不回归（无 Java 代码变更）
