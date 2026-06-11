package com.travis.monolith.system.config.internal.controller.admin;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmitNamespace;
import com.travis.monolith.system.config.api.request.SysConfigPageReq;
import com.travis.monolith.system.config.api.request.SysConfigReq;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置控制器
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
@OperationLogModule("系统配置")
@NoRepeatSubmitNamespace("system:config")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /** 分页查询系统配置 */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysConfigResp>> page(SysConfigPageReq req) {
        return ApiResponse.success(sysConfigService.page(req));
    }

    /** 获取配置详情 */
    @GetMapping("/{id}")
    public ApiResponse<SysConfigResp> getById(@PathVariable Long id) {
        return ApiResponse.success(sysConfigService.getById(id));
    }

    /** 根据配置键获取配置值 */
    @GetMapping("/key/{configKey}/value")
    public ApiResponse<String> getValue(@PathVariable String configKey) {
        return ApiResponse.success(sysConfigService.getValue(configKey));
    }

    /** 新增配置 */
    @OperationLog(action = "新增配置")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SysConfigReq req) {
        sysConfigService.create(req);
        return ApiResponse.success();
    }

    /** 更新配置 */
    @OperationLog(action = "更新配置")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysConfigReq req) {
        sysConfigService.update(id, req);
        return ApiResponse.success();
    }

    /** 删除配置 */
    @OperationLog(action = "删除配置")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        sysConfigService.deleteById(id);
        return ApiResponse.success();
    }
}
