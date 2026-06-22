package com.travis.monolith.system.log.versionlog.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogCreateReq;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogUpdateReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogDetailResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPageResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPublishedResp;
import com.travis.monolith.system.log.versionlog.internal.service.SysVersionLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统版本日志管理控制器，提供CRUD接口和已发布日志查询
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/version-log")
@RequiredArgsConstructor
@Validated
public class SysVersionLogController {

    private final SysVersionLogService versionLogService;

    /** 分页查询版本日志 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.VERSION_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysVersionLogPageResp>> page(
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(
                versionLogService.page(version, title, status, pageNum, pageSize));
    }

    /** 获取版本日志详情 */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysVersionLogDetailResp> get(@PathVariable Long id) {
        return ApiResponse.success(versionLogService.getById(id));
    }

    /** 新增版本日志 */
    @PostMapping
    @SaCheckPermission(value = SystemPermission.VERSION_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> add(@RequestBody @Valid SysVersionLogCreateReq req) {
        versionLogService.create(req);
        return ApiResponse.success();
    }

    /** 更新版本日志 */
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysVersionLogUpdateReq req) {
        versionLogService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改版本日志状态 */
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.VERSION_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        versionLogService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /** 删除版本日志 */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        versionLogService.deleteById(id);
        return ApiResponse.success();
    }

    /** 获取已发布的版本日志列表（供前端用户查看） */
    @GetMapping("/published")
    public ApiResponse<List<SysVersionLogPublishedResp>> listPublished(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(versionLogService.listPublished(limit));
    }
}
