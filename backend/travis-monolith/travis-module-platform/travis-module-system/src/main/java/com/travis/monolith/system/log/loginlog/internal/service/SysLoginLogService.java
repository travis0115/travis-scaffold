package com.travis.monolith.system.log.loginlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.loginlog.api.request.SysLoginLogPageReq;
import com.travis.monolith.system.log.loginlog.internal.entity.SysLoginLog;

/**
 * 登录日志服务接口，提供登录日志的分页查询和记录
 *
 * @author travis
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    PageResp<SysLoginLog> page(SysLoginLogPageReq req);

    /**
     * 记录登录日志（使用独立事务，不受外层事务回滚影响）
     *
     * @param username 登录用户名
     * @param status 登录状态（0-失败 1-成功）
     * @param message 提示信息
     * @param ip 客户端IP
     * @param browser 浏览器
     * @param os 操作系统
     */
    void recordLoginLog(
            String username, int status, String message, String ip, String browser, String os);
}
