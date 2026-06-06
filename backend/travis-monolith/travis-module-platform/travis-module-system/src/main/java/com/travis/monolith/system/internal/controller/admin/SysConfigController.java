package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.internal.model.request.config.SysConfigPageReq;
import com.travis.monolith.system.internal.model.request.config.SysConfigReq;
import com.travis.monolith.system.internal.model.response.config.SysConfigResp;
import com.travis.monolith.system.internal.service.SysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置管理控制器
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
public class SysConfigController {

    private final SysConfigService configService;

    /** 分页查询系统配置 */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysConfigResp>> page(SysConfigPageReq req) {
        return ApiResponse.success(configService.getConfigPage(req));
    }

    /** 获取配置详情 */
    @GetMapping("/{id}")
    public ApiResponse<SysConfigResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(configService.getConfigDetail(id));
    }

    /** 根据配置键获取配置值 */
    @GetMapping("/value")
    public ApiResponse<String> getValue(@RequestParam String configKey) {
        return ApiResponse.success(configService.getConfigValue(configKey));
    }

    /** 新增配置 */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysConfigReq req) {
        configService.addConfig(req);
        return ApiResponse.success();
    }

    /** 更新配置 */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysConfigReq req) {
        configService.updateConfig(id, req);
        return ApiResponse.success();
    }

    /** 删除配置 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ApiResponse.success();
    }
}
