package com.travis.monolith.system.file.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigReq;
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
    public ApiResponse<List<SysFileStorageConfig>> listAll() {
        return ApiResponse.success(storageConfigService.list());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SysFileStorageConfigReq req) {
        storageConfigService.create(req);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysFileStorageConfigReq req) {
        storageConfigService.update(id, req);
        return ApiResponse.success();
    }
}
