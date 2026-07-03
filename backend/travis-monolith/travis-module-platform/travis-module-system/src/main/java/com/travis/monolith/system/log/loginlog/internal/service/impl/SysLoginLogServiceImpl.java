package com.travis.monolith.system.log.loginlog.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.log.loginlog.api.request.SysLoginLogPageReq;
import com.travis.monolith.system.log.loginlog.api.response.SysLoginLogResp;
import com.travis.monolith.system.log.loginlog.internal.converter.SysLoginLogConverter;
import com.travis.monolith.system.log.loginlog.internal.entity.SysLoginLog;
import com.travis.monolith.system.log.loginlog.internal.mapper.SysLoginLogMapper;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 登录日志服务实现，按登录时间倒序分页查询，记录登录日志
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImplX<SysLoginLogMapper, SysLoginLog>
        implements SysLoginLogService {

    private final SysLoginLogConverter converter;

    private static final Map<String, SFunction<SysLoginLog, ?>> SORT_COLUMNS =
            Map.of("loginTime", SysLoginLog::getLoginTime);

    /** 分页查询登录日志，支持按用户名、状态筛选，按登录时间倒序排列 */
    @Override
    public PageResp<SysLoginLogResp> page(SysLoginLogPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysLoginLog>()
                        .likeIfPresent(SysLoginLog::getUsername, req.getUsername())
                        .likeIfPresent(SysLoginLog::getIp, req.getIp())
                        .eqIfPresent(SysLoginLog::getStatus, req.getStatus())
                        .geIfPresent(SysLoginLog::getLoginTime, req.getStartTime())
                        .leIfPresent(SysLoginLog::getLoginTime, req.getEndTime())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysLoginLog::getLoginTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 记录登录日志，使用 REQUIRES_NEW 独立事务，确保日志不受外层事务回滚影响 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginLog(
            String username, int status, String message, String ip, String browser, String os) {
        var loginLog =
                SysLoginLog.builder()
                        .username(username)
                        .ip(ip)
                        .location(Ip2RegionUtil.getRegionByIP(ip))
                        .browser(browser)
                        .os(os)
                        .status(status)
                        .message(message)
                        .loginTime(LocalDateTime.now())
                        .build();
        save(loginLog);
    }
}
