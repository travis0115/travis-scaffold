package com.travis.monolith.system.file.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/file-folder")
@RequiredArgsConstructor
public class SysFileFolderController {
    private final SysFileFolderService folderService;

    @GetMapping("/list")
    public ApiResponse<List<SysFileFolder>> listAll() {
        return ApiResponse.success(folderService.listAll());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SysFileFolderCreateReq req) {
        folderService.create(req);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysFileFolderUpdateReq req) {
        folderService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        folderService.removeById(id);
        return ApiResponse.success();
    }
}
