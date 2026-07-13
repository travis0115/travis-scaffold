package com.travis.monolith.system.message.api;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import org.springframework.validation.annotation.Validated;

/** 消息推送模块对外发布 API。 */
@Validated
public interface SysMessageApi {

    void publishToUsers(
            @NotBlank(message = "消息标题不能为空") String title,
            @NotBlank(message = "消息内容不能为空") String content,
            Collection<@NotNull(message = "用户ID不能为空") Long> userIds);

    void publishToUsers(
            @NotBlank(message = "消息标题不能为空") String title,
            @NotBlank(message = "消息内容不能为空") String content,
            Collection<@NotNull(message = "用户ID不能为空") Long> userIds,
            @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误") String sourceType,
            @Size(max = 64, message = "来源ID长度不能超过64个字符") String sourceId);

    void publishToUsers(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotBlank(message = "消息标题不能为空") String title,
            @NotBlank(message = "消息内容不能为空") String content,
            Collection<@NotNull(message = "用户ID不能为空") Long> userIds,
            @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误") String sourceType,
            @Size(max = 64, message = "来源ID长度不能超过64个字符") String sourceId);

    void publishSourceMessage(
            @NotNull(message = "来源消息发布请求不能为空") @Valid SysSourceMessagePublishReq req);

    void revokeSourceMessage(
            @NotBlank(message = "来源类型不能为空")
                    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
                    String sourceType,
            @NotBlank(message = "来源ID不能为空") @Size(max = 64, message = "来源ID长度不能超过64个字符")
                    String sourceId,
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType);

    void deleteSourceMessage(
            @NotBlank(message = "来源类型不能为空")
                    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
                    String sourceType,
            @NotBlank(message = "来源ID不能为空") @Size(max = 64, message = "来源ID长度不能超过64个字符")
                    String sourceId,
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType);
}
