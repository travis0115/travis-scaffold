package com.travis.monolith.system.file.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFile extends BaseEntity {
    private Long folderId;
    private Long storageConfigId;
    private String fileName;
    private String originalName;
    private String path;
    private String url;
    private String extension;
    private String mimeType;
    private Long size;
}
