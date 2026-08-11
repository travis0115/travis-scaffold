package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.ops.common.api.OpsPermission;
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

/** 定时任务管理接口。 */
@RestController
@RequestMapping("/ops/job")
@RequiredArgsConstructor
@Validated
@OperationLogModule("Job")
public class OpsJobController {

    private final OpsJobService jobService;
    private final OpsJobLogService logService;

    /** 分页查询定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobPageResp>> page(OpsJobPageReq req) {
        return ApiResponse.success(jobService.page(req));
    }

    /** 查询定时任务详情。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id:\\d+}")
    public ApiResponse<OpsJobDetailResp> get(@PathVariable Long id) {
        return ApiResponse.success(jobService.getOrThrow(id));
    }

    /** 创建定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "新增任务")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid OpsJobCreateReq req) {
        jobService.create(req);
        return ApiResponse.success();
    }

    /** 更新定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "修改任务")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid OpsJobUpdateReq req) {
        jobService.update(id, req);
        return ApiResponse.success();
    }

    /** 删除定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "删除任务")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ApiResponse.success();
    }

    /** 启用或停用定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "启停任务")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        jobService.changeStatus(id, status);
        return ApiResponse.success();
    }

    /** 立即触发一次任务执行。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "立即执行任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/run")
    public ApiResponse<Void> runNow(
            @PathVariable Long id, @RequestBody(required = false) OpsJobRunReq req) {
        jobService.runNow(id, req == null ? null : req.getParams());
        return ApiResponse.success();
    }

    /** 复制定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "复制任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/copy")
    public ApiResponse<Void> copy(@PathVariable Long id) {
        jobService.copy(id);
        return ApiResponse.success();
    }

    /** 预览任务后续计划执行时间。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @PostMapping("/preview")
    public ApiResponse<List<LocalDateTime>> preview(
            @RequestBody @Valid OpsJobPreviewReq req,
            @RequestParam(defaultValue = "5") Integer count) {
        return ApiResponse.success(jobService.preview(req, count));
    }

    /** 查询已注册的任务处理器。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/handlers")
    public ApiResponse<Collection<String>> handlers() {
        return ApiResponse.success(jobService.listHandlers());
    }

    /** 查询任务负责人及告警接收人的用户选项。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/user-options")
    public ApiResponse<List<SysUserOptionResp>> userOptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> userIds) {
        return ApiResponse.success(jobService.listUserOptions(keyword, userIds));
    }

    /** 导出全部定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/export")
    public ApiResponse<List<OpsJobExportResp>> exportJobs() {
        return ApiResponse.success(jobService.exportJobs());
    }

    /** 批量导入定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "导入任务")
    @NoRepeatSubmit
    @PostMapping("/import")
    public ApiResponse<Void> importJobs(@RequestBody List<@Valid OpsJobImportReq> jobs) {
        jobService.importJobs(jobs);
        return ApiResponse.success();
    }

    /** 查询指定任务的执行统计。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id}/stats")
    public ApiResponse<OpsJobStatsResp> stats(@PathVariable Long id) {
        return ApiResponse.success(logService.stats(id));
    }

    /** 查询任务调度看板汇总数据。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/dashboard")
    public ApiResponse<OpsJobDashboardResp> dashboard() {
        return ApiResponse.success(logService.dashboard());
    }
}
