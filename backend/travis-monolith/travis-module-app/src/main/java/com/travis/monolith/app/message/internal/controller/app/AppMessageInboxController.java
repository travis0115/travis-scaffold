package com.travis.monolith.app.message.internal.controller.app;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.message.api.SysMessageInboxApi;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 客户端用户消息收件箱接口。 */
@RestController
@RequestMapping("/message/inbox")
@RequiredArgsConstructor
public class AppMessageInboxController {

    private final SysMessageInboxApi messageInboxApi;

    /** 查询当前用户的最近消息。 */
    @GetMapping("/recent")
    public ApiResponse<List<SysUserMessageResp>> recent(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(
                messageInboxApi.listRecent(
                        LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong(), limit));
    }

    /** 分页查询当前用户的消息。 */
    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserMessageResp>> page(SysUserMessagePageReq req) {
        return ApiResponse.success(
                messageInboxApi.page(
                        LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong(), req));
    }

    /** 查询消息详情，并在首次查看时标记为已读。 */
    @GetMapping("/{id}")
    public ApiResponse<SysUserMessageResp> get(@PathVariable Long id) {
        var userId = StpKit.of(LoginType.APP).getLoginIdAsLong();
        var message = messageInboxApi.get(LoginType.APP, userId, id);
        if (SysMessageReadStatus.UNREAD.getValue().equals(message.getReadStatus())) {
            messageInboxApi.markRead(LoginType.APP, userId, id);
            message.setReadStatus(SysMessageReadStatus.READ.getValue());
            message.setReadTime(LocalDateTime.now());
        }
        return ApiResponse.success(message);
    }

    /** 统计当前用户的未读消息数。 */
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(
                Map.of(
                        "count",
                        messageInboxApi.countUnread(
                                LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong())));
    }

    /** 将指定消息标记为已读。 */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        messageInboxApi.markRead(LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong(), id);
        return ApiResponse.success();
    }

    /** 将当前用户的全部消息标记为已读。 */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        messageInboxApi.markAllRead(LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong());
        return ApiResponse.success();
    }

    /** 删除当前用户的指定消息。 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageInboxApi.delete(LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong(), id);
        return ApiResponse.success();
    }

    /** 清空当前用户的全部消息。 */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        messageInboxApi.clear(LoginType.APP, StpKit.of(LoginType.APP).getLoginIdAsLong());
        return ApiResponse.success();
    }
}
