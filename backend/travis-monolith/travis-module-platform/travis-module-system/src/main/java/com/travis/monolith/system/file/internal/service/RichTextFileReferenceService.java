package com.travis.monolith.system.file.internal.service;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.ManagedFileReferenceParser;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 富文本中的系统文件图片引用处理 */
@Service
@RequiredArgsConstructor
public class RichTextFileReferenceService {

    /** 匹配富文本中的图片标签。 */
    private static final Pattern IMG_TAG_PATTERN =
            Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);

    /** 匹配系统文件图片标签中的文件 ID 属性。 */
    private static final Pattern DATA_FILE_ID_PATTERN =
            Pattern.compile("\\sdata-file-id\\s*=\\s*([\"'])(\\d+)\\1", Pattern.CASE_INSENSITIVE);

    /** 匹配图片标签中的 src 属性。 */
    private static final Pattern SRC_PATTERN =
            Pattern.compile("\\ssrc\\s*=\\s*([\"']).*?\\1", Pattern.CASE_INSENSITIVE);

    private final SysFileService fileService;

    /** 移除系统文件图片的临时访问地址，仅保留文件 ID 引用。 */
    public String stripManagedImageSources(String html) {
        var fileIds = ManagedFileReferenceParser.extractFileIds(html);
        if (fileService.getFileUrlMapByIds(fileIds).size() != fileIds.size()) {
            throw new BizException(SystemErrorCode.FILE_NOT_FOUND);
        }
        return replaceManagedImageTags(html, this::stripSource);
    }

    /** 根据文件 ID 将系统文件图片补充为当前可访问地址。 */
    public String resolveManagedImageSources(String html) {
        return resolveManagedImageSources(Collections.singletonList(html)).getFirst();
    }

    /** 批量补充富文本中的文件访问地址，全部内容共用一次文件查询。 */
    public List<String> resolveManagedImageSources(List<String> htmlList) {
        if (htmlList == null || htmlList.isEmpty()) {
            return List.of();
        }
        var fileIds = new LinkedHashSet<Long>();
        htmlList.forEach(html -> fileIds.addAll(ManagedFileReferenceParser.extractFileIds(html)));
        var urls = fileService.getFileUrlMapByIds(fileIds);
        return htmlList.stream()
                .map(
                        html ->
                                replaceManagedImageTags(
                                        html, (tag, fileId) -> resolveSource(tag, fileId, urls)))
                .toList();
    }

    /** 遍历包含系统文件 ID 的图片标签并执行替换。 */
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

    /** 从图片标签中解析系统文件 ID。 */
    private Long parseFileId(String tag) {
        var matcher = DATA_FILE_ID_PATTERN.matcher(tag);
        if (!matcher.find()) {
            return null;
        }
        return Long.valueOf(matcher.group(2));
    }

    /** 移除图片标签中的 src 属性。 */
    private String stripSource(String tag, Long fileId) {
        var tagWithoutSource = SRC_PATTERN.matcher(tag).replaceFirst("");
        return DATA_FILE_ID_PATTERN
                .matcher(tagWithoutSource)
                .replaceFirst(Matcher.quoteReplacement(" data-file-id=\"" + fileId + "\""));
    }

    /** 将图片标签中的 src 属性替换为文件当前访问地址。 */
    private String resolveSource(String tag, Long fileId, Map<Long, String> urls) {
        var url = urls.get(fileId);
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
