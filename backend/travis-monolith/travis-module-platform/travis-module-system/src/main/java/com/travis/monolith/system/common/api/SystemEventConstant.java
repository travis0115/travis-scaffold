package com.travis.monolith.system.common.api;

/**
 * System模块 Event相关常量
 *
 * @author travis
 */
public class SystemEventConstant {
    /** 统一 Topic，所有系统事件共用 */
    public static final String TOPIC = "system-event";

    /** 用户登录事件 Tag */
    public static final String USER_LOGIN_TAG = "user-login";

    /** 部门删除事件 Tag */
    public static final String DEPT_DELETED_TAG = "dept-deleted";
}
