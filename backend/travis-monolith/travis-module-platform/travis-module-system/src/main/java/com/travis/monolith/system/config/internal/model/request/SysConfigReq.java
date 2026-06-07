package com.travis.monolith.system.config.internal.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysConfigReq {
    /** 配置分组 */
    @Size(max = 100, message = "配置分组长度不能超过100个字符")
    private String configGroup;

    /** 配置键 */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 200, message = "配置键长度不能超过200个字符")
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
