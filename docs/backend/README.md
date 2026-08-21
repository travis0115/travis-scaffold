# 后端文档

后端基于 Spring Boot、Spring Modulith 和 Maven 多模块组织。先读模块开发与配置，再按具体能力进入专题。

## 建议阅读顺序

1. [模块开发](module-development.md)：代码放置、公开 API、事件、权限和模块验证；
2. [后端配置](configuration.md)：`application*.yml`、环境变量、starter 前缀和 Compose；
3. [公共基础](common.md)：统一响应、分页、异常、校验与 MapStruct；
4. 再按当前需求选择数据、Web、事件、文件、消息或 Quartz 专题。

## 按需求查找

| 我想做什么 | 文档 | 主要复用入口 |
| --- | --- | --- |
| 新增业务模块、跨模块 API、事件或权限 | [模块开发](module-development.md) | `@ApplicationModule`、`@NamedInterface`、`@ApplicationModuleListener` |
| 配置环境、数据库、Redis、MQ、Quartz、WebSocket | [后端配置](configuration.md) | `application*.yml`、`travis.*`、环境变量 |
| 返回统一结果、分页、抛业务异常、校验和转换 | [公共基础](common.md) | `ApiResponse`、`PageRequest`、`BizException`、`BaseMapperConfig` |
| 使用 MyBatis、JSON、Redis、缓存和分布式锁 | [数据访问与缓存](data-and-cache.md) | `BaseMapperX`、`JsonUtil`、`RedisUtil`、`DistributedLock` |
| 新增 HTTP API、认证、输入清洗或 WebSocket | [Web、认证与 WebSocket](web-auth-websocket.md) | WebMVC starter、`StpKit`、`WebSocketMessageSender` |
| 使用模块事件、RocketMQ、日志和错误上报 | [事件、消息与可观测性](events-messaging-observability.md) | `TransactionalApplicationEventPublisher`、`MessagePublisher`、`ErrorReporter` |
| 上传附件、保护引用和处理富文本图片 | [文件管理](file-management.md) | `SysFileApi`、引用检查器、上传主体解析器 |
| 发布站内信、来源消息和接入收件箱 | [系统消息与收件箱](system-message.md) | `SysMessageApi`、`SysMessageInboxApi` |
| 创建后台动态任务或业务一次性任务 | [Quartz 调度任务](quartz.md) | `QuartzJobHandler`、`QuartzOneShotManager` |
| 对 DTO、JSON、参数或日志脱敏 | [数据脱敏](desensitization.md) | `Desensitizer` 与脱敏注解 |

## 按 starter 查找

| starter | 文档 |
| --- | --- |
| desensitize | [数据脱敏](desensitization.md) |
| event | [事件、消息与可观测性](events-messaging-observability.md) |
| jackson | [数据访问与缓存](data-and-cache.md) |
| logging / monitor | [事件、消息与可观测性](events-messaging-observability.md) |
| mybatis | [数据访问与缓存](data-and-cache.md) |
| quartz | [Quartz 调度任务](quartz.md) |
| redis | [数据访问与缓存](data-and-cache.md) |
| rocketmq | [事件、消息与可观测性](events-messaging-observability.md) |
| sa-token | [Web、认证与 WebSocket](web-auth-websocket.md) |
| webmvc / websocket | [Web、认证与 WebSocket](web-auth-websocket.md) |

返回 [文档总入口](../README.md)。
