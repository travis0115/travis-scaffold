package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.common.api.OpsPermission;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 定时任务执行日志管理接口。 */
@RestController
@RequestMapping("/ops/job-log")
@RequiredArgsConstructor
@OperationLogModule("JobLog")
public class OpsJobLogController {

    private final OpsJobLogService logService;

    /** 分页查询任务执行日志。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobLogPageResp>> page(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.page(req));
    }

    /** 查询任务执行日志详情。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id}")
    public ApiResponse<OpsJobLogDetailResp> detail(@PathVariable Long id) {
        return ApiResponse.success(logService.getOrThrow(id));
    }

    /** 按查询条件导出任务执行日志。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_LOG_QUERY, type = LoginType.ADMIN)
    @GetMapping("/export")
    public ApiResponse<List<OpsJobLogExportResp>> export(OpsJobLogPageReq req) {
        return ApiResponse.success(logService.exportLogs(req));
    }

    /** 清理指定任务或全部任务的执行日志。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "清理任务日志")
    @DeleteMapping("/clean")
    public ApiResponse<Void> clean(@RequestParam(required = false) Long jobId) {
        logService.clean(jobId);
        return ApiResponse.success();
    }
}
