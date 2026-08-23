package com.travis.monolith.system.user.api.event;

import lombok.Builder;

/**
 * 用户登录事件，由认证服务在登录成功或失败时发布
 *
 * @author travis
 * @param username 登录用户名
 * @param status 登录状态
 * @param message 登录结果提示
 * @param ip 客户端 IP 地址
 * @param browser 浏览器信息
 * @param os 操作系统信息
 */
@Builder
public record UserLoginEvent(
        String username,
        int status,
        String message,
        String ip,
        String browser,
        String os) {}
