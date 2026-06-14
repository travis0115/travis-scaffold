package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ops/job-log")
@RequiredArgsConstructor
@OperationLogModule("任务执行日志")
public class OpsJobLogController {

    private final OpsJobLogService logService;

    @SaCheckPermission(value = "ops:job:view", type = "admin")
    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobLogPageResp>> page(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.page(req));
    }

    @SaCheckPermission(value = "ops:job:exception", type = "admin")
    @GetMapping("/{id}")
    public ApiResponse<OpsJobLogDetailResp> detail(@PathVariable Long id) {
        return ApiResponse.success(logService.getDetail(id));
    }

    @SaCheckPermission(value = "ops:job:view", type = "admin")
    @GetMapping("/export")
    public ApiResponse<List<OpsJobLogExportResp>> export(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.exportLogs(req));
    }

    @SaCheckPermission(value = "ops:job:edit", type = "admin")
    @OperationLog(action = "清理任务日志")
    @DeleteMapping("/clean")
    public ApiResponse<Void> clean(@RequestParam(required = false) Long jobId) {
        logService.clean(jobId);
        return ApiResponse.success();
    }
}
