package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.ops.common.api.OpsPermsConstant;
import com.travis.monolith.ops.job.api.request.*;
import com.travis.monolith.ops.job.api.response.*;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ops/job")
@RequiredArgsConstructor
@Validated
@OperationLogModule("Job")
public class OpsJobController {

    private final OpsJobService jobService;
    private final OpsJobLogService logService;

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobPageResp>> page(OpsJobPageReq req) {
        return ApiResponse.success(jobService.page(req));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id:\\d+}")
    public ApiResponse<OpsJobDetailResp> get(@PathVariable Long id) {
        return ApiResponse.success(jobService.get(id));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "新增任务")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid OpsJobCreateReq req) {
        jobService.create(req);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "修改任务")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid OpsJobUpdateReq req) {
        jobService.update(id, req);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "删除任务")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "启停任务")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        jobService.changeStatus(id, status);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "立即执行任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/run")
    public ApiResponse<Void> runNow(
            @PathVariable Long id, @RequestBody(required = false) OpsJobRunReq req) {
        jobService.runNow(id, req == null ? null : req.getParams());
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "复制任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/copy")
    public ApiResponse<Void> copy(@PathVariable Long id) {
        jobService.copy(id);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @PostMapping("/preview")
    public ApiResponse<List<LocalDateTime>> preview(
            @RequestBody @Valid OpsJobPreviewReq req,
            @RequestParam(defaultValue = "5") Integer count) {
        return ApiResponse.success(jobService.preview(req, count));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/handlers")
    public ApiResponse<Collection<String>> handlers() {
        return ApiResponse.success(jobService.listHandlers());
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/user-options")
    public ApiResponse<List<SysUserOptionResp>> userOptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> userIds) {
        return ApiResponse.success(jobService.listUserOptions(keyword, userIds));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/export")
    public ApiResponse<List<OpsJobExportResp>> exportJobs() {
        return ApiResponse.success(jobService.exportJobs());
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "导入任务")
    @NoRepeatSubmit
    @PostMapping("/import")
    public ApiResponse<Void> importJobs(@RequestBody List<@Valid OpsJobImportReq> jobs) {
        jobService.importJobs(jobs);
        return ApiResponse.success();
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id}/stats")
    public ApiResponse<OpsJobStatsResp> stats(@PathVariable Long id) {
        return ApiResponse.success(logService.stats(id));
    }

    @SaCheckPermission(value = OpsPermsConstant.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/dashboard")
    public ApiResponse<OpsJobDashboardResp> dashboard() {
        return ApiResponse.success(logService.dashboard());
    }
}
