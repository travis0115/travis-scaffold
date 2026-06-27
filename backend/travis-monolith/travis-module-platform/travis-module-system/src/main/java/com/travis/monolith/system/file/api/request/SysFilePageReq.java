package com.travis.monolith.system.file.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFilePageReq extends PageRequest {
    private Long folderId;

    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    @Size(max = 500, message = "原文件名长度不能超过500个字符")
    private String originalName;

    @Size(max = 255, message = "MIME类型长度不能超过255个字符")
    private String mimeType;

    private Long storageConfigId;
}
