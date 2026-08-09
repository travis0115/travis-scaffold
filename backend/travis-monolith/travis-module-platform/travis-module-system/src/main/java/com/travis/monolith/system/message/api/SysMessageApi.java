package com.travis.monolith.system.message.api;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import org.springframework.validation.annotation.Validated;

/** 消息推送模块对外发布 API。 */
@Validated
public interface SysMessageApi {

    /** 向指定登录体系的用户发布站内消息。 */
    void publishToUsers(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotBlank(message = "消息标题不能为空") @Size(max = 255, message = "消息标题长度不能超过255个字符")
                    String title,
            @NotBlank(message = "消息内容不能为空") @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
                    String content,
            @NotEmpty(message = "非全部接收时必须选择接收对象") @Size(max = 1000, message = "接收对象数量不能超过1000个")
                    Collection<
                                    @NotNull(message = "接收对象ID不能为空")
                                    @Positive(message = "接收对象ID必须为正数") Long>
                            userIds);

    /** 按业务来源发布或更新一条消息。 */
    void publishSourceMessage(
            @NotNull(message = "来源消息发布请求不能为空") @Valid SysSourceMessagePublishReq req);

    /** 撤回指定业务来源的消息。 */
    void revokeSourceMessage(
            @NotBlank(message = "来源类型不能为空")
                    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
                    @Pattern(regexp = "NOTICE|VERSION", message = "来源类型仅支持NOTICE或VERSION")
                    String sourceType,
            @NotBlank(message = "来源ID不能为空") @Size(max = 64, message = "来源ID长度不能超过64个字符")
                    String sourceId,
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType);

    /** 删除指定业务来源的消息。 */
    void deleteSourceMessage(
            @NotBlank(message = "来源类型不能为空")
                    @EnumValue(value = SysMessageSourceType.class, message = "来源类型错误")
                    @Pattern(regexp = "NOTICE|VERSION", message = "来源类型仅支持NOTICE或VERSION")
                    String sourceType,
            @NotBlank(message = "来源ID不能为空") @Size(max = 64, message = "来源ID长度不能超过64个字符")
                    String sourceId,
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType);
}
