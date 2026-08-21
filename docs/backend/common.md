# 后端公共基础使用说明

本文覆盖 `travis-common` 中日常业务开发应直接复用的公共契约。

## 统一响应与业务异常

Controller 明确返回 `ApiResponse<T>`：

```java
@GetMapping("/{id}")
public ApiResponse<UserResp> get(@PathVariable Long id) {
    return ApiResponse.success(userService.get(id));
}
```

业务失败抛出带模块错误码的 `BizException`，不要在每个 Controller 中重复 `try/catch`：

```java
if (user == null) {
    throw new BizException(SystemErrorCode.USER_NOT_FOUND);
}
```

WebMVC starter 已统一处理业务异常、参数校验异常和未预期服务端异常。`ApiResponse.error(...)` 主要供异常处理器或确实需要直接构造失败响应的边界使用。

错误码规则：

- 通用 HTTP/校验/数据库/缓存错误使用 `CommonErrorCode`；
- 业务模块定义自己的错误码枚举并实现 `ErrorCode`；
- 错误消息可以使用 `{0}`、`{1}` 参数，由响应 advice 格式化或国际化；
- 不要用成功码 `200` 构造失败响应。

## 分页

分页请求 DTO 继承 `PageRequest`：

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReq extends PageRequest {
    private String username;
}
```

默认页码为 1、每页 10 条，每页允许 1～200 条。Controller 参数需要 `@Valid`：

```java
@GetMapping("/page")
public ApiResponse<PageResp<UserResp>> page(@Valid UserPageReq req) {
    return ApiResponse.success(userService.page(req));
}
```

MyBatis-Plus 的 `IPage<T>` 可以使用 `PageConverter.toResp(page)` 转为统一 `PageResp<T>`。如果分页记录还需要 DTO 转换，先完成记录转换，再构造 `PageResp`，不要把数据库实体直接暴露给前端。

### 排序安全

`PageRequest.orderBy` 来自客户端，不允许直接拼进 SQL。Lambda 查询优先使用 `orderByAllowed` 将前端字段映射到白名单列：

```java
wrapper.orderByAllowed(
        req.getOrderBy(),
        req.getAsc(),
        Map.of("createTime", UserEntity::getCreateTime),
        false,
        UserEntity::getCreateTime);
```

未知字段自动回退到默认排序，不应使用 `${orderBy}`。

## 参数校验

公共校验注解：

| 注解 | 支持的值 | 空值行为 |
| --- | --- | --- |
| `@EnumValue(MyEnum.class)` | 枚举公开 `getValue()` 返回的值；枚举必须提供该方法 | 需要必填时叠加 `@NotNull`/`@NotBlank` |
| `@JsonValue` | 默认要求 JSON 对象；可选 `OBJECT`、`ARRAY`、`ANY` | 需要必填时叠加非空校验 |
| `@Mobile` | 中国大陆手机号 | 需要必填时叠加 `@NotBlank` |
| `@Username` | 系统用户名格式 | 需要必填时叠加 `@NotBlank` |
| `@Password` | 8～32 位，四类字符至少三类 | 需要必填时叠加 `@NotBlank` |
| `@ImageFile` | 支持的图片上传文件 | 需要必传时叠加 `@NotNull` |

示例：

```java
public record JobCreateReq(
        @NotBlank String name,
        @EnumValue(OpsJobStatus.class) Integer status,
        @JsonValue String params) {}
```

跨字段规则用 DTO 自身的 `@AssertTrue` 或类级约束表达，并为校验方法添加 `@JsonIgnore`，避免被序列化为响应字段。Service 仍需校验必须查库或会产生副作用的业务规则。

## MapStruct

Converter 统一引用 `BaseMapperConfig`，无需重复声明 Spring component model 和未映射字段策略：

```java
@Mapper(config = BaseMapperConfig.class)
public interface UserConverter {
    UserResp toResp(UserEntity entity);
}
```

需要依赖其他 Converter 时使用 `uses`。复杂业务查库、权限判断或副作用不要写进 MapStruct 映射。

## 事务提交后的动作

仅需要在当前事务成功提交后执行一个本地副作用时，使用 `AfterCommitExecutor`：

```java
AfterCommitExecutor.execute(() -> searchIndexer.refresh(entity.getId()));
```

- 当前存在事务时：成功提交后执行；回滚不执行。
- 当前没有事务时：立即执行。
- 它不是持久化队列，进程在提交后、执行前退出时不会自动恢复。
- 需要可恢复的模块事件，使用 Spring Modulith；需要跨系统、顺序或延迟投递，使用 MQ。

WebSocket 发送、部分缓存删除和 Quartz 同步已经在各自封装中处理了提交时机，调用方不要再套一层重复的 `AfterCommitExecutor`。

## 操作日志

后台关键写操作在 Controller 上声明模块和动作：

```java
@RestController
@OperationLogModule("用户管理")
public class SysUserController {

    @PostMapping
    @OperationLog(action = "新增用户", businessType = OperationBusinessType.CREATE)
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateReq req) {
        return ApiResponse.success(userService.create(req));
    }
}
```

`businessType` 默认 `AUTO`，也支持 `CREATE`、`UPDATE`、`DELETE`、`GRANT`、`UPLOAD`、`IMPORT`、`EXPORT`、`OTHER`。敏感或超大请求/响应可通过 `recordRequest`、`recordResponse` 关闭采集。

操作日志注解描述的是管理员可审计的业务动作，不用于普通查询，也不替代业务日志与错误上报。

## 常量与上下文

- 登录类型统一使用 `LoginType`，当前主要是 `admin`、`app`；
- 请求、用户、链路字段使用 `MdcKey`，不要自行发明同义 MDC key；
- 过滤器顺序使用 `WebFilterOrder`；
- 自定义异常处理器顺序使用 `ExceptionHandlerOrder`；
- HTTP 头名称优先查 `HttpHeader`。
