package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.common.api.OpsPermsConstant;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ops/job-log")
@RequiredArgsConstructor
@OperationLogModule("任务执行日志")
public class OpsJobLogController {

    private final OpsJobLogService logService;

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobLogPageResp>> page(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.page(req));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id}")
    public ApiResponse<OpsJobLogDetailResp> detail(@PathVariable Long id) {
        return ApiResponse.success(logService.getDetail(id));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/export")
    public ApiResponse<List<OpsJobLogExportResp>> export(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.exportLogs(req));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "清理任务日志")
    @DeleteMapping("/clean")
    public ApiResponse<Void> clean(@RequestParam(required = false) Long jobId) {
        logService.clean(jobId);
        return ApiResponse.success();
    }
}
