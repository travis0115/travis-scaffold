package com.travis.monolith.system.common.api;

/**
 * System模块 Event相关常量
 *
 * @author travis
 */
public class SystemEventConstant {
    /** 系统普通消息 Topic */
    public static final String NORMAL_EVENT = "system-normal-event";

    /** 用户登录事件 Tag */
    public static final String USER_LOGIN = "user-login";

    /** 部门删除事件 Tag */
    public static final String DEPT_DELETED = "dept-deleted";

    /** 用户 WebSocket 上线/下线状态变更事件 Tag */
    public static final String USER_ONLINE_STATUS = "user-online-status";
}
