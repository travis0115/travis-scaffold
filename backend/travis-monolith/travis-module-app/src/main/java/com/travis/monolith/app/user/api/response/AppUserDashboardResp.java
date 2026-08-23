package com.travis.monolith.app.user.api.response;

/** 首页客户端用户概览。 */
public record AppUserDashboardResp(long totalUsers, long newUsersToday, long onlineUsers) {}
