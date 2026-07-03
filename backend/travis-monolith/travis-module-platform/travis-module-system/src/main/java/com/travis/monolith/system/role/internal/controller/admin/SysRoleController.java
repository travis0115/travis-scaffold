package com.travis.monolith.system.role.internal.controller.admin;

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
import com.travis.monolith.system.role.api.request.SysRoleCreateReq;
import com.travis.monolith.system.role.api.request.SysRoleMenuReq;
import com.travis.monolith.system.role.api.request.SysRolePageReq;
import com.travis.monolith.system.role.api.request.SysRoleUpdateReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器，提供角色的增删改查及菜单分配接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Validated
@OperationLogModule("角色管理")
public class SysRoleController {

    /** 角色管理服务 */
    private final SysRoleService roleService;

    /** 分页查询角色列表 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.ROLE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysRoleResp>> page(@Valid SysRolePageReq req) {
        return ApiResponse.success(roleService.page(req));
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情（含已分配菜单）
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ROLE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysRoleResp> get(@PathVariable Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    /**
     * 新增角色
     *
     * @param req 角色信息
     * @return 空响应
     */
    @OperationLog(action = "新增角色")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.ROLE_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> add(@RequestBody @Valid SysRoleCreateReq req) {
        roleService.create(req);
        return ApiResponse.success();
    }

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param req 角色信息
     * @return 空响应
     */
    @OperationLog(action = "更新角色")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ROLE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysRoleUpdateReq req) {
        roleService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改角色状态 */
    @OperationLog(action = "修改角色状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.ROLE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        roleService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 空响应
     */
    @OperationLog(action = "删除角色")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.ROLE_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 为角色分配菜单权限
     *
     * @param req 角色菜单分配请求
     * @return 空响应
     */
    @OperationLog(action = "分配角色菜单")
    @NoRepeatSubmit
    @PostMapping("/menus")
    @SaCheckPermission(value = SystemPermission.ROLE_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> assignMenus(@RequestBody @Valid SysRoleMenuReq req) {
        roleService.assignMenus(req);
        return ApiResponse.success();
    }

    /** 获取所有启用角色列表（不分页） */
    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.ROLE_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysRoleResp>> list() {
        return ApiResponse.success(roleService.listEnabled());
    }
}
