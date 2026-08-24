# 后端配置项参考

本文是后端配置的统一字典。业务模块目前没有各自独立加载一份 yml；可运行应用的配置集中在 `travis-server/src/main/resources/application*.yml`，各 starter 通过 Spring Boot 配置属性读取对应前缀。前端变量见 [前端配置](../frontend/configuration.md)。

## 配置加载关系

| 文件 | 用途 |
| --- | --- |
| `application.yml` | 所有环境共享的服务、Redis、数据库连接池、Quartz、Modulith、Web 和认证基础配置 |
| `application-dev.yml` | 开发日志、Actuator、开发数据库和 RocketMQ |
| `application-prod.yml` | 生产日志、受限 Actuator、生产数据库和 RocketMQ ACL |
| starter 下的 `application-reference.yml` | 配置示例和可选项参考，不是业务模块自己的环境配置 |
| `compose.yaml` | 本地 MySQL、Redis、RocketMQ 基础设施 |

默认 profile 是 `dev`。生产启动必须显式使用 `prod`，并通过环境变量注入密钥；不要把真实密码写回仓库。

## 按模块查配置

| 模块/starter | 主要前缀 | 当前配置位置 |
| --- | --- | --- |
| server 基础运行 | `server.*`、`spring.*`、`management.*` | `application.yml` 与 profile 文件 |
| common/项目扫描 | `travis.info.*` | `application.yml` |
| webmvc | `travis.web.apis/cors/i18n/no-repeat-submit`、`logging.access.*` | `application.yml` 与 profile 文件 |
| sa-token | `sa-token.*`、`travis.web.security.*` | `application.yml` |
| websocket | `travis.web.websocket.*` | `application.yml` |
| redis | `spring.data.redis.*`、`spring.cache.*`、`travis.redis.*` | `application.yml`；starter 有 `application-reference.yml` |
| jackson | `spring.jackson.*`、`travis.web.locale/time-zone` | `application.yml`；starter 有 `application-reference.yml` |
| mybatis | `spring.datasource.*`、`mybatis-plus.*` | `application.yml` 与 profile 文件 |
| quartz/ops job | `spring.quartz.*`、`travis.ops.job.*` | `application.yml` |
| event/Modulith | `spring.modulith.*` | `application.yml` 与 profile 文件 |
| logging/monitor | `logging.*`、`management.*` | profile 文件 |
| rocketmq | `rocketmq.*`、`travis.rocketmq.*` | profile 文件；starter 有 `application-reference.yml` |
| system/file | `travis.web.file.*`、`spring.servlet.multipart.*` | `application.yml` |
| system/message | 无独立 yml 前缀 | 使用 Quartz、Redis、WebSocket 等基础模块配置 |

starter 的 `application-reference.yml` 只存在于 Jackson、Redis、RocketMQ starter，用来展示标准属性；其他 starter 的可配置项以配置属性类和本页为准。

## 必要环境变量

