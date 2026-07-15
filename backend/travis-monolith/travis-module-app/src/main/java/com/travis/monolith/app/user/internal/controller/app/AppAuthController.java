package com.travis.monolith.app.user.internal.controller.app;

import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.app.user.api.request.AppUserLoginReq;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.api.response.AppWebSocketTicketResp;
import com.travis.monolith.app.user.internal.service.AppAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 客户端认证接口。 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AppAuthController {

    private final AppAuthService authService;

    /** 登录客户端。 */
    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody @Valid AppUserLoginReq req) {
        authService.login(req);
        return ApiResponse.success();
    }

    /** 退出当前登录会话。 */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }

    /** 创建一次性 WebSocket 握手凭证。 */
    @PostMapping("/ws-ticket")
    public ApiResponse<AppWebSocketTicketResp> createWebSocketTicket() {
        return ApiResponse.success(authService.createWebSocketTicket());
    }

    /** 获取当前登录用户信息。 */
    @GetMapping("/user-info")
    public ApiResponse<AppUserInfoResp> getUserInfo() {
        return ApiResponse.success(authService.getUserInfo());
    }
}
