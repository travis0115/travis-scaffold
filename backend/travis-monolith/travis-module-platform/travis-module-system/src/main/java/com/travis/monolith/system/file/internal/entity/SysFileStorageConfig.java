package com.travis.monolith.system.file.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileStorageConfig extends BaseEntity {
    private String configName;
    private String storageType;
    private String basePath;
    private String accessPrefix;
    private String domain;
    private Integer isDefault;
    private Integer status;
    private String remark;
}
