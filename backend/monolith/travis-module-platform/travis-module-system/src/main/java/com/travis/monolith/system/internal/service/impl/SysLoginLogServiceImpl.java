package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.mapper.SysLoginLogMapper;
import com.travis.monolith.system.internal.model.entity.SysLoginLog;
import com.travis.monolith.system.internal.service.SysLoginLogService;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现，按登录时间倒序分页查询
 *
 * @author travis
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    /**
     * 分页查询登录日志，支持按用户名、状态筛选，按登录时间倒序排列
     */
    @Override
    public PageResult<SysLoginLog> getLoginLogPage(String username, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(username != null, SysLoginLog::getUsername, username)
                .eq(status != null, SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize(), (int) page.getPages());
    }
}
