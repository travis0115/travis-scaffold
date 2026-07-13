package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 用户收件箱分页响应对象。 */
@Data
public class SysUserMessageResp {
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
}
