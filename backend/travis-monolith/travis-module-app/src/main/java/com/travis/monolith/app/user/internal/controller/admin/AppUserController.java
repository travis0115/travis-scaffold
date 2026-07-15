package com.travis.monolith.app.user.internal.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.app.user.api.request.AppUserPageReq;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.constant.SystemPermission;
import jakarta.validation.Valid;
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

    /** 分页查询客户端用户。 */
    @GetMapping("/page")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<PageResp<AppUserOptionResp>> page(@Valid AppUserPageReq req) {
        return ApiResponse.success(userService.page(req));
    }

    /** 根据用户 ID 集合查询选择项。 */
    @GetMapping("/options/by-ids")
    @SaCheckPermission(value = SystemPermission.USER_QUERY, type = LoginType.ADMIN)
    public ApiResponse<List<AppUserOptionResp>> listOptionsByIds(@RequestParam List<Long> ids) {
        return ApiResponse.success(userService.listOptionsByIds(ids));
    }
}
