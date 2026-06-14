package com.travis.monolith.system.notice.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SysNoticeDetailResp {
    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer status;
    private Integer audienceType;
    private List<Long> targetIds;
    private LocalDateTime publishTime;
    private String remark;
    private LocalDateTime createTime;
}
