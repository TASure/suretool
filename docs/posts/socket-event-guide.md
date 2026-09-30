# sure-socket + sure-event 实战：TCP 门面与零依赖事件总线

> 适用版本：suretool ≥ 1.4.0 ｜ JDK 25+ ｜ 零第三方运行时依赖

## 1. sure-socket：30 秒搭一个并发 TCP 服务

### 1.1 服务端（每连接一个虚拟线程）

```java
import com.sure.tool.socket.SocketServer;
import com.sure.tool.socket.SocketUtil;
import java.nio.charset.StandardCharsets;

// port=0 自动分配端口；handler 每连接一个虚拟线程执行
SocketServer server = new SocketServer(0, socket -> {
    String line = SocketUtil.readLine(socket, StandardCharsets.UTF_8);
    SocketUtil.writeString(socket, "echo: " + line + "\n", StandardCharsets.UTF_8);
});
server.start();
int port = server.getPort();
```

### 1.2 客户端门面

```java
import com.sure.tool.socket.SocketClient;
import java.nio.charset.StandardCharsets;

try (SocketClient client = new SocketClient("127.0.0.1", port)) {
    client.sendString("hello suretool\n", StandardCharsets.UTF_8);
    String resp = client.receiveLine(StandardCharsets.UTF_8); // "echo: hello suretool"
}
```

### 1.3 探测工具

```java
import com.sure.tool.socket.SocketUtil;

boolean up = SocketUtil.isReachable("10.0.0.1", 80, 3000); // 连接探测（带超时）
boolean free = SocketUtil.isPortAvailable(8080);
boolean inner = SocketUtil.isInnerIP("192.168.1.1");          // 内网判断
```

> 适用：轻量 RPC、内部 agent 通信、测试桩；高吞吐生产场景仍建议 Netty。

## 2. sure-event：函数式事件总线

### 2.1 函数式订阅（可取消）

```java
import com.sure.tool.event.EventBus;

EventBus bus = new EventBus();

var sub = bus.subscribe(OrderCreated.class, e -> System.out.println("订单: " + e.orderId()));
sub.cancel();   // 取消订阅
```

### 2.2 注解注册

```java
import com.sure.tool.event.EventBus;
import com.sure.tool.event.EventSubscribe;

public class OrderListener {
    @EventSubscribe
    public void onOrder(OrderCreated e) { /* 业务处理 */ }
}

EventBus bus = new EventBus();
bus.register(new OrderListener());   // 扫描 @EventSubscribe 方法
```

### 2.3 同步与异步投递

```java
bus.post(new OrderCreated(1001L));        // 同步：调用方线程，有序可靠
bus.postAsync(new OrderCreated(1002L));   // 异步：虚拟线程投递，不阻塞调用方

// 异常处理可插拔
bus.setExceptionHandler((event, ex) -> log.warn("事件处理失败", ex));
```

> 特点：零第三方依赖（对比 Guava EventBus）、支持父类事件匹配（`Object` 监听器可收全部事件）、并发安全。

## 3. 组合实战：订单事件驱动 + 回调通知

```java
// 1) 事件总线解耦：下单 → 发通知 + 记账，互不阻塞
EventBus bus = new EventBus();
bus.subscribe(OrderCreated.class, e -> mailService.send(e.orderId()));
bus.subscribe(OrderCreated.class, e -> accountService.book(e.orderId()));

// 2) 异步通知服务：socket 回调给客户端
SocketServer server = new SocketServer(0, socket -> {
    String cmd = SocketUtil.readLine(socket, StandardCharsets.UTF_8);
    SocketUtil.writeString(socket, "received: " + cmd + "\n", StandardCharsets.UTF_8);
});
server.start();
```

> 完整可运行示例见 `sure-examples` 的 `SocketDemo` 与 `EventBusDemo`。
