package com.travis.monolith.system.common.api.event;

/**
 * System模块 Event
 *
 * @author travis
 */
public class SystemEventType {

    /** 用户登录事件 Tag */
    public static final String USER_LOGIN = "user-login";

    /** 部门删除事件 Tag */
    public static final String DEPT_DELETED = "dept-deleted";

    /** 用户 WebSocket 上线/下线状态变更事件 Tag */
    public static final String USER_ONLINE_STATUS = "user-online-status";
}
