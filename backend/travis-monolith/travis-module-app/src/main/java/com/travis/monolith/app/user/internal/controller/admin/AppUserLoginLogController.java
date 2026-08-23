package com.travis.monolith.app.user.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.app.user.api.response.AppUserLoginDashboardResp;
import com.travis.monolith.app.user.internal.service.AppUserLoginLogService;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 客户端用户登录日志后台查询接口。 */
@RestController
@RequestMapping("/app/user-login-log")
@RequiredArgsConstructor
public class AppUserLoginLogController {

    private final AppUserLoginLogService loginLogService;

    /** 获取首页客户端登录活跃概览。 */
    @GetMapping("/dashboard")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<AppUserLoginDashboardResp> dashboard() {
        return ApiResponse.success(loginLogService.dashboard());
    }
}
