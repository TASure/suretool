# PRD：sure-core 批14 · Props / 表达式引擎 / 兼容门禁（v1.9.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.9.0（sure-core 增量 + 工程可信度）
- 对标：Hutool `Props`（cn.hutool.setting）、commons-jexl / Aviator 的极简子集
- 硬约束：仅 JDK25+；核心模块零第三方**运行期**依赖；XxxUtil 命名 + 私有构造器 + 中文 Javadoc + Tab 缩进；每新增方法配套 JUnit 4.13.2 测试；门禁：checkstyle + SpotBugs(effort=Max) + jacoco line ≥ 0.70；japicmp 二进制兼容。

## 1. 背景与目标

v1.8.0 已具备 88 个核心类，但两个方向仍有缺口：

1. **配置读取**：`SettingUtil` 偏轻量，缺少对 `java.util.Properties` 的完整增强封装（Hutool `Props` 支持链式类型读取、转 Bean/Map、按相对路径加载）。业务项目中 `.properties` 是事实标准格式。
2. **表达式求值**：Hutool 无内置表达式引擎，主流方案（Aviator/MVEL/JEXL）均为第三方依赖。提供**零运行期依赖**的极简算术表达式求值器是差异化亮点，覆盖 90% 的"动态阈值/规则计算"场景。
3. **工程可信度**：缺少发布级二进制兼容门禁。引入 japicmp 在 CI 校验 main 与上一发布版本（v1.8.0）的 API 兼容性，防破坏性变更悄悄进入快照。

目标：以上三项落地，v1.9.0 发布。

## 2. 变更范围

全部在 **sure-core** 模块内（表达式引擎为新增类，不新增模块，保持"核心零依赖"卖点）。

| 类 | 新增方法/能力 | 说明 |
|---|---|---|
| `config.Props`（新类，对标 hutool Props） | `Props()` / `Props(String path)` / `Props(File)`、`load(String)` / `load(File)`、`getStr/getInt/getLong/getDouble/getBool/getObj/getBigDecimal`（含默认值重载）、`toBean(Class)`、`toMap()`、`getProperty(String)` | `java.util.Properties` 增强：相对 classpath 加载、类型安全读取、转 Bean/Map |
| `util.ExpressionUtil`（新类，零依赖） | `eval(String)`、`eval(String, Map<String,Object>)`、`evalNumber(String)`（返回 BigDecimal）、`check(String)` | 算术表达式求值：`+ - * / % ( )`、整数/小数、变量替换、一元负号、空格容错；不支持函数/逻辑/字符串 |
| `japicmp 门禁`（工程项） | pom 新增 japicmp-maven-plugin | `mvn verify` 时对比 `1.8.0` 与当前代码，二进制不兼容即失败；CI 全量门禁覆盖 |

## 3. API 设计要点

### 3.1 Props（config 包）

- `Props()` 空构造；`Props(String path)` 从 classpath 相对路径加载（找不到抛 `IllegalArgumentException` 并说明可用的 classpath 前缀）；`Props(File)` 从文件加载。
- 类型读取：`getInt(key, defaultValue)`、`getBool(key, false)` 等；解析失败返回默认值，不抛异常（与 hutool 一致）。
- `toBean(Class<T>)`：属性名 → Bean 属性（走 `BeanUtil.setProperty` 类型转换）。
- `toMap()`：返回 `Map<String, String>` 快照（与 hutool 一致）。

### 3.2 ExpressionUtil（util 包，零依赖）

- **文法**（LL(1) 递归下降）：
  - `expr := term (('+' | '-') term)*`
  - `term := factor (('*' | '/' | '%') factor)*`
  - `factor := ('-') factor | number | ident | '(' expr ')'`
  - `number := 整数或小数（支持科学计数法）`
  - `ident := [a-zA-Z_][a-zA-Z0-9_]*`（变量，替换为 BigDecimal；未提供变量抛 `IllegalArgumentException`）
- `eval(String)`：无变量，返回 `double`；`eval(String, Map<String,Object>)`：变量替换后求值；`evalNumber` 返回 `BigDecimal`（高精度）。
- 解析错误/除零：抛 `IllegalArgumentException`（带表达式片段与位置）。
- 分词器：跳过空白；数字、标识符、运算符、括号、负号。
- **不做**：函数、幂、逻辑比较、字符串、位运算（v2.x 再扩展）。

### 3.3 japicmp 门禁

- sure-core pom 新增 japicmp-maven-plugin（版本 0.22.x，仅 JDK25 可用版本）：
  - `<oldVersion>1.8.0</oldVersion>`（从中央仓库取），`<breakOnBinaryIncompatibleModifications>true</breakOnBinaryIncompatibleModifications>`
  - 绑定到 `verify` 阶段（与 spotbugs/checkstyle 同门禁）
- 效果：删除/改签名公共 API 时 CI 立即失败，保护下游引用方。

## 4. 测试要点（JUnit 4.13.2）

- Props：classpath 加载、默认值（含解析失败回退）、toBean 类型转换、toMap、文件不存在报错
- ExpressionUtil：四则/括号/优先级/取负/小数/科学计数法、变量替换、除零/除模零报错、文法错误报错、空格容错、`check` 合法非法判定
- japicmp：配置正确性由 `mvn verify` 门禁隐式覆盖（本批人工核对插件执行）

## 5. 验收标准

1. `mvn -B verify`（sure-core）全绿：测试通过 + checkstyle + SpotBugs + jacoco ≥ 0.70 + japicmp（无破坏性变更）
2. 新类全量中文 Javadoc；CHANGELOG Unreleased 更新；commit + push 至 main
3. 表达式引擎零第三方运行期依赖（sure-core module-info 不新增 requires）
