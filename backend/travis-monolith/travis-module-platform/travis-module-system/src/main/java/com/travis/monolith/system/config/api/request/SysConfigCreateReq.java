package com.travis.monolith.system.config.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置新增请求参数
 *
 * @author travis
 */
@Data
public class SysConfigCreateReq {
    /** 配置键 */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 255, message = "配置键长度不能超过255个字符")
    private String configKey;

    /** 配置值 */
    @Size(max = 1000, message = "配置值长度不能超过1000个字符")
    private String configValue;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
