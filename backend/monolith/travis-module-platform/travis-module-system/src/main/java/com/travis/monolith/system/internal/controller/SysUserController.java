package com.travis.monolith.system.internal.controller;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.req.SysUserReq;
import com.travis.monolith.system.internal.model.req.SysUserRoleReq;
import com.travis.monolith.system.internal.model.resp.SysUserResp;
import com.travis.monolith.system.internal.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理控制器，提供管理员账号的增删改查及角色分配接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SysUserController {

    /**
     * 用户管理服务
     */
    private final SysUserService userService;

    /**
     * 分页查询用户列表
     *
     * @param username 用户名（模糊匹配）
     * @param phone    手机号（模糊匹配）
     * @param status   状态
     * @param deptId   所属部门ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SysUserResp>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(userService.getUserPage(username, phone, status, deptId, pageNum, pageSize));
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情（含角色信息）
     */
    @GetMapping("/{id}")
    public ApiResponse<SysUserResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    /**
     * 新增用户
     *
     * @param req 用户信息
     * @return 空响应
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid SysUserReq req) {
        userService.addUser(req);
        return ApiResponse.success();
    }

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param req 用户信息
     * @return 空响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid SysUserReq req) {
        userService.updateUser(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    /**
     * 为用户分配角色
     *
     * @param req 用户角色分配请求
     * @return 空响应
     */
    @PostMapping("/roles")
    public ApiResponse<Void> assignRoles(@RequestBody @Valid SysUserRoleReq req) {
        userService.assignRoles(req);
        return ApiResponse.success();
    }
}
