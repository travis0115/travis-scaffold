package com.travis.monolith.system.config.api.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置修改修改请求参数
 *
 * @author travis
 */
@Data
public class SysConfigUpdateReq {

    /** 配置值 */
    @Size(max = 1000, message = "配置值长度不能超过1000个字符")
    private String configValue;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
