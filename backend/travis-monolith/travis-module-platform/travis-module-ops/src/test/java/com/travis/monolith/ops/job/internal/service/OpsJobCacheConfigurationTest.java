package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.monolith.ops.job.api.enums.OpsJobDashboardRange;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobDashboardServiceImpl;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobLogServiceImpl;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobServiceImpl;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

class OpsJobCacheConfigurationTest {

    @Test
    void shouldCacheDashboardAndEvictItAfterJobMutation() throws Exception {
        assertThat(cacheNames(OpsJobDashboardServiceImpl.class))
                .containsExactly("ops:job-dashboard");
        assertThat(
                        method(
                                        OpsJobDashboardServiceImpl.class,
                                        "dashboard",
                                        OpsJobDashboardRange.class)
                                .getAnnotation(Cacheable.class)
                                .key())
                .isEqualTo("#range.name() + ':' + T(java.time.LocalDate).now().toString()");
        var eviction =
                method(OpsJobServiceImpl.class, "create", OpsJobCreateReq.class)
                        .getAnnotation(CacheEvict.class);
        assertThat(eviction.cacheNames()).containsExactly("ops:job-dashboard");
        assertThat(eviction.allEntries()).isTrue();
    }

    @Test
    void shouldNotCacheExecutionConfigOrLogDetail() throws Exception {
        assertThat(OpsJobServiceImpl.class.getAnnotation(CacheConfig.class)).isNull();
        assertThat(
                        method(OpsJobServiceImpl.class, "findExecutionConfig", Long.class)
                                .getAnnotation(Cacheable.class))
                .isNull();
        assertThat(
                        method(OpsJobLogServiceImpl.class, "getOrThrow", Long.class)
                                .getAnnotation(Cacheable.class))
                .isNull();
    }

    @Test
    void shouldEvictLogCachesAfterExecutionChanges() throws Exception {
        var cacheable =
                method(OpsJobLogServiceImpl.class, "stats", Long.class)
                        .getAnnotation(Cacheable.class);
        assertThat(cacheable.cacheNames()).containsExactly("ops:job-stats");
        assertThat(cacheable.key()).isEqualTo("#jobId");

        var updateCaching =
                method(OpsJobLogServiceImpl.class, "updateExecution", OpsJobLog.class)
                        .getAnnotation(Caching.class);
        assertThat(updateCaching.evict())
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-stats");
                            assertThat(eviction.key()).isEqualTo("#log.jobId");
                        })
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-dashboard");
                            assertThat(eviction.allEntries()).isTrue();
                        });

        var interruptedCaching =
                method(OpsJobLogServiceImpl.class, "markInterruptedExecutions")
                        .getAnnotation(Caching.class);
        assertThat(interruptedCaching.evict())
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-stats");
                            assertThat(eviction.allEntries()).isTrue();
                        });
    }

    @Test
    void shouldEvictOnlySelectedJobStatsWhenCleaningItsLogs() throws Exception {
        var cleanCaching =
                method(OpsJobLogServiceImpl.class, "clean", Long.class)
                        .getAnnotation(Caching.class);

        assertThat(cleanCaching.evict())
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-stats");
                            assertThat(eviction.key()).isEqualTo("#jobId");
                            assertThat(eviction.condition()).isEqualTo("#jobId != null");
                        })
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-stats");
                            assertThat(eviction.allEntries()).isTrue();
                            assertThat(eviction.condition()).isEqualTo("#jobId == null");
                        });
    }

    @Test
    void shouldEvictSelectedJobStatsWhenDeletingJob() throws Exception {
        var deleteCaching =
                method(OpsJobServiceImpl.class, "delete", Long.class).getAnnotation(Caching.class);

        assertThat(deleteCaching.evict())
                .anySatisfy(
                        eviction -> {
                            assertThat(eviction.cacheNames()).containsExactly("ops:job-stats");
                            assertThat(eviction.key()).isEqualTo("#id");
                        });
    }

    private Method method(Class<?> type, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return type.getMethod(name, parameterTypes);
    }

    private String[] cacheNames(Class<?> type) {
        return type.getAnnotation(CacheConfig.class).cacheNames();
    }
}
