package com.travis.monolith.system.dept.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 部门管理控制器，提供部门树的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Validated
@OperationLogModule("部门管理")
public class SysDeptController {

    /** 部门管理服务 */
    private final SysDeptService deptService;

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.DEPT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysDeptResp>> list() {
        return ApiResponse.success(deptService.listTree());
    }

    /**
     * 获取启用部门树形列表
     *
     * @return 启用部门树
     */
    @GetMapping("/list-enabled")
    @SaCheckPermission(value = SystemPermission.DEPT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysDeptResp>> listEnabled() {
        return ApiResponse.success(deptService.listEnabledTree());
    }

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.DEPT_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysDeptResp> get(@PathVariable Long id) {
        return ApiResponse.success(deptService.getById(id));
    }

    /**
     * 新增部门
     *
     * @param req 部门信息
     * @return 空响应
     */
    @OperationLog(action = "新增部门")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.DEPT_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> add(@RequestBody @Valid SysDeptCreateReq req) {
        deptService.create(req);
        return ApiResponse.success();
    }

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param req 部门信息
     * @return 空响应
     */
    @OperationLog(action = "更新部门")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.DEPT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysDeptUpdateReq req) {
        deptService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改部门状态 */
    @OperationLog(action = "修改部门状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.DEPT_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        deptService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /**
     * 删除部门（递归删除所有下级部门，关联用户的部门字段将重置为空）
     *
     * @param id 部门ID
     * @return 空响应
     */
    @OperationLog(action = "删除部门")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.DEPT_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.deleteById(id);
        return ApiResponse.success();
    }
}
