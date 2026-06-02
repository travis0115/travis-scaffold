package com.travis.monolith.system.internal.model.entity;

import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体，对应 sys_config 表，存储系统级键值对配置
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysConfig extends BaseEntity {
    /**
     * 配置分组
     */
    private String configGroup;
    /**
     * 配置键（唯一标识）
     */
    private String configKey;
    /**
     * 配置值
     */
    private String configValue;
    /**
     * 备注
     */
    private String remark;
}
