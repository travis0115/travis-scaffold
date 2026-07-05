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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AppAuthController {

    private final AppAuthService authService;

    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody @Valid AppUserLoginReq req) {
        authService.login(req);
        return ApiResponse.success();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }

    @PostMapping("/ws-ticket")
    public ApiResponse<AppWebSocketTicketResp> createWebSocketTicket() {
        return ApiResponse.success(authService.createWebSocketTicket());
    }

    @GetMapping("/user-info")
    public ApiResponse<AppUserInfoResp> getUserInfo() {
        return ApiResponse.success(authService.getUserInfo());
    }
}
