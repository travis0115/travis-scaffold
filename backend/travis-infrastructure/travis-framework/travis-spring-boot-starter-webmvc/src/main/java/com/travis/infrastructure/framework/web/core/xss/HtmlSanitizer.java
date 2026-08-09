package com.travis.infrastructure.framework.web.core.xss;

import org.jsoup.Jsoup;

/** HTML 白名单清洗器。 */
public interface HtmlSanitizer {

    String sanitize(String html);

    /** 判断富文本是否包含可见文字或图片。 */
    static boolean hasContent(String html) {
        if (html == null || html.isBlank()) {
            return false;
        }
        var body = Jsoup.parseBodyFragment(html).body();
        return !body.text().isBlank() || !body.select("img").isEmpty();
    }
}
