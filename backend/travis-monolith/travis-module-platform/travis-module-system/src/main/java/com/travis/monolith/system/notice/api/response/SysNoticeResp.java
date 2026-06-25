package com.travis.monolith.system.notice.api.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNoticeResp {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private Integer isPinned;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
}
