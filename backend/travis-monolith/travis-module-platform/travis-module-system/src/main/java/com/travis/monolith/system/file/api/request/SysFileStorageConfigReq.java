package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysFileStorageConfigReq {
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotBlank(message = "存储类型不能为空")
    private String storageType;

    @NotBlank(message = "存储目录不能为空")
    private String basePath;

    @NotBlank(message = "访问前缀不能为空")
    private String accessPrefix;

    private String domain;
    private Integer isDefault;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