| 变量 | dev | prod | 作用 |
| --- | --- | --- | --- |
| `JWT_SECRET_KEY` | 有仅供本地使用的默认值 | 必填 | Sa-Token JWT 签名密钥 |
| `MYSQL_USERNAME` | 默认 `travis` | 必填 | MySQL 用户名 |
| `MYSQL_PASSWORD` | 必填 | 必填 | MySQL 密码 |
| `MYSQL_URL` | 使用固定本地 URL | 必填 | 完整 JDBC URL |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_DATABASE` | 均有本地默认值 | `REDIS_HOST` 必填，其余有默认值 | Redis 连接 |
| `REDIS_PASSWORD` | 可为空 | 必填 | Redis 密码 |
| `DRUID_USERNAME` / `DRUID_PASSWORD` | 均有开发默认值 | 不使用 | Druid 管理页在生产关闭 |
| `OPS_JOB_LOG_RETENTION_DAYS` | 默认 `30` | 默认 `30` | Ops 任务日志保留天数，最小 1 |
| `ROCKETMQ_NAMESRV_ADDR` | 默认 `127.0.0.1:9876` | 必填 | 自动创建 Topic/消费者组时连接 NameServer |
| `ROCKETMQ_PRODUCER_ENDPOINTS` / `ROCKETMQ_CONSUMER_ENDPOINTS` | 默认 `127.0.0.1:8081` | 必填 | RocketMQ v5 Proxy gRPC 地址 |
| `ROCKETMQ_*_ACCESS_KEY` / `ROCKETMQ_*_SECRET_KEY` | 可为空 | 必填 | 生产者/消费者 ACL |
| `HOSTNAME` | 不使用 | 默认 `local` | 生产日志实例目录，并可参与实例识别 |

`${VAR:default}` 表示有默认值，`${VAR}` 表示缺失时无法正确绑定或启动。生产部署还会由 `validate.sh` 检查弱口令、密钥长度和必要变量。

## 服务与 Spring 基础配置

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | HTTP 服务端口 |
| `spring.profiles.active` | `dev` | 默认环境；生产需覆盖为 `prod` |
| `spring.application.name` | `travis-monolith` | 日志、缓存和 Redis 前缀的基础名称 |
| `spring.application.instance-id` | 未配置；回退 `HOSTNAME`/`local` | 可选，Ops 错误日志记录的实例名 |
| `info.app.version` | 未配置 | 可选，缺少构建版本信息时作为 Ops 错误日志的版本回退 |
| `spring.autoconfigure.exclude` | Redisson V4 自动配置 | 保持 `RedisTemplate` 使用 Lettuce，项目 Redis starter 单独构建 Redisson 客户端 |
| `spring.threads.virtual.enabled` | `true` | 启用虚拟线程 |
| `spring.docker.compose.enabled` | `false` | 应用启动不自动拉起 Compose |
| `spring.jmx.enabled` | `false` | 关闭 JMX |
| `travis.info.base-package` | `com.travis.monolith` | MyBatis/项目组件扫描使用的根包，不要随意改成子模块包 |

## 数据库、MyBatis 与 Flyway

| 配置项 | 当前值/作用 |
| --- | --- |
| `spring.datasource.url/username/password` | dev 与本地 Compose 对齐；prod 必须通过环境变量提供 |
| `spring.datasource.type` | Druid 数据源 |
| `spring.datasource.druid.initial-size/min-idle/max-active/max-wait` | 初始 5、最小空闲 10、最大 20、等待 60000ms |
| `spring.datasource.druid.validation-query` | `SELECT 1 FROM DUAL` |
| `spring.datasource.druid.stat-view-servlet.*` | `/druid/*` 监控页、访问 IP、账号和重置开关 |
| `spring.datasource.druid.filters/filter.*` | SQL 统计、慢 SQL 和 Wall 防护；当前允许多语句 |
| `spring.datasource.druid.web-stat-filter.*` | Web 统计范围及静态资源排除 |
| `mybatis-plus.configuration.log-impl` | 使用 SLF4J 输出 SQL 日志 |
| `mybatis-plus.mapper-locations` | `classpath*:mapper/*.xml` |
| `spring.flyway.enabled` | 当前为 `false`，应用默认不会执行迁移 |
| `spring.flyway.locations` | `classpath:db/migration` |
| `spring.flyway.out-of-order` | `true`，允许补充较低版本迁移 |
| `spring.flyway.validate-on-migrate` / `validate-migration-naming` | 均为 `true` |
| `spring.flyway.clean-disabled` | `true`，禁止清库 |
| `spring.flyway.baseline-on-migrate` | `false`，不会自动接管非空库 |

迁移脚本规则见 [数据库迁移说明](../../backend/travis-monolith/travis-server/src/main/resources/db/README.md)。需要应用启动迁移时应明确启用 Flyway，并先确认目标库基线；不要把“脚本存在”当成“启动时自动执行”。

## Redis 与 Spring Cache

| 配置项 | 当前值/默认值 | 说明 |
| --- | --- | --- |
| `spring.data.redis.host/port/database` | `127.0.0.1` / `6379` / `0` | Redis 连接 |
| `spring.data.redis.client-name` | 应用名 | 客户端标识 |
| `spring.data.redis.timeout` | `3000ms` | 操作超时 |
| `spring.data.redis.lettuce.pool.*` | active 2000、idle 20/5 | Lettuce 连接池 |
| `spring.cache.type` | `redis` | Spring Cache 后端 |
| `spring.cache.redis.key-prefix` | `${travis.redis.key-prefix}` | 缓存键前缀 |
| `spring.cache.redis.time-to-live` | `1d` | 未单独配置缓存名时的 TTL |
| `spring.cache.redis.cache-null-values` | `true` | 缓存空值，降低穿透 |
| `travis.redis.key-prefix` | 应用名 | 项目 Redis key 总前缀 |
| `travis.redis.cache-ttl.<cacheName>` | 未配置 | 按 Spring Cache 名称覆盖 TTL，值使用 `10m`、`1h` 等 Duration |

业务代码应使用 `RedisUtil`、Spring Cache 和项目锁封装，不自行拼全局前缀。详见 [数据访问与缓存](data-and-cache.md)。

## WebMVC、国际化与文件

| 配置项 | 当前值/默认值 | 说明 |
| --- | --- | --- |
| `travis.web.apis[].prefix` | `/api/admin`、`/api/app` | 自动添加的接口前缀 |
| `travis.web.apis[].package-pattern` | `controller.admin/app` | Controller 包名包含匹配 |
| `travis.web.apis[].enabled` | 默认 `true` | 是否启用该规则 |
| `travis.web.request-cache-limit` | 默认 `262144` 字节 | 可重复读取的请求体缓存上限，`<=0` 不限制 |
| `travis.web.cors.allowed-origin-patterns/methods/headers` | `*` | CORS 范围；生产建议收紧来源 |
| `travis.web.cors.allow-credentials` | `true` | 是否允许凭证 |
| `travis.web.cors.exposed-headers` | Authorization、Content-Disposition、X-Request-Id | 浏览器可读响应头 |
| `travis.web.cors.max-age` | `3600` 秒 | 预检缓存时间 |
| `travis.web.no-repeat-submit.key-prefix` | `repeat-submit:` | 防重复提交 Redis key 业务前缀 |
| `travis.web.locale` | `zh_CN` | 同时供 Spring Web 与 Jackson 使用 |
| `travis.web.time-zone` | `UTC` | Jackson、数据库 URL 与日志使用的业务时区 |
| `travis.web.i18n.enabled` | `true` | 启用项目国际化能力 |
| `spring.messages.basename` | `i18n/messages` | 消息资源位置 |
| `spring.servlet.multipart.max-file-size` | `50MB` | 单文件上限 |
| `spring.servlet.multipart.max-request-size` | `100MB` | 整个 multipart 请求上限 |
| `travis.web.file.resource-handler` | `/files/**` | 本地文件对外访问匹配路径 |
| `travis.web.file.allowed-extensions` | yml 中列出的图片、文档、压缩包、音视频等 | 扩展名白名单，可使用带点或不带点写法 |

API、安全清洗和防重复提交见 [Web、认证与 WebSocket](web-auth-websocket.md)，文件业务接入见 [文件管理与业务附件接入](file-management.md)。

## Sa-Token 与鉴权规则

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `sa-token.token-name` / `token-prefix` | `Authorization` / `Bearer` | HTTP Token 头 |
| `sa-token.jwt-secret-key` | `${JWT_SECRET_KEY}` | JWT 密钥，无默认值 |
| `sa-token.timeout` | `-1` | Token 总有效期永久，仍受 active timeout 约束 |
| `sa-token.active-timeout` | `604800` 秒 | 7 天无操作失效 |
| `sa-token.auto-renew` | `true` | 自动续活 |
| `sa-token.is-read-body/is-read-cookie` | 均为 `false` | 不从 body/cookie 读取 Token |
| `sa-token.is-write-header` | `true` | 登录后写响应头 |
| `travis.web.security.auth-rules[].login-type` | admin、app | 必须匹配对应 `StpLogic`/`LoginType` |
| `path-patterns` / `exclude-path-patterns` | 各端 API 与登录排除路径 | Ant 路径规则 |
| `websocket-path` | `/ws/admin`、`/ws/app` | 对应登录体系的握手端点 |

新增登录体系时必须同时提供对应 `StpLogic` Bean、HTTP 规则和需要的 WebSocket 端点。

## WebSocket

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `travis.web.websocket.enabled` | `true` | 总开关 |
| `heartbeat-interval` | `30000ms` | 心跳间隔，`<=0` 关闭心跳 |
| `session-timeout` | `300000ms` | Session 超时 |
| `offline-grace-period` | `15000ms` | 刷新页面等短暂断连的离线宽限期 |
| `credential-key` | `ticket` | 握手凭证查询参数名 |
| `redis.enabled` | `true` | Redis Pub/Sub 集群广播；关闭后只投递本实例 |
| `redis.channel` | `websocket:channel:broadcast` | 广播频道 |
| `redis.session-key-prefix` | `websocket:session` | 在线 Session 映射前缀 |
| `redis.retry-interval` | `30000ms` | Redis 异常后的重试间隔 |

## Quartz 与 Modulith

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `spring.quartz.scheduler-name` | `travisScheduler` | 调度器名称 |
| `spring.quartz.job-store-type` | `jdbc` | 持久化 JobStore |
| `spring.quartz.jdbc.initialize-schema` | `never` | 不自动建 Quartz 表 |
| `spring.quartz.overwrite-existing-jobs` | `false` | 不覆盖持久化任务 |
| `spring.quartz.wait-for-jobs-to-complete-on-shutdown` | `true` | 关闭时等待任务 |
| `org.quartz.scheduler.instanceId` | `AUTO` | 集群实例 ID |
| `org.quartz.jobStore.isClustered` | `true` | 集群模式 |
| `clusterCheckinInterval` / `tablePrefix` | `10000ms` / `qrtz_` | 集群心跳与表前缀 |
| `org.quartz.threadPool.threadCount` | `10` | Quartz 工作线程数 |
| `travis.ops.job.log-retention-days` | `30` | Ops 执行日志保留天数 |
| `spring.modulith.detection-strategy` | `explicitly-annotated` | 只识别显式模块 |
| `spring.modulith.events.completion-mode` | `DELETE` | 完成后删除事件发布记录 |
| `spring.modulith.events.jdbc.schema-initialization.enabled` | `false` | 不自动建事件表 |
| `republish-outstanding-events-on-restart` | `true` | 重启补发未完成事件 |
| `spring.modulith.runtime.verification-enabled` | dev `true`、prod `false` | 开发期运行时边界验证 |

Quartz 表和 Modulith 事件表必须由数据库脚本提供。详细用法见 [Quartz 调度任务](quartz.md) 与 [后端模块开发](module-development.md)。

## RocketMQ

| 配置项 | dev | prod | 说明 |
| --- | --- | --- | --- |
| `rocketmq.producer.endpoints` | 本地 Proxy | 环境变量必填 | v5 Producer gRPC 端点 |
| `rocketmq.push-consumer.endpoints` | 本地 Proxy | 环境变量必填 | v5 Consumer gRPC 端点 |
| `access-key/secret-key` | 可为空 | 环境变量必填 | ACL 凭证 |
| `enable-msg-trace` | `false` | `true` | 消息轨迹 |
| `travis.rocketmq.auto-initialize.enabled` | `true` | `true` | 启动时创建缺失 Topic/消费者组 |
| `travis.rocketmq.auto-initialize.namesrv-addr` | `127.0.0.1:9876` | 环境变量必填 | 初始化使用 NameServer，不是 Proxy 端口 |
| `travis.rocketmq.client.log-path` | `${user.home}/data/logs/rocketmq` | `/data/logs/rocketmq` | v5 客户端日志根目录 |

starter 中的 `application-reference.yml` 还列出 Producer group、请求超时等 RocketMQ 官方属性；实际值以 server 的 profile 配置和官方 starter 绑定为准。

## 日志、监控与 Jackson

| 配置项 | dev | prod |
| --- | --- | --- |
| `logging.access.enabled` | `true` | `true` |
| `logging.output` | 未设置，控制台 | `file` |
| `logging.level.root` | INFO | info |
| `logging.file.path` | 不使用 | `/data/logs/<应用>/<实例>` |
| `logging.file.async.*` | 不使用 | 队列 1024、默认允许阻塞以避免丢日志 |
| `logging.logback.rollingpolicy.*` | 不使用 | 30 天、单文件 10MB、总量 5GB |
| `management.endpoints.web.exposure.include` | `*` | 仅 `health`；详情和组件始终隐藏 |
| `management.otlp.metrics.export.enabled` | `false` | `false` |

Jackson 当前为 `yyyy-MM-dd HH:mm:ss`、UTC、非空字段输出、未知入参字段不报错。starter 参考 yml 中的示例值不一定等于应用最终值，最终以 `application.yml` 为准。

## 本地 Compose 注意事项

`backend/travis-monolith/compose.yaml` 会启动 MySQL、Redis、RocketMQ NameServer/Broker/Proxy。MySQL 固定映射 `3306`，创建 `travis_monolith` 库和 `travis` 本地账号；Redis 固定映射 `6379`；RocketMQ Proxy 只映射 `8081`。这些值已与 dev Profile 对齐，应用仍使用默认 `8080`，不会与 Proxy 冲突。

Compose 内置账号、密码和 dev JWT 只适合本地开发，不可直接用于生产。Flyway 在 dev 默认关闭；如需在全新且已审阅的数据库上执行迁移，应显式设置 `SPRING_FLYWAY_ENABLED=true`。本项目的生产 Compose、凭据和网络边界见 [生产部署](../production-deployment.md)。

## 修改配置时的检查表

1. 配置类字段、yml 键、环境变量和文档是否一致；
2. dev/prod 是否都定义了必需差异，生产是否仍误用本地默认值；
3. Redis、Quartz、Modulith、RocketMQ 是否需要共享数据库或集群运行时验证；
4. 新增公共配置项后，是否更新本页和对应专题文档。
