package com.travis.monolith.ops.job.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.ops.common.api.OpsPermission;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.api.request.*;
import com.travis.monolith.ops.job.api.response.*;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public ApiResponse<PageResp<OpsJobPageResp>> page(@Valid OpsJobPageReq req) {
        return ApiResponse.success(jobService.page(req));
    }

    /** 查询定时任务详情。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id:\\d+}")
    public ApiResponse<OpsJobResp> get(
            @PathVariable @Positive(message = "任务ID必须为正数") Long id) {
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
            @PathVariable @Positive(message = "任务ID必须为正数") Long id,
            @RequestBody @Valid OpsJobUpdateReq req) {
        jobService.update(id, req);
        return ApiResponse.success();
    }

    /** 删除定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "删除任务")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Positive(message = "任务ID必须为正数") Long id) {
        jobService.delete(id);
        return ApiResponse.success();
    }

    /** 启用或停用定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "启停任务")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable @Positive(message = "任务ID必须为正数") Long id,
            @RequestParam @EnumValue(value = OpsJobStatus.class, message = "任务状态值错误")
                    Integer status) {
        jobService.changeStatus(id, status);
        return ApiResponse.success();
    }

    /** 立即触发一次任务执行。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_OPERATION, type = LoginType.ADMIN)
    @OperationLog(action = "立即执行任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/run")
    public ApiResponse<Void> runNow(
            @PathVariable @Positive(message = "任务ID必须为正数") Long id,
            @RequestBody(required = false) OpsJobRunReq req) {
        jobService.runNow(id, req == null ? null : req.getParams());
        return ApiResponse.success();
    }

    /** 复制定时任务。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_UPDATE, type = LoginType.ADMIN)
    @OperationLog(action = "复制任务")
    @NoRepeatSubmit
    @PostMapping("/{id}/copy")
    public ApiResponse<Void> copy(@PathVariable @Positive(message = "任务ID必须为正数") Long id) {
        jobService.copy(id);
        return ApiResponse.success();
    }

    /** 预览任务后续计划执行时间。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @PostMapping("/preview")
    public ApiResponse<List<LocalDateTime>> preview(
            @RequestBody @Valid OpsJobPreviewReq req,
            @RequestParam(defaultValue = "5")
                    @Min(value = 1, message = "预览数量不能小于1")
                    @Max(value = 100, message = "预览数量不能大于100")
                    Integer count) {
        return ApiResponse.success(jobService.preview(req, count));
    }

    /** 查询已注册的任务处理器名称及说明。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/handlers")
    public ApiResponse<List<OpsJobHandlerResp>> handlers(
            @RequestParam(defaultValue = "false") boolean includeBuiltin) {
        return ApiResponse.success(jobService.listHandlers(includeBuiltin));
    }

    /** 查询任务告警接收人的用户选项。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/user-options")
    public ApiResponse<List<SysUserOptionResp>> userOptions(
            @RequestParam(required = false) @Size(max = 64, message = "搜索关键字长度不能超过64个字符")
                    String keyword,
            @RequestParam(required = false) List<@Positive(message = "用户ID必须为正数") Long> userIds) {
        return ApiResponse.success(jobService.listUserOptions(keyword, userIds));
    }

    /** 查询指定任务的执行统计。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/{id}/stats")
    public ApiResponse<OpsJobStatsResp> stats(
            @PathVariable @Positive(message = "任务ID必须为正数") Long id) {
        return ApiResponse.success(logService.stats(id));
    }

    /** 查询任务调度看板汇总数据。 */
    @SaCheckPermission(value = OpsPermission.OPS_JOB_QUERY, type = LoginType.ADMIN)
    @GetMapping("/dashboard")
    public ApiResponse<OpsJobDashboardResp> dashboard() {
        return ApiResponse.success(logService.dashboard());
    }
}
