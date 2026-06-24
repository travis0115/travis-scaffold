package com.travis.monolith.system.config.internal.api;

import com.travis.monolith.system.config.api.SysConfigApi;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SysConfigApiImpl implements SysConfigApi {

    private final SysConfigService configService;

    @Override
    public String getValue(String configKey) {
        SysConfigResp config;
        try {
            config = configService.getByKey(configKey);
        } catch (Exception e) {
            log.warn("SysConfig is not found, configKey={}", configKey, e);
            return "";
        }
        return config.getConfigValue();
    }
}
