package com.travis.monolith.system.log.updatelog.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.log.updatelog.api.request.SysUpdateLogReq;
import com.travis.monolith.system.log.updatelog.api.response.SysUpdateLogResp;
import com.travis.monolith.system.log.updatelog.internal.service.SysUpdateLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统更新日志管理控制器，提供CRUD接口和已发布日志查询
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/update-log")
@RequiredArgsConstructor
@Validated
public class SysUpdateLogController {

    private final SysUpdateLogService updateLogService;

    /** 分页查询更新日志 */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysUpdateLogResp>> page(
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(
                updateLogService.page(version, title, status, pageNum, pageSize));
    }

    /** 获取更新日志详情 */
    @GetMapping("/{id}")
    public ApiResponse<SysUpdateLogResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(updateLogService.getById(id));
    }

    /** 新增更新日志 */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysUpdateLogReq req) {
        updateLogService.create(req);
        return ApiResponse.success();
    }

    /** 更新更新日志 */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysUpdateLogReq req) {
        updateLogService.update(id, req);
        return ApiResponse.success();
    }

    /** 删除更新日志 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        updateLogService.deleteById(id);
        return ApiResponse.success();
    }

    /** 获取已发布的更新日志列表（供前端用户查看） */
    @GetMapping("/published")
    public ApiResponse<List<SysUpdateLogResp>> listPublished(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(updateLogService.listPublished(limit));
    }
}
