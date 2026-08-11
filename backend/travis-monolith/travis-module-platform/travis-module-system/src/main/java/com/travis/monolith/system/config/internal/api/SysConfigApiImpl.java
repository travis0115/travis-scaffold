package com.travis.monolith.system.config.internal.api;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
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
            config = configService.getByKeyOrThrow(configKey);
        } catch (BizException exception) {
            if (exception.getErrorCode() == CommonErrorCode.DATABASE_RECORD_NOT_FOUND) {
                log.warn("SysConfig is not found, configKey={}", configKey);
                return "";
            }
            throw exception;
        }
        return config.getConfigValue();
    }
}
