package com.travis.monolith.system.file.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 文件夹管理接口。 */
@RestController
@RequestMapping("/system/file/folder")
@RequiredArgsConstructor
@OperationLogModule("FileFolder")
public class SysFileFolderController {
    private final SysFileFolderService folderService;

    /** 查询全部文件夹。 */
    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysFileFolder>> listAll() {
        return ApiResponse.success(folderService.listAll());
    }

    /** 创建文件夹。 */
    @PostMapping
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "新增文件夹")
    public ApiResponse<Void> create(@RequestBody @Valid SysFileFolderCreateReq req) {
        folderService.create(req);
        return ApiResponse.success();
    }

    /** 更新文件夹。 */
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "更新文件夹")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysFileFolderUpdateReq req) {
        folderService.update(id, req);
        return ApiResponse.success();
    }

    /** 删除文件夹及其子文件夹。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.FILE_DELETE, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "删除文件夹")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        folderService.deleteById(id);
        return ApiResponse.success();
    }
}
