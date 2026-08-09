package com.travis.infrastructure.framework.redis.core.key;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.infrastructure.framework.redis.config.properties.TravisRedisProperties;
import org.junit.jupiter.api.Test;

class RedisKeyPrefixResolverTest {

    @Test
    void shouldBuildDistributedLockKeyWithProjectPrefix() {
        var properties = new TravisRedisProperties();
        properties.setKeyPrefix("travis");
        var resolver = new RedisKeyPrefixResolver(properties);

        String key = resolver.applyLock("system-message", "scheduled-push-reconcile");

        assertThat(key).isEqualTo("travis:lock:system-message:scheduled-push-reconcile");
    }
}
