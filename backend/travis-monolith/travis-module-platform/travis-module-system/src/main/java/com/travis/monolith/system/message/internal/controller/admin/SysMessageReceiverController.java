package com.travis.monolith.system.message.internal.controller.admin;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.ApiResponse;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 管理后台消息收件箱控制器。 */
@RestController
@RequestMapping("/system/message/inbox")
@RequiredArgsConstructor
@Validated
public class SysMessageReceiverController {
    private final SysMessageReceiverService messageService;

    @GetMapping("/recent")
    public ApiResponse<List<SysUserMessageRecentResp>> recent(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(messageService.listRecent(currentUserId(), limit));
    }

    @GetMapping("/page")
    public ApiResponse<PageResp<SysUserMessageResp>> page(@Valid SysUserMessagePageReq req) {
        return ApiResponse.success(messageService.page(currentUserId(), req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUserMessageDetailResp> get(@PathVariable Long id) {
        return ApiResponse.success(messageService.get(currentUserId(), id));
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
