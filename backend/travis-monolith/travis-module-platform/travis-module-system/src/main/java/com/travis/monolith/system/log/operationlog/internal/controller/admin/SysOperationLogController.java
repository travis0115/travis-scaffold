package com.travis.monolith.system.log.operationlog.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志查询控制器，提供只读的分页查询接口，支持按时间范围筛选
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    /** 操作日志服务 */
    private final SysOperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param username 操作用户名（模糊匹配）
     * @param module 操作模块（模糊匹配）
     * @param status 操作状态
     * @param startTime 操作开始时间
     * @param endTime 操作结束时间
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysOperationLog>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(
                operationLogService.getOperationLogPage(
                        username, module, status, startTime, endTime, pageNum, pageSize));
    }
}
