package com.travis.monolith.system.log.loginlog.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台用户登录日志响应。
 *
 * @author travis
 */
@Data
public class SysLoginLogResp {

    /** 登录日志 ID。 */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 登录IP地址 */
    private String ip;

    /** 登录地点（IP 解析） */
    private String location;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录状态（0-失败 1-成功） */
    private Integer status;

    /** 登录提示信息（成功/失败原因） */
    private String message;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
