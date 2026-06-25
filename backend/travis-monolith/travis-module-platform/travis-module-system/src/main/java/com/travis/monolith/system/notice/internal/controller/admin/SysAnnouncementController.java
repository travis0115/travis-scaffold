package com.travis.monolith.system.notice.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.notice.api.request.SysAnnouncementCreateReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementPageReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementUpdateReq;
import com.travis.monolith.system.notice.api.response.SysAnnouncementResp;
import com.travis.monolith.system.notice.internal.service.SysAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/announcement")
@RequiredArgsConstructor
@OperationLogModule("系统公告")
public class SysAnnouncementController {
    private final SysAnnouncementService announcementService;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysAnnouncementResp>> page(SysAnnouncementPageReq req) {
        return ApiResponse.success(announcementService.page(req));
    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysAnnouncementResp> getById(@PathVariable Long id) {
        return ApiResponse.success(announcementService.get(id));
    }

    @OperationLog(action = "新增公告")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysAnnouncementCreateReq req) {
        announcementService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "更新公告")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysAnnouncementUpdateReq req) {
        announcementService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "修改公告状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        announcementService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除公告")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ANNOUNCEMENT_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.success();
    }
}
