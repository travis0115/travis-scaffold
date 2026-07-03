package com.travis.monolith.system.message.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplatePageReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/message/template")
@RequiredArgsConstructor
@OperationLogModule("MessageTemplate")
public class SysMessageTemplateController {
    private final SysMessageTemplateService templateService;

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.MESSAGE_TEMPLATE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysMessageTemplateResp>> page(SysMessageTemplatePageReq req) {
        return ApiResponse.success(templateService.page(req));
    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_TEMPLATE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysMessageTemplateResp> get(@PathVariable Long id) {
        return ApiResponse.success(templateService.get(id));
    }

    @OperationLog(action = "新增消息模板")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.MESSAGE_TEMPLATE_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysMessageTemplateCreateReq req) {
        templateService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "更新消息模板")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_TEMPLATE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysMessageTemplateUpdateReq req) {
        templateService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除消息模板")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MESSAGE_TEMPLATE_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.success();
    }
}
