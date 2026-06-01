package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.monolith.system.internal.service.SysFileService;
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
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileService.upload(file));
    }
}
