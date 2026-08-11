package com.travis.monolith.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * @author Travis
 */
@ApplicationModuleTest
public class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(MonolithServerApplication.class);

    @Autowired private CacheManager cacheManager;

    // 验证模块结构
    @Test
    void verifyModuleStructure() {
        modules.forEach(System.out::println);
        modules.verify(); // 验证模块隔离性
    }

    @Test
    void shouldAllowCachingNullValues() {
        var cache = cacheManager.getCache("system:user");
        assertThat(cache).isNotNull();
        var targetCache = (RedisCache) ((TransactionAwareCacheDecorator) cache).getTargetCache();
        assertThat(targetCache.getCacheConfiguration().getAllowCacheNullValues()).isTrue();
        var key = "test:null-value";
        try {
            cache.put(key, null);
            assertThat(cache.get(key)).isNotNull();
            assertThat(cache.get(key).get()).isNull();
        } finally {
            cache.evict(key);
        }
    }
}
