# Quartz 调度任务使用说明

项目通过 `travis-spring-boot-starter-quartz` 统一接入 Quartz，并使用 JDBC JobStore 支持持久化和集群调度。

当前有两种使用方式：

1. 需要在运维后台配置、启停、立即执行并查看日志的通用任务，实现 `QuartzJobHandler`；
2. 由业务数据决定执行时间的一次性任务，使用 `QuartzOneShotManager`，例如定时推送消息。

普通业务定时任务优先使用第一种。只有任务天然属于某个业务实体、应由业务表作为事实来源时，才使用第二种。

## 基础配置与数据库

需要直接使用 Quartz 能力的业务模块引入项目 Starter；版本由项目 BOM 统一管理：

```xml
<dependency>
    <groupId>com.travis</groupId>
    <artifactId>travis-spring-boot-starter-quartz</artifactId>
</dependency>
```

单体应用已经在 `travis-server/src/main/resources/application.yml` 中启用 Quartz：

```yaml
spring:
  quartz:
    scheduler-name: travisScheduler
    job-store-type: jdbc
    jdbc:
      initialize-schema: never
    overwrite-existing-jobs: false
    wait-for-jobs-to-complete-on-shutdown: true
    properties:
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.jobStore.isClustered: true
      org.quartz.jobStore.clusterCheckinInterval: 10000
      org.quartz.jobStore.tablePrefix: qrtz_
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.threadPool.threadCount: 10
```

`ops_job`、`ops_job_log` 和 `qrtz_` 系列表都由 Flyway 迁移统一创建，入口为：

```text
travis-server/src/main/resources/db/migration/V20260801_0_000__initial_schema.sql
```

不需要另外执行 `docs/sql` 下的初始化脚本。由于 `initialize-schema` 为 `never`，关闭 Flyway 或跳过数据库迁移会导致 Quartz 表不存在。

集群部署时，所有实例必须连接同一套业务数据库，并保持相同的 `scheduler-name` 和 `tablePrefix`；`instanceId` 保持 `AUTO`。项目启动时将 JVM 默认时区设置为 UTC，Cron、单次执行时间和日志时间均按应用默认时区解释。

## 后台动态任务

### 注册任务处理器

业务模块实现 `QuartzJobHandler`，并注册为 Spring Bean：

```java
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import org.springframework.stereotype.Component;

@Component
public class DemoCleanupJobHandler implements QuartzJobHandler {

    @Override
    public String getName() {
        return "demoCleanupJob";
    }

    @Override
    public void execute(String params) throws Exception {
        // 解析 JSON 参数并执行业务逻辑
    }
}
```

后台使用的处理器名称来自 `getName()`，与 Spring Bean 名称无关。名称必须非空且全局唯一，否则应用启动时会失败。

处理器名称是已经持久化到 `ops_job.handler_name` 的稳定标识。发布新版本时不要直接改名；确需改名，应先迁移已有任务配置。

处理器只负责业务执行，不要在其中自行创建 Quartz Job 或写执行日志。统一调度入口会自动完成参数传递、并发控制、耗时统计、异常记录和失败告警。

### 编写可安全调度的处理器

任务可能因手动执行、Misfire 补偿、节点故障恢复或运维操作而再次执行，因此处理器应尽量具备幂等性。建议使用业务唯一键、状态机或数据库约束防止重复副作用。

`execute` 抛出的异常会被记录为失败。不要吞掉需要被运维感知的异常；如果业务允许忽略某个局部失败，应在处理器内部明确记录并继续。

禁止并发只能约束同一个 Quartz JobDetail，不能替代业务幂等，也不能阻止其他入口同时操作同一份业务数据。

### 创建和启用任务

处理器发布后，在管理后台的“运维管理 / 任务调度”中创建任务。新建和复制的任务默认处于停用状态，确认配置无误后再启用。

系统内置维护任务同样展示在任务列表中，并使用统一的执行日志、统计和立即执行能力。
这类任务通过 `is_builtin=1` 标识，调度配置由代码和数据库迁移维护，后台不允许修改、删除或复制，
也不允许启停，但可以按权限立即执行。当前内置任务包括 Quartz 配置对账、消息定时推送对账和任务执行日志清理。

