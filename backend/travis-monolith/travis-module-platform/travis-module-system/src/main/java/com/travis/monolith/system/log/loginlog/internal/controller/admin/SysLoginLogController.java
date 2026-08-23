package com.travis.monolith.system.log.loginlog.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.log.loginlog.api.request.SysLoginLogPageReq;
import com.travis.monolith.system.log.loginlog.api.response.SysLoginDashboardResp;
import com.travis.monolith.system.log.loginlog.api.response.SysLoginLogResp;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志查询控制器，提供只读的分页查询接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    /** 登录日志服务 */
    private final SysLoginLogService loginLogService;

    /**
     * 分页查询登录日志
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.LOGIN_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysLoginLogResp>> page(@Valid SysLoginLogPageReq req) {
        return ApiResponse.success(loginLogService.page(req));
    }

    /** 获取首页登录活跃概览。 */
    @GetMapping("/dashboard")
    @SaCheckPermission(value = SystemPermission.LOGIN_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysLoginDashboardResp> dashboard() {
        return ApiResponse.success(loginLogService.dashboard());
    }
}
