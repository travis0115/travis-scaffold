package com.travis.monolith.system.common.api;

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
    ROLE_CODE_EXISTS("SYS_100", "角色编码已存在"),

    /* dept模块 200-299 */
    DEPT_NOT_FOUND("SYS_200", "部门不存在"),
    DEPT_PARENT_INVALID("SYS_201", "上级部门不能是当前部门或其下级部门"),

    /* menu模块 300-399 */
    MENU_PATH_EXISTS("SYS_300", "菜单路由路径已存在"),

    /* dict模块 400-499 */
    DICT_TYPE_EXISTS("SYS_400", "字典类型编码已存在"),

/* errorlog模块 500-599 */
/* loginlog模块 600-699 */
/* operationlog模块 700-799 */
/* versionlog模块 800-899 */
/* notice模块 900-999 */
/* file模块 1000-1099 */
/* config模块 1100-1199 */

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
