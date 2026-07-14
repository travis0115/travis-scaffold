package com.travis.monolith.system.menu.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.api.request.SysMenuUpdateReq;
import com.travis.monolith.system.menu.api.response.SysMenuResp;
import com.travis.monolith.system.menu.internal.service.SysMenuService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单管理控制器，提供菜单树的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Validated
@OperationLogModule("Menu")
public class SysMenuController {

    /** 菜单管理服务 */
    private final SysMenuService menuService;

    /**
     * 获取菜单树形列表
     *
     * @return 菜单树
     */
    @GetMapping("/list")
    @SaCheckPermission(value = SystemPermission.MENU_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<SysMenuResp>> list() {
        return ApiResponse.success(menuService.listTree());
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MENU_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysMenuResp> get(@PathVariable Long id) {
        return ApiResponse.success(menuService.getDetailByIdOrThrow(id));
    }

    /**
     * 新增菜单
     *
     * @param req 菜单信息
     * @return 空响应
     */
    @OperationLog(action = "新增菜单")
    @NoRepeatSubmit
    @PostMapping
    @SaCheckPermission(value = SystemPermission.MENU_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Void> create(@RequestBody @Valid SysMenuCreateReq req) {
        menuService.create(req);
        return ApiResponse.success();
    }

    /**
     * 更新菜单信息
     *
     * @param id 菜单ID
     * @param req 菜单信息
     * @return 空响应
     */
    @OperationLog(action = "更新菜单")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MENU_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysMenuUpdateReq req) {
        menuService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改菜单状态 */
    @OperationLog(action = "修改菜单状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.MENU_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        menuService.updateStatus(id, status);
        return ApiResponse.success();
    }

    /**
     * 删除菜单及其所有子菜单
     *
     * @param id 菜单ID
     * @return 空响应
     */
    @OperationLog(action = "删除菜单")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.MENU_DELETE, type = LoginType.ADMIN)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 上移菜单（与同级上一个菜单交换排序号）
     *
     * @param id 菜单ID
     * @return 空响应
     */
    @OperationLog(action = "上移菜单")
    @NoRepeatSubmit(interval = 1)
    @PutMapping("/{id}/move-up")
    @SaCheckPermission(value = SystemPermission.MENU_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> moveUp(@PathVariable Long id) {
        menuService.moveUp(id);
        return ApiResponse.success();
    }

    /**
     * 下移菜单（与同级下一个菜单交换排序号）
     *
     * @param id 菜单ID
     * @return 空响应
     */
    @OperationLog(action = "下移菜单")
    @NoRepeatSubmit(interval = 1)
    @PutMapping("/{id}/move-down")
    @SaCheckPermission(value = SystemPermission.MENU_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> moveDown(@PathVariable Long id) {
        menuService.moveDown(id);
        return ApiResponse.success();
    }
}
