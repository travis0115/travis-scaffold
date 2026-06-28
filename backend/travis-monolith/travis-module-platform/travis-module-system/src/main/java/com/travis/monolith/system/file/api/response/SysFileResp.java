package com.travis.monolith.system.file.api.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysFileResp {
    private Long id;
    private Long folderId;
    private Long storageConfigId;
    private String storageConfigName;
    private String storageType;
    private Long createBy;
    private String uploaderType;
    private String uploaderName;
    private String fileName;
    private String originalName;
    private String path;
    private String url;
    private String extension;
    private String mimeType;
    private Long size;
    private LocalDateTime createTime;
}
