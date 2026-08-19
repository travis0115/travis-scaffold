package com.travis.monolith.ops.errorlog.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.common.api.OpsPermission;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogHandleReq;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogDetailResp;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统异常日志管理接口。 */
@RestController
@RequestMapping("/ops/error-log")
@RequiredArgsConstructor
@Validated
@OperationLogModule("ErrorLog")
public class SysErrorLogController {
    private final SysErrorLogService errorLogService;

    /** 分页查询系统异常日志。 */
    @GetMapping("/page")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysErrorLogResp>> page(@Valid SysErrorLogPageReq req) {
        return ApiResponse.success(errorLogService.page(req));
    }

    /** 查询错误日志详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysErrorLogDetailResp> detail(
            @PathVariable @Positive(message = "错误日志ID必须为正数") Long id) {
        return ApiResponse.success(errorLogService.getDetailOrThrow(id));
    }

    /** 标记错误日志处理结果。 */
    @PutMapping("/{id}/handle")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_HANDLE, type = LoginType.ADMIN)
    @OperationLog(action = "处理错误日志")
    public ApiResponse<Void> handle(
            @PathVariable @Positive(message = "错误日志ID必须为正数") Long id,
            @RequestBody @Valid SysErrorLogHandleReq req) {
        errorLogService.handle(id, req);
        return ApiResponse.success();
    }

    /** 批量处理全部待处理错误日志。 */
    @PutMapping("/handle-all")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_HANDLE, type = LoginType.ADMIN)
    @OperationLog(action = "批量处理全部错误日志")
    public ApiResponse<Integer> handleAll(@RequestBody @Valid SysErrorLogHandleReq req) {
        return ApiResponse.success(errorLogService.handleAllPending(req));
    }

    /** 删除错误日志及其发生明细。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = OpsPermission.OPS_ERROR_LOG_DELETE, type = LoginType.ADMIN)
    @OperationLog(action = "删除错误日志")
    public ApiResponse<Void> delete(@PathVariable @Positive(message = "错误日志ID必须为正数") Long id) {
        errorLogService.delete(id);
        return ApiResponse.success();
    }
}
