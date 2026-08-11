package com.travis.monolith.system.config.internal.entity;

import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
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
    /** 乐观锁版本号。 */
    @Version private Integer lockVersion;

    /** 配置键（唯一标识） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 是否系统内置（0-否 1-是） */
    private Integer isBuiltin;

    /** 备注 */
    private String remark;
}
