# PRD · SettingUtil：分层配置读取（批 3 / P1 · v1.2.0）

> 状态：已确认（产品经理 → 架构师 → 工程师）
> 所属迭代：v1.2.0 = 批 3 sure-log + SettingUtil 分层配置 + 批 4 sure-db
> 团队：研发小组五步闭环 · 第 1/2 步产物

## 1. 目标与范围

### 1.1 背景

对标 Hutool `Setting`（配置文件 + 分层配置源）。工具库与小型应用常需读取 `key=value` 配置，并希望：**系统属性 > 环境变量 > 配置文件 > 默认值** 的分层覆盖语义，避免硬编码。

### 1.2 目标

- 在 `sure-core` 新增 `com.sure.tool.config.SettingUtil`：解析 UTF-8 `key=value` 配置文件（`.setting` / `.properties` 风格）。
- 分层查找：`系统属性（-Dkey）→ 环境变量（key 点转下划线 + 大写）→ 配置文件 → 默认值`。
- 类型转换便捷方法：`getInt/getLong/getDouble/getBoolean/getString`。
- 线程安全、可重复 `load` 覆盖、`keys()` 枚举。

### 1.3 不做（边界）

- 不做 yaml/json/toml 解析（P2 候选：sure-json 联动）。
- 不做 profile/多环境选择、变量插值（`${}`）——分层语义已覆盖绝大多数场景。
- 不做自动热重载（`autoReload` 留 P2）。

## 2. 用户场景

1. `SettingUtil.load("app.setting")` 读配置文件，`getInt("server.port", 8080)`。
2. 部署时用 `-Dserver.port=9090` 或环境变量 `SERVER_PORT=9090` 覆盖文件值，无需改文件。
3. 无文件时全部走默认值，库不抛异常。

## 3. 验收标准（门禁）

| # | 验收项 | 验证方式 |
| --- | --- | --- |
| B1 | 解析 `key=value` 文件：跳过 `#` 注释与空行，保留行内值首尾空白裁剪 | JUnit |
| B2 | 分层优先级：系统属性 > 环境变量 > 文件 > 默认值（逐层验证） | JUnit（临时文件 + 注入系统属性/环境变量） |
| B3 | `getInt/getLong/getDouble/getBoolean` 正确转换；非法值抛清晰异常 | JUnit |
| B4 | 缺失 key 返回默认值；无默认返回 `null` | JUnit |
| B5 | 重复 `load` 覆盖旧值；`keys()` 返回当前全部 key | JUnit |
| B6 | 线程安全：并发 `load` + `get` 不抛错、不丢值（用不可变快照） | JUnit：10 线程 |
| B7 | 门禁：sure-core 覆盖率 ≥ 0.70 不降、Checkstyle 0、SpotBugs 0 | `mvn -B verify` |
| B8 | CHANGELOG / README 工具表同步 | 仓库一致性检查 |

## 4. 架构评审结论（软件架构师）

- **模块归属**：`sure-core` 新包 `com.sure.tool.config`（零依赖域内）。
- **API 形状**（`XxxUtil` + 私有构造 + `@since 1.2.0`）：
  - `load(String path)` / `load(File)` / `load(InputStream)` / `load(URL)`：解析并**整体替换**当前配置快照
  - `getString(key)` / `getString(key, default)` / `getInt/getLong/getDouble/getBoolean`
  - `get(key)`（Object）、`keys()`（Set）、`contains(key)`、`clear()`
  - `getCurrent()`（内部快照，不可变 Map）
- **分层查找实现**（`resolve(key)`）：
  1. `System.getProperty(key)` 命中即返；
  2. 环境变量：`key.replace('.', '_').toUpperCase()` 查 `System.getenv`；
  3. 文件快照 Map；4. 默认值。
- **状态模型**：`volatile Map<String,String> snapshot` + `load` 构造新 Map 后原子替换（Copy-on-Write），保证并发读安全。
- **解析规则**：按 `=` 首次切分；`#` 开头整行注释；空行跳过；UTF-8 读取。

## 5. 风险与回退

- P2：大文件性能（每 get 一次遍历三层）——仅文件快照为 Map，O(1)。
- 环境变量命名规则（点转下划线）为既定约定，写入 Javadoc 说明。
