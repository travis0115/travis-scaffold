package com.travis.monolith.system.file.internal.entity;

import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件存储配置实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileStorageConfig extends BaseEntity {
    /** 乐观锁版本号。 */
    @Version private Integer lockVersion;

    /** 配置名称。 */
    private String configName;

    /** 存储类型。 */
    private String storageType;

    /** 文件在存储介质中的根路径。 */
    private String storagePath;

    /** 文件访问域名。 */
    private String domain;

    /** 对象存储服务端点。 */
    private String endpoint;

    /** 对象存储区域。 */
    private String region;

    /** 对象存储桶 ID。 */
    private String bucketId;

    /** 对象存储桶名称。 */
    private String bucketName;

    /** 对象存储访问密钥 ID。 */
    private String accessKey;

    /** 对象存储访问密钥。 */
    private String secretKey;

    /** 存储提供商扩展配置，使用 JSON 对象格式。 */
    private String meta;

    /** 是否为默认存储配置。 */
    private Integer isDefault;

    /** 配置状态。 */
    private Integer status;

    /** 备注。 */
    private String remark;
}
