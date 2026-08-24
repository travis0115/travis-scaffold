package com.travis.monolith.system.file.internal.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SysFileStorageConfigConverterTest {

    private final SysFileStorageConfigConverter converter =
            Mappers.getMapper(SysFileStorageConfigConverter.class);

    @Test
    void shouldOnlyExposeWhetherSecretIsConfigured() {
        var entity = new SysFileStorageConfig();
        entity.setSecretKey("secret-value");

        var response = converter.toResp(entity);

        assertThat(response.getSecretConfigured()).isTrue();
    }

    @Test
    void shouldReportMissingSecret() {
        var entity = new SysFileStorageConfig();

        var response = converter.toResp(entity);

        assertThat(response.getSecretConfigured()).isFalse();
    }
}
