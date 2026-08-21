# 文件管理与业务附件接入

文件模块提供文件夹、存储配置、上传、引用保护、删除通知和富文本图片处理。跨模块接入时使用 `SysFileApi` 等公开接口，不直接调用 file 模块的 `internal` 实现。

## 当前能力边界

| 能力 | 当前状态 |
| --- | --- |
| 本地磁盘存储 | 已实现，按 `yyyy/MM/dd` 分目录并生成 UUID 文件名 |
| 文件夹与存储配置管理 | 已实现，管理端可维护和启用存储配置 |
| S3、OSS、COS 等对象存储 | 仅在枚举中保留了注释示意，当前没有可用策略 |
| 扩展名白名单 | 已实现，由 `travis.web.file.allowed-extensions` 配置 |
| 文件引用检查 | 已实现扩展点，业务模块可阻止仍被引用的文件删除 |
| 上传主体展示 | 已实现扩展点，可按主体类型批量补充名称 |
| 富文本托管图片 | 已实现保存前去 URL、读取时补当前 URL |

不要仅增加一个存储类型枚举就认为对象存储已经接入；完整接入还需要实现 `FileStorageStrategy`、配置字段校验和管理端表单。

## 跨模块上传

注入公开 API：

```java
private final SysFileApi sysFileApi;

FileUploadResp result =
        sysFileApi.upload(file, folderId, "ORDER", orderId);
```

参数含义：

- `file`：不能为空的 `MultipartFile`；
- `folderId`：可选文件夹 ID；
- `uploaderType`：业务上传主体类型，由接入模块定义稳定字符串；
- `uploaderId`：该类型下的业务主体 ID。

`createBy` 是审计创建者，不等于业务归属。业务附件归属应写入 `uploaderType/uploaderId`，不要复用审计字段表达订单、患者等主体。

查询 URL 时使用 `getFileUrlById` 或批量的 `getFileUrlMapByIds`。数据库保存文件 ID 或模块约定的引用，不要把当前域名下的完整 URL 当作稳定业务标识。

## 上传限制和本地存储

上传同时受两层限制：

- Spring Multipart：`spring.servlet.multipart.max-file-size` 与 `max-request-size`；
- 文件模块：`travis.web.file.allowed-extensions`。

本地存储目录来自启用的文件存储配置的 `storagePath`，支持 Spring 占位符解析。`travis.web.file.resource-handler` 只定义对外静态资源路径，例如 `/files/**`，并不等于磁盘目录。

本地策略会拒绝无扩展名、异常扩展名和白名单外扩展名。仅校验扩展名不能代替内容安全扫描；接入不可信上传场景时需按业务风险增加病毒扫描、内容嗅探或隔离存储。

## 防止误删业务附件

业务模块若持有文件 ID，应实现公开扩展点 `SysFileReferenceChecker`。文件删除前，file 模块会询问所有检查器；仍有引用时应返回引用说明并阻止删除。

文件删除成功后会发布 `FileDeletedEvent`。需要清理本模块冗余数据时监听该事件，但不能用监听器代替删除前引用保护。

## 上传主体名称

需要在文件列表展示订单、用户等上传主体名称时，实现 `SysFileUploaderNameResolver`：

- 每个 resolver 负责一种 `uploaderType`；
- 批量接收 ID 并批量返回名称，避免逐行查库；
- file 模块不应反向依赖每个业务模块的内部 service。

## 富文本图片

编辑器中的系统文件图片应保留 `data-file-id`。保存业务数据前调用：

```java
String storedHtml = sysFileApi.stripManagedImageSources(html);
```

返回接口前调用：

```java
String displayHtml = sysFileApi.resolveManagedImageSources(storedHtml);
```

批量列表使用 `resolveManagedImageSources(List<String>)`，避免自行解析 HTML 或重复查询。这样更换域名、资源前缀后，历史富文本无需批量改库。

## 相关配置

文件访问路径、扩展名和 Multipart 限制见 [后端配置](configuration.md)。管理端上传组件的默认大小还受 `VITE_UPLOAD_FILE_MAX_SIZE` 影响；前端限制只用于交互提示，后端限制才是最终边界。
