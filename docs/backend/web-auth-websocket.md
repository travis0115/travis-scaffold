# Web、认证与 WebSocket 使用说明

## 新增 HTTP API

Controller 放入约定包即可自动获得 API 前缀：

```text
*.internal.controller.admin.* -> /api/admin
*.internal.controller.app.*   -> /api/app
```

路径来自配置，而不是写死在 Controller：

```yaml
travis:
  web:
    apis:
      - prefix: /api/admin
        package-pattern: controller.admin
      - prefix: /api/app
        package-pattern: controller.app
```

Controller 自身只声明业务路径并返回 `ApiResponse<T>`：

```java
@RestController
@RequestMapping("/order")
public class OrderController {
    @GetMapping("/{id}")
    public ApiResponse<OrderResp> get(@PathVariable Long id) { ... }
}
```

不要在 `@RequestMapping` 再写 `/api/admin`。包名匹配是包含匹配，新增相似包名时应避免误命中已有规则。

## WebMVC 已有能力

引入 WebMVC starter 后自动获得：

- Query/Form 的 `LocalDateTime` 转换，格式跟随 `spring.jackson.date-format`；
- 业务、校验和服务端异常统一处理；
- 请求 ID、用户与链路 MDC；
- 请求体缓存和访问日志使用同一请求上下文；
- API 响应消息格式化/i18n；
- CORS；
- 防重复提交与富文本白名单清洗。

`travis.web.request-cache-limit` 默认 256 KiB，只限制基础设施为日志/异常快照缓存的请求体大小，不是上传或 Servlet 请求体上限。上传大小由 `spring.servlet.multipart.*` 控制。

## CORS 与国际化

CORS 使用 `travis.web.cors`：

```yaml
travis:
  web:
    cors:
      allowed-origin-patterns: ['https://admin.example.com']
      allowed-methods: ['GET', 'POST', 'PUT', 'DELETE']
      allowed-headers: ['*']
      allow-credentials: true
      exposed-headers: [Authorization, Content-Disposition, X-Request-Id]
      max-age: 3600
```

生产环境不建议在 `allow-credentials=true` 时长期保留任意来源模式。

启用 `travis.web.i18n.enabled=true` 后，根据 `Accept-Language` 翻译 `ApiResponse.code`；请求未携带语言时回退到 `spring.web.locale`。项目资源文件优先于 starter 的 `i18n/travis_messages`，因此业务可覆盖同 code 文案。

## 防重复提交

写接口需要阻止同一主体短时间重复操作时使用：

```java
@PostMapping
@NoRepeatSubmit(key = "#req.orderNo", interval = 5)
public ApiResponse<Long> create(@Valid @RequestBody OrderCreateReq req) { ... }
```

- key 支持方法参数 SpEL；为空时使用可序列化参数摘要；
- Redis key 同时包含 Controller 方法、HTTP 方法、URI、登录用户（未登录时为 IP）和业务 key；
- `deleteOnFailure=true` 时业务抛错会释放本次防重 key，允许立即重试；
- Redis starter 不存在时不会装配防重切面；
- 这是短时间请求防抖，不替代数据库幂等键。

项目级 key 由 `travis.redis.key-prefix` 加 `travis.web.no-repeat-submit.key-prefix` 组成。

## 富文本输入清洗

只对允许 HTML 的 DTO 字段添加 `@SanitizeHtml`：

```java
public record NoticeCreateReq(
        @NotBlank String title,
        @SanitizeHtml String content) {}
```

反序列化时使用 Jsoup 白名单清洗，保留常见段落、强调、列表、标题、链接、图片、代码和表格标签。链接只允许 `http`、`https`、`mailto`，图片只允许 `http`、`https`，并为链接强制添加安全 `rel`。

普通纯文本字段不要依赖清洗器，应按业务校验和正确输出编码处理。清洗只处理 HTML，不负责 SQL 参数化、鉴权或文件引用权限。

## 多登录类型认证

当前通过 `travis.web.security.auth-rules` 声明 admin/app：

