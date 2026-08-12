# 数据脱敏使用说明

`travis-spring-boot-starter-desensitize` 提供统一的数据脱敏能力，支持 DTO 字段、对象、字符串、请求参数和 JSON。业务代码优先使用脱敏注解；需要主动处理数据时，构造注入 `Desensitizer`。

脱敏只改变输出内容，不会修改原对象中的字段值。脱敏失败时返回空结果，不向调用方继续抛出异常，也不会回退为未脱敏的原始内容。

## DTO 字段自动脱敏

在 DTO 字段或 record component 上添加对应注解。对象经过项目配置的 Jackson `ObjectMapper` 序列化时会自动脱敏。

```java
@Data
public class UserResp {

    @ChineseNameDesensitize
    private String nickname;

    @MobileDesensitize
    private String mobile;

    @EmailDesensitize
    private String email;

    @IdCardDesensitize
    private String idCard;

    @BankCardDesensitize
    private String bankCard;

    @PasswordDesensitize
    private String password;
}
```

Controller 可以正常返回 DTO，无需手动调用脱敏方法：

```java
return ApiResponse.success(userResp);
```

当前内置注解包括：

| 注解 | 用途 |
| --- | --- |
| `ChineseNameDesensitize` | 中文姓名 |
| `MobileDesensitize` | 手机号 |
| `FixedPhoneDesensitize` | 固定电话 |
| `EmailDesensitize` | 邮箱 |
| `IdCardDesensitize` | 身份证号 |
| `BankCardDesensitize` | 银行卡号 |
| `CarLicenseDesensitize` | 车牌号 |
| `PasswordDesensitize` | 密码 |
| `SliderDesensitize` | 按前后保留长度脱敏 |
| `RegexDesensitize` | 按正则表达式脱敏 |

## 注入统一脱敏执行器

需要主动脱敏时，构造注入 `Desensitizer`：

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final Desensitizer desensitizer;
}
```

不要自行创建重复的脱敏工具类，也不要通过 `SpringUtil` 静态获取脱敏组件。

## 主动脱敏对象

```java
String json = desensitizer.desensitizeObject(userResp);
```

对象脱敏会同时应用：

1. DTO 字段上的脱敏注解；
2. 默认敏感字段名兜底；
3. 嵌套对象和数组中的敏感字段名递归脱敏。

调用方可以追加业务敏感字段名：

```java
String json =
        desensitizer.desensitizeObject(
                userResp,
                Set.of("apiKey", "signKey"));
```

追加字段只扩充默认集合，不会覆盖默认敏感字段。

如果传入的是单个 `String`，由于字符串本身没有字段名或注解信息，`desensitizeObject` 会保持原值。单个字符串应明确指定脱敏规则。

## 按规则脱敏字符串

使用滑动规则保留手机号前三位和后四位：

```java
var rule = new SliderDesensitizeRule(3, 4, '*');

String mobile = desensitizer.desensitize("13800138000", rule);
// 138****8000
```

使用正则规则：

```java
var rule = new RegexDesensitizeRule("(?<=.{2}).(?=.{2})", "*");

String value = desensitizer.desensitize("ABCDEFGH", rule);
```

## 脱敏 JSON

### 已知目标类型

传入目标 DTO 类型后，会先应用 DTO 字段注解，再按敏感字段名递归兜底：

```java
String result = desensitizer.desensitizeJson(rawJson, UserLoginReq.class);
```

### 未知目标类型

不知道目标类型时传入 `null`，按字段名递归脱敏：

```java
String result = desensitizer.desensitizeJson(rawJson, null);
```

例如下面的嵌套 `token` 也会被处理：

```json
{
  "data": {
    "token": "******"
  }
}
```

### 追加敏感字段名

```java
String result =
        desensitizer.desensitizeJson(
                rawJson,
                null,
                Set.of("apiKey", "signKey"));
```

字段名匹配忽略大小写、下划线和连字符，以下名称会被视为同一字段：

```text
apiKey
api_key
api-key
API_KEY
```

## 脱敏参数 Map

`desensitizeParameters` 主要供 WebMVC 等基础设施根据 Controller 方法参数采集日志时使用：

```java
Map<String, String> result =
        desensitizer.desensitizeParameters(
                rawParameters,
                handlerMethod.getMethodParameters());
```

追加敏感字段名：

```java
Map<String, String> result =
        desensitizer.desensitizeParameters(
                rawParameters,
                handlerMethod.getMethodParameters(),
                Set.of("apiKey", "signKey"));
```

该方法会综合处理：

- Controller 简单参数上的脱敏注解；
- Controller DTO 参数的字段注解；
- 默认敏感字段名；
- 调用方追加的敏感字段名。

普通业务代码通常不需要直接调用这个方法。

## 自定义脱敏注解

业务中存在固定规则时，可以基于 `SliderDesensitize` 或 `RegexDesensitize` 定义组合注解：

```java
@Documented
@Target({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
@SliderDesensitize(prefix = 2, suffix = 2)
public @interface AccountDesensitize {

    int prefix() default 2;

    int suffix() default 2;

    char mask() default '*';

    String disable() default "";
}
```

使用方式：

```java
@AccountDesensitize
private String account;
```

如果要直接标记 Controller 的简单参数，需要自定义允许 `PARAMETER` 的组合注解：

```java
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@SliderDesensitize(prefix = 3, suffix = 4)
public @interface MobileParameterDesensitize {}
```

```java
@GetMapping
public void query(
        @MobileParameterDesensitize
        @RequestParam String mobile) {}
```

## 默认敏感字段

字段名经过忽略大小写、下划线和连字符的标准化后，与以下默认集合匹配：

```text
password
oldPassword
newPassword
confirmPassword
secret
secretKey
accessKey
token
accessToken
refreshToken
authorization
credential
credentials
privateKey
```

业务字段不在默认集合中时，通过对应方法的 `additionalSensitiveFields` 参数追加。

## 已自动接入的场景

以下场景已经由脚手架统一处理，业务代码无需重复调用：

- Controller 响应 DTO 的 Jackson 序列化；
- Access Log 的请求参数和请求体；
- 系统错误日志的请求快照；
- `OperationLogAspect` 记录的请求参数和响应结果。

## 失败策略

脱敏是日志和输出保护能力，失败不能阻断主业务：

| 方法 | 失败结果 |
| --- | --- |
| `desensitize` | `null` |
| `desensitizeObject` | `null` |
| `desensitizeJson` | `null` |
| `desensitizeParameters` | 空 Map |

调用方收到空结果后应省略对应日志内容，不能再次记录原始值。`OperationLogAspect` 会将对象脱敏失败记录为 `[无法序列化]`。
