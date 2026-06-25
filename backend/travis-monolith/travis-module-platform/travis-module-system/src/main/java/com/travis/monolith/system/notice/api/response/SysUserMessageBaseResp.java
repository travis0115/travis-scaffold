package com.travis.monolith.system.notice.api.response;

import java.time.LocalDateTime;
import lombok.Data;

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