```yaml
travis:
  web:
    security:
      auth-rules:
        - login-type: admin
          path-patterns: [/api/admin/**]
          exclude-path-patterns: [/api/admin/system/auth/login]
          websocket-path: /ws/admin
        - login-type: app
          path-patterns: [/api/app/**]
          exclude-path-patterns: [/api/app/auth/login]
          websocket-path: /ws/app
```

starter 为每个唯一 `loginType` 创建 JWT `StpLogic` 并注册独立拦截器。业务统一通过：

```java
StpKit.of(LoginType.ADMIN).login(userId);
long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
StpKit.of(LoginType.ADMIN).checkPermission("system:user:update");
```

新增登录类型时同时：

1. 在 `auth-rules` 增加规则；
2. 在 `LoginType` 增加稳定常量；
3. 提供对应登录接口和主体查询逻辑；
4. 如需 WebSocket，配置独立 `websocket-path`。

前端隐藏按钮不是安全边界，写接口仍需后端权限校验。

## WebSocket 连接

Sa-Token 适配使用一次性短期 ticket，不把长期 token 直接放在 WebSocket URL。当前 admin 和 app 都有 `POST .../auth/ws-ticket` 接口。流程：

1. 已登录 HTTP 客户端申请 ticket；
2. 客户端在 60 秒内连接 `/ws/admin?ticket=...` 或 `/ws/app?ticket=...`；
3. ticket 消费后立即删除，不能重放；
4. principal 由适配层构造为 `loginType:loginId`，例如 `admin:123`。

凭证参数名可用 `travis.web.websocket.credential-key` 修改，但申请接口和客户端必须保持一致。

## 发送消息

业务层注入 `WebSocketMessageSender`，不要直接操作本地 Session：

```java
var principal = "admin:" + userId;
var message = WebSocketMessage.toPrincipal(
        WebSocketSender.SYSTEM, principal, Map.of("event", "order-updated", "id", orderId));
webSocketMessageSender.sendToPrincipal(principal, message);
```

也支持：

```java
webSocketMessageSender.sendToNamespace(
        LoginType.ADMIN,
        WebSocketMessage.toNamespace(
                WebSocketSender.SYSTEM, LoginType.ADMIN, event, content));

webSocketMessageSender.sendToAll(WebSocketMessage.toAll("system", content));
```

发送在事务内调用时自动延迟到成功提交后执行。发送失败会记录并上报错误，不回滚已经提交的业务事务。

`WebSocketEvent` 适合把稳定事件名放入统一 `content.event`。点对点消息的 `message.to` 应与方法传入的 principal 保持一致，便于日志和前端调试。

## 会话与集群

配置：

```yaml
travis:
  web:
    websocket:
      enabled: true
      heartbeat-interval: 30000
      session-timeout: 300000
      offline-grace-period: 15000
      redis:
        enabled: true
        channel: websocket:channel:broadcast
        session-key-prefix: websocket:session
        retry-interval: 30000
```

Redis starter 可用且 `redis.enabled=true` 时，点对点、命名空间、广播、关闭连接和在线查询覆盖集群；否则自动以单实例模式运行。`offline-grace-period` 用于避免刷新页面造成瞬时离线事件。

业务需要关注首个连接建立和最后连接断开时，实现 `WebSocketSessionListener`。回调只发生在实际持有连接的实例；如果在线事件需要全局可靠传播，在回调中发布 MQ，而不是假设所有实例都会收到本地回调。

## 自定义端点或认证

大多数业务只需在 Sa-Token `auth-rules` 配置端点。独立认证协议可以提供：

- `WebSocketAuthService`：校验握手并生成 principal/attributes；
- `WebSocketEndpoint` 或 `WebSocketEndpointProvider`：声明一个或多个路径及固定属性；
- 可选 `WebSocketSessionListener`：监听主体级上下线。

没有 `WebSocketAuthService` 或没有端点时，starter 会跳过端点注册，不会开放匿名 WebSocket。
