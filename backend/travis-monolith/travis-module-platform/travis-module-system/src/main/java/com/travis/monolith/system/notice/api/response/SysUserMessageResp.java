package com.travis.monolith.system.notice.api.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysUserMessageResp {
    private Long id;
    private Long noticeId;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
