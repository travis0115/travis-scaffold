package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息推送分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessagePageReq extends PageRequest {
    /** 消息标题，支持模糊匹配。 */
    @Size(max = 255, message = "消息标题长度不能超过255个字符")
    private String title;

    /** 推送方式。 */
    @EnumValue(value = SysMessagePushType.class, message = "推送方式错误")
    private Integer pushType;

    /** 消息状态。 */
    @EnumValue(value = SysMessageStatus.class, message = "消息状态错误")
    private Integer status;

    /** 发布日期范围起点。 */
    private LocalDate publishStartDate;

    /** 发布日期范围终点。 */
    private LocalDate publishEndDate;
}
