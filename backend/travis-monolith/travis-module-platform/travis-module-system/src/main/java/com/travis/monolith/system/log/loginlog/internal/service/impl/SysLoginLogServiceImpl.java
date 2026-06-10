package com.travis.monolith.system.log.loginlog.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.log.loginlog.api.request.SysLoginLogPageReq;
import com.travis.monolith.system.log.loginlog.internal.entity.SysLoginLog;
import com.travis.monolith.system.log.loginlog.internal.mapper.SysLoginLogMapper;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志服务实现，按登录时间倒序分页查询，记录登录日志
 *
 * @author travis
 */
@Slf4j
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog>
        implements SysLoginLogService {

    private static final Map<String, SFunction<SysLoginLog, ?>> SORT_COLUMNS =
            Map.of(
                    "id", SysLoginLog::getId,
                    "username", SysLoginLog::getUsername,
                    "ip", SysLoginLog::getIp,
                    "status", SysLoginLog::getStatus,
                    "loginTime", SysLoginLog::getLoginTime);

    /** 分页查询登录日志，支持按用户名、状态筛选，按登录时间倒序排列 */
    @Override
    public PageResp<SysLoginLog> page(SysLoginLogPageReq req) {
        LambdaQueryWrapperX<SysLoginLog> wrapper =
                new LambdaQueryWrapperX<SysLoginLog>()
                        .likeIfPresent(SysLoginLog::getUsername, req.getUsername())
                        .eqIfPresent(SysLoginLog::getStatus, req.getStatus())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysLoginLog::getLoginTime);
        Page<SysLoginLog> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page);
    }

    /** 记录登录日志，使用 REQUIRES_NEW 独立事务，确保日志不受外层事务回滚影响 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginLog(
            String username, int status, String message, String ip, String browser, String os) {
        try {
            SysLoginLog loginLog =
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
        } catch (Exception e) {
            // 日志记录失败不影响登录流程
            log.warn("记录登录日志失败: {}", e.getMessage());
        }
    }
}
