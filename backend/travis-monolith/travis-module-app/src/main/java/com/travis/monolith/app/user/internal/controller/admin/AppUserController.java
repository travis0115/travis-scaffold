package com.travis.monolith.app.user.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 客户端用户后台查询接口。 */
@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
@Validated
public class AppUserController {
    private final AppUserService userService;

    @GetMapping("/options")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<AppUserOptionResp>> options(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(userService.listOptions(keyword, limit));
    }

    @GetMapping("/options/by-ids")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<AppUserOptionResp>> optionsByIds(@RequestParam List<Long> ids) {
        return ApiResponse.success(userService.listOptionsByIds(ids));
    }
}
