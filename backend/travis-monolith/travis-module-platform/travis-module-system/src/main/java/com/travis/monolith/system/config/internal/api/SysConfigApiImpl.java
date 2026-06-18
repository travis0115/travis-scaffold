package com.travis.monolith.system.config.internal.api;

import com.travis.monolith.system.config.api.SysConfigApi;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysConfigApiImpl implements SysConfigApi {

    private final SysConfigService configService;

    @Override
    public String getValue(String configKey) {
        return configService.getValueByKey(configKey);
    }
}
