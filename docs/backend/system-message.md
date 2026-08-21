# 系统消息与收件箱使用说明

消息模块提供站内信模板、手动/定时推送、公告和版本来源消息、撤回、收件箱、未读数与 WebSocket 变更通知。业务模块应通过 `SysMessageApi` 发布，不直接操作消息表或内部 service。

## 当前能力边界

| 能力 | 当前状态 |
| --- | --- |
| `IN_APP` 站内信 | 已实现完整发送处理器、收件箱和撤回 |
| `SMS`、`WECHAT_MP`、`WECHAT_OA` | 枚举和模板字段已预留，但当前没有对应发送处理器 |
| admin 接收端 | 支持全部用户、指定用户、角色、部门 |
| app 接收端 | 只支持全部用户或指定用户 |
| 手动、自动、定时发布 | 已实现；定时任务通过 Quartz 一次性任务和周期对账保证 |
| 公告/版本来源消息 | 支持创建或更新、撤回、删除 |

在新增短信或微信 handler 之前，不要把预留渠道用于生产发送。

## 直接向用户发站内信

```java
sysMessageApi.publishToUsers(
        LoginType.ADMIN,
        "审核结果",
        "<p>您的申请已通过</p>",
        userIds);
```

该入口固定创建 `IN_APP`、指定用户、手动来源消息，并立即自动推送。内容会经过富文本清洗；用户 ID 集合为空时直接返回。单次最多 1000 个用户，标题最多 255 字符，内容清洗后最多 5000 字符。

`receiverType` 使用 `admin` 或 `app`，不要用角色名、用户类型或页面端名称代替登录体系。

## 发布公告或版本来源消息

公告、版本等具有独立业务生命周期的内容使用 `publishSourceMessage`。核心字段：

| 字段 | 说明 |
| --- | --- |
| `messageType` | 必须与来源对应：公告对应 NOTICE，版本对应 VERSION |
| `sourceType` | 当前只允许 `NOTICE` 或 `VERSION` |
| `sourceId` | 来源业务 ID，最长 64 字符 |
| `receiverType` | `admin` 或 `app` |
| `receiverScope` | 全部、用户、角色、部门；app 只允许全部或用户 |
| `receiverValues` | 非全部范围必填，最多 1000 个 ID |
| `publishTime` | 不晚于当前时间则立即发布，未来时间进入定时待发送 |

同一 `sourceType + sourceId + receiverType` 会创建或更新同一来源消息。来源撤回和删除分别调用：

```java
sysMessageApi.revokeSourceMessage(sourceType, sourceId, receiverType);
sysMessageApi.deleteSourceMessage(sourceType, sourceId, receiverType);
```

这两个入口同样只接受公告或版本来源。业务自己的普通通知不要伪装成 NOTICE/VERSION；需要新的来源生命周期时，应增加明确的枚举、校验和业务契约。

## 收件箱与实时通知

客户端和管理端收件箱通过公开的 `SysMessageInboxApi` 或对应 Controller 查询列表、详情、未读数，并执行已读操作。接收人变化后会清理未读缓存。

WebSocket 推送的是收件箱发生变化的事件，用于让客户端刷新未读数或列表；不要假定每次通知都携带完整消息正文。连接认证、命名空间和集群广播见 [Web、认证与 WebSocket](web-auth-websocket.md)。

## 定时发布和可靠性

未来发布时间的消息会创建 Quartz 一次性触发器；周期对账会补偿数据库待发送记录与 Quartz 状态不一致的情况。应用集群共享 Quartz JDBC JobStore，因此必须保证实例连接同一数据库，并正确配置 Redis 与 Quartz 表。

消息调度使用稳定名称和内部管理器，业务模块不要自行拼 Quartz job/trigger key。通用业务一次性任务的接入方式见 [Quartz 调度任务](quartz.md)。

## 模板和渠道扩展

模板负责可复用的标题、内容和渠道参数。使用模板时先确认变量完整，最终内容仍需经过渠道自己的校验与安全处理。

新增渠道至少需要：

1. 实现 `SysMessageChannelHandler` 并注册稳定渠道值；
2. 明确发送成功、失败与重试语义；
3. 补齐模板字段校验和敏感配置；
4. 说明是否支持撤回、跳转 URL 和收件箱；
5. 增加运行环境验证，不能只验证枚举和表单可选。
