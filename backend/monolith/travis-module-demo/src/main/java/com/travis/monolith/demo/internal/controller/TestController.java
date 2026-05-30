package com.travis.monolith.demo.internal.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.monolith.demo.internal.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@Validated
public class TestController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    public TestController(RedisTemplate<String, Object> redisTemplate, UserService userService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestParam @NotNull Long id) {
        StpUtil.login(id);
        return ApiResponse.success();
    }

    @PostMapping("/checkLogin")
    public ApiResponse<?> checkLogin() {
        StpUtil.checkLogin();
        return ApiResponse.success();
    }

}
