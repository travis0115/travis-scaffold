package com.travis.infrastructure.framework.web.core.xss;

/** HTML 白名单清洗器。 */
public interface HtmlSanitizer {

    String sanitize(String html);
}
