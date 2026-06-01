package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.req.SysRoleMenuReq;
import com.travis.monolith.system.internal.model.req.SysRoleReq;
import com.travis.monolith.system.internal.model.resp.SysRoleResp;
import com.travis.monolith.system.internal.service.SysRoleService;
import jakarta.validation.Valid;
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
public class SysRoleController {

    /**
     * 角色管理服务
     */
    private final SysRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysRoleResp>> page(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(roleService.getRolePage(roleName, roleCode, status, pageNum, pageSize));
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情（含已分配菜单）
     */
    @GetMapping("/{id}")
    public ApiResponse<SysRoleResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleDetail(id));
    }

    /**
     * 新增角色
     *
     * @param req 角色信息
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysRoleReq req) {
        roleService.addRole(req);
        return ApiResponse.success();
    }

    /**
     * 更新角色信息
     *
     * @param id  角色ID
     * @param req 角色信息
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysRoleReq req) {
        roleService.updateRole(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success();
    }

    /**
     * 为角色分配菜单权限
     *
     * @param req 角色菜单分配请求
     * @return 空响应
     */
    @PostMapping("/menus")
    public ApiResponse<Void> assignMenus(@RequestBody @Valid SysRoleMenuReq req) {
        roleService.assignMenus(req);
        return ApiResponse.success();
    }

    /**
     * 获取所有启用角色列表（不分页）
     */
    @GetMapping("/list")
    public ApiResponse<java.util.List<SysRoleResp>> list() {
        return ApiResponse.success(roleService.getEnabledRoleList());
    }
}
