package com.travis.monolith.system.file.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/file-storage")
@RequiredArgsConstructor
public class SysFileStorageConfigController {
    private final SysFileStorageConfigService storageConfigService;

    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysFileStorageConfig>> listAll() {
        return ApiResponse.success(storageConfigService.listAll());
    }

    @PostMapping
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysFileStorageConfigCreateReq req) {
        storageConfigService.create(req);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysFileStorageConfigUpdateReq req) {
        storageConfigService.update(id, req);
        return ApiResponse.success();
    }
}
