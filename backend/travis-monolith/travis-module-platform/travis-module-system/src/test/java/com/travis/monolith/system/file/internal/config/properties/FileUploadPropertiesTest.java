package com.travis.monolith.system.file.internal.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class FileUploadPropertiesTest {

    private static final Set<String> ACTIVE_CONTENT_EXTENSIONS =
            Set.of("html", "css", "js", "ts", "vue", "svg", "xml");

    @Test
    void shouldRejectActiveContentByDefault() {
        FileUploadProperties properties = new FileUploadProperties();

        assertThat(properties.getNormalizedAllowedExtensions())
                .doesNotContainAnyElementsOf(ACTIVE_CONTENT_EXTENSIONS);
    }
}
