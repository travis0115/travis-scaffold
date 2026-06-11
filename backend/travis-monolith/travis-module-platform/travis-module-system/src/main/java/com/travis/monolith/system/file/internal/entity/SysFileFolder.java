package com.travis.monolith.system.file.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileFolder extends BaseEntity {
    private Long parentId;
    private String folderName;
    private Integer sort;
}
