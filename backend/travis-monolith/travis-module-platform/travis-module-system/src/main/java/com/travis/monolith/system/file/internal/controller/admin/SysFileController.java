package com.travis.monolith.system.file.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.service.SysFileService;
import lombok.RequiredArgsConstructor;
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
public class SysFileController {

    private final SysFileService fileService;

    /**
     * 上传文件，返回相对路径和完整访问URL
     *
     * @param file 文件
     * @return 文件上传响应（path用于存储，url用于展示）
     */
    @PostMapping("/upload")
    public ApiResponse<FileUploadResp> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long folderId) {
        return ApiResponse.success(fileService.upload(file, folderId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResp<SysFileResp>> page(SysFilePageReq req) {
        return ApiResponse.success(fileService.page(req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileService.removeById(id);
        return ApiResponse.success();
    }
}
