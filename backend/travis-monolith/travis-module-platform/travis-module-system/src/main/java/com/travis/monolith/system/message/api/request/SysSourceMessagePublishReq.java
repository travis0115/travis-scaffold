package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 来源消息发布请求。 */
@Data
public class SysSourceMessagePublishReq {
    /** 消息类型。 */
    @NotNull(message = "消息类型不能为空")
    @EnumValue(value = SysMessageType.class, message = "消息类型错误")
    private Integer messageType;

    /** 消息来源类型。 */
    @NotBlank(message = "来源类型不能为空")
    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
    private String sourceType;

    /** 来源业务数据ID。 */
    @NotBlank(message = "来源ID不能为空")
    @Size(max = 64, message = "来源ID长度不能超过64个字符")
    private String sourceId;

    /** 消息标题。 */
    @NotBlank(message = "消息标题不能为空")
    @Size(max = 255, message = "消息标题长度不能超过255个字符")
    private String title;

    /** 接收端登录体系：admin/app。 */
    @NotBlank(message = "接收端不能为空")
    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
    private String receiverType;

    /** 接收范围：0-全部用户 1-指定用户 2-指定角色 3-指定部门。 */
    @NotNull(message = "接收范围不能为空")
    @EnumValue(value = SysMessageReceiverScope.class, message = "接收范围错误")
    private Integer receiverScope;

    /** 接收对象ID列表，全部用户时为空。 */
    private List<Long> receiverValues;

    /** 发布时间。 */
    @NotNull(message = "发布时间不能为空")
    private LocalDateTime publishTime;

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
}
