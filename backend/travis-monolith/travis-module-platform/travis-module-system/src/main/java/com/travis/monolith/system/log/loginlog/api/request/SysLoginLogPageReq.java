package com.travis.monolith.system.log.loginlog.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysLoginLogPageReq extends PageRequest {

    /** 用户名（模糊匹配） */
    @Size(max = 16, message = "用户名长度不能超过16个字符")
    private String username;

    /** 登录IP（模糊匹配） */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String ip;

    /** 登录状态（0-失败 1-成功） */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;

    /** 登录开始时间 */
    private LocalDateTime startTime;

    /** 登录结束时间 */
    private LocalDateTime endTime;
}
