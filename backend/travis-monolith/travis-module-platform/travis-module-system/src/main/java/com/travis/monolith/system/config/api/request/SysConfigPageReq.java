package com.travis.monolith.system.config.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysConfigPageReq extends PageRequest {
    /** 配置键（模糊匹配） */
    @Size(max = 255, message = "配置键长度不能超过255个字符")
    private String configKey;
}
