package com.travis.monolith.app.user.internal.service.impl;

import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.app.user.api.response.AppUserLoginDashboardResp;
import com.travis.monolith.app.user.internal.entity.AppUserLoginLog;
import com.travis.monolith.app.user.internal.mapper.AppUserLoginLogMapper;
import com.travis.monolith.app.user.internal.service.AppUserLoginLogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 客户端用户登录日志服务实现。 */
@Service
public class AppUserLoginLogServiceImpl extends ServiceImplX<AppUserLoginLogMapper, AppUserLoginLog>
        implements AppUserLoginLogService {

    @Override
    public AppUserLoginDashboardResp dashboard() {
        LocalDate today = LocalDate.now();
        long todayLoginUsers =
                baseMapper.selectSuccessfulUserCount(
                        today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return new AppUserLoginDashboardResp(todayLoginUsers);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginLog(
            String username, int status, String message, String ip, String browser, String os) {
        save(
                AppUserLoginLog.builder()
                        .username(username)
                        .ip(ip)
                        .location(Ip2RegionUtil.getRegionByIP(ip))
                        .browser(browser)
                        .os(os)
                        .status(status)
                        .message(message)
                        .loginTime(LocalDateTime.now())
                        .build());
    }
}
