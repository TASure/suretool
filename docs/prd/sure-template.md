# PRD：sure-template 模块（批 5 / v1.3.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.3.0（与 sure-script 同批发布）
- 对标：Hutool hutool-template（`cn.hutool.template`，SimpleTemplate / TemplateUtil）
- 硬约束：仅 JDK25+；核心模块零第三方**运行期**依赖；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG。

## 1. 背景与目标

模板渲染是日志模板、配置模板、邮件/通知文案、代码生成等场景的高频刚需。Hutool 的 `hutool-template` 模块提供与具体模板引擎解耦的 `Template` 接口 + 内置 `SimpleTemplate`（Freemarker 风格 `${name}` 占位符），可无缝替换为 Velocity/Freemarker 等引擎。

目标：提供**零第三方运行期依赖**的模板引擎门面：`Template` 接口（引擎可替换）+ 内置 `SimpleTemplate`（${} 占位符 + 转义 + Bean 渲染），覆盖轻量渲染场景。

## 2. 模块信息

- 模块：`sure-template`，坐标 `io.github.tasure:sure-template`，包 `com.sure.tool.template`
- 运行期依赖：`sure-core`（Bean 取值）
- 测试依赖：无第三方
- module-info：`module sure.template { requires transitive sure.core; exports com.sure.tool.template; }`

## 3. API 设计

### 3.1 Template（接口，对标 hutool Template）

- `String render(Map<String, ?> data)`：按数据渲染
- `String render(Object bean)`：Bean 属性渲染（经 sure-core BeanUtil 取值）

### 3.2 SimpleTemplate（内置实现，对标 hutool SimpleTemplate）

- 构造：`SimpleTemplate(String templateText)`（模板文本，非空校验）
- 占位符：`${name}`；`name` 支持点号层级（`${user.name}` 经 Map 嵌套或 Bean 属性）
- 转义：`\${` 渲染为字面 `${`
- 空值语义：值为 null → 渲染为空串；**未知变量保留占位符原样**（便于发现模板拼写错误，与 Hutool 语义一致）
- 线程安全：模板解析结果为内部结构，render 不修改状态 → 实例可共享

### 3.3 TemplateUtil（静态门面，对标 hutool TemplateUtil）

- `static Template createTemplate(String templateText)`：创建模板
- `static String render(String templateText, Map<String, ?> data)`：一行式渲染
- `static String render(String templateText, Object bean)`：Bean 渲染

### 3.4 解析器实现

单遍扫描模板文本：遇 `\${` 输出 `${` 并跳过；遇 `${` 找闭合 `}`，截取 key，查值（Map 直取；点号路径递归；Bean 反射）；无 `}` 视为普通文本。解析结果缓存在模板实例内（只解析一次）。

## 4. 验收标准

- A1 `${name}` 基础替换正确；重复占位符全部替换
- A2 `\${name}` 转义输出字面 `${name}` 且不参与替换
- A3 未知变量保留 `${name}` 原样；null 值渲染为空串
- A4 点号路径 `${user.name}`：Map 嵌套 / Bean 嵌套均正确
- A5 Bean 渲染：`template.render(bean)` 按属性名取值
- A6 边界：空模板、纯文本无占位符、`${}` 空 key、未闭合 `${`、key 含特殊字符（中文/下划线/数字）
- A7 模板实例可复用（同一模板多次 render 结果一致）
- A8 全量 verify 门禁：Checkstyle 0、SpotBugs 0、javadoc 0 error、sure-core 覆盖率不降
- A9 模块注册同步：root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG

## 5. 架构评审结论

- **零依赖可行性**：占位符渲染为纯字符串处理，无外部依赖——通过。
- **引擎可替换**：`Template` 接口 + `TemplateUtil.createTemplate` 工厂，未来可扩展 `FreemarkerTemplate`/`VelocityTemplate` 适配器，接口不变——通过。
- **线程安全**：渲染只读解析结果（List 片断 + key 序列），无共享可变状态——通过。
- **语义对齐**：未知变量保留原样、null 渲染空串，与 Hutool SimpleTemplate 一致，降低迁移成本。
- **命名与包**：`com.sure.tool.template`；TemplateUtil 私有构造器 + @since 1.3.0。
