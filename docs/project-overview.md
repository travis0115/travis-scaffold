# 项目能力总览

本文回答两个问题：脚手架现在能做什么，以及开发新功能前应该先复用哪里。

## 项目结构与边界

| 目录 | 职责 | 使用原则 |
| --- | --- | --- |
| `backend/travis-dependencies` | 全局 BOM 与第三方版本 | 统一管理版本，业务模块不要自行覆盖公共依赖版本 |
| `backend/travis-infrastructure/travis-common` | 与具体 starter 无关的公共契约 | 放响应模型、错误码、校验、事件端口等轻量 API |
| `backend/travis-infrastructure/travis-framework` | Spring Boot starters | 通过自动配置提供基础设施，不在业务模块重复装配 |
| `backend/travis-monolith/travis-module-platform` | system、ops 等平台业务模块 | 模块间只依赖公开 `api`、`event` 或 `@NamedInterface` |
| `backend/travis-monolith/travis-module-app` | 客户端接口与客户端侧适配 | `/api/app` 下的认证、用户与收件箱入口 |
| `backend/travis-monolith/travis-server` | 启动、环境配置与数据库迁移 | 不承载可复用业务实现 |
| `frontend/admin-vben/apps/travis-admin` | 项目管理端 | 项目 API、页面、adapter、权限与业务工具 |
| `frontend/admin-vben/packages` | Vben 共享包 | 上游通用能力；修改前先确认是否应留在应用层 |

Spring Modulith 使用显式模块检测。一个业务域的 `internal` 包属于实现细节；跨模块需要查询或触发行为时，先查目标模块的 `api` 接口和 `event` 包。

## 基础设施能力

| 场景 | 已有能力 | 应复用的入口 |
| --- | --- | --- |
| 统一接口返回与错误 | 成功/失败响应、业务异常、全局异常映射、i18n | `ApiResponse`、`BizException`、各模块错误码 |
| 参数校验 | 枚举、手机号、用户名、密码、图片、JSON 及 Jakarta Validation | `@EnumValue`、`@Mobile`、`@Username`、`@Password`、`@ImageFile`、`@JsonValue` |
| DTO 转换与分页 | MapStruct 全局配置、MyBatis 分页转响应 | `BaseMapperConfig`、`PageConverter`、`PageRequest`、`PageResp` |
| 数据访问 | 审计字段、逻辑删除、条件拼接、分页、乐观锁、防全表更新/删除 | `BaseEntity`、`BaseMapperX`、`ServiceImplX`、`LambdaQueryWrapperX` |
| JSON | 统一 ObjectMapper、时间格式、Long 安全输出、对象/数组转换 | `JsonUtil`、`JsonObject`、`JsonArray` |
| Redis 与缓存 | JSON RedisTemplate、项目 key 前缀、Spring Cache、按缓存 TTL | `RedisUtil`、`RedisKeyPrefixResolver`、`@Cacheable` |
| 并发与集群周期任务 | Redisson 分布式锁、集群内每周期成功一次 | `@DistributedLock`、`ClusterPeriodicTaskExecutor` |
| HTTP Web | Controller 包路径前缀、CORS、请求上下文、MDC、访问日志 | WebMVC starter 与 `travis.web.*` |
| 输入安全 | 重复提交拦截、富文本白名单清洗、敏感信息脱敏 | `@NoRepeatSubmit`、`@SanitizeHtml`、脱敏注解 |
| 多端认证 | admin/app 等多 `loginType` 的 JWT 鉴权 | `StpKit`、`travis.web.security.auth-rules` |
| 实时推送 | 多 WebSocket 端点、ticket、集群广播、在线状态 | `WebSocketMessageSender`、`WebSocketSessionListener` |
| 模块事件 | 事务内发布、提交后可靠监听、提交后副作用 | `TransactionalApplicationEventPublisher`、Spring Modulith、`AfterCommitExecutor` |
| MQ | 普通、顺序、延迟消息，异步回调，消费者模板 | `MessagePublisher`、`Message`、`AbstractEventListener` |
| 调度 | 后台动态任务、执行日志、一次性业务任务、集群对账 | `QuartzJobHandler`、`QuartzOneShotManager` |
| 错误治理 | HTTP、异步、调度、Quartz、MQ、WebSocket 异常统一上报 | `ErrorReporter`、`ErrorReporterContributor`、Ops 错误日志 |
| 操作与结构化日志 | 后台关键操作日志、访问日志、事件 JSON 日志 | `@OperationLog`、`@OperationLogModule`、`EventLoggerUtil` |

