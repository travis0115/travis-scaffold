package com.travis.infrastructure.framework.web.core.xss;

import lombok.AllArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/** 仅处理标注了 {@code @SanitizeHtml} 的字符串字段。 */
@AllArgsConstructor
public class SanitizeHtmlDeserializer extends StringDeserializer {

    private final HtmlSanitizer htmlSanitizer;

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        return htmlSanitizer.sanitize(super.deserialize(parser, context));
    }
}
