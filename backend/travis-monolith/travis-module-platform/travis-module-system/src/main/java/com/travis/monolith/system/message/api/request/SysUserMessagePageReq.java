package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户收件箱分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessagePageReq extends PageRequest {
    @EnumValue(value = SysMessageType.class, message = "消息类型错误")
    private Integer messageType;

    @EnumValue(value = SysMessageReadStatus.class, message = "阅读状态错误")
    private Integer readStatus;

    private String title;
}
