package com.travis.monolith.system.message.api;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverType;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

/** 消息收件箱对外 API。 */
@Validated
public interface SysMessageInboxApi {

    /** 查询指定用户的最近消息。 */
    List<SysUserMessageResp> listRecent(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            Integer limit);

    /** 分页查询指定用户的收件箱消息。 */
    PageResp<SysUserMessageResp> page(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "分页查询请求不能为空") @Valid SysUserMessagePageReq req);

    /** 查询指定用户的收件箱消息详情，并在首次查看时标记为已读。 */
    SysUserMessageResp getAndMarkRead(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    /** 统计指定用户的未读消息数。 */
    Long countUnread(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);

    /** 将指定用户的一条消息标记为已读。 */
    void markRead(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    /** 将指定用户的全部消息标记为已读。 */
    void markAllRead(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);

    /** 删除指定用户的一条收件箱消息。 */
    void delete(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId,
            @NotNull(message = "消息ID不能为空") Long id);

    /** 清空指定用户的收件箱消息。 */
    void clear(
            @NotBlank(message = "接收端不能为空")
                    @EnumValue(value = SysMessageReceiverType.class, message = "接收端错误")
                    String receiverType,
            @NotNull(message = "用户ID不能为空") Long userId);
}
