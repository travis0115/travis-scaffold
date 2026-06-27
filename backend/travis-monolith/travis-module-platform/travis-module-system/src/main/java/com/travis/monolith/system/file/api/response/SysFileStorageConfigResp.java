package com.travis.monolith.system.file.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 文件存储配置响应 */
@Data
public class SysFileStorageConfigResp {
    private Long id;
    private String configName;
    private String storageType;
    private String storagePath;
    private String domain;
    private String endpoint;
    private String region;
    private String bucketId;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String meta;
    private Integer isDefault;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
