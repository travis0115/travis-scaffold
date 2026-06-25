package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessagePageReq extends PageRequest {
    private String title;
    private Integer messageType;
    private Integer status;
}
