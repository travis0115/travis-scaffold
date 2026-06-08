package com.travis.monolith.system.user.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应视图，返回令牌信息
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserLoginResp {
    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;
}
