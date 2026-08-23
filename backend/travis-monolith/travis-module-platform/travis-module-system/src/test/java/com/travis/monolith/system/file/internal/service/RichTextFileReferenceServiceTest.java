package com.travis.monolith.system.file.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.file.api.ManagedFileReferenceParser;
import com.travis.monolith.system.file.internal.service.impl.RichTextFileReferenceServiceImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RichTextFileReferenceServiceTest {

    @Test
    void shouldRecognizeNonCanonicalFileReferenceAndNormalizeIt() {
        var fileService = mock(SysFileService.class);
        when(fileService.getFileUrlMapByIds(anyCollection())).thenReturn(Map.of(12L, "/a.png"));
        var service = new RichTextFileReferenceServiceImpl(fileService);
        var html = "<p>x</p><IMG DATA-FILE-ID = '12' src=\"/temporary.png\">";

        var normalized = service.stripManagedImageSources(html);

        assertThat(normalized).contains("data-file-id=\"12\"").doesNotContain("src=");
        assertThat(ManagedFileReferenceParser.containsFileId(html, 12L)).isTrue();
    }

    @Test
    void shouldResolveMultipleContentsWithOneBatchQuery() {
        var fileService = mock(SysFileService.class);
        when(fileService.getFileUrlMapByIds(anyCollection()))
                .thenReturn(Map.of(1L, "/1.png", 2L, "/2.png"));
        var service = new RichTextFileReferenceServiceImpl(fileService);

        var result =
                service.resolveManagedImageSources(
                        List.of(
                                "<img data-file-id=\"1\">",
                                "<img data-file-id='2'><img data-file-id=\"1\">"));

        assertThat(result.get(0)).contains("src=\"/1.png\"");
        assertThat(result.get(1)).contains("src=\"/2.png\"").contains("src=\"/1.png\"");
        verify(fileService).getFileUrlMapByIds(anyCollection());
    }
}
