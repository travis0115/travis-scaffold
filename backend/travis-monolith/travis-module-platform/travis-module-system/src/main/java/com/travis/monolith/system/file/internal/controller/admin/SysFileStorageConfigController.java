package com.travis.monolith.system.file.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigPageReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.api.response.SysFileStorageTypeResp;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import com.travis.monolith.system.file.internal.strategy.FileStorageStrategy;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 文件存储配置管理接口。 */
@RestController
@RequestMapping("/system/file/storage")
@RequiredArgsConstructor
@OperationLogModule("FileStorageConfig")
public class SysFileStorageConfigController {
    private final SysFileStorageConfigService storageConfigService;
    private final List<FileStorageStrategy> storageStrategies;

    /** 分页查询存储配置。 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysFileStorageConfigResp>> page(
            @Valid SysFileStorageConfigPageReq req) {
        return ApiResponse.success(storageConfigService.page(req));
    }

    /** 查询全部存储配置。 */
    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysFileStorageConfigResp>> listAll() {
        return ApiResponse.success(storageConfigService.listAll());
    }

    /** 查询当前已注册的存储类型。 */
    @GetMapping("/types")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysFileStorageTypeResp>> listStorageTypes() {
        return ApiResponse.success(
                storageStrategies.stream()
                        .map(FileStorageStrategy::getStorageType)
                        .distinct()
                        .map(value -> new SysFileStorageTypeResp(getStorageTypeLabel(value), value))
                        .toList());
    }

    /** 查询存储配置详情。 */
    @GetMapping("/{id:\\d+}")
    @SaCheckPermission(value = SystemPermission.FILE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysFileStorageConfigResp> get(@PathVariable Long id) {
        return ApiResponse.success(storageConfigService.getOrThrow(id));
    }

    /** 创建存储配置。 */
    @PostMapping
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "新增文件存储配置")
    public ApiResponse<Void> create(@RequestBody @Valid SysFileStorageConfigCreateReq req) {
        storageConfigService.create(req);
        return ApiResponse.success();
    }

    /** 更新存储配置。 */
    @PutMapping("/{id:\\d+}")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "更新文件存储配置")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysFileStorageConfigUpdateReq req) {
        storageConfigService.update(id, req);
        return ApiResponse.success();
    }

    /** 更新存储配置状态。 */
    @PutMapping("/{id:\\d+}/status")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "修改文件存储配置状态")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        storageConfigService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /** 将指定配置设为默认存储配置。 */
    @PutMapping("/{id:\\d+}/default")
    @SaCheckPermission(value = SystemPermission.FILE_UPLOAD, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "设置默认文件存储配置")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        storageConfigService.setDefault(id);
        return ApiResponse.success();
    }

    /** 删除存储配置。 */
    @DeleteMapping("/{id:\\d+}")
    @SaCheckPermission(value = SystemPermission.FILE_DELETE, type = LoginType.ADMIN)
    @NoRepeatSubmit
    @OperationLog(action = "删除文件存储配置")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        storageConfigService.deleteById(id);
        return ApiResponse.success();
    }

    private String getStorageTypeLabel(String value) {
        return Arrays.stream(FileStorageType.values())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .map(FileStorageType::getLabel)
                .orElse(value);
    }
}
