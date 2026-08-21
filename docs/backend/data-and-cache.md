# 数据访问、JSON 与缓存使用说明

## MyBatis-Plus

业务实体继承 `BaseEntity`，自动获得雪花 ID、创建/更新时间、创建/更新人和逻辑删除字段：

```java
@TableName("biz_order")
public class OrderEntity extends BaseEntity {
    private String orderNo;
    private Integer status;
}
```

默认 `MetaObjectHandler` 自动填充审计字段。需要项目级替换时提供自己的 `MetaObjectHandler` Bean，不要在每个 Service 重复写填充逻辑。

Mapper 继承 `BaseMapperX<T>`：

```java
@Mapper
public interface OrderMapper extends BaseMapperX<OrderEntity> {}
```

它在 MyBatis-Plus `BaseMapper` 上补充 `countAll()`、`listAll()` 和分页方法。starter 会从 `travis.info.base-package` 扫描 Mapper，并注册：

1. 分页拦截器；
2. 乐观锁拦截器；
3. 防全表更新/删除拦截器。

因此不要另建一套 MyBatis 配置，也不要绕过防全表保护。确需批量操作时，必须使用明确条件和专用 Mapper 方法。

### 条件查询

优先使用 `LambdaQueryWrapperX`：

```java
var wrapper = new LambdaQueryWrapperX<OrderEntity>()
        .likeIfPresent(OrderEntity::getOrderNo, req.getOrderNo())
        .eqIfPresent(OrderEntity::getStatus, req.getStatus())
        .betweenIfPresent(OrderEntity::getCreateTime, req.getStartTime(), req.getEndTime());
```

`likeIfPresent` 忽略空白字符串；范围只有下界时转为 `>=`，只有上界时转为 `<=`。字符串列名场景才使用 `QueryWrapperX`。

`ServiceImplX` 提供 `mapper()`、`getByIdOrThrow()`、`getOneOrThrow()`、`exists()` 和分页便捷方法。它的 `getOne` 对扩展 Lambda wrapper 会追加 `LIMIT 1`，如果业务要求“多条即错误”，应显式使用对应 Mapper 查询并校验。

## Jackson 与 JsonUtil

Jackson starter 自动统一：

- `LocalDateTime` 按 `spring.jackson.date-format` 序列化和反序列化；
- `Long`/`long` 输出为字符串，避免 JavaScript 超过安全整数后丢失精度；
- `JsonUtil` 使用项目的 ObjectMapper；
- 脱敏和富文本清洗 starter 通过 Jackson module 接入同一序列化链路。

常见方法：

```java
String json = JsonUtil.toJsonString(value);
OrderReq req = JsonUtil.parseObject(json, OrderReq.class);
List<OrderReq> list = JsonUtil.parseArray(json, OrderReq.class);
JsonNode tree = JsonUtil.parseTree(json);
OrderResp resp = JsonUtil.convertObject(entity, OrderResp.class);
```

解析不可信输入时使用正常 `parseObject` 并让错误显式暴露。`parseObjectQuietly` 或宽松解析只适合已经定义了失败降级语义的场景，不要用它掩盖数据问题。

## Redis 基础配置

starter 同时配置 Spring Data Redis、JSON `RedisTemplate<String, Object>`、`StringRedisTemplate`、Redisson、Spring Cache 和 Pub/Sub。连接信息使用 Spring Boot 标准配置：

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
      client-name: travis-monolith
      timeout: 3s

travis:
  redis:
    key-prefix: ${spring.application.name}
