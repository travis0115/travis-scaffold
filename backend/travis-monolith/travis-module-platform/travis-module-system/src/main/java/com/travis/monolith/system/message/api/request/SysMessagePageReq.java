package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 消息推送分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessagePageReq extends PageRequest {
    @Size(max = 255, message = "消息标题长度不能超过255个字符")
    private String title;

    @EnumValue(value = SysMessagePushType.class, message = "推送方式错误")
    private Integer pushType;

    @EnumValue(value = SysMessageStatus.class, message = "消息状态错误")
    private Integer status;

    private LocalDate publishStartDate;
    private LocalDate publishEndDate;
}
