package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户收件箱分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessagePageReq extends PageRequest {
    /** 消息类型。 */
    @EnumValue(value = SysMessageType.class, message = "消息类型错误")
    private Integer messageType;

    /** 阅读状态。 */
    @EnumValue(value = SysMessageReadStatus.class, message = "阅读状态错误")
    private Integer readStatus;

    /** 消息标题，支持模糊匹配。 */
    @Size(max = 255, message = "消息标题长度不能超过255个字符")
    private String title;

    /** 发布日期范围起点。 */
    private LocalDate publishStartDate;

    /** 发布日期范围终点。 */
    private LocalDate publishEndDate;
}
