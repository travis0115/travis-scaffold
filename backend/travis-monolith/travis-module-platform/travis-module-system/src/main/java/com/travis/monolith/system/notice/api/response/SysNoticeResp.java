package com.travis.monolith.system.notice.api.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysNoticeResp {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private Integer pinned;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
}
