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
import com.travis.monolith.system.notice.api.request.SysMessageCreateReq;
import com.travis.monolith.system.notice.api.request.SysMessagePageReq;
import com.travis.monolith.system.notice.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.notice.api.response.SysMessageDetailResp;
import com.travis.monolith.system.notice.api.response.SysMessagePageResp;
import com.travis.monolith.system.notice.internal.service.SysMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/message/push")
@RequiredArgsConstructor
@OperationLogModule("消息推送")
public class SysMessageController {
    private final SysMessageService messageService;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.MESSAGE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysMessagePageResp>> page(SysMessagePageReq req) {
        return ApiResponse.success(messageService.page(req));
    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysMessageDetailResp> getById(@PathVariable Long id) {
        return ApiResponse.success(messageService.get(id));
    }

    @OperationLog(action = "新增消息")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.MESSAGE_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysMessageCreateReq req) {
        messageService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "更新消息")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysMessageUpdateReq req) {
        messageService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "修改消息状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.MESSAGE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        messageService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除消息")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ApiResponse.success();
    }
}
