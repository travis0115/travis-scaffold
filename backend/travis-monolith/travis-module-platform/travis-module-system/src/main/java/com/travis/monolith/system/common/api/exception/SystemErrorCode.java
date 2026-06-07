package com.travis.monolith.system.common.api.exception;

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
    SYSTEM_USER_OLD_PASSWORD_ERROR("SYS_001", "原密码错误"),
    SYSTEM_USER_USERNAME_EXISTS("SYS_002", "用户名已存在"),
    SYSTEM_ROLE_CODE_EXISTS("SYS_003", "角色编码已存在"),
    SYSTEM_DICT_TYPE_EXISTS("SYS_004", "字典类型编码已存在"),
    SYSTEM_DEPT_HAS_USERS("SYS_005", "部门下存在关联用户，无法删除"),
    SYSTEM_MENU_HAS_CHILDREN("SYS_006", "存在子菜单，无法删除"),
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
