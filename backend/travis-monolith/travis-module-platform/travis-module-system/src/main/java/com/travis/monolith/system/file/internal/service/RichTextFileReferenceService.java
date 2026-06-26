package com.travis.monolith.system.file.internal.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 富文本中的系统文件图片引用处理 */
@Service
@RequiredArgsConstructor
public class RichTextFileReferenceService {

    private static final Pattern IMG_TAG_PATTERN =
            Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_FILE_ID_PATTERN =
            Pattern.compile("\\sdata-file-id\\s*=\\s*([\"'])(\\d+)\\1", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN =
            Pattern.compile("\\ssrc\\s*=\\s*([\"']).*?\\1", Pattern.CASE_INSENSITIVE);

    private final SysFileService fileService;

    public String stripManagedImageSources(String html) {
        return replaceManagedImageTags(html, this::stripSource);
    }

    public String resolveManagedImageSources(String html) {
        return replaceManagedImageTags(html, this::resolveSource);
    }

    private String replaceManagedImageTags(String html, ImageTagReplacer replacer) {
        if (html == null || html.isBlank()) {
            return html;
        }
        var matcher = IMG_TAG_PATTERN.matcher(html);
        var result = new StringBuilder();
        while (matcher.find()) {
            var tag = matcher.group();
            var fileId = parseFileId(tag);
            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(fileId == null ? tag : replacer.replace(tag, fileId)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Long parseFileId(String tag) {
        var matcher = DATA_FILE_ID_PATTERN.matcher(tag);
        if (!matcher.find()) {
            return null;
        }
        return Long.valueOf(matcher.group(2));
    }

    private String stripSource(String tag, Long fileId) {
        return SRC_PATTERN.matcher(tag).replaceFirst("");
    }

    private String resolveSource(String tag, Long fileId) {
        var url = fileService.getFileUrlById(fileId);
        if (url == null || url.isBlank()) {
            return tag;
        }
        var src = " src=\"" + url + "\"";
        var matcher = SRC_PATTERN.matcher(tag);
        if (matcher.find()) {
            return matcher.replaceFirst(Matcher.quoteReplacement(src));
        }
        return tag.replaceFirst("\\s*/?>$", Matcher.quoteReplacement(src) + ">");
    }

    @FunctionalInterface
    private interface ImageTagReplacer {
        String replace(String tag, Long fileId);
    }
}
