# PRD：sure-socket 网络模块（TCP Socket 门面）

- 状态：Accepted（2026-09-29）｜ 模块：`sure-socket` ｜ 包：`com.sure.tool.socket` ｜ 版本：1.4.0
- 依赖：仅 sure-core（零第三方运行时依赖）｜ JDK 25+ ｜ 模块化：`module sure.socket`
- 对标：Hutool socket（SocketUtil / NioServer / NioClient）

## A. 目标与定位

补齐 TCP Socket 轻量门面。不做 NIO/Reactor 复杂抽象（Hutool NIO 过于厚重），
提供**同步阻塞 + 虚拟线程**的实用模型：静态探测工具 + 可复用服务端 + 客户端门面。

## B. 模块 API 设计

### B1. `SocketUtil`（静态工具）
- `connect(String host, int port, int timeoutMillis)`：带超时连接，失败抛 `SocketRuntimeException`
- `isReachable(String host, int port, int timeoutMillis)`：TCP 可达性探测（非阻塞 try-connect）
- `isPortAvailable(int port)`：本机端口是否可绑定（自动释放）
- `getLocalHost()`：本机 IPv4（跳过 loopback/虚拟网卡，首选 site-local）
- `isInnerIP(String ip)`：内网 IP 判断（10.x / 172.16-31.x / 192.168.x / 127.x / 0.x / 169.254.x）
- `safeClose(AutoCloseable...)`：null 安全、异常吞并的关闭
- `getRemoteAddress(Socket)`：远程 `host:port` 描述
- `readBytes(Socket)` / `readLine(Socket, Charset)`：阻塞读全部可用/读一行（复用 sure-core IoUtil 风格）
- `writeString(Socket, String, Charset)` / `writeBytes(Socket, byte[])`：一次性写出并 flush

### B2. `SocketServer`（可复用 TCP 服务端）
- 构造：`(int port, SocketHandler handler)` / `(int port, BiConsumer<Socket, SocketServer> handler)`
- `start()`：绑定端口（port=0 自动分配，`getPort()` 取实际），accept 循环
- 每连接**虚拟线程**（`Thread.ofVirtual()`）执行 handler，连接异常不中断 accept 循环
- `stop()`：关闭 ServerSocket 并中断 accept（幂等）
- `isRunning()` / `getPort()` / `getLocalAddress()`

### B3. `SocketClient`（TCP 客户端门面）
- 构造即连接：`(String host, int port)` / `(String host, int port, int timeoutMillis)` / `(Socket)`
- `send(byte[])` / `sendString(String, Charset)`；`receive(byte[])` 读满 / `receiveLine(Charset)` / `receiveAll(Charset)` 读到 EOF
- `isConnected()` / `close()`（幂等）；`getSocket()`

### B4. `SocketHandler` 函数式接口 + `SocketRuntimeException`

## C. 验收标准（A1-A8）
- A1 `connect` 成功建立 TCP 连接；非法 host/port 抛 `SocketRuntimeException`
- A2 `isReachable` 对开放端口 true、未开放端口 false；`isPortAvailable` 对占用端口 false
- A3 `isInnerIP`：`192.168.1.1`/`10.0.0.1`/`172.16.0.1` true；`8.8.8.8`/`114.114.114.114` false
- A4 `SocketServer` 端口 0 启动，`getPort()` 返回实际可用端口，`isRunning()` true
- A5 回环测试：SocketClient 连 SocketServer，sendString → receiveLine 内容一致（中文 UTF-8）
- A6 并发：5 个客户端并发连接服务端，全部收到正确回显
- A7 `stop()` 后 `isRunning()` false；重复 stop 不抛异常
- A8 `safeClose(null)` 不抛；`SocketClient.close()` 幂等

## D. 非目标
- 不做 NIO/Netty 级抽象、不做 UDP（后续版本可加 DatagramUtil）
- 不做粘包/拆包协议层（receiveLine 按 \n 分隔的约定足够覆盖多数脚本/工具场景）
