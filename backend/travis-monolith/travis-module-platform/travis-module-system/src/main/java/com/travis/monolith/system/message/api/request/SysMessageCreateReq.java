package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import com.travis.monolith.system.message.api.enums.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 新增消息推送请求参数。 */
@Data
public class SysMessageCreateReq {
    @Size(max = 255, message = "消息标题长度不能超过255个字符")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @SanitizeHtml
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
    private String content;

    @EnumValue(value = SysMessageType.class, message = "消息类型错误")
    private Integer messageType;

    @NotNull(message = "推送方式不能为空")
    @EnumValue(value = SysMessagePushType.class, message = "推送方式错误")
    private Integer pushType;

    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
    private String sourceType;

    private String sourceId;

    @NotBlank(message = "推送通道不能为空")
    @EnumValue(value = SysMessageChannel.class, message = "推送通道错误")
    private String channel;

    @Size(max = 500, message = "跳转链接长度不能超过500个字符")
    private String jumpUrl;

    private Long templateId;

    @Size(max = 4000, message = "模板参数长度不能超过4000个字符")
    @JsonValue(message = "模板参数必须是合法JSON对象")
    private String templateParams;

    /** 接收端登录体系，与 LoginType 常量取值保持一致：admin/app */
    @NotBlank(message = "接收端不能为空")
    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
    private String receiverType;

    /** 接收范围：0-全部用户 1-指定用户 2-指定角色 3-指定部门 */
    @NotNull(message = "接收范围不能为空")
    @EnumValue(value = SysMessageReceiverScope.class, message = "接收范围错误")
    private Integer receiverScope;

    private List<Long> receiverValues;

    private LocalDateTime publishTime;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    @AssertTrue(message = "客户端用户不支持按角色或部门接收")
    public boolean isAppReceiverScopeValid() {
        return !LoginType.APP.equals(receiverType)
                || SysMessageReceiverScope.ALL.getValue().equals(receiverScope)
                || SysMessageReceiverScope.USER.getValue().equals(receiverScope);
    }

    @AssertTrue(message = "非全部接收时必须选择接收对象")
    public boolean isReceiverValuesValid() {
        return SysMessageReceiverScope.ALL.getValue().equals(receiverScope)
                || (receiverValues != null && !receiverValues.isEmpty());
    }

    @AssertTrue(message = "消息标题不能为空")
    public boolean isTitleValid() {
        return SysMessageChannel.SMS.getValue().equals(channel)
                || (title != null && !title.isBlank());
    }

    @AssertTrue(message = "非站内信通道必须选择消息模板")
    public boolean isExternalChannelTemplateValid() {
        return !SysMessageChannel.isExternal(channel) || templateId != null;
    }

    @AssertTrue(message = "定时推送必须设置发布时间")
    public boolean isPublishTimeValid() {
        return !SysMessagePushType.SCHEDULED.getValue().equals(pushType) || publishTime != null;
    }
}
