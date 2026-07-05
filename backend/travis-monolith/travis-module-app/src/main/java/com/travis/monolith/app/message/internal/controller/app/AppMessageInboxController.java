package com.travis.monolith.app.message.internal.controller.app;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.message.api.SysMessageInboxApi;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message/inbox")
@RequiredArgsConstructor
public class AppMessageInboxController {

    private final SysMessageInboxApi messageInboxApi;

    @GetMapping("/recent")
    public ApiResponse<List<SysUserMessageRecentResp>> recent(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(
                messageInboxApi.listRecent(LoginType.APP, currentUserId(), limit));
    }

    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserMessagePageResp>> page(SysUserMessagePageReq req) {
        return ApiResponse.success(messageInboxApi.page(LoginType.APP, currentUserId(), req));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(
                Map.of("count", messageInboxApi.countUnread(LoginType.APP, currentUserId())));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        messageInboxApi.markRead(LoginType.APP, currentUserId(), id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        messageInboxApi.markAllRead(LoginType.APP, currentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageInboxApi.delete(LoginType.APP, currentUserId(), id);
        return ApiResponse.success();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        messageInboxApi.clear(LoginType.APP, currentUserId());
        return ApiResponse.success();
    }

    private Long currentUserId() {
        return StpKit.of(LoginType.APP).getLoginIdAsLong();
    }
}
