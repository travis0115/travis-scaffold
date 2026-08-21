# 后端模块开发说明

本文说明如何在当前 Spring Modulith 单体中新增业务域、暴露跨模块能力和接入权限，重点是保持模块边界，避免直接依赖其他模块的内部实现。

## 先判断代码放在哪里

| 内容 | 位置 | 说明 |
| --- | --- | --- |
| 跨项目通用模型、校验或轻量端口 | `travis-infrastructure/travis-common` | 不依赖具体业务模块 |
| 可自动装配的技术能力 | `travis-infrastructure/travis-framework` | 形成 starter，业务模块只引入依赖和配置 |
| system、ops 等平台业务域 | `travis-module-platform` | 按业务域拆分 Modulith 模块 |
| 客户端接口及客户端适配 | `travis-module-app` | 当前整体是一个 `app` 模块 |
| 启动、环境配置、迁移脚本 | `travis-server` | 不放可复用业务逻辑 |

新增能力前先搜索 `travis-common`、已有 starter、目标模块的 `api`/`event` 和相似业务域。技术能力能通过自动配置复用时，不要在业务模块再次手工装配。

## 业务域目录约定

现有 system/ops 采用“业务域即 Modulith 模块”的结构：

```text
com.travis.monolith.system.example
├── api/                    # 对其他模块公开的接口、DTO、枚举、事件
│   ├── request/
│   ├── response/
│   └── event/
├── internal/               # Controller、Service、Mapper、Entity 等内部实现
│   ├── api/                # 公开 API 的默认实现
│   ├── controller/admin/   # 自动获得 /api/admin 前缀
│   ├── controller/app/     # 自动获得 /api/app 前缀
│   └── event/
└── package-info.java       # @ApplicationModule
```

- 模块间只能依赖公开 `api`、`event` 或标记为 `@NamedInterface` 的包。
- 不要 import 其他模块的 `internal`、entity、mapper 或 service 实现。
- 对外 API 只暴露 DTO/值对象，不暴露数据库实体。
- 公开包的 `package-info.java` 使用 `@NamedInterface` 声明稳定接口；模块根包用 `@ApplicationModule` 声明显示名称和允许依赖。

可以参考 `system/file` 的 `SysFileApi` 与 `internal/api/SysFileApiImpl`，以及 `system/message` 的 `SysMessageApi`。

## 跨模块查询与副作用

同步查询或必须立即得到结果的操作，通过目标模块的公开 API：

```java
@RequiredArgsConstructor
@Service
public class ExampleService {
    private final SysFileApi sysFileApi;

    public String resolveFileUrl(Long fileId) {
        return sysFileApi.getFileUrlById(fileId);
    }
}
```

一个业务状态变化需要通知其他模块时，优先发布公开事件：

1. 事件类型放在发布方的 `api/event`；
2. 通过 `TransactionalApplicationEventPublisher` 发布；
3. 消费方用 `@ApplicationModuleListener` 监听；
4. 监听器需考虑重复投递，按业务键保证幂等；
5. 只想在当前事务提交后执行本模块副作用时，可使用 `AfterCommitExecutor`。

事件投递和事务语义的详细用法见 [事件、消息与可观测性](events-messaging-observability.md)。

## 新增管理端接口

Controller 放在 `internal/controller/admin`，类上只写业务相对路径，`travis.web.apis` 会根据包名自动增加 `/api/admin`。客户端 Controller 同理放在 `internal/controller/app`，获得 `/api/app` 前缀。

接口应统一：

- 返回 `ApiResponse<T>`；
- 分页请求继承 `PageRequest`，分页结果使用 `PageResp<T>`；
- 参数使用 Jakarta Validation 和项目已有校验注解；
- 业务错误使用模块错误码并抛出 `BizException`；
- 有增删改等后台关键操作时使用现有操作日志注解。

## 权限接入

权限必须以后端校验为安全边界。新增后台操作时：

1. 在所属模块的权限常量类中增加权限码，例如 `SystemPermission` 或 `OpsPermission`；
2. Controller 使用 `@SaCheckPermission(value = ..., type = LoginType.ADMIN)`；
3. 数据库菜单/按钮的权限码与常量保持完全一致；
4. 管理端用路由权限、`v-access` 或 `AccessControl` 控制展示；
5. 不要因为前端隐藏按钮而省略后端权限校验。

一个操作需要多个权限时，显式配置注解的权限关系；不要在 service 内散落字符串权限码。

## 数据库和前端接入

- 表结构变更写入 `travis-server/src/main/resources/db/migration`，遵循该目录 [数据库迁移说明](../../backend/travis-monolith/travis-server/src/main/resources/db/README.md)。
- 新模块加入业务 BOM 和 `travis-server/pom.xml` 前，先参考现有 system/ops 模块的依赖方式。
- 管理端 API 类型、表单字段、列表 projection 与后端 DTO 一起核对，不要只凭页面字段推断接口契约。
- 前端列表、表单、操作列、字典和权限封装见 [管理端开发约定](../frontend/development.md)。

## 验证

项目已有 `travis-server/src/test/java/com/travis/monolith/server/ModulithTest.java`：

```bash
cd backend/travis-monolith
mvn test -pl travis-server -Dtest=ModulithTest
```

该测试通过 `ApplicationModules.verify()` 检查模块隔离。还应运行新增模块自己的测试；配置、数据库、Redis、MQ 或集群行为需要对应运行环境，编译通过不代表运行时已验证。
