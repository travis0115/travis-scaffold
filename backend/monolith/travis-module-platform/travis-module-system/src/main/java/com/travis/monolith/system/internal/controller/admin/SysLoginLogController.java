package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysLoginLog;
import com.travis.monolith.system.internal.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志查询控制器，提供只读的分页查询接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    /** 登录日志服务 */
    private final SysLoginLogService loginLogService;

    /**
     * 分页查询登录日志
     *
     * @param username 用户名（模糊匹配）
     * @param status 登录状态
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysLoginLog>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(
                loginLogService.getLoginLogPage(username, status, pageNum, pageSize));
    }
}
