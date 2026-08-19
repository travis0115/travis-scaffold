package com.travis.monolith.ops.errorlog.internal.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.infrastructure.common.monitor.error.ErrorSource;
import org.junit.jupiter.api.Test;

class ErrorLogClassifierTest {

    @Test
    void shouldIgnoreDynamicMessageAndUseFirstStackFrameForFingerprint() {
        var first =
                ErrorLogClassifier.fingerprint(
                        ErrorSource.WEB,
                        "com.travis.monolith.system.user.UserService#create",
                        IllegalStateException.class.getName(),
                        "java.lang.IllegalStateException: user 1\n\tat com.travis.User.create(User.java:10)");
        var second =
                ErrorLogClassifier.fingerprint(
                        ErrorSource.WEB,
                        "com.travis.monolith.system.user.UserService#create",
                        IllegalStateException.class.getName(),
                        "java.lang.IllegalStateException: user 2\n\tat com.travis.User.create(User.java:10)");

        assertThat(first).isEqualTo(second).hasSize(64);
    }

    @Test
    void shouldDeriveModuleAndPlatformFromKnownBoundaries() {
        assertThat(
                        ErrorLogClassifier.moduleName(
                                ErrorSource.WEB,
                                "com.travis.monolith.ops.job.internal.controller.JobController#run"))
                .isEqualTo("ops.job");
        assertThat(ErrorLogClassifier.platformType(ErrorSource.WEB, "/api/admin/ops/job"))
                .isEqualTo("ADMIN");
        assertThat(ErrorLogClassifier.platformType(ErrorSource.QUARTZ, null)).isEqualTo("SYSTEM");
    }
}
