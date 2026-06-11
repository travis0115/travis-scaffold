package com.travis.monolith.system.notice.internal.controller.admin;

import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.notice.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.notice.api.response.SysUserMessageResp;
import com.travis.monolith.system.notice.internal.service.SysUserMessageService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/message")
@RequiredArgsConstructor
public class SysUserMessageController {
    private final SysUserMessageService messageService;

    @GetMapping("/recent")
    public ApiResponse<List<SysUserMessageResp>> recent(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(messageService.listRecent(currentUserId(), limit));
    }

    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserMessageResp>> page(SysUserMessagePageReq req) {
        return ApiResponse.success(messageService.page(currentUserId(), req));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(Map.of("count", messageService.countUnread(currentUserId())));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        messageService.markRead(currentUserId(), id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        messageService.markAllRead(currentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageService.delete(currentUserId(), id);
        return ApiResponse.success();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        messageService.clear(currentUserId());
        return ApiResponse.success();
    }

    private Long currentUserId() {
        return StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
    }
}
