# 批49：Spring Boot Starter 成熟化（v1.20.0 候选，阶段 C「生态」）

> 批次：批49 · 版本：v1.20.0 候选 · 底线：可靠 + 影响力
> 路线图出处：`docs/strategy-beyond-ecosystem.html` 阶段 C（v3.1→v4.0）「生态与治理成熟」→ 生态：「Spring Boot Starter 成熟（自动配置 / 条件装配）」

## 1. 现状盘点（探查结论）

| 项 | 现状 | 缺口 |
| --- | --- | --- |
| `SureToolAutoConfiguration` | 仅 1 个 Bean：CaptchaGenerator（@ConditionalOnMissingBean + @ConditionalOnProperty 已有） | 能力面太薄，仅覆盖验证码 |
| `SureToolProperties` | 仅 `suretool.captcha.*` 一组（width/height/enabled） | 无 event/cron/cache 配置组 |
| 自动装配 | 无 EventBus / CronUtil / Cache 装配 | 未接入 sure-event/sure-cron/sure-cache 能力 |
| 测试 | 3 例（默认装配/属性绑定/禁用开关） | 未覆盖新增域 |
| 对标 | hutool-spring-boot-starter 提供多域自动配置 + 属性绑定 | 成熟度差距 |

## 2. 交付清单（本批 4 项）

### 2.1 SureToolProperties 扩展三配置组
- `suretool.event.enabled`（默认 true）：EventBus 装配开关
- `suretool.cron.enabled`（默认 false）：CronUtil 调度装配开关（默认关，避免隐式启动后台线程）
- `suretool.cache.enabled`（默认 false）+ `suretool.cache.type`（lru/fifo/timed/weak，默认 lru）+ `suretool.cache.capacity`（默认 1024）+ `suretool.cache.timeout`（timed/weak 过期毫秒，默认 60000）

### 2.2 新增自动装配 Bean（SureToolAutoConfiguration 扩展 3 方法）
| Bean | 条件 | 语义 |
| --- | --- | --- |
| `EventBus sureEventBus` | `@ConditionalOnClass(EventBus)` + `@ConditionalOnProperty(suretool.event.enabled, matchIfMissing=true)` + `@ConditionalOnMissingBean` | 返回 `EventBus.getDefault()` 全局单例；`DisposableBean` 销毁时 `clear()` |
| `CronUtil 生命周期`（SmartLifecycle） | `@ConditionalOnClass(CronUtil)` + `@ConditionalOnProperty(suretool.cron.enabled)` + `@ConditionalOnMissingBean` | `start()` → `CronUtil.start()`；`stop()` → `CronUtil.stop()`（幂等）；`getPhase()=Integer.MAX_VALUE`（最后启动最先停止） |
| `Cache<?,?> sureCache` | `@ConditionalOnClass(Cache)` + `@ConditionalOnProperty(suretool.cache.enabled)` + `@ConditionalOnMissingBean` | 按 `type` 分发：lru→`newLruCache(capacity)`、fifo→`newFifoCache(capacity)`、timed→`newTimedCache(timeout)`、weak→`newWeakCache(capacity, timeout)`；`DisposableBean` 销毁时 `clear()` |

### 2.3 条件装配完备性
- `@ConditionalOnClass`（模块 class 缺失自动跳过，不误伤 classpath 精简应用）
- `@ConditionalOnMissingBean`（用户自定义 Bean 优先，spring 惯例）
- `@ConditionalOnProperty`（每域独立开关，`suretool.*.enabled`）
- 全部 Bean 中文 JavaDoc + 可运行示例（starter 风格）

### 2.4 测试扩展（SureToolAutoConfigurationTest +8 例）
- event 默认装配（hasSingleBean EventBus）+ `suretool.event.enabled=false` 不装配
- event 属性开关 + getDefault 单例语义
- cron 默认不装配 + `suretool.cron.enabled=true` 装配 + SmartLifecycle start/stop 幂等
- cache 默认不装配 + lru 装配 + capacity 绑定 + fifo/timed 分支
- cache `suretool.cache.enabled=true` + type=timed + timeout 绑定
- 用户自定义 EventBus/Cache Bean 优先（@ConditionalOnMissingBean）

## 3. 验收标准

1. `mvn -pl sure-spring-boot-starter test` 全绿（含新增 8 例）
2. 全库 `mvn verify` BUILD SUCCESS（sure-core 覆盖率 98% 铁律不受影响——本批不改 sure-core 源码）
3. checkstyle / spotbugs 全绿（starter 模块）
4. CHANGELOG 批49 条目 + 推送闭环（含回读验证）

## 4. 范围边界

- 不改 sure-core / sure-event / sure-cron / sure-cache 源码（纯 starter 层装配，零 API 变更，无 japicmp 风险）
- 不创建新的定时任务；不修改已发布 Maven 制品
- cron/cache 默认关闭（安全默认：不隐式启动后台线程/占用内存）
- Portal publish 待办（v1.19.0 Central staging 未发布）继续跟踪，不因本批关闭
