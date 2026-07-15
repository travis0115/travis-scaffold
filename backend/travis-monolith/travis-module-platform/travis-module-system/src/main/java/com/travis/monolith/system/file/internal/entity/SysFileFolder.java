package com.travis.monolith.system.file.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件夹实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileFolder extends BaseEntity {
    /** 父文件夹 ID，0 表示根目录。 */
    private Long parentId;

    /** 文件夹名称。 */
    private String folderName;

    /** 排序号。 */
    private Integer sort;

    /** 是否为系统内置文件夹。 */
    private Integer isBuiltin;
}
