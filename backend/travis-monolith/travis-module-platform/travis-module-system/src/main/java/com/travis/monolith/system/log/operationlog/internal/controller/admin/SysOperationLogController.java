package com.travis.monolith.system.log.operationlog.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.operationlog.api.request.SysOperationLogPageReq;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志查询控制器，提供只读的分页查询接口，支持按时间范围筛选
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    /** 操作日志服务 */
    private final SysOperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysOperationLog>> page(@Valid SysOperationLogPageReq req) {
        return ApiResponse.success(operationLogService.page(req));
    }
}
