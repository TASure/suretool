# PRD：sure-db 模块（批 4 / v1.2.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.2.0（与批 3 sure-log 合并发布）
- 对标：Hutool Db（`cn.hutool.db`）
- 硬约束：仅 JDK21+；核心模块零第三方**运行期**依赖（HSQLDB 仅 test scope）；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG。

## 1. 背景与目标

suretool 已覆盖 util/codec/collection/json/http/crypto/cache/xml/poi/log 等域，缺"数据库访问"这一 Java 生态高频刚需。Hutool Db 以「Entity + Db 门面 + SqlRunner」极简风格闻名：不引入 ORM，用 Map 风格实体 + 手写 SQL + 参数绑定完成 90% 的日常 CRUD。

目标：提供**零第三方运行期依赖**的 JDBC 数据访问层（javax.sql/java.sql 为 JDK 自带），API 对齐 Hutool Db 常用面，内置最小连接池，支持事务与分页，覆盖小型项目/工具类场景。

## 2. 模块信息

- 模块：`sure-db`，坐标 `io.github.tasure:sure-db`，包 `com.sure.tool.db`
- 运行期依赖：**无第三方**（仅 JDK java.sql/javax.sql）
- 测试依赖：`org.hsqldb:hsqldb:2.7.4`（test scope，内存库）
- module-info：`module sure.db { requires transitive sure.core; requires transitive java.sql; }`
  - 说明：sure-core 提供断言/异常/集合工具；java.sql 作为模块必需导出。

## 3. API 设计

### 3.1 Entity（核心实体，对标 hutool Entity）

`Entity extends LinkedHashMap<String, Object>`，带表名：

- `static Entity create(String tableName)` / `create()`；`getTableName()`
- `Entity set(String key, Object value)`（链式，put 别名）；`setIgnoreNull(...)`
- 类型化取值：`getStr / getInt / getLong / getDouble / getBigDecimal / getDate / getBytes / getBool`
- 主键辅助：`setPk`？——不引入，insert 由 SqlRunner 取 generatedKeys

### 3.2 SqlRunner（底层执行器）

- `SqlRunner(DataSource ds)`（校验非空）
- `List<Entity> query(String sql, Object... params)`
- `Entity queryOne(String sql, Object... params)`（多行取第一行）
- `Number queryNumber(String sql, Object... params)`
- `long queryCount(String sql, Object... params)`（`SELECT COUNT(*)` 语义）
- `int execute(String sql, Object... params)`（UPDATE/DELETE/DDL）
- `int insert(Entity entity)`（自动拼 INSERT，返回影响行数）+ `Object insertReturnKey(Entity)`（generatedKeys）
- `int update(Entity entity, Entity where)`（按 where 字段拼 UPDATE）与 `int update(String sql, Object... params)`
- `int del(Entity entity, Entity where)` 与 `int del(String sql, Object... params)`
- `int page(...)` 见 PageResult
- `void transaction(SqlConsumer<SqlRunner>)`（自动 COMMIT/ROLLBACK，异常回滚并抛 DbRuntimeException）
- 参数绑定：`setObject`，null 用 `setNull`；SQL 统一 PreparedStatement（防注入）

### 3.3 Db（门面，对标 hutool Db）

- `static Db use()`（用 DbUtil 默认数据源，未设置抛异常）
- `static Db use(DataSource ds)`；`static Db use(String driver, String url, String user, String pass)`
- 实例方法委托 SqlRunner：query / queryOne / queryNumber / queryCount / execute / insert / insertReturnKey / update / del / page / transaction / tx(alias)

### 3.4 DbUtil（静态门面）

- `static Db use()` / `use(DataSource)`；`use(String driver, String url, String user, String pass)`
- `static void setDefaultDataSource(DataSource)`（全局默认）
- `static SqlRunner newSqlRunner(DataSource)`

### 3.5 SimpleDataSource（内置零依赖最小连接池）

- 构造：`new SimpleDataSource(String url, String user, String pass)` 或 driver 版本；可选 `setMaxSize(int)`
- 实现 `javax.sql.DataSource`：`getConnection()` 从池借用（空闲不足新建，上限 maxSize 后等待，默认超时 10s 抛异常）
- `close()` 关闭池；实现 AutoCloseable
- 池内连接 `isValid(3)` 校验；归还时回收坏连接

### 3.6 PageResult（分页结果）

- `int pageNo / pageSize / long total`，`List<Entity> list`
- `int getPageCount()`（`ceil(total/pageSize)`）
- `static PageResult of(int pageNo, int pageSize, long total, List<Entity> list)`

### 3.7 DbRuntimeException

- `extends RuntimeException`，包装 SQLException 根因；`getCause()` 保留 SQLException
- 提供 `of(SQLException)` 便捷工厂

## 4. 验收标准（A 系列）

| 编号 | 验收点 |
|---|---|
| A1 | sure-db 运行期零第三方依赖（pom 仅 test scope 有 hsqldb） |
| A2 | Entity 支持链式 set / 表名 / 类型化取值（str/int/long/double/date/bool） |
| A3 | SqlRunner.query 返回 List<Entity>，queryOne 取首行，参数绑定防注入（含 null 参数） |
| A4 | insert 支持自动生成 INSERT 并返回 generatedKeys 主键（HSQLDB IDENTITY 验证） |
| A5 | update/del 支持 (entity, whereEntity) 与 (sql, params) 两种形态 |
| A6 | transaction 回调内异常自动回滚，提交成功持久化（HSQLDB 验证回滚） |
| A7 | SimpleDataSource 池化：复用连接、超上限等待、close 释放（测试用 Connection 计数校验） |
| A8 | Db.use() 默认数据源 + DbUtil 全局默认 + Db.use(ds) 显式数据源三种获取路径 |
| A9 | 全量 verify BUILD SUCCESS；sure-core 覆盖率 ≥0.70 不降；javadoc 0 error |
| A10 | 新模块同步 root/sure-all/sure-bom/README/CHANGELOG；docs/prd/sure-db.md 落库 |

## 5. 架构评审结论

1. **零依赖策略**：连接池/参数绑定/ResultSet 映射全部手写，避免引入 HikariCP/Commons-DBCP（符合 suretool 核心价值主张）；HSQLDB 仅在测试用内存库。
2. **API 形态**：Entity（LinkedHashMap 子类）保证与 Map 生态互通；SqlRunner 无状态（每次操作新开/借用连接并自动归还），天然线程安全，Db 门面无状态可复用。
3. **事务语义**：transaction 回调内使用**单连接**（从池借用一次贯穿回调），保证 ACID；外层嵌套事务不做传播（简化，明确 Javadoc 说明）。
4. **连接池取舍**：SimpleDataSource 提供"最小可用"池（借用/归还/校验/上限等待），不做 FIFO 公平、不做 idle 回收（文档明示）；重负载场景建议用户替换为连接池 DataSource。
5. **安全**：全程 PreparedStatement 参数绑定；SQL 由调用方书写（与 Hutool 一致），Entity 字段名/表名拼接处做反引号无必要——保持与 Hutool 相同约定并 Javadoc 提示。

## 6. 里程碑

- 实现 + 测试 + 集成 → 全量 verify → commit+push → 并入 v1.2.0 发布链（批 3 + 批 4 合并发布）。
