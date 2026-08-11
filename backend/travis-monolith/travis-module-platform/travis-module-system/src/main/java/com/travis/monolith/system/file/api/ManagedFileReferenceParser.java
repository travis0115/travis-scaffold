package com.travis.monolith.system.file.api;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** 系统文件富文本引用解析器。 */
public final class ManagedFileReferenceParser {

    private static final Pattern IMG_TAG_PATTERN =
            Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATA_FILE_ID_PATTERN =
            Pattern.compile("\\sdata-file-id\\s*=\\s*([\"'])(\\d+)\\1", Pattern.CASE_INSENSITIVE);

    private ManagedFileReferenceParser() {}

    /** 提取富文本中全部系统文件 ID。 */
    public static Set<Long> extractFileIds(String html) {
        var fileIds = new LinkedHashSet<Long>();
        if (html == null || html.isBlank()) {
            return fileIds;
        }
        var imageMatcher = IMG_TAG_PATTERN.matcher(html);
        while (imageMatcher.find()) {
            var idMatcher = DATA_FILE_ID_PATTERN.matcher(imageMatcher.group());
            if (idMatcher.find()) {
                fileIds.add(Long.valueOf(idMatcher.group(2)));
            }
        }
        return fileIds;
    }

    /** 判断富文本是否引用指定系统文件。 */
    public static boolean containsFileId(String html, Long fileId) {
        return fileId != null && extractFileIds(html).contains(fileId);
    }
}
