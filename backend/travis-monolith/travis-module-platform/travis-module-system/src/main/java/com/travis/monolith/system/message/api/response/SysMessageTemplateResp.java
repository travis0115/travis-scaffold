package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysMessageTemplateResp {
    private Long id;
    private String templateCode;
    private String templateName;
    private String channel;
    private String templateType;
    private String title;
    private String platformTemplateId;
    private String contentSchema;
    private String content;
    private String redirectUrl;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
