package com.travis.monolith.system.file.internal.config.properties;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 文件上传配置 */
@Data
@Component
@ConfigurationProperties(prefix = "travis.web.file")
public class FileUploadProperties {

    private String resourceHandler = "/files/**";

    private List<String> allowedExtensions =
            List.of(
                    "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico", "pdf", "doc", "docx",
                    "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md", "json", "xml", "html", "css",
                    "js", "ts", "vue", "zip", "rar", "7z", "tar", "gz", "mp3", "wav", "ogg", "m4a",
                    "mp4", "webm", "mov", "avi", "mkv");

    public Set<String> getNormalizedAllowedExtensions() {
        if (allowedExtensions == null) {
            return Set.of();
        }
        return allowedExtensions.stream()
                .filter(extension -> extension != null && !extension.isBlank())
                .map(extension -> extension.replaceFirst("^\\.", "").toLowerCase())
                .collect(Collectors.toUnmodifiableSet());
    }
}
