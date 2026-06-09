package com.travis.monolith.system.log.loginlog.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysLoginLogPageReq extends PageRequest {
    /** 用户名（模糊匹配） */
    private String username;

    /** 登录状态（0-失败 1-成功） */
    private Integer status;
}
