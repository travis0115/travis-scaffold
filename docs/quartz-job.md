# Quartz 调度任务接入说明

## 初始化

1. 执行 `docs/sql/quartz-mysql.sql`，创建 Quartz 集群表。
2. 执行 `docs/sql/ops-job.sql`，创建任务与执行日志表。
3. 启动应用。Quartz 使用业务数据源、JDBC JobStore 和集群模式，多实例共享同一套调度表。

Quartz 表统一使用小写 `qrtz_` 前缀，所有表统一使用 MySQL 8.0+ 的 `utf8mb4_0900_ai_ci` 排序规则。业务侧只新增 `ops_job`、`ops_job_log` 两张表，统计数据以执行日志为准并缓存到 Redis。

已经创建过表的数据库执行 `docs/sql/quartz-table-metadata.sql`，可统一排序规则并补充表注释。

旧数据库需要全库统一排序规则时，执行 `docs/sql/mysql-utf8mb4-0900-migration.sql`。

## 注册任务处理器

业务模块实现 `QuartzJobHandler` 并注册为 Spring Bean，Bean 名称就是后台选择的处理器名称。

```java
@Component("demoCleanupJob")
public class DemoCleanupJob implements QuartzJobHandler {

    @Override
    public void execute(String params) {
        // 执行业务逻辑
    }
}
```

任务保存时允许处理器暂未上线，但启用任务或立即执行前必须存在对应 Bean。这样可以先导入暂停任务，再随应用版本发布处理器。

## 权限

在系统菜单管理中为调度任务页面配置以下按钮权限：

| 权限码 | 用途 |
| --- | --- |
| `ops:job:view` | 查询任务、日志、统计和导出 |
| `ops:job:edit` | 新增、修改、复制、删除、导入和清理日志 |
| `ops:job:status` | 启用或暂停任务 |
| `ops:job:run` | 立即执行任务 |
| `ops:job:exception` | 查看异常类、异常信息和堆栈 |

接口层会再次校验权限，不能只依赖前端按钮隐藏。

已有旧版任务调度单页菜单的环境执行 `docs/sql/quartz-job-permissions.sql`，脚本会将任务调度调整为目录，新增任务管理和执行日志页面，并继承原菜单的角色授权。

## 配置

Quartz 集群配置位于 `travis-server/src/main/resources/application.yml` 的 `spring.quartz`。生产环境必须保证所有实例连接同一个数据库，并为每个实例保留 `instanceId=AUTO`。

默认每天 03:00 按任务配置的日志保留天数清理过期日志。任务失败后会通过系统站内通知发送给任务配置的告警接收人。

## 参数校验范围

任务参数和参数 Schema 都必须是合法 JSON。当前内置 Schema 校验覆盖后台常用的 `type`、`required`、`properties`、`items`，不是完整 JSON Schema 标准实现。
