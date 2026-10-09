# 批23 PRD：事件总线（DeadEvent）+ 图算法 + 数学子集

- 状态：已批准
- 目标版本：1.11.0（@since 1.11.0）
- 模块：sure-event（DeadEvent）、sure-core（graph 新包）、sure-math（增强 + StatUtil）

## 背景与缺口

对标 Guava EventBus / Guava Graph / commons-math 子集：
- sure-event EventBus 已有：函数式/注解式注册、继承分发（isAssignableFrom）、异常隔离、同步/虚拟线程异步。**缺 Guava 的 DeadEvent（死事件）**。
- **图能力完全空白**：无 DiGraph/GraphUtil。
- sure-math MathUtil（素数/gcd/lcm/factorial/comb/perm/进制）、BigDecimalUtil（add/sub/mul/div/round）已有；**缺 clamp/lerp/统计（mean/median/stddev/percent）**。

## 迭代内容

### 1. sure-event：DeadEvent（对标 Guava EventBus）
- `EventBus.DeadEvent`：静态嵌套类（source + event 两个字段 + getter）
- post 逻辑改造：事件无任何匹配监听器时，自动改投 `DeadEvent(this, originalEvent)`；DeadEvent 本身无监听器不再递归（内部 allowDead 标志）
- 有监听器时行为不变（不产生 DeadEvent）

### 2. sure-core 新包 graph（对标 Guava Graph 子集）
- `DiGraph<V>`：简单有向图（邻接表）
  - 顶点：addVertex / addVertexIfAbsent / removeVertex / containsVertex / vertexCount / vertices()
  - 边：addEdge(V,V)（自动补顶点）/ removeEdge / containsEdge / edgeCount / edges()（Edge<V> 记录）
  - 邻接：successors / predecessors / adjacentNodes / inDegree / outDegree / degree
- `GraphUtil`（算法）：
  - `topologicalSort(DiGraph)`：Kahn 拓扑排序（有环返回部分排序 + 异常？——返回 Optional 空表示有环）
  - `hasCycle(DiGraph)`：Kahn 计数环检测
  - `dfs(DiGraph, V)` / `bfs(DiGraph, V)`：遍历顺序
  - `isReachable(DiGraph, V, V)`：BFS 可达
  - `shortestPath(DiGraph, V, V)`：无权 BFS 最短路径（返回路径 List 或 null）

### 3. sure-math 增强（对标 commons-math 高频子集）
- `MathUtil +7`：clamp(int/long/double)、lerp(double,double,double)、isEven、isOdd、average(int[]/long[]/double[])、maxOf/minOf（varargs）
- `BigDecimalUtil +4`：percent(part,total,scale)、isZero、compare、toPlainString
- 新增 `StatUtil`（对标 commons-math StatUtils）：mean、median、variance、stdDev、min、max、sum（double[] 输入）

## 验收标准

- EventBusTest（DeadEvent 2 例：无监听器触发死事件 / 有监听器不触发）
- DiGraphTest ≥8 例、GraphUtilTest ≥6 例（拓扑/环/dfs/bfs/可达/最短路径）
- MathUtilBatch23Test ≥5、BigDecimalUtilBatch23Test ≥3、StatUtilTest ≥6
- 三模块门禁全绿：checkstyle + SpotBugs(effort=Max) + jacoco ≥0.90 + 全测试
- 中文 Javadoc + @since 1.11.0、Apache-2.0 header、Tab 缩进、零新增第三方运行期依赖
