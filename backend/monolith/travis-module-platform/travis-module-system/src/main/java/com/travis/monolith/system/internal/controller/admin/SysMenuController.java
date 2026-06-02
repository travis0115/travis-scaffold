package com.travis.monolith.system.internal.controller.admin;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import com.travis.monolith.system.internal.service.SysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器，提供菜单树的增删改查接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Validated
public class SysMenuController {

    /**
     * 菜单管理服务
     */
    private final SysMenuService menuService;

    /**
     * 获取菜单树形列表
     *
     * @return 菜单树
     */
    @GetMapping("/list")
    public ApiResponse<List<SysMenuResp>> list() {
        return ApiResponse.success(menuService.getMenuTree());
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SysMenuResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(menuService.getMenuDetail(id));
    }

    /**
     * 新增菜单
     *
     * @param req 菜单信息
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysMenuReq req) {
        menuService.addMenu(req);
        return ApiResponse.success();
    }

    /**
     * 更新菜单信息
     *
     * @param id  菜单ID
     * @param req 菜单信息
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysMenuReq req) {
        menuService.updateMenu(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除菜单（存在子菜单时禁止删除）
     *
     * @param id 菜单ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success();
    }

    /**
     * 上移菜单（与同级上一个菜单交换排序号）
     *
     * @param id 菜单ID
     * @return 空响应
     */
    @PutMapping("/{id}/move-up")
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
    @PutMapping("/{id}/move-down")
    public ApiResponse<Void> moveDown(@PathVariable Long id) {
        menuService.moveDown(id);
        return ApiResponse.success();
    }
}
