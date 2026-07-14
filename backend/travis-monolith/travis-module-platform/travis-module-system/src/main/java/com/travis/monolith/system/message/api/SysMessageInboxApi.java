package com.travis.monolith.system.message.api;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** 消息收件箱对外 API。 */
@Validated
public interface SysMessageInboxApi {

    List<SysUserMessageResp> listRecent(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            Integer limit);

    PageResp<SysUserMessageResp> page(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "分页查询请求不能为空") @Valid SysUserMessagePageReq req);

    SysUserMessageResp get(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    Long countUnread(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);

    void markRead(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    void markAllRead(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);

    void delete(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    void clear(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);
}
