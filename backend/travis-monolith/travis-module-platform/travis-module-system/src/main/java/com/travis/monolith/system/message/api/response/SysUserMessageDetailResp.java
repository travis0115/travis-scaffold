package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 用户收件箱消息详情响应。 */
@Data
public class SysUserMessageDetailResp {
    private Long id;
    private Long messageId;
    private String title;
    private String content;
    private Integer messageType;
    private String sourceType;
    private String sourceId;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private Map<String, Object> metadata;
}
