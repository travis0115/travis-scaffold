package com.travis.monolith.system.log.loginlog.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.log.loginlog.internal.model.entity.SysLoginLog;

/**
 * 登录日志服务接口，提供登录日志的分页查询和记录
 *
 * @author travis
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param username 用户名（模糊匹配，可为空）
     * @param status 登录状态（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysLoginLog> getLoginLogPage(
            String username, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 记录登录日志（使用独立事务，不受外层事务回滚影响）
     *
     * @param username 登录用户名
     * @param status 登录状态（0-失败 1-成功）
     * @param message 提示信息
     */
    void recordLoginLog(String username, int status, String message);
}
