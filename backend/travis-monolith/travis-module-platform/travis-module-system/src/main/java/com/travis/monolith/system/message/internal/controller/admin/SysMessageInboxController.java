package com.travis.monolith.system.message.internal.controller.admin;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** 管理后台消息收件箱控制器。 */
@RestController
@RequestMapping("/system/message/inbox")
@RequiredArgsConstructor
@Validated
public class SysMessageInboxController {
    private final SysMessageReceiverService messageService;

    /** 查询当前后台用户的最近消息。 */
    @GetMapping("/recent")
    public ApiResponse<List<SysUserMessageResp>> recent(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(
                messageService.listRecent(
                        LoginType.ADMIN,
                        StpKit.of(LoginType.ADMIN).getLoginIdAsLong(),
                        limit));
    }

    /** 分页查询当前后台用户的收件箱消息。 */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserMessageResp>> page(@Valid SysUserMessagePageReq req) {
        return ApiResponse.success(
                messageService.page(
                        LoginType.ADMIN, StpKit.of(LoginType.ADMIN).getLoginIdAsLong(), req));
    }

    /** 查询消息详情，并在首次查看时标记为已读。 */
    @GetMapping("/{id}")
    public ApiResponse<SysUserMessageResp> get(@PathVariable Long id) {
        var userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        var message = messageService.getOrThrow(LoginType.ADMIN, userId, id);
        if (SysMessageReadStatus.UNREAD.getValue().equals(message.getReadStatus())) {
            messageService.markRead(LoginType.ADMIN, userId, id);
            message.setReadStatus(SysMessageReadStatus.READ.getValue());
            message.setReadTime(LocalDateTime.now());
        }
        return ApiResponse.success(message);
    }

    /** 将指定消息标记为已读。 */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        messageService.markRead(
                LoginType.ADMIN, StpKit.of(LoginType.ADMIN).getLoginIdAsLong(), id);
        return ApiResponse.success();
    }

    /** 将当前后台用户的全部消息标记为已读。 */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        messageService.markAllRead(
                LoginType.ADMIN, StpKit.of(LoginType.ADMIN).getLoginIdAsLong());
        return ApiResponse.success();
    }

    /** 删除当前后台用户的一条收件箱消息。 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageService.delete(
                LoginType.ADMIN, StpKit.of(LoginType.ADMIN).getLoginIdAsLong(), id);
        return ApiResponse.success();
    }

    /** 清空当前后台用户的收件箱消息。 */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        messageService.clear(LoginType.ADMIN, StpKit.of(LoginType.ADMIN).getLoginIdAsLong());
        return ApiResponse.success();
    }
}
