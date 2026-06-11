package com.travis.monolith.ops.job.internal.controller.admin;

import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.xxljob.core.XxlJobSsoUrlService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 任务调度统一入口。 */
@RestController
@RequestMapping("/ops/job")
@RequiredArgsConstructor
public class OpsJobController {

    private final XxlJobSsoUrlService ssoUrlService;

    @GetMapping("/entry")
    public ApiResponse<Map<String, String>> entry() {
        String userId = StpKit.of(LoginType.ADMIN).getLoginIdAsString();
        return ApiResponse.success(Map.of("url", ssoUrlService.createLoginUrl(userId)));
    }
}
