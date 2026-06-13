package com.travis.monolith.ops.job.internal.controller.admin;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobImportReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobRunReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobStatsResp;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ops/job")
@RequiredArgsConstructor
@Validated
@OperationLogModule("任务调度")
public class OpsJobController {

    private final OpsJobService jobService;
    private final OpsJobLogService logService;

    @GetMapping("/page")
    public ApiResponse<PageResp<OpsJobPageResp>> page(OpsJobPageReq req) {
        check("ops:job:view");
        return ApiResponse.success(jobService.page(req));
    }

    @GetMapping("/{id:\\d+}")
    public ApiResponse<OpsJobDetailResp> get(@PathVariable Long id) {
        check("ops:job:view");
        return ApiResponse.success(jobService.getDetail(id));
    }

    @OperationLog(action = "新增任务")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid OpsJobCreateReq req) {
        check("ops:job:edit");
        jobService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "修改任务")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid OpsJobUpdateReq req) {
        check("ops:job:edit");
        jobService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除任务")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        check("ops:job:edit");
        jobService.delete(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "启停任务")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        check("ops:job:status");
        jobService.changeStatus(id, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "立即执行任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/run")
    public ApiResponse<Void> runNow(
            @PathVariable Long id, @RequestBody(required = false) OpsJobRunReq req) {
        check("ops:job:run");
        jobService.runNow(id, req == null ? null : req.getParams());
        return ApiResponse.success();
    }

    @OperationLog(action = "复制任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/copy")
    public ApiResponse<Void> copy(@PathVariable Long id) {
        check("ops:job:edit");
        jobService.copy(id);
        return ApiResponse.success();
    }

    @PostMapping("/preview")
    public ApiResponse<List<LocalDateTime>> preview(
            @RequestBody @Valid OpsJobPreviewReq req,
            @RequestParam(defaultValue = "5") Integer count) {
        check("ops:job:view");
        return ApiResponse.success(jobService.preview(req, count));
    }

    @GetMapping("/handlers")
    public ApiResponse<Collection<String>> handlers() {
        check("ops:job:view");
        return ApiResponse.success(jobService.listHandlers());
    }

    @GetMapping("/user-options")
    public ApiResponse<List<SysUserOptionResp>> userOptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> userIds) {
        check("ops:job:view");
        return ApiResponse.success(jobService.listUserOptions(keyword, userIds));
    }

    @GetMapping("/export")
    public ApiResponse<List<OpsJobExportResp>> exportJobs() {
        check("ops:job:view");
        return ApiResponse.success(jobService.exportJobs());
    }

    @OperationLog(action = "导入任务")
    @NoRepeatSubmit
    @PostMapping("/import")
    public ApiResponse<Void> importJobs(@RequestBody List<@Valid OpsJobImportReq> jobs) {
        check("ops:job:edit");
        jobService.importJobs(jobs);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<OpsJobStatsResp> stats(@PathVariable Long id) {
        check("ops:job:view");
        return ApiResponse.success(logService.stats(id));
    }

    @GetMapping("/dashboard")
    public ApiResponse<OpsJobDashboardResp> dashboard() {
        check("ops:job:view");
        return ApiResponse.success(logService.dashboard());
    }

    private void check(String permission) {
        StpKit.of(LoginType.ADMIN).checkPermission(permission);
    }
}
