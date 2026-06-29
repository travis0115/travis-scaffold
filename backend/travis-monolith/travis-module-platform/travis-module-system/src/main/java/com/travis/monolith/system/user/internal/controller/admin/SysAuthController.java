package com.travis.monolith.system.user.internal.controller.admin;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.user.api.request.SysUserLoginReq;
import com.travis.monolith.system.user.api.response.SysUserInfoResp;
import com.travis.monolith.system.user.api.response.SysWebSocketTicketResp;
import com.travis.monolith.system.user.internal.service.SysAuthService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器，提供登录、登出、获取用户信息及权限等接口
 *
 * @author travis
 */
@RestController
@RequestMapping("/system/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SysAuthController {

    /** 认证管理服务 */
    private final SysAuthService sysAuthService;

    /**
     * 管理员登录
     *
     * @param req 登录请求参数
     * @return 空响应，Token 由 Sa-Token 写入响应头
     */
    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody @Valid SysUserLoginReq req) {
        sysAuthService.login(req);
        return ApiResponse.success();
    }

    /**
     * 管理员登出
     *
     * @return 空响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        sysAuthService.logout();
        return ApiResponse.success();
    }

    /** 签发 WebSocket 握手 ticket */
    @PostMapping("/ws-ticket")
    public ApiResponse<SysWebSocketTicketResp> createWebSocketTicket() {
        return ApiResponse.success(sysAuthService.createWebSocketTicket());
    }

    /**
     * 获取当前登录用户的详细信息
     *
     * @return 用户信息（包含角色和权限）
     */
    @GetMapping("/user-info")
    public ApiResponse<SysUserInfoResp> getUserInfo() {
        return ApiResponse.success(sysAuthService.getUserInfo());
    }

    /**
     * 获取当前用户的菜单列表（用于前端路由渲染）
     *
     * @return Vben Admin 格式的菜单树
     */
    @GetMapping("/menus")
    public ApiResponse<List<VbenMenuResp>> listMenus() {
        return ApiResponse.success(sysAuthService.listMenus());
    }

    /**
     * 获取当前用户的权限标识列表（用于前端按钮级权限控制）
     *
     * @return 权限标识列表
     */
    @GetMapping("/codes")
    public ApiResponse<List<String>> getAccessCodes() {
        return ApiResponse.success(sysAuthService.getAccessCodes());
    }
}
