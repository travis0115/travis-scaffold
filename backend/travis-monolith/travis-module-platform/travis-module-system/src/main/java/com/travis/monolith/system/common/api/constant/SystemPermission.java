package com.travis.monolith.system.common.api.constant;

/**
 * 系统权限常量
 *
 * @author travis
 */
public class SystemPermission {
    private SystemPermission() {}

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 菜单模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */

    /** 菜单查询权限 */
    public static final String MENU_QUERY = "system:menu:query";

    /** 菜单创建权限 */
    public static final String MENU_CREATE = "system:menu:create";

    /** 菜单更新权限 */
    public static final String MENU_UPDATE = "system:menu:update";

    /** 菜单删除权限 */
    public static final String MENU_DELETE = "system:menu:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 角色模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 角色查询权限 */
    public static final String ROLE_QUERY = "system:role:query";

    /** 角色创建权限 */
    public static final String ROLE_CREATE = "system:role:create";

    /** 角色更新权限 */
    public static final String ROLE_UPDATE = "system:role:update";

    /** 角色删除权限 */
    public static final String ROLE_DELETE = "system:role:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 用户模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 用户查询权限 */
    public static final String USER_QUERY = "system:user:query";

    /** 用户创建权限 */
    public static final String USER_CREATE = "system:user:create";

    /** 用户更新权限 */
    public static final String USER_UPDATE = "system:user:update";

    /** 用户删除权限 */
    public static final String USER_DELETE = "system:user:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 部门模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 部门查询权限 */
    public static final String DEPT_QUERY = "system:dept:query";

    /** 部门创建权限 */
    public static final String DEPT_CREATE = "system:dept:create";

    /** 部门更新权限 */
    public static final String DEPT_UPDATE = "system:dept:update";

    /** 部门删除权限 */
    public static final String DEPT_DELETE = "system:dept:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 字典模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 字典查询权限 */
    public static final String DICT_QUERY = "system:dict:query";

    /** 字典创建权限 */
    public static final String DICT_CREATE = "system:dict:create";

    /** 字典更新权限 */
    public static final String DICT_UPDATE = "system:dict:update";

    /** 字典删除权限 */
    public static final String DICT_DELETE = "system:dict:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 参数配置模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 参数配置查询权限 */
    public static final String CONFIG_QUERY = "system:config:query";

    /** 参数配置创建权限 */
    public static final String CONFIG_CREATE = "system:config:create";

    /** 参数配置更新权限 */
    public static final String CONFIG_UPDATE = "system:config:update";

    /** 参数配置删除权限 */
    public static final String CONFIG_DELETE = "system:config:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 版本管理模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 版本管理查询权限 */
    public static final String VERSION_QUERY = "system:version:query";

    /** 版本管理创建权限 */
    public static final String VERSION_CREATE = "system:version:create";

    /** 版本管理更新权限 */
    public static final String VERSION_UPDATE = "system:version:update";

    /** 版本管理删除权限 */
    public static final String VERSION_DELETE = "system:version:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 系统公告模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 系统公告查询权限 */
    public static final String ANNOUNCEMENT_QUERY = "system:announcement:query";

    /** 系统公告创建权限 */
    public static final String ANNOUNCEMENT_CREATE = "system:announcement:create";

    /** 系统公告更新权限 */
    public static final String ANNOUNCEMENT_UPDATE = "system:announcement:update";

    /** 系统公告删除权限 */
    public static final String ANNOUNCEMENT_DELETE = "system:announcement:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 消息推送模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 消息推送查询权限 */
    public static final String MESSAGE_QUERY = "system:message:query";

    /** 消息推送创建权限 */
    public static final String MESSAGE_CREATE = "system:message:create";

    /** 消息推送更新权限 */
    public static final String MESSAGE_UPDATE = "system:message:update";

    /** 消息推送删除权限 */
    public static final String MESSAGE_DELETE = "system:message:delete";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 日志模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 登录日志查询权限 */
    public static final String LOGIN_LOG_QUERY = "system:log:login:query";

    /** 操作日志查询权限 */
    public static final String OPERATION_LOG_QUERY = "system:log:operation:query";

    /** 错误日志查询权限 */
    public static final String ERROR_LOG_QUERY = "system:log:error:query";

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 文件模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 文件查询权限 */
    public static final String FILE_QUERY = "system:file:query";

    /** 文件上传权限 */
    public static final String FILE_UPLOAD = "system:file:upload";

    /** 文件删除权限 */
    public static final String FILE_DELETE = "system:file:delete";
}