这些基础设施的具体用法见 [文档总目录](README.md)。

## 当前业务功能

### 系统管理

| 功能 | 当前能力 | 可供其他模块复用的入口/事件 |
| --- | --- | --- |
| 用户与认证 | admin 登录/退出、资料、密码、头像、角色、状态、在线数量；app 登录与用户查询 | `SysUserApi`、`UserLoginEvent`、`UserMessageAudienceChangedEvent` |
| 角色与权限 | 角色增删改查、启停、菜单授权、权限码 | `SysRoleApi`、`RoleMessageAudienceChangedEvent`、`SystemPermission` |
| 部门 | 树结构、启停、删除联动 | `SysDeptApi`、`DeptDeletedEvent` |
| 菜单 | 目录/菜单/按钮、动态路由与权限码 | `SysMenuApi` |
| 字典 | 字典与字典项维护、状态、标签样式、前端缓存 | 当前主要通过管理接口和前端 `getDictOptions` 使用 |
| 参数配置 | 配置分页维护与按 key 读取 | `SysConfigApi` |
| 文件 | 文件夹、存储配置、本地存储、上传策略、引用检查、删除事件 | `SysFileApi`、`SysFileReferenceChecker`、`SysFileUploaderNameResolver`、`FileDeletedEvent` |
| 公告与版本 | 富文本、图片上传、发布/撤回、置顶、已发布查询 | 发布后进入消息/收件箱链路 |
| 消息 | 模板、手动/定时推送、撤回、收件箱、未读、WebSocket 通知、多接收人类型 | `SysMessageApi`、`SysMessageInboxApi` |
| 登录/操作日志 | 登录结果、客户端信息、后台关键操作审计 | `UserLoginEvent`、`OperationLogEvent`；业务 Controller 优先使用操作日志注解 |

### 运维管理

| 功能 | 当前能力 | 说明文档 |
| --- | --- | --- |
| 任务调度 | CRON/固定间隔/单次任务、启停、立即执行、预览、统计、日志、内置任务 | [Quartz 调度任务](backend/quartz.md) |
| 错误日志 | 聚合异常、发生明细、上下文、处理、全部处理、删除 | [事件、消息与可观测性](backend/events-messaging-observability.md) |

### 管理端

管理端已经覆盖上述 system、ops 功能，并封装了统一请求、动态菜单、权限指令、字典翻译、表单、VXE Grid、操作列、状态切换、图片/头像单元格和空值展示。新增列表页前先看 [管理端开发约定](frontend/development.md)，不要在页面内重新实现同类 renderer 或请求错误处理。

## 已补齐的业务接入专题

- [后端模块开发](backend/module-development.md)：模块边界、公开 API、事件、权限与验证；
- [后端配置项参考](backend/configuration.md)：后端 yml、starter、环境变量和 Compose；
- [前端配置](frontend/configuration.md)：管理端 `.env`、API、WebSocket 和构建变量；
- [文件管理与业务附件接入](backend/file-management.md)：上传归属、引用保护、删除事件和富文本图片；
- [系统消息与收件箱](backend/system-message.md)：站内信、来源消息、收件箱与渠道边界。

公告/版本已经通过来源消息和文件富文本专题说明其跨模块接入点；字典/参数配置和新增管理页面的日常使用已收敛在 [管理端开发约定](frontend/development.md)。若后续这些模块增加独立扩展 API 或复杂生命周期，再拆分专题，避免仅按菜单机械生成文档。

## 开发前检查表

新增封装前至少搜索以下范围：

1. `travis-common` 是否已有模型、注解、端口或工具；
2. 对应 starter 是否已有自动配置和稳定入口；
3. 目标业务模块的 `api`、`event` 与 `@NamedInterface`；
4. 管理端 `adapter`、`utils`、`api/types.ts` 与相似页面；
5. 本目录是否已经说明相同能力及其边界。
