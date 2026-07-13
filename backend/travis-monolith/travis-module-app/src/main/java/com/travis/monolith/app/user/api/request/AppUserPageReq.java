package com.travis.monolith.app.user.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 客户端用户分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppUserPageReq extends PageRequest {
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    @Size(max = 11, message = "手机号长度不能超过11个字符")
    private String mobile;
}
