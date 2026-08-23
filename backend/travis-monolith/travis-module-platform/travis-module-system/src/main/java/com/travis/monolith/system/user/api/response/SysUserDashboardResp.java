package com.travis.monolith.system.user.api.response;

/** 首页用户概览。 */
public record SysUserDashboardResp(
        /** 用户总数。 */
        long totalUsers,
        /** 今日新增用户数。 */
        long newUsersToday,
        /** 当前在线用户数。 */
        long onlineUsers) {}
