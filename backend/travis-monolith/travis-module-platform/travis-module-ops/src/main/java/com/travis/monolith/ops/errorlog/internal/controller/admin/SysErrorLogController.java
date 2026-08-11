package com.travis.monolith.ops.errorlog.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.common.api.OpsPermission;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统异常日志管理接口。 */
@RestController
@RequestMapping("/ops/error-log")
@RequiredArgsConstructor
public class SysErrorLogController {
    private final SysErrorLogService errorLogService;

    /** 分页查询系统异常日志。 */
    @GetMapping("/page")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysErrorLog>> page(SysErrorLogPageReq req) {
        return ApiResponse.success(errorLogService.page(req));
    }
}