停用任务在 Quartz 中只保留不可自动触发的持久 JobDetail，不保留 Trigger。处理器可以暂未上线，但启用任务或立即执行时必须存在对应的 `QuartzJobHandler`。

支持的调度类型如下：

| 类型 | 必填配置 | 说明 |
| --- | --- | --- |
| `CRON` | `cronExpression` | 使用 Quartz Cron 表达式 |
| `INTERVAL` | `intervalMillis` | 固定间隔，必须大于 0；后台表单最小为 1000 毫秒 |
| `ONCE` | `executeAt` | 在指定时间执行一次 |

可以先使用“执行时间预览”检查后续触发时间。后端实际最多返回 20 个预览结果。

单次计划任务执行结束后，无论成功或失败，任务都会恢复为停用状态。后台“立即执行”属于手动运行，不会消费原来的单次计划，也不会改变任务启停状态。

### 参数

任务参数必须是合法 JSON。未填写参数时，调度执行使用 `{}`。

```json
{
  "batchSize": 100,
  "dryRun": false
}
```

后台立即执行时可以临时传入参数；不传则使用任务默认参数。临时参数必须是合法 JSON，但不会覆盖已保存的默认参数。

### 并发和 Misfire

并发策略：

| 值 | 策略 | 说明 |
| --- | --- | --- |
| `0` | 禁止并发 | 同一任务上一次执行未结束时，不并发启动下一次 |
| `1` | 允许并发 | 同一任务可以同时存在多次执行 |

Misfire 表示计划触发时间已错过，例如应用停机或线程池繁忙。项目支持：

| 值 | 策略 | 说明 |
| --- | --- | --- |
| `0` | Quartz 智能策略 | 由 Quartz 根据 Trigger 类型选择默认处理方式 |
| `1` | 忽略 Misfire | 使用 Quartz 的 Ignore Misfires 指令处理错过的触发 |
| `2` | 立即补执行一次 | 恢复后尽快触发一次 |
| `3` | 等待下一次 | 跳过当前错过的时间，等待后续计划 |

Misfire 不是业务重试策略。处理器执行失败后，系统会记录失败和告警，但不会自动按业务规则重试；需要重试时应设计明确的幂等和重试机制。

### 执行日志和失败告警

每次动态任务执行都会记录：任务和处理器快照、执行参数、计划时间、实际开始和结束时间、耗时、调度器实例、执行状态及异常堆栈。

任务失败时，系统向任务配置的告警接收人发送管理员站内消息。告警发送失败只记录警告，不会覆盖原任务的执行结果。

执行日志统一保留 30 天，可通过 `travis.ops.job.log-retention-days` 或环境变量 `OPS_JOB_LOG_RETENTION_DAYS` 调整。系统在北京时间每天 03:00 物理清理过期执行日志；
应用使用 UTC 默认时区，因此持久化的 Quartz Cron 为 `0 0 19 * * ?`，即 UTC 前一日 19:00。
也可以在执行日志页面按任务或全部清理。调度对账还会把节点中断后遗留的 `RUNNING` 日志标记为中断。

## 业务一次性任务

当调度状态应由业务表决定时，不要为每条业务数据创建 `ops_job`，而是使用 `QuartzOneShotManager` 投影业务状态。系统消息的定时推送就是这一模式。

### 定义任务

```java
QuartzOneShotTask task =
        new QuartzOneShotTask(
                "demo-order-timeout",
                "order-" + order.getId(),
                OrderTimeoutJob.class,
                Map.of("orderId", order.getId()),
                order.getTimeoutAt().atZone(ZoneId.systemDefault()).toInstant(),
                "关闭超时订单 " + order.getId());
```

`group` 和 `taskName` 共同构成稳定身份。`data` 中只放 Quartz Job 执行所需的简单、可序列化数据，通常只传业务 ID，在执行时重新查询最新业务状态。

