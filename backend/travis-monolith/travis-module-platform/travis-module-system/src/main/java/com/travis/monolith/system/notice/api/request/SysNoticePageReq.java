package com.travis.monolith.system.notice.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticePageReq extends PageRequest {
    private String title;
    private Integer noticeType;
    private Integer status;
}
