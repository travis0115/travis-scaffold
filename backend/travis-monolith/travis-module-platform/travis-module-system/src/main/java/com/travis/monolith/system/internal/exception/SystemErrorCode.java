package com.travis.monolith.system.internal.exception;

import com.travis.infrastructure.common.web.exception.IErrorCode;
import lombok.AllArgsConstructor;

/**
 * 业务状态码枚举
 *
 * <p>模块前缀 + 模块内唯一 例如：USER_0001
 */
@AllArgsConstructor
public enum SystemErrorCode implements IErrorCode {

    /** System模块 */
    SYSTEM_AUTH_LOGIN_BAD_CREDENTIALS("SYS_0001", "用户名或密码错误"),
    SYSTEM_AUTH_LOGIN_USER_DISABLED("SYS_0002", "账号已被禁用"),
    SYSTEM_USER_OLD_PASSWORD_ERROR("SYS_0003", "旧密码错误"),
    SYSTEM_USER_USERNAME_EXISTS("SYS_0004", "用户名已存在"),
    SYSTEM_ROLE_CODE_EXISTS("SYS_0005", "角色编码已存在"),
    SYSTEM_DICT_TYPE_EXISTS("SYS_0006", "字典类型编码已存在"),
    SYSTEM_DEPT_HAS_USERS("SYS_0007", "部门下存在关联用户，无法删除"),
    SYSTEM_MENU_HAS_CHILDREN("SYS_0008", "存在子菜单，无法删除"),
    SYSTEM_MENU_ALREADY_TOP("SYS_0009", "已处于最上方，无法上移"),
    SYSTEM_MENU_ALREADY_BOTTOM("SYS_0010", "已处于最下方，无法下移"),
    ;

    private final String code;

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
