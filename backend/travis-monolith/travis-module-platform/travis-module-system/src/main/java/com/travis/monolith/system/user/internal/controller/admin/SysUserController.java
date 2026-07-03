package com.travis.monolith.system.user.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.ImageFile;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.LoginSubjectSessionKey;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.api.constant.FileFolderId;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.user.api.request.*;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.service.SysUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台用户管理控制器，提供管理员账号的增删改查及角色分配接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
@OperationLogModule("User")
public class SysUserController {

    /** 用户管理服务 */
    private final SysUserService userService;

    private final SysFileApi fileApi;

    /**
     * 分页查询用户列表
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<SysUserResp>> page(@Valid SysUserPageReq req) {
        return ApiResponse.success(userService.page(req));
    }

    /** 获取在线用户数量 */
    @GetMapping("/online-count")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<Long> onlineCount() {
        return ApiResponse.success(userService.countOnlineUsers());
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情（含角色信息）
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<SysUserResp> get(@PathVariable Long id) {
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
    @SaCheckPermission(value = SystemPermission.USER_CREATE, type = LoginType.ADMIN)
    public ApiResponse<Long> create(@RequestBody @Valid SysUserCreateReq req) {
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
    @SaCheckPermission(value = SystemPermission.USER_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> update(
            @PathVariable Long id, @RequestBody @Valid SysUserUpdateReq req) {
        userService.update(id, req);
        return ApiResponse.success();
    }

    /** 修改用户状态 */
    @OperationLog(action = "修改用户状态")
    @NoRepeatSubmit
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = SystemPermission.USER_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam @EnumValue(value = Status.class, message = "状态值错误") Integer status) {
        userService.updateStatus(id, status);
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
    @SaCheckPermission(value = SystemPermission.USER_DELETE, type = LoginType.ADMIN)
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
    @SaCheckPermission(value = SystemPermission.USER_UPDATE, type = LoginType.ADMIN)
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
    public ApiResponse<Void> updateProfile(@RequestBody @Valid SysUserProfileReq req) {
        userService.updateProfile(req);
        return ApiResponse.success();
    }

    /** 当前登录用户更新头像 */
    @OperationLog(action = "修改头像")
    @NoRepeatSubmit
    @PutMapping("/avatar")
    public ApiResponse<Void> updateAvatar(
            @RequestParam @NotNull(message = "头像文件ID不能为空") Long avatarFileId) {
        userService.updateAvatar(avatarFileId);
        return ApiResponse.success();
    }

    @OperationLog(action = "上传头像")
    @NoRepeatSubmit
    @PostMapping("/avatar/upload")
    public ApiResponse<FileUploadResp> uploadAvatar(
            @RequestParam("file") @ImageFile MultipartFile file) {
        var username =
                StpKit.of(LoginType.ADMIN).getSession().getString(LoginSubjectSessionKey.USERNAME);
        return ApiResponse.success(
                fileApi.upload(file, FileFolderId.AVATAR, LoginType.ADMIN, username));
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
    public ApiResponse<Void> changePassword(@RequestBody @Valid SysUserChangePasswordReq req) {
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
    @SaCheckPermission(value = SystemPermission.USER_UPDATE, type = LoginType.ADMIN)
    public ApiResponse<String> resetPassword(
            @PathVariable Long id, @RequestBody(required = false) SysUserResetPasswordReq req) {
        var newPassword =
                (req != null && req.getNewPassword() != null && !req.getNewPassword().isBlank())
                        ? req.getNewPassword()
                        : null;
        var resultPassword = userService.resetPassword(id, newPassword);
        return ApiResponse.success(resultPassword);
    }
}
