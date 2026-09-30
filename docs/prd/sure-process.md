# PRD：sure-process 进程管理模块

| 项 | 内容 |
|---|---|
| 版本 | v1.4.0（批 7） |
| 模块 | sure-process |
| 包 | com.sure.tool.process |
| 依赖 | 仅 sure-core（零第三方运行期依赖） |
| 许可 | Apache-2.0 |

## 背景与目标

提供面向日常开发与运维的进程管理门面：启动外部进程、等待完成（含超时强杀）、捕获标准输出/错误输出、设置环境变量与工作目录。对标 Hutool 的 ProcessUtil。

## API 设计

- `ProcessResult`：不可变结果对象，含 `exitCode`、`stdout`、`stderr`、`timedOut` 四字段。
- `ProcessUtil`（私有构造器）：
  - `exec(String...)` / `exec(List<String>)` / `exec(String)`（空白拆分参数）/ `exec(ProcessBuilder)` → `ProcessResult`（同步等待）
  - `execWithTimeout(List<String>, long, TimeUnit)` → 超时 `destroyForcibly` 后返回带 `timedOut=true` 的结果
  - `start(List<String>)` / `start(ProcessBuilder)` → `Process`（异步）
  - stdout/stderr 用虚拟线程并发读取，避免管道死锁

## 验收清单

- A1 `exec` 返回退出码与完整 stdout/stderr
- A2 字符串命令按空白拆分为参数，支持双引号包裹的含空格参数
- A3 `execWithTimeout` 超时后进程被强杀，结果 `timedOut=true`
- A4 `ProcessBuilder` 重载支持环境变量与工作目录
- A5 失败命令（非 0 退出）仍正常返回结果，不抛异常
- A6 null/空参数校验抛 IllegalArgumentException
- A7 大输出无死锁（虚拟线程并发读）
- A8 `@since 1.4.0`、中文 Javadoc、私有构造器

## 架构评审

- 零依赖，仅 JDK API；虚拟线程利用 JDK25 特性。
- 不引入进程池/守护进程管理等重能力，保持门面轻量。
- 异常策略：IO 异常包装为 `ProcessRuntimeException`。
