package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplatePageReq extends PageRequest {
    private String templateCode;
    private String templateName;
    private String channel;
    private Integer status;
}
