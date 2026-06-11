package com.travis.infrastructure.framework.web.core.xss;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/** 基于 Jsoup 的富文本白名单清洗器。 */
public class JsoupHtmlSanitizer implements HtmlSanitizer {

    private final Safelist safelist = buildSafelist();

    @Override
    public String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, "", safelist, new Document.OutputSettings().prettyPrint(false));
    }

    private Safelist buildSafelist() {
        return Safelist.none()
                .addTags(
                        "p",
                        "br",
                        "strong",
                        "b",
                        "em",
                        "i",
                        "u",
                        "s",
                        "ul",
                        "ol",
                        "li",
                        "blockquote",
                        "pre",
                        "code",
                        "h1",
                        "h2",
                        "h3",
                        "h4",
                        "h5",
                        "h6",
                        "a",
                        "img",
                        "table",
                        "thead",
                        "tbody",
                        "tr",
                        "th",
                        "td")
                .addAttributes("a", "href", "title", "target")
                .addAttributes("img", "src", "alt", "title", "width", "height")
                .addAttributes("th", "colspan", "rowspan")
                .addAttributes("td", "colspan", "rowspan")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https")
                .addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow");
    }
}
