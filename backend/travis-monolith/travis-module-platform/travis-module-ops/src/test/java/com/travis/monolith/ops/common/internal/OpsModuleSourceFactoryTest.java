package com.travis.monolith.ops.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpsModuleSourceFactoryTest {

    @Test
    void shouldExposeOpsRootPackage() {
        assertThat(new OpsModuleSourceFactory().getRootPackages())
                .containsExactly("com.travis.monolith.ops");
    }
}
