package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysLoginLog;

/**
 * 登录日志服务接口，提供登录日志的分页查询
 *
 * @author travis
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param username 用户名（模糊匹配，可为空）
     * @param status   登录状态（可为空）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysLoginLog> getLoginLogPage(String username, Integer status, Integer pageNum, Integer pageSize);
}
