package com.travis.monolith.system.user.internal.controller.admin;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.user.api.request.ChangePasswordReq;
import com.travis.monolith.system.user.api.request.ResetPasswordReq;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserPageReq;
import com.travis.monolith.system.user.api.request.SysUserRoleReq;
import com.travis.monolith.system.user.api.request.SysUserUpdateReq;
import com.travis.monolith.system.user.api.request.UpdateAvatarReq;
import com.travis.monolith.system.user.api.request.UserProfileReq;
import com.travis.monolith.system.user.api.response.SysUserDetailResp;
import com.travis.monolith.system.user.api.response.SysUserPageResp;
import com.travis.monolith.system.user.internal.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理控制器，提供管理员账号的增删改查及角色分配接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
@OperationLogModule("用户管理")
public class SysUserController {

    /** 用户管理服务 */
    private final SysUserService userService;

    /**
     * 分页查询用户列表
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserPageResp>> page(@Valid SysUserPageReq req) {
        return ApiResponse.success(userService.page(req));
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情（含角色信息）
     */
    @GetMapping("/{id}")
    public ApiResponse<SysUserDetailResp> getDetail(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    /**
     * 新增用户
     *
     * @param req 用户信息
     * @return 新建用户ID
     */
    @OperationLog(action = "新增用户")
    @NoRepeatSubmit
    @PostMapping
    public ApiResponse<Long> add(@RequestBody @Valid SysUserCreateReq req) {
        return ApiResponse.success(userService.create(req));
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param req 用户信息
     * @return 空响应
     */
    @OperationLog(action = "更新用户")
    @NoRepeatSubmit
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysUserUpdateReq req) {
        userService.update(id, req);
        return ApiResponse.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 空响应
     */
    @OperationLog(action = "删除用户")
    @NoRepeatSubmit
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 为用户分配角色
     *
     * @param req 用户角色分配请求
     * @return 空响应
     */
    @OperationLog(action = "分配用户角色")
    @NoRepeatSubmit
    @PostMapping("/roles")
    public ApiResponse<Void> assignRoles(@RequestBody @Valid SysUserRoleReq req) {
        userService.assignRoles(req);
        return ApiResponse.success();
    }

    /**
     * 当前登录用户修改个人资料
     *
     * @param req 个人资料请求
     * @return 空响应
     */
    @OperationLog(action = "修改个人资料")
    @NoRepeatSubmit
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody @Valid UserProfileReq req) {
        userService.updateProfile(req);
        return ApiResponse.success();
    }

    /**
     * 当前登录用户更新头像
     *
     * @param req 头像更新请求
     * @return 空响应
     */
    @OperationLog(action = "修改头像")
    @NoRepeatSubmit
    @PutMapping("/avatar")
    public ApiResponse<Void> updateAvatar(@RequestBody @Valid UpdateAvatarReq req) {
        userService.updateAvatar(req);
        return ApiResponse.success();
    }

    /**
     * 当前登录用户修改密码
     *
     * @param req 修改密码请求
     * @return 空响应
     */
    @OperationLog(action = "修改密码", recordRequest = false, recordResponse = false)
    @NoRepeatSubmit
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordReq req) {
        userService.changePassword(req);
        return ApiResponse.success();
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param req 重置密码请求（可选指定新密码，不指定则自动生成随机密码）
     * @return 最终使用的密码（明文，供管理员转达用户）
     */
    @OperationLog(action = "重置用户密码", recordRequest = false, recordResponse = false)
    @NoRepeatSubmit
    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(
            @PathVariable Long id, @RequestBody(required = false) ResetPasswordReq req) {
        String newPassword =
                (req != null && req.getNewPassword() != null && !req.getNewPassword().isBlank())
                        ? req.getNewPassword()
                        : null;
        String resultPassword = userService.resetPassword(id, newPassword);
        return ApiResponse.success(resultPassword);
    }
}
