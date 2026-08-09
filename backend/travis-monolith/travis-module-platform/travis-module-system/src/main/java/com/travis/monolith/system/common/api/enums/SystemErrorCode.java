package com.travis.monolith.system.common.api.enums;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import lombok.AllArgsConstructor;

/**
 * 业务状态码枚举
 *
 * <p>模块前缀 + 模块内唯一 例如：SYS_001
 */
@AllArgsConstructor
public enum SystemErrorCode implements ErrorCode {

    /* user模块 000-099 */
    USER_OLD_PASSWORD_ERROR("SYS_000", "原密码错误"),
    USER_USERNAME_EXISTS("SYS_001", "用户名已存在"),

    /* role模块 100-199 */
    ROLE_BUILTIN_NOT_DELETABLE("SYS_100", "系统内置角色不允许删除"),
    ROLE_NOT_MODIFIABLE("SYS_101", "该角色不允许修改或删除"),
    ROLE_CODE_EXISTS("SYS_102", "角色编码已存在"),
    ROLE_ADMIN_BUILTIN_MODIFIABLE_ONLY("SYS_103", "系统内置数据不允许修改"),

    /* dept模块 200-299 */
    DEPT_PARENT_INVALID("SYS_200", "上级部门不能是当前部门或其下级部门"),

    /* menu模块 300-399 */
    MENU_PATH_EXISTS("SYS_300", "菜单路由路径已存在"),
    MENU_PARENT_INVALID("SYS_301", "上级菜单不能是当前菜单或其下级菜单"),

    /* dict模块 400-499 */
    DICT_CODE_EXISTS("SYS_400", "字典编码已存在"),

    /* errorlog模块 500-599 */
    /* loginlog模块 600-699 */
    /* operationlog模块 700-799 */
    /* versionlog模块 800-899 */
    VERSION_EXISTS("SYS_800", "版本号已存在"),

    /* message模块 900-999 */
    MESSAGE_TEMPLATE_BUILTIN_NOT_DELETABLE("SYS_900", "系统内置消息模板不允许删除"),
    MESSAGE_CHANNEL_UNAVAILABLE("SYS_901", "当前推送通道暂不可用"),
    MESSAGE_CHANNEL_REVOKE_NOT_SUPPORTED("SYS_902", "当前推送通道不支持撤回"),
    MESSAGE_NOT_FOUND("SYS_903", "未找到消息"),

    /* file模块 1000-1099 */
    FILE_STORAGE_DEFAULT_REQUIRED("SYS_1000", "系统内必须存在默认存储配置"),
    FILE_STORAGE_DEFAULT_NOT_DELETABLE("SYS_1001", "默认存储配置不允许删除或禁用"),
    FILE_STORAGE_IN_USE("SYS_1002", "存储配置已被文件使用，不允许删除"),
    FILE_STORAGE_NOT_FOUND("SYS_1003", "未找到存储配置"),
    FILE_FOLDER_BUILTIN_NOT_DELETABLE("SYS_1100", "系统内置文件夹不允许删除"),
    FILE_UPLOAD_FAILED("SYS_1200", "文件上传失败"),
    FILE_UPLOAD_EXTENSION_NOT_ALLOWED("SYS_1201", "不支持上传该文件类型"),

    /* config模块 1100-1199 */
    CONFIG_BUILTIN_NOT_DELETABLE("SYS_1100", "系统内置配置不允许删除"),
    CONFIG_KEY_EXISTS("SYS_1101", "配置键已存在"),
    ;

    /** 错误码。 */
    private final String code;

    /** 错误消息模板。 */
    private final String msg;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