```

`travis.redis.key-prefix` 会规范化为以 `:` 结尾，并应用到 `RedisUtil`、分布式锁、Pub/Sub、WebSocket 集群状态和 Sa-Token Redis 物理 key。业务代码传逻辑 key 即可，不要手动重复加项目名前缀。

Redis JSON value 包含类型信息，只应用于可信的服务端缓存数据；不要把外部提供的任意 Redis payload 当作安全反序列化输入。

## Spring Cache

直接使用 Spring Cache 注解：

```java
@Cacheable(cacheNames = "user-detail", key = "#id")
public UserResp get(Long id) { ... }
```

全局 TTL 使用 `spring.cache.redis.time-to-live`，单个缓存 TTL 使用：

```yaml
travis:
  redis:
    cache-ttl:
      user-detail: 30m
      dict-tree: 10m
```

CacheManager 是事务感知的，并使用 `SCAN` 批量清理。缓存 value 与普通 Redis value 使用同一 JSON serializer。

精确清理 Spring Cache key 时使用：

```java
RedisUtil.deleteCacheKey("user-detail", userId.toString());
```

该方法会自动计算 Cache 前缀，并在事务成功提交后删除。不要用 `RedisUtil.delete("user-detail:" + id)` 猜测物理缓存 key。

## RedisUtil

`RedisUtil` 已覆盖常用 value、Set、计数、过期和删除操作，并自动加项目级前缀。时间参数统一为毫秒。

```java
RedisUtil.set("verify-code:" + mobile, code, Duration.ofMinutes(5).toMillis());
Object value = RedisUtil.get("verify-code:" + mobile);
boolean first = RedisUtil.setIfAbsent("idempotent:" + key, 1, 60_000);
Long count = RedisUtil.incrementAndExpire("rate:" + userId, 1, 60_000);
```

按 pattern 删除内部使用 Redis `KEYS`，大 key 空间可能阻塞，只能用于范围明确、数量可控的治理操作。常规缓存失效优先精确 key 或 Spring Cache。

## 分布式锁

方法级并发互斥使用 `@DistributedLock`，key 支持 SpEL：

```java
@Service
@DistributedLockNamespace("order-pay")
public class OrderPayService {

    @Transactional
    @DistributedLock(key = "#orderId", waitTime = 1, timeUnit = TimeUnit.SECONDS)
    public void pay(Long orderId) { ... }
}
```

锁切面优先级高于事务切面，因此事务完成后才释放锁。规则：

- `namespace` 可在方法上覆盖，未配置时使用类上的 `@DistributedLockNamespace`；两者都没有时回退到类名和方法名；
- `waitTime=0` 表示立即尝试；
- `leaseTime<0` 使用 Redisson watchdog 自动续期；显式租期适合执行时间有明确上限的动作；
- 获取失败抛 `DISTRIBUTED_LOCK_FAILED`；
- 分布式锁不替代数据库唯一约束、状态机和业务幂等。

不要为已经由 `@DistributedLock` 覆盖的方法再手写一层 Redisson 锁。

## 集群周期任务

固定系统维护或业务对账需要“集群内每个周期最多成功一次”时，使用 `ClusterPeriodicTaskExecutor`：

```java
@Scheduled(fixedDelay = 30_000)
public void reconcile() {
    clusterPeriodicTaskExecutor.executeOncePerInterval(
            "message", "scheduled-push-reconcile", Duration.ofMinutes(1), this::doReconcile);
}
```

它先竞争分布式锁，再检查 Redis 的最后成功标记；只有动作成功返回后才写入一个周期的标记。动作抛异常时不会写成功标记，后续节点仍可重试。

它适合代码固定、后台不需要编辑的周期推进器。需要运维后台配置、启停和执行日志的任务使用 Quartz 动态任务；业务实体决定单次执行时间时使用 `QuartzOneShotManager`。

## Redis Pub/Sub

轻量集群广播可注入 `RedisPubSubClient`：

```java
redisPubSubClient.subscribe("order:changed", (channel, payload) -> handle(payload));
redisPubSubClient.publish("order:changed", JsonUtil.toJsonString(event));
```

频道会自动加项目级前缀。Redis Pub/Sub 不持久化、离线消费者不会补收；需要可靠投递、重试、顺序或跨系统通信时使用 RocketMQ。
