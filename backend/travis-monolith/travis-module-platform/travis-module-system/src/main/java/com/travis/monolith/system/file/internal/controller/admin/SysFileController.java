package com.travis.monolith.system.file.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.LoginSubjectSessionKey;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadPolicyResp;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.config.properties.FileUploadProperties;
import com.travis.monolith.system.file.internal.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器，提供文件上传和访问接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
@OperationLogModule("文件管理")
public class SysFileController {

    private final SysFileService fileService;
    private final FileUploadProperties fileUploadProperties;
    private final MultipartProperties multipartProperties;

    /** 获取文件上传的策略，包括允许的文件类型和最大文件大小 */
    @GetMapping("/upload-policy")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<FileUploadPolicyResp> uploadPolicy() {
        return ApiResponse.success(
                FileUploadPolicyResp.builder()
                        .allowedExtensions(
                                fileUploadProperties.getNormalizedAllowedExtensions().stream()
                                        .sorted()
                                        .toList())
                        .maxFileSizeBytes(multipartProperties.getMaxFileSize().toBytes())
                        .build());
    }

    /**
     * 上传文件，返回相对路径和完整访问URL
     *
     * @param file 文件
     * @return 文件上传响应（path用于存储，url用于展示）
     */
    @PostMapping("/upload")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "上传文件")
    public ApiResponse<FileUploadResp> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long folderId) {
        var logic = StpKit.of(LoginType.ADMIN);
        var username = logic.getSession().get(LoginSubjectSessionKey.USERNAME);
        return ApiResponse.success(
                fileService.upload(
                        file,
                        folderId,
                        LoginType.ADMIN,
                        username == null
                                ? String.valueOf(logic.getLoginIdAsLong())
                                : username.toString()));
    }

    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysFileResp>> page(SysFilePageReq req) {
        return ApiResponse.success(fileService.page(req));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.FILE_DELETE, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "删除文件")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileService.removeById(id);
        return ApiResponse.success();
    }
}
