package com.travis.monolith.system.notice.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.ImageFile;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.IsPinned;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.api.constant.FileFolderId;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 公告管理接口。 */
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@Validated
@OperationLogModule("Notice")
public class SysNoticeController {
    private final SysNoticeService noticeService;
    private final SysFileApi fileApi;

    /** 分页查询全部公告。 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.NOTICE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysNoticeResp>> page(SysNoticePageReq req) {
        return ApiResponse.success(noticeService.page(req));
    }

    /** 分页查询当前已发布公告。 */
    @GetMapping("/published")
    public ApiResponse<PageResp<SysNoticeResp>> pagePublished(SysNoticePageReq req) {
        return ApiResponse.success(noticeService.pagePublished(req));
    }

    /** 查询公告详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysNoticeResp> getById(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getOrThrow(id));
    }

    /** 创建公告。 */
    @OperationLog(action = "新增公告")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.NOTICE_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysNoticeCreateReq req) {
        noticeService.create(req);
        return ApiResponse.success();
    }

    /** 更新公告。 */
    @OperationLog(action = "更新公告")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysNoticeUpdateReq req) {
        noticeService.update(id, req);
        return ApiResponse.success();
    }

    /** 上传公告正文图片。 */
    @OperationLog(action = "上传公告图片")
    @NoRepeatSubmit
    @PostMapping("/image/upload")
    @SaCheckPermission(
            value = {SystemPermission.NOTICE_CREATE, SystemPermission.NOTICE_UPDATE},
            mode = SaMode.OR,
            type = LoginType.ADMIN)
    public ApiResponse<FileUploadResp> uploadImage(
            @RequestParam("file") @ImageFile MultipartFile file) {
        var uploaderId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        return ApiResponse.success(
                fileApi.upload(file, FileFolderId.NOTICE, LoginType.ADMIN, uploaderId));
    }

    /** 更新公告发布状态。 */
    @OperationLog(action = "修改公告状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.NOTICE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = PublishStatus.class, message = "状态值错误")
                    Integer status) {
        noticeService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /** 更新公告置顶状态。 */
    @OperationLog(action = "修改公告置顶状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/pinned")
    @SaCheckPermission(value = SystemPermission.NOTICE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updatePinned(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = IsPinned.class, message = "置顶值错误") Integer isPinned) {
        noticeService.updatePinned(id, isPinned);
        return ApiResponse.success();
    }

    /** 删除公告。 */
    @OperationLog(action = "删除公告")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.NOTICE_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }
}
