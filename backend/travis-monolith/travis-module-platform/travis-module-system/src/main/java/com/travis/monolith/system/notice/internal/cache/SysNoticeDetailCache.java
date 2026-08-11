package com.travis.monolith.system.notice.internal.cache;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** 系统公告原始详情缓存，不缓存动态文件访问地址。 */
@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:notice")
public class SysNoticeDetailCache {

    private final SysNoticeMapper mapper;

    @Cacheable(key = "'detail:'+#id")
    public SysNotice getOrThrow(Long id) {
        var notice = mapper.selectById(id);
        if (notice == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        return notice;
    }
}
