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
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeDetailResp;
import com.travis.monolith.system.notice.api.response.SysNoticePageResp;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@OperationLogModule("通知公告")
public class SysNoticeController {
    private final SysNoticeService noticeService;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.NOTICE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysNoticePageResp>> page(SysNoticePageReq req) {
        return ApiResponse.success(noticeService.page(req));
    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysNoticeDetailResp> getById(@PathVariable Long id) {
        return ApiResponse.success(noticeService.get(id));
    }

    @OperationLog(action = "新增公告")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.NOTICE_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysNoticeCreateReq req) {
        noticeService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "更新公告")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysNoticeUpdateReq req) {
        noticeService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "修改公告状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.NOTICE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        noticeService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除公告")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }
}
