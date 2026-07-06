package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 用户收件箱消息基础响应对象。 */
@Data
public class SysUserMessageBaseResp {
    private Long id;
    private Long messageId;
    private String title;
    private String content;
    private Integer messageType;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
