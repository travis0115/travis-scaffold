package com.travis.monolith.system.internal.model.request.config;

import com.travis.infrastructure.common.web.model.PageRequest;
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
    /** 配置分组（模糊匹配） */
    private String configGroup;

    /** 配置键（模糊匹配） */
    private String configKey;
}
