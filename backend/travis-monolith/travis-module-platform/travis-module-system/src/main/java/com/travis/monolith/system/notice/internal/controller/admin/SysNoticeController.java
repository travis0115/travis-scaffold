package com.travis.monolith.system.notice.internal.controller.admin;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmitNamespace;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@OperationLogModule("通知公告")
@NoRepeatSubmitNamespace("system:notice")
public class SysNoticeController {
    private final SysNoticeService noticeService;

    @GetMapping("/page")
    public ApiResponse<PageResp<SysNoticeResp>> page(SysNoticePageReq req) {
        return ApiResponse.success(noticeService.page(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysNoticeResp> getById(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getDetail(id));
    }

    @OperationLog(action = "新增公告")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SysNoticeReq req) {
        noticeService.create(req);
        return ApiResponse.success();
    }

    @OperationLog(action = "更新公告")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysNoticeReq req) {
        noticeService.update(id, req);
        return ApiResponse.success();
    }

    @OperationLog(action = "删除公告")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }
}
