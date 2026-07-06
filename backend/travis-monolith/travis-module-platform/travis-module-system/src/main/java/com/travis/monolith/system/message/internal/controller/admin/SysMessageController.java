package com.travis.monolith.system.message.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.ImageFile;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.LoginSubjectSessionKey;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.api.constant.FileFolderId;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysMessagePageResp;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 管理后台消息推送控制器。 */
@RestController
@RequestMapping("/system/message")
@RequiredArgsConstructor
@Validated
@OperationLogModule("Message")
public class SysMessageController {
    private final SysMessageService messageService;
    private final SysFileApi fileApi;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.MESSAGE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysMessagePageResp>> page(@Valid SysMessagePageReq req) {
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

    @OperationLog(action = "上传消息图片")
    @NoRepeatSubmit
    @PostMapping("/image/upload")
    @SaCheckPermission(
            value = {SystemPermission.MESSAGE_CREATE, SystemPermission.MESSAGE_UPDATE},
            mode = SaMode.OR,
            type = LoginType.ADMIN)
    public ApiResponse<FileUploadResp> uploadImage(
            @RequestParam("file") @ImageFile MultipartFile file) {
        var username =
                StpKit.of(LoginType.ADMIN).getSession().getString(LoginSubjectSessionKey.USERNAME);
        return ApiResponse.success(
                fileApi.upload(file, FileFolderId.MESSAGE, LoginType.ADMIN, username));
    }

    @OperationLog(action = "推送消息")
    @NoRepeatSubmit
    @PutMapping("/{id}/push")
    @SaCheckPermission(value = SystemPermission.MESSAGE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> push(@PathVariable Long id) {
        messageService.push(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "撤回消息")
    @NoRepeatSubmit
    @PutMapping("/{id}/revoke")
    @SaCheckPermission(value = SystemPermission.MESSAGE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> revoke(@PathVariable Long id) {
        messageService.revoke(id);
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
