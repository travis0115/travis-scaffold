package com.travis.monolith.system.file.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.enums.IsDefault;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysFileStorageConfigUpdateReq {
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    @NotBlank(message = "存储类型不能为空")
    @EnumValue(value = FileStorageType.class, message = "存储类型错误")
    private String storageType;

    @NotBlank(message = "存储路径不能为空")
    @Size(max = 500, message = "存储路径长度不能超过500")
    private String storagePath;

    @Size(max = 500, message = "域名长度不能超过500")
    private String domain;

    @Size(max = 500, message = "endpoint长度不能超过500")
    private String endpoint;

    @Size(max = 100, message = "region长度不能超过100")
    private String region;

    @Size(max = 200, message = "bucketId长度不能超过200")
    private String bucketId;

    @Size(max = 200, message = "bucketName长度不能超过200")
    private String bucketName;

    @Size(max = 500, message = "accessKey长度不能超过500")
    private String accessKey;

    @Size(max = 1000, message = "secretKey长度不能超过1000")
    private String secretKey;

    @Size(max = 5000, message = "扩展配置长度不能超过5000")
    private String meta;

    @NotNull(message = "是否默认不能为空")
    @EnumValue(value = IsDefault.class, message = "是否默认错误")
    private Integer isDefault;

    @NotNull(message = "状态不能为空")
    @EnumValue(value = Status.class, message = "状态错误")
    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    @AssertTrue(message = "对象存储必须配置Bucket名称")
    public boolean isObjectBucketValid() {
        return isLocalStorage() || StrUtil.isNotBlank(bucketName);
    }

    @AssertTrue(message = "对象存储必须配置endpoint或region")
    public boolean isObjectEndpointValid() {
        return isLocalStorage() || StrUtil.isNotBlank(endpoint) || StrUtil.isNotBlank(region);
    }

    @AssertTrue(message = "对象存储必须配置访问凭据")
    public boolean isObjectCredentialValid() {
        return isLocalStorage() || (StrUtil.isNotBlank(accessKey) && StrUtil.isNotBlank(secretKey));
    }

    private boolean isLocalStorage() {
        return FileStorageType.LOCAL.getValue().equals(storageType);
    }
}
