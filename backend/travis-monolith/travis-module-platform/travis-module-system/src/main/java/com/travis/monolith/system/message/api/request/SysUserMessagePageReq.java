package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户收件箱分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessagePageReq extends PageRequest {
    @EnumValue(value = SysMessageReadStatus.class, message = "阅读状态错误")
    private Integer readStatus;

    private String title;
}
