package com.travis.monolith.app.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.app.user.api.response.AppUserLoginDashboardResp;
import com.travis.monolith.app.user.internal.entity.AppUserLoginLog;

/** 客户端用户登录日志服务。 */
public interface AppUserLoginLogService extends IService<AppUserLoginLog> {

    /** 获取首页客户端登录活跃概览。 */
    AppUserLoginDashboardResp dashboard();

    /** 记录客户端用户登录日志。 */
    void recordLoginLog(
            String username, int status, String message, String ip, String browser, String os);
}
