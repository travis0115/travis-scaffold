package com.travis.monolith.system.file.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFilePageReq extends PageRequest {
    private Long folderId;
    private Boolean unclassified;
    private String fileName;
    private String originalName;
    private String mimeType;
    private Long storageConfigId;
}