### 注册分组并同步

每个业务分组必须有唯一所有者：

```java
public DemoOrderScheduler(QuartzOneShotManager oneShotManager) {
    this.oneShotManager = oneShotManager;
    oneShotManager.registerGroup("demo-order-timeout", "demo-order-scheduler");
}
```

业务数据提交后，根据最新状态同步：

```java
if (order.shouldScheduleTimeout()) {
    oneShotManager.sync(toTask(order));
} else {
    oneShotManager.delete("demo-order-timeout", "order-" + order.getId());
}
```

不要在数据库事务提交前修改 Quartz，否则事务回滚后会留下与业务数据不一致的任务。项目已有 `AfterCommitExecutor`，业务写入后应通过它安排同步，并在同步失败时记录错误，由周期对账恢复。

### 周期对账

一次性任务必须提供从业务表扫描全部期望任务的能力：

```java
oneShotManager.reconcile(
        "demo-order-timeout",
        "demo-order-scheduler",
        taskConsumer -> scanPendingOrders(taskConsumer));
```

对账会：

1. 创建缺失任务；
2. 更新执行时间、Job 类型或数据已变化的任务；
3. 删除该分组中业务表已不再需要的孤立任务。

大量数据应分批扫描，避免一次性加载全部记录。集群环境中还应使用项目的 `ClusterPeriodicTaskExecutor` 和分布式锁，保证同一对账周期只由一个节点推进。

`QuartzOneShotManager` 固定使用“错过后立即执行一次”的 Misfire 策略。对应的 Quartz Job 仍必须在执行时核对业务实体的最新状态，避免已取消或已完成的数据产生重复副作用。

## 数据一致性与自动恢复

运维动态任务以 `ops_job` 为事实来源。新增、修改、启停和删除先提交业务事务，再同步 Quartz；同步失败不会回滚已经提交的业务操作。

应用启动时会立即执行一次对账，运行期间每分钟在集群内限频对账一次。对账根据任务配置指纹检查 JobDetail 和 Trigger，并修复缺失、过期或孤立的 Quartz 数据。因此不要直接修改 `qrtz_` 表，也不要把 Quartz 表当作业务配置来源。

单次动态任务完成时也会校验调度配置指纹。任务执行期间如果配置已被修改，旧执行结果不会把新配置错误地停用。

## 权限

调度管理接口使用以下权限：

| 权限码 | 用途 |
| --- | --- |
| `ops:job:query` | 查询任务、处理器、告警接收人选项、预览、统计和看板 |
| `ops:job:update` | 新增、修改、删除和复制任务 |
| `ops:job:operation` | 启停、立即执行和清理执行日志 |
| `ops:job:log:query` | 查询、查看和导出执行日志 |

权限同时在后端接口校验，前端按钮隐藏不能替代权限控制。失败告警接收人还会按当前管理员的数据范围校验。

## 常见问题

### 后台找不到处理器

确认实现类已被 Spring 扫描、实现了 `QuartzJobHandler`，并且 `getName()` 返回值与任务配置完全一致。列表显示“未上线”时，该任务不能启用或立即执行。

### 修改任务后 Quartz 没有立即变化

任务同步发生在业务事务提交后。先检查应用错误日志；即使即时同步失败，启动对账或下一次每分钟对账也会按 `ops_job` 恢复。

### 单次任务执行后仍然启用

只有计划触发会在完成后停用。后台立即执行不会消费计划。如果执行期间任务配置已改变，旧执行也不会停用新配置。

### 集群中出现重复业务效果

JDBC JobStore 和禁止并发可以减少重复调度，但不能提供业务层“恰好一次”。处理器仍应以业务状态和唯一约束实现幂等，并确认所有实例连接同一数据库、时钟和时区配置一致。

### 可以直接操作 Quartz 表吗

不可以。动态任务应通过运维任务接口维护，一次性业务任务应通过 `QuartzOneShotManager` 维护。直接修改 `qrtz_` 表会绕过业务校验、事务提交后同步和周期对账。
