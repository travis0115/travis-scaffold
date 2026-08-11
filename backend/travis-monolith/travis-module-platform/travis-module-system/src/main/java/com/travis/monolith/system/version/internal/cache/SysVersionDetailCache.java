package com.travis.monolith.system.version.internal.cache;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import com.travis.monolith.system.version.internal.mapper.SysVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** 系统版本原始详情缓存，不缓存动态文件访问地址。 */
@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:version")
public class SysVersionDetailCache {

    private final SysVersionMapper mapper;

    @Cacheable(key = "'detail:'+#id")
    public SysVersion getOrThrow(Long id) {
        var version = mapper.selectById(id);
        if (version == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        return version;
    }
}
