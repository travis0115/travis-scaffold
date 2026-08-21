# 事件、消息与可观测性使用说明

## 先选择正确的机制

| 需求 | 使用 | 不适合 |
| --- | --- | --- |
| 同一事务成功后做一个不要求恢复的本地动作 | `AfterCommitExecutor` | 进程故障后必须补偿 |
| 单体模块解耦，要求事务事件可持久化和重发布 | Spring Modulith 类型事件 | 跨系统、顺序或延迟投递 |
| 明确控制“加入当前事务”或“新事务”发布事件 | `TransactionalApplicationEventPublisher` | 普通无事务通知 |
| 跨系统、顺序、延迟、消费重试 | `MessagePublisher`/RocketMQ | 仅为绕开模块边界直接传内部对象 |
| 固定周期维护 | Spring Scheduling + `ClusterPeriodicTaskExecutor` | 后台可编辑任务 |
| 后台可配置调度或业务一次性任务 | Quartz 封装 | 简单的事务提交后动作 |

## Spring Modulith 事件

事件类型放在所属模块公开的 `api.event` 包，内容使用不可变、可序列化的业务快照或 ID：

```java
public record OrderPaidEvent(Long orderId) {}
```

发布时如果业务方法已经有事务，可直接使用 Spring 的 `ApplicationEventPublisher`。需要显式保证事务语义时注入：

```java
transactionalEventPublisher.publishEvent(new OrderPaidEvent(orderId));
```

- `publishEvent`：加入当前事务；当前无事务时新建事务；
- `publishEventRequiresNew`：挂起当前事务，在独立新事务中发布。

接收方优先使用 Spring Modulith 的 `@ApplicationModuleListener`。监听器要通过事件中的 ID 重新查询最新状态，不要依赖发布模块的内部实体。当前配置会删除已完成事件，并在启动时重发未完成事件，因此监听器必须幂等。

模块间只依赖目标模块公开的 `api`/`event` `@NamedInterface`，不能引用其 `internal` 包。

## RocketMQ 发布

只有需要 MQ 语义时才定义实现 `Message` 的枚举：

```java
@Getter
@AllArgsConstructor
public enum OrderMessage implements Message {
    ORDER_CREATED("order-event", "order-created", MessageType.ORDERED),
    ORDER_TIMEOUT("order-delay", "order-timeout", MessageType.DELAYED);

    private final String topic;
    private final String type;
    private final MessageType messageType;
}
```

业务注入抽象 `MessagePublisher`：

```java
messagePublisher.publishOrdered(
        OrderMessage.ORDER_CREATED, payload, "order-" + orderId);
messagePublisher.publishDelayed(
        OrderMessage.ORDER_TIMEOUT, payload, Duration.ofMinutes(30));
```

普通、顺序、延迟分别使用 `NORMAL`、`ORDERED`、`DELAYED`。同一个 Topic 的所有枚举值必须使用同一种 `MessageType`，否则启动扫描会失败。

异步发布返回 `CompletableFuture<Void>`，也可以传 `MessagePublishCallback` 感知成功/失败。异步不是可靠事务消息；数据库提交和 MQ 发送需要严格一致时，应设计 outbox/补偿，而不是只把同步调用改成异步。

业务代码优先依赖 `MessagePublisher`，`RocketMQProducerUtil` 是底层实现工具，不应在各模块形成第二套发送封装。

## RocketMQ 消费

消费者继承 `AbstractEventListener<T>`：

```java
@Component
@RocketMQMessageListener(
        topic = "order-event",
        tag = "order-created",
        consumerGroup = "order-created-consumer")
public class OrderCreatedListener extends AbstractEventListener<OrderCreatedPayload> {
    @Override
    protected void onEvent(OrderCreatedPayload payload) {
        // 幂等处理
    }
}
```

基类完成 body 读取、JSON 反序列化、事务、耗时日志和错误上报。`onEvent` 抛异常会让消费失败并交给 RocketMQ 重试，因此：

- 消费逻辑必须幂等；
- 不要吞掉需要重试的异常；
- 泛型必须是可解析的具体 payload 类；
- 消费者组名称应稳定，修改前评估历史消费位点。

## RocketMQ 配置与自动初始化

客户端生产/消费使用 Proxy gRPC `endpoints`；自动初始化使用 NameServer 地址，二者端口不同：

```yaml
rocketmq:
  producer:
    endpoints: 127.0.0.1:8081
  push-consumer:
    endpoints: 127.0.0.1:8081

travis:
  rocketmq:
    auto-initialize:
      enabled: true
      namesrv-addr: 127.0.0.1:9876
```

自动初始化会扫描 `*Message` 枚举和 `@RocketMQMessageListener` Bean，在消费者启动前检查 Topic 类型和消费者组。默认关闭；当前单体的 dev/prod 配置显式开启。连接或创建失败目前记录警告后继续启动，因此生产环境仍应由运维确认 Broker 资源和权限。

## 操作日志、访问日志与事件日志

三类日志用途不同：

| 类型 | 用途 | 入口 |
| --- | --- | --- |
| 操作日志 | 管理员执行了什么关键业务动作，可在后台审计 | `@OperationLogModule`、`@OperationLog` |
| 访问日志 | HTTP 请求、响应状态、耗时和请求上下文 | WebMVC/Logging starter 自动采集，`logging.access.enabled` 控制 |
| 事件日志 | 应用关键业务/技术事件的结构化记录 | `EventLoggerUtil.log(...)` |

开发环境日志偏向可读控制台，生产环境按 `logging.output=file|stdout` 选择结构化 logger。事件附加字段使用稳定 snake_case key，禁止记录密码、token、secret 或未脱敏请求体。

## 错误上报

未预期异常统一通过 `ErrorReporter`：

```java
try {
    externalClient.call();
} catch (RuntimeException ex) {
    errorReporter.report(ErrorSource.ASYNC, "OrderSync#call", orderId.toString(), ex);
    throw ex;
}
```

当前来源包括 `WEB`、`QUARTZ`、`ROCKETMQ`、`WEBSOCKET`、`SCHEDULING`、`ASYNC`。starter 已自动覆盖：

- 全局 HTTP 未预期异常及脱敏请求快照；
- 无返回值 `@Async` 异常；
- Spring 调度器未捕获异常；
- Quartz、RocketMQ 和 WebSocket 封装内的执行异常。

因此这些边界内不要重复手工上报同一个异常。只有自己捕获并吞掉、转为降级或位于新的执行边界时才显式调用 `ErrorReporter`。

## 扩展错误渠道

业务或部署环境需要额外告警渠道时，实现 `ErrorReporterContributor`：

```java
@Component
public class AlertContributor implements ErrorReporterContributor {
    @Override
    public void report(ErrorEvent event) {
        // 发送到外部告警渠道
    }
}
```

Monitor starter 会把所有 contributor 组合为主 `ErrorReporter`。Contributor 自身应快速、隔离失败、避免再次抛出导致主业务异常；需要慢速网络调用时应采用有界异步或可靠队列。

默认 Modulith 上报器把错误事件交给 Ops 错误日志模块持久化，支持同类聚合、发生明细、上下文、处理和删除。请求参数在进入错误事件前应已经脱敏；脱敏失败必须保持 fail-closed，不能回退记录原文。
