package com.travis.monolith.system.file.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import com.travis.monolith.system.common.api.enums.IsDefault;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 文件存储配置创建参数。 */
@Data
public class SysFileStorageConfigCreateReq {
    /** 配置名称。 */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    /** 存储类型。 */
    @NotBlank(message = "存储类型不能为空")
    @EnumValue(value = FileStorageType.class, message = "存储类型错误")
    private String storageType;

    /** 文件在存储介质中的根路径。 */
    @NotBlank(message = "存储路径不能为空")
    @Size(max = 500, message = "存储路径长度不能超过500")
    private String storagePath;

    /** 文件访问域名。 */
    @NotBlank(message = "域名不能为空")
    @Size(max = 500, message = "域名长度不能超过500")
    @Pattern(regexp = "^https?://.*", message = "域名必须以http://或https://开头")
    private String domain;

    /** 对象存储服务端点。 */
    @Size(max = 500, message = "endpoint长度不能超过500")
    private String endpoint;

    /** 对象存储区域。 */
    @Size(max = 100, message = "region长度不能超过100")
    private String region;

    /** 对象存储桶 ID。 */
    @Size(max = 200, message = "bucketId长度不能超过200")
    private String bucketId;

    /** 对象存储桶名称。 */
    @Size(max = 200, message = "bucketName长度不能超过200")
    private String bucketName;

    /** 对象存储访问密钥 ID。 */
    @Size(max = 500, message = "accessKey长度不能超过500")
    private String accessKey;

    /** 对象存储访问密钥。 */
    @Size(max = 1000, message = "secretKey长度不能超过1000")
    private String secretKey;

    /** 存储提供商扩展配置，使用 JSON 对象格式。 */
    @Size(max = 5000, message = "扩展配置长度不能超过5000")
    @JsonValue(message = "扩展配置必须是合法JSON对象")
    private String meta;

    /** 是否为默认存储配置。 */
    @NotNull(message = "是否默认不能为空")
    @EnumValue(value = IsDefault.class, message = "是否默认错误")
    private Integer isDefault;

    /** 配置状态。 */
    @NotNull(message = "状态不能为空")
    @EnumValue(value = Status.class, message = "状态错误")
    private Integer status;

    /** 备注。 */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /** 校验对象存储是否配置存储桶名称。 */
    @AssertTrue(message = "对象存储必须配置Bucket名称")
    public boolean isObjectBucketValid() {
        return isLocalStorage() || StrUtil.isNotBlank(bucketName);
    }

    /** 校验对象存储是否配置服务端点或区域。 */
    @AssertTrue(message = "对象存储必须配置endpoint或region")
    public boolean isObjectEndpointValid() {
        return isLocalStorage() || StrUtil.isNotBlank(endpoint) || StrUtil.isNotBlank(region);
    }

    /** 校验对象存储是否同时配置访问密钥 ID 和访问密钥。 */
    @AssertTrue(message = "对象存储必须配置访问凭据")
    public boolean isObjectCredentialValid() {
        return isLocalStorage() || (StrUtil.isNotBlank(accessKey) && StrUtil.isNotBlank(secretKey));
    }

    private boolean isLocalStorage() {
        return FileStorageType.LOCAL.getValue().equals(storageType);
    }
}
