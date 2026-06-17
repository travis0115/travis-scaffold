package com.travis.monolith.system.log.errorlog.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.log.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.system.log.errorlog.internal.service.SysErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/error-log")
@RequiredArgsConstructor
public class SysErrorLogController {
    private final SysErrorLogService errorLogService;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.ERROR_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysErrorLog>> page(SysErrorLogPageReq req) {
        return ApiResponse.success(errorLogService.page(req));
    }
}
