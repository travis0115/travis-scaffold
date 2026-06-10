package com.travis.monolith.system.user.api.event;

import lombok.Builder;

/**
 * 用户登录事件载荷，由认证服务在登录成功或失败时发布
 *
 * @author travis
 */
@Builder
public record UserLoginPayload(
        /* 登录用户名 */
        String username,
        /* 登录状态（0-失败 1-成功） */
        int status,
        /* 提示信息 */
        String message,
        /* 客户端 IP 地址（发布端在 Web 线程捕获） */
        String ip,
        /* 浏览器信息（发布端在 Web 线程捕获） */
        String browser,
        /* 操作系统信息（发布端在 Web 线程捕获） */
        String os) {}
