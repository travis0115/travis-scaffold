package com.travis.monolith.system.version.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionPageReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.service.SysVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统版本日志管理控制器，提供CRUD接口和已发布日志查询
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/version")
@RequiredArgsConstructor
@Validated
public class SysVersionController {

    private final SysVersionService versionService;

    /** 分页查询版本日志 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.VERSION_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysVersionResp>> page(SysVersionPageReq req) {
        return ApiResponse.success(versionService.page(req));
    }

    /** 获取版本日志详情 */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysVersionResp> get(@PathVariable Long id) {
        return ApiResponse.success(versionService.getById(id));
    }

    /** 新增版本日志 */
    @PostMapping
    @SaCheckPermission(value = SystemPermission.VERSION_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> add(@RequestBody @Valid SysVersionCreateReq req) {
        versionService.create(req);
        return ApiResponse.success();
    }

    /** 更新版本日志 */
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysVersionUpdateReq req) {
        versionService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改版本日志状态 */
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.VERSION_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        versionService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /** 删除版本日志 */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.VERSION_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        versionService.deleteById(id);
        return ApiResponse.success();
    }

    /** 获取已发布的版本日志列表（供前端用户查看） */
    @GetMapping("/published")
    public ApiResponse<PageResp<SysVersionResp>> pagePublished(PageRequest req) {
        return ApiResponse.success(versionService.pagePublished(req));
    }
}
