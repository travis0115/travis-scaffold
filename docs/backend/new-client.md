# 新增客户端/登录端指南

V1 已提供 `admin` 和 `app` 两个登录体系。`app` 是基础适配示例，用于展示客户端账号、认证、消息收件箱、文件主体和 WebSocket 的完整边界；会员、积分、支付、注册、验证码登录、找回密码和微信扫码登录不属于 V1 基础能力。

如果新产品只是 App 的另一个界面或渠道，优先复用 `app`，不要为每个前端都新增 loginType。只有账号主体、Token 隔离、权限模型或生命周期确实不同，才新增登录端。

## 需要修改的范围

### 1. 稳定标识与鉴权规则

1. 在 `LoginType` 增加稳定、不可变的常量，并纳入支持集合；
2. 在 `travis.web.security.auth-rules` 增加 `/api/<client>/**`、登录排除路径和可选 `/ws/<client>`；
3. 在 `travis.web.apis` 增加 Controller 包到 API 前缀的映射；
4. 所有登录、取当前主体、退出和权限检查都使用 `StpKit.of(LoginType.<CLIENT>)`，禁止混用 admin/app Token。

公开路径只能保留实际存在的登录/刷新接口。前端隐藏入口不是安全边界，不要把尚未实现的注册、验证码、找回密码或扫码接口加入排除列表。

### 2. 账号与认证

建立独立账号实体/表、Mapper、Service 和登录请求/响应。登录流程至少覆盖：

- 标准化用户名后调用 `LoginProtectionApi.checkAllowed`；
- 不论账号不存在还是密码错误，都记录一次失败且返回同类错误，避免账号枚举；
- 成功后清理账号失败计数，记录登录日志，再签发对应 loginType 的 Token；
- 状态禁用、逻辑删除和密码策略；
- 日志不记录密码、Token、完整用户名或客户端 Secret。

为 Nginx 增加新登录路径的独立 IP 限流区，并为应用层增加 `security.login.<client>.*` 默认值或明确复用策略。

### 3. 权限与公开 API

- 如果客户端需要权限，提供对应的角色/权限查询，并扩展 `StpInterface`；不应读取 system 模块的 internal 实现。
- 跨模块查询通过目标模块公开 `api`、`event` 或 `@NamedInterface`。
- Controller 放在新端对应的 `internal/controller/<client>` 包，返回统一 `ApiResponse`，写操作做后端权限校验和必要的操作审计。
- 在 Spring Modulith 验证中确认没有跨模块依赖其他模块 `internal` 包。

### 4. WebSocket

如需实时通信：

1. 配置独立 `websocket-path`；
2. 提供 `SaTokenWebSocketSubjectValidator` 校验该账号仍存在且可用；
3. 提供已登录的 `ws-ticket` 申请接口；
4. principal 使用 `<loginType>:<loginId>`；
5. 消息按 namespace 或 principal 投递，客户端实现断线重连。

不要把长期 Token 放进 WebSocket URL。

### 5. 消息与文件

- 消息：实现该客户端的接收目标校验、收件箱查询和未读行为；确认模板受众、撤回和来源消息生命周期。
- 文件：实现 `SysFileUploaderNameResolver` 和需要时的 `SysFileReferenceChecker`；上传主体类型与主体 ID 必须来自服务端当前登录态，不能信任请求传入。
- 删除文件前必须经过所有业务引用检查；业务数据删除后再发布对应解除引用事件。

### 6. 前端或客户端

- 独立配置 API 前缀、Token 存储键、刷新/退出策略和 WebSocket 地址；
- 统一处理 401、403、429，429 不自动高频重试；
- 路由隐藏只影响展示，不能替代后端鉴权；
- API 类型与后端 DTO 一起核对，敏感字段不得出现在响应类型；
- 注册、验证码、找回密码、扫码登录只有后端闭环完成后才开放入口。

## 验收清单

- [ ] 新旧 loginType 的 Token 不能交叉访问；
- [ ] 登录成功、错误密码、账号不存在、禁用、账号/IP 临时锁和 Redis 故障均有测试；
- [ ] 未登录路径只有明确白名单，所有写接口有后端权限检查；
- [ ] WebSocket ticket 一次性消费，主体失效后不能建立连接；
- [ ] 消息受众、未读、撤回和文件上传归属/引用保护正确；
- [ ] 日志、响应、前端状态和错误上报不包含密钥、密码或 Token；
- [ ] Modulith、后端测试、前端类型/Lint/测试/构建通过；
- [ ] Nginx 登录限流、真实 IP、HTTPS、多实例与客户端重连完成运行时验证；
- [ ] 同步更新根 README、文档导航、配置参考、生产部署和 CHANGELOG。
