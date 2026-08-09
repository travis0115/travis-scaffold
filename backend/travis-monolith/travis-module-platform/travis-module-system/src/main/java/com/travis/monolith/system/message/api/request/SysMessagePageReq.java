package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import jakarta.validation.constraints.AssertTrue;
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

    /** 消息类型。 */
    @EnumValue(value = SysMessageType.class, message = "消息类型错误")
    private Integer messageType;

    /** 推送渠道。 */
    @EnumValue(value = SysMessageChannel.class, message = "推送渠道错误")
    private String channel;

    /** 接收端。 */
    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
    private String receiverType;

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

    /** 后台列表只查询即时推送或定时推送消息。 */
    @AssertTrue(message = "推送方式错误")
    public boolean isManualPushTypeValid() {
        return pushType == null
                || SysMessagePushType.MANUAL.getValue().equals(pushType)
                || SysMessagePushType.SCHEDULED.getValue().equals(pushType);
    }

    /** 发布日期起点不能晚于终点。 */
    @AssertTrue(message = "发布开始日期不能晚于结束日期")
    public boolean isPublishDateRangeValid() {
        return publishStartDate == null
                || publishEndDate == null
                || !publishStartDate.isAfter(publishEndDate);
    }
}
